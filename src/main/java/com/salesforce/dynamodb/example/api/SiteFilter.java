package com.salesforce.dynamodb.example.api;

import com.salesforce.dynamodb.example.impl.Context;
import com.salesforce.dynamodbv2.mt.context.MtAmazonDynamoDbContextProvider;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Map;

/**
 * A request filter that figures out which site the user is browsing and adds it to
 * the context.
 */
@Provider
public class SiteFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final Context context;

    /**
     * @param context the context to set
     */
    public SiteFilter(Context context) {
        this.context = context;
    }

    @Override
    public void filter(ContainerRequestContext req) {
        Map<String, Cookie> cookies = req.getCookies();
        if (cookies == null) {
            return;
        }

        Cookie site = cookies.get("site");
        if (site == null) {
            return;
        }

        context.setContext(site.getValue());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) {
        context.removeContext();
    }
}
