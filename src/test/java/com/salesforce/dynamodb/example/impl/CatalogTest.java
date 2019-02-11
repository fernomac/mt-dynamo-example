package com.salesforce.dynamodb.example.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.salesforce.dynamodbv2.mt.mappers.MtAmazonDynamoDbByTable;
import com.salesforce.dynamodbv2.mt.mappers.sharedtable.SharedTableBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

/**
 * Basic unit tests of the catalog class.
 */
public class CatalogTest {

    private AmazonDynamoDBLocal local;

    @BeforeEach
    public void setUp() {
        System.setProperty("sqlite4java.library.path", "src/test/resources/bin");
        local = DynamoDBEmbedded.create();
    }

    @AfterEach
    public void cleanUp() {
        local.shutdown();
    }

    @Test
    public void testCatalog() {
        testIt(new Catalog(local.amazonDynamoDB()));
    }

    @Test
    public void testMtByTableCatalog() {
        Context context = new Context();

        AmazonDynamoDB ddb = MtAmazonDynamoDbByTable.builder()
                .withAmazonDynamoDb(local.amazonDynamoDB())
                .withContext(context)
                .build();

        Catalog catalog = new Catalog(ddb);

        context.setContext("abc123");
        testIt(catalog);

        context.setContext("def456");
        testIt(catalog);
    }

    @Test
    public void testMtSharedTableCatalog() {
        Context context = new Context();

        AmazonDynamoDB ddb = SharedTableBuilder.builder()
                .withDefaultProvisionedThroughput(5L)
                .withAmazonDynamoDb(local.amazonDynamoDB())
                .withContext(context)
                .build();

        Catalog catalog = new Catalog(ddb);

        context.setContext("abc123");
        testIt(catalog);

        context.setContext("def456");
        testIt(catalog);
    }

    private void testIt(Catalog catalog) {
        catalog.init();

        String af1s = catalog.addItem(new Item()
                .withType("shoes")
                .withName("air force ones")
                .withDescription("they got me stompin'"));

        String docs = catalog.addItem(new Item()
                .withType("shoes")
                .withName("doc martins")
                .withDescription("sum boots"));

        Assertions.assertNull(catalog.getItemByID("bogus"));

        Assertions.assertEquals("shoes", catalog.getItemByID(af1s).getType());
        Assertions.assertEquals("air force ones", catalog.getItemByID(af1s).getName());
        Assertions.assertEquals("sum boots", catalog.getItemByID(docs).getDescription());

        Assertions.assertEquals(Collections.emptyList(), catalog.getItemsByType("bogus"));

        List<Item> items = catalog.getItemsByType("shoes");
        Assertions.assertEquals(2, items.size());
    }
}
