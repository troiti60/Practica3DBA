package practica3.megatron;

import java.util.HashMap;

/**
 * Class to store the nodes of the map
 * 
 * @author Antonio Troitiño del Río
 */
public class Map {

    /**
     * Maps and target
     */
    private final HashMap<Coord, Node> map;
    private final HashMap<Coord, Node> accessible;
    private final HashMap<Coord, Node> pretendExplored;
    private Coord target = null;
    private final int resolution;

    /**
     * Constructor
     * 
     * @param resolution Resolution of the map
     * @author Antonio Troitiño del Río
     */
    public Map(int resolution) {
        this.map = new HashMap<>((resolution + 2) * (resolution + 2));
        this.accessible = new HashMap<>((resolution + 2) * (resolution + 2));
        this.pretendExplored = new HashMap<>((resolution + 2) * (resolution + 2));
        
        // Fill map as it was discovered completely (assuming only
        // accessible cells)
        for (int i = 0; i < resolution; i++) {
            for (int j = 0; j < resolution; j++) {
                Node node = new Node(i, j, 0);
                this.pretendExplored.put(new Coord(i, j), node);
                checkAdjacent(node, this.pretendExplored);
            }
        }
        
        // Add border at top and bottom
        for (int i = -1; i <= resolution; i++) {
            addNode(new Coord(i, -1), 2);
            addNode(new Coord(i, resolution), 2);
        }
        
        // Add border at sides
        for (int i = 0; i < resolution; i++) {
            addNode(new Coord(-1, i), 2);
            addNode(new Coord(resolution, i), 2);
        }
        
        this.resolution = resolution;
    }

    /**
     * Associates the specified value with the specified key in this map If the
     * map previously contained a mapping for the key, the new value is ignored.
     *
     * @param key Position of the node in the map. Used as key in the HashMap
     * @param value Radar value for that position
     * @author Antonio Troitiño del Río, Alexander Straub
     */
    public final void addNode(Coord key, int value) {
        addNode(new Node(key, value));
    }
    
    /**
     * Associates the specified value with the specified key in this map If the
     * map previously contained a mapping for the key, the new value is ignored.
     *
     * @param node The node to add
     * @author Antonio Troitiño del Río, Alexander Straub
     */
    public final void addNode(Node node) {
        if (!this.map.containsKey(node.getCoord())) {
            if (node.getRadar() == 3 && this.target == null) {
                this.target = node.getCoord();
            }
            
            this.map.put(node.getCoord(), node);
            checkAdjacent(node);
            
            node = new Node(node);
            this.pretendExplored.put(node.getCoord(), node);
            checkAdjacent(node, this.pretendExplored);
        }

        // Add if it is not a wall, also to a map for accessible cells
        // (advantage for search algorithms using only a graph of accessible cells)
        if (!this.accessible.containsKey(node.getCoord()) && (node.getRadar() == 0 || node.getRadar() == 3)) {
            node = new Node(node);
            this.accessible.put(node.getCoord(), node);
            checkAdjacent(node, this.accessible);
        }
    }

    /**
     * Updates the list of adjacent nodes of the given node
     *
     * @param node Node to be updated
     * @author Alexander Straub
     */
    private void checkAdjacent(Node node) {
        checkAdjacent(node, this.map);
    }
    
