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
package com.codenvy.organization.shared.dto;

import com.codenvy.organization.shared.event.EventType;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for organization member removed events.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationMemberRemovedEventDto extends OrganizationEventDto {

    @Override
    OrganizationMemberRemovedEventDto withOrganizationId(String organizationId);

    @Override
    OrganizationMemberRemovedEventDto withType(EventType eventType);

    /** Returns the manager of the organization that removed the organization member */
    String getOrganizationManagerName();

    void setOrganizationManagerName(String organizationManagerName);

    OrganizationMemberRemovedEventDto withOrganizationManagerName(String organizationManagerName);

    /** Returns removed organization member */
    String getRemovedUserId();

    void setRemovedUserId(String removedUserId);

    OrganizationMemberRemovedEventDto withRemovedUserId(String removedUserId);

}
