package com.salesforce.dynamodb.example.impl;

public class Admin {

    private final Catalog catalog;
    private final Context context;

    public Admin(Catalog catalog, Context context) {
        this.catalog = catalog;
        this.context = context;
    }

    public void init() {
        doFor("abc", () -> {
            catalog.init();

            catalog.addItem(new Item()
                    .withType("shoes")
                    .withName("Hair Force Ones")
                    .withDescription("Big wigs stomping in my Hair Force Ones."));

            catalog.addItem(new Item()
                    .withType("shoes")
                    .withName("Doc Martinis")
                    .withDescription("These boots are made for sipping, and that's just what they'll do."));
        });

        doFor("def", () -> {
            catalog.init();

            catalog.addItem(new Item()
                    .withType("books")
                    .withName("A Book")
                    .withDescription("Blah blah"));

            catalog.addItem(new Item()
                    .withType("dvds")
                    .withName("A DVD")
                    .withDescription("Blah blah blah"));
        });

        doFor("ghi", () -> {
            catalog.init();
        });
    }

    public void createCatalog(String id) {
        doFor(id, () -> catalog.init());
    }

    private void doFor(String id, Runnable action) {
        context.setContext(id);
        try {
            action.run();
        } finally {
            context.removeContext();
        }
    }
}
