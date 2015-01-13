package practica3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * Base class for all decepticons
 * 
 * @author Antonio Troitiño del Rio, Alexander Straub
 */
public class Decepticon extends SingleAgent {

    // ID of Megatron
    private final AgentID megatron;
    
    // Information about the decepticon
    private final int role;
    private boolean alive;
    
    // Communication
    private final String key;
    private final JsonDBA json;
    
    // Name
    protected String name = "Decepticon";
    
    // Reference to the map
    protected Map map;
    
    // State of the decepticon
    private Coord currentPosition, lastPosition, startPosition;
    private int fuel;
    Megatron.Action lastAction;
    private boolean inGoal;
    private Coord myGoal;

    /**
     * Constructor
     * 
     * @param aid ID of the new decepticon
     * @param megatron ID of megatron
     * @param role Type: 0-Flydron, 1-Birdron, 2-Falcdron
     * @param key Key for communication
     * @param map Reference to the map
     * @throws Exception 
     * @author Antonio Troitiño del Rio
     */
    public Decepticon(AgentID aid, AgentID megatron, int role, String key, Map map) throws Exception {
        super(aid);
        this.megatron = megatron;
        this.alive = true;
        this.role = role;
        this.key = key;
        this.json = new JsonDBA();
        this.map = map;
        this.currentPosition = null;
        this.lastPosition = null;
        this.startPosition = null;
        this.fuel = 100;
        this.lastAction = null;
        this.inGoal = false;
        this.myGoal = null;
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
        hm.put("rol", this.role);
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
    public final int getRole() {
        return this.role;
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
    
    /**
     * Sets the new position
     * 
     * @param newPosition New position
     * @author Alexander Straub
     */
    public void setPosition(Coord newPosition) {
        if (this.startPosition == null)
            this.startPosition = newPosition;
        
        this.lastPosition = this.currentPosition;
        this.currentPosition = newPosition;
        
        if (lastPosition != null) {
            if (lastPosition.NW().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.NW;
            } else if (lastPosition.N().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.N;
            } else if (lastPosition.NE().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.NE;
            } else if (lastPosition.E().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.E;
            } else if (lastPosition.SE().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.SE;
            } else if (lastPosition.S().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.S;
            } else if (lastPosition.SW().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.SW;
            } else if (lastPosition.W().equals(this.currentPosition)) {
                this.lastAction = Megatron.Action.W;
            }
        }
        
        if (this.currentPosition.equals(this.myGoal)) {
            this.inGoal = true;
        }
    }
    
    /**
     * Returns the current position
     * 
     * @return Current position
     * @author Antonio Troitiño del Río
     */
    public Coord getPosition() {
        return this.currentPosition;
    }
    
    /**
     * Returns the last position
     * 
     * @return Last position
     * @author Antonio Troitiño del Río
     */
    public Coord getLastPosition() {
        return this.lastPosition;
    }
    
    /**
     * Returns the initial position
     * 
     * @return Initial position
     * @author Antonio Troitiño del Río
     */
    public Coord getStartPosition() {
        return this.startPosition;
    }
    
    /**
     * Sets the new amount of fuel
     * 
     * @param fuel New amount of fuel
     * @author Antonio Troitiño del Río
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
    }
    
    /**
     * Returns the amount of fuel left
     * 
     * @return Fuel
     * @author Antonio Troitiño del Río
     */
    public int getFuel() {
        return this.fuel;
    }
    
    /**
     * Returns the last action executed
     * 
     * @return Last action
     * @author Antonio Troitiño del Río
     */
    public Megatron.Action getLastAction() {
        return this.lastAction;
    }
    
    /**
     * Sets a new target for this decepticon
     * 
     * @param newGoal New target
     * @author Antonio Troitiño del Río
     */
    public void setMyGoal(Coord newGoal) {
        this.myGoal = newGoal;
        
        if (this.myGoal.equals(this.currentPosition))
            this.inGoal = true;
    }
    
    /**
     * Returns the target of this decepticon
     * 
     * @return Target
     * @author Antonio Troitiño del Río
     */
    public Coord getMyGoal() {
       return this.myGoal;
    }
    
    /**
     * Is the decepticon already at its target?
     * 
     * @return True if decepticon is at its target
     * @author Antonio Troitiño del Río
     */
    public boolean isInGoal() {
        return this.inGoal;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Functions for search ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    // For mapv0
    protected Stack<Megatron.Action> map0_pathToUnexploredCell = new Stack<>();
    
    // For mapv1
    protected Stack<Megatron.Action> map1_pathToUnexploredCell = new Stack<>();
    
    // For mapv2
    private boolean map2_comprobation = false;
    private int map2_contador = 0;
    private boolean map2_direccion = true;
    private boolean map2_iod = false;
    private boolean map2_bordeando = false;
    
    // For mapv3
    protected Stack<Megatron.Action> map3_pathToUnexploredCell = new Stack<>();
    protected Megatron.Action map3_lastAction = null;
    
    /**
     * The best path to reach the goal, once we have found it, using A*
     *
     * @return Direction where drone should move
     * @param start the position of the selected drone
     * @param goal the position of the goal
     * @throws Exception
     * @author Daniel Sánchez Alcaide
     */
    public final Stack<Megatron.Action> aStar(Nodo start, Nodo goal) throws Exception {
        Comparator<Nodo> comp = new ComparadorHeuristicaNodo(goal);
        PriorityQueue<Nodo> abiertos = new PriorityQueue<>(10, comp);
        ArrayList<Nodo> cerrados = new ArrayList<>();
        
        Stack<Megatron.Action> caminito = new Stack<>();
        
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
                    caminito.add(Megatron.Action.S);
                }
                //El padre está al sur
                if (current.getCamino().getCoord().equals(current.S())) {
                    caminito.add(Megatron.Action.N);
                }
                //El padre está al este
                if (current.getCamino().getCoord().equals(current.E())) {
                    caminito.add(Megatron.Action.W);
                }
                //El padre está al oeste
                if (current.getCamino().getCoord().equals(current.O())) {
                    caminito.add(Megatron.Action.E);
                }
                //El padre está al noreste
                if (current.getCamino().getCoord().equals(current.NE())) {
                    caminito.add(Megatron.Action.SW);
                }
                //El padre está al noroeste
                if (current.getCamino().getCoord().equals(current.NO())) {
                    caminito.add(Megatron.Action.SE);
                }
                //El padre está al sureste
                if (current.getCamino().getCoord().equals(current.SE())) {
                    caminito.add(Megatron.Action.NW);
                }
                //El padre está al suroeste
                if (current.getCamino().getCoord().equals(current.SO())) {
                    caminito.add(Megatron.Action.NE);
                }
            }
        }
        //Limpieza de Nodos
        Collection<Nodo> m = this.map.getMap().values();
        for(Nodo n : m){
            n.setCamino(null);
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
    public final Stack<Megatron.Action> dijkstra(Nodo start, Nodo target) {
        // Sanity check for the parameters
        if (start == null || target == null || start.equals(target)) return null;
        
        // Get the map
        HashMap<Coord, Nodo> localMap = this.map.getAccessibleMap();
        start = localMap.get(start.getCoord());
        target = localMap.get(target.getCoord());

        // Initialize
        List<Nodo> nodes = new ArrayList<>(localMap.values());
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
            for (Nodo neighbour : minNode.getAdy()) {
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
        Stack<Megatron.Action> actions = new Stack<>();
        
        while (target != null && target != start) {
            // Get next direction
            if (target.getCoord().equals(target.getCamino().getCoord().NW())) {
                actions.push(Megatron.Action.NW);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().N())) {
                actions.push(Megatron.Action.N);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().NE())) {
                actions.push(Megatron.Action.NE);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().E())) {
                actions.push(Megatron.Action.E);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().SE())) {
                actions.push(Megatron.Action.SE);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().S())) {
                actions.push(Megatron.Action.S);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().SW())) {
                actions.push(Megatron.Action.SW);
            }
            if (target.getCoord().equals(target.getCamino().getCoord().W())) {
                actions.push(Megatron.Action.W);
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
    public Megatron.Action mapv0() {
        Megatron.Action toDo;
        HashMap<Coord, Nodo> localMap = this.map.getMap();
        
        if (this.map0_pathToUnexploredCell.isEmpty()) {
            char pos;
            if (this.startPosition.getY() > 5) {
                pos = 'S';
            } else {
                pos = 'N';
            }
            switch (pos) {
                case 'S':
                    if ((this.lastAction == Megatron.Action.W
                            && !localMap.containsKey(this.currentPosition.W().W().W())
                            && localMap.get(this.currentPosition.W().W()).getRadar() != 2)
                            || (this.lastAction == Megatron.Action.N
                            && !localMap.containsKey(this.currentPosition.SW().SW().W())
                            && localMap.get(this.currentPosition.W().W()).getRadar() != 2)) {
                        toDo = Megatron.Action.W;
                        this.map0_pathToUnexploredCell.push(toDo);
                    } else if ((this.lastAction == Megatron.Action.E
                            && !localMap.containsKey(this.currentPosition.E().E().E())
                            && localMap.get(this.currentPosition.E().E()).getRadar() != 2)
                            || (this.lastAction == Megatron.Action.N
                            && !localMap.containsKey(this.currentPosition.SE().SE().E())
                            && localMap.get(this.currentPosition.E().E()).getRadar() != 2)) {
                        toDo = Megatron.Action.E;
                        this.map0_pathToUnexploredCell.push(toDo);
                    } else {
                        toDo = Megatron.Action.N;
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                    }
                    break;
                case 'N':
                    if ((this.lastAction == Megatron.Action.W
                            && !localMap.containsKey(this.currentPosition.W().W().W())
                            && localMap.get(this.currentPosition.W().W()).getRadar() != 2)
                            || (this.lastAction == Megatron.Action.S
                            && !localMap.containsKey(this.currentPosition.NW().NW().W())
                            && localMap.get(this.currentPosition.W().W()).getRadar() != 2)) {
                        toDo = Megatron.Action.W;
                        this.map0_pathToUnexploredCell.push(toDo);
                    } else if ((this.lastAction == Megatron.Action.E
                            && !localMap.containsKey(this.currentPosition.E().E().E())
                            && localMap.get(this.currentPosition.E().E()).getRadar() != 2)
                            || (this.lastAction == Megatron.Action.S
                            && !localMap.containsKey(this.currentPosition.NE().NE().E())
                            && localMap.get(this.currentPosition.E().E()).getRadar() != 2)) {
                        toDo = Megatron.Action.E;
                        this.map0_pathToUnexploredCell.push(toDo);
                    } else {
                        toDo = Megatron.Action.S;
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                        this.map0_pathToUnexploredCell.push(toDo);
                    }
                    break;
                default:
                    System.err.println("Error: posición de comienzo del dron no inicializada");

                    break;
            }
        }
        
        toDo = this.map0_pathToUnexploredCell.pop();
        if (toDo == Megatron.Action.N && localMap.get(this.currentPosition.N()).getRadar() != 2) {
            return toDo;
        } else if (toDo == Megatron.Action.S && localMap.get(this.currentPosition.S()).getRadar() != 2) {
            return toDo;
        } else {
            return null;
        }
    }

    /**
     * Search a given region in one of two different ways
     *
     * @param mode Mode of the drone: 0 - North/South, 1 - East/West
     * @param regionMin Lower bound of the assigned region
     * @param regionMax Upper bound of the assigned region
     * @return Next action for the specified drone
     * @throws Exception
     * @deprecated 
     * @author Alexander Straub
     */
    public Megatron.Action mapv1(int mode, Coord regionMin, Coord regionMax) throws Exception {
        if (this.map1_pathToUnexploredCell.isEmpty()) {
            // Get search window size in one direction
            int offset = 5;
            if (this.role == 0) {
                offset = 1;
            } else if (this.role == 1) {
                offset = 2;
            }

            int sign = 1;

            // Store coord to check
            Coord coord = this.currentPosition;

            switch (mode) {
                case 0: // North/South
                    if (this.lastAction == Megatron.Action.N) {
                        sign = -1;
                    }
                    coord.setY(coord.getY() + sign * offset);

                    // If coord hits a wall, go sideways then opposite direction
                    if (coord.getY() < regionMin.getY() || coord.getY() > regionMax.getY()
                            || (this.map.getMap().containsKey(coord) && this.map.getMap().get(coord).getRadar() == 1)) {

                        // Initialize to get as furthest to the side as the current
                        // search window
                        coord = this.currentPosition;
                        boolean possible = true;
                        int sideDirection = 1; // 1 - West, -1 - East, TODO: get this info from somewhere
                        int stepsSide = 0, stepsOpposite = 0;

                        // While it is possible to go sideways, do it
                        while (possible && stepsSide < offset && stepsOpposite < offset) { // TODO: prevent from going outside the region
                            // If sideways is free, go there
                            if (this.map.getMap().get(new Coord(coord.getX() + sideDirection, coord.getY())).getRadar() != 1) {
                                coord.setX(coord.getX() + sideDirection);
                                stepsSide++;

                                if (sideDirection == 1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.W);
                                } else if (sideDirection == -1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.E);
                                }
                            } // If sideways is not possible, but sideways and in the
                            // opposite direction, go there
                            else if (this.map.getMap().get(new Coord(coord.getX() + sideDirection, coord.getY() - sign)).getRadar() != 1) {
                                coord.setX(coord.getX() + sideDirection);
                                coord.setY(coord.getY() - sign);
                                stepsSide++;
                                stepsOpposite++;

                                if (sideDirection == 1 && sign == -1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.SW);
                                } else if (sideDirection == 1 && sign == 1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.NW);
                                } else if (sideDirection == -1 && sign == -1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.SE);
                                } else if (sideDirection == -1 && sign == 1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.NE);
                                }
                            } else {
                                possible = false;
                            }
                        }

                        // After the last side step go into the opposite direction,
                        // thus this algorithm knows where to go in the next
                        // execution
                        if (this.map.getMap().get(new Coord(coord.getX(), coord.getY() - sign)).getRadar() != 1) {
                            if (sign == -1) {
                                this.map1_pathToUnexploredCell.push(Megatron.Action.S);
                            } else if (sign == 1) {
                                this.map1_pathToUnexploredCell.push(Megatron.Action.N);
                            }
                        }
                    } else {
                        this.map1_pathToUnexploredCell.push(this.lastAction);
                    }

                    break;
                case 1: // East/West
                    if (this.lastAction == Megatron.Action.W) {
                        sign = -1;
                    }
                    coord.setX(coord.getX() + sign * offset);

                    // If coord hits a wall, go sideways then opposite direction
                    if (coord.getX() < regionMin.getX() || coord.getX() > regionMax.getX()
                            || (this.map.getMap().containsKey(coord) && this.map.getMap().get(coord).getRadar() == 1)) {

                        // Initialize to get as furthest to the side as the current
                        // search window
                        coord = this.currentPosition;
                        boolean possible = true;
                        int sideDirection = 1; // 1 - South, -1 - North, TODO: get this info from somewhere
                        int stepsSide = 0, stepsOpposite = 0;

                        // While it is possible to go sideways, do it
                        while (possible && stepsSide < offset && stepsOpposite < offset) { // TODO: prevent from going outside the region
                            // If sideways is free, go there
                            if (this.map.getMap().get(new Coord(coord.getX(), coord.getY() + sideDirection)).getRadar() != 1) {
                                coord.setY(coord.getY() + sideDirection);
                                stepsSide++;

                                if (sideDirection == 1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.S);
                                } else if (sideDirection == -1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.N);
                                }
                            } // If sideways is not possible, but sideways and in the
                            // opposite direction, go there
                            else if (this.map.getMap().get(new Coord(coord.getX() - sign, coord.getY() + sideDirection)).getRadar() != 1) {
                                coord.setX(coord.getX() - sign);
                                coord.setY(coord.getY() + sideDirection);
                                stepsSide++;
                                stepsOpposite++;

                                if (sideDirection == 1 && sign == -1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.SE);
                                } else if (sideDirection == 1 && sign == 1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.SW);
                                } else if (sideDirection == -1 && sign == -1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.NE);
                                } else if (sideDirection == -1 && sign == 1) {
                                    this.map1_pathToUnexploredCell.push(Megatron.Action.NW);
                                }
                            } else {
                                possible = false;
                            }
                        }

                        // After the last side step go into the opposite direction,
                        // thus this algorithm knows where to go in the next
                        // execution
                        if (this.map.getMap().get(new Coord(coord.getX() - sign, coord.getY())).getRadar() != 1) {
                            if (sign == -1) {
                                this.map1_pathToUnexploredCell.push(Megatron.Action.E);
                            } else if (sign == 1) {
                                this.map1_pathToUnexploredCell.push(Megatron.Action.W);
                            }
                        }
                    } else {
                        this.map1_pathToUnexploredCell.push(this.lastAction);
                    }

                    break;
            }
        }

        return this.map1_pathToUnexploredCell.pop();
    }

    /**
     * Search the whole map in North-South-pattern
     *
     * @return Next action for the specified drone
     * @throws Exception
     * @deprecated 
     * @author Alexander Straub
     */
    public Megatron.Action mapv1() throws Exception {
        return mapv1(0, new Coord(0, 0), new Coord(100, 100));
    }

    /**
     * Search the whole map in one of two possible ways
     *
     * @param mode Mode of the drone: 0 - North/South, 1 - East/West
     * @return Next action for the specified drone
     * @throws Exception
     * @deprecated 
     * @author Alexander Straub
     */
    public Megatron.Action mapv1(int mode) throws Exception {
        return mapv1(mode, new Coord(0, 0), new Coord(100, 100));
    }

    /**
     * Search a region of the map using North-South-pattern
     *
     * @param regionMin Lower bound of the assigned region
     * @param regionMax Upper bound of the assigned region
     * @return Next action for the specified drone
     * @throws Exception
     * @deprecated 
     * @author Alexander Straub
     */
    public Megatron.Action mapv1(Coord regionMin, Coord regionMax) throws Exception {
        return mapv1(0, regionMin, regionMax);
    }

    /**
     * Go to origin or next coord not explored
     *
     * @return next action to be done by specified drone
     * @throws java.lang.Exception
     * @author Jesús Cobo Sánchez
     */
    public Megatron.Action mapv2() throws Exception {
        Megatron.Action actions = null;
        HashMap<Coord, Nodo> localMap = this.map.getMap();
        Nodo current;
        Nodo next = null;
        current = new Nodo(this.currentPosition.getX(), this.currentPosition.getY(),
                localMap.get(this.currentPosition).getRadar());

        if (map2_bordeando) {
            //funcion de bordear obstaculos
        }

        if (map2_comprobation == false) {
            //ir esquina inferor derecha
            if (current.getY() > 50) {
                map2_direccion = false;
                map2_iod = false;
                if (localMap.get(current.SE()).getRadar() == 2 && localMap.get(current.S()).getRadar() == 2
                        && localMap.get(current.E()).getRadar() == 2) {
                    map2_comprobation = true;
                } else {
                    if (localMap.get(current.E()).getRadar() == 0) {
                        actions = Megatron.Action.E;
                    } else if (localMap.get(current.SE()).getRadar() == 0) {
                        actions = Megatron.Action.SE;
                    } else if (localMap.get(current.S()).getRadar() == 0) {
                        actions = Megatron.Action.S;
                    } else if (localMap.get(current.NE()).getRadar() == 0) {
                        actions = Megatron.Action.NE;
                    } else if (localMap.get(current.N()).getRadar() == 0) {
                        actions = Megatron.Action.N;
                    } else if (localMap.get(current.NO()).getRadar() == 0) {
                        actions = Megatron.Action.NW;
                    } else if (localMap.get(current.O()).getRadar() == 0) {
                        actions = Megatron.Action.W;
                    } else {
                        actions = Megatron.Action.SW;
                    }
                }
                //ir esquina superior izquierda
            } else {
                map2_direccion = true;
                map2_iod = true;
                if (localMap.get(current.NO()).getRadar() == 2 && localMap.get(current.N()).getRadar() == 2
                        && localMap.get(current.O()).getRadar() == 2) {
                    map2_comprobation = true;
                } else {
                    if (localMap.get(current.NO()).getRadar() == 0) {
                        actions = Megatron.Action.NW;
                    } else if (localMap.get(current.N()).getRadar() == 0) {
                        actions = Megatron.Action.N;
                    } else if (localMap.get(current.O()).getRadar() == 0) {
                        actions = Megatron.Action.W;
                    } else if (localMap.get(current.SO()).getRadar() == 0) {
                        actions = Megatron.Action.SW;
                    } else if (localMap.get(current.NE()).getRadar() == 0) {
                        actions = Megatron.Action.NE;
                    } else if (localMap.get(current.E()).getRadar() == 0) {
                        actions = Megatron.Action.E;
                    } else if (localMap.get(current.SE()).getRadar() == 0) {
                        actions = Megatron.Action.SE;
                    } else {
                        actions = Megatron.Action.S;
                    }
                }

            }
            //segunda ruta
        } else {
            //hacia abajo e izquierda
            if (!map2_iod && map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.S()).getRadar() == 0) {
                    actions = Megatron.Action.S;
                } else if (localMap.get(current.S()).getRadar() == 2) {
                    map2_contador -= 3;
                    actions = Megatron.Action.W;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                }
                //hacia arriba e izquierda
            } else if (!map2_iod && !map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.N()).getRadar() == 0) {
                    actions = Megatron.Action.N;
                } else if (localMap.get(current.N()).getRadar() == 2) {
                    map2_direccion = true;
                    map2_contador -= 3;
                    actions = Megatron.Action.W;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                }
                //hacia abajo y derecha
            } else if (map2_iod && map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.S()).getRadar() == 0) {
                    actions = Megatron.Action.S;
                } else if (localMap.get(current.S()).getRadar() == 2) {
                    map2_contador += 3;
                    actions = Megatron.Action.E;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                }
                //hacia arriba y derecha
            } else if (map2_iod && !map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.N()).getRadar() == 0) {
                    actions = Megatron.Action.N;
                } else if (localMap.get(current.N()).getRadar() == 2) {
                    map2_direccion = true;
                    map2_contador += 3;
                    actions = Megatron.Action.E;
                } else {
                    map2_bordeando = true;
                    //bordear obstaculo bordearObstaculo(coord x)
                }
                //ir hacia la x cambiada
            } else {
                //ir hacia la izquierda
                if (current.getX() > map2_contador) {
                    if (localMap.get(current.E()).getRadar() == 0) {
                        actions = Megatron.Action.E;
                    } else {
                        //bordear
                        map2_bordeando = true;
                    }
                    //ir hacia la derecha
                } else {
                    if (localMap.get(current.O()).getRadar() == 0) {
                        actions = Megatron.Action.W;
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
     * @return Next action for the specified drone
     * @throws java.lang.Exception
     * @author Alexander Straub
     */
    public Megatron.Action mapv3() throws Exception {
        Megatron.Action ret = mapv3(false);

        // If mapv3 returns an illegal action, try again
        if (this.map.getMap().get(this.currentPosition.neighbour(ret)).getRadar() == 1 || 
                this.map.getMap().get(this.currentPosition.neighbour(ret)).getRadar() == 2) {
            System.err.println("ERROR: mapv3 devolvió una acción ilegal");
            
            this.map3_pathToUnexploredCell.clear();
            ret = mapv3(true);
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
     * @param findWay True: Don't look at border cells but only for a close cell
     * @return Next action for the specified drone
     * @author Alexander Straub
     */
    private Megatron.Action mapv3(boolean findWay) throws Exception {
        Coord position = this.currentPosition;

        if (this.map3_pathToUnexploredCell.isEmpty()) {
            // Get the border cells of the visual range of the specified drone
            if (!findWay) {
                Nodo[] borderCells;
                Megatron.Action[] actions;
                
                switch (this.role) {
                    case 0: // Mosca
                        borderCells = new Nodo[8];
                        actions = new Megatron.Action[8];
                        
                        // Different order of directions, depending on start position
                        if (this.startPosition.getY() == 0) {
                            borderCells[0] = this.map.getMap().get(position.E());
                            actions[0] = Megatron.Action.E;
                            borderCells[1] = this.map.getMap().get(position.N());
                            actions[1] = Megatron.Action.N;
                            borderCells[2] = this.map.getMap().get(position.W());
                            actions[2] = Megatron.Action.W;
                            borderCells[3] = this.map.getMap().get(position.S());
                            actions[3] = Megatron.Action.S;
                            
                            borderCells[4] = this.map.getMap().get(position.NE());
                            actions[4] = Megatron.Action.NE;
                            borderCells[5] = this.map.getMap().get(position.NW());
                            actions[5] = Megatron.Action.NW;
                            borderCells[6] = this.map.getMap().get(position.SW());
                            actions[6] = Megatron.Action.SW;
                            borderCells[7] = this.map.getMap().get(position.SE());
                            actions[7] = Megatron.Action.SE;
                        } else {
                            borderCells[0] = this.map.getMap().get(position.W());
                            actions[0] = Megatron.Action.W;
                            borderCells[1] = this.map.getMap().get(position.S());
                            actions[1] = Megatron.Action.S;
                            borderCells[2] = this.map.getMap().get(position.E());
                            actions[2] = Megatron.Action.E;
                            borderCells[3] = this.map.getMap().get(position.N());
                            actions[3] = Megatron.Action.N;
                            
                            borderCells[4] = this.map.getMap().get(position.SW());
                            actions[4] = Megatron.Action.SW;
                            borderCells[5] = this.map.getMap().get(position.SE());
                            actions[5] = Megatron.Action.SE;
                            borderCells[6] = this.map.getMap().get(position.NE());
                            actions[6] = Megatron.Action.NE;
                            borderCells[7] = this.map.getMap().get(position.NW());
                            actions[7] = Megatron.Action.NW;
                        }
                        
                        // Main directions 3x for optimal exploration
                        for (int i = 0; i < 4; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                if (this.map3_lastAction != actions[i]) {
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                }
                                this.map3_lastAction = actions[i];
                                return actions[i];
                            }
                        }
                        
                        // Diagonal directions only once
                        for (int i = 4; i < 8; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                this.map3_lastAction = actions[i];
                                return actions[i];
                            }
                        }
                        break;
                    case 1: // Pájaro
                        borderCells = new Nodo[16];
                        actions = new Megatron.Action[16];
                        
                        // Different order of directions, depending on start position
                        if (this.startPosition.getY() == 0) {
                            borderCells[0] = this.map.getMap().get(position.addX(2));
                            actions[0] = Megatron.Action.E;
                            borderCells[1] = this.map.getMap().get(position.addY(-2));
                            actions[1] = Megatron.Action.N;
                            borderCells[2] = this.map.getMap().get(position.addX(-2));
                            actions[2] = Megatron.Action.W;
                            borderCells[3] = this.map.getMap().get(position.addY(2));
                            actions[3] = Megatron.Action.S;
                            
                            borderCells[4] = this.map.getMap().get(position.add(2, -1));
                            actions[4] = Megatron.Action.NE;
                            borderCells[5] = this.map.getMap().get(position.add(2, -2));
                            actions[5] = Megatron.Action.NE;
                            borderCells[6] = this.map.getMap().get(position.add(1, -2));
                            actions[6] = Megatron.Action.NE;
                            
                            borderCells[7] = this.map.getMap().get(position.add(-1, -2));
                            actions[7] = Megatron.Action.NW;
                            borderCells[8] = this.map.getMap().get(position.add(-2, -2));
                            actions[8] = Megatron.Action.NW;
                            borderCells[9] = this.map.getMap().get(position.add(-2, -1));
                            actions[9] = Megatron.Action.NW;
                            
                            borderCells[10] = this.map.getMap().get(position.add(-2, 1));
                            actions[10] = Megatron.Action.SW;
                            borderCells[11] = this.map.getMap().get(position.add(-2, 2));
                            actions[11] = Megatron.Action.SW;
                            borderCells[12] = this.map.getMap().get(position.add(-1, 2));
                            actions[12] = Megatron.Action.SW;
                            
                            borderCells[13] = this.map.getMap().get(position.add(1, 2));
                            actions[13] = Megatron.Action.SE;
                            borderCells[14] = this.map.getMap().get(position.add(2, 2));
                            actions[14] = Megatron.Action.SE;
                            borderCells[15] = this.map.getMap().get(position.add(2, 1));
                            actions[15] = Megatron.Action.SE;
                        } else {
                            borderCells[0] = this.map.getMap().get(position.addX(-2));
                            actions[0] = Megatron.Action.W;
                            borderCells[1] = this.map.getMap().get(position.addY(2));
                            actions[1] = Megatron.Action.S;
                            borderCells[2] = this.map.getMap().get(position.addX(2));
                            actions[2] = Megatron.Action.E;
                            borderCells[3] = this.map.getMap().get(position.addY(-2));
                            actions[3] = Megatron.Action.N;
                            
                            borderCells[4] = this.map.getMap().get(position.add(-2, 1));
                            actions[4] = Megatron.Action.SW;
                            borderCells[5] = this.map.getMap().get(position.add(-2, 2));
                            actions[5] = Megatron.Action.SW;
                            borderCells[6] = this.map.getMap().get(position.add(-1, 2));
                            actions[6] = Megatron.Action.SW;
                            
                            borderCells[7] = this.map.getMap().get(position.add(1, 2));
                            actions[7] = Megatron.Action.SE;
                            borderCells[8] = this.map.getMap().get(position.add(2, 2));
                            actions[8] = Megatron.Action.SE;
                            borderCells[9] = this.map.getMap().get(position.add(2, 1));
                            actions[9] = Megatron.Action.SE;
                            
                            borderCells[10] = this.map.getMap().get(position.add(2, -1));
                            actions[10] = Megatron.Action.NE;
                            borderCells[11] = this.map.getMap().get(position.add(2, -2));
                            actions[11] = Megatron.Action.NE;
                            borderCells[12] = this.map.getMap().get(position.add(1, -2));
                            actions[12] = Megatron.Action.NE;
                            
                            borderCells[13] = this.map.getMap().get(position.add(-1, -2));
                            actions[13] = Megatron.Action.NW;
                            borderCells[14] = this.map.getMap().get(position.add(-2, -2));
                            actions[14] = Megatron.Action.NW;
                            borderCells[15] = this.map.getMap().get(position.add(-2, -1));
                            actions[15] = Megatron.Action.NW;
                        }
                        
                        // Main directions 3x for optimal exploration
                        for (int i = 0; i < 4; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                if (this.map3_lastAction != actions[i]) {
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                }
                                this.map3_lastAction = actions[i];
                                return actions[i];
                            }
                        }
                        
                        // Diagonal directions only once
                        for (int i = 4; i < 16; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                this.map3_lastAction = actions[i];
                                return actions[i];
                            }
                        }
                        break;
                    case 2: // Halcón
                        borderCells = new Nodo[40];
                        actions = new Megatron.Action[40];
                        
                        // Different order of directions, depending on start position
                        if (this.startPosition.getY() == 0) {
                            borderCells[0] = this.map.getMap().get(position.addX(5));
                            actions[0] = Megatron.Action.E;
                            borderCells[1] = this.map.getMap().get(position.addY(-5));
                            actions[1] = Megatron.Action.N;
                            borderCells[2] = this.map.getMap().get(position.addX(-5));
                            actions[2] = Megatron.Action.W;
                            borderCells[3] = this.map.getMap().get(position.addY(5));
                            actions[3] = Megatron.Action.S;
                            
                            borderCells[4] = this.map.getMap().get(position.add(5, -1));
                            actions[4] = Megatron.Action.NE;
                            borderCells[5] = this.map.getMap().get(position.add(5, -2));
                            actions[5] = Megatron.Action.NE;
                            borderCells[6] = this.map.getMap().get(position.add(5, -3));
                            actions[6] = Megatron.Action.NE;
                            borderCells[7] = this.map.getMap().get(position.add(5, -4));
                            actions[7] = Megatron.Action.NE;
                            borderCells[8] = this.map.getMap().get(position.add(5, -5));
                            actions[8] = Megatron.Action.NE;
                            borderCells[9] = this.map.getMap().get(position.add(4, -5));
                            actions[9] = Megatron.Action.NE;
                            borderCells[10] = this.map.getMap().get(position.add(3, -5));
                            actions[10] = Megatron.Action.NE;
                            borderCells[11] = this.map.getMap().get(position.add(2, -5));
                            actions[11] = Megatron.Action.NE;
                            borderCells[12] = this.map.getMap().get(position.add(1, -5));
                            actions[12] = Megatron.Action.NE;
                            
                            borderCells[13] = this.map.getMap().get(position.add(-1, -5));
                            actions[13] = Megatron.Action.NW;
                            borderCells[14] = this.map.getMap().get(position.add(-2, -5));
                            actions[14] = Megatron.Action.NW;
                            borderCells[15] = this.map.getMap().get(position.add(-3, -5));
                            actions[15] = Megatron.Action.NW;
                            borderCells[16] = this.map.getMap().get(position.add(-4, -5));
                            actions[16] = Megatron.Action.NW;
                            borderCells[17] = this.map.getMap().get(position.add(-5, -5));
                            actions[17] = Megatron.Action.NW;
                            borderCells[18] = this.map.getMap().get(position.add(-5, -4));
                            actions[18] = Megatron.Action.NW;
                            borderCells[19] = this.map.getMap().get(position.add(-5, -3));
                            actions[19] = Megatron.Action.NW;
                            borderCells[20] = this.map.getMap().get(position.add(-5, -2));
                            actions[20] = Megatron.Action.NW;
                            borderCells[21] = this.map.getMap().get(position.add(-5, -1));
                            actions[21] = Megatron.Action.NW;
                            
                            borderCells[22] = this.map.getMap().get(position.add(-5, 1));
                            actions[22] = Megatron.Action.SW;
                            borderCells[23] = this.map.getMap().get(position.add(-5, 2));
                            actions[23] = Megatron.Action.SW;
                            borderCells[24] = this.map.getMap().get(position.add(-5, 3));
                            actions[24] = Megatron.Action.SW;
                            borderCells[25] = this.map.getMap().get(position.add(-5, 4));
                            actions[25] = Megatron.Action.SW;
                            borderCells[26] = this.map.getMap().get(position.add(-5, 5));
                            actions[26] = Megatron.Action.SW;
                            borderCells[27] = this.map.getMap().get(position.add(-4, 5));
                            actions[27] = Megatron.Action.SW;
                            borderCells[28] = this.map.getMap().get(position.add(-3, 5));
                            actions[28] = Megatron.Action.SW;
                            borderCells[29] = this.map.getMap().get(position.add(-2, 5));
                            actions[29] = Megatron.Action.SW;
                            borderCells[30] = this.map.getMap().get(position.add(-1, 5));
                            actions[30] = Megatron.Action.SW;
                            
                            borderCells[31] = this.map.getMap().get(position.add(1, 5));
                            actions[31] = Megatron.Action.SE;
                            borderCells[32] = this.map.getMap().get(position.add(2, 5));
                            actions[32] = Megatron.Action.SE;
                            borderCells[33] = this.map.getMap().get(position.add(3, 5));
                            actions[33] = Megatron.Action.SE;
                            borderCells[34] = this.map.getMap().get(position.add(4, 5));
                            actions[34] = Megatron.Action.SE;
                            borderCells[35] = this.map.getMap().get(position.add(5, 5));
                            actions[35] = Megatron.Action.SE;
                            borderCells[36] = this.map.getMap().get(position.add(5, 4));
                            actions[36] = Megatron.Action.SE;
                            borderCells[37] = this.map.getMap().get(position.add(5, 3));
                            actions[37] = Megatron.Action.SE;
                            borderCells[38] = this.map.getMap().get(position.add(5, 2));
                            actions[38] = Megatron.Action.SE;
                            borderCells[39] = this.map.getMap().get(position.add(5, 1));
                            actions[39] = Megatron.Action.SE;
                        } else {
                            borderCells[0] = this.map.getMap().get(position.addX(-5));
                            actions[0] = Megatron.Action.W;
                            borderCells[1] = this.map.getMap().get(position.addY(5));
                            actions[1] = Megatron.Action.S;
                            borderCells[2] = this.map.getMap().get(position.addX(5));
                            actions[2] = Megatron.Action.E;
                            borderCells[3] = this.map.getMap().get(position.addY(-5));
                            actions[3] = Megatron.Action.N;
                            
                            borderCells[4] = this.map.getMap().get(position.add(-5, 1));
                            actions[4] = Megatron.Action.SW;
                            borderCells[5] = this.map.getMap().get(position.add(-5, 2));
                            actions[5] = Megatron.Action.SW;
                            borderCells[6] = this.map.getMap().get(position.add(-5, 3));
                            actions[6] = Megatron.Action.SW;
                            borderCells[7] = this.map.getMap().get(position.add(-5, 4));
                            actions[7] = Megatron.Action.SW;
                            borderCells[8] = this.map.getMap().get(position.add(-5, 5));
                            actions[8] = Megatron.Action.SW;
                            borderCells[9] = this.map.getMap().get(position.add(-4, 5));
                            actions[9] = Megatron.Action.SW;
                            borderCells[10] = this.map.getMap().get(position.add(-3, 5));
                            actions[10] = Megatron.Action.SW;
                            borderCells[11] = this.map.getMap().get(position.add(-2, 5));
                            actions[11] = Megatron.Action.SW;
                            borderCells[12] = this.map.getMap().get(position.add(-1, 5));
                            actions[12] = Megatron.Action.SW;
                            
                            borderCells[13] = this.map.getMap().get(position.add(1, 5));
                            actions[13] = Megatron.Action.SE;
                            borderCells[14] = this.map.getMap().get(position.add(2, 5));
                            actions[14] = Megatron.Action.SE;
                            borderCells[15] = this.map.getMap().get(position.add(3, 5));
                            actions[15] = Megatron.Action.SE;
                            borderCells[16] = this.map.getMap().get(position.add(4, 5));
                            actions[16] = Megatron.Action.SE;
                            borderCells[17] = this.map.getMap().get(position.add(5, 5));
                            actions[17] = Megatron.Action.SE;
                            borderCells[18] = this.map.getMap().get(position.add(5, 4));
                            actions[18] = Megatron.Action.SE;
                            borderCells[19] = this.map.getMap().get(position.add(5, 3));
                            actions[19] = Megatron.Action.SE;
                            borderCells[20] = this.map.getMap().get(position.add(5, 2));
                            actions[20] = Megatron.Action.SE;
                            borderCells[21] = this.map.getMap().get(position.add(5, 1));
                            actions[21] = Megatron.Action.SE;
                            
                            borderCells[22] = this.map.getMap().get(position.add(5, -1));
                            actions[22] = Megatron.Action.NE;
                            borderCells[23] = this.map.getMap().get(position.add(5, -2));
                            actions[23] = Megatron.Action.NE;
                            borderCells[24] = this.map.getMap().get(position.add(5, -3));
                            actions[24] = Megatron.Action.NE;
                            borderCells[25] = this.map.getMap().get(position.add(5, -4));
                            actions[25] = Megatron.Action.NE;
                            borderCells[26] = this.map.getMap().get(position.add(5, -5));
                            actions[26] = Megatron.Action.NE;
                            borderCells[27] = this.map.getMap().get(position.add(4, -5));
                            actions[27] = Megatron.Action.NE;
                            borderCells[28] = this.map.getMap().get(position.add(3, -5));
                            actions[28] = Megatron.Action.NE;
                            borderCells[29] = this.map.getMap().get(position.add(2, -5));
                            actions[29] = Megatron.Action.NE;
                            borderCells[30] = this.map.getMap().get(position.add(1, -5));
                            actions[30] = Megatron.Action.NE;
                            
                            borderCells[31] = this.map.getMap().get(position.add(-1, -5));
                            actions[31] = Megatron.Action.NW;
                            borderCells[32] = this.map.getMap().get(position.add(-2, -5));
                            actions[32] = Megatron.Action.NW;
                            borderCells[33] = this.map.getMap().get(position.add(-3, -5));
                            actions[33] = Megatron.Action.NW;
                            borderCells[34] = this.map.getMap().get(position.add(-4, -5));
                            actions[34] = Megatron.Action.NW;
                            borderCells[35] = this.map.getMap().get(position.add(-5, -5));
                            actions[35] = Megatron.Action.NW;
                            borderCells[36] = this.map.getMap().get(position.add(-5, -4));
                            actions[36] = Megatron.Action.NW;
                            borderCells[37] = this.map.getMap().get(position.add(-5, -3));
                            actions[37] = Megatron.Action.NW;
                            borderCells[38] = this.map.getMap().get(position.add(-5, -2));
                            actions[38] = Megatron.Action.NW;
                            borderCells[39] = this.map.getMap().get(position.add(-5, -1));
                            actions[39] = Megatron.Action.NW;
                        }
                        
                        // Main directions 3x for optimal exploration
                        for (int i = 0; i < 4; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                if (this.map3_lastAction != actions[i]) {
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                    this.map3_pathToUnexploredCell.add(actions[i]);
                                }
                                this.map3_lastAction = actions[i];
                                return actions[i];
                            }
                        }
                        
                        // Diagonal directions only once
                        for (int i = 4; i < 40; i++) {
                            if (borderCells[i] != null && borderCells[i].getRadar() != 1 && borderCells[i].getRadar() != 2 && !borderCells[i].explored()) {
                                this.map3_lastAction = actions[i];
                                return actions[i];
                            }
                        }
                }
            }

            // If everything around the drone already has been explored,
            // look for the closest node with unexplored neighbours
            Nodo closestNode = null;
            Nodo currentNode;

            for (Iterator<Nodo> it = this.map.getMap().values().iterator();
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
            this.map3_pathToUnexploredCell = dijkstra(this.map.getMap().get(position), closestNode);
        }

        // Return next action to follow the path to the closest unexplored node
        return this.map3_pathToUnexploredCell.pop();
    }

}
