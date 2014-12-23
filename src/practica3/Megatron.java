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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that controls the rest of Decepticons
 *
 * @author Fco Javier Ortega Rodriguez
 */
public class Megatron extends SingleAgent {

    private ArrayList<DataDecepticon> drones;
    private Map myMap;
    private ACLMessage inbox, outbox;
    private JsonDBA json;
    private DataAccess dataAccess;
    private Decepticon dron1,dron2,dron3,dron4;
    
/**
     * Enum with possible movement actions
     *
     * @author Daniel Sánchez Alcaide
     */
    public enum Action {

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
        private Action(final String command) {
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
        drones = new ArrayList<DataDecepticon>(4);
    }

    @Override
    protected void init() {
        this.myMap = new Map();
        this.dataAccess = DataAccess.crearInstancia();
        this.drones.add( new DataDecepticon(DataAccess.getNameDron1(),2));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron2(),2));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron3(),2));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron4(),2));

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
        if (perception.isEmpty() || dron >= drones.size()) {
            System.err.println("ERROR: Megatron received an empty perception!");
        } else {
            drones.get(dron).setPosition(pos);
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
     * @author JC con su flow
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
                    this.dron1 = new Birdron(this.drones.get(0).getId(), this.getAid(), dataAccess.getKey() );
                    this.dron2 = new Birdron(this.drones.get(1).getId(), this.getAid(), dataAccess.getKey() );
                    this.dron3 = new Birdron(this.drones.get(2).getId(), this.getAid(), dataAccess.getKey() );
                    this.dron4 = new Birdron(this.drones.get(3).getId(), this.getAid(), dataAccess.getKey() );
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
     * @param start the position of the selected drone
     * @param goal the position of the goal
     * @throws Exception
     * @author Daniel Sánchez Alcaide
     */
    private Stack<Action> busqueda(Nodo start, Nodo goal) throws Exception {
       Stack<Action> camino = new Stack<Action>();
       Comparator<Nodo> comp = new ComparadorHeuristicaNodo();
       PriorityQueue<Nodo> abiertos = new PriorityQueue<Nodo>((Collection<? extends Nodo>) comp);
       ArrayList<Nodo> cerrados = new ArrayList<Nodo>();
       Nodo current = start;
       abiertos.add(current);
       //El vértice no es la meta y abiertos no está vacío
       while(!abiertos.isEmpty() && !current.equals(goal)){
           //Sacamos el nodo de abiertos
           current = abiertos.poll();
           //Metemos el nodo en cerrados
           cerrados.add(current);
           //Examinamos los nodos vecinos
           ArrayList<Nodo> vecinos = current.getAdy();
           for(int i=0;i<vecinos.size();i++){
               Nodo vecino = vecinos.get(i);
               //Comprobamos que no esté ni en abiertos ni en cerrados
               if(!abiertos.contains(vecino) && !cerrados.contains(vecino)){
                   //Guardamos el camino hacia el nodo actual desde los vecinos
                   vecino.setCamino(current);
                   abiertos.add(vecino);
               }
               if(abiertos.contains(vecino)){
                   //Si el vecino está en abiertos comparamos los valores de g 
                   //para los posibles nodos padre
                   if(vecino.getCamino().g(start) > current.g(start))
                       vecino.setCamino(current);
               }
           }
       }
       return camino;
    }
    
    /**
     * Gives a way to explore a void map (plainworld) with a single drone
     * @return next action to be done by specified drone
     * @author Antonio Troitiño del Río
     */
    private Action mapv0(int drone){

        Action toDo=null;
        HashMap<Coord,Nodo> map = myMap.getMap();
        if(drones.get(drone).hasWork()){ 
            toDo= drones.get(drone).getAction();
            if(toDo==Action.N&&map.get(drones.get(drone).getCurrent().N()).getRadar()!=2)
                return toDo;
            else if (toDo==Action.S&&map.get(drones.get(drone).getCurrent().S()).getRadar()!=2)
                return toDo;
            else {drones.get(drone).cancelJob(); toDo=null;}
        }
        char pos;
        if(drones.get(drone).getStart().getY()>5) pos='S';
        else pos='N';
        switch(pos){
            case 'S':
                if((drones.get(drone).getLastAction()==Action.W&&
                   !map.containsKey(drones.get(drone).getCurrent().O().O().O())&&
                   map.get(drones.get(drone).getCurrent().O().O()).getRadar()!=2)
                    ||(drones.get(drone).getLastAction()==Action.N&&
                   !map.containsKey(drones.get(drone).getCurrent().SO().SO().O())&&
                   map.get(drones.get(drone).getCurrent().O().O()).getRadar()!=2)){
                    toDo=Action.W;
                    drones.get(drone).doThat(toDo);}
                else if((drones.get(drone).getLastAction()==Action.E&&
                   !map.containsKey(drones.get(drone).getCurrent().E().E().E())&&
                   map.get(drones.get(drone).getCurrent().E().E()).getRadar()!=2)
                    ||(drones.get(drone).getLastAction()==Action.N&&
                   !map.containsKey(drones.get(drone).getCurrent().SE().SE().E())&&
                   map.get(drones.get(drone).getCurrent().E().E()).getRadar()!=2)){
                    toDo=Action.E;
                    drones.get(drone).doThat(toDo);}
                else {
                    toDo=Action.N;
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).doThat(toDo);
                }                
                break;
            case 'N':
                 if((drones.get(drone).getLastAction()==Action.W&&
                   !map.containsKey(drones.get(drone).getCurrent().O().O().O())&&
                   map.get(drones.get(drone).getCurrent().O().O()).getRadar()!=2)
                    ||(drones.get(drone).getLastAction()==Action.S&&
                   !map.containsKey(drones.get(drone).getCurrent().NO().NO().O())&&
                   map.get(drones.get(drone).getCurrent().O().O()).getRadar()!=2)){
                    toDo=Action.W;
                    drones.get(drone).doThat(toDo);}
                else if((drones.get(drone).getLastAction()==Action.E&&
                   !map.containsKey(drones.get(drone).getCurrent().E().E().E())&&
                   map.get(drones.get(drone).getCurrent().E().E()).getRadar()!=2)
                    ||(drones.get(drone).getLastAction()==Action.S&&
                   !map.containsKey(drones.get(drone).getCurrent().NE().NE().E())&&
                   map.get(drones.get(drone).getCurrent().E().E()).getRadar()!=2)){
                    toDo=Action.E;
                    drones.get(drone).doThat(toDo);}
                else {
                    toDo=Action.S;
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).doThat(toDo);
                } 
                break;     
            default:
                System.err.println("Error: posición de comienzo del dron no inicializada");
        
                break;
        }
        return toDo;
    }

}
