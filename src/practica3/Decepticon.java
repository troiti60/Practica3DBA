/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import com.google.gson.Gson;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antonio
 */
public class Decepticon extends SingleAgent {
    public AgentID boss;
    private final int type;
    private int status; // mapping -> 0
                        // target found -> 1
    private boolean alive;
    private final String key;
    
    public Decepticon(AgentID aid, AgentID boss1,int mytype,String mykey) throws Exception{
        super(aid);
        boss=boss1;
        status = 0;
        alive=true;
        type=mytype;
        key=mykey;
    }
    
    public void refreshSensors(){
        ACLMessage out=new ACLMessage(ACLMessage.QUERY_REF);
        LinkedHashMap hash = new LinkedHashMap();
        Gson gson = new Gson();
        hash.put("key",key);
        out.setContent(gson.toJson(hash));
        out.setSender(this.getAid());
        out.setReceiver(new AgentID("Canis"));
        this.send(out);
    
    }

    @Override
    public void execute(){
        
        while(alive){
            ACLMessage msg=new ACLMessage();
            try {
                msg = this.receiveACLMessage();
            } catch (InterruptedException ex) {
                Logger.getLogger(Decepticon.class.getName()).log(Level.SEVERE, null, ex);
            }
            int performative=msg.getPerformativeInt();
            if(msg.getSender().getLocalName().equals("Canis")){
                if(performative==ACLMessage.INFORM){
                    if(msg.getContent().contains("battery")){
                        ACLMessage out= new ACLMessage();
                        out.setPerformative(ACLMessage.INFORM);
                        out.setSender(this.getAid());
                        out.setReceiver(boss);
                        out.setContent(msg.getContent());
                        this.send(out);
                    }
                } else if (performative==ACLMessage.NOT_UNDERSTOOD||
                            performative==ACLMessage.REFUSE||
                            performative==ACLMessage.FAILURE){
                    ACLMessage out= new ACLMessage(performative);
                    out.setSender(this.getAid());
                    out.setReceiver(boss);
                    out.setContent(msg.getContent());
                    this.send(out);
                    alive=false;
                }
            } else if (msg.getSender().getLocalName().equals(boss.getLocalName())){
                if(performative==ACLMessage.REQUEST){
                    ACLMessage out = new ACLMessage(msg.getPerformativeInt());
                    out.setSender(this.getAid());
                    out.setReceiver(new AgentID("Canis"));
                    out.setContent(msg.getContent());
                    this.send(out);
                    
                    refreshSensors();
                }
            }
        }
    }
}
