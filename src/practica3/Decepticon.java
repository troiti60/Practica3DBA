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
public abstract class Decepticon extends SingleAgent {

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
    private Megatron.Action lastAction;
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
            if (msg != null) {
                System.err.println("\t" + msg.getContent());
            }
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

                if (msg.getSender().getLocalName().equals(DataAccess.createInstance().getVirtualHost())) {
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
                        out.setReceiver(new AgentID(DataAccess.createInstance().getVirtualHost()));
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
        String msg = this.json.createJson(hm);

        System.out.println(this.name + ": Checking in with " + msg);

        ACLMessage outbox = new ACLMessage(ACLMessage.REQUEST);
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(DataAccess.createInstance().getVirtualHost()));
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
        out.setContent(this.json.createJson("key", key));
        out.setSender(this.getAid());
        out.setReceiver(new AgentID(DataAccess.createInstance().getVirtualHost()));
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
        if (this.startPosition == null) {
            this.startPosition = newPosition;
        }

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

        if (this.myGoal.equals(this.currentPosition)) {
            this.inGoal = true;
        }
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

    /**
     * Returns the visual range of the Decepticon
     *
     * @return Visual range
     * @author Alexander Straub
     */
    public abstract int getVisualRange();

    /**
     * Returns the battery consumation per step
     *
     * @return Battery consumation
     * @author Alexander Straub
     */
    public abstract int getConsumation();

    ////////////////////////////////////////////////////////////////////////////
    /////////////////////////// Functions for search ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    // For mapv0
    protected Stack<Megatron.Action> map0_pathToUnexploredCell = new Stack<>();

    // For mapv1
    protected Stack<Megatron.Action> map1_pathToUnexploredCell = new Stack<>();

    // For mapv2
    private boolean map2_comprobation = false;//false ->part1 || true ->part2
    private int map2_contador = 0;//coord x
    private boolean map2_direccion = true;//true down || false up
    private boolean map2_iod = false;//false <- || true ->
    private boolean map2_bordeando = false;

    // For mapv3
    protected Stack<Megatron.Action> map3_pathToUnexploredCell = new Stack<>();
    protected Megatron.Action map3_lastAction = null;
    
    // For mapv4
    protected boolean map4_start = true;
    protected boolean map4_stop = false;
    protected Coord map4_target = null;
    protected Stack<Megatron.Action> map4_pathToUnexploredCell = new Stack<>();

