package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class in charge of the launch
 *
 * @author Javier Ortega Rodríguez
 */
public class BaseDecepticon {

    /**
     * Main entrance point for this project
     *
     * @param args the command line arguments
     * @author Javier Ortega Rodríguez
     */
    public static void main(String[] args) {
        System.out.println("BaseDecepticon: Initiating...");

        // Create data structure
        DataAccess data = DataAccess.createInstance();

        // Conection with server
        AgentsConnection.connect(data.getHost(), data.getPort(),
                data.getVirtualHost(), data.getUsername(),
                data.getPassword(), data.getSSL());

        System.out.println("BaseDecepticon: Connection to server established");

        // Create and start Megatron
        AgentID idMega = new AgentID(DataAccess.getNameMegatron());
        Megatron mega;

        try {
            System.out.println("BaseDecepticon: Instantiating Megatron");
            mega = new Megatron(idMega);
        } catch (Exception ex) {
            Logger.getLogger(BaseDecepticon.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("\nBaseDecepticon: Error instantiating Megatron");
            System.err.println("\t" + ex.getMessage());
            return;
        }

        System.out.println("BaseDecepticon: Starting Megatron");
        mega.start();
        System.out.println("BaseDecepticon: Megatron started");
    }

}
