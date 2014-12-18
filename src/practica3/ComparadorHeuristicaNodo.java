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
public class ComparadorHeuristicaNodo implements Comparator<Nodo>{
    private static Nodo goal = Megatron.getTarget();
    
    /**
     * Compare two nodos for the priority queue comparator
     * @param x one nodo
     * @param y the other nodo
     * @return -1 if x priority is higher that y; 1 if y priority is higher that x;
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
