package org.mvc.controllers;

import org.mvc.Controller;
import org.mvc.View;

import org.mvc.models.*;

public class ProductController extends Controller {

    // Our View handler ...
    public View view;

    // Default Layout initialization ...
    public ProductController() {
        view = new View("Product_Layout"); // Default...
        view.set_variable("title","Products");
    }

    public String index() {
        View menu = new View("Product_MenuPartial");
        view.set_variable("content", menu.render());
        return view.render();
    }

    public String find_form() {
        View form = new View("Product_FindFormPartial");
        view.set_variable("content", form.render());
        return view.render();
    }


    public String create_form() {
        View form = new View("Product_CreateFormPartial");
        view.set_variable("content", form.render());
        return view.render();
    }


    public String find() {
        // Instantiate User model, ...
        Product p = new Product();

        String product_sku = "";
        String output = "";

        try {
            product_sku = get_param("sku");
        }catch (Exception e) {
            System.out.println("ProductController: find: error: " + e.toString());
        }

        if(parameters.containsKey("sku")) {
            // Find it (aka. build it) ...
            p.find(product_sku);
        }

        if(p.is_found()) {
            output = "" +
                    "SKU: " + p.sku + "<br/>\n" +
                    "Name: " + p.name + "<br/>\n" +
                    "Description: " + p.description + "<br/>\n";

        } else {
            output = "Product <b>"+product_sku+"</b> could not be found.";
        }


        // Copy content and render!
        view.set_variable("content", output);
        return view.render();
    }


    public String create() {
        // Heck yes ...
        String output = "";

        try {
            // Build User and save it in Redis ...
            Product p = new Product();
            p.sku = get_param("sku").toString();
            p.name = get_param("name").toString();
            p.description = get_param("description").toString();
            p.save();

            output = "Product Created Successfully! :" + p.sku;

        } catch (Exception e) {
            output = "Product could not be created, missing arguments? Error Code: " + e.toString();
            System.out.println("ProductController: create: error: " + e.toString());
        }

        view.set_variable("content", output);
        return view.render();
    }


}
