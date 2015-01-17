package practica3.megatron;

import practica3.drones.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import practica3.DataAccess;
import practica3.Draw.Window;
import practica3.JsonDBA;
import practica3.Draw.MapImage;

/**
 * Class that controls the rest of Decepticons
 *
 * @author Javier Ortega Rodriguez
 */
public class Megatron extends SingleAgent {

    private final ArrayList<DataDecepticon> drones;
    private Map myMap;
    private int energyOfWorld;
    private final JsonDBA json;
    private DataAccess dataAccess;
    private Node nodoGoal;
    private Window draw;

    private State state;
    private boolean live;
    private Action sigAction;
    private int droneNumber;

    private int pasos = 0;
    private boolean zoneGoalFound = false;

    // Image of the map for visualization
    private MapImage mapImage = null;

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
        this.json = new JsonDBA();
    }

    @Override
    protected void init() {
        this.dataAccess = DataAccess.createInstance();

        int resolution = 100;
        if (this.dataAccess.getWorld().equals("newyork")) {
            resolution = 500;
        }

        this.myMap = new Map(resolution);
        draw = new Window();
        draw.setResizable(true);
        draw.setVisible(true);

        this.mapImage = new MapImage(resolution);
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
        if (perception.isEmpty() || dron >= drones.size()) {
            System.err.println("ERROR: Megatron received an empty perception!");
        } else {
            drones.get(dron).setPosition(pos);
            this.mapImage.setDronePosition(dron, pos);
            int cont = Math.round((float) Math.sqrt(perception.size()));
            cont = (cont - 1) / 2;
            int count = 0;
            for (int i = 0 - cont; i <= cont; i++) {
                for (int j = 0 - cont; j <= cont; j++) {
                    myMap.addNode(new Coord(pos.getX() + j, pos.getY() + i), perception.get(count));
                    this.mapImage.setCell(perception.get(count), new Coord(pos.getX() + j, pos.getY() + i));
                    count++;
                }
            }
        }

        draw.getJpanel().updateDraw(myMap, drones.get(dron));
        draw.setLabelCoordinate(pos.getX(), pos.getY());
        draw.setBatteryDroneValue(drones.get(dron).getFuel());
        draw.setTotalBatteryValue(energyOfWorld);

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
        ACLMessage outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setReceiver(new AgentID("Canis"));
        outbox.setSender(getAid());
        outbox.setContent(json.createJson("world", dataAccess.getWorld()));
        System.out.println("\tContenido subscribe: " + outbox.getContent());
        this.send(outbox);

    }

    /**
     * Send the message to cancel the session
     *
     * @author JC
     */
    public void Cancel() {
        ACLMessage outbox = new ACLMessage();
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
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", action.toString());
        hm.put("key", dataAccess.getKey());
        String msg = json.createJson(hm);

        ACLMessage outbox = new ACLMessage();
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
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", "refuel");
        hm.put("key", dataAccess.getKey());
        String msg = json.createJson(hm);

        ACLMessage outbox = new ACLMessage();
        outbox.setSender(getAid());
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setReceiver(new AgentID(nameDron));
        outbox.setContent(msg);
        this.send(outbox);

    }

    @Override
    public void execute() {
        ACLMessage inbox = null;
        String msg = null;
        state = State.Subscribe;
        live = true;
        sigAction = null;
        droneNumber = -1;
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

                    if (inbox != null && inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        String result = (String) json.getElement(msg, "result");
                        dataAccess.setKey(result);
                        System.out.println("Megatron: Clave recibida " + dataAccess.getKey());
                        System.out.println("Megatron: Cambiando a estado Create");
                        state = State.Create;
                    } else {
                        System.err.print("Megatron ERROR");
                        if (inbox != null) {
                            System.err.print(": " + inbox.getPerformative());
                        }
                        System.err.print("\n");
                        state = State.Cancel;
                    }
                    break;

                // Lanzar x drones de mapeo
                case Create:
                    System.out.println("Megatron------ Estado: Create");

                    try {
                        Decepticon drone = new Falcdron(new AgentID(this.dataAccess.getNameDrone1()),
                                this.getAid(), dataAccess.getKey());

                        this.drones.add(new DataFalcdron(this.myMap));
                        
                        System.out.println("Megatron: Lanzando decepticion 1...");
                        drone.start();
                        
                        drone = new Birdron(new AgentID(this.dataAccess.getNameDrone2()),
                                this.getAid(), dataAccess.getKey());

                        this.drones.add(new DataFalcdron(this.myMap));
                        
                        System.out.println("Megatron: Lanzando decepticion 2...");
                        drone.start();
                        
                        drone = new Flytron(new AgentID(this.dataAccess.getNameDrone3()),
                                this.getAid(), dataAccess.getKey());

                        this.drones.add(new DataFalcdron(this.myMap));

                        System.out.println("Megatron: Lanzando decepticion 3...");
                        drone.start();

                        System.out.println("Megatron: Cambiando a estado Feel");
                        state = State.Feel;

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    break;

                case LaunchRest:
                    System.out.println("Megatron------ Estado: LaunchRest");
                    try {
                        Decepticon drone;

                        System.out.println("Megatron: Lanzando decepticion 2...");
                        drone = new Birdron(new AgentID(this.dataAccess.getNameDrone2()),
                                this.getAid(), dataAccess.getKey());
                        drone.start();

                        System.out.println("Megatron: Lanzando decepticion 3...");
                        drone = new Birdron(new AgentID(this.dataAccess.getNameDrone3()),
                                this.getAid(), dataAccess.getKey());
                        drone.start();

                        System.out.println("Megatron: Lanzando decepticion 4...");
                        drone = new Birdron(new AgentID(this.dataAccess.getNameDrone4()),
                                this.getAid(), dataAccess.getKey());
                        drone.start();

                        this.drones.add(new DataBirdron(this.myMap));
                        this.drones.add(new DataBirdron(this.myMap));
                        this.drones.add(new DataBirdron(this.myMap));

                        System.out.println("Megatron: Cambiando a estado Feel");
                        state = State.Feel;

                    } catch (Exception ex) {
                        Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("Megatron: ERROR al instanciar los drones");
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }

                    break;
                // Esperar x percepciones
                case Feel:
                    System.out.println("Megatron------ Estado: Feel");
                    droneNumber = -1;

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
                        break;
                    }

                    //if(!inbox.getSender().getLocalName().equals("Canis")){
                    System.out.println("Megatron: Recibido de un Decepticon");

                    if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone1())) {
                        droneNumber = 0;
                    } else if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone2())) {
                        droneNumber = 1;
                    } else if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone3())) {
                        droneNumber = 2;
                    } else if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone4())) {
                        droneNumber = 3;
                    }

                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        System.out.println("Megatron: Informe");

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
                        boolean goalFound = (boolean) sensor.contains(3);

                        System.out.println("\tSensor      " + sensor.toString());

                        updateMap(nuevaCordenada, sensor, droneNumber);
                        updateDataDecepticon(droneNumber, nuevaCordenada, battery);
                        energyOfWorld = energy;

                        if (goal) {
                            System.out.println("\tGoal     Si");

                            this.drones.get(droneNumber).setMyGoal(nuevaCordenada);

                            state = State.Heuristic;
                        } else if (goalFound) {
                            if (!zoneGoalFound) {
                                // Es el primer dron que llega, asignar metas al resto
                                // Llamar al método para aparcar

                                int resolution;
                                switch (this.drones.get(droneNumber).getRole()) {
                                    case 0:
                                        resolution = 3;
                                        break;
                                    case 1:
                                        resolution = 5;
                                        break;
                                    default:
                                        resolution = 11;
                                        break;
                                }

                                int mapWidth = 100;
                                if (this.dataAccess.getWorld().equals("newyork")) {
                                    mapWidth = 500;
                                }

                                int index = sensor.indexOf(3);
                                int i = index % resolution;
                                i -= resolution / 2;
                                int j = index / resolution;
                                j -= resolution / 2;

                                Coord coordGoal = nuevaCordenada.add(i, j);
                                if (coordGoal.getX() >= 0 && coordGoal.getY() >= 0
                                        && coordGoal.getX() < mapWidth && coordGoal.getY() < mapWidth) {

                                    zoneGoalFound = true;
                                    System.err.println("Paso " + pasos + "\n\n###################\nD:" + droneNumber + " ha encontrado el objetivo\n###########\n");
                                    System.out.println("Megatron: Cambiando a estado LaunchRest");
                                    state = State.LaunchRest;

                                    nodoGoal = new Node(coordGoal, 3);

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
                            System.err.println("Paso " + pasos + "\n\n###################\nD:" + droneNumber + " En objetivo\n###########\n");

                            // si todos los vivos han llegado
                            //System.out.println("Megatron: Primer decepticon llegó, cancelando...");
                            //state = State.Cancel;
                        }
                    } else {
                        this.drones.get(droneNumber).setDead();
                        System.err.println("Megatron: Cambiando a estado Cancel");
                        state = State.Cancel;
                    }
                    //}

                    break;

                // Heuristica
                case Heuristic:
                    System.out.println("Megatron------ Estado: Heuristic");
                    try {
                        if (fuelH(droneNumber, nodoGoal)) { // Heuristica refuel
                            System.err.println("Megatron: Necesita repostar");
                            Refuel(this.dataAccess.getNameDrone()[droneNumber]);
                            System.err.println("Megatron: Cambiando a estado Feel");
                            state = State.Feel;
                        } else {
                            if (zoneGoalFound) {
                                // Nodo goal ha de ser drones.get(numero).getGoal
                                // su ndo asignado para aterrizar
                                System.err.println("Megatron: Usando la búsqueda desde: ("
                                        + this.drones.get(droneNumber).getPosition().getX() + ","
                                        + this.drones.get(droneNumber).getPosition().getY() + ") hasta ("
                                        + nodoGoal.toString());
                                sigAction = this.drones.get(droneNumber).aStar(this.myMap.getMap().get(this.drones.get(droneNumber).getPosition()), nodoGoal).firstElement();
                            } else {
                                if (droneNumber == 2) {
                                    sigAction = this.drones.get(droneNumber).mapv4();
                                } else {
                                    sigAction = this.drones.get(droneNumber).mapv3();
                                }
                            }

                            System.out.println("Megatron: Dron " + droneNumber + " accion " + sigAction);

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

                    System.out.println("Megatron: Realizando la acción " + sigAction + " en " + this.dataAccess.getNameDrone()[droneNumber]);
                    Move(this.dataAccess.getNameDrone()[droneNumber], sigAction);

                    System.out.println("Megatron: Cambiando a estado Feel");
                    state = State.Feel;

                    break;

                // Cancelar todo para reiniciar
                case Cancel:
                    System.out.println("Megatron------ Estado: Cancel");

                    System.out.println("\n#######\nInforme\n#######");
                    for (DataDecepticon drone : drones) {
                        System.out.print("\n\t" + this.dataAccess.getNameDrone()[droneNumber] + "\t");
                        if (drone.isAlive()) {
                            System.out.print("Vivo");
                        } else {
                            System.out.print("Muerto");
                        }
                        if (drone.isInGoal()) {
                            System.out.print("\ten meta");
                        } else {
                            System.out.print("\tfuera de la meta");
                        }
                    }
                    System.out.println("\n#######\n#######");
                    Cancel();
                    System.out.println("Megatron: Muriendo");
                    live = false;

                    try {
                        this.mapImage.saveToFile();
                    } catch (Exception e) {
                    }

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
    private boolean fuelH(int drone, Node goal) throws Exception {
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
            HashMap<Coord, Node> map = myMap.getMap();
            Node current = new Node(drones.get(drone).getPosition().getX(),
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
