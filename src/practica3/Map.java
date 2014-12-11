/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import java.util.HashMap;

/**
 *
 * @author 
 */
public class Map {
    private HashMap map;
    
    public Map(){
        this.map = new HashMap<Coord, Integer>(100000);
    }
    /**
     * Associates the specified value with the specified key in this map
     * If the map previously contained a mapping for the key, the new 
     * value is ignored.
     * @param key Position of the node in the map. Used as key in the HashMap 
     * @param value Radar value for that position
     * @author Antonio Troitiño del Río
     */
    public void addNode(Coord key,int value){
        if(!map.containsKey(key))
            map.put(key, value);
    }
}