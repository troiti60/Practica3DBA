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
    private HashMap<Coord,Nodo> map;
    private static Nodo target=null;
    
    public Map(){
        this.map = new HashMap<Coord, Nodo>(100000);
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
        if(!map.containsKey(key)){
            Nodo newNode=new Nodo(key,value);
            if(value==3&&target==null) target=newNode;
            map.put(key, newNode);
            checkAdjacent(newNode);
            
        
        }
    }
     /**
     * Updates the list of adjacent nodes of the given node
     *
     * @param node Node to be updated
     * @author Antonio Troitiño, Alexander Straub
     */
    public void checkAdjacent(Nodo node){
         Nodo aux;

        if (this.map.containsKey(node.getCoord().NO())) {
            aux = this.map.get(node.getCoord().NO());
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
        if (this.map.containsKey(node.getCoord().SO())) {
            aux = this.map.get(node.getCoord().SO());
            node.add(aux);
            aux.add(node);
        }
        if (this.map.containsKey(node.getCoord().O())) {
            aux = this.map.get(node.getCoord().O());
            node.add(aux);
            aux.add(node);
        }
    }
    /**
     * Returns the collection of nodes known to date
     * @return map containing all of the nodes
     * @author Antonio Trotiño de Río
     */
    public HashMap<Coord,Nodo> getMap(){return map;}
    /**
     * Returns the first node added to the map with a 
     * radar value of 3
     * @return first target sighted,null if target has not been set yet
     * @author Antonio Troitiño del Río
     */
    public static Nodo getTarget(){return target;}
    /**
     * Boolean static method used to know if variable target has been already set
     * @return false if target equals null, true otherwise
     * @author Antonio Troitiño del Río
     */
    public static boolean isTargetSet(){if(target==null)return false; else return true;}
    
}