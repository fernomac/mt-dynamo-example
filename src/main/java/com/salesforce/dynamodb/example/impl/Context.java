package com.salesforce.dynamodb.example.impl;

import com.salesforce.dynamodbv2.mt.context.MtAmazonDynamoDbContextProvider;

import java.util.Optional;

public class Context implements MtAmazonDynamoDbContextProvider {

    private final ThreadLocal<String> tenant = new ThreadLocal<>();

    @Override
    public Optional<String> getContextOpt() {
        return Optional.ofNullable(tenant.get());
    }

    @Override
    public void setContext(String tenantId) {
        tenant.set(tenantId);
    }

    public void removeContext() {
        tenant.remove();
    }
}
