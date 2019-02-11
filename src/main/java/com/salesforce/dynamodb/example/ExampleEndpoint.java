package com.salesforce.dynamodb.example;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableNotFoundException;
import com.salesforce.dynamodbv2.mt.context.MtAmazonDynamoDbContextProvider;
import com.salesforce.dynamodbv2.mt.context.impl.MtAmazonDynamoDbContextProviderImpl;
import com.salesforce.dynamodbv2.mt.mappers.sharedtable.SharedTableBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.Optional;

@Path("/")
public class ExampleEndpoint {

    private final MtAmazonDynamoDbContextProvider context;
    private final AmazonDynamoDBLocal local;
    private final AmazonDynamoDB ddb;

    public ExampleEndpoint() {
        context = new MtAmazonDynamoDbContextProviderImpl();

        System.setProperty("sqlite4java.library.path", "src/test/resources/bin");
        local = DynamoDBEmbedded.create();

        ddb = SharedTableBuilder.builder()
                .withDefaultProvisionedThroughput(5L)
                .withAmazonDynamoDb(local.amazonDynamoDB())
                .withContext(context)
                .build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello!";
    }

    @GET
    @Path("admin")
    @Produces(MediaType.TEXT_HTML)
    public String scan() {
        StringBuilder out = new StringBuilder();
        out.append("<html><head><style>");
        out.append(".main { position: relative; margin-left: auto; margin-right: auto; text-align: center; } ");
        out.append("table { width: 80%; border: 1px solid black; display: inline-block; } ");
        out.append("th, td { border-bottom: 1px solid #ddd; }");
        out.append("</style></head><body><div class='main'>");

        for (String table : local.amazonDynamoDB().listTables().getTableNames()) {
            appendTableContentsTo(local.amazonDynamoDB(), table, out);
        }

        out.append("</div></body></html>");
        return out.toString();
    }

    @GET
    @Path("tenants/{id}")
    public String tenant(@PathParam("id") String id) {
        context.setContext(id);

        try {
            ddb.describeTable("MyPersonalTable");
        } catch (ResourceNotFoundException _) {
            ddb.createTable(new CreateTableRequest()
                    .withTableName("MyPersonalTable")
                    .withKeySchema(new KeySchemaElement()
                            .withAttributeName("Key")
                            .withKeyType("HASH"))
                    .withAttributeDefinitions(new AttributeDefinition()
                            .withAttributeName("Key")
                            .withAttributeType("S"))
                    .withProvisionedThroughput(new ProvisionedThroughput()
                            .withReadCapacityUnits(5L)
                            .withWriteCapacityUnits(5L)));
        }

        StringBuilder out = new StringBuilder();
        out.append("<html><head><style>");
        out.append(".main { position: relative; margin-left: auto; margin-right: auto; text-align: center; } ");
        out.append("table { width: 80%; border: 1px solid black; display: inline-block; } ");
        out.append("th, td { border-bottom: 1px solid #ddd; }");
        out.append("</style></head><body><div class='main'>");

        appendTableContentsTo(ddb, "MyPersonalTable", out);

        out.append("<form action='/tenants/").append(id).append("' method='post'>");
        out.append("<input type='text' name='key'> ");
        out.append("<input type='text' name='value'> ");
        out.append("<input type='submit' value='Submit'>");
        out.append("</form>");

        out.append("</div></body></html>");
        return out.toString();
    }

    @POST
    @Path("tenants/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @PathParam("id") String id,
            @FormParam("key") String key,
            @FormParam("value") String value) {

        context.setContext(id);

        ddb.putItem(new PutItemRequest()
                .withTableName("MyPersonalTable")
                .addItemEntry("Key", new AttributeValue().withS(key))
                .addItemEntry("Value", new AttributeValue().withS(value)));

        return Response.seeOther(UriBuilder.fromPath("/tenants").path(id).build()).build();
    }


    private void appendTableContentsTo(AmazonDynamoDB ddb, String table, StringBuilder out) {
        out.append("<table>");
        out.append("<tr><th>").append(table).append("</th></tr>");

        ScanResult result = ddb.scan(new ScanRequest().withTableName(table));
        for (Map<String, AttributeValue> item : result.getItems()) {
            out.append("<tr><td>").append(item.toString()).append("</td></tr>");
        }

        if (result.getLastEvaluatedKey() != null) {
            out.append("<tr><td>...</td></tr>");
        }

        out.append("</table>");
        out.append("<br/>&nbsp;<br/>");
    }

}
