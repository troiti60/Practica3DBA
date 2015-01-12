/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author rod
 */
public class Birdron extends Decepticon{

    public Birdron(AgentID aid, AgentID boss1,String mykey) throws Exception {
        super(aid,boss1,1,mykey);
        System.out.println("Birdron: Instanciado");
    }
    
}
