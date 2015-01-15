/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import practica3.Draw.Ventana;

/**
 * Class that controls the rest of Decepticons
 *
 * @author Fco Javier Ortega Rodriguez
 */
public class Megatron extends SingleAgent {

    private final ArrayList<Decepticon> drones;
    private Map myMap;
    private int energyOfWorld;
    private ACLMessage inbox, outbox;
    private JsonDBA json;
    private DataAccess dataAccess;
    private Nodo nodoGoal;
    private Ventana draw;
    
    private State state;
    private String msg;
    private boolean live;
    private Action sigAction;
    private int numeroDron;

    private int pasos = 0;
    private boolean zoneGoalFound = false;
    
    // Image of the map for visualization
    //private MapImage mapImage = null;

    /**
     * Enum with possible movement actions
     *
     * @author Daniel Sánchez Alcaide
     */
    public enum Action {

        NW("moveNW"),
        N("moveN"),
        NE("moveNE"),
        E("moveE"),
        SE("moveSE"),
        S("moveS"),
        SW("moveSW"),
        W("moveW");

        // String that defines the action
        private final String command;

        /**
         * Constructor
         *
         * @param command String that defines the action to take
         * @author Daniel Sánchez Alcaide
         */
        private Action(final String command) {
            this.command = command;
        }

        /**
         * Return the string that defines the action
         *
         * @return String that defines the action
         * @author Daniel Sánchez Alcaide
         */
        @Override
        public String toString() {
            return this.command;
        }
    }

    /**
     * Enum of state
     *
     * @author Fco Javier Ortega Rodríguez
     */
    public enum State {

        Subscribe(0),
        Create(1),
        LaunchRest(2),
        Feel(3),
        Heuristic(4),
        Action(5),
        Cancel(6);

        private final int value;

        private State(final int value) {
            this.value = value;
        }
    }

    /**
     * Constructor
     * 
     * @param aid Agent ID
     * @throws Exception 
     * @author Alexander Straub
     */
    public Megatron(AgentID aid) throws Exception {
        super(aid);
        drones = new ArrayList<>(4);
    }

    @Override
    protected void init() {
        this.myMap = new Map();
        this.dataAccess = DataAccess.crearInstancia();
        System.out.println("Va a crear la ventana");
        draw = new Ventana();     
        draw.setResizable(true);
        draw.setVisible(true);
        System.out.println("VHa terminado  de crear la ventana");

        //int resolution = 100;
        //if (this.dataAccess.getWorld().equals("newyork")) resolution = 500;
        //this.mapImage = new MapImage(resolution);
    }

    /**
     * This method is thought to be called by Megatron itself after parsing a
     * new perception of one of his drons so he can update the map.
     *
     * @param pos current dron position
     * @param perception last perception received by decepticon
     * @param dron local identifier of the sender. It must be an integer between
     * 0 and 3
     * @author Antonio Troitiño del Río
     */
    private void updateMap(Coord pos, ArrayList<Integer> perception, int dron) {
        draw.getJpanel().setWorld(dataAccess.getWorld());
        
        if (perception.isEmpty() || dron >= drones.size()) {
            System.err.println("ERROR: Megatron received an empty perception!");
        } else {
            System.err.println("Creando coordenadas");
            Coord c = new Coord(pos.getX(), pos.getY());
            drones.get(dron).setPosition(pos);
            draw.getJpanel().setDronPosition(pos);
           
            System.err.println("Actualizando mapa");
            //this.mapImage.setDronePosition(dron, pos);
            int cont = Math.round((float) Math.sqrt(perception.size()));
            cont = (cont - 1) / 2;
            int count = 0;
            for (int i = 0 - cont; i <= cont; i++) {
                for (int j = 0 - cont; j <= cont; j++) {
                    myMap.addNode(new Coord(pos.getX() + j, pos.getY() + i), perception.get(count));                   
                    //this.mapImage.setCell(perception.get(count), new Coord(pos.getX() + j, pos.getY() + i));
                    count++;
                }
            }  
            myMap.getMap().get(c).setVisitado(dron);
            draw.getJpanel().updateDraw(myMap,dron);
            draw.setLabelCoordinate(pos.getX(), pos.getY(),dron);     
            draw.setBatteryDroneValue(dron,drones.get(dron).getFuel());
            draw.setTotalBatteryValue(energyOfWorld);
        }
        System.out.println("Megatron: Mapa actualizado");
    }

