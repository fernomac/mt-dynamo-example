package com.salesforce.dynamodb.example.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.salesforce.dynamodb.example.impl.Catalog;
import com.salesforce.dynamodb.example.impl.Item;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Jersey REST endpoint for the catalog service.
 */
@Path("/api/v1/items")
public class CatalogEndpoint {

    private final Catalog catalog;

    public CatalogEndpoint(Catalog catalog) {
        this.catalog = catalog;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ItemList list(@QueryParam("type") String type) {
        if (type == null || type == "") {
            return new ItemList(catalog.listItems());
        } else {
            return new ItemList(catalog.getItemsByType(type));
        }
    }


    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ItemRep get(@PathParam("id") String id) {
        return new ItemRep(catalog.getItemByID(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PostRep post(ItemRep item) {
        return new PostRep(catalog.addItem(item.item));
    }

    public static class ItemList {

        private final List<ItemRep> items;

        public ItemList(List<Item> items) {
            List<ItemRep> reps = new ArrayList<>(items.size());
            for (Item i : items) {
                reps.add(new ItemRep(i));
            }
            this.items = reps;
        }

        @JsonProperty("items")
        public List<ItemRep> getItems() {
            return items;
        }
    }

    public static class ItemRep {

        private final Item item;

        public ItemRep() {
            this.item = new Item();
        }

        public ItemRep(Item item) {
            this.item = item;
        }

        @JsonProperty("id")
        public String getID() {
            return item.getID();
        }

        public void setID(String value) {
            item.setID(value);
        }

        @JsonProperty("type")
        public String getType() {
            return item.getType();
        }

        public void setType(String value) {
            item.setType(value);
        }

        @JsonProperty("name")
        public String getName() {
            return item.getName();
        }

        public void setName(String value) {
            item.setName(value);
        }

        @JsonProperty("desc")
        public String getDescription() {
            return item.getDescription();
        }

        public void setDescription(String value) {
            item.setDescription(value);
        }
    }

    public static class PostRep {

        private final String id;

        public PostRep(String id) {
            this.id = id;
        }

        @JsonProperty("id")
        public String getID() {
            return id;
        }
    }
}
