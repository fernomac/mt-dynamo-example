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
        SHARED_ACCOUNT,
        SHARED_TABLE,
    }

    private static final Type TYPE = Type.SHARED_TABLE;

    private final AmazonDynamoDB ddb;
    private final Context context;

    public DdbFactory(AmazonDynamoDB ddb, Context context) {
        this.ddb = ddb;
        this.context = context;
    }

    public AmazonDynamoDB create() {
        switch (TYPE) {
        case SHARED_ACCOUNT:
            return sharedAccount();

        case SHARED_TABLE:
            return sharedTable();

        default:
            throw new IllegalStateException();
        }
    }

    public AmazonDynamoDB sharedAccount() {
        return MtAmazonDynamoDbByTable.builder()
                .withAmazonDynamoDb(ddb)
                .withContext(context)
                .build();
    }

    public AmazonDynamoDB sharedTable() {
        return SharedTableBuilder.builder()
                .withDefaultProvisionedThroughput(5L)
                .withAmazonDynamoDb(ddb)
                .withContext(context)
                .build();
    }
}
