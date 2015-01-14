/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import java.util.Comparator;

/**
 *
 * @author Daniel Sánchez Alcaide
 */
public class ComparadorHeuristicaNodo implements Comparator<Nodo> {
    private final Nodo goal;
    
    public ComparadorHeuristicaNodo(Nodo goal) {
        this.goal = goal;
    }
    
    /**
     * Compare two nodes for the priority queue comparator
     * @param x a node
     * @param y the other node
     * @return -1 if x priority is higher than y; 1 if y priority is higher than x;
     * 0 if both have same priority
     * @author Daniel Sánchez Alcaide
     */
    @Override
    public int compare(Nodo x, Nodo y){
        if (x.h(goal) < y.h(goal))
        {
            return 1;
        }
        if (x.h(goal) > y.h(goal))
        {
            return -1;
        }
        return 0;
    }
}
