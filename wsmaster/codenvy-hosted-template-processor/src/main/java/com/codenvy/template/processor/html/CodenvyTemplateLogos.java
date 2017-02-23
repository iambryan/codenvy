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
package com.codenvy.template.processor.html;

import com.google.common.collect.ImmutableMap;

/**
 * @author Anton Korneta
 */
public class CodenvyTemplateLogos {

    public static final ImmutableMap<String, String> LOGOS = ImmutableMap.<String, String>builder()
            .put("codenvy", "/email-templates/logo-codenvy-white.png")
            .put("codenvySmall", "/email-templates/196x196-white.png")
            .put("linkedin", "/email-templates/logo_social_linkedin.png")
            .put("facebook", "/email-templates/logo_social_facebook.png")
            .put("twitter", "/email-templates/logo_social_twitter.png")
            .put("medium", "/email-templates/logo_social_medium.png")
            .build();
}
