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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
    private Decepticon dron1, dron2, dron3, dron4;
    private boolean map2_comprobation = false;
    private int map2_contador = 0;
    private boolean map2_direccion = true;
    private boolean map2_iod = false;
    private boolean map2_bordeando = false;
    private Nodo nodoGoal;

    private State state;
    private String msg;
    private boolean live;
    private Action sigAction;
    private int numeroDron;

    private int pasos = 0;
    private boolean zoneGoalFound = false;

    // For mapv3
    private Stack<Action> pathToClosestUnexploredCell = new Stack<>();
    private Action lastAction = null;
    
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

    public Megatron(AgentID aid) throws Exception {
        super(aid);
        drones = new ArrayList<DataDecepticon>(4);
    }

    @Override
    protected void init() {
        this.myMap = new Map();
        this.dataAccess = DataAccess.crearInstancia();
        this.drones.add(new DataDecepticon(DataAccess.getNameDron1()));
        this.drones.add(new DataDecepticon(DataAccess.getNameDron2()));
        this.drones.add(new DataDecepticon(DataAccess.getNameDron3()));
        this.drones.add(new DataDecepticon(DataAccess.getNameDron4()));

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
        Comparator<Nodo> comp = new ComparadorHeuristicaNodo();
        PriorityQueue<Nodo> abiertos;
        abiertos = new PriorityQueue<>(10, comp);
        //abiertos = new PriorityQueue<>();

        ArrayList<Nodo> cerrados;
        cerrados = new ArrayList<>();
        Stack<Action> caminito;
        caminito = new Stack<>();
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
     * 14 13 12 11 10
     * 15  x  x  x  9
     *  0  x  D  x  8
     *  1  x  x  x  7
     *  2  3  4  5  6
     *
     * Beginning with the cell 0 the method tries to find a cell at the border
     * of the visual range of the drone that has not yet been explored
     * completely and is not a wall. If the cell i does not match these
     * criteria, proceed with cell i+1. If none of the border cells is
     * considered usable, use a path finding algorithm to get to the next
     * unexplored cell, there resetting back to the exploration pattern.
     *
     * @param drone ID of the drone
     * @return Next action for the specified drone
     * @author Alexander Straub
     */
    private Action mapv3(int drone) throws Exception {
        Action ret = mapv3_(drone);

        // If mapv3 returns an illegal action, stop there (maybe then use another
        // heuristic)
        if (this.myMap.getMap().get(this.drones.get(drone).getCurrent().neighbour(ret)).getRadar() == 1 || 
                this.myMap.getMap().get(this.drones.get(drone).getCurrent().neighbour(ret)).getRadar() == 2) {
            System.err.println("ERROR: mapv3 devolvió una acción ilegal");
            
            this.pathToClosestUnexploredCell.clear();
            ret = mapv3_(drone);
        }

        return ret;
    }

    /**
     * Exploration of the following form: (example 5x5)
     *
     * 14 13 12 11 10
     * 15  x  x  x  9
     *  0  x  D  x  8
     *  1  x  x  x  7
     *  2  3  4  5  6
     *
     * Beginning with the cell 0 the method tries to find a cell at the border
     * of the visual range of the drone that has not yet been explored
     * completely and is not a wall. If the cell i does not match these
     * criteria, proceed with cell i+1. If none of the border cells is
     * considered usable, use a path finding algorithm to get to the next
     * unexplored cell, there resetting back to the exploration pattern.
     *
     * PLEASE do NOT call this function directly but call mapv3(int).
     * 
     * @param drone ID of the drone
     * @return Next action for the specified drone
     * @author Alexander Straub
     */
    private Action mapv3_(int drone) throws Exception {
        Coord position = this.drones.get(drone).getCurrent();

        if (this.pathToClosestUnexploredCell.isEmpty()) {
            // Get the border cells of the visual range of the specified drone
            Nodo borderCell;

            switch (this.drones.get(drone).getRole()) {
                case 0: // Mosca
                    borderCell = this.myMap.getMap().get(position.W());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.W) {
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                        }
                        this.lastAction = Action.W;
                        return Action.W;
                    }

                    borderCell = this.myMap.getMap().get(position.S());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.S) {
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                        }
                        this.lastAction = Action.S;
                        return Action.S;
                    }

                    borderCell = this.myMap.getMap().get(position.E());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.E) {
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                        }
                        this.lastAction = Action.E;
                        return Action.E;
                    }

                    borderCell = this.myMap.getMap().get(position.N());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.N) {
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                        }
                        this.lastAction = Action.N;
                        return Action.N;
                    }
                    
                    borderCell = this.myMap.getMap().get(position.SW());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }
                    
                    borderCell = this.myMap.getMap().get(position.SE());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.NE());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.NW());
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }
                    break;
                case 1: // Pájaro
                    borderCell = this.myMap.getMap().get(position.addX(-2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.W) {
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                        }
                        this.lastAction = Action.W;
                        return Action.W;
                    }

                    borderCell = this.myMap.getMap().get(position.addY(2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.S) {
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                        }
                        this.lastAction = Action.S;
                        return Action.S;
                    }

                    borderCell = this.myMap.getMap().get(position.addX(2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.E) {
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                        }
                        this.lastAction = Action.E;
                        return Action.E;
                    }

                    borderCell = this.myMap.getMap().get(position.addY(-2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.N) {
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                        }
                        this.lastAction = Action.N;
                        return Action.N;
                    }
                    
                    borderCell = this.myMap.getMap().get(position.add(-2, 1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-2, 2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-1, 2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }
                    
                    borderCell = this.myMap.getMap().get(position.add(1, 2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(2, 2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(2, 1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }
                    
                    borderCell = this.myMap.getMap().get(position.add(2, -1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(2, -2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(1, -2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-1, -2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-2, -2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-2, -1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }
                    break;
                case 2: // Halcón
                    borderCell = this.myMap.getMap().get(position.addX(-5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.W) {
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                            this.pathToClosestUnexploredCell.add(Action.W);
                        }
                        this.lastAction = Action.W;
                        return Action.W;
                    }
                
                    borderCell = this.myMap.getMap().get(position.addY(5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.S) {
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                            this.pathToClosestUnexploredCell.add(Action.S);
                        }
                        this.lastAction = Action.S;
                        return Action.S;
                    }

                    borderCell = this.myMap.getMap().get(position.addX(5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.E) {
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                            this.pathToClosestUnexploredCell.add(Action.E);
                        }
                        this.lastAction = Action.E;
                        return Action.E;
                    }
                
                    borderCell = this.myMap.getMap().get(position.addY(-5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        if (this.lastAction != Action.N) {
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                            this.pathToClosestUnexploredCell.add(Action.N);
                        }
                        this.lastAction = Action.N;
                        return Action.N;
                    }
                
                    borderCell = this.myMap.getMap().get(position.add(-5, 1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, 2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, 3));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, 4));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-4, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-3, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-2, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-1, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SW;
                        return Action.SW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(1, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(2, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(3, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(4, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, 5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, 4));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, 3));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, 2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, 1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.SE;
                        return Action.SE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, -1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, -2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, -3));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, -4));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(5, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(4, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(3, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(2, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(1, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NE;
                        return Action.NE;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-1, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-2, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-3, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-4, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, -5));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, -4));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, -3));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, -2));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
                    }

                    borderCell = this.myMap.getMap().get(position.add(-5, -1));
                    if (borderCell != null && borderCell.getRadar() != 1 && borderCell.getRadar() != 2 && !borderCell.explored()) {
                        this.lastAction = Action.NW;
                        return Action.NW;
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
            this.pathToClosestUnexploredCell = busqueda(this.myMap.getMap().get(position), closestNode);
        }

        // Return next action to follow the path to the closest unexplored node
        return this.pathToClosestUnexploredCell.pop();
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
