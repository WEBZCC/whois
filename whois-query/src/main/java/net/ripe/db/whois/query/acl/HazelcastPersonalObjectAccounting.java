package net.ripe.db.whois.query.acl;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.OperationTimeoutException;
import net.ripe.db.whois.common.profiles.DeployedProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DeployedProfile
@Primary
@Component
public class HazelcastPersonalObjectAccounting implements PersonalObjectAccounting {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastPersonalObjectAccounting.class);

    private static IMap<InetAddress, Integer> counterMap;

    private static volatile HazelcastInstance instance;

    static synchronized void startHazelcast() {
        if (instance != null) {
            throw new IllegalStateException("Hazelcast already started");
        }

        instance = Hazelcast.newHazelcastInstance(null);

        //TODO: use  efs mount for hazelcast config, instead of java config. This is just for testing
        instance.getConfig().getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        instance.getConfig().getNetworkConfig().getJoin().getAwsConfig().setEnabled(true);
        instance.getConfig().getNetworkConfig().getInterfaces().setEnabled(true).addInterface("10.231.*.*");

        instance.getCluster().addMembershipListener(new HazelcastMemberShipListner());

        LOGGER.info("hazelcast instances : " + instance.getName() +  " members: " + instance.getCluster().getMembers());
        LOGGER.info("hazelcast instances interfaces  : " + instance.getConfig().getNetworkConfig().getInterfaces());

        counterMap = instance.getMap("queriedPersonal");
    }

    static void shutdownHazelcast() {
        LOGGER.debug("Shutting down hazelcast instance");

        instance.getLifecycleService().shutdown();
        instance = null;
    }

    @PostConstruct
    public void startService() {
        startHazelcast();
    }

    @PreDestroy
    public void stopService() {
        shutdownHazelcast();
    }

    @Override
    public int getQueriedPersonalObjects(final InetAddress remoteAddress) {
        Integer count = null;
        try {
            count = counterMap.get(remoteAddress);
        } catch (OperationTimeoutException | IllegalStateException e) {
            // no answer from hazelcast, expected, don't rethrow
            LOGGER.debug("{}: {}", e.getClass().getName(), e.getMessage());
        }

        if (count == null) {
            return 0;
        }

        return count;
    }

    @Override
    public int accountPersonalObject(final InetAddress remoteAddress, final int amount) {
        try {
            Integer count = counterMap.tryLockAndGet(remoteAddress, 3, TimeUnit.SECONDS);

            if (count == null) {
                count = amount;
            } else {
                count += amount;
            }

            counterMap.putAndUnlock(remoteAddress, count);
            return count;
        } catch (TimeoutException | IllegalStateException e) {
            LOGGER.info("Unable to account personal object, allowed by default. Threw {}: {}", e.getClass().getName(), e.getMessage());
        }

        return 0;
    }

    @Override
    public void resetAccounting() {
        LOGGER.debug("Reset person object counters ({} entries)", counterMap.size());
        counterMap.clear();
    }
}
