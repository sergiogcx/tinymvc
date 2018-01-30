package org.mvc;

import java.util.Map;

public class Controller {
    public Map<String, Object> parameters = null;


    public Controller() {}

    public String index() {
        return null;
    }

    public void set_parameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    protected String get_param(String key) {
        System.out.println("Reading param: " + key);
        try {
            String output = parameters.get(key).toString();
            System.out.println("Param: " + key + ", value: "  + output);
            return output;
        } catch (Exception e) {
            System.out.println("Reading param failed: " + key + ", error: " + e.toString());
        }
        return null;
    }
}

