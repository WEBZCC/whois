package net.ripe.db.whois.update.handler.validator.organisation;

import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_ADDRESS;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_EMAIL;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_FAX;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_ORG_NAME;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_ORG_NAME_CASE_SENSITIVE;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.LIR_ORG_PHONE;
import static net.ripe.db.whois.update.handler.validator.organisation.LirAttributeValidatorFixtures.NON_LIR_ORG;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LirUserMaintainedAttributesValidatorTest {

    @Mock PreparedUpdate update;
    @Mock UpdateContext updateContext;
    @Mock Subject authenticationSubject;

    @InjectMocks LirUserMaintainedAttributesValidator subject;

    @BeforeEach
    public void setup() {
        lenient().when(updateContext.getSubject(any(UpdateContainer.class))).thenReturn(authenticationSubject);
    }

    @Test
    public void getActions() {
        assertThat(subject.getActions().size(), is(1));
        assertTrue(subject.getActions().contains(Action.MODIFY));
    }

    @Test
    public void getTypes() {
        assertThat(subject.getTypes().size(), is(1));
        assertTrue(subject.getTypes().contains(ObjectType.ORGANISATION));
    }

    @Test
    public void update_of_non_lir() {
        when(update.getReferenceObject()).thenReturn(NON_LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_address() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ADDRESS);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.ADDRESS));
        verifyNoMoreInteractions(update);
    }

    @Test
    public void update_of_phone() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_PHONE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.PHONE));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_fax() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_FAX);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.FAX_NO));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_email() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_EMAIL);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.E_MAIL));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_name() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.ORG_NAME));
        verifyNoMoreInteractions(updateContext);
    }

    @Test
    public void update_of_org_name_case_sensitive() {
        when(update.getReferenceObject()).thenReturn(LIR_ORG);
        when(authenticationSubject.hasPrincipal(Principal.OVERRIDE_MAINTAINER)).thenReturn(false);
        when(authenticationSubject.hasPrincipal(Principal.ALLOC_MAINTAINER)).thenReturn(false);
        when(update.getUpdatedObject()).thenReturn(LIR_ORG_ORG_NAME_CASE_SENSITIVE);

        subject.validate(update, updateContext);

        verify(updateContext).getSubject(update);
        verify(update).getReferenceObject();
        verify(update).getUpdatedObject();
        verify(updateContext).addMessage(update, UpdateMessages.canOnlyBeChangedinLirPortal(AttributeType.ORG_NAME));
        verifyNoMoreInteractions(updateContext);
    }
}
