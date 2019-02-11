package com.salesforce.dynamodb.example.api;

import com.amazonaws.util.IOUtils;
import com.salesforce.dynamodb.example.impl.Catalog;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Jersey REST endpoint that serves up the e-commerce sites.
 */
@Path("/")
public class SiteEndpoint {

    private final Catalog catalog;

    public SiteEndpoint(Catalog catalog) {
        this.catalog = catalog;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response root(@CookieParam("site") String site) throws IOException {
        if (site == null || site.equals("")) {
            return Response.ok()
                    .entity(SiteEndpoint.class.getResourceAsStream("index.html"))
                    .build();
        }

        byte[] bytes = IOUtils.toByteArray(SiteEndpoint.class.getResourceAsStream("site.html"));
        String body = new String(bytes, StandardCharsets.UTF_8);

        body = body.replaceAll("\\$\\{SITE\\}", site);

        return Response.ok().entity(body).build();
    }

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_HTML)
    public Response login(@QueryParam("site") String site) {
        return Response.seeOther(UriBuilder.fromPath("/").build())
                .cookie(new NewCookie("site", site))
                .build();
    }

    @GET
    @Path("logout")
    @Produces(MediaType.TEXT_HTML)
    public Response logout() {
        return Response.seeOther(UriBuilder.fromPath("/").build())
                .cookie(new NewCookie("site", ""))
                .build();
    }
}
