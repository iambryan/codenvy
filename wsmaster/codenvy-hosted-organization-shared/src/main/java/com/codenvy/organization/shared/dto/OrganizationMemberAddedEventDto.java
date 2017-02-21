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
 * DTO for organization member added events.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationMemberAddedEventDto extends OrganizationEventDto {

    @Override
    OrganizationMemberAddedEventDto withOrganizationId(String organizationId);

    @Override
    OrganizationMemberAddedEventDto withType(EventType eventType);

    /** Returns username of organization member who invite */
    String getReferrer();

    void setReferrer(String referrer);

    OrganizationMemberAddedEventDto withReferrer(String referrer);

    /** Returns new organization member uid */
    String getAddedUserId();

    void setAddedUserId(String addedUserId);

    OrganizationMemberAddedEventDto withAddedUserId(String addedUserId);

}