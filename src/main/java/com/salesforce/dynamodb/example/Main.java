package com.salesforce.dynamodb.example;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.local.shared.access.AmazonDynamoDBLocal;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesforce.dynamodb.example.api.AdminEndpoint;
import com.salesforce.dynamodb.example.api.CatalogEndpoint;
import com.salesforce.dynamodb.example.api.SiteEndpoint;
import com.salesforce.dynamodb.example.api.SiteFilter;
import com.salesforce.dynamodb.example.impl.Admin;
import com.salesforce.dynamodb.example.impl.Catalog;
import com.salesforce.dynamodb.example.impl.Context;
import com.salesforce.dynamodbv2.mt.mappers.sharedtable.SharedTableBuilder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Dependencies().server();
        server.start();
        server.dumpStdErr();
        server.join();
    }

    private static class Dependencies {

        private Context context;
        private JacksonJaxbJsonProvider jackson;
        private AmazonDynamoDBLocal localDdb;
        private AmazonDynamoDB ddb;
        private Catalog catalog;
        private Admin admin;
        private ResourceConfig config;
        private Server server;

        public Context context() {
            if (context == null) {
                context = new Context();
            }
            return context;
        }


        public JacksonJaxbJsonProvider jackson() {
            if (jackson == null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                JacksonJaxbJsonProvider json = new JacksonJaxbJsonProvider();
                json.setMapper(mapper);

                jackson = json;
            }
            return jackson;
        }

        public AmazonDynamoDBLocal localDdb() {
            if (localDdb == null) {
                System.setProperty("sqlite4java.library.path", "src/test/resources/bin");
                localDdb = DynamoDBEmbedded.create();
            }
            return localDdb;
        }

        public AmazonDynamoDB ddb() {
            if (ddb == null) {
                ddb = SharedTableBuilder.builder()
                        .withDefaultProvisionedThroughput(5L)
                        .withAmazonDynamoDb(localDdb().amazonDynamoDB())
                        .withContext(context())
                        .build();
            }
            return ddb;
        }

        public Catalog catalog() {
            if (catalog == null) {
                catalog = new Catalog(ddb());
            }
            return catalog;
        }

        public Admin admin() {
            if (admin == null) {
                admin = new Admin(catalog(), context());
                admin.init();
            }
            return admin;
        }


        public ResourceConfig config() {
            if (config == null) {
                config = new ResourceConfig();
                config.register(jackson());
                config.register(new SiteFilter(context()));
                config.register(new CatalogEndpoint(catalog()));
                config.register(new SiteEndpoint(catalog()));
                config.register(new AdminEndpoint(admin(), localDdb().amazonDynamoDB()));
            }
            return config;
        }

        public Server server() {
            if (server == null) {
                String portStr = System.getenv("PORT");
                int port = 8080;
                if (portStr != null) {
                    port = Integer.parseInt(portStr);
                }

                URI base = UriBuilder.fromUri("http://localhost/").port(port).build();

                server = JettyHttpContainerFactory.createServer(base, config());
            }
            return server;
        }
    }
}
