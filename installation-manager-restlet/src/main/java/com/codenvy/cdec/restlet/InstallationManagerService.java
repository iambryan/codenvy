/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.cdec.restlet;

import com.codenvy.cdec.user.UserCredentials;

import org.restlet.ext.jackson.JacksonRepresentation;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author Dmytro Nochevnov
 */
@Path("im")
public interface InstallationManagerService extends DigestAuthSupport {

    /** Download all latest updates */
    @POST
    @Path("download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String download(JacksonRepresentation<UserCredentials> userCredentialsRep);
    
    @POST
    @Path("download/{artifact}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String download(@PathParam(value = "artifact") final String artifactName,
                           JacksonRepresentation<UserCredentials> userCredentialsRep);

    @POST
    @Path("download/{artifact}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String download(@PathParam(value = "artifact") final String artifactName,
                           @PathParam(value = "version") final String version,
                           JacksonRepresentation<UserCredentials> requestRepresentation);

    @POST
    @Path("check-updates")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String getUpdates(JacksonRepresentation<UserCredentials> requestRepresentation);

    /** Install all latest updates. */
    @POST
    @Path("install")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String install(JacksonRepresentation<UserCredentials> userCredentialsRep) throws IOException;

    @POST
    @Path("install/{artifact}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String install(@PathParam(value = "artifact") final String artifactName,
                          JacksonRepresentation<UserCredentials> userCredentialsRep) throws IOException;

    @POST
    @Path("install/{artifact}/{version}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String install(@PathParam(value = "artifact") final String artifactName,
                          @PathParam(value = "version") final String version,
                          JacksonRepresentation<UserCredentials> userCredentialsRep) throws IOException;

    /** Get the url of the update server. */
    @GET
    @Path("update-server-url")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUpdateServerUrl();
}