    /**
     * The best path to reach the goal, once we have found it, using A*
     *
     * @return Direction where drone should move
     * @param start the position of the selected drone
     * @param goal the position of the goal
     * @throws Exception
     * @author Daniel Sánchez Alcaide
     */
    public final Stack<Megatron.Action> aStar(Node start, Node goal) throws Exception {
        Comparator<Node> comp = new ComparadorHeuristicaNodo(goal);
        PriorityQueue<Node> abiertos = new PriorityQueue<>(10, comp);
        ArrayList<Node> cerrados = new ArrayList<>();

        Stack<Megatron.Action> caminito = new Stack<>();

        Node current = start;
        abiertos.add(current);

        //El vértice no es la meta y abiertos no está vacío
        while (!abiertos.isEmpty() && !current.equals(goal)) {
            //Sacamos el nodo de abiertos
            current = abiertos.poll();
            //Metemos el nodo en cerrados
            cerrados.add(current);
            //Examinamos los nodos vecinos
            ArrayList<Node> vecinos = current.getAdyacents();
            for (Node vecino : vecinos) {
                //Comprobamos que no esté ni en abiertos ni en cerrados
                if (!abiertos.contains(vecino) && !cerrados.contains(vecino)) {
                    //Guardamos el camino hacia el nodo actual desde los vecinos
                    vecino.setPath(current);
                    abiertos.add(vecino);
                }
                if (abiertos.contains(vecino)) {
                    //Si el vecino está en abiertos comparamos los valores de g 
                    //para los posibles nodos padre
                    if (vecino.getPath().g(start) > current.g(start)) {
                        vecino.setPath(current);
                    }
                }
            }
        }
        //Recorremos el camino desde el nodo objetivo hacia atrás para obtener la accion
        if (current.equals(goal)) {
            while (!current.getPath().equals(start)) {
                //El padre está al norte
                if (current.getPath().getCoord().equals(current.N())) {
                    caminito.add(Megatron.Action.S);
                }
                //El padre está al sur
                if (current.getPath().getCoord().equals(current.S())) {
                    caminito.add(Megatron.Action.N);
                }
                //El padre está al este
                if (current.getPath().getCoord().equals(current.E())) {
                    caminito.add(Megatron.Action.W);
                }
                //El padre está al oeste
                if (current.getPath().getCoord().equals(current.W())) {
                    caminito.add(Megatron.Action.E);
                }
                //El padre está al noreste
                if (current.getPath().getCoord().equals(current.NE())) {
                    caminito.add(Megatron.Action.SW);
                }
                //El padre está al noroeste
                if (current.getPath().getCoord().equals(current.NW())) {
                    caminito.add(Megatron.Action.SE);
                }
                //El padre está al sureste
                if (current.getPath().getCoord().equals(current.SE())) {
                    caminito.add(Megatron.Action.NW);
                }
                //El padre está al suroeste
                if (current.getPath().getCoord().equals(current.SW())) {
                    caminito.add(Megatron.Action.NE);
                }
            }
        }
        //Limpieza de Nodos
        Collection<Node> m = this.map.getMap().values();
        for (Node n : m) {
            n.setPath(null);
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
    public final Stack<Megatron.Action> dijkstra(Node start, Node target) {
        // Sanity check for the parameters
        if (start == null || target == null || start.equals(target)) {
            return null;
        }

        // Get the map
        HashMap<Coord, Node> localMap = this.map.getAccessibleMap();
        start = localMap.get(start.getCoord());
        target = localMap.get(target.getCoord());

        // Initialize
        List<Node> nodes = new ArrayList<>(localMap.values());
        for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
            i.next().resetSearch();
        }
        start.setDistance(0.0);

        // While there are nodes unexplored
        while (!nodes.isEmpty()) {
            // Get node with less distance
            Node minNode = (Node) Collections.min(nodes);
            nodes.remove(minNode);

            // Get neighbours of the current node
            for (Node neighbour : minNode.getAdyacents()) {
                // If the neighbour is still in the list
                if (nodes.contains(neighbour)) {
                    // Calculate alternative distance
                    double alternative = minNode.getDistance() + 1;

                    // If the distance is better, add to the path
                    if (alternative < neighbour.getDistance()) {
                        neighbour.setDistance(alternative);
                        neighbour.setPath(minNode);
                    }
                }
            }
        }

        // Check if a path is even possible
        if (target.getDistance() == Double.MAX_VALUE) {
            return null;
        }

        // Starting with the target node, trace back to the start
        Stack<Megatron.Action> actions = new Stack<>();

        while (target != null && target != start) {
            // Get next direction
            if (target.getCoord().equals(target.getPath().getCoord().NW())) {
                actions.push(Megatron.Action.NW);
            }
            if (target.getCoord().equals(target.getPath().getCoord().N())) {
                actions.push(Megatron.Action.N);
            }
            if (target.getCoord().equals(target.getPath().getCoord().NE())) {
                actions.push(Megatron.Action.NE);
            }
            if (target.getCoord().equals(target.getPath().getCoord().E())) {
                actions.push(Megatron.Action.E);
            }
            if (target.getCoord().equals(target.getPath().getCoord().SE())) {
                actions.push(Megatron.Action.SE);
            }
            if (target.getCoord().equals(target.getPath().getCoord().S())) {
                actions.push(Megatron.Action.S);
            }
            if (target.getCoord().equals(target.getPath().getCoord().SW())) {
                actions.push(Megatron.Action.SW);
            }
            if (target.getCoord().equals(target.getPath().getCoord().W())) {
                actions.push(Megatron.Action.W);
            }

            target = target.getPath();
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
        HashMap<Coord, Node> localMap = this.map.getMap();

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
        HashMap<Coord, Node> localMap = this.map.getMap();
        Node current;
        current = new Node(this.currentPosition.getX(), this.currentPosition.getY(),
                localMap.get(this.currentPosition).getRadar());

        if (map2_bordeando) {
            //funcion de bordear obstaculos
            if(map2_direccion){
                actions=BordearDerecha(map2_contador,0,lastAction,current);
            }else{
                actions=BordearDerecha(map2_contador,1,lastAction,current);
            }
            
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
                    if (localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W) {
                        actions = Megatron.Action.E;
                    } else if (localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW) {
                        actions = Megatron.Action.SE;
                    } else if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                        actions = Megatron.Action.S;
                    } else if (localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW) {
                        actions = Megatron.Action.NE;
                    } else if (localMap.get(current.N()).getRadar() == 0 && lastAction!=Megatron.Action.S) {
                        actions = Megatron.Action.N;
                    } else if (localMap.get(current.NW()).getRadar() == 0 && lastAction!=Megatron.Action.SE) {
                        actions = Megatron.Action.NW;
                    } else if (localMap.get(current.W()).getRadar() == 0 && lastAction!=Megatron.Action.E) {
                        actions = Megatron.Action.W;
                    } else if(lastAction!=Megatron.Action.NE){
                        actions = Megatron.Action.SW;
                    }
                }
                //ir esquina superior izquierda
            } else {
                map2_direccion = true;
                map2_iod = true;
                if (localMap.get(current.NW()).getRadar() == 2 && localMap.get(current.N()).getRadar() == 2
                        && localMap.get(current.W()).getRadar() == 2) {
                    map2_comprobation = true;
                } else {
                    if (localMap.get(current.NW()).getRadar() == 0 && lastAction!=Megatron.Action.SE) {
                        actions = Megatron.Action.NW;
                    } else if (localMap.get(current.N()).getRadar() == 0 && lastAction!=Megatron.Action.S) {
                        actions = Megatron.Action.N;
                    } else if (localMap.get(current.W()).getRadar() == 0 && lastAction!=Megatron.Action.E) {
                        actions = Megatron.Action.W;
                    } else if (localMap.get(current.SW()).getRadar() == 0 && lastAction!=Megatron.Action.NE) {
                        actions = Megatron.Action.SW;
                    } else if (localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW) {
                        actions = Megatron.Action.NE;
                    } else if (localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W) {
                        actions = Megatron.Action.E;
                    } else if (localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW) {
                        actions = Megatron.Action.SE;
                    } else if(lastAction!=Megatron.Action.N){
                        actions = Megatron.Action.S;
                    }
                }

            }
            //segunda ruta
        } else {
            //hacia abajo e izquierda
            if (!map2_iod && map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                    actions = Megatron.Action.S;
                } else if (localMap.get(current.S()).getRadar() == 2) {
                    map2_contador -= 3;
                    actions = Megatron.Action.W;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                    if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }
                }
                //hacia arriba e izquierda
            } else if (!map2_iod && !map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.N()).getRadar() == 0 && lastAction!=Megatron.Action.S) {
                    actions = Megatron.Action.N;
                } else if (localMap.get(current.N()).getRadar() == 2) {
                    map2_direccion = true;
                    map2_contador -= 3;
                    actions = Megatron.Action.W;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                    if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }
                }
                //hacia abajo y derecha
            } else if (map2_iod && map2_direccion && map2_contador == current.getX()) {
                if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                    actions = Megatron.Action.S;
                } else if (localMap.get(current.S()).getRadar() == 2) {
                    map2_contador += 3;
                    actions = Megatron.Action.E;
                } else {
                    //bordear obstaculo
                    map2_bordeando = true;
                    if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }
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
                    if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }
                }
                //ir hacia la x cambiada
            } else {
                if(current.getX()>map2_contador){
                    if(localMap.get(current.W()).getRadar() == 0 && lastAction!=Megatron.Action.E){
                        actions=Megatron.Action.W;
                    }else{
                        map2_bordeando=true;
                    }
                }else{
                    if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else{
                        map2_bordeando=true;
                    }
                }
            }
        }
        return actions;
    }
    
    /**
     * Avoid obstacles
     * @param int x -> go to coord x
     * @param int direction -> direction of the drone [0-> up to down, 1-> down to up]
     * @param lastAction -> last action of the drone
     * @return next action to be done to avoid obstacles by specified drone
     * @throws java.lang.Exception
     * @author Jesús Cobo Sánchez
     */
    private Megatron.Action BordearDerecha(int x, int direction, Megatron.Action lastAction, Node current){
        Megatron.Action action=null;
        HashMap<Coord, Node> localMap = this.map.getMap();
        
        if(direction==0 && x!=current.getX()){
            if (localMap.get(current.W()).getRadar() == 0 && lastAction!=Megatron.Action.E) {
                action = Megatron.Action.W;
            } else if (localMap.get(current.SW()).getRadar() == 0 && lastAction!=Megatron.Action.NE) {
                action = Megatron.Action.SW;
            } else if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                action = Megatron.Action.S;
            } else if (localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW) {
                action = Megatron.Action.SE;
            } else if (localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W) {
                action = Megatron.Action.E;
            } else if (localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW) {
                action = Megatron.Action.NE;
            } else if (localMap.get(current.N()).getRadar() == 0 && lastAction!=Megatron.Action.S) {
                action = Megatron.Action.N;
            }
        }else if(direction==1 && x!=current.getX()){
            if (localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W) {
                action = Megatron.Action.E;
            } else if (localMap.get(current.NW()).getRadar() == 0 && lastAction!=Megatron.Action.SE) {
                action = Megatron.Action.NW;
            } else if (localMap.get(current.N()).getRadar() == 0 && lastAction!=Megatron.Action.S) {
                action = Megatron.Action.N;
            } else if (localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW) {
                action = Megatron.Action.NE;
            } else if (localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W) {
                action = Megatron.Action.E;
            } else if (localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW) {
                action = Megatron.Action.SE;
            } else if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                action = Megatron.Action.S;
            }
        }else if(x==current.getX()){
            map2_bordeando=false;
        }
        
        return action;
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
    public final Megatron.Action mapv3() throws Exception {
        Megatron.Action ret = mapv3(false);

        // If mapv3 returns an illegal action, try again
        if (this.map.getMap().get(this.currentPosition.neighbour(ret)).getRadar() == 1
                || this.map.getMap().get(this.currentPosition.neighbour(ret)).getRadar() == 2) {
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
        
        // If it's the first time executing, step away from the border
        if (this.currentPosition.equals(this.startPosition)) {
            if (this.startPosition.getY() == 0) {
                for (int i = 0; i < getVisualRange() / 2 - 1; i++) {
                    this.map3_pathToUnexploredCell.add(Megatron.Action.S);
                }
                this.map3_lastAction = Megatron.Action.S;
                return Megatron.Action.S;
            } else {
                for (int i = 0; i < getVisualRange() / 2 - 1; i++) {
                    this.map3_pathToUnexploredCell.add(Megatron.Action.N);
                }
                this.map3_lastAction = Megatron.Action.N;
                return Megatron.Action.N;
            }
        }

        if (this.map3_pathToUnexploredCell.isEmpty()) {
            // Get the border cells of the visual range of the specified drone
            if (!findWay) {
                ArrayList<Node> borderCells = new ArrayList<>();
                ArrayList<Megatron.Action> actions = new ArrayList<>();

                mapv3_getBorderCells(position, borderCells, actions);

                // Main directions x times for optimal exploration
                for (int i = 0; i < 4; i++) {
                    if (borderCells.get(i) != null && borderCells.get(i).getRadar() != 1 && borderCells.get(i).getRadar() != 2 && !borderCells.get(i).isExplored()) {
                        if (this.map3_lastAction != actions.get(i)) {
                            for (int j = 0; j < getVisualRange() - 1; j++) {
                                this.map3_pathToUnexploredCell.add(actions.get(i));
                            }
                        }
                        this.map3_lastAction = actions.get(i);
                        return actions.get(i);
                    }
                }

                // Diagonal directions only once
                for (int i = 4; i < borderCells.size(); i++) {
                    if (borderCells.get(i) != null && borderCells.get(i).getRadar() != 1 && borderCells.get(i).getRadar() != 2 && !borderCells.get(i).isExplored()) {
                        this.map3_lastAction = actions.get(i);
                        return actions.get(i);
                    }
                }
            }

            // If everything around the drone already has been explored,
            // look for the closest node with unexplored neighbours
            Node closestNode = null;
            Node currentNode;

            for (Iterator<Node> it = this.map.getAccessibleMap().values().iterator();
                    it.hasNext();) {

                currentNode = it.next();
                if (currentNode.getRadar() == 0 && !currentNode.isExplored()
                        && (closestNode == null || position.distanceTo(currentNode.getCoord()) < position.distanceTo(closestNode.getCoord()))) {

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

    /**
     * Return the border cells in the right order, together with the respective
     * actions.
     *
     * @param position Current position of the drone
     * @param borderCells Array to fill with border cells
     * @param actions Array to fill with actions for the border cells
     * @author Alexander Straub
     */
    protected abstract void mapv3_getBorderCells(Coord position, ArrayList<Node> borderCells, ArrayList<Megatron.Action> actions);

    /**
     * First go to the nearest corner, then try to cross the map
     * 
     * @return Next action
     * @author Alexander Straub
     */
    public final Megatron.Action mapv4() {
        // If it's the first time executing, go to the nearest corner
        if (this.map4_start) {
            this.map4_start = false;
            
            if (this.startPosition.getX() <= this.map.getResolution() / 2) {
                // Get to (0,0)
                for (int i = 0; i < this.startPosition.getX() - 1; i++) {
                    this.map4_pathToUnexploredCell.push(Megatron.Action.W);
                }
                return Megatron.Action.W;
            } else {
                // Get to (res-1,0)
                for (int i = 0; i < this.map.getResolution() - this.startPosition.getX() - 2; i++) {
                    this.map4_pathToUnexploredCell.push(Megatron.Action.E);
                }
                return Megatron.Action.E;
            }
        }
        
        // If actions are pre-planned: execute them
        if (!this.map4_pathToUnexploredCell.isEmpty()) {
            return this.map4_pathToUnexploredCell.pop();
        }
        
        // Try to cross the map
        Megatron.Action ret = mapv4_crossMap();
        
//        // Go to the other corner and cross again
//        if (ret == null) {
//            this.map4_stop = false;
//            
//            if (this.getPosition().getX() == 0) {
//                for (int i = 2; i < this.map.getResolution(); i++) {
//                    this.map4_pathToUnexploredCell.push(Megatron.Action.E);
//                }
//                ret = Megatron.Action.E;
//            } else {
//                for (int i = 2; i < this.map.getResolution(); i++) {
//                    this.map4_pathToUnexploredCell.push(Megatron.Action.W);
//                }
//                ret = Megatron.Action.W;
//            }
//        }
        
        return ret;
    }
    
    /**
     * Try to cross the map
     * 
     * @return Next action
     * @author Alexander Straub
     */
    protected Megatron.Action mapv4_crossMap() {
        if (this.map4_stop) {
            return null;
        }
        
        // Get target border if not set
        if (this.map4_target == null) {
            int x, y;
            
            if (this.getPosition().getX() == 0) {
                x = this.map.getResolution() - 1;
            } else {
                x = 0;
            }
            
            if (this.getPosition().getY() == 0) {
                y = this.map.getResolution() - 1;
            } else {
                y = 0;
            }
            
            this.map4_target = new Coord(x, y);
        }
        
        Node closestNode = null;
        
        // Check if target node is in the graph
        if (this.map.getAccessibleMap().containsKey(this.map4_target)) {
            this.map4_stop = true;
            
            closestNode = this.map.getAccessibleMap().get(this.map4_target);
        } else {
            // Get neighbour closest to the target            
            if (closestNode == null || this.getPosition().NW().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().NW());
            }
            if (closestNode == null || this.getPosition().N().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().N());
            }
            if (closestNode == null || this.getPosition().NE().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().NE());
            }
            if (closestNode == null || this.getPosition().E().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().E());
            }
            if (closestNode == null || this.getPosition().SE().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().SE());
            }
            if (closestNode == null || this.getPosition().S().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().S());
            }
            if (closestNode == null || this.getPosition().SW().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().SW());
            }
            if (closestNode == null || this.getPosition().W().distanceTo(this.map4_target) < closestNode.getCoord().distanceTo(this.map4_target)) {
                closestNode = this.map.getAccessibleMap().get(this.getPosition().W());
            }
            
            if (closestNode == null || closestNode.getCoord().distanceTo(this.map4_target) >= this.getPosition().distanceTo(this.map4_target)) {
                closestNode = null;
                
                // Find unexplored cell closest to the corner to get to
                Node currentNode;

                for (Iterator<Node> it = this.map.getAccessibleMap().values().iterator();
                        it.hasNext();) {

                    currentNode = it.next();
                    if (currentNode.getRadar() == 0 && !currentNode.isExplored()
                            && (closestNode == null || this.map4_target.distanceTo(currentNode.getCoord()) < this.map4_target.distanceTo(closestNode.getCoord()))) {

                        closestNode = currentNode;
                    }
                }
            }
        }
        
        if (closestNode == null) {
            System.err.println("ERROR: mapv4 called, but map already explored completely");
            return null;
        }

        // Find way to the previously found closest node (or the target)
        this.map4_pathToUnexploredCell = dijkstra(this.map.getMap().get(this.getPosition()), closestNode);
        
        return this.map4_pathToUnexploredCell.pop();
    }
}
