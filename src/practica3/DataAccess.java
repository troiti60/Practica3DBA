/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

/**
 *
 * @author rod
 */
public class DataAccess {
    /**
     * Instancia singleton
     */
    private static DataAccess instance = null;
    
    /**
     * Server Data
     */
    private final String host;
    private final int port;
    private final String virtualhost;
    private final String username;
    private final String passwd;
    private final Boolean ssl;
    private final String world;

    /**
     * Name Decepticons
     */
    private static final String nameMegatron = "BotMegatron";
    private static final String nameDron1 = "BotDrone1";
    private static final String nameDron2 = "BotDrone2";
    private static final String nameDron3 = "BotDrone3";
    private static final String nameDron4 = "BotDrone4";

    /**
     * key
     */
    private String key;

    /**
     * Create a singleton instance. If it already exists, return the reference
     *
     * @return Reference to single object
     * @author Fco Javier Ortega Rodríguez
     */
    public static DataAccess crearInstancia() {
        if (DataAccess.instance == null) {
            DataAccess.instance = new DataAccess();
        }
        return DataAccess.instance;
    }

    /**
     * Constructor
     *
     * @author Javier Ortega Rodríguez
     */
    private DataAccess() {
        this.host = "siadex.ugr.es";
        this.port = 6000;
        this.virtualhost = "Canis";
        this.username = "Capricornio";
        this.passwd = "Gaugin";
        this.ssl = false;
        this.world  = "beach";
        this.key = null;
    }

    /**
     * Getter name host
     *
     * @return name host
     * @author Javier Ortega Rodríguez
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Getter port
     *
     * @return number port
     * @author Javier Ortega Rodríguez
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Getter virtual host
     *
     * @return name virtual host
     * @author Javier Ortega Rodríguez
     */
    public String getVirtualHost() {
        return this.virtualhost;
    }

    /**
     * Getter user name
     *
     * @return name user
     * @author Javier Ortega Rodríguez
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Getter password
     *
     * @return password
     * @author Javier Ortega Rodríguez
     */
    public String getPassword() {
        return this.passwd;
    }

    /**
     * Getter encryption
     *
     * @return value encryption
     * @author Javier Ortega Rodríguez
     */
    public Boolean getSSL() {
        return this.ssl;
    }

    /**
     * Setter of key
     *
     * @param key key session
     * @author Javier Ortega Rodríguez
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Getter key
     *
     * @return key
     * @author Javier Ortega Rodríguez
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Getter name megatron
     *
     * @return name megatron
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public static String getNameMegatron() {
        return DataAccess.nameMegatron;
    }

    /**
     * Getter name dron1
     *
     * @return name dron1
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public static String getNameDron1() {
        return DataAccess.nameDron1;
    }
    
    /**
     * Getter name dron2
     *
     * @return name dron2
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public static String getNameDron2() {
        return DataAccess.nameDron2;
    }
    
    /**
     * Getter name dron3
     *
     * @return name dron3
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public static String getNameDron3() {
        return DataAccess.nameDron3;
    }
    
    /**
     * Getter name dron4
     *
     * @return name dron4
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public static String getNameDron4() {
        return DataAccess.nameDron4;
    }
    
    /**
     * Getter of the world
     * @author JC
     */
    public String getWorld(){
        return this.world;
    }
    
}