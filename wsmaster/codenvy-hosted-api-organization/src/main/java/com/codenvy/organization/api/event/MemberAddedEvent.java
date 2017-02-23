/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.organization.api.event;

import com.codenvy.organization.shared.event.EventType;
import com.codenvy.organization.shared.event.OrganizationEvent;
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.model.user.User;

import java.util.Objects;

import static com.codenvy.organization.shared.event.EventType.MEMBER_ADDED;

/**
 * Defines the event of adding the organization member.
 *
 * @author Anton Korneta
 */
public class MemberAddedEvent implements OrganizationEvent {

    private final String       performerName;
    private final User         addedUser;
    private final Organization organization;

    public MemberAddedEvent(String performerName,
                            User addedUser,
                            Organization organization) {
        this.performerName = performerName;
        this.addedUser = addedUser;
        this.organization = organization;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    @Override
    public EventType getType() {
        return MEMBER_ADDED;
    }

    public String getPerformerName() {
        return performerName;
    }

    public User getAddedUser() {
        return addedUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemberAddedEvent)) {
            return false;
        }
        final MemberAddedEvent that = (MemberAddedEvent)obj;
        return Objects.equals(performerName, that.performerName)
               && Objects.equals(addedUser, that.addedUser)
               && Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(performerName);
        hash = 31 * hash + Objects.hashCode(addedUser);
        hash = 31 * hash + Objects.hashCode(organization);
        return hash;
    }

    @Override
    public String toString() {
        return "MemberAddedEvent{" +
               "performerName='" + performerName + '\'' +
               ", eventType='" + getType() + '\'' +
               ", addedUser=" + addedUser +
               ", organization=" + organization +
               '}';
    }

}
