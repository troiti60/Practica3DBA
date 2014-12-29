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
import java.util.LinkedHashMap;
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
    private int energyOfWorld;
    private ACLMessage inbox, outbox;
    private JsonDBA json;
    private DataAccess dataAccess;
    private Decepticon dron1,dron2,dron3,dron4;
    private Boolean map2_comprobation=false;
    
    private State state;
    private String msg;
    private boolean live;
    private Action sigAction;
    private int numeroDron;
    
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
        this.drones.add( new DataDecepticon(DataAccess.getNameDron1()));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron2()));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron3()));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron4()));

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
        
        System.out.println("Megatron: Mapa actualizado");
    }
    
    /**
     * Method to update data related to Decepticon
     * 
     * @param numDron The number of Decepticon to update
     * @param newCoord The new coordinate
     * @param newFuel The current fuel
     * @author Fco Javier Ortega Rodriguez
     * 
     */
    private void updateDataDecepticon(int numDron, Coord newCoord, int newFuel){
        this.drones.get(numDron).setPosition(newCoord);
        this.drones.get(numDron).setFuel(newFuel);
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
        System.out.println("\tContenido subscribe: " + outbox.getContent());
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
    
    
    
    /**
     * It will send the order to move depending the action.
     * @author JC
     * @param nameDron Agent's name will recibe the message
     * @param action   Kind of movement.
     */
    public void Move(String nameDron, Action action){
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String,String> hm = new LinkedHashMap<>();
        hm.put("command", action.toString());
        hm.put("key",dataAccess.getKey());
        String msg = json.crearJson(hm);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID(nameDron));
        outbox.setContent(msg);
        this.send(outbox);
    }
    
    /**
     * it will send the order to refuel
     * @author JC
     * @param nameDron Agent's name will recibe the message
     */
    public void Refuel(String nameDron){
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String,String> hm = new LinkedHashMap<>();
        hm.put("command", "refuel");
        hm.put("key",dataAccess.getKey());
        String msg = json.crearJson(hm);
        
        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID(nameDron));
        outbox.setContent(msg);
        this.send(outbox);
        
    }
    
    @Override
    public void execute() {
        state = State.Subscribe;
        msg = null;
        live = true;
        sigAction = null;
        numeroDron = -1;
        System.out.println("Megatron: Iniciado");
        
        while(live){
            switch (state) {
                // Suscribe
                case Subscribe:
                    System.out.println("Megatron------ Estado: Subscribe");
                    Suscribe();                                     
                    
                    try {
                        inbox = receiveACLMessage();
                        msg = inbox.getContent();
                    } catch (InterruptedException ex) {
                        System.err.println("Megatron: Problema al recibir mensaje en Subscribe: "+ ex);
                    }
                    
                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        JsonDBA json = new JsonDBA();
                        String result = (String) json.getElement(msg, "result");
                        dataAccess.setKey(result);   
                        System.out.println("Megatron: Clave recibida " + dataAccess.getKey());
                        System.out.println("Megatron: Cambiando a estado Create");
                        state = State.Create;
                        
                    } else {
                        System.err.println("Megatron ERROR: " + inbox.getPerformative());
                    }
                    break;

                // Lanzar x drones y esperar el OK
                case Create:
                    System.out.println("Megatron------ Estado: Create");
                    try {
                        this.dron1 = new Birdron(this.drones.get(0).getId(), this.getAid(), dataAccess.getKey() );
                        this.drones.get(0).setRole(2); // por ser pájaro, rol 2
                        
                        //this.dron2 = new Birdron(this.drones.get(1).getId(), this.getAid(), dataAccess.getKey() );
                        //this.dron3 = new Birdron(this.drones.get(2).getId(), this.getAid(), dataAccess.getKey() );
                        //this.dron4 = new Birdron(this.drones.get(3).getId(), this.getAid(), dataAccess.getKey() );

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
                    
                    System.out.println("Megatron: Lanzando decepticion...");
                    this.dron1.start();
                    
                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;
                    
                    break;

                // Pedir percepcion a x drones
                // Esperar x percepciones
                case Feel:
                    System.out.println("Megatron------ Estado: Feel");
                    numeroDron = -1;
            
                    try {
                        System.out.println("Megatron: Esperando mensaje");
                        inbox = receiveACLMessage();
                        System.out.println("Megatron: Mensaje recibido de " + inbox.getSender().toString());
                        System.out.println("\tM: " + inbox.getSender().getLocalName() + " " + inbox.getPerformative() + " " + inbox.getContent());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: Error al recibir el mensaje");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
            
                    //if(!inbox.getSender().getLocalName().equals("Canis")){
                        System.out.println("Megatron: Recibido de un Decepticon");

                        if(inbox.getPerformativeInt() == ACLMessage.INFORM){
                            System.out.println("Megatron: Informe");
                     
                            json = new JsonDBA();
                            String result = (String) json.getElement(inbox.getContent(), "result");
                            int battery =  json.getElementInteger(result, "battery");
                            int x = (Integer) json.getElementInteger(result, "x");
                            int y = (Integer) json.getElementInteger(result, "y");
                            Coord nuevaCordenada = new Coord(x,y);
                            ArrayList<Integer> sensor = json.jsonElementToArrayInt(json.getElement(result, "sensor"));
                            int energy = json.getElementInteger(result, "result");
                            boolean goal = (boolean) json.getElement(result, "goal");
                            
                                           
                            System.out.println("\tBateria     " + battery);
                            System.out.println("\tCoordenadas (" + x + "," + y + ")");
                            System.out.println("\tEnergia     " + energy);
                            
                            if(goal)
                                System.out.println("\tGoal     Si");
                            else
                                System.out.println("\tGoal     No");
                            
                            System.out.println("\tSensor      " + sensor.toString());
                            
                            
                            if(inbox.getSender().getLocalName().equals(this.dron1.getName()))
                                numeroDron = 0;
                            else if(inbox.getSender().getLocalName().equals(this.dron2.getName()))
                                numeroDron = 1;
                            else if(inbox.getSender().getLocalName().equals(this.dron3.getName()))
                                numeroDron = 2;
                            else if(inbox.getSender().getLocalName().equals(this.dron4.getName()))
                                numeroDron = 3;
                            
                            updateMap(nuevaCordenada, sensor, numeroDron);
                            updateDataDecepticon(numeroDron, nuevaCordenada, battery);
                            energyOfWorld = energy;
                            
                            System.out.println("Megatron: Cambiando a estado Heuristic");
                            state = State.Heuristic;
                        }
                    //}
                    
                    break;

                // Heuristica
                case Heuristic:
                    System.out.println("Megatron------ Estado: Heuristic");
                    
                    if(false){ // Heuristica refuel
                        System.err.println("Megatron: Necesita repostar");
                        Refuel(this.drones.get(numeroDron).getName());
                        System.err.println("Megatron: Cambiando a estado Feel");
                        state = State.Feel; // o cancel si ya han llegado todos
                    }else{
                        sigAction = mapv0(numeroDron);
                        System.out.println("Megatron: Dron "+ numeroDron + " accion " + sigAction);
                    
                        System.out.println("Megatron: Cambiando a estado Action");
                        state = State.Action; // o cancel si ya han llegado todos
                    }
                    
                    break;

                // Dar orden(es) a x drones
                case Action:
                    System.out.println("Megatron------ Estado: Action");
                    
                    System.out.println("Megatron: Realizando la acción " + sigAction + " en " + this.drones.get(numeroDron).getName());
                    Move(this.drones.get(numeroDron).getName(), sigAction);
                    
                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;
                            
                    break;

                // Cancelar todo para reiniciar
                case Cancel:
                    System.out.println("Megatron------ Estado: Cancel");
                    Cancel();
                    System.out.println("Megatron: Muriendo");
                    live = false;
                    break;
            }
        }
        
        System.out.println("Megatron: Muerto");
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
    private Action busqueda(Nodo start, Nodo goal) throws Exception {
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
            for (Nodo vecino : vecinos) {
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
        //Recorremos el camino desde el nodo objetivo hacia atrás para obtener la accion
        if(current.equals(goal)){
            while(!current.getCamino().equals(start)){
                current = current.getCamino();
            }
            //Posibles nodos adyacentes
            //Nodo al norte
            if(current.getCamino().N().equals(start.getCoord()))
                return Action.N;
            //Nodo al noreste
            if(current.getCamino().NE().equals(start.getCoord()))
                return Action.NE;
            //Nodo al este
            if(current.getCamino().E().equals(start.getCoord()))
                return Action.E;
            //Nodo al sureste
            if(current.getCamino().SE().equals(start.getCoord()))
                return Action.SE;            
            //Nodo al sur
            if(current.getCamino().S().equals(start.getCoord()))
                return Action.S;            
            //Nodo al suroeste
            if(current.getCamino().SO().equals(start.getCoord()))
                return Action.SW;            
            //Nodo al oeste
            if(current.getCamino().O().equals(start.getCoord()))
                return Action.W;            
            //Nodo al noroeste
            if(current.getCamino().NO().equals(start.getCoord()))
                return Action.NW;
       }
        return null;
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
    
    /**
    * Go to origin or next coord not explored
    * @return next action to be done by specified drone
    * @author Jesús Cobo Sánchez
    */
    private Action mapv2(int drone) throws Exception{
        Action actions=null;
        HashMap<Coord,Nodo> map = myMap.getMap();
        Nodo origen=new Nodo(0,0,0); //suponemos que esta libre
        Nodo current;
        Nodo next=null;
        current=new Nodo(drones.get(drone).getCurrent().getX(),drones.get(drone).getCurrent().getY(),
                map.get(drones.get(drone).getCurrent()).getRadar());
        
        if(current.getRadar()==2){
            //encontrado
        }      
        else if(current!=origen && !map2_comprobation){
            actions=busqueda(current,origen);    
        }else{
            int x=1, y=0;
            boolean search_next=false;
            while(!search_next){
                Coord coord=new Coord(x,y);
                if(!map.get(coord).isVisitado() || !map.containsKey(coord)){
                    next=map.get(coord);
                    search_next=true;
                }else{
                    if(x>y) y++;
                    else x++;
                }
            }
            actions=busqueda(current,next);
            search_next=false;
        }
        
        return actions;
    }
}


