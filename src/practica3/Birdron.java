package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author Alexander Straub
 */
public final class Birdron extends Decepticon{

    /**
     * Constructor
     * 
     * @param aid ID of the new decepticon
     * @param megatron ID of megatron
     * @param key Key for communication
     * @throws Exception 
     * @author Alexander Straub
     */
    public Birdron(AgentID aid, AgentID megatron, String key) throws Exception {
        super(aid, megatron, 1, key);
        this.name = "Birdron " + this.getName();
        
        System.out.println(this.name + ": Instantiated");
    }
    
}
