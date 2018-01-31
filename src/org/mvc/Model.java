package org.mvc;


// For MD5 Hashes
import java.security.*;

// To manipulate methods/fields by name ...
import java.lang.reflect.Field;

// Required Libraries ...
import redis.clients.jedis.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/*
    User <-> Json <-> Redis (key : value)

    User <-> Model <-> Redis (email : json document)
*/



public class Model {

    // Required Variables
    private JSONObject obj = new JSONObject();
    private String identifier = null;
    private String identifier_value = null;
    private String identifier_prefix = null;
    private Jedis records = null;
    private boolean is_found = false;


    public boolean is_found() {
        return is_found;
    }


    // Constructor requires an identifier
    public Model(String identifier) {
        this.identifier = identifier;
        System.out.println("Identifier Initialized: " + identifier);
        this.identifier_prefix = this.getClass().getTypeName();
        try {
            records = new Jedis("localhost");
        } catch (Exception e) {
            System.out.println("Could not connect to local redis server: " + e.toString());
        }
    }

    // Save method (writes into the database)
    // Translates the object into a json_document and saves it in redis ...
    public void save() {

        // Get a list of all member fields of the class
        Field[] fields = this.getClass().getDeclaredFields();

        // Loop through each one field, get its value and build a JSON document
        for(Field f:fields) {
            try {
                // This "key" is the name of the field ...
                String key = f.toGenericString();

                // Clean it up, we do not need to know extra details ...
                key = key.toString().replace("public ", "")
                                    .replace("java.lang.String", "")
                                    .replace(identifier_prefix + ".", "")
                                    .trim();

                // We not get its value (the value of that field) ...
                f.setAccessible(true);
                String value = (String) f.get(this);

                // If this key is our identifier, then we need to copy its value for indexing in Redis ...
                if(identifier.equals(key)) this.identifier_value = value;

                // Output for verbose debug ...
                System.out.println("Building Object: key: " +  key + ", value: " + value);

                // We now attach this one field into the json document ...
                obj.put(key, value);
            } catch (Exception e) {
                // Report if there is a problem ...
                e.printStackTrace();
            }
        }

        // For verbose debugging ...
        identifier_value = identifier_prefix + ":" + identifier_value;
        System.out.println("KeyIdentifier: " + identifier);
        System.out.println("KeyIdentifier_Prefix: " + identifier_prefix);
        System.out.println("KeyIdentifier_Value: " + identifier_value);
        System.out.println("KeyIdentifier_MD5: " + hash_md5(identifier_value));
        System.out.println("Object: " + obj);

        // Once we have our identifier key in md5 format, then we save the record into Redis ...
        // Why MD5? It should help us have a nice short unique identifier that is private (non-human-readable)...
        set_record(hash_md5(identifier_value), obj.toString());
    }

    // Saves a record in Redis using the key-value provided ...
    public void set_record(String key, String value) {
        try {
            records.set(key, value);
        } catch (Exception e) {
            System.out.println("Could not set value: " + e.toString());
        }

    }

    // Gets from Redis a value using the key ...
    public String get_record(String key) {
        try {
            return records.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    // Builds the md5 identifier using the identifier prefix and the identifier record id (ie. email, id number, etc) ...
    public String buildIdentifier(String record_id) {
        return hash_md5(identifier_prefix + ":" + record_id);
    }


    // This one gave me headaches, rebuilding the Object based on the Json document ...
    public void buildObject(String json) {

        // First, we need the document and parser handily, ...
        System.out.println("Building Object: " + json);
        JSONParser parser = new JSONParser();

        // Let's get a list of all fields in our theoretical model class ...
        Field[] fields = this.getClass().getDeclaredFields();

        try {

            // Parse the JSON into a JSON Object ...
            JSONObject jsonObject = (JSONObject) parser.parse(json);

            // For each field in our theoretical model class, ...
            for(Field f:fields) {

                // Get the name of the field ...
                String key = f.toGenericString();

                // Clean the key (name) of that field ...
                key = key.toString().replace("public ", "")
                        .replace("java.lang.String", "")
                        .replace(identifier_prefix + ".", "")
                        .trim();

                // Then Find the value in the Json Object using our key ...
                // Then set that value to that field in our theoretical model...
                f.setAccessible(true);
                f.set(this, jsonObject.get(key));
            }

            is_found = true;
        } catch (Exception e) {
            is_found = false;
            System.out.println("User could not be found, error: " + e.toString());
            // report back please ...
            // e.printStackTrace();
        }
    }

    // Populates this theoretical model by pulling data from Redis (using our identifier) ...
    public void find(String identifier) {
        // Build the key using our identifier ...
        String record_key = this.buildIdentifier(identifier);
        System.out.println("Finding by identifier (" + identifier+ "): Key:  " + record_key);

        // Fetch the Record and Build this theoretical model ...
        buildObject(this.get_record(record_key));
    }


    // I don't know where I got this one from, but it works ...
    // Translates a string into an md5 hash
    private String hash_md5(String md5) {
        return md5;
        /*
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
        */
    }
}
