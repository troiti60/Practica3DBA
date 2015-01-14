package practica3;

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
    private static Node target = null;

    /**
     * Constructor
     * 
     * @param resolution Resolution of the map
     * @author Antonio Troitiño del Río
     */
    public Map(int resolution) {
        this.map = new HashMap<>((resolution + 2) * (resolution + 2));
        this.accessible = new HashMap<>((resolution + 2) * (resolution + 2));
        
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
        Node newNode = new Node(key, value);

        if (!map.containsKey(key)) {
            if (value == 3 && target == null) {
                target = newNode;
            }
            map.put(key, newNode);
            checkAdjacent(newNode);
        }

        // Add if it is not a wall, also to a map for accessible cells
        // (advantage for search algorithms using only a graph of accessible cells)
        if (!this.accessible.containsKey(key) && (value == 0 || value == 3)) {
            this.accessible.put(key, newNode);
        }
    }

    /**
     * Updates the list of adjacent nodes of the given node
     *
     * @param node Node to be updated
     * @author Antonio Troitiño, Alexander Straub
     */
    public void checkAdjacent(Node node) {
        Node aux;

        if (this.map.containsKey(node.getCoord().NW())) {
            aux = this.map.get(node.getCoord().NW());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().N())) {
            aux = this.map.get(node.getCoord().N());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().NE())) {
            aux = this.map.get(node.getCoord().NE());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().E())) {
            aux = this.map.get(node.getCoord().E());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().SE())) {
            aux = this.map.get(node.getCoord().SE());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().S())) {
            aux = this.map.get(node.getCoord().S());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().SW())) {
            aux = this.map.get(node.getCoord().SW());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().W())) {
            aux = this.map.get(node.getCoord().W());
            node.add(aux);
            aux.add(node);
        }
    }

    /**
     * Returns the collection of nodes known to date
     *
     * @return Map containing all of the nodes
     * @author Antonio Trotiño de Río
     */
    public HashMap<Coord, Node> getMap() {
        return map;
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
     * Returns the first node added to the map with a radar value of 3
     *
     * @return First target sighted, null if target has not been set yet
     * @author Antonio Troitiño del Río
     */
    public static Node getTarget() {
        return target;
    }

    /**
     * Boolean static method used to know if variable target has been already
     * set
     *
     * @return False if target equals null, true otherwise
     * @author Antonio Troitiño del Río
     */
    public static boolean isTargetSet() {
        return target != null;
    }

}
