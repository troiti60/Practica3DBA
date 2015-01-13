package practica3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.LinkedHashMap;

/**
 * Base class for all decepticons
 * 
 * @author Antonio Troitiño del Rio, Alexander Straub
 */
public class Decepticon extends SingleAgent {

    // ID of Megatron
    private final AgentID megatron;
    
    // Information about the decepticon
    private final int type;
    private boolean alive;
    
    // Communication
    private final String key;
    private final JsonDBA json;
    
    // Name
    protected String name = "Decepticon";

    /**
     * Constructor
     * 
     * @param aid ID of the new decepticon
     * @param megatron ID of megatron
     * @param type Type: 0-Flydron, 1-Birdron, 2-Falcdron
     * @param key Key for communication
     * @throws Exception 
     * @author Antonio Troitiño del Rio
     */
    public Decepticon(AgentID aid, AgentID megatron, int type, String key) throws Exception {
        super(aid);
        this.megatron = megatron;
        this.alive = true;
        this.type = type;
        this.key = key;
        this.json = new JsonDBA();
    }

    /**
     * Function called after starting the decepticon
     * 
     * @author Antonio Troitiño del Rio
     */
    @Override
    public final void execute() {
        // Checkin
        System.out.println(this.name + " starting...");
        System.out.println(this.name + " registrating with server...");
        checkin();

        ACLMessage msg = null;
        
        // Await answer from server
        try {
            msg = receiveACLMessage();
            System.out.println(this.name + ": Answer for registration received");
        } catch (InterruptedException ex) {
            System.err.println(this.name + ": Error registrating");
            System.err.println("\t" + ex.getMessage());
            this.alive = false;
        }

        // Check answer and scan surroundings
        if (msg != null && msg.getPerformativeInt() == ACLMessage.INFORM) {
            System.out.println(this.name + ": Registrated successfully");
            System.out.println(this.name + ": Scanning surroundings");
            refreshSensors();
        } else {
            System.err.println(this.name + ": Error registrating");
            if (msg != null) System.err.println("\t" + msg.getContent());
            this.alive = false;
        }

        // While the bot is still alive
        while (this.alive) {
            // Wait for answer with sensor data
            System.out.println("\n" + this.name + ": Waiting for message...");
            
            try {
                msg = this.receiveACLMessage();
                System.out.println(this.name + ": Message content " + msg.getContent());
            } catch (InterruptedException ex) {
                System.err.println(this.name + ": Error receiving message");
                System.err.println("\t" + ex.getMessage());
                msg = null;
            }
            
            // Extract information from answer
            if (msg != null) {
                int performative = msg.getPerformativeInt();
                
                if (msg.getSender().getLocalName().equals(DataAccess.crearInstancia().getVirtualHost())) {
                    // If the answer came from the server
                    System.out.println(this.name + ": Received message from server");
                    
                    if (performative == ACLMessage.INFORM) {
                        // Send sensor data to Megatron
                        if (msg.getContent().contains("battery")) {
                            System.out.println(this.name + ": Sensor data received");
                            System.out.println(this.name + ": Sending data to Megatron");
                            
                            ACLMessage out = new ACLMessage(ACLMessage.INFORM);
                            out.setSender(this.getAid());
                            out.setReceiver(this.megatron);
                            out.setContent(msg.getContent());
                            this.send(out);
                        }
                    } else if (performative == ACLMessage.NOT_UNDERSTOOD
                            || performative == ACLMessage.REFUSE
                            || performative == ACLMessage.FAILURE) {
                        
                        // In case of an error
                        System.err.println(this.name + ": ERROR");
                        
                        ACLMessage out = new ACLMessage(performative);
                        out.setSender(this.getAid());
                        out.setReceiver(this.megatron);
                        out.setContent(msg.getContent());
                        this.send(out);
                        
                        this.alive = false;
                    }
                } else if (msg.getSender().getLocalName().equals(this.megatron.getLocalName())) {
                    // If message came from Megatron
                    if (performative == ACLMessage.REQUEST) {
                        // If it's a request: refresh sensors
                        System.out.println(this.name + ": Received request from Megatron");
                        
                        ACLMessage out = new ACLMessage(performative);
                        out.setSender(this.getAid());
                        out.setReceiver(new AgentID(DataAccess.crearInstancia().getVirtualHost()));
                        out.setContent(msg.getContent());
                        this.send(out);

                        System.out.println(this.name + ": Requesting sensor data");
                        refreshSensors();
                    } else if (performative == ACLMessage.CANCEL) {
                        // Megatron told this decepticon to die
                        System.out.println(this.name + ": Megatron wants me dead");
                        this.alive = false;
                    }
                }
            }
        }
        
        // Tell that this decepticon just died
        System.out.println(this.name + ": Dying");
    }

    /**
     * Handle the checkin to the server
     * 
     * @author Antonio Troitiño del Rio
     */
    private void checkin() {
        LinkedHashMap<String, Object> hm = new LinkedHashMap<>();
        hm.put("command", "checkin");
        hm.put("rol", this.type);
        hm.put("key", this.key);
        String msg = this.json.crearJson(hm);

        System.out.println(this.name + ": Checking in with " + msg);

        ACLMessage outbox = new ACLMessage(ACLMessage.REQUEST);
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(DataAccess.crearInstancia().getVirtualHost()));
        outbox.setContent(msg);
        this.send(outbox);
    }
    
    /**
     * Ask server to send new sensor information
     * 
     * @author Antonio Troitiño del Rio
     */
    private void refreshSensors() {
        ACLMessage out = new ACLMessage(ACLMessage.QUERY_REF);
        out.setContent(this.json.crearJson("key", key));
        out.setSender(this.getAid());
        out.setReceiver(new AgentID(DataAccess.crearInstancia().getVirtualHost()));
        this.send(out);
    }
    
    /**
     * Returns the type of the decepticon
     * 
     * @return 0-Flydron, 1-Birdron, 2-Falcdron
     * @author Alexander Straub
     */
    public final int getType() {
        return this.type;
    }
    
    /**
     * Returns the vital state
     * 
     * @return Vital state
     * @author Alexander Straub
     */
    public final boolean isAlive() {
        return this.alive;
    }

}
