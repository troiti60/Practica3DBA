package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author Alexander Straub
 */
public final class Falcdron extends Decepticon{

    /**
     * Constructor
     * 
     * @param aid ID of the new decepticon
     * @param megatron ID of megatron
     * @param key Key for communication
     * @throws Exception 
     * @author Alexander Straub
     */
    public Falcdron(AgentID aid, AgentID megatron, String key) throws Exception {
        super(aid, megatron, 2, key);
        this.name = "Falcdron " + this.getName();
        
        System.out.println(this.name + ": Instantiated");
    }
    
}
