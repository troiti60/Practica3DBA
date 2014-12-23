/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import java.util.Stack;
import practica3.Megatron.Action;

/**
 *Class DataDecepticon made to keep interesting data of any Decepticon known by megatron
 * @author Antonio Troitiño del Río
 */
public class DataDecepticon {
    //Current position, last position and initial position
    private Coord current=null, last=null,start=null;
    private int fuel=100,role;
    private String name;
    private AgentID id;
    private boolean alive;
    private Stack<Action> todo;
    Action lastAction;

    /**
     * Builds a new DataDecepticon object from its name
     * @param aname name for the new DataDecepticon
     * @param arole role for the decepticon
     */
    DataDecepticon(String aname,int arole){
        name=aname;
        role=arole;
        id=new AgentID(aname);
        alive=true;
        fuel=100;
        todo=new Stack<Action>();
    }

    /**
     * @return the current position
     * @author Antonio Troitiño del Río
     */
    public Coord getCurrent() {
        return current;
    }
    /**
     * Adds an action to the todo list
     * @param anAction the action to be added
     * @author Antonio Troitiño del Río
     */
    public void push(Action anAction) {
        todo.push(anAction);
    }/**
     * @return true if drone has assigned things to do
     * @author Antonio Troitiño del Río
     */
    public boolean hasWork() {
        return (!todo.isEmpty());
    }
    /**
     * @return the next action to be done
     * @author Antonio Troitiño del Río
     */
    public Action getAction() {
        lastAction=todo.pop();
        return lastAction;
    }
    public void doThat(Action anAction){
        lastAction=anAction;
    }
    public Action getLastAction(){
        return lastAction;
    }
    public void cancelJob(){
        while(!todo.isEmpty()) todo.pop();
    }
    
    /**
     * 
     * @return the position of the map where the bot started;
     * @authro Antonio Troitiño del Río
     */
    public Coord getStart(){
        return start;
    }
    public void willMoveTo(Coord next){
        last=current;
        current=next;
        fuel--;
    }
    public void setPosition(Coord pos){
        if(start==null){start=pos;current=pos;}
        else if(!current.equals(pos)){
        System.err.println("Drone named "+name+" was supposed to be at ("+
                current.getX()+","+current.getY()+") and was found at ("+
                pos.getX()+","+pos.getY()+")");
        current=pos;
        }
        
    }
    /**
     * @return the last position
     * @author Antonio Troitiño del Río
     */
    public Coord getLast() {
        return last;
    }

    /**
     * @return the remaining fuel
     * @author Antonio Troitiño del Río
     */
    public int getFuel() {
        return fuel;
    }

    /**
     * @param fuel the fuel to set
     * @author Antonio Troitiño del Río
     */
    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    /**
     * @return the current role
     * @author Antonio Troitiño del Río
     */
    public int getRole() {
        return role;
    }

    /**
     * @param role the role to set
     * @author Antonio Troitiño del Río
     */
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * @return the drone name
     * @author Antonio Troitiño del Río
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     * @author Antonio Troitiño del Río
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the drone id
     * @author Antonio Troitiño del Río
     */
    public AgentID getId() {
        return id;
    }

    /**
     * @param id the id to set
     * @author Antonio Troitiño del Río
     */
    public void setId(AgentID id) {
        this.id = id;
    }

    /**
     * @return true if decepticon is alive, false otherwise
     * @author Antonio Troitiño del Río
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * @param alive the new value for alive
     * @author Antonio Troitiño del Río
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    
    
}
