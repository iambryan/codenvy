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
import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for organization removed events.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationRemovedEventDto extends OrganizationEventDto {

    @Override
    OrganizationRemovedEventDto withOrganizationId(String organizationId);

    @Override
    OrganizationRemovedEventDto withType(EventType eventType);

    /** Returns the name of the user that performed the removal of the organization */
    String getPerformerName();

    void setPerformerName(String performerName);

    OrganizationRemovedEventDto withPerformerName(String performerName);

    /** Returns removed organization */
    OrganizationDto getOrganization();

    void setOrganization(OrganizationDto organization);

    OrganizationRemovedEventDto withOrganization(OrganizationDto organization);

}
