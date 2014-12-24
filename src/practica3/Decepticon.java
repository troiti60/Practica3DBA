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
    private final int rol;

    private boolean alive;
    private final String key;
    private JsonDBA json;
    
    public Decepticon(AgentID aid, AgentID boss1,int mytype,String mykey) throws Exception{
        super(aid);
        boss=boss1;
        alive=true;
        rol=mytype;
        key=mykey;
        json = new JsonDBA();
    }
    
    public void refreshSensors(){
        ACLMessage out=new ACLMessage(ACLMessage.QUERY_REF);
        out.setContent(json.crearJson("key", key));
        out.setSender(this.getAid());
        out.setReceiver(new AgentID("Canis"));
        this.send(out);
    }

    @Override
    public void execute(){
        System.out.println("Decepticon " + this.getName().toString() + " Ejecuntandose...");
        
        ACLMessage msg;   
        
        System.out.println("Decepticon " + this.getName().toString() + " Registrandose en el servidor...");
        Checkin();
        
        try {
            msg = this.receiveACLMessage();
            
            if(msg.getPerformativeInt() == ACLMessage.INFORM)
                System.out.println("Decepticon " + this.getName().toString() + " Registrado");
            else{
                System.out.println("Decepticon " + this.getName().toString() + " No se ha podido registrar");
                alive = false;
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Decepticon.class.getName()).log(Level.SEVERE, null, ex);
            alive = false;
        }
        
        
        
        while(alive){
            msg=new ACLMessage();
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
    
    public void Checkin(){
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String,String> hm = new LinkedHashMap<>();
        hm.put("command", "checkin");
        hm.put("rol",Integer.toString(rol));
        hm.put("key",key);
        String msg = json.crearJson(hm);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setContent(msg);
        this.send(outbox);
    }
    
    public void Move(){
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String,String> hm = new LinkedHashMap<>();
        hm.put("command", "moveX");
        hm.put("key",key);
        String msg = json.crearJson(hm);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setContent(msg);
        this.send(outbox);
    }
    
    public void Refuel(){
 
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String,String> hm = new LinkedHashMap<>();
        hm.put("command", "refuel");
        hm.put("key",key);
        String msg = json.crearJson(hm);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setContent(msg);
        this.send(outbox);
        
    }
    
    public void Perception(){
 
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        String msg = json.crearJson("key",key);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.QUERY_REF);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setContent(msg);
        this.send(outbox);
        
    }
}
