package org.mvc.models;

import org.mvc.Model;

// Our Sample Module
public class User extends Model {

    public String email;
    public String name;
    public String githubrepo;
    public String password;

    // We just have to define which field is king (our unique identifier) ...
    public User () {
        super("email");
    }
}


