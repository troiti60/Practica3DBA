package practica3;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Class for parsing and creating Json strings
 *
 * @author José Carlos Alfaro
 */
public class JsonDBA {

    private final Gson gson;
    private final JsonParser parser;

    /**
     * Constructor
     */
    public JsonDBA() {
        this.gson = new Gson();
        this.parser = new JsonParser();
    }

    /**
     * Serialize a collection to Json format
     *
     * @param collection Collection containing name value pairs
     * @return Return string in Json format
     * @author José Carlos Alfaro
     */
    public String createJson(LinkedHashMap collection) {
        return this.gson.toJson(collection);
    }

    /**
     * Serialize a name value pair as Json string
     *
     * @param key Key
     * @param value Value for this key
     * @return Return a name value pair as Json string
     * @author José Carlos Alfaro
     */
    public String createJson(String key, String value) {
        LinkedHashMap hm = new LinkedHashMap();
        hm.put(key, value);
        return this.gson.toJson(hm);
    }

    /**
     * Extract the value to a corresponding key from a Json element
     *
     * @param jsonElement Contains a name value pair
     * @param key Key to access
     * @return Corresponding value to the key
     * @author José Carlos Alfaro
     */
    public JsonElement getElement(JsonElement jsonElement, String key) {
        String message = jsonElement.toString();
        JsonElement element = null;

        if (message.contains(key)) {
            element = this.parser.parse(message);
            return element.getAsJsonObject().get(key);
        } else {
            System.out.print("The key " + key + " can not be found in the message");
        }

        return element;
    }

    /**
     * Extract an element from a Json string
     * 
     * @param message Message as Json string
     * @param element Element to extract
     * @return Value of the element
     * @author José Carlos Alfaro
     */
    public Object getElement(String message, String element) {
        return this.gson.fromJson(message, LinkedHashMap.class).get(element);
    }

    /**
     * Parse an answer string from the server to a Json element
     *
     * @param message Answer from the server to parse
     * @return JsonElement containing the message
     * @author José Carlos Alfaro
     */
    public JsonElement receiveAnswer(String message) {
        return this.parser.parse(message);
    }

    /**
     * Transform a Json element with a float collection into a float array
     *
     * @param string Contains the float collection
     * @return Float array
     * @author José Carlos Alfaro
     */
    public ArrayList<Float> jsonElementToArrayFloat(JsonElement string) {
        ArrayList<Float> arr_float = new ArrayList<>();
        JsonElement element = this.parser.parse(string.toString());
        JsonArray jsArray = element.getAsJsonArray();

        for (JsonElement jse : jsArray) {
            arr_float.add(jse.getAsFloat());
        }
        return arr_float;
    }

    /**
     * Transform a Json element with an integer collection into an integer array
     *
     * @param string Contains the integer collection
     * @return Integer array
     * @author José Carlos Alfaro
     */
    public ArrayList<Integer> jsonElementToArrayInt(Object string) {
        ArrayList<Integer> arr_int = new ArrayList<>();
        JsonElement element = this.parser.parse(string.toString());
        JsonArray jsArray = element.getAsJsonArray();

        for (JsonElement jse : jsArray) {
            arr_int.add(jse.getAsInt());
        }
        return arr_int;
    }

    /**
     * Get Integer element from Json string message
     *
     * @param msg Whole message
     * @param element Element's key
     * @return Integer element
     * @author José Carlos Alfaro
     */
    public int getElementInteger(String msg, String element) {
        return (int) Double.parseDouble(this.gson.fromJson(msg, LinkedHashMap.class).get(element).toString());
    }

    /**
     * Deserialize Json string within another Json string
     *
     * @param msg String in Json format
     * @return HashMap with the elements from the Json string
     * @author José Carlos Alfaro
     */
    public LinkedHashMap jsonInJson(String msg) {
        HashMap hm;
        LinkedHashMap aux;

        hm = (HashMap) this.gson.fromJson(msg, HashMap.class);
        JsonElement toJsonTree = this.gson.toJsonTree(hm.get("result"));
        aux = this.gson.fromJson(toJsonTree, LinkedHashMap.class);

        return aux;
    }
}
