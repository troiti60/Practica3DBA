package practica3;

import java.util.Comparator;

/**
 * Comparator to compare distance to goal between two nodes
 * 
 * @author Daniel Sánchez Alcaide
 */
public class ComparadorHeuristicaNodo implements Comparator<Node> {

    private final Node goal;

    /**
     * Constructor
     * 
     * @param goal Goal for comparison
     * @author Alexander Straub
     */
    public ComparadorHeuristicaNodo(Node goal) {
        this.goal = goal;
    }

    /**
     * Compare two nodes for the priority queue comparator
     *
     * @param x a node
     * @param y the other node
     * @return -1 if x priority is higher than y; 1 if y priority is higher than
     * x; 0 if both have same priority
     * @author Daniel Sánchez Alcaide
     */
    @Override
    public int compare(Node x, Node y) {
        if (x.h(goal) < y.h(goal)) {
            return 1;
        }
        if (x.h(goal) > y.h(goal)) {
            return -1;
        }
        return 0;
    }
}
