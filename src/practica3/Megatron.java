/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.organization.DataBaseAccess;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that controls the rest of Decepticons
 * 
 * @author Fco Javier Ortega Rodriguez
 */
public class Megatron extends SingleAgent{
    
    private Decepticon dron1, dron2, dron3, dron4;
    private AgentID idDron1, idDron2, idDron3, idDron4;
    private Map myMap;
    
    /**
     * Enum of state
     *
     * @author Fco Javier Ortega Rodríguez
     */
    public enum State {

        Subscribe(0),
        Create(1),
        Feel(2),
        Heuristic(3),
        Action(4),
        Cancel(5);
        
        private final int value;

        private State(final int value) {
            this.value = value;
        }
    }

    public Megatron(AgentID aid) throws Exception {
        super(aid);
    }
    
    @Override
    protected void init(){
        this.myMap = new Map();
        this.idDron1 = new AgentID( DataAccess.getNameDron1() );
        this.idDron2 = new AgentID( DataAccess.getNameDron2() );
        this.idDron3 = new AgentID( DataAccess.getNameDron3() );
        this.idDron4 = new AgentID( DataAccess.getNameDron4() );
        
        try {
            this.dron1 = new Birdron(this.idDron1,this.getAid(),"CLAVE!");
            this.dron2 = new Birdron(this.idDron2,this.getAid(),"CLAVE!");
            this.dron3 = new Birdron(this.idDron3,this.getAid(),"CLAVE!");
            this.dron4 = new Birdron(this.idDron4,this.getAid(),"CLAVE!");
        } catch (Exception ex) {
            Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error al instanciar los drones");
        }
    }
    
    public void Suscribe(){  }
    
    public void Cancel(){  }
    
    @Override
    public void execute() {
        State state = State.Subscribe;
        
        switch(state){
            // Suscribe
            case Subscribe:
                System.out.println("Megatron------\nEstado: Subscribe");
                break;
    
            // Lanzar x drones y esperar el OK
            case Create:
                System.out.println("Megatron------\nEstado: Create");
                break;
    
            // Pedir percepcion a x drones
            // Esperar x percepciones
            case Feel:
                System.out.println("Megatron------\nEstado: Feel");
                break;
            
            // Heuristica
            case Heuristic:
                System.out.println("Megatron------\nEstado: Heuristic");
                break;
            
            // Dar orden(es) a x drones
            case Action:
                System.out.println("Megatron------\nEstado: Action");
                break;
            
            // Cancelar todo para reiniciar
            case Cancel:
                System.out.println("Megatron------\nEstado: Cancel");
                break;
        }            
    }
}