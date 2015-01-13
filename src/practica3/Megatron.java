/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import practica3.Draw.Ventana;

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
    private Decepticon dron1, dron2, dron3, dron4;
    private boolean map2_comprobation = false;
    private int map2_contador = 0;
    private boolean map2_direccion = true;
    private boolean map2_iod = false;
    private boolean map2_bordeando = false;
    private Nodo nodoGoal;
    private Ventana draw;
    
    private State state;
    private String msg;
    private boolean live;
    private Action sigAction;
    private int numeroDron;

    private int pasos = 0;
    private boolean zoneGoalFound = false;

    // For mapv3
    private Stack<Action>[] pathToClosestUnexploredCell = new Stack[4];
    private Action[] lastAction = new Action[4];
    
    // Image of the map for visualization
    private MapImage mapImage = null;

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
        LaunchRest(2),
        Feel(3),
        Heuristic(4),
        Action(5),
        Cancel(6);

        private final int value;

        private State(final int value) {
            this.value = value;
        }
    }

    /**
     * Constructor
     * 
     * @param aid Agent ID
     * @throws Exception 
     * @author Alexander Straub
     */
    public Megatron(AgentID aid) throws Exception {
        super(aid);
        drones = new ArrayList<>(4);
        
        this.lastAction[0] = null;
        this.lastAction[1] = null;
        this.lastAction[2] = null;
        this.lastAction[3] = null;
        
        this.pathToClosestUnexploredCell[0] = new Stack<>();
        this.pathToClosestUnexploredCell[1] = new Stack<>();
        this.pathToClosestUnexploredCell[2] = new Stack<>();
        this.pathToClosestUnexploredCell[3] = new Stack<>();
    }

    @Override
    protected void init() {
        this.myMap = new Map();
        this.dataAccess = DataAccess.crearInstancia();
        this.drones.add( new DataDecepticon(DataAccess.getNameDron1()));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron2()));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron3()));
        this.drones.add( new DataDecepticon(DataAccess.getNameDron4()));
        draw = new Ventana();     
        draw.setResizable(true);
        draw.setVisible(true);

        int resolution = 100;
        if (this.dataAccess.getWorld().equals("newyork")) resolution = 500;
        this.mapImage = new MapImage(resolution);
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
            this.mapImage.setDronePosition(dron, pos);
            int cont = Math.round((float) Math.sqrt(perception.size()));
            cont = (cont - 1) / 2;
            int count = 0;
            for (int i = 0 - cont; i <= cont; i++) {
                for (int j = 0 - cont; j <= cont; j++) {
                    myMap.addNode(new Coord(pos.getX() + j, pos.getY() + i), perception.get(count));
                    this.mapImage.setCell(perception.get(count), new Coord(pos.getX() + j, pos.getY() + i));
                    count++;
                }
            }
        }
        
        draw.getJpanel().updateDraw(myMap, drones.get(dron));
        draw.setLabelCoordinate(pos.getX(), pos.getY());     
        draw.setBatteryDroneValue(drones.get(dron).getFuel());
        draw.setTotalBatteryValue(energyOfWorld);
        
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
    private void updateDataDecepticon(int numDron, Coord newCoord, int newFuel) {
        this.drones.get(numDron).setPosition(newCoord);
        this.drones.get(numDron).setFuel(newFuel);
    }

    /**
     * Send the message to subscribe to the world
     *
     * @author JC con su flow
     *
     */
    public void Suscribe() {

        json = new JsonDBA();
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setSender(getAid());
        outbox.setContent(json.crearJson("world", dataAccess.getWorld()));
        System.out.println("\tContenido subscribe: " + outbox.getContent());
        this.send(outbox);

    }

    /**
     * Send the message to cancel the session
     *
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
     *
     * @author JC
     * @param nameDron Agent's name will recibe the message
     * @param action Kind of movement.
     */
    public void Move(String nameDron, Action action) {
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", action.toString());
        hm.put("key", dataAccess.getKey());
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
     *
     * @author JC
     * @param nameDron Agent's name will recibe the message
     */
    public void Refuel(String nameDron) {
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", "refuel");
        hm.put("key", dataAccess.getKey());
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

        while (live) {
            pasos++;
            System.err.println("Paso: " + pasos);
            switch (state) {
                // Suscribe
                case Subscribe:
                    System.out.println("Megatron------ Estado: Subscribe");
                    Suscribe();

                    try {
                        inbox = receiveACLMessage();
                        msg = inbox.getContent();
                    } catch (InterruptedException ex) {
                        System.err.println("Megatron: Problema al recibir mensaje en Subscribe: " + ex);
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
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

                // Lanzar x drones de mapeo
                case Create:
                    System.out.println("Megatron------ Estado: Create");
                    try {
                        this.dron1 = new Birdron(this.drones.get(0).getId(), this.getAid(), dataAccess.getKey());
                        this.drones.get(0).setRole(1); // por ser pájaro, rol 2

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    System.out.println("Megatron: Lanzando decepticion 1...");
                    this.dron1.start();

                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;

                    break;

                case LaunchRest:
                    System.out.println("Megatron------ Estado: LaunchRest");
                    try {
                        this.dron2 = new Birdron(this.drones.get(1).getId(), this.getAid(), dataAccess.getKey());
                        this.drones.get(1).setRole(1); // por ser pájaro, rol 2

                        this.dron3 = new Birdron(this.drones.get(2).getId(), this.getAid(), dataAccess.getKey());
                        this.drones.get(2).setRole(1); // por ser pájaro, rol 2

                        this.dron4 = new Birdron(this.drones.get(3).getId(), this.getAid(), dataAccess.getKey());
                        this.drones.get(3).setRole(1); // por ser pájaro, rol 2

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    System.out.println("Megatron: Lanzando decepticion 2...");
                    this.dron2.start();

                    System.out.println("Megatron: Lanzando decepticion 3...");
                    this.dron3.start();

                    System.out.println("Megatron: Lanzando decepticion 4...");
                    this.dron4.start();

                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;

                    break;
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

                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        System.out.println("Megatron: Informe");

                        json = new JsonDBA();
                        String result = json.getElement(inbox.getContent(), "result").toString();
                        int battery = json.getElementInteger(result, "battery");
                        System.out.println("Mostrando Bateria: " + battery);
                        int x = json.getElementInteger(result, "x");
                        int y = json.getElementInteger(result, "y");
                        Coord nuevaCordenada = new Coord(x, y);
                        System.out.println("Coordenadas guardadas: " + "(" + x + " , " + y + ")");
                        ArrayList<Integer> sensor = json.jsonElementToArrayInt(json.getElement(result, "sensor"));
                        int energy = json.getElementInteger(result, "energy");
                        System.out.println("Mostrando energia restante: " + energy);
                        boolean goal = (boolean) json.getElement(result, "goal");
                        boolean goalFound = (boolean)sensor.contains(3);
                        
                        if (inbox.getSender().getLocalName().equals(this.dron1.getName())) {
                            numeroDron = 0;
                        } else if (inbox.getSender().getLocalName().equals(this.dron2.getName())) {
                            numeroDron = 1;
                        } else if (inbox.getSender().getLocalName().equals(this.dron3.getName())) {
                            numeroDron = 2;
                        } else if (inbox.getSender().getLocalName().equals(this.dron4.getName())) {
                            numeroDron = 3;
                        }

                        System.out.println("\tSensor      " + sensor.toString());

                        updateMap(nuevaCordenada, sensor, numeroDron);
                        updateDataDecepticon(numeroDron, nuevaCordenada, battery);
                        energyOfWorld = energy;

                        if (goal) {
                            System.out.println("\tGoal     Si");

                            this.drones.get(numeroDron).setInGoal();
                            this.drones.get(numeroDron).setGoal(nuevaCordenada);
                            
                            state = State.Heuristic;
                        } else if (goalFound) {
                            if (!zoneGoalFound) {
                                // Es el primer dron que llega, asignar metas al resto
                                // Llamar al método para aparcar
                                
                                int resolution;
                                switch (this.drones.get(numeroDron).getRole()) {
                                    case 0: resolution = 3; break;
                                    case 1: resolution = 5; break;
                                    default: resolution = 11; break;
                                }
                                
                                int mapWidth = 100;
                                if (this.dataAccess.getWorld().equals("newyork")) mapWidth = 500;
                                
                                int index = sensor.indexOf(3);
                                int i = index % resolution; i -= resolution / 2;
                                int j = index / resolution; j -= resolution / 2;
                                
                                Coord coordGoal = nuevaCordenada.add(i, j);
                                if (coordGoal.getX() >= 0 && coordGoal.getY() >= 0 && 
                                        coordGoal.getX() < mapWidth && coordGoal.getY() < mapWidth) {
                                    
                                    zoneGoalFound = true;
                                    System.err.println("Paso " + pasos + "\n\n###################\nD:" + numeroDron + " ha encontrado el objetivo\n###########\n");
                                    System.out.println("Megatron: Cambiando a estado LaunchRest");
                                    state = State.LaunchRest;
                                    
                                    nodoGoal = new Nodo(coordGoal, 3);
                                    
                                    // DEBUG: only until goal has been found
                                    state = State.Cancel;
                                } else {
                                    System.out.println("\tGoal     No");
                                    System.out.println("Megatron: Cambiando a estado Heuristic");
                                    state = State.Heuristic;
                                }
                            }
                        } else {
                            System.out.println("\tGoal     No");
                            System.out.println("Megatron: Cambiando a estado Heuristic");
                            state = State.Heuristic;
                        }

                        if (goal) {
                            System.err.println("Paso " + pasos + "\n\n###################\nD:" + numeroDron + " En objetivo\n###########\n");

                                // si todos los vivos han llegado
                            //System.out.println("Megatron: Primer decepticon llegó, cancelando...");
                            //state = State.Cancel;
                        }
                    } else {
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
                    //}

                    break;

                // Heuristica
                case Heuristic:
                    System.out.println("Megatron------ Estado: Heuristic");
                    try {
                        if (fuelH(numeroDron, nodoGoal)) { // Heuristica refuel
                            System.err.println("Megatron: Necesita repostar");
                            Refuel(this.drones.get(numeroDron).getName());
                            System.err.println("Megatron: Cambiando a estado Feel");
                            state = State.Feel;
                        } else {
                            if (zoneGoalFound) {
                                // Nodo goal ha de ser drones.get(numero).getGoal
                                // su ndo asignado para aterrizar
                                System.err.println("Megatron: Usando la búsqueda desde: ("
                                        + this.drones.get(numeroDron).getCurrent().getX() + ","
                                        + this.drones.get(numeroDron).getCurrent().getY() + ") hasta ("
                                        + nodoGoal.toString());
                                sigAction = busqueda(this.myMap.getMap().get(this.drones.get(numeroDron).getCurrent()), nodoGoal).firstElement();
                            } else {
                                sigAction = mapv3(numeroDron);
                            }

                            System.out.println("Megatron: Dron " + numeroDron + " accion " + sigAction);

                            System.out.println("Megatron: Cambiando a estado Action");
                            state = State.Action; // o cancel si ya han llegado todos
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: Error en la heuristica");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
                    break;

                // Dar orden a drone x
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

                    System.out.println("\n#######\nInforme\n#######");
                    for (int i = 0; i < drones.size(); i++) {
                        System.out.print("\n\t" + this.drones.get(i).getName() + "\t");

                        if (this.drones.get(i).isAlive()) {
                            System.out.print("Vivo");
                        } else {
                            System.out.print("Muerto");
                        }

                        if (this.drones.get(i).getInGoal()) {
                            System.out.print("\ten meta");
                        } else {
                            System.out.print("\tfuera de la meta");
                        }
                    }
                    System.out.println("\n#######\n#######");
                    Cancel();
                    System.out.println("Megatron: Muriendo");
                    live = false;
                    
                    try {
                        this.mapImage.saveToFile();
                    } catch (Exception e) {}

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
    private Stack<Action> busqueda(Nodo start, Nodo goal) throws Exception {
        Comparator<Nodo> comp = new ComparadorHeuristicaNodo(goal);
        PriorityQueue<Nodo> abiertos = new PriorityQueue<>(10, comp);
        ArrayList<Nodo> cerrados = new ArrayList<>();
        
        Stack<Action> caminito = new Stack<>();
        
        Nodo current = start;
        abiertos.add(current);
        
        //El vértice no es la meta y abiertos no está vacío
        while (!abiertos.isEmpty() && !current.equals(goal)) {
            //Sacamos el nodo de abiertos
            current = abiertos.poll();
            //Metemos el nodo en cerrados
            cerrados.add(current);
            //Examinamos los nodos vecinos
            ArrayList<Nodo> vecinos = current.getAdy();
            for (Nodo vecino : vecinos) {
                //Comprobamos que no esté ni en abiertos ni en cerrados
                if (!abiertos.contains(vecino) && !cerrados.contains(vecino)) {
                    //Guardamos el camino hacia el nodo actual desde los vecinos
                    vecino.setCamino(current);
                    abiertos.add(vecino);
                }
                if (abiertos.contains(vecino)) {
                    //Si el vecino está en abiertos comparamos los valores de g 
                    //para los posibles nodos padre
                    if (vecino.getCamino().g(start) > current.g(start)) {
                        vecino.setCamino(current);
                    }
                }
            }
        }
        //Recorremos el camino desde el nodo objetivo hacia atrás para obtener la accion
        if (current.equals(goal)) {
            while (!current.getCamino().equals(start)) {
                //El padre está al norte
                if (current.getCamino().getCoord().equals(current.N())) {
                    caminito.add(Action.S);
                }
                //El padre está al sur
                if (current.getCamino().getCoord().equals(current.S())) {
                    caminito.add(Action.N);
                }
                //El padre está al este
                if (current.getCamino().getCoord().equals(current.E())) {
                    caminito.add(Action.W);
                }
                //El padre está al oeste
                if (current.getCamino().getCoord().equals(current.O())) {
                    caminito.add(Action.E);
                }
                //El padre está al noreste
                if (current.getCamino().getCoord().equals(current.NE())) {
                    caminito.add(Action.SW);
                }
                //El padre está al noroeste
                if (current.getCamino().getCoord().equals(current.NO())) {
                    caminito.add(Action.SE);
                }
                //El padre está al sureste
                if (current.getCamino().getCoord().equals(current.SE())) {
                    caminito.add(Action.NW);
                }
                //El padre está al suroeste
                if (current.getCamino().getCoord().equals(current.SO())) {
                    caminito.add(Action.NE);
                }
            }
        }
        return caminito;
    }
    
    /**
     * Search for the best way to go from one node to another
     *
     * @param start Node where to start
     * @param target Node where to get to
     * @return Stack of actions
     * @author Alexander Straub
     */
    private Stack<Action> dijkstra(Nodo start, Nodo target) {
        // Sanity check for the parameters
        if (start == null || target == null || start.equals(target)) return null;
        
        // Get the map
        HashMap<Coord, Nodo> map = this.myMap.getAccessibleMap();
        start = map.get(start.getCoord());
        target = map.get(target.getCoord());

        // Initialize
        List<Nodo> nodes = new ArrayList<>(map.values());
        for (Iterator<Nodo> i = nodes.iterator(); i.hasNext();) {
            i.next().resetBusqueda();
        }
        start.setDistancia(0.0);

        // While there are nodes unexplored
        while (!nodes.isEmpty()) {
            // Get node with less distance
            Nodo minNode = (Nodo) Collections.min(nodes);
            nodes.remove(minNode);

            // Get neighbours of the current node
            for (Iterator<Nodo> i = minNode.getAdy().iterator(); i.hasNext();) {
                Nodo neighbour = i.next();

                // If the neighbour is still in the list
                if (nodes.contains(neighbour)) {
                    // Calculate alternative distance
                    double alternative = minNode.getDistancia() + 1;

                    // If the distance is better, add to the path
                    if (alternative < neighbour.getDistancia()) {
                        neighbour.setDistancia(alternative);
                        neighbour.setCamino(minNode);
                    }
                }
            }
        }

        // Check if a path is even possible
        if (target.getDistancia() == Double.MAX_VALUE) return null;

        // Starting with the target node, trace back to the start
        Stack<Action> actions = new Stack<>();
        
        while (target != null && target != start) {
            // Get next direction
            if (target.getCoord().equals(target.getCamino().getCoord().NW())) {
                actions.push(Action.NW);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().N())) {
                actions.push(Action.N);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().NE())) {
                actions.push(Action.NE);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().E())) {
                actions.push(Action.E);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().SE())) {
                actions.push(Action.SE);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().S())) {
                actions.push(Action.S);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().SW())) {
                actions.push(Action.SW);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().W())) {
                actions.push(Action.W);
            }
            
            target = target.getCamino();
        }

        return actions;
    }

    /**
     * Gives a way to explore a void map (plainworld) with a single drone
     *
     * @return next action to be done by specified drone
     * @author Antonio Troitiño del Río
     */
    private Action mapv0(int drone) {

        Action toDo = null;
        HashMap<Coord, Nodo> map = myMap.getMap();
        if (drones.get(drone).hasWork()) {
            toDo = drones.get(drone).getAction();
            if (toDo == Action.N && map.get(drones.get(drone).getCurrent().N()).getRadar() != 2) {
                return toDo;
            } else if (toDo == Action.S && map.get(drones.get(drone).getCurrent().S()).getRadar() != 2) {
                return toDo;
            } else {
                drones.get(drone).cancelJob();
                toDo = null;
            }
        }
        char pos;
        if (drones.get(drone).getStart().getY() > 5) {
            pos = 'S';
        } else {
            pos = 'N';
        }
        switch (pos) {
            case 'S':
                if ((drones.get(drone).getLastAction() == Action.W
                        && !map.containsKey(drones.get(drone).getCurrent().W().W().W())
                        && map.get(drones.get(drone).getCurrent().W().W()).getRadar() != 2)
                        || (drones.get(drone).getLastAction() == Action.N
                        && !map.containsKey(drones.get(drone).getCurrent().SW().SW().W())
                        && map.get(drones.get(drone).getCurrent().W().W()).getRadar() != 2)) {
                    toDo = Action.W;
                    drones.get(drone).doThat(toDo);
                } else if ((drones.get(drone).getLastAction() == Action.E
                        && !map.containsKey(drones.get(drone).getCurrent().E().E().E())
                        && map.get(drones.get(drone).getCurrent().E().E()).getRadar() != 2)
                        || (drones.get(drone).getLastAction() == Action.N
                        && !map.containsKey(drones.get(drone).getCurrent().SE().SE().E())
                        && map.get(drones.get(drone).getCurrent().E().E()).getRadar() != 2)) {
                    toDo = Action.E;
                    drones.get(drone).doThat(toDo);
                } else {
                    toDo = Action.N;
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).push(toDo);
                    drones.get(drone).doThat(toDo);
                }
                break;
            case 'N':
                if ((drones.get(drone).getLastAction() == Action.W
                        && !map.containsKey(drones.get(drone).getCurrent().W().W().W())
                        && map.get(drones.get(drone).getCurrent().W().W()).getRadar() != 2)
                        || (drones.get(drone).getLastAction() == Action.S
                        && !map.containsKey(drones.get(drone).getCurrent().NW().NW().W())
                        && map.get(drones.get(drone).getCurrent().W().W()).getRadar() != 2)) {
                    toDo = Action.W;
                    drones.get(drone).doThat(toDo);
                } else if ((drones.get(drone).getLastAction() == Action.E
                        && !map.containsKey(drones.get(drone).getCurrent().E().E().E())
                        && map.get(drones.get(drone).getCurrent().E().E()).getRadar() != 2)
                        || (drones.get(drone).getLastAction() == Action.S
                        && !map.containsKey(drones.get(drone).getCurrent().NE().NE().E())
                        && map.get(drones.get(drone).getCurrent().E().E()).getRadar() != 2)) {
                    toDo = Action.E;
                    drones.get(drone).doThat(toDo);
                } else {
                    toDo = Action.S;
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
     * Search a given region in one of two different ways
     *
     * @param drone ID of the drone
     * @param mode Mode of the drone: 0 - North/South, 1 - East/West
     * @param regionMin Lower bound of the assigned region
     * @param regionMax Upper bound of the assigned region
     * @throws Exception
     * @author Alexander Straub
     */
    private void mapv1(int drone, int mode, Coord regionMin, Coord regionMax) throws Exception {
        if (this.drones.get(drone).hasWork()) {
            return;
        }

        // Get search window size in one direction
        int offset = 5;
        if (this.drones.get(drone).getRole() == 0) {
            offset = 1;
        } else if (this.drones.get(drone).getRole() == 1) {
            offset = 2;
        }

        int sign = 1;

        // Store coord to check
        Coord coord = this.drones.get(drone).getCurrent();

        switch (mode) {
            case 0: // North/South
                if (this.drones.get(drone).getLastAction() == Action.N) {
                    sign = -1;
                }
                coord.setY(coord.getY() + sign * offset);

                // If coord hits a wall, go sideways then opposite direction
                if (coord.getY() < regionMin.getY() || coord.getY() > regionMax.getY()
                        || (this.myMap.getMap().containsKey(coord) && this.myMap.getMap().get(coord).getRadar() == 1)) {

                    // Initialize to get as furthest to the side as the current
                    // search window
                    coord = this.drones.get(drone).getCurrent();
                    boolean possible = true;
                    int sideDirection = 1; // 1 - West, -1 - East, TODO: get this info from somewhere
                    int stepsSide = 0, stepsOpposite = 0;

                    // While it is possible to go sideways, do it
                    while (possible && stepsSide < offset && stepsOpposite < offset) { // TODO: prevent from going outside the region
                        // If sideways is free, go there
                        if (this.myMap.getMap().get(new Coord(coord.getX() + sideDirection, coord.getY())).getRadar() != 1) {
                            coord.setX(coord.getX() + sideDirection);
                            stepsSide++;

                            if (sideDirection == 1) {
                                this.drones.get(drone).push(Action.W);
                            } else if (sideDirection == -1) {
                                this.drones.get(drone).push(Action.E);
                            }
                        } // If sideways is not possible, but sideways and in the
                        // opposite direction, go there
                        else if (this.myMap.getMap().get(new Coord(coord.getX() + sideDirection, coord.getY() - sign)).getRadar() != 1) {
                            coord.setX(coord.getX() + sideDirection);
                            coord.setY(coord.getY() - sign);
                            stepsSide++;
                            stepsOpposite++;

                            if (sideDirection == 1 && sign == -1) {
                                this.drones.get(drone).push(Action.SW);
                            } else if (sideDirection == 1 && sign == 1) {
                                this.drones.get(drone).push(Action.NW);
                            } else if (sideDirection == -1 && sign == -1) {
                                this.drones.get(drone).push(Action.SE);
                            } else if (sideDirection == -1 && sign == 1) {
                                this.drones.get(drone).push(Action.NE);
                            }
                        } else {
                            possible = false;
                        }
                    }

                    // After the last side step go into the opposite direction,
                    // thus this algorithm knows where to go in the next
                    // execution
                    if (this.myMap.getMap().get(new Coord(coord.getX(), coord.getY() - sign)).getRadar() != 1) {
                        if (sign == -1) {
                            this.drones.get(drone).push(Action.S);
                        } else if (sign == 1) {
                            this.drones.get(drone).push(Action.N);
                        }
                    }
                } else {
                    this.drones.get(drone).push(this.drones.get(drone).getLastAction());
                }

                break;
            case 1: // East/West
                if (this.drones.get(drone).getLastAction() == Action.W) {
                    sign = -1;
                }
                coord.setX(coord.getX() + sign * offset);

                // If coord hits a wall, go sideways then opposite direction
                if (coord.getX() < regionMin.getX() || coord.getX() > regionMax.getX()
                        || (this.myMap.getMap().containsKey(coord) && this.myMap.getMap().get(coord).getRadar() == 1)) {

                    // Initialize to get as furthest to the side as the current
                    // search window
                    coord = this.drones.get(drone).getCurrent();
                    boolean possible = true;
                    int sideDirection = 1; // 1 - South, -1 - North, TODO: get this info from somewhere
                    int stepsSide = 0, stepsOpposite = 0;

                    // While it is possible to go sideways, do it
                    while (possible && stepsSide < offset && stepsOpposite < offset) { // TODO: prevent from going outside the region
                        // If sideways is free, go there
                        if (this.myMap.getMap().get(new Coord(coord.getX(), coord.getY() + sideDirection)).getRadar() != 1) {
                            coord.setY(coord.getY() + sideDirection);
                            stepsSide++;

                            if (sideDirection == 1) {
                                this.drones.get(drone).push(Action.S);
                            } else if (sideDirection == -1) {
                                this.drones.get(drone).push(Action.N);
                            }
                        } // If sideways is not possible, but sideways and in the
                        // opposite direction, go there
                        else if (this.myMap.getMap().get(new Coord(coord.getX() - sign, coord.getY() + sideDirection)).getRadar() != 1) {
                            coord.setX(coord.getX() - sign);
                            coord.setY(coord.getY() + sideDirection);
                            stepsSide++;
                            stepsOpposite++;

                            if (sideDirection == 1 && sign == -1) {
                                this.drones.get(drone).push(Action.SE);
                            } else if (sideDirection == 1 && sign == 1) {
                                this.drones.get(drone).push(Action.SW);
                            } else if (sideDirection == -1 && sign == -1) {
                                this.drones.get(drone).push(Action.NE);
                            } else if (sideDirection == -1 && sign == 1) {
                                this.drones.get(drone).push(Action.NW);
                            }
                        } else {
                            possible = false;
                        }
                    }

                    // After the last side step go into the opposite direction,
                    // thus this algorithm knows where to go in the next
                    // execution
                    if (this.myMap.getMap().get(new Coord(coord.getX() - sign, coord.getY())).getRadar() != 1) {
                        if (sign == -1) {
                            this.drones.get(drone).push(Action.E);
                        } else if (sign == 1) {
                            this.drones.get(drone).push(Action.W);
                        }
                    }
                } else {
                    this.drones.get(drone).push(this.drones.get(drone).getLastAction());
                }

                break;
        }
    }

    /**
     * Search the whole map in North-South-pattern
     *
     * @param drone ID of the drone
     * @throws Exception
     * @author Alexander Straub
     */
    private void mapv1(int drone) throws Exception {
        mapv1(drone, 0, new Coord(0, 0), new Coord(100, 100));
    }

    /**
     * Search the whole map in one of two possible ways
     *
     * @param drone ID of the drone
     * @param mode Mode of the drone: 0 - North/South, 1 - East/West
     * @throws Exception
     * @author Alexander Straub
     */
    private void mapv1(int drone, int mode) throws Exception {
        mapv1(drone, mode, new Coord(0, 0), new Coord(100, 100));
    }

    /**
     * Search a region of the map using North-South-pattern
     *
     * @param drone ID of the drone
     * @param regionMin Lower bound of the assigned region
     * @param regionMax Upper bound of the assigned region
     * @throws Exception
     * @author Alexander Straub
     */
    private void mapv1(int drone, Coord regionMin, Coord regionMax) throws Exception {
        mapv1(drone, 0, regionMin, regionMax);
    }

    /**
     * Go to origin or next coord not explored
     *
     * @return next action to be done by specified drone
     * @author Jesús Cobo Sánchez
     */
    private Action mapv2(int drone) throws Exception {
        Action actions = null;
        HashMap<Coord, Nodo> map = myMap.getMap();
        Nodo current;
        Nodo next = null;
        current = new Nodo(drones.get(drone).getCurrent().getX(), drones.get(drone).getCurrent().getY(),
                map.get(drones.get(drone).getCurrent()).getRadar());

        if (map2_bordeando) {
            //funcion de bordear obstaculos
        }

        if (map2_comprobation == false) {
            //ir esquina inferor derecha
            if (current.getY() > 50) {
                map2_direccion = false;
                map2_iod = false;
                if (map.get(current.SE()).getRadar() == 2 && map.get(current.S()).getRadar() == 2
                        && map.get(current.E()).getRadar() == 2) {
                    map2_comprobation = true;
                } else {
                    if (map.get(current.E()).getRadar() == 0) {
                        actions = Action.E;
                    } else if (map.get(current.SE()).getRadar() == 0) {
                        actions = Action.SE;
                    } else if (map.get(current.S()).getRadar() == 0) {
                        actions = Action.S;
                    } else if (map.get(current.NE()).getRadar() == 0) {
                        actions = Action.NE;
                    } else if (map.get(current.N()).getRadar() == 0) {
                        actions = Action.N;
                    } else if (map.get(current.NO()).getRadar() == 0) {
                        actions = Action.NW;
                    } else if (map.get(current.O()).getRadar() == 0) {
                        actions = Action.W;
                    } else {
                        actions = Action.SW;
                    }
                }
                //ir esquina superior izquierda
            } else {
                map2_direccion = true;
                map2_iod = true;
                if (map.get(current.NO()).getRadar() == 2 && map.get(current.N()).getRadar() == 2
                        && map.get(current.O()).getRadar() == 2) {
                    map2_comprobation = true;
                } else {
                    if (map.get(current.NO()).getRadar() == 0) {
                        actions = Action.NW;
                    } else if (map.get(current.N()).getRadar() == 0) {
                        actions = Action.N;
                    } else if (map.get(current.O()).getRadar() == 0) {
                        actions = Action.W;
                    } else if (map.get(current.SO()).getRadar() == 0) {
                        actions = Action.SW;
                    } else if (map.get(current.NE()).getRadar() == 0) {
                        actions = Action.NE;
                    } else if (map.get(current.E()).getRadar() == 0) {
                        actions = Action.E;
                    } else if (map.get(current.SE()).getRadar() == 0) {
                        actions = Action.SE;
                    } else {
                        actions = Action.S;
                    }
                }

            }
            //segunda ruta
        } else {
            //hacia abajo e izquierda
            if (!map2_iod && map2_direccion && map2_contador == current.getX()) {
                if (map.get(current.S()).getRadar() == 0) {
                    actions = Action.S;
                } else if (map.get(current.S()).getRadar() == 2) {
                    map2_contador -= 3;
                    actions = Action.W;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                }
                //hacia arriba e izquierda
            } else if (!map2_iod && !map2_direccion && map2_contador == current.getX()) {
                if (map.get(current.N()).getRadar() == 0) {
                    actions = Action.N;
                } else if (map.get(current.N()).getRadar() == 2) {
                    map2_direccion = true;
                    map2_contador -= 3;
                    actions = Action.W;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                }
                //hacia abajo y derecha
            } else if (map2_iod && map2_direccion && map2_contador == current.getX()) {
                if (map.get(current.S()).getRadar() == 0) {
                    actions = Action.S;
                } else if (map.get(current.S()).getRadar() == 2) {
                    map2_contador += 3;
                    actions = Action.E;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                }
                //hacia arriba y derecha
            } else if (map2_iod && !map2_direccion && map2_contador == current.getX()) {
                if (map.get(current.N()).getRadar() == 0) {
                    actions = Action.N;
                } else if (map.get(current.N()).getRadar() == 2) {
                    map2_direccion = true;
                    map2_contador += 3;
                    actions = Action.E;
                } else {
                    map2_bordeando = true;
                    //bordear obstaculo bordearObstaculo(coord x)
                }
                //ir hacia la x cambiada
            } else {
                //ir hacia la izquierda
                if (current.getX() > map2_contador) {
                    if (map.get(current.E()).getRadar() == 0) {
                        actions = Action.E;
                    } else {
                        //bordear
                        map2_bordeando = true;
                    }
                    //ir hacia la derecha
                } else {
                    if (map.get(current.O()).getRadar() == 0) {
                        actions = Action.W;
                    } else {
                        //bordear
                        map2_bordeando = true;
                    }
                }
            }
        }
        return actions;
    }

    /**
     * Exploration of the following form: (example 5x5)
     *
     * 14 13  3 12 11
     * 15  x  x  x 10
     *  0  x  D  x  2
     *  4  x  x  x  9
     *  5  6  1  7  8
     *
     * Beginning with the cell 0 the method tries to find a cell at the border
     * of the visual range of the drone that has not yet been explored
     * completely and is not a wall. If the cell i does not match these
     * criteria, proceed with cell i+1. If none of the border cells is
     * considered usable, use a path finding algorithm to get to the next
     * unexplored cell, there resetting to the exploration pattern above.
     *
     * @param drone ID of the drone
     * @return Next action for the specified drone
     * @author Alexander Straub
     */
    private Action mapv3(int drone) throws Exception {
        Action ret = mapv3(drone, false);

        // If mapv3 returns an illegal action, try again
        if (this.myMap.getMap().get(this.drones.get(drone).getCurrent().neighbour(ret)).getRadar() == 1 || 
                this.myMap.getMap().get(this.drones.get(drone).getCurrent().neighbour(ret)).getRadar() == 2) {
            System.err.println("ERROR: mapv3 devolvió una acción ilegal");
            
            this.pathToClosestUnexploredCell[drone].clear();
            ret = mapv3(drone, true);
        }

        return ret;
    }

    /**
     * Exploration of the following form: (example 5x5)
     *
     * 14 13  3 12 11
     * 15  x  x  x 10
     *  0  x  D  x  2
     *  4  x  x  x  9
     *  5  6  1  7  8
     *
     * Beginning with the cell 0 the method tries to find a cell at the border
     * of the visual range of the drone that has not yet been explored
     * completely and is not a wall. If the cell i does not match these
     * criteria, proceed with cell i+1. If none of the border cells is
     * considered usable, use a path finding algorithm to get to the next
     * unexplored cell, there resetting to the exploration pattern above.
     *
     * PLEASE do NOT call this function directly but call mapv3(int).
     * 
     * @param drone ID of the drone
     * @return Next action for the specified drone
     * @author Alexander Straub
     */
    private Action mapv3(int drone, boolean findWay) throws Exception {
        Coord position = this.drones.get(drone).getCurrent();

        if (this.pathToClosestUnexploredCell[drone].isEmpty()) {
            // Get the border cells of the visual range of the specified drone
            if (!findWay) {
                Nodo[] borderCells;
                Action[] actions;
                
                switch (this.drones.get(drone).getRole()) {
                    case 0: // Mosca
                        borderCells = new Nodo[8];
                        actions = new Action[8];
                        
                        // Different order of directions, depending on start position
                        if (this.drones.get(drone).getStart().getY() == 0) {
                            borderCells[0] = this.myMap.getMap().get(position.E());
                            actions[0] = Action.E;
                            borderCells[1] = this.myMap.getMap().get(position.N());
                            actions[1] = Action.N;
                            borderCells[2] = this.myMap.getMap().get(position.W());
                            actions[2] = Action.W;
                            borderCells[3] = this.myMap.getMap().get(position.S());
                            actions[3] = Action.S;
                            
                            borderCells[4] = this.myMap.getMap().get(position.NE());
                            actions[4] = Action.NE;
                            borderCells[5] = this.myMap.getMap().get(position.NW());
                            actions[5] = Action.NW;
                            borderCells[6] = this.myMap.getMap().get(position.SW());
                            actions[6] = Action.SW;
                            borderCells[7] = this.myMap.getMap().get(position.SE());
                            actions[7] = Action.SE;
                        } else {
                            borderCells[0] = this.myMap.getMap().get(position.W());
                            actions[0] = Action.W;
                            borderCells[1] = this.myMap.getMap().get(position.S());
                            actions[1] = Action.S;
                            borderCells[2] = this.myMap.getMap().get(position.E());
                            actions[2] = Action.E;
                            borderCells[3] = this.myMap.getMap().get(position.N());
                            actions[3] = Action.N;
                            
                            borderCells[4] = this.myMap.getMap().get(position.SW());
                            actions[4] = Action.SW;
                            borderCells[5] = this.myMap.getMap().get(position.SE());
                            actions[5] = Action.SE;
                            borderCells[6] = this.myMap.getMap().get(position.NE());
                            actions[6] = Action.NE;
                            borderCells[7] = this.myMap.getMap().get(position.NW());
                            actions[7] = Action.NW;
                        }
                        
                        // Main directions 3x for optimal exploration
                        for (int i = 0; i < 4; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                if (this.lastAction[drone] != actions[i]) {
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                }
                                this.lastAction[drone] = actions[i];
                                return actions[i];
                            }
                        }
                        
                        // Diagonal directions only once
                        for (int i = 4; i < 8; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                this.lastAction[drone] = actions[i];
                                return actions[i];
                            }
                        }
                        break;
                    case 1: // Pájaro
                        borderCells = new Nodo[16];
                        actions = new Action[16];
                        
                        // Different order of directions, depending on start position
                        if (this.drones.get(drone).getStart().getY() == 0) {
                            borderCells[0] = this.myMap.getMap().get(position.addX(2));
                            actions[0] = Action.E;
                            borderCells[1] = this.myMap.getMap().get(position.addY(-2));
                            actions[1] = Action.N;
                            borderCells[2] = this.myMap.getMap().get(position.addX(-2));
                            actions[2] = Action.W;
                            borderCells[3] = this.myMap.getMap().get(position.addY(2));
                            actions[3] = Action.S;
                            
                            borderCells[4] = this.myMap.getMap().get(position.add(2, -1));
                            actions[4] = Action.NE;
                            borderCells[5] = this.myMap.getMap().get(position.add(2, -2));
                            actions[5] = Action.NE;
                            borderCells[6] = this.myMap.getMap().get(position.add(1, -2));
                            actions[6] = Action.NE;
                            
                            borderCells[7] = this.myMap.getMap().get(position.add(-1, -2));
                            actions[7] = Action.NW;
                            borderCells[8] = this.myMap.getMap().get(position.add(-2, -2));
                            actions[8] = Action.NW;
                            borderCells[9] = this.myMap.getMap().get(position.add(-2, -1));
                            actions[9] = Action.NW;
                            
                            borderCells[10] = this.myMap.getMap().get(position.add(-2, 1));
                            actions[10] = Action.SW;
                            borderCells[11] = this.myMap.getMap().get(position.add(-2, 2));
                            actions[11] = Action.SW;
                            borderCells[12] = this.myMap.getMap().get(position.add(-1, 2));
                            actions[12] = Action.SW;
                            
                            borderCells[13] = this.myMap.getMap().get(position.add(1, 2));
                            actions[13] = Action.SE;
                            borderCells[14] = this.myMap.getMap().get(position.add(2, 2));
                            actions[14] = Action.SE;
                            borderCells[15] = this.myMap.getMap().get(position.add(2, 1));
                            actions[15] = Action.SE;
                        } else {
                            borderCells[0] = this.myMap.getMap().get(position.addX(-2));
                            actions[0] = Action.W;
                            borderCells[1] = this.myMap.getMap().get(position.addY(2));
                            actions[1] = Action.S;
                            borderCells[2] = this.myMap.getMap().get(position.addX(2));
                            actions[2] = Action.E;
                            borderCells[3] = this.myMap.getMap().get(position.addY(-2));
                            actions[3] = Action.N;
                            
                            borderCells[4] = this.myMap.getMap().get(position.add(-2, 1));
                            actions[4] = Action.SW;
                            borderCells[5] = this.myMap.getMap().get(position.add(-2, 2));
                            actions[5] = Action.SW;
                            borderCells[6] = this.myMap.getMap().get(position.add(-1, 2));
                            actions[6] = Action.SW;
                            
                            borderCells[7] = this.myMap.getMap().get(position.add(1, 2));
                            actions[7] = Action.SE;
                            borderCells[8] = this.myMap.getMap().get(position.add(2, 2));
                            actions[8] = Action.SE;
                            borderCells[9] = this.myMap.getMap().get(position.add(2, 1));
                            actions[9] = Action.SE;
                            
                            borderCells[10] = this.myMap.getMap().get(position.add(2, -1));
                            actions[10] = Action.NE;
                            borderCells[11] = this.myMap.getMap().get(position.add(2, -2));
                            actions[11] = Action.NE;
                            borderCells[12] = this.myMap.getMap().get(position.add(1, -2));
                            actions[12] = Action.NE;
                            
                            borderCells[13] = this.myMap.getMap().get(position.add(-1, -2));
                            actions[13] = Action.NW;
                            borderCells[14] = this.myMap.getMap().get(position.add(-2, -2));
                            actions[14] = Action.NW;
                            borderCells[15] = this.myMap.getMap().get(position.add(-2, -1));
                            actions[15] = Action.NW;
                        }
                        
                        // Main directions 3x for optimal exploration
                        for (int i = 0; i < 4; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                if (this.lastAction[drone] != actions[i]) {
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                }
                                this.lastAction[drone] = actions[i];
                                return actions[i];
                            }
                        }
                        
                        // Diagonal directions only once
                        for (int i = 4; i < 16; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                this.lastAction[drone] = actions[i];
                                return actions[i];
                            }
                        }
                        break;
                    case 2: // Halcón
                        borderCells = new Nodo[40];
                        actions = new Action[40];
                        
                        // Different order of directions, depending on start position
                        if (this.drones.get(drone).getStart().getY() == 0) {
                            borderCells[0] = this.myMap.getMap().get(position.addX(5));
                            actions[0] = Action.E;
                            borderCells[1] = this.myMap.getMap().get(position.addY(-5));
                            actions[1] = Action.N;
                            borderCells[2] = this.myMap.getMap().get(position.addX(-5));
                            actions[2] = Action.W;
                            borderCells[3] = this.myMap.getMap().get(position.addY(5));
                            actions[3] = Action.S;
                            
                            borderCells[4] = this.myMap.getMap().get(position.add(5, -1));
                            actions[4] = Action.NE;
                            borderCells[5] = this.myMap.getMap().get(position.add(5, -2));
                            actions[5] = Action.NE;
                            borderCells[6] = this.myMap.getMap().get(position.add(5, -3));
                            actions[6] = Action.NE;
                            borderCells[7] = this.myMap.getMap().get(position.add(5, -4));
                            actions[7] = Action.NE;
                            borderCells[8] = this.myMap.getMap().get(position.add(5, -5));
                            actions[8] = Action.NE;
                            borderCells[9] = this.myMap.getMap().get(position.add(4, -5));
                            actions[9] = Action.NE;
                            borderCells[10] = this.myMap.getMap().get(position.add(3, -5));
                            actions[10] = Action.NE;
                            borderCells[11] = this.myMap.getMap().get(position.add(2, -5));
                            actions[11] = Action.NE;
                            borderCells[12] = this.myMap.getMap().get(position.add(1, -5));
                            actions[12] = Action.NE;
                            
                            borderCells[13] = this.myMap.getMap().get(position.add(-1, -5));
                            actions[13] = Action.NW;
                            borderCells[14] = this.myMap.getMap().get(position.add(-2, -5));
                            actions[14] = Action.NW;
                            borderCells[15] = this.myMap.getMap().get(position.add(-3, -5));
                            actions[15] = Action.NW;
                            borderCells[16] = this.myMap.getMap().get(position.add(-4, -5));
                            actions[16] = Action.NW;
                            borderCells[17] = this.myMap.getMap().get(position.add(-5, -5));
                            actions[17] = Action.NW;
                            borderCells[18] = this.myMap.getMap().get(position.add(-5, -4));
                            actions[18] = Action.NW;
                            borderCells[19] = this.myMap.getMap().get(position.add(-5, -3));
                            actions[19] = Action.NW;
                            borderCells[20] = this.myMap.getMap().get(position.add(-5, -2));
                            actions[20] = Action.NW;
                            borderCells[21] = this.myMap.getMap().get(position.add(-5, -1));
                            actions[21] = Action.NW;
                            
                            borderCells[22] = this.myMap.getMap().get(position.add(-5, 1));
                            actions[22] = Action.SW;
                            borderCells[23] = this.myMap.getMap().get(position.add(-5, 2));
                            actions[23] = Action.SW;
                            borderCells[24] = this.myMap.getMap().get(position.add(-5, 3));
                            actions[24] = Action.SW;
                            borderCells[25] = this.myMap.getMap().get(position.add(-5, 4));
                            actions[25] = Action.SW;
                            borderCells[26] = this.myMap.getMap().get(position.add(-5, 5));
                            actions[26] = Action.SW;
                            borderCells[27] = this.myMap.getMap().get(position.add(-4, 5));
                            actions[27] = Action.SW;
                            borderCells[28] = this.myMap.getMap().get(position.add(-3, 5));
                            actions[28] = Action.SW;
                            borderCells[29] = this.myMap.getMap().get(position.add(-2, 5));
                            actions[29] = Action.SW;
                            borderCells[30] = this.myMap.getMap().get(position.add(-1, 5));
                            actions[30] = Action.SW;
                            
                            borderCells[31] = this.myMap.getMap().get(position.add(1, 5));
                            actions[31] = Action.SE;
                            borderCells[32] = this.myMap.getMap().get(position.add(2, 5));
                            actions[32] = Action.SE;
                            borderCells[33] = this.myMap.getMap().get(position.add(3, 5));
                            actions[33] = Action.SE;
                            borderCells[34] = this.myMap.getMap().get(position.add(4, 5));
                            actions[34] = Action.SE;
                            borderCells[35] = this.myMap.getMap().get(position.add(5, 5));
                            actions[35] = Action.SE;
                            borderCells[36] = this.myMap.getMap().get(position.add(5, 4));
                            actions[36] = Action.SE;
                            borderCells[37] = this.myMap.getMap().get(position.add(5, 3));
                            actions[37] = Action.SE;
                            borderCells[38] = this.myMap.getMap().get(position.add(5, 2));
                            actions[38] = Action.SE;
                            borderCells[39] = this.myMap.getMap().get(position.add(5, 1));
                            actions[39] = Action.SE;
                        } else {
                            borderCells[0] = this.myMap.getMap().get(position.addX(-5));
                            actions[0] = Action.W;
                            borderCells[1] = this.myMap.getMap().get(position.addY(5));
                            actions[1] = Action.S;
                            borderCells[2] = this.myMap.getMap().get(position.addX(5));
                            actions[2] = Action.E;
                            borderCells[3] = this.myMap.getMap().get(position.addY(-5));
                            actions[3] = Action.N;
                            
                            borderCells[4] = this.myMap.getMap().get(position.add(-5, 1));
                            actions[4] = Action.SW;
                            borderCells[5] = this.myMap.getMap().get(position.add(-5, 2));
                            actions[5] = Action.SW;
                            borderCells[6] = this.myMap.getMap().get(position.add(-5, 3));
                            actions[6] = Action.SW;
                            borderCells[7] = this.myMap.getMap().get(position.add(-5, 4));
                            actions[7] = Action.SW;
                            borderCells[8] = this.myMap.getMap().get(position.add(-5, 5));
                            actions[8] = Action.SW;
                            borderCells[9] = this.myMap.getMap().get(position.add(-4, 5));
                            actions[9] = Action.SW;
                            borderCells[10] = this.myMap.getMap().get(position.add(-3, 5));
                            actions[10] = Action.SW;
                            borderCells[11] = this.myMap.getMap().get(position.add(-2, 5));
                            actions[11] = Action.SW;
                            borderCells[12] = this.myMap.getMap().get(position.add(-1, 5));
                            actions[12] = Action.SW;
                            
                            borderCells[13] = this.myMap.getMap().get(position.add(1, 5));
                            actions[13] = Action.SE;
                            borderCells[14] = this.myMap.getMap().get(position.add(2, 5));
                            actions[14] = Action.SE;
                            borderCells[15] = this.myMap.getMap().get(position.add(3, 5));
                            actions[15] = Action.SE;
                            borderCells[16] = this.myMap.getMap().get(position.add(4, 5));
                            actions[16] = Action.SE;
                            borderCells[17] = this.myMap.getMap().get(position.add(5, 5));
                            actions[17] = Action.SE;
                            borderCells[18] = this.myMap.getMap().get(position.add(5, 4));
                            actions[18] = Action.SE;
                            borderCells[19] = this.myMap.getMap().get(position.add(5, 3));
                            actions[19] = Action.SE;
                            borderCells[20] = this.myMap.getMap().get(position.add(5, 2));
                            actions[20] = Action.SE;
                            borderCells[21] = this.myMap.getMap().get(position.add(5, 1));
                            actions[21] = Action.SE;
                            
                            borderCells[22] = this.myMap.getMap().get(position.add(5, -1));
                            actions[22] = Action.NE;
                            borderCells[23] = this.myMap.getMap().get(position.add(5, -2));
                            actions[23] = Action.NE;
                            borderCells[24] = this.myMap.getMap().get(position.add(5, -3));
                            actions[24] = Action.NE;
                            borderCells[25] = this.myMap.getMap().get(position.add(5, -4));
                            actions[25] = Action.NE;
                            borderCells[26] = this.myMap.getMap().get(position.add(5, -5));
                            actions[26] = Action.NE;
                            borderCells[27] = this.myMap.getMap().get(position.add(4, -5));
                            actions[27] = Action.NE;
                            borderCells[28] = this.myMap.getMap().get(position.add(3, -5));
                            actions[28] = Action.NE;
                            borderCells[29] = this.myMap.getMap().get(position.add(2, -5));
                            actions[29] = Action.NE;
                            borderCells[30] = this.myMap.getMap().get(position.add(1, -5));
                            actions[30] = Action.NE;
                            
                            borderCells[31] = this.myMap.getMap().get(position.add(-1, -5));
                            actions[31] = Action.NW;
                            borderCells[32] = this.myMap.getMap().get(position.add(-2, -5));
                            actions[32] = Action.NW;
                            borderCells[33] = this.myMap.getMap().get(position.add(-3, -5));
                            actions[33] = Action.NW;
                            borderCells[34] = this.myMap.getMap().get(position.add(-4, -5));
                            actions[34] = Action.NW;
                            borderCells[35] = this.myMap.getMap().get(position.add(-5, -5));
                            actions[35] = Action.NW;
                            borderCells[36] = this.myMap.getMap().get(position.add(-5, -4));
                            actions[36] = Action.NW;
                            borderCells[37] = this.myMap.getMap().get(position.add(-5, -3));
                            actions[37] = Action.NW;
                            borderCells[38] = this.myMap.getMap().get(position.add(-5, -2));
                            actions[38] = Action.NW;
                            borderCells[39] = this.myMap.getMap().get(position.add(-5, -1));
                            actions[39] = Action.NW;
                        }
                        
                        // Main directions 3x for optimal exploration
                        for (int i = 0; i < 4; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                if (this.lastAction[drone] != actions[i]) {
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                    this.pathToClosestUnexploredCell[drone].add(actions[i]);
                                }
                                this.lastAction[drone] = actions[i];
                                return actions[i];
                            }
                        }
                        
                        // Diagonal directions only once
                        for (int i = 4; i < 40; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                this.lastAction[drone] = actions[i];
                                return actions[i];
                            }
                        }
                }
            }

            // If everything around the drone already has been explored,
            // look for the closest node with unexplored neighbours
            Nodo closestNode = null;
            Nodo currentNode;

            for (Iterator<Nodo> it = this.myMap.getMap().values().iterator();
                    it.hasNext();) {

                currentNode = it.next();
                if (currentNode.getRadar() == 0 && !currentNode.explored()
                        && (closestNode == null || position.distanciaA(currentNode.getCoord()) < position.distanciaA(closestNode.getCoord()))) {

                    closestNode = currentNode;
                }
            }

            if (closestNode == null) {
                System.err.println("ERROR: mapv3 called, but map already explored completely");
                return null;
            }

            // Find way to the previously found closest node
            this.pathToClosestUnexploredCell[drone] = dijkstra(this.myMap.getMap().get(position), closestNode);
        }

        // Return next action to follow the path to the closest unexplored node
        return this.pathToClosestUnexploredCell[drone].pop();
    }

    /**
     * Fuel heuristic
     *
     * @param pasos steps from current position to reach goal
     * @param drone the integer of the current bot
     * @param goal goal Nodo
     * @return true if steps to reach goal equals pasos, false otherwise
     * @author Daniel Sánchez Alcaide
     */
    private boolean fuelH(int drone, Nodo goal) throws Exception {
        boolean res = false;
        int consumo = 1;
        switch (drones.get(drone).getRole()) {
            //mosca
            case 0:
                consumo = 2;
                break;
            //pájaro (del terror)
            case 1:
                consumo = 1;
                break;
            //halcón (milenario)
            case 2:
                consumo = 4;
                break;
        }
        if (!zoneGoalFound && drones.get(drone).getFuel() <= consumo) {
            res = true;
        } else {
            HashMap<Coord, Nodo> map = myMap.getMap();
            Nodo current = new Nodo(drones.get(drone).getCurrent().getX(),
                    drones.get(drone).getCurrent().getY(),
                    map.get(drones.get(drone).getCurrent()).getRadar());
            if (busqueda(current, goal).capacity() * consumo == 100) {
                //Esto consume mucho tiempo de CPU, es mejor crear una variable en la clase y
                //guardar ahí la pila cuando se llame a la búsqueda desde los mapeos o desde donde sea
                res = true;
            }
        }
        return res;
    }
}
