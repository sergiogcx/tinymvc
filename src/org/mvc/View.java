package org.mvc;

import java.util.Map;
import java.util.Hashtable;
import java.io.InputStream;
import java.util.Scanner;


public class View {
    public String html_template;
    public String output = "";
    public Hashtable<String, String> variables = new Hashtable<String, String>();

    // We need to load the template HTML
    public View(String view_name) {
        html_template = render_partial(view_name);
    }

    // Load HTML file into string ...
    private String render_partial(String view_name) {
        view_name = (view_name+"").toString().length() == 0 ? "Layout" : view_name;
        InputStream stream = View.class.getResourceAsStream("views/"+view_name+".html");
        System.out.println(stream != null);
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    // Set variables (for later search-replace) ...
    public void set_variable(String key, String value) {
        variables.put(key, value);
    }

    // Another way to access the template
    public String get_html_template() {
        return html_template;
    }

    // Render Loop ...
    public void internal_render() {
        // Copy
        output = html_template;

        // Traversing map
        for(Map.Entry<String, String> entry:variables.entrySet()){
            String k = entry.getKey();
            String v = entry.getValue();
            System.out.println("Rendering variable: @"+k + " -> " + v);
            output = output.replace("@"+k, v);
        }
    }

    // Pass the input through ...
    public String render(String input) {
        return input;
    }

    // Actual Render Function ...
    public String render() {
        internal_render();
        return output;
    }
}
