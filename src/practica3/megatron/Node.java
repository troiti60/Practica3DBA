package practica3.megatron;

import static java.lang.Math.sqrt;
import java.util.ArrayList;

/**
 * Class to represent a node
 *
 * @author Antonio Troitiño del Rio, Alexander Straub
 */
public class Node implements Comparable<Node> {

    // Coordinates of the node
    private final Coord coord;

    // Is connected to the graph of the bot?
    private boolean connected = false;

    // Radar value: 0=free 1=wall 2=border 3=objective
    private final int radar;

    // Array of adyacent nodes
    private final ArrayList<Node> adyacents = new ArrayList<>();

    // Array of adyacent walls
    private final ArrayList<Node> adyacentWalls = new ArrayList<>();

    // Explored if all 8 neighbours are in any of the lists above
    private int explored;

    // Indicates whether the node has already been visited
    private int visited = -1;

    // Variables used by the dijkstra algorithm
    private Node path = null;
    private double distance = Double.MAX_VALUE;

    /**
     * Constructor
     *
     * @param x x coordinate of the node
     * @param y y coordinate of the node
     * @param radar Radar value
     * @author Antonio Troitiño
     */
    public Node(int x, int y, int radar) {
        this.coord = new Coord(x, y);
        this.radar = radar;
        this.explored = 0;
    }

    /**
     * Constructor
     *
     * @param coord Coordinates of the node
     * @param radar Radar value
     * @author Alexander Straub
     */
    public Node(Coord coord, int radar) {
        this.coord = coord;
        this.radar = radar;
        this.explored = 0;
    }

    /**
     * Copy constructor
     *
     * @param n Node to copy
     * @author José Carlos Alfaro
     */
    public Node(Node n) {
        this.coord = n.coord;
        this.radar = n.radar;
        this.explored = n.explored;
    }

    /**
     * Returns the indicater for an explored node
     *
     * @return True if all neighbours are in the list
     * @author Antonio Troitiño
     */
    public boolean isExplored() {
        return this.radar != 1 && this.explored == 8;
    }

    /**
     * Mark as visited node
     *
     * @author Alexander Straub
     */
    public void setVisited(int i) {
        this.visited = i;
    }

    /**
     * Return visit state
     *
     * @return True if already visited
     * @author Alexander Straub
     */
    public int isVisited() {
        return this.visited;
    }

    /**
     * Adds an adjacent node to the right collection
     *
     * @param aNode Node to be added
     * @author Antonio Troitiño
     */
    public void add(Node aNode) {
        if (aNode.getRadar() == 0 || aNode.getRadar() == 3) {
            this.adyacents.add(aNode);
            this.explored++;
        } else if (aNode.getRadar() == 1 || aNode.getRadar() == 2) {
            this.explored++;
            this.adyacentWalls.add(aNode);
        }
    }

    /**
     * Return the x coordinate
     *
     * @return x coordinate
     * @author Antonio Troitiño
     */
    public int getX() {
        return this.coord.getX();
    }

    /**
     * Return the y coordinate
     *
     * @return y coordinate
     * @author Antonio Troitiño
     */
    public int getY() {
        return this.coord.getY();
    }

    /**
     * Return the radar value
     *
     * @return Radar value
     * @author Antonio Troitiño
     */
    public int getRadar() {
        return this.radar;
    }

    /**
     * Return list of adyacent nodes
     *
     * @return List of adyacent nodes
     * @author Antonio Troitiño
     */
    public ArrayList<Node> getAdyacents() {
        return this.adyacents;
    }

    /**
     * Return list of adyacent walls
     *
     * @return List of adyacent walls
     * @author Antonio Troitiño
     */
    public ArrayList<Node> getAdyacentWalls() {
        return this.adyacentWalls;
    }

    /**
     * Set the state of connected to the graph of the bot
     *
     * @param connected True if connected to the graph
     * @author Antonio Troitiño
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Returns the state of connected to the graph of the bot
     *
     * @return True if connected to the graph
     * @author Antonio Troitiño
     */
    public boolean getConnected() {
        return this.connected;
    }

    /**
     * Return coordinates
     *
     * @return Coordinates
     * @author Antonio Troitiño
     */
    public Coord getCoord() {
        return this.coord;
    }

