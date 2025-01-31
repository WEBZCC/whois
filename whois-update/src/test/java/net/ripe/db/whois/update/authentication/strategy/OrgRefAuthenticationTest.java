package net.ripe.db.whois.update.authentication.strategy;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.credential.AuthenticationModule;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static net.ripe.db.whois.common.domain.CIString.ciSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrgRefAuthenticationTest {
    @Mock private PreparedUpdate update;
    @Mock private UpdateContext updateContext;
    @Mock private AuthenticationModule credentialValidators;
    @Mock private RpslObjectDao rpslObjectDao;

    @InjectMocks private OrgRefAuthentication subject;

    @Test
    public void supports_update_with_new_org_references() {
        when(update.getNewValues(AttributeType.ORG)).thenReturn(ciSet("ORG2"));

        assertThat(subject.supports(update), is(true));
    }

    @Test
    public void no_difference_in_org_refs_is_not_supported() {
        assertThat(subject.supports(update), is(false));
    }

    @Test
    public void authentication_succeeds() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);
        final RpslObject organisation = RpslObject.parse("organisation: ORG1\nmnt-ref: REF-MNT");
        final List<RpslObject> organisations = Lists.newArrayList(organisation);
        when(rpslObjectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn(organisations);

        final RpslObject maintainer = RpslObject.parse("mntner: REF-MNT");
        when(rpslObjectDao.getByKey(ObjectType.MNTNER, "REF-MNT")).thenReturn(maintainer);

        final ArrayList<RpslObject> candidates = Lists.newArrayList(maintainer);
        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList(), eq(OrgRefAuthentication.class))).thenReturn(candidates);

        final List<RpslObject> result = subject.authenticate(update, updateContext);

        assertThat(result.size(), is(1));
        assertThat(result.get(0), is(maintainer));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void no_mntnerref_found() {
        when(update.getType()).thenReturn(ObjectType.PERSON);

        final List<RpslObject> organisations = Lists.newArrayList(RpslObject.parse("organisation: ORG2"));
        when(rpslObjectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn((organisations));

        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList(), eq(OrgRefAuthentication.class))).thenReturn(emptyList());

        Assertions.assertThrows(AuthenticationFailedException.class, () -> {
            subject.authenticate(update, updateContext);
        });
    }

    @Test
    public void mntnerref_does_not_exist() {
        when(update.getType()).thenReturn(ObjectType.INETNUM);

        final List<RpslObject> organisations = Lists.newArrayList(RpslObject.parse("organisation: ORG1\nmnt-ref: REF-MNT"));
        when(rpslObjectDao.getByKeys(eq(ObjectType.ORGANISATION), anyCollection())).thenReturn(organisations);

        when(rpslObjectDao.getByKey(ObjectType.MNTNER, "REF-MNT")).thenThrow(EmptyResultDataAccessException.class);

        when(credentialValidators.authenticate(eq(update), eq(updateContext), anyList(), eq(OrgRefAuthentication.class))).thenReturn(emptyList());

        Assertions.assertThrows(AuthenticationFailedException.class, () -> {
            subject.authenticate(update, updateContext);
        });
    }
}