    /**
     * Method to update data related to Decepticon
     *
     * @param numDron The number of Decepticon to update
     * @param newCoord The new coordinate
     * @param newFuel The current fuel
     * @author Fco Javier Ortega Rodriguez
     *
     */
    private void updateDataDecepticon(int numDron, Coord newCoord, int newFuel) {
        this.drones.get(numDron).setPosition(newCoord);
        this.drones.get(numDron).setFuel(newFuel);
    }

    /**
     * Send the message to subscribe to the world
     *
     * @author JC con su flow
     *
     */
    public void Suscribe() {

        json = new JsonDBA();
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setSender(getAid());
        outbox.setContent(json.crearJson("world", dataAccess.getWorld()));
        System.out.println("\tContenido subscribe: " + outbox.getContent());
        this.send(outbox);

    }

    /**
     * Send the message to cancel the session
     *
     * @author JC
     */
    public void Cancel() {

        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.CANCEL);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setSender(getAid());
        outbox.setContent(dataAccess.getKey());
        this.send(outbox);
    }

    /**
     * It will send the order to move depending the action.
     *
     * @author JC
     * @param nameDron Agent's name will recibe the message
     * @param action Kind of movement.
     */
    public void Move(String nameDron, Action action) {
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", action.toString());
        hm.put("key", dataAccess.getKey());
        String msg = json.crearJson(hm);

        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID(nameDron));
        outbox.setContent(msg);
        this.send(outbox);
    }

    /**
     * it will send the order to refuel
     *
     * @author JC
     * @param nameDron Agent's name will recibe the message
     */
    public void Refuel(String nameDron) {
        JsonDBA json = new JsonDBA();
        ACLMessage outbox;
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", "refuel");
        hm.put("key", dataAccess.getKey());
        String msg = json.crearJson(hm);

        outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID(nameDron));
        outbox.setContent(msg);
        this.send(outbox);

    }

    @Override
    public void execute() {
        state = State.Subscribe;
        msg = null;
        live = true;
        sigAction = null;
        numeroDron = -1;
        System.out.println("Megatron: Iniciado");

        while (live) {
            pasos++;
            System.err.println("Paso: " + pasos);
            switch (state) {
                // Suscribe
                case Subscribe:
                    System.out.println("Megatron------ Estado: Subscribe");
                    Suscribe();

                    try {
                        inbox = receiveACLMessage();
                        msg = inbox.getContent();
                    } catch (InterruptedException ex) {
                        System.err.println("Megatron: Problema al recibir mensaje en Subscribe: " + ex);
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        JsonDBA json = new JsonDBA();
                        String result = (String) json.getElement(msg, "result");
                        dataAccess.setKey(result);
                        System.out.println("Megatron: Clave recibida " + dataAccess.getKey());
                        System.out.println("Megatron: Cambiando a estado Create");
                        state = State.Create;

                    } else {
                        System.err.println("Megatron ERROR: " + inbox.getPerformative());
                        state = State.Cancel;
                    }
                    break;

                // Lanzar x drones de mapeo
                case Create:
                    System.out.println("Megatron------ Estado: Create");
                    try {
                        this.drones.add(new Birdron(new AgentID(DataAccess.getNameDron1()), 
                                this.getAid(), dataAccess.getKey(), this.myMap));

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    System.out.println("Megatron: Lanzando decepticion 1...");
                    this.drones.get(0).start();

                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;

                    break;

                case LaunchRest:
                    System.out.println("Megatron------ Estado: LaunchRest");
                    try {
                        this.drones.add(new Birdron(new AgentID(DataAccess.getNameDron2()),
                                this.getAid(), dataAccess.getKey(), this.myMap));

                        this.drones.add(new Birdron(new AgentID(DataAccess.getNameDron3()),
                                this.getAid(), dataAccess.getKey(), this.myMap));
                        
                        this.drones.add(new Birdron(new AgentID(DataAccess.getNameDron4()),
                                this.getAid(), dataAccess.getKey(), this.myMap));

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    System.out.println("Megatron: Lanzando decepticion 2...");
                    this.drones.get(1).start();

                    System.out.println("Megatron: Lanzando decepticion 3...");
                    this.drones.get(2).start();

                    System.out.println("Megatron: Lanzando decepticion 4...");
                    this.drones.get(3).start();

                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;

                    break;
                // Esperar x percepciones
                case Feel:
                    System.out.println("Megatron------ Estado: Feel");
                    numeroDron = -1;

                    try {
                        System.out.println("Megatron: Esperando mensaje");
                        inbox = receiveACLMessage();
                        System.out.println("Megatron: Mensaje recibido de " + inbox.getSender().toString());
                        System.out.println("\tM: " + inbox.getSender().getLocalName() + " " + inbox.getPerformative() + " " + inbox.getContent());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: Error al recibir el mensaje");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    //if(!inbox.getSender().getLocalName().equals("Canis")){
                    System.out.println("Megatron: Recibido de un Decepticon");

                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        System.out.println("Megatron: Informe");

                        json = new JsonDBA();
                        String result = json.getElement(inbox.getContent(), "result").toString();
                        int battery = json.getElementInteger(result, "battery");
                        System.out.println("Mostrando Bateria: " + battery);
                        int x = json.getElementInteger(result, "x");
                        int y = json.getElementInteger(result, "y");
                        Coord nuevaCordenada = new Coord(x, y);
                        System.out.println("Coordenadas guardadas: " + "(" + x + " , " + y + ")");
                        ArrayList<Integer> sensor = json.jsonElementToArrayInt(json.getElement(result, "sensor"));
                        int energy = json.getElementInteger(result, "energy");
                        System.out.println("Mostrando energia restante: " + energy);
                        boolean goal = (boolean) json.getElement(result, "goal");
                        boolean goalFound = (boolean)sensor.contains(3);
                        
                        if (inbox.getSender().getLocalName().equals(this.drones.get(0).getName())) {
                            numeroDron = 0;
                        } else if (inbox.getSender().getLocalName().equals(this.drones.get(1).getName())) {
                            numeroDron = 1;
                        } else if (inbox.getSender().getLocalName().equals(this.drones.get(2).getName())) {
                            numeroDron = 2;
                        } else if (inbox.getSender().getLocalName().equals(this.drones.get(3).getName())) {
                            numeroDron = 3;
                        }

                        System.out.println("\tSensor      " + sensor.toString());

                        updateMap(nuevaCordenada, sensor, numeroDron);
                        updateDataDecepticon(numeroDron, nuevaCordenada, battery);
                        energyOfWorld = energy;

                        if (goal) {
                            System.out.println("\tGoal     Si");

                            this.drones.get(numeroDron).setMyGoal(nuevaCordenada);
                            
                            state = State.Heuristic;
                        } else if (goalFound) {
                            if (!zoneGoalFound) {
                                // Es el primer dron que llega, asignar metas al resto
                                // Llamar al método para aparcar
                                
                                int resolution;
                                switch (this.drones.get(numeroDron).getRole()) {
                                    case 0: resolution = 3; break;
                                    case 1: resolution = 5; break;
                                    default: resolution = 11; break;
                                }
                                
                                int mapWidth = 100;
                                if (this.dataAccess.getWorld().equals("newyork")) mapWidth = 500;
                                
                                int index = sensor.indexOf(3);
                                int i = index % resolution; i -= resolution / 2;
                                int j = index / resolution; j -= resolution / 2;
                                
                                Coord coordGoal = nuevaCordenada.add(i, j);
                                if (coordGoal.getX() >= 0 && coordGoal.getY() >= 0 && 
                                        coordGoal.getX() < mapWidth && coordGoal.getY() < mapWidth) {
                                    
                                    zoneGoalFound = true;
                                    System.err.println("Paso " + pasos + "\n\n###################\nD:" + numeroDron + " ha encontrado el objetivo\n###########\n");
                                    System.out.println("Megatron: Cambiando a estado LaunchRest");
                                    state = State.LaunchRest;
                                    
                                    nodoGoal = new Nodo(coordGoal, 3);
                                    
                                    // DEBUG: only until goal has been found
                                    state = State.Cancel;
                                } else {
                                    System.out.println("\tGoal     No");
                                    System.out.println("Megatron: Cambiando a estado Heuristic");
                                    state = State.Heuristic;
                                }
                            }
                        } else {
                            System.out.println("\tGoal     No");
                            System.out.println("Megatron: Cambiando a estado Heuristic");
                            state = State.Heuristic;
                        }

                        if (goal) {
                            System.err.println("Paso " + pasos + "\n\n###################\nD:" + numeroDron + " En objetivo\n###########\n");

                                // si todos los vivos han llegado
                            //System.out.println("Megatron: Primer decepticon llegó, cancelando...");
                            //state = State.Cancel;
                        }
                    } else {
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
                    //}

                    break;

                // Heuristica
                case Heuristic:
                    System.out.println("Megatron------ Estado: Heuristic");
                    try {
                        if (fuelH(numeroDron, nodoGoal)) { // Heuristica refuel
                            System.err.println("Megatron: Necesita repostar");
                            Refuel(this.drones.get(numeroDron).getName());
                            System.err.println("Megatron: Cambiando a estado Feel");
                            state = State.Feel;
                        } else {
                            if (zoneGoalFound) {
                                // Nodo goal ha de ser drones.get(numero).getGoal
                                // su ndo asignado para aterrizar
                                System.err.println("Megatron: Usando la búsqueda desde: ("
                                        + this.drones.get(numeroDron).getPosition().getX() + ","
                                        + this.drones.get(numeroDron).getPosition().getY() + ") hasta ("
                                        + nodoGoal.toString());
                                sigAction = this.drones.get(numeroDron).aStar(this.myMap.getMap().get(this.drones.get(numeroDron).getPosition()), nodoGoal).firstElement();
                            } else {
                                sigAction = this.drones.get(numeroDron).mapv3();
                            }

                            System.out.println("Megatron: Dron " + numeroDron + " accion " + sigAction);

                            System.out.println("Megatron: Cambiando a estado Action");
                            state = State.Action; // o cancel si ya han llegado todos
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: Error en la heuristica");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
                    break;

                // Dar orden a drone x
                case Action:
                    System.out.println("Megatron------ Estado: Action");

                    System.out.println("Megatron: Realizando la acción " + sigAction + " en " + this.drones.get(numeroDron).getName());
                    Move(this.drones.get(numeroDron).getName(), sigAction);

                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;

                    break;

                // Cancelar todo para reiniciar
                case Cancel:
                    System.out.println("Megatron------ Estado: Cancel");

                    System.out.println("\n#######\nInforme\n#######");
                    for (int i = 0; i < drones.size(); i++) {
                        System.out.print("\n\t" + this.drones.get(i).getName() + "\t");

                        if (this.drones.get(i).isAlive()) {
                            System.out.print("Vivo");
                        } else {
                            System.out.print("Muerto");
                        }

                        if (this.drones.get(i).isInGoal()) {
                            System.out.print("\ten meta");
                        } else {
                            System.out.print("\tfuera de la meta");
                        }
                    }
                    System.out.println("\n#######\n#######");
                    Cancel();
                    System.out.println("Megatron: Muriendo");
                    live = false;
                    
                    /*try {
                        this.mapImage.saveToFile();
                    } catch (Exception e) {}*/

                    break;
            }
        }

        System.out.println("Megatron: Muerto");
    }

    

    /**
     * Fuel heuristic
     *
     * @param pasos steps from current position to reach goal
     * @param drone the integer of the current bot
     * @param goal goal Nodo
     * @return true if steps to reach goal equals pasos, false otherwise
     * @author Daniel Sánchez Alcaide
     */
    private boolean fuelH(int drone, Nodo goal) throws Exception {
        boolean res = false;
        int consumo = 1;
        switch (drones.get(drone).getRole()) {
            //mosca
            case 0:
                consumo = 2;
                break;
            //pájaro (del terror)
            case 1:
                consumo = 1;
                break;
            //halcón (milenario)
            case 2:
                consumo = 4;
                break;
        }
        if (!zoneGoalFound && drones.get(drone).getFuel() <= consumo) {
            res = true;
        } else {
            HashMap<Coord, Nodo> map = myMap.getMap();
            Nodo current = new Nodo(drones.get(drone).getPosition().getX(),
                    drones.get(drone).getPosition().getY(),
                    map.get(drones.get(drone).getPosition()).getRadar());
            if (drones.get(drone).aStar(current, goal).capacity() * consumo == 100) {
                //Esto consume mucho tiempo de CPU, es mejor crear una variable en la clase y
                //guardar ahí la pila cuando se llame a la búsqueda desde los mapeos o desde donde sea
                res = true;
            }
        }
        return res;
    }
}
