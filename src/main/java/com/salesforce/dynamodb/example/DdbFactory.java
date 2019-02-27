package com.salesforce.dynamodb.example;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.salesforce.dynamodb.example.impl.Context;
import com.salesforce.dynamodbv2.mt.mappers.MtAmazonDynamoDbByTable;
import com.salesforce.dynamodbv2.mt.mappers.sharedtable.SharedTableBuilder;

/**
 * Factory for Multi-tenant DynamoDB instances.
 */
public class DdbFactory {

    private enum Type {
        SEPARATE_TABLES,
        SHARED_TABLE,
    }

    private final AmazonDynamoDB ddb;
    private final Context context;

    /**
     * @param ddb a single-tenant dynamodb client
     * @param context the tenant context
     */
    public DdbFactory(AmazonDynamoDB ddb, Context context) {
        this.ddb = ddb;
        this.context = context;
    }

    private static final Type TYPE = Type.SEPARATE_TABLES;

    /**
     * Creates a multi-tenant dynamodb client.
     */
    public AmazonDynamoDB create() {
        switch (TYPE) {
        case SEPARATE_TABLES:
            return separateTables();

        case SHARED_TABLE:
            return sharedTable();

        default:
            throw new IllegalStateException();
        }
    }

    /**
     * @return a multi-tenant dynamodb client that uses separate
     *         tables in a shared account
     */
    private AmazonDynamoDB separateTables() {
        return MtAmazonDynamoDbByTable.builder()
                .withAmazonDynamoDb(ddb)
                .withContext(context)
                .build();
    }

    /**
     * @return a multi-tenant dynamodb client that uses a single
     *         shared table
     */
    private AmazonDynamoDB sharedTable() {
        return SharedTableBuilder.builder()
                .withDefaultProvisionedThroughput(5L)
                .withAmazonDynamoDb(ddb)
                .withContext(context)
                .build();
    }
}
