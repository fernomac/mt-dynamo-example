package com.salesforce.dynamodb.example.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * A product catalog stored in DynamoDB.
 */
public class Catalog {

    private final AmazonDynamoDB ddb;
    private final DynamoDBMapper mapper;

    public Catalog(AmazonDynamoDB ddb) {
        this.ddb = ddb;
        this.mapper = new DynamoDBMapper(ddb, DynamoDBMapperConfig.builder()
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.PUT)
                .build());
    }

    /**
     * Initialize the catalog table if it does not yet exist.
     */
    public void init() {
        try {
            ddb.describeTable("catalog");
        } catch (ResourceNotFoundException _) {
            createTable();
        }
    }

    private void createTable() {
        KeySchemaElement id_kse = new KeySchemaElement().withAttributeName("id").withKeyType("HASH");
        KeySchemaElement type_kse = new KeySchemaElement().withAttributeName("type").withKeyType("HASH");

        AttributeDefinition id_ad = new AttributeDefinition().withAttributeName("id").withAttributeType("S");
        AttributeDefinition type_ad = new AttributeDefinition().withAttributeName("type").withAttributeType("S");

        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex()
                .withIndexName("type")
                .withKeySchema(type_kse)
                .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L));

        ddb.createTable(new CreateTableRequest()
                .withTableName("catalog")
                .withKeySchema(id_kse)
                .withGlobalSecondaryIndexes(gsi)
                .withAttributeDefinitions(id_ad, type_ad)
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L)));
    }

    /**
     * Gets an item from the catalog by ID.
     *
     * @param id the id of the item to fetch
     * @return the item, or null if not found
     */
    public Item getItemByID(String id) {
        return mapper.load(Item.class, id);
    }

    /**
     * Lists all items of the given type from the catalog.
     *
     * @param type the type of item to fetch
     * @return all items of the given type
     */
    public List<Item> getItemsByType(String type) {
        Item key = new Item();
        key.setType(type);

        return mapper.query(Item.class, new DynamoDBQueryExpression<Item>()
                .withIndexName("type")
                .withHashKeyValues(key)
                .withConsistentRead(false));
    }

    /**
     * Lists all items in the catalog.
     *
     * @return a list of items.
     */
    public List<Item> listItems() {
        return mapper.scan(Item.class, new DynamoDBScanExpression()
                .withConsistentRead(false));
    }

    /**
     * Adds an item to the catalog, assigning it an id.
     *
     * @param item the item
     * @return the id assigned to the item
     */
    public String addItem(Item item) {
        if (item.getType() == null) {
            throw new IllegalArgumentException("no type");
        }
        if (item.getName() == null) {
            throw new IllegalArgumentException("no name");
        }
        if (item.getDescription() == null) {
            throw new IllegalArgumentException("no description");
        }

        String id = UUID.randomUUID().toString();
        item.setID(id);

        mapper.save(item);

        return id;
    }
}
