package org.mvc.models;

import org.mvc.Model;

public class Product extends Model {
    public String sku;
    public String name;
    public String description;

    public Product() {
        super("sku");
    }
}
