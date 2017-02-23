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

/**
 * Defines the event for organization member removal.
 *
 * @author Anton Korneta
 */
public class MemberRemovedEvent implements OrganizationEvent {

    private final String       performerName;
    private final User         removedUser;
    private final Organization organization;

    public MemberRemovedEvent(String performerName,
                              User removedUser,
                              Organization organization) {
        this.performerName = performerName;
        this.removedUser = removedUser;
        this.organization = organization;
    }

    @Override
    public EventType getType() {
        return EventType.MEMBER_REMOVED;
    }

    @Override
    public Organization getOrganization() {
        return organization;
    }

    public String getPerformerName() {
        return performerName;
    }

    public User getRemovedUser() {
        return removedUser;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MemberRemovedEvent)) {
            return false;
        }
        final MemberRemovedEvent that = (MemberRemovedEvent)obj;
        return Objects.equals(performerName, that.performerName)
               && Objects.equals(removedUser, that.removedUser)
               && Objects.equals(organization, that.organization);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(performerName);
        hash = 31 * hash + Objects.hashCode(removedUser);
        hash = 31 * hash + Objects.hashCode(organization);
        return hash;
    }

    @Override
    public String toString() {
        return "MemberRemovedEvent{" +
               "performerName='" + performerName + '\'' +
               ", eventType='" + getType() + '\'' +
               ", removedUser=" + removedUser +
               ", organization=" + organization +
               '}';
    }
}
