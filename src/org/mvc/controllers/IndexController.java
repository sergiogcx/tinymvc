package org.mvc.controllers;

import org.mvc.Controller;
import org.mvc.View;

import org.mvc.models.*;


// This is our default Index Controller
public class IndexController extends Controller {

    // Our View handler ...
    public View view;

    // Default Layout initialization ...
    public IndexController() {
        view = new View("Layout"); // Default...
        view.set_variable("title", "MVC Super Example - Sergio Garcia");
    }

    // Overriding our default index method ...
    @Override
    public String index() {
        return view.render();
    }


}
