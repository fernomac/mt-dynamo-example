package com.salesforce.dynamodb.example.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * An item in the catalog.
 */
@DynamoDBTable(tableName = "catalog")
public class Item {

    private String id;
    private String type;
    private String name;
    private String description;

    @DynamoDBHashKey(attributeName = "id")
    public String getID() {
        return id;
    }

    @DynamoDBIndexHashKey(attributeName = "type", globalSecondaryIndexName = "type")
    public String getType() {
        return type;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    @DynamoDBAttribute(attributeName = "description")
    public String getDescription() {
        return description;
    }

    public void setID(String value) {
        id = value;
    }

    public Item withID(String value) {
        setID(value);
        return this;
    }

    public void setType(String value) {
        type = value;
    }

    public Item withType(String value) {
        setType(value);
        return this;
    }

    public void setName(String value) {
        name = value;
    }

    public Item withName(String value) {
        setName(value);
        return this;
    }

    public void setDescription(String value) {
        description = value;
    }

    public Item withDescription(String value) {
        setDescription(value);
        return this;
    }
}
