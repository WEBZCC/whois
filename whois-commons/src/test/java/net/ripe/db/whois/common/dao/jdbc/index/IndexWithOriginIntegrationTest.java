package net.ripe.db.whois.common.dao.jdbc.index;


import net.ripe.db.whois.common.dao.RpslObjectInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@org.junit.jupiter.api.Tag("IntegrationTest")
public class IndexWithOriginIntegrationTest extends IndexIntegrationTestBase {
    private IndexStrategy subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = IndexStrategies.get(AttributeType.ORIGIN);
    }


    @Test
    public void find_by_origin_route() {
        databaseHelper.addObject("" +
                "route:           180.0/8\n" +
                "descr:           Less specific other route\n" +
                "origin:          AS12726\n" +
                "source:          TEST\n");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS12726");

        assertThat(result, hasSize(1));
        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.ROUTE));
        assertThat(rpslObject.getKey(), is("180.0/8AS12726"));
    }

    @Test
    public void find_by_origin_route6() {
        databaseHelper.addObject("" +
                "route6:          9999::/16\n" +
                "descr:           Less specific other route\n" +
                "origin:          AS12726\n" +
                "source:          TEST\n");

        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS12726");

        assertThat(result, hasSize(1));
        RpslObjectInfo rpslObject = result.get(0);
        assertThat(rpslObject.getObjectType(), is(ObjectType.ROUTE6));
        assertThat(rpslObject.getKey(), is("9999::/16AS12726"));
    }

    @Test
    public void find_by_origin_not_found() {
        List<RpslObjectInfo> result = subject.findInIndex(whoisTemplate, "AS12726");

        assertThat(result, hasSize(0));
    }
}
