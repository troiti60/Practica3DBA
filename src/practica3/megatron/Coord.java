package practica3.megatron;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import practica3.megatron.Megatron.Action;

/**
 * Represents coordinates in 2D
 *
 * @author Alexander Straub, Antonio Troitiño
 */
public class Coord {

    /**
     * Coordinates
     */
    private int x, y;

    /**
     * Constructor
     *
     * @param x x coordinate
     * @param y y coordinate
     * @author Alexander Straub
     */
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor
     *
     * @param copy Create new coordinates as carbon copy
     * @author Alexander Straub
     */
    public Coord(Coord copy) {
        this.x = copy.getX();
        this.y = copy.getY();
    }
    
    /**
     * Getter for the x coordinate
     *
     * @return x coordinate
     * @author Alexander Straub
     */
    public int getX() {
        return this.x;
    }

    /**
     * Setter to store new x coordinate
     *
     * @param newX New x coordinate
     * @author Alexander Straub
     */
    public void setX(int newX) {
        this.x = newX;
    }

    /**
     * Getter for the y coordinate
     *
     * @return y coordinate
     * @author Alexander Straub
     */
    public int getY() {
        return this.y;
    }

    /**
     * Setter to store new y coordinate
     *
     * @param newY New y coordinate
     * @author Alexander Straub
     */
    public void setY(int newY) {
        this.y = newY;
    }

    /**
     * Return coordinate to the north
     *
     * @return Coordinate to the north
     * @author Alexander Straub
     */
    public Coord N() {
        return new Coord(x, y - 1);
    }

    /**
     * Return coordinate to the east
     *
     * @return Coordinate to the east
     * @author Alexander Straub
     */
    public Coord E() {
        return new Coord(x + 1, y);
    }

    /**
     * Return coordinate to the south
     *
     * @return Coordinate to the south
     * @author Alexander Straub
     */
    public Coord S() {
        return new Coord(x, y + 1);
    }

    /**
     * Return coordinate to the west
     *
     * @return Coordinate to the west
     * @author Alexander Straub
     */
    public Coord W() {
        return new Coord(x - 1, y);
    }

    /**
     * Return coordinate to the northeast
     *
     * @return Coordinate to the northeast
     * @author Alexander Straub
     */
    public Coord NE() {
        return new Coord(x + 1, y - 1);
    }

    /**
     * Return coordinate to the southeast
     *
     * @return Coordinate to the southeast
     * @author Alexander Straub
     */
    public Coord SE() {
        return new Coord(x + 1, y + 1);
    }

    /**
     * Return coordinate to the southwest
     *
     * @return Coordinate to the southwest
     * @author Alexander Straub
     */
    public Coord SW() {
        return new Coord(x - 1, y + 1);
    }

    /**
     * Return coordinate to the northwest
     *
     * @return Coordinate to the northwest
     * @author Alexander Straub
     */
    public Coord NW() {
        return new Coord(x - 1, y - 1);
    }
    
    /**
     * Return the neighbour indicated by an action
     * 
     * @param action Direction from current coordinate
     * @return Neighbour cell coordinates indicated by action
     * @author Alexander Straub
     */
    public Coord neighbour(Action action) {
        if (action == Action.N) return N();
        if (action == Action.NW) return NW();
        if (action == Action.W) return W();
        if (action == Action.SW) return SW();
        if (action == Action.S) return S();
        if (action == Action.SE) return SE();
        if (action == Action.E) return E();
        return NE();
    }
    
    /**
     * Return position with modified x coordinate
     * 
     * @param offsetX Value to add to the current x coordinate
     * @return Modified coordinate
     * @author Alexander Straub
     */
    public Coord addX(int offsetX) {
        return new Coord(this.x + offsetX, this.y);
    }
    
    /**
     * Return position with modified y coordinate
     * 
     * @param offsetY Value to add to the current y coordinate
     * @return Modified coordinate
     * @author Alexander Straub
     */
    public Coord addY(int offsetY) {
        return new Coord(this.x, this.y + offsetY);
    }
    
    /**
     * Return position with modified coordinates
     * 
     * @param offsetX Value to add to the current x coordinate
     * @param offsetY Value to add to the current y coordinate
     * @return Modified coordinates
     * @author Alexander Straub
     */
    public Coord add(int offsetX, int offsetY) {
        return new Coord(this.x + offsetX, this.y + offsetY);
    }

    /**
     * Calculate distance between two nodes
     *
     * @param other Coordinate to compare with
     * @return Distance between two nodes
     * @author Alexander Straub
     */
    public double distanceTo(Coord other) {
        return sqrt(pow(this.x - other.getX(), 2.0) + pow(this.y - other.getY(), 2.0));
    }

    /**
     * Compare two nodes returning true if they are equal
     *
     * @param other Coordinate to compare with
     * @return True if the coordinates are equal
     * @author Alexander Straub
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Coord)) {
            return false;
        }

        Coord second = (Coord) other;
        return (this.x == second.getX()
                && this.y == second.getY());
    }

    /**
     * Create simple hash code
     *
     * @return Hash code
     * @author Antonio Troitiño
     */
    @Override
    public int hashCode() {
        return (this.x + (this.y * 1000));
    }
    
    /**
     * Returns the coordinates as readable string
     * 
     * @return Coordinates
     * @author Alexander Straub
     */
    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

}