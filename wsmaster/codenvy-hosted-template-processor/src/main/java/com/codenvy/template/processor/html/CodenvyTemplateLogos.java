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