    /**
     * Return coordinates to the north
     *
     * @return Coordinates to the north
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord N() {
        return new Coord(this.coord.getX(), this.coord.getY() - 1);
    }

    /**
     * Return coordinates to the east
     *
     * @return Coordinates to the east
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord E() {
        return new Coord(this.coord.getX() + 1, this.coord.getY());
    }

    /**
     * Return coordinates to the south
     *
     * @return Coordinates to the south
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord S() {
        return new Coord(this.coord.getX(), this.coord.getY() + 1);
    }

    /**
     * Return coordinates to the west
     *
     * @return Coordinates to the west
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord W() {
        return new Coord(this.coord.getX() - 1, this.coord.getY());
    }

    /**
     * Return coordinates to the northeast
     *
     * @return Coordinates to the northeast
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord NE() {
        return new Coord(this.coord.getX() + 1, this.coord.getY() - 1);
    }

    /**
     * Return coordinates to the southeast
     *
     * @return Coordinates to the southeast
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord SE() {
        return new Coord(this.coord.getX() + 1, this.coord.getY() + 1);
    }

    /**
     * Return coordinates to the southwest
     *
     * @return Coordinates to the southwest
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord SW() {
        return new Coord(this.coord.getX() - 1, this.coord.getY() + 1);
    }

    /**
     * Return coordinates to the northwest
     *
     * @return Coordinates to the northwest
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord NW() {
        return new Coord(this.coord.getX() - 1, this.coord.getY() - 1);
    }

    /**
     * Reset values for a new search
     *
     * @author Alexander Straub
     */
    public void resetSearch() {
        this.path = null;
        this.distance = Double.MAX_VALUE;
    }

    /**
     * Set path found by the search
     *
     * @param nodo Nodo delante en el camino
     * @author Alexander Straub
     */
    public void setPath(Node nodo) {
        this.path = nodo;
    }

    /**
     * Return the path found by the search
     *
     * @return Next node on the path
     * @author Alexander Straub
     */
    public Node getPath() {
        return this.path;
    }

    /**
     * Set distance calculated by the search
     *
     * @param distance New distance calculated
     * @author Alexander Straub
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Return the calculated distance
     *
     * @return Calculated distance
     * @author Alexander Straub
     */
    public double getDistance() {
        return this.distance;
    }

    /**
     * Compare two nodes using their distances
     * 
     * Info: only for use in the search algorithm
     *
     * @param other Node to compare with
     * @return -1: other is nearer, +1: other is further away, 0: same distance
     * @author Alexander Straub
     */
    @Override
    public int compareTo(Node other) {
        if (other == null) {
            return 0; // TODO: Exception
        }
        if (other == this) {
            return 0;
        }

        if (other.getDistance() < this.distance) {
            return 1;
        }
        if (other.getDistance() > this.distance) {
            return -1;
        }
        return 0;
    }

    /**
     * Return distance between two nodes
     *
     * @param other Node to compare with
     * @return Distance
     * @author Alexander Straub
     */
    public double distanceTo(Node other) {
        return this.coord.distanceTo(other.getCoord());
    }

    /**
     * Return the value of the heuristic function for actual Nodo
     *
     * @param goal Goal node
     * @return Heuristic value
     * @author Daniel Sánchez Alcaide, Antonio Troitiño del Río
     */
    public double h(Node goal) {
        return sqrt(((this.getX() - goal.getX()) * (this.getX() - goal.getX())) + ((this.getY() - goal.getY()) * (this.getY() - goal.getY())));
    }

    /**
     * Return the value of the consumption function for actual node
     *
     * @param start Starting node
     * @return Consumption value
     * @author Daniel Sánchez Alcaide
     */
    public double g(Node start) {
        double steps = 0;
        while (this.getPath().getCoord() != start.getCoord()) {
            steps += 1;
        }
        return steps;
    }

    /**
     * Return the value of the f function for actual node
     *
     * @param start Starting node
     * @param goal Goal node
     * @return Consumption value
     * @author Daniel Sánchez Alcaide
     */
    public double f(Node start, Node goal) {
        return this.g(start) + this.h(goal);
    }
}