    /**
     * Updates the list of adjacent nodes of the given node
     *
     * @param node Node to be updated
     * @param map Map to use as reference
     * @author Antonio Troitiño, Alexander Straub
     */
    private void checkAdjacent(Node node, HashMap<Coord, Node> map) {
        Node aux;

        if (map.containsKey(node.getCoord().NW())) {
            aux = map.get(node.getCoord().NW());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().N())) {
            aux = map.get(node.getCoord().N());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().NE())) {
            aux = map.get(node.getCoord().NE());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().E())) {
            aux = map.get(node.getCoord().E());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().SE())) {
            aux = map.get(node.getCoord().SE());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().S())) {
            aux = map.get(node.getCoord().S());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().SW())) {
            aux = map.get(node.getCoord().SW());
            node.add(aux);
            aux.add(node);
        }
        if (map.containsKey(node.getCoord().W())) {
            aux = map.get(node.getCoord().W());
            node.add(aux);
            aux.add(node);
        }
    }
    
    /**
     * Moves the node from the adyacent list of its neighbours to the adyacentWalls
     *
     * @param node Node to be updated
     * @param map Map to use as reference
     * @author Antonio Troitiño, Alexander Straub
     */
    private void updateAdjacent(Node node, HashMap<Coord, Node> map) {
        Node aux;

        if (map.containsKey(node.getCoord().NW())) {
            aux = map.get(node.getCoord().NW());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().N())) {
            aux = map.get(node.getCoord().N());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().NE())) {
            aux = map.get(node.getCoord().NE());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().E())) {
            aux = map.get(node.getCoord().E());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().SE())) {
            aux = map.get(node.getCoord().SE());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().S())) {
            aux = map.get(node.getCoord().S());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().SW())) {
            aux = map.get(node.getCoord().SW());
            aux.move(node);
        }
        if (map.containsKey(node.getCoord().W())) {
            aux = map.get(node.getCoord().W());
            aux.move(node);
        }
    }
    
    /**
     * Removes the node from the adyacent list of its neighbours
     *
     * @param node Node to be updated
     * @param map Map to use as reference
     * @author Antonio Troitiño, Alexander Straub
     */
    private void removeAdjacent(Node node, HashMap<Coord, Node> map) {
        Node aux;

        if (map.containsKey(node.getCoord().NW())) {
            aux = map.get(node.getCoord().NW());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().N())) {
            aux = map.get(node.getCoord().N());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().NE())) {
            aux = map.get(node.getCoord().NE());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().E())) {
            aux = map.get(node.getCoord().E());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().SE())) {
            aux = map.get(node.getCoord().SE());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().S())) {
            aux = map.get(node.getCoord().S());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().SW())) {
            aux = map.get(node.getCoord().SW());
            aux.remove(node);
        }
        if (map.containsKey(node.getCoord().W())) {
            aux = map.get(node.getCoord().W());
            aux.remove(node);
        }
    }
    
    /**
     * Set node as not accessible because of parking drone
     * 
     * @param coord Coord of parking drone
     * @author Alexander Straub
     */
    public void setDroneParkingSpace(Coord coord) {
        this.map.get(coord).setOccupied();
        updateAdjacent(this.map.get(coord), this.map);
        
        removeAdjacent(this.accessible.remove(coord), this.accessible);
        
        this.pretendExplored.get(coord).setOccupied();
        updateAdjacent(this.pretendExplored.get(coord), this.pretendExplored);
    }

    /**
     * Returns the collection of nodes known to date
     *
     * @return Map containing all of the nodes
     * @author Antonio Trotiño de Río
     */
    public HashMap<Coord, Node> getMap() {
        return this.map;
    }

    /**
     * Returns the map with only accessible cells
     *
     * @return Map containing accessible cells
     * @author Alexander Straub
     */
    public HashMap<Coord, Node> getAccessibleMap() {
        return this.accessible;
    }
    
    /**
     * Returns the map. Where it is unexplored it assumes accessible cells
     * 
     * @return Map pretending to be explored
     * @author Alexander Straub
     */
    public HashMap<Coord, Node> getPretendExploredMap() {
        return this.pretendExplored;
    }

    /**
     * Returns the first node added to the map with a radar value of 3
     *
     * @return First target sighted, null if target has not been set yet
     * @author Antonio Troitiño del Río
     */
    public Coord getTarget() {
        return this.target;
    }

    /**
     * Boolean static method used to know if variable target has been already
     * set
     *
     * @return False if target equals null, true otherwise
     * @author Antonio Troitiño del Río
     */
    public boolean isTargetSet() {
        return this.target != null;
    }
    
    /**
     * Returns the resolution of the map
     * 
     * @return Resolution of the map
     * @author Alexander Straub
     */
    public int getResolution() {
        return this.resolution;
    }

}
