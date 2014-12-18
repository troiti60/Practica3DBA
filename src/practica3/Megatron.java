/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.organization.DataBaseAccess;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that controls the rest of Decepticons
 *
 * @author Fco Javier Ortega Rodriguez
 */
public class Megatron extends SingleAgent {

    private Decepticon dron1, dron2, dron3, dron4;
    private AgentID idDron1, idDron2, idDron3, idDron4;
    private ArrayList<Coord> positions;
    private Map myMap;
    private ACLMessage inbox, outbox;
    private JsonDBA json;
    private DataAccess dataAccess;
    
/**
     * Enum with possible movement actions
     *
     * @author Daniel Sánchez Alcaide
     */
    public enum Accion {

        NW("moveNW"),
        N("moveN"),
        NE("moveNE"),
        E("moveE"),
        SE("moveSE"),
        S("moveS"),
        SW("moveSW"),
        W("moveW");

        // String that defines the action
        private final String command;

        /**
         * Constructor
         *
         * @param command String that defines the action to take
         * @author Daniel Sánchez Alcaide
         */
        private Accion(final String command) {
            this.command = command;
        }

        /**
         * Return the string that defines the action
         *
         * @return String that defines the action
         * @author Daniel Sánchez Alcaide
         */
        @Override
        public String toString() {
            return this.command;
        }
    }
    
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
        positions = new ArrayList<Coord>(4);
    }

    @Override
    protected void init() {
        this.myMap = new Map();
        this.dataAccess = DataAccess.crearInstancia();
        this.idDron1 = new AgentID(DataAccess.getNameDron1());
        this.idDron2 = new AgentID(DataAccess.getNameDron2());
        this.idDron3 = new AgentID(DataAccess.getNameDron3());
        this.idDron4 = new AgentID(DataAccess.getNameDron4());

    }

    /**
     * This method is thought to be called by Megatron itself after parsing a
     * new perception of one of his drons so he can update the map.
     *
     * @param pos current dron position
     * @param perception last perception received by decepticon
     * @param dron local identifier of the sender. It must be an integer between
     * 0 and 3
     * @author Antonio Troitiño del Río
     */
    private void updateMap(Coord pos, ArrayList<Integer> perception, int dron) {
        if (perception.isEmpty() || dron >= positions.size()) {
            System.err.println("ERROR: Megatron received an empty perception!");
        } else {
            positions.set(dron, pos);
            int cont = Math.round((float) Math.sqrt(perception.size()));
            cont = (cont - 1) / 2;
            int count = 0;
            for (int i = 0 - cont; i <= cont; i++) {
                for (int j = 0 - cont; j <= cont; j++) {
                    myMap.addNode(new Coord(pos.getX() + j, pos.getY() + i), perception.get(count));
                    count++;
                }
            }

        }
    }
    /**
     * Send the message to subscribe to the world
     * @author JC
     * 
     */
    public void Suscribe() {
        
        json = new JsonDBA();       
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setSender(getAid());
        outbox.setContent(json.crearJson("world",dataAccess.getWorld()));
        this.send(outbox);
        
    }
    /**
     * Send the message to cancel the session
     * @author JC
     */
    public void Cancel() {

        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.CANCEL);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setSender(getAid());
        outbox.setContent(dataAccess.getKey());
        this.send(outbox);
    }

    @Override
    public void execute() {
        State state = State.Subscribe;
        String msg = null;
        
        switch (state) {
            // Suscribe
            case Subscribe:
                System.out.println("Megatron------\nEstado: Subscribe");
                Suscribe();                                     
                try {
                        inbox = receiveACLMessage();
                        msg = inbox.getContent();
                    } catch (InterruptedException ex) {
                        System.out.println("Problema al recibir mensaje en Subscribe: "+ ex);
                    }
                if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                    state = State.Create;
                    JsonDBA json = new JsonDBA();
                    String result = (String) json.getElement(msg, "result");
                    dataAccess.setKey(result);                   
                    
                } else {
                    System.out.println("ERROR: " + inbox.getPerformative());
                }
                break;

            // Lanzar x drones y esperar el OK
            case Create:
                System.out.println("Megatron------\nEstado: Create");
                try {
                    this.dron1 = new Birdron(this.idDron1, this.getAid(), dataAccess.getKey() );
                    this.dron2 = new Birdron(this.idDron2, this.getAid(), dataAccess.getKey() );
                    this.dron3 = new Birdron(this.idDron3, this.getAid(), dataAccess.getKey() );
                    this.dron4 = new Birdron(this.idDron4, this.getAid(), dataAccess.getKey() );
                } catch (Exception ex) {
                    Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println("Error al instanciar los drones");
                }
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
    
    /**
     * The best path to reach the goal, once we have found it, using A*
     *
     * @return Direction where drone should move
     * @param actual the position of the selected drone
     * @throws Exception
     * @author Daniel Sánchez Alcaide
     */
    private Accion busqueda(Nodo actual) throws Exception {
       Accion camino = Accion.N;
       Comparator<Nodo> comp = new ComparadorHeuristicaNodo();
       PriorityQueue<Nodo> abiertos = new PriorityQueue<Nodo>(comp);
       ArrayList<Nodo> cerrados = new ArrayList<Nodo>();
       abiertos.add(actual);
       
       return camino;
    }
    
    /**
     * The best path to reach the goal, once we have found it, using A*
     *
     * @return The goal nodo
     * @author Antonio Troitiño del Río
     */
    public static Nodo getTarget(){
        Nodo goal = new Nodo(0,0,0,0);
        return goal;
    }
}
