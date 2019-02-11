package com.salesforce.dynamodb.example.api;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.salesforce.dynamodb.example.impl.Admin;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/admin")
public class AdminEndpoint {

    private final Admin admin;
    private final AmazonDynamoDB ddb;

    public AdminEndpoint(Admin admin, AmazonDynamoDB ddb) {
        this.admin = admin;
        this.ddb = ddb;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String scan() {
        StringBuilder out = new StringBuilder();
        out.append("<html><head><style>");
        out.append(".main { position: relative; margin-left: auto; margin-right: auto; text-align: center; } ");
        out.append("table { width: 80%; border: 1px solid black; display: inline-block; } ");
        out.append("th, td { border-bottom: 1px solid #ddd; }");
        out.append("</style></head><body><div class='main'>");

        for (String table : ddb.listTables().getTableNames()) {
            appendTableContentsTo(table, out);
        }

        out.append("</div></body></html>");
        return out.toString();
    }

    private void appendTableContentsTo(String table, StringBuilder out) {
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

    @POST
    @Path("/sites/{id}")
    public void createCatalog(@PathParam("id") String id) {
        admin.createCatalog(id);
    }
}
