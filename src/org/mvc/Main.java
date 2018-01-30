package org.mvc;

/*
                    e   e       Y8b Y88888P     e88'Y88
                   d8b d8b       Y8b Y888P     d888  'Y
                  e Y8b Y8b       Y8b Y8P     C8888
                 d8b Y8b Y8b       Y8b Y       Y888  ,d
                d888b Y8b Y8b       Y8P         "88,d88


                    A Tiny MVC Http Server for Redis
                    Sergio Garcia <gar13065@byui.edu>

                            ToDo:
                                - Implement support for HTTP POST requests
                                - Implement support for REST
                                - Implement support for static asset service.

        References:
            https://docs.oracle.com/javase/tutorial/reflect/member/methodInvocation.html
            https://docs.oracle.com/javase/tutorial/reflect/class/classMembers.html
            https://docs.oracle.com/javase/7/docs/api/java/util/Arrays.html
            https://www.javatpoint.com/java-hashmap

            Inspiration:
                https://www.codeproject.com/Tips/1040097/Create-a-Simple-Web-Server-in-Java-HTTP-Server
                http://whowish-programming.blogspot.com/2011/04/get-post-parameters-from-java-http.html

        Dependencies:
            - Jedis: https://github.com/xetorthio/jedis
            - Simple JSON: https://github.com/fangyidong/json-simple
*/


// Common Objects ...
import java.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

// Server Requirements (sockets)
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;

// Necessary to call methods and fields by name ...
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Server Objects
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


// A Tiny MVC/HTTP Server with Redis ...

public class Main {

    public static void main(String[] args) throws Exception {

        // Initialize Http Server (port 8000)
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Create Context (Catch-all "/")
        server.createContext("/", new MvcHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    // Implementing MvcHandler
    static class MvcHandler implements HttpHandler {

        // Processes the request through T variable ...
        public void handle(HttpExchange t) throws IOException {

            // Helper Variables
            String output = "";
            String query = t.getRequestURI().getPath(); // Get the URI path
            String class_name = null;
            String method_name = null;


            // Parse URL Arguments
            Map<String, Object> parameters = new HashMap<String, Object>();
            URI requestedUri = t.getRequestURI();
            String query_raw = requestedUri.getRawQuery();
            parseQuery(query_raw, parameters);


            // Split URI path into an array ...
            String[] route = path_parsed(query.split("/"));

            // Set array into variables
            try { class_name = (route[0]+"").toString().isEmpty() ? "Index" : route[0]; } catch(Exception e) { class_name = "Index";  }
            try { method_name = (route[1]+"").toString().isEmpty() ? "Index" : route[1]; } catch(Exception e) { method_name = "index";  }

            // Nicely show in console for debug...
            System.out.println("\n\nNew Request: " + query);
            System.out.println("Class: " + class_name);
            System.out.println("Method: " + method_name + "\n");

            // Execute ...
            try {
                Class noparams[] = {};

                // Fin Controller class based on name, then instantiate it...
                Class execution = Class.forName("org.mvc.controllers." + capitalize_stirng(class_name) + "Controller");
                Object obj = execution.newInstance();

                // Find the parameters field in that class, then pass the Parameters (url arguments) map ...
                Field field = execution.getField("parameters");
                field.setAccessible(true);
                field.set(obj, parameters);

                // Now we call the specific method in that class (index being the default) ...
                Method method = execution.getDeclaredMethod(method_name, noparams);
                output = (String)method.invoke(obj, null);

            } catch(Exception e) {
                // Report back if there is a problem ....
                System.out.println("Failed to run Controller: " + route[0] + ": " + e.toString());
                e.printStackTrace();
            }


            // Now we return output of the method ...
            byte [] response = output.getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    // Taken from Sources (CodeProject), turns an URL argument query into a Map
    public static void parseQuery(String query, Map<String,
            Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");
            for (String pair : pairs) {
                String param[] = pair.split("[=]");
                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);

                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<String>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }


    // Removes empty array items ...
    public static String[] path_parsed (String[] input) {
        return Arrays.stream(input)
                .filter(value ->
                        value != null && value.length() > 0
                )
                .toArray(size -> new String[size]);
    }

    // Capitalizes a string ...
    public static String capitalize_stirng(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    }
}
