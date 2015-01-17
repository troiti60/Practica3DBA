package practica3.drones;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 * Flytron class (mosca)
 *
 * @author Alexander Straub
 */
public final class Flytron extends Decepticon {

    /**
     * Constructor
     *
     * @param aid ID of the new decepticon
     * @param megatron ID of megatron
     * @param key Key for communication
     * @throws Exception
     * @author Alexander Straub
     */
    public Flytron(AgentID aid, AgentID megatron, String key) throws Exception {
        super(aid, megatron, 0, key);
        this.name = "Flytron " + this.getName();

        System.out.println(this.name + ": Instantiated");
    }

}
