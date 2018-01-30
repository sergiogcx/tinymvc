package org.mvc.controllers;

import org.mvc.Controller;
import org.mvc.View;
import org.mvc.models.*;

public class UserController extends Controller {

    // Our View handler ...
    public View view;

    // Default Layout initialization ...
    public UserController() {
        view = new View("User_Layout"); // Default...
        view.set_variable("title","MVC - Users Example - Sergio Garcia");
    }

    // Overriding our default index method ...
    @Override
    public String index() {
        View main_partial = new View("User_MainPartial");
        view.set_variable("content", main_partial.render());
        return view.render();
    }


    // Additional method (page) ...
    public String finduser() {
        // Instantiate User model, ...
        User u = new User();

        String user_email = "";
        String output = "";

        try {
            user_email = parameters.get("email").toString();
        } catch (Exception e) {
            System.out.println("UserController: finduser: error: " + e.toString());
        }

        if(parameters.containsKey("email")) {
            // Find it (aka. build it) ...
            u.find(user_email);
        }

        if(u.is_found()) {
            output = "" +
                    "Name: " + u.name + "<br/>\n" +
                    "E-Mail: " + u.email + "<br/>\n" +
                    "GIT: " + u.githubrepo + "<br/>\n" +
                    "Password: " + u.password + "<br/>\n";

        } else {
            output = "User <b>"+user_email+"</b> could not be found.";
        }


        // Copy content and render!
        view.set_variable("content", output);
        return view.render();
    }


    // Additional method (page) ...
    public String create_user() {

        // Heck yes ...
        String output = "";

        try {
            // Build User model and save it in Redis ...
            User n = new User();
            n.email = get_param("email").toString();
            n.name = get_param("name").toString();
            n.githubrepo = get_param("githubrepo").toString();
            n.password = get_param("password").toString();
            n.save();

            output = "User Created Successfully! :" + n.email;

        } catch (Exception e) {
            output = "User could not be created, missing arguments? Error Code: " + e.toString();
            System.out.println("UserController: create_user: error: " + e.toString());
        }

        // Copy content and render!
        view.set_variable("content", output);
        return view.render();
    }
}
