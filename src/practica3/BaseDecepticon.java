/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
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
        DataAccess data = DataAccess.crearInstancia();
        AgentID idMega = new AgentID( DataAccess.getNameMegatron() );
        Megatron mega = null;
        
        try {
            mega = new Megatron(idMega);
        } catch (Exception ex) {
            Logger.getLogger(BaseDecepticon.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error al instanciar Megatron");
        }
        
        mega.start();
    }
    
}
