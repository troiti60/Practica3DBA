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
     * Funcion de login en el Servidor. Parametro World es obligatorio.
     *
     * @param world Nombre del mapa
     * @param radar Nombre del agente Radar
     * @param scanner Nombre del agente Scanner
     * @param battery Nombre del agente Battery
     * @param gps Nombre del agente GPS
     * @return Devuelve el string de logeo
     */
    public String login(String world, String radar, String scanner, String battery, String gps) {
        LinkedHashMap hash = new LinkedHashMap();

        hash.put("command", "login");
        hash.put("world", world);
        hash.put("radar", radar);
        hash.put("scanner", scanner);
        hash.put("battery", battery);
        hash.put("gps", gps);

        return gson.toJson(hash);
    }

    /**
     * Funcion que te serializa una coleccion de datos a formato JSON
     *
     * @param coleccion
     * @return Devuelve un String con el texto en formato JSON
     */
    public String crearJson(LinkedHashMap coleccion) {
        return gson.toJson(coleccion);
    }
    
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
 
        return gson.fromJson(msg, HashMap.class).get(element);
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
    public ArrayList<Integer> jsonElementToArrayInt(JsonElement cadena) {
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