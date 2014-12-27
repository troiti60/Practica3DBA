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
             
        System.out.println("Decepticon " + this.getName() + " Ejecuntandose...");
        
        ACLMessage msg = null;   
        
        System.out.println("Decepticon " + this.getName() + " Registrandose en el servidor...");
        Checkin();
        
        try {
            msg = receiveACLMessage();
            System.out.println("Decepticon " + this.getName() + " Respuesta de registro recibida");
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Decepticon.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Decepticon " + this.getName() + " Error al registrar");
            alive = false;
        }
        
        if(msg.getPerformativeInt() == ACLMessage.INFORM){
            System.out.println("Decepticon " + this.getName() + " Registrado");

            System.out.println("Decepticon " + this.getName() + " Escaneando entorno");
            refreshSensors();
        }
        else{
            System.out.println("Decepticon " + this.getName() + " No se ha podido registrar");
            System.out.println("\t"+msg.getContent());
            alive = false;
        }
        
        while(alive){
            System.out.println("\nDecepticon " + this.getName() + " Esperando mensaje...");
            msg=new ACLMessage();
            try {
                msg = this.receiveACLMessage();
                System.out.println("\t" + this.getName() + " Contenido " + msg.getContent());
            } catch (InterruptedException ex) {
                Logger.getLogger(Decepticon.class.getName()).log(Level.SEVERE, null, ex);
            }
            int performative=msg.getPerformativeInt();
            if(msg.getSender().getLocalName().equals("Canis")){
                System.out.println("Decepticon " + this.getName() + " Recibido informe del servidor");
                if(performative==ACLMessage.INFORM){
                    if(msg.getContent().contains("battery")){
                        System.out.println("Decepticon " + this.getName() + " bateria");
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
                    System.out.println("Decepticon " + this.getName() + " ERROR");
                    ACLMessage out= new ACLMessage(performative);
                    out.setSender(this.getAid());
                    out.setReceiver(boss);
                    out.setContent(msg.getContent());
                    this.send(out);
                    alive=false;
                }
            } else if (msg.getSender().getLocalName().equals(boss.getLocalName())){
                if(performative==ACLMessage.REQUEST){
                    System.out.println("Decepticon " + this.getName().toString() + " Recibido solicitud de Megatron");
                    ACLMessage out = new ACLMessage(msg.getPerformativeInt());
                    out.setSender(this.getAid());
                    out.setReceiver(new AgentID("Canis"));
                    out.setContent(msg.getContent());
                    this.send(out);
                    
                    System.out.println("Decepticon " + this.getName() + " Escaneando entorno");
                    refreshSensors();
                }
                // Mensaje recibido de Megatron para morir con estilo
                else if(performative==ACLMessage.CANCEL){
                    System.out.println("Decepticon " + this.getName().toString() + " Megatron me mata");
                    alive = false;
                }
            }
        }
        System.out.println("Decepticon " + this.getName() + " Muerto");
    }
    
    //####################################3
    // Arreglar el parseo
    //#################################
    public void Checkin(){
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String,Object> hm = new LinkedHashMap<>();
        hm.put("command", "checkin");
        hm.put("rol",rol);
        hm.put("key",key);
        String msg = json.crearJson(hm);
        
        //msg = "{\"command\":\"checkin\",\"rol\":"+1+",\"key\":\""+ this.key +"\"}";
        System.out.println("Contenido del mensaje enviado: " + msg);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setContent(msg);
        this.send(outbox);
    }
    
    //#############################3
    // Igual que refreshSensor ?????
    //###############################
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
