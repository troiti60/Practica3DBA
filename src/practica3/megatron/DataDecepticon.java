package practica3.megatron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

/**
 * Base class for all decepticons
 *
 * @author Antonio Troitiño del Rio, Alexander Straub
 */
public abstract class DataDecepticon {

    // Information about the decepticon
    private final int role;
    private boolean alive;

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
    private boolean standby;
    private int reactivateCounter;

    /**
     * Constructor
     *
     * @param role Type: 0-Flydron, 1-Birdron, 2-Falcdron
     * @param map Reference to the map
     * @throws Exception
     * @author Antonio Troitiño del Rio
     */
    public DataDecepticon(int role, Map map) throws Exception {
        this.alive = true;
        this.role = role;
        this.map = map;
        this.currentPosition = null;
        this.lastPosition = null;
        this.startPosition = null;
        this.fuel = 100;
        this.lastAction = null;
        this.inGoal = false;
        this.myGoal = null;
        this.standby = false;
        this.reactivateCounter = 0;
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
     * Sets the vital state to dead
     * 
     * @author Alexander Straub
     */
    public final void setDead() {
        this.alive = false;
    }

    /**
     * Sets the new position
     *
     * @param newPosition New position
     * @author Alexander Straub
     */
    public final void setPosition(Coord newPosition) {
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

        if (this.myGoal != null && this.currentPosition.equals(this.myGoal)) {
            this.inGoal = true;
        }
    }

    /**
     * Returns the current position
     *
     * @return Current position
     * @author Antonio Troitiño del Río
     */
    public final Coord getPosition() {
        return this.currentPosition;
    }

    /**
     * Returns the last position
     *
     * @return Last position
     * @author Antonio Troitiño del Río
     */
    public final Coord getLastPosition() {
        return this.lastPosition;
    }

    /**
     * Returns the initial position
     *
     * @return Initial position
     * @author Antonio Troitiño del Río
     */
    public final Coord getStartPosition() {
        return this.startPosition;
    }

    /**
     * Sets the new amount of fuel
     *
     * @param fuel New amount of fuel
     * @author Antonio Troitiño del Río
     */
    public final void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /**
     * Returns the amount of fuel left
     *
     * @return Fuel
     * @author Antonio Troitiño del Río
     */
    public final int getFuel() {
        return this.fuel;
    }

    /**
     * Returns the last action executed
     *
     * @return Last action
     * @author Antonio Troitiño del Río
     */
    public final Megatron.Action getLastAction() {
        return this.lastAction;
    }

    /**
     * Sets a new target for this decepticon
     *
     * @param newGoal New target
     * @author Antonio Troitiño del Río
     */
    public final void setMyGoal(Coord newGoal) {
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
    public final Coord getMyGoal() {
        return this.myGoal;
    }

    /**
     * Is the decepticon already at its target?
     *
     * @return True if decepticon is at its target
     * @author Antonio Troitiño del Río
     */
    public final boolean isInGoal() {
        return this.inGoal;
    }
    
    /**
     * Sets the drone to standby
     * 
     * @author Alexander Straub
     */
    protected final void setStandby() {
        this.standby = true;
    }
    
    /**
     * Reactivates the drone
     * 
     * @author Alexander Straub
     */
    public final void reactivate() {
        this.standby = false;
        this.reactivateCounter++;
    }
    
    /**
     * Returns the state of standby
     * 
     * @return True if on standby
     * @author Alexander Straub
     */
    public final boolean isOnStandby() {
        return this.standby;
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
    private boolean map2_comprobation = false;
    private int map2_counter = 0;
    private boolean map2_direction = true;
    private boolean map2_iod = false;
    private boolean map2_passBy = false;

    // For mapv3
    protected Stack<Megatron.Action> map3_pathToUnexploredCell = new Stack<>();
    protected Megatron.Action map3_lastAction = null;
    
    // For mapv4
    protected boolean map4_start = true;
    protected boolean map4_stop = false;
    protected Coord map4_target = null;
    protected int map4_stepNum = 0;
    protected Stack<Megatron.Action> map4_pathToUnexploredCell = new Stack<>();
    
    // For findWay
    protected int findWay_wayLength = 0;
    protected int findWay_minWayLength = 0;
    protected Stack<Megatron.Action> findWay_pathToTarget = new Stack<>();

    /**
     * The best path to reach the goal, once we have found it, using A*
     *
     * @return Direction where drone should move
     * @param start the position of the selected drone
     * @param goal the position of the goal
     * @param posDrones the positions of the drones
     * @throws Exception
     * @author Daniel Sánchez Alcaide
     */
    public final Stack<Megatron.Action> aStar(Coord start, Coord goal, ArrayList<Coord> posDrones) throws Exception {
        Node startNode = this.map.getMap().get(start);
        Node goalNode = this.map.getMap().get(goal);
        
        Comparator<Node> comp = new ComparatorNodeHeuristic(goalNode);
        PriorityQueue<Node> abiertos = new PriorityQueue<>(10, comp);
        ArrayList<Node> cerrados = new ArrayList<>();

        Stack<Megatron.Action> caminito = new Stack<>();

        Node current = startNode;
        abiertos.add(current);

        //El vértice no es la meta y abiertos no está vacío
        while (!abiertos.isEmpty() && !current.equals(goalNode)) {
            //Sacamos el nodo de abiertos
            current = abiertos.poll();
            //Metemos el nodo en cerrados
            cerrados.add(current);
            //Examinamos los nodos vecinos
            ArrayList<Node> vecinos = current.getAdyacents();
            for (Node vecino : vecinos) {
                //Comprobamos que no esté ni en abiertos ni en cerrados
                if (!abiertos.contains(vecino) && !cerrados.contains(vecino)) {
                    //Comprobamos que no sea un nodo en el que esté otro dron
                    boolean libre = true;
                    for(Coord pos : posDrones){
                        if(pos == vecino.getCoord()){
                            libre = false;
                            cerrados.add(vecino);
                        }
                    }
                    //Guardamos el camino hacia el nodo actual desde los vecinos
                    if(libre){
                        vecino.setPath(current);
                        abiertos.add(vecino);
                    }
                }
                if (abiertos.contains(vecino)) {
                    //Si el vecino está en abiertos comparamos los valores de g 
                    //para los posibles nodos padre
                    if (vecino.getPath().g(startNode) > current.g(startNode)) {
                        vecino.setPath(current);
                    }
                }
            }
        }
        //Recorremos el camino desde el nodo objetivo hacia atrás para obtener la accion
        if (current.equals(goalNode)) {
            while (!current.getPath().equals(startNode)) {
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
     * Search for the best way to go from one node to one not completely explored
     *
     * @param start Starting position
     * @param positions Positions of the drones to evade collision
     * @return Stack of actions
     * @author Alexander Straub
     */
    public final Stack<Megatron.Action> dijkstra(Coord start, ArrayList<Coord> positions) {
        // Sanity check for the parameter
        if (start == null) {
            return null;
        }

        // Get the map
        HashMap<Coord, Node> localMap = this.map.getAccessibleMap();
        Node startNode = localMap.get(start);
        List<Node> nodes = new ArrayList<>(localMap.values());
        
        // Eliminate positions of other drones from list
        for (Coord position : positions) {
            if (!position.equals(start)) {
                nodes.remove(localMap.get(position));
            }
        }

        // Assign distances and set paths
        expandNodes(startNode, new ArrayList<>(nodes));

        // Search for a node not fully explored with least distance
        Node target = null, temp;
        
        // First, search for nodes with number of neighbours <7
        for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
            temp = i.next();
            
            if ((temp.getDistance() != Double.MAX_VALUE && this.map.getMap().get(temp.getCoord()).getExplored() < 7) 
                    && (target == null || target.getDistance() > temp.getDistance())) {
                target = temp;
            }
        }
        
        // If no such node was found, go for all not completely explored
        if (target == null) {
            for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
                temp = i.next();

                if ((temp.getDistance() != Double.MAX_VALUE && !temp.isExplored()) 
                        && (target == null || target.getDistance() > temp.getDistance())) {
                    target = temp;
                }
            }
        }
        
        // If no such node exists, we already explored the whole map
        if (target == null) {
            return null;
        }

        // Return found path
        return retracePath(startNode, target);
    }
    
    /**
     * Search for the best way to go from one node to another
     *
     * @param start Node where to start
     * @param target Node where to get to
     * @param positions Positions of the drones to evade collision
     * @return Stack of actions
     * @author Alexander Straub
     */
    public final Stack<Megatron.Action> dijkstra(Coord start, Coord target, ArrayList<Coord> positions) {
        return dijkstra(start, target, positions, this.map.getAccessibleMap());
    }
    
    /**
     * Search for the best way to go from one node to another
     *
     * @param start Node where to start
     * @param target Node where to get to
     * @param positions Positions of the drones to evade collision
     * @param localMap Map to use
     * @return Stack of actions
     * @author Alexander Straub
     */
    public final Stack<Megatron.Action> dijkstra(Coord start, Coord target, ArrayList<Coord> positions, HashMap<Coord, Node> localMap) {
        // Sanity check for the parameters
        if (start == null || target == null || start.equals(target)) {
            return null;
        }

        // Get the map
        Node startNode = localMap.get(start);
        Node targetNode = localMap.get(target);
        List<Node> nodes = new ArrayList<>(localMap.values());
        
        // Eliminate positions of other drones from list
        for (Coord position : positions) {
            if (!position.equals(start)) {
                nodes.remove(localMap.get(position));
            }
        }
        
        // Assign distances and set paths
        expandNodes(startNode, nodes);

        // Check if a path is even possible (may not be connected to this graph)
        if (targetNode.getDistance() == Double.MAX_VALUE) {
            return null;
        }

        // Return found path
        return retracePath(startNode, targetNode);
    }
    
    /**
     * Expand nodes for dijkstra, assigning distances and paths
     * 
     * @param start Node to start from
     * @param nodes Nodes of the map to use
     * @author Alexander Straub
     */
    private void expandNodes(Node start, List<Node> nodes) {
        // Initialize
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
    }
    
    /**
     * Retrace the path beginning at the target going back to start
     * 
     * @param start Start node
     * @param target Target node
     * @return Stack of actions
     * @author Alexander Straub
     */
    private Stack<Megatron.Action> retracePath(Node start, Node target) {
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
        if (isOnStandby()) return null;
        
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
        if (isOnStandby()) return null;
        
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
     * @deprecated
     */
    public Megatron.Action mapv2() throws Exception {
        if (isOnStandby()) return null;
        
        Megatron.Action actions = null;
        HashMap<Coord, Node> localMap = this.map.getMap();
        Node current;
        current = new Node(this.currentPosition.getX(), this.currentPosition.getY(),
                localMap.get(this.currentPosition).getRadar());

        if (map2_passBy) {
            //funcion de bordear obstaculos
            if(map2_direction){
                actions=passByRight(map2_counter,0,lastAction,current);
            }else{
                actions=passByRight(map2_counter,1,lastAction,current);
            }
            
        }

        if (map2_comprobation == false) {
            //ir esquina inferor derecha
            if (current.getY() > 50) {
                map2_direction = false;
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
                map2_direction = true;
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
            if (!map2_iod && map2_direction && map2_counter == current.getX()) {
                if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                    actions = Megatron.Action.S;
                } else if (localMap.get(current.S()).getRadar() == 2) {
                    map2_counter -= 3;
                    actions = Megatron.Action.W;
                } else {
                    //bordear obstaculo
                    map2_passBy = true;
                    if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }
                }
                //hacia arriba e izquierda
            } else if (!map2_iod && !map2_direction && map2_counter == current.getX()) {
                if (localMap.get(current.N()).getRadar() == 0 && lastAction!=Megatron.Action.S) {
                    actions = Megatron.Action.N;
                } else if (localMap.get(current.N()).getRadar() == 2) {
                    map2_direction = true;
                    map2_counter -= 3;
                    actions = Megatron.Action.W;
                } else {
                    //bordear obstaculo
                    map2_passBy = true;
                    if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }
                }
                //hacia abajo y derecha
            } else if (map2_iod && map2_direction && map2_counter == current.getX()) {
                if (localMap.get(current.S()).getRadar() == 0 && lastAction!=Megatron.Action.N) {
                    actions = Megatron.Action.S;
                } else if (localMap.get(current.S()).getRadar() == 2) {
                    map2_counter += 3;
                    actions = Megatron.Action.E;
                } else {
                    //bordear obstaculo
                    map2_passBy = true;
                    if(localMap.get(current.SE()).getRadar() == 0 && lastAction!=Megatron.Action.NW){
                        actions=Megatron.Action.SE;
                    }else if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else if(localMap.get(current.NE()).getRadar() == 0 && lastAction!=Megatron.Action.SW){
                        actions=Megatron.Action.NE;
                    }
                }
                //hacia arriba y derecha
            } else if (map2_iod && !map2_direction && map2_counter == current.getX()) {
                if (localMap.get(current.N()).getRadar() == 0) {
                    actions = Megatron.Action.N;
                } else if (localMap.get(current.N()).getRadar() == 2) {
                    map2_direction = true;
                    map2_counter += 3;
                    actions = Megatron.Action.E;
                } else {
                    map2_passBy = true;
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
                if(current.getX()>map2_counter){
                    if(localMap.get(current.W()).getRadar() == 0 && lastAction!=Megatron.Action.E){
                        actions=Megatron.Action.W;
                    }else{
                        map2_passBy=true;
                    }
                }else{
                    if(localMap.get(current.E()).getRadar() == 0 && lastAction!=Megatron.Action.W){
                        actions=Megatron.Action.E;
                    }else{
                        map2_passBy=true;
                    }
                }
            }
        }
        return actions;
    }
    
    /**
     * Avoid obstacles
     * 
     * @param x Go to coord x
     * @param direction Direction of the drone [0-> up to down, 1-> down to up]
     * @param lastAction Last action of the drone
     * @return Next action to be done to avoid obstacles by specified drone
     * @throws java.lang.Exception
     * @author Jesús Cobo Sánchez
     */
    private Megatron.Action passByRight(int x, int direction, Megatron.Action lastAction, Node current){
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
            map2_passBy=false;
        }
        
        return action;
    }

    /**
     * Exploration of the following form: (example 5x5)
     *
     *  3 14 13 12  2
     * 15  x  x  x 11
     *  4  x  D  x 10
     *  5  x  x  x  9
     *  0  6  7  8  1
     *
     * Beginning with the cell 0 the method tries to find a cell at the border
     * of the visual range of the drone that has not yet been explored
     * completely and is not a wall. If the cell i does not match these
     * criteria, proceed with cell i+1. If none of the border cells is
     * considered usable, use a path finding algorithm to get to the next
     * unexplored cell, there resetting to the exploration pattern above.
     *
     * @param positions Positions of the drones to evade collision
     * @return Next action for the specified drone
     * @throws java.lang.Exception
     * @author Alexander Straub
     */
    public final Megatron.Action mapv3(ArrayList<Coord> positions) throws Exception {
        if (isOnStandby()) return null;
        
        Megatron.Action ret = mapv3(positions, false);

        // If mapv3 returns an illegal action, try again
        if (this.map.getMap().get(this.currentPosition.neighbour(ret)).getRadar() == 1
                || this.map.getMap().get(this.currentPosition.neighbour(ret)).getRadar() == 2
                || positions.contains(this.currentPosition.neighbour(ret))) {
            System.err.println("ERROR: mapv3 devolvió una acción ilegal");

            this.map3_pathToUnexploredCell.clear();
            ret = mapv3(positions, true);
        }

        return ret;
    }

    /**
     * Exploration of the following form: (example 5x5)
     *
     *  3 14 13 12  2
     * 15  x  x  x 11
     *  4  x  D  x 10
     *  5  x  x  x  9
     *  0  6  7  8  1
     *
     * Beginning with the cell 0 the method tries to find a cell at the border
     * of the visual range of the drone that has not yet been explored
     * completely and is not a wall. If the cell i does not match these
     * criteria, proceed with cell i+1. If none of the border cells is
     * considered usable, use a path finding algorithm to get to the next
     * unexplored cell, there resetting to the exploration pattern above.
     * 
     * @param positions Positions of the drones to evade collision
     * @param findWay True: Don't look at border cells but only for a close cell
     * @return Next action for the specified drone
     * @throws java.lang.Exception
     * @author Alexander Straub
     */
    protected Megatron.Action mapv3(ArrayList<Coord> positions, boolean findWay) throws Exception {
        Coord position = this.currentPosition;
        
        // If it's the first time executing, step away from the border
        if (this.currentPosition.equals(this.startPosition)) {
            if (this.startPosition.getY() == 0) {
                for (int i = 0; i < getVisualRange() / 2 - 2; i++) {
                    this.map3_pathToUnexploredCell.add(Megatron.Action.S);
                }
                this.map3_lastAction = Megatron.Action.S;
                return Megatron.Action.S;
            } else {
                for (int i = 0; i < getVisualRange() / 2 - 2; i++) {
                    this.map3_pathToUnexploredCell.add(Megatron.Action.N);
                }
                this.map3_lastAction = Megatron.Action.N;
                return Megatron.Action.N;
            }
        }
        
        // Prevent going too far in an undesirable direction
        if (!this.map3_pathToUnexploredCell.isEmpty()) {
            Coord target = getPosition();
            
            for (int i = 0; i < getVisualRange() / 2; i++) {
                target = target.neighbour(this.map3_pathToUnexploredCell.peek());
            }
            
            if (this.map.getMap().get(target).getRadar() == 1
                    || this.map.getMap().get(target).getRadar() == 2
                    || this.map.getMap().get(target).isExplored()) {
                this.map3_pathToUnexploredCell.clear();
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
                        // Only do more steps after a change of direction
                        if (this.map3_lastAction != actions.get(i)) {
                            for (int j = 0; j < getVisualRange() / 2; j++) {
                                this.map3_pathToUnexploredCell.add(actions.get(i));
                            }
                        }
                        this.map3_lastAction = actions.get(i);
                        return actions.get(i);
                    }
                }

                // Other directions only once
                for (int i = 4; i < borderCells.size(); i++) {
                    if (borderCells.get(i) != null && borderCells.get(i).getRadar() != 1 && borderCells.get(i).getRadar() != 2 && !borderCells.get(i).isExplored()) {
                        this.map3_lastAction = actions.get(i);
                        return actions.get(i);
                    }
                }
            }

            // If everything around the drone already has been explored,
            // look for the closest node with unexplored neighbours
            this.map3_pathToUnexploredCell = dijkstra(position, positions);
            
            // If the way is too long, set drone to standby
            if (this.map3_pathToUnexploredCell != null && this.map3_pathToUnexploredCell.size() > 50 
                    && this.reactivateCounter == 0) {
                setStandby();
            }
            
            // Pick only the next action
            if (this.map3_pathToUnexploredCell != null && !this.map3_pathToUnexploredCell.isEmpty()) {
                Megatron.Action ret = this.map3_pathToUnexploredCell.pop();
                this.map3_pathToUnexploredCell.clear();
                this.map3_pathToUnexploredCell.push(ret);
            } else {
                this.map3_pathToUnexploredCell = new Stack<>();
                this.map3_pathToUnexploredCell.push(null);
            }
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
     * @param positions Positions of the drones to evade collision
     * @return Next action
     * @author Alexander Straub
     */
    public final Megatron.Action mapv4(ArrayList<Coord> positions) {
        if (this.map4_stepNum++ * getConsumation() == 192) {
            setStandby();
        }
        
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
        Megatron.Action ret = mapv4_crossMap(positions);
        
        // Go to the other corner and cross again
        if (ret == null) {
            if (this.reactivateCounter == 1) {
                setStandby();
            }
            this.map4_stop = false;
            
            if (this.getPosition().getX() == 0) {
                for (int i = 2; i < this.map.getResolution(); i++) {
                    this.map4_pathToUnexploredCell.push(Megatron.Action.E);
                }
                ret = Megatron.Action.E;
            } else {
                for (int i = 2; i < this.map.getResolution(); i++) {
                    this.map4_pathToUnexploredCell.push(Megatron.Action.W);
                }
                ret = Megatron.Action.W;
            }
        }
        
        return ret;
    }
    
    /**
     * Try to cross the map
     * 
     * @param positions Positions of the drones to evade collision
     * @return Next action
     * @author Alexander Straub
     */
    protected Megatron.Action mapv4_crossMap(ArrayList<Coord> positions) {
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
        
        Coord closestCoord = null;
        
        // Check if target node is in the graph
        if (this.map.getAccessibleMap().containsKey(this.map4_target)) {
            this.map4_stop = true;
            
            closestCoord = this.map4_target;
        } else {
            // Get neighbour closest to the target            
            if ((closestCoord == null || this.getPosition().NW().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().NW()) != null) {
                closestCoord = this.getPosition().NW();
            }
            if ((closestCoord == null || this.getPosition().N().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().N()) != null) {
                closestCoord = this.getPosition().N();
            }
            if ((closestCoord == null || this.getPosition().NE().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().NE()) != null) {
                closestCoord = this.getPosition().NE();
            }
            if ((closestCoord == null || this.getPosition().E().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().E()) != null) {
                closestCoord = this.getPosition().E();
            }
            if ((closestCoord == null || this.getPosition().SE().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().SE()) != null) {
                closestCoord = this.getPosition().SE();
            }
            if ((closestCoord == null || this.getPosition().S().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().S()) != null) {
                closestCoord = this.getPosition().S();
            }
            if ((closestCoord == null || this.getPosition().SW().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().SW()) != null) {
                closestCoord = this.getPosition().SW();
            }
            if ((closestCoord == null || this.getPosition().W().distanceTo(this.map4_target) < closestCoord.distanceTo(this.map4_target)) && this.map.getAccessibleMap().get(this.getPosition().W()) != null) {
                closestCoord = this.getPosition().W();
            }
            
            if (closestCoord == null || closestCoord.distanceTo(this.map4_target) >= this.getPosition().distanceTo(this.map4_target)) {
                closestCoord = null;
                
                // Find unexplored cell closest to the corner to get to
                Node currentNode;

                for (Iterator<Node> it = this.map.getAccessibleMap().values().iterator();
                        it.hasNext();) {

                    currentNode = it.next();
                    if (currentNode.getRadar() == 0 && !currentNode.isExplored()
                            && (closestCoord == null || this.map4_target.distanceTo(currentNode.getCoord()) < this.map4_target.distanceTo(closestCoord))) {

                        closestCoord = currentNode.getCoord();
                    }
                }
            }
        }
        
        if (closestCoord == null) {
            System.err.println("ERROR: mapv4 called, but map already explored completely");
            return null;
        }

        // Find way to the previously found closest node (or the target)
        this.map4_pathToUnexploredCell = dijkstra(this.getPosition(), closestCoord, positions);
        
        return this.map4_pathToUnexploredCell.pop();
    }
    
    /**
     * Tries to find a way to the target position, updating the way lengths
     * 
     * @param start Current position
     * @param target Target position
     * @param positions Positions of the drones to evade collision
     * @return Next action for the decepticon
     * @author Alexander Straub
     */
    public Megatron.Action findWay(Coord start, Coord target, ArrayList<Coord> positions) {
        if (start.equals(target)) {
            return null;
        }
        
        Megatron.Action action = findWay(start, target, positions, false);
        
        // If it's an ilegal action, try again
        if (this.map.getMap().get(this.currentPosition.neighbour(action)).getRadar() == 1) {
            this.findWay_pathToTarget.clear();
            
            action = findWay(start, target, positions, false);
        }
        
        return action;
    }
    
    /**
     * Tries to find a way to the target position, updating the way lengths
     * 
     * @param start Current position
     * @param target Target position
     * @param positions Positions of the drones to evade collision
     * @param keep Keep the element on the stack, returning null
     * @return Next action for the decepticon
     * @author Alexander Straub
     */
    public Megatron.Action findWay(Coord start, Coord target, ArrayList<Coord> positions, boolean keep) {
        if (start.equals(target)) {
            return null;
        }
        
        if (this.findWay_pathToTarget == null || this.findWay_pathToTarget.isEmpty()) {
            // Execute the search
            this.findWay_pathToTarget = dijkstra(start, target, positions);
            Stack<Megatron.Action> actionsExploredMap = dijkstra(start, target, positions, this.map.getPretendExploredMap());

            // If search was not successfull (when there is no direct path)
            if (this.findWay_pathToTarget == null) {
                this.findWay_pathToTarget = actionsExploredMap;

                // Update way information
                this.findWay_wayLength = 0;
                this.findWay_minWayLength = actionsExploredMap.size();
            } else {
                // Update way information
                this.findWay_wayLength = this.findWay_pathToTarget.size();
                this.findWay_minWayLength = actionsExploredMap.size();
                
                if (this.findWay_pathToTarget.size() > actionsExploredMap.size() * Math.sqrt(2.0)) {
                    this.findWay_pathToTarget = actionsExploredMap;
                }
            }
        }
        
        if (keep) {
            return null;
        }
        return this.findWay_pathToTarget.pop();
    }
    
    /**
     * Return the actual way length needed to arrive at the target position
     * 
     * @return Way length
     * @author Alexander Straub
     */
    public final int findWay_getWayLength() {
        return this.findWay_wayLength;
    }
    
    /**
     * Return the minimal possible way length needed to arrive at the target
     * 
     * @return Minimal possible way length
     * @author Alexander Straub
     */
    public final int findWay_getMinWayLength() {
        return this.findWay_minWayLength;
    }
    
}
