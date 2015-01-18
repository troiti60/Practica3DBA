package practica3;

/**
 * Data class to store login information
 * 
 * @author Javier Ortega Rodríguez
 */
public class DataAccess {
    /**
     * Singleton instance
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
    private final String nameMegatron = "Megatron1";
    private final String nameDrone1 = "Spectro1";
    private final String nameDrone2 = "Viewfinder1";
    private final String nameDrone3 = "Nightbird1";
    private final String nameDrone4 = "Squawktalk1";

    /**
     * Key
     */
    private String key;

    /**
     * Create a singleton instance. If it already exists, return the reference
     *
     * @return Reference to single object
     * @author Javier Ortega Rodríguez
     */
    public static DataAccess createInstance() {
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
        //this.world  = "beach";
        //this.world  = "pyrenees";
        //this.world  = "alps";
        //this.world  = "andes";
        //this.world  = "valleys";
        this.world  = "faun";
        //this.world  = "everest";
        //this.world  = "newyork";
        this.key = null;
    }

    /**
     * Getter hostname
     *
     * @return Hostname
     * @author Javier Ortega Rodríguez
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Getter port
     *
     * @return Port
     * @author Javier Ortega Rodríguez
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Getter virtual host
     *
     * @return Virtual host name
     * @author Javier Ortega Rodríguez
     */
    public String getVirtualHost() {
        return this.virtualhost;
    }

    /**
     * Getter user name
     *
     * @return Username
     * @author Javier Ortega Rodríguez
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Getter password
     *
     * @return Password
     * @author Javier Ortega Rodríguez
     */
    public String getPassword() {
        return this.passwd;
    }

    /**
     * Getter SSL encryption
     *
     * @return SSL encryption
     * @author Javier Ortega Rodríguez
     */
    public Boolean getSSL() {
        return this.ssl;
    }

    /**
     * Setter for key
     *
     * @param key Session key
     * @author Javier Ortega Rodríguez
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Getter key
     *
     * @return Session key
     * @author Javier Ortega Rodríguez
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Getter name of Megatron
     *
     * @return Name of Megatron
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public String getNameMegatron() {
        return this.nameMegatron;
    }

    /**
     * Getter name of first drone
     *
     * @return Name of first drone
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public String getNameDrone1() {
        return this.nameDrone1;
    }
    
    /**
     * Getter name of second drone
     *
     * @return Name of second drone
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public String getNameDrone2() {
        return this.nameDrone2;
    }
    
    /**
     * Getter name of third drone
     *
     * @return Name of third drone
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public String getNameDrone3() {
        return this.nameDrone3;
    }
    
    /**
     * Getter name of fourth drone
     *
     * @return Name of fourth drone
     * @author Javier Ortega Rodríguez, Antonio Troitiño
     */
    public String getNameDrone4() {
        return this.nameDrone4;
    }
    
    /**
     * Getter name of the drones
     *
     * @return List of drone names
     * @author Alexander Straub
     */
    public String[] getNameDrone() {
        return new String[] {this.nameDrone1, this.nameDrone2, 
            this.nameDrone3, this.nameDrone4};
    }
    
    /**
     * Getter of the world
     * 
     * @return Name of the world
     * @author José Carlos Alfaro
     */
    public String getWorld(){
        return this.world;
    }
    
}
