/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class in charge of the launch
 * 
 * @author Fco Javier Ortega Rodríguez
 */
public class BaseDecepticon {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        System.out.println("BaseDecepticon: Iniciando...");
        
        DataAccess data = DataAccess.crearInstancia();
        
        // Conection with server
        AgentsConnection.connect(data.getHost(), data.getPort(),
                data.getVirtualHost(), data.getUsername(),
                data.getPassword(), data.getSSL());
        
        System.out.println("BaseDecepticon: Creada conexión con el servidor");
        
        AgentID idMega = new AgentID( DataAccess.getNameMegatron() );
        Megatron mega = null;
        
        try {
            System.out.println("BaseDecepticon: Instanciando Megatron");
            mega = new Megatron(idMega);
        } catch (Exception ex) {
            Logger.getLogger(BaseDecepticon.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("\n####################\nError al instanciar Megatron\n####################\n");
        }
        
        System.out.println("BaseDecepticon: Lanzando Megatron");
        mega.start();
        System.out.println("BaseDecepticon: Fin");
    }
    
}
