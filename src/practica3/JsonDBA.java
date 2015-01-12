package practica3;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Clase para parsear y crear strings de JSON
 *
 * @author José Carlos Alfaro
 */
public class JsonDBA {

    private final Gson gson;
    private final JsonParser parser;

    public JsonDBA() {
        this.gson = new Gson();
        this.parser = new JsonParser();
    }

    //**************************************************************************************************//
    //*******************   FUNCIONES DE CREACION DE JSON (SERIALIZACION) ******************************//
    //**************************************************************************************************//
    /**
     * Funcion que serializa una coleccion de datos a formato JSON
     *
     * @param coleccion
     * @return Devuelve un String con el texto en formato JSON
     */
    public String crearJson(LinkedHashMap coleccion) {
        return gson.toJson(coleccion);
    }
    /**
     * Serializa un mensaje a formato json.
     * @param key: clave json
     * @param value: contenido asociado a la clave
     * @return devuelve un string clave y valor en formato json
     */
    public String crearJson(String key, String value){
        LinkedHashMap hm = new LinkedHashMap();
        hm.put(key, value);
        return gson.toJson(hm);
    }

    //**************************************************************************************************//
    //**********************   FUNCIONES DE RECEPCION DE JSON (DESERIALIZACION) ************************//
    //**************************************************************************************************//
    /**
     * Se coge el elemento de la cadena json que pasamos por parametro
     *
     * @param cadena Contiene un elemento json Clave: Valor
     * @param clave Key por la que se accedera
     * @return
     */
    public JsonElement getElement(JsonElement cadena, String clave) {

        String mensaje = cadena.toString();
        JsonElement element = null;
        
        if(mensaje.contains(clave))
        {       
            element = parser.parse(mensaje);
            return element.getAsJsonObject().get(clave);
        }
        else
            System.out.print("La clave "+clave+ " no está en el mensaje");
        
        return element;
        
    }
    
    public Object getElement(String msg, String element){
 
        return gson.fromJson(msg, LinkedHashMap.class).get(element);
    }

    /**
     * Parsea el string de respuesta del servidor a JsonElement 
     * @param result
     * @return JsonElement con el mensaje 
     */
    public JsonElement recibirRespuesta(String result) {
        return parser.parse(result);
    }

    //**************************************************************************************************//
    //*******************************   FUNCIONES AUXILIARES *******************************************//
    //**************************************************************************************************//
    
    /**
     * Transforma un elemento JsonELement con una colección de float en un Array de float.
     * @param cadena: contiene la cadena de float
     * @return Array de float
     */
    public ArrayList<Float> jsonElementToArrayFloat(JsonElement cadena) {
        //JsonParser parser = new JsonParser();
        ArrayList<Float> arr_float = new ArrayList<>();
        JsonElement element = parser.parse(cadena.toString());
        JsonArray jsArray = element.getAsJsonArray();

        for (JsonElement jse : jsArray) {
            arr_float.add(jse.getAsFloat());
        }
        return arr_float;
    }
    
    /**
     * Transforma un elemento JsonELement con una colección de enteros en un Array de enteros.
     * @param cadena: contiene la cadena de enteros
     * @return Array de enteros
     */
    public ArrayList<Integer> jsonElementToArrayInt(Object cadena) {
        //JsonParser parser = new JsonParser();
        ArrayList<Integer> arr_int = new ArrayList<>();
        JsonElement element = parser.parse(cadena.toString());
        JsonArray jsArray = element.getAsJsonArray();

        for (JsonElement jse : jsArray) {
            arr_int.add(jse.getAsInt());
        }
        return arr_int;
    }
    /**
     * Get Integer element from Json string message.
     * @author JC
     * @param msg   Whole message
     * @param element   Element's key
     * @return Integer element
     */
    public int getElementInteger(String msg, String element) {
        Gson gson = new Gson();
        
        return (int) Double.parseDouble(gson.fromJson(msg, LinkedHashMap.class).get(element).toString());
    }
    /**
     * Deserializa una cadena Json que se encuentra dentro de otra cadena Json
     * @param msg: String en formato Json
     * @return HashMap con todos los elementos que contenia la cadena Json
     */
    public LinkedHashMap jsonInJson(String msg){
        
        HashMap hm = new HashMap();
        LinkedHashMap aux= new LinkedHashMap();
        
        hm = (HashMap) gson.fromJson(msg, HashMap.class);
        JsonElement toJsonTree = gson.toJsonTree(hm.get("result"));
        aux = gson.fromJson(toJsonTree, LinkedHashMap.class);
        
        return aux;
        
    }
}