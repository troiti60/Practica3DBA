package practica3.megatron;

import practica3.drones.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import practica3.DataAccess;
import practica3.JsonDBA;
import practica3.Draw.MapImage;
import practica3.Draw.Window;

/**
 * Class that controls the rest of Decepticons
 *
 * @author Javier Ortega Rodriguez
 */
public class Megatron extends SingleAgent {

    private final ArrayList<DataDecepticon> drones;
    private Map myMap;
    private final JsonDBA json;
    private DataAccess dataAccess;
    private Window draw;
    private State state;
    private boolean live;
    private Action sigAction;
    private int droneNumber;
    private double PENALTY; // read-only multiplicative value used to estimate the number of steps
                            //necessary to go from A to B on an unknown environment
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
        this.PENALTY = 1.0;
        drones = new ArrayList<>(4);
        this.json = new JsonDBA();
    }

    /**
     * Init function for agents
     *
     * @author Alexander Straub
     */
    @Override
    protected void init() {
        this.dataAccess = DataAccess.createInstance();

        int resolution = 100;
        if (this.dataAccess.getWorld().equals("newyork")) {
            resolution = 500;
        }
        this.myMap = new Map(resolution);

        this.draw = new Window();
        this.draw.setResizable(true);
        this.draw.setVisible(true);

        this.mapImage = new MapImage(resolution);
    }

    /**
     * This method is thought to be called by Megatron itself after parsing a
     * new perception of one of his drones so he can update the map
     *
     * @param pos Current drone position
     * @param perception Last perception received by the decepticon
     * @param drone Drone ID
     * @param woldEnergy Energy level of the world
     * @author Antonio Troitiño del Río
     */
    private void updateMapAndDrone(Coord pos, ArrayList<Integer> perception, int drone, int battery, int worldEnergy) {
        if (perception.isEmpty() || drone >= this.drones.size()) {
            System.err.println("Megatron: Received an empty perception");
        } else {
            // Set new drone position and battery level
            this.drones.get(drone).setPosition(pos);
            this.drones.get(drone).setFuel(battery);
            this.mapImage.setDronePosition(drone, pos);

            // Add perceived nodes to the map
            int cont = Math.round((float) Math.sqrt(perception.size()));
            cont = (cont - 1) / 2;
            int count = 0;
            for (int i = 0 - cont; i <= cont; i++) {
                for (int j = 0 - cont; j <= cont; j++) {
                    this.myMap.addNode(new Coord(pos.getX() + j, pos.getY() + i), perception.get(count));
                    this.mapImage.setCell(perception.get(count), new Coord(pos.getX() + j, pos.getY() + i));
                    count++;
                }
            }

            // Draw new map and positions
            this.draw.getJpanel().setDronPosition(pos);
            this.draw.getJpanel().updateDraw(this.myMap, drone, this.drones.get(drone).getLastPosition());
            this.draw.setLabelCoordinate(pos.getX(), pos.getY(), drone);
            this.draw.setBatteryDroneValue(drone, this.drones.get(drone).getFuel());
            this.draw.setTotalBatteryValue(worldEnergy);

            System.out.println("Megatron: Map updated");
        }
    }

    /**
     * Send the message to subscribe to the world
     *
     * @author José Carlos Alfaro
     */
    public void subscribe() {
        ACLMessage outbox = new ACLMessage(ACLMessage.SUBSCRIBE);
        outbox.setReceiver(new AgentID(this.dataAccess.getVirtualHost()));
        outbox.setSender(getAid());
        outbox.setContent(this.json.createJson("world", this.dataAccess.getWorld()));
        this.send(outbox);

        System.out.println("Megatron: Subscription request " + outbox.getContent());
    }

    /**
     * Send the message to cancel the session
     *
     * @author José Carlos Alfaro
     */
    public void cancel() {
        ACLMessage outbox = new ACLMessage(ACLMessage.CANCEL);
        outbox.setReceiver(new AgentID(this.dataAccess.getVirtualHost()));
        outbox.setSender(getAid());
        outbox.setContent(this.dataAccess.getKey());
        this.send(outbox);

        System.out.println("Megatron: Cancel sent to server");
    }

    /**
     * It will send the order to move depending the action.
     *
     * @param nameDrone Agent's name who will receive the message
     * @param action Direction of movement.
     * @author José Carlos Alfaro
     */
    public void move(String nameDrone, Action action) {
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", action.toString());
        hm.put("key", this.dataAccess.getKey());
        String msg = this.json.createJson(hm);

        ACLMessage outbox = new ACLMessage(ACLMessage.REQUEST);
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(nameDrone));
        outbox.setContent(msg);
        this.send(outbox);
    }

    /**
     * It will send the order to refuel
     *
     * @param nameDrone Agent's name who will receive the message
     * @author José Carlos Alfaro
     */
    public void refuel(String nameDrone) {
        LinkedHashMap<String, String> hm = new LinkedHashMap<>();
        hm.put("command", "refuel");
        hm.put("key", this.dataAccess.getKey());
        String msg = this.json.createJson(hm);

        ACLMessage outbox = new ACLMessage(ACLMessage.REQUEST);
        outbox.setSender(getAid());
        outbox.setReceiver(new AgentID(nameDrone));
        outbox.setContent(msg);
        this.send(outbox);
    }

    /**
     * Main method in form of a state machine
     *
     * @author All
     */
    @Override
    public void execute() {
        // Objects for communication
        ACLMessage inbox;
        String msg;

        // Objects for the state
        State state = State.Subscribe;
        boolean alive = true;
        Action nextAction = null;
        int droneNumber = -1;
        int worldEnergy = 0;
        boolean firstSearch = true;

        System.out.println("Megatron: Initiating");

        // While there are drones alive, go on
        while (alive) {
            // If all drones have arrived or are dead, go to cancel
            if (!this.drones.isEmpty()) {
                boolean stop = true;
                for (DataDecepticon drone : this.drones) {
                    stop &= drone.isInGoal() || !drone.isAlive();
                }

                if (stop) {
                    state = State.Cancel;
                }
            }

            // State machine
            switch (state) {
                // Suscribe to server
                case Subscribe:
                    System.out.println("Megatron------ State: Subscribe");
                    subscribe();

                    try {
                        inbox = receiveACLMessage();
                        msg = inbox.getContent();
                    } catch (InterruptedException ex) {
                        System.err.println("Megatron: Problem receiving answer after subscribing");
                        System.err.println("\t" + ex.getMessage());

                        state = State.Cancel;
                        break;
                    }

                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        String result = (String) json.getElement(msg, "result");
                        this.dataAccess.setKey(result);

                        System.out.println("Megatron: Key received " + this.dataAccess.getKey());

                        state = State.Create;
                        break;
                    } else {
                        System.err.println("Megatron: Server denied subscription");
                        System.err.println("\t" + inbox.getPerformative());

                        state = State.Cancel;
                        break;
                    }

                // Create drones to find the objective
                case Create:
                    System.out.println("Megatron------ State: Create");

                    try {
                        Decepticon drone;

                        System.out.println("Megatron: Launching decepticon 1: " + this.dataAccess.getNameDrone1());
                        drone = new Birdron(new AgentID(this.dataAccess.getNameDrone1()),
                                this.getAid(), this.dataAccess.getKey());
                        drone.start();
                        this.drones.add(new DataBirdron(this.myMap));

                        System.out.println("Megatron: Launching decepticon 2: " + this.dataAccess.getNameDrone2());
                        drone = new Birdron(new AgentID(this.dataAccess.getNameDrone2()),
                                this.getAid(), this.dataAccess.getKey());
                        drone.start();
                        this.drones.add(new DataBirdron(this.myMap));

                        System.out.println("Megatron: Launching decepticon 3: " + this.dataAccess.getNameDrone3());
                        drone = new Flytron(new AgentID(this.dataAccess.getNameDrone3()),
                                this.getAid(), this.dataAccess.getKey());
                        drone.start();
                        this.drones.add(new DataFlytron(this.myMap));

                        System.out.println("Megatron: Launching decepticon 4: " + this.dataAccess.getNameDrone4());
                        drone = new Falcdron(new AgentID(this.dataAccess.getNameDrone4()),
                                this.getAid(), this.dataAccess.getKey());
                        drone.start();
                        this.drones.add(new DataFalcdron(this.myMap));

                        state = State.Feel;
                        break;
                    } catch (Exception ex) {
                        System.err.println("Megatron: Failed to instantiate and start drones");
                        System.err.println("\t" + ex.getMessage());

                        state = State.Cancel;
                        break;
                    }

                // In case not every drone has yet been launched, launch the rest
                case LaunchRest:
                    System.out.println("Megatron------ State: LaunchRest");

                    try {
                        Decepticon drone;

                        if (this.drones.size() == 1) {
                            System.out.println("Megatron: Launching decepticon 2: " + this.dataAccess.getNameDrone2());
                            drone = new Birdron(new AgentID(this.dataAccess.getNameDrone2()),
                                    this.getAid(), this.dataAccess.getKey());
                            drone.start();
                            this.drones.add(new DataBirdron(this.myMap));
                        }

                        if (this.drones.size() == 2) {
                            System.out.println("Megatron: Launching decepticon 3: " + this.dataAccess.getNameDrone3());
                            drone = new Birdron(new AgentID(this.dataAccess.getNameDrone3()),
                                    this.getAid(), this.dataAccess.getKey());
                            drone.start();
                            this.drones.add(new DataBirdron(this.myMap));
                        }

                        if (this.drones.size() == 3) {
                            System.out.println("Megatron: Launching decepticon 4: " + this.dataAccess.getNameDrone4());
                            drone = new Birdron(new AgentID(this.dataAccess.getNameDrone4()),
                                    this.getAid(), this.dataAccess.getKey());
                            drone.start();
                            this.drones.add(new DataBirdron(this.myMap));
                        }

                        state = State.Heuristic;
                        break;
                    } catch (Exception ex) {
                        System.err.println("Megatron: Failed to instantiate and start drones");
                        System.err.println("\t" + ex.getMessage());

                        state = State.Cancel;
                        break;
                    }

                // Receive update on drone sensors
                case Feel:
                    System.out.println("Megatron------ State: Feel");

                    droneNumber = -1;

                    // If drones are on standby but all other drones are
                    // incapacitated, reactivate one of them
                    boolean reactivate = true;
                    for (int i = 0; i < 4 && reactivate; i++) {
                        reactivate &= this.drones.get(i).isOnStandby()
                                || !this.drones.get(i).isAlive();
                    }

                    for (int i = 0; i < 4 && reactivate; i++) {
                        if (this.drones.get(i).isOnStandby()) {
                            this.drones.get(i).reactivate();
                            reactivate = false;
                            state = State.Heuristic;
                            droneNumber = i;
                        }
                    }

                    if (state == State.Heuristic) {
                        break;
                    }

                    // Wait for message from drones
                    try {
                        System.out.println("Megatron: Waiting for message");
                        inbox = receiveACLMessage();
                        System.out.println("Megatron: Message received from " + inbox.getSender().toString());
                        System.out.println("\t" + inbox.getSender().getLocalName() + " " + inbox.getPerformative() + " " + inbox.getContent());
                    } catch (InterruptedException ex) {
                        System.err.println("Megatron: Error receiving message from drone");
                        System.err.println("\t" + ex.getMessage());

                        state = State.Cancel;
                        break;
                    }

                    // Get drone number from the sender's name
                    if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone1())) {
                        droneNumber = 0;
                    } else if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone2())) {
                        droneNumber = 1;
                    } else if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone3())) {
                        droneNumber = 2;
                    } else if (inbox.getSender().getLocalName().equals(this.dataAccess.getNameDrone4())) {
                        droneNumber = 3;
                    }

                    // If it is a normal message
                    if (inbox.getPerformativeInt() == ACLMessage.INFORM) {
                        String result = this.json.getElement(inbox.getContent(), "result").toString();

                        // Get battery status
                        int battery = this.json.getElementInteger(result, "battery");
                        System.out.println("\tBattery level: " + battery);

                        // Get position
                        int x = this.json.getElementInteger(result, "x");
                        int y = this.json.getElementInteger(result, "y");
                        Coord newCoordinates = new Coord(x, y);
                        System.out.println("\tDrone position: " + "(" + x + " , " + y + ")");

                        // Get sensor data
                        ArrayList<Integer> sensor = this.json.jsonElementToArrayInt(this.json.getElement(result, "sensor"));
                        System.out.println("\tSensor: " + sensor.toString());

                        // Get world energy
                        int energy = this.json.getElementInteger(result, "energy");
                        System.out.println("\tWorld energy: " + energy);

                        // Save if goal was found and drone is in goal area
                        boolean goal = (boolean) this.json.getElement(result, "goal");
                        boolean goalFound = (boolean) sensor.contains(3);

                        // Update map and information of the drone
                        updateMapAndDrone(newCoordinates, sensor, droneNumber, battery, energy);
                        worldEnergy = energy;
                        
                        // If goal was found for the first time
                        if (goalFound && !this.zoneGoalFound) {
                            // If this is the first time the goal area was found
                            this.zoneGoalFound = true;

                            // Get parking spot for all the drones
                            parking(droneNumber);

                            System.out.println("Megatron: Drone " + inbox.getSender().getLocalName() + " has found the objective");

                            state = State.LaunchRest;
                            if (!goal) {
                                break;
                            }
                        }
                        
                        // If drone is in the goal area
                        if (goal) {
                            System.out.println("Megatron: Drone " + inbox.getSender().getLocalName() + " has arrived");
                            this.drones.get(droneNumber).setMyGoal(newCoordinates);
                            this.myMap.setDroneParkingSpace(newCoordinates);
                            
                            if (state != State.LaunchRest) {
                                state = State.Feel;
                            }
                            break;
                        }

                        state = State.Heuristic;
                        break;
                    } else {
                        // Drone died
                        this.drones.get(droneNumber).setDead();

                        // If all drones are dead, go to cancel
                        if (!this.drones.get(0).isAlive() && !this.drones.get(1).isAlive()
                                && !this.drones.get(2).isAlive() && !this.drones.get(3).isAlive()) {

                            state = State.Cancel;
                            break;
                        } else {
                            state = State.Feel;
                            break;
                        }
                    }

                // Heuristic: Find objective, find way to target, refuel
                case Heuristic:
                    System.out.println("Megatron------ State: Heuristic");

                    try {
                        // Check if drone needs to refuel
                        if (fuelHeuristic(droneNumber, this.drones.get(droneNumber).getMyGoal())) {
                            System.out.println("Megatron: Drone " + this.dataAccess.getNameDrone()[droneNumber] + " needs refueling");
                            refuel(this.dataAccess.getNameDrone()[droneNumber]);

                            state = State.Feel;
                            break;
                        } else {
                            // Decide between searching for the goal area or finding a way
                            if (this.zoneGoalFound) {
                                System.out.println("Megatron: Using search algorithm to get from "
                                        + this.drones.get(droneNumber).getPosition().toString()
                                        + " to " + this.drones.get(droneNumber).getMyGoal().toString());

                                // Ask for next action
                                nextAction = this.drones.get(droneNumber).findWay(this.drones.get(droneNumber).getPosition(), this.drones.get(droneNumber).getMyGoal());
                                
                                // Start heuristic to decide who goes to the target
                                if (firstSearch) {
                                    firstSearch = false;
                                    
                                    System.out.println("Megatron: Starting heuristic to decide who goes and who dies");
                                    if (!deathHeuristic(droneNumber, worldEnergy)) {
                                        nextAction = null;
                                    }
                                }
                                
                                // TODO: New heuristic if drones from heuristic before
                                // arrived but there are still drones alive with a chance to get there
                                
                            } else {
                                // Let Flytron use another method of searching
                                if (this.drones.get(droneNumber).getRole() == 0) {
                                    nextAction = this.drones.get(droneNumber).mapv4();
                                } else {
                                    nextAction = this.drones.get(droneNumber).mapv3();
                                }
                            }

                            System.out.println("Megatron: Action " + nextAction + " chosen for drone " + this.dataAccess.getNameDrone()[droneNumber]);

                            state = State.Action;
                            break;
                        }
                    } catch (Exception ex) {
                        System.err.println("Megatron: Error occured in the heuristics");
                        System.err.println("\t" + ex.getMessage());

                        state = State.Cancel;
                        break;
                    }

                // Execute the action
                case Action:
                    System.out.println("Megatron------ State: Action");

                    if (nextAction == null) {
                        System.out.println("Megatron: No action executed for " + this.dataAccess.getNameDrone()[droneNumber]);
                    } else {
                        System.out.println("Megatron: Drone " + this.dataAccess.getNameDrone()[droneNumber] + " executing action " + nextAction);
                        move(this.dataAccess.getNameDrone()[droneNumber], nextAction);
                    }

                    state = State.Feel;
                    break;

                // Cancel if drones are dead or arrived
                case Cancel:
                    System.out.println("Megatron------ State: Cancel");

                    System.out.println("\n####### Inform #######\n");
                    for (int i = 0; i < this.drones.size(); i++) {
                        System.out.print("\t" + this.dataAccess.getNameDrone()[i] + "\t");

                        if (this.drones.get(i).isAlive()) {
                            System.out.print("Alive");
                        } else {
                            System.out.print("Dead");
                        }

                        if (this.drones.get(i).isInGoal()) {
                            System.out.println("\tArrived");
                        } else {
                            System.out.println("\tNot arrived");
                        }
                    }
                    System.out.println("\n######################\n");

                    cancel();

                    System.out.println("Megatron: Dying");
                    alive = false;

                    try {
                        this.mapImage.saveToFile();
                    } catch (Exception e) {
                    }

                    break;
            }
        }

        System.out.println("Megatron: Dead");
    }

    /**
     * Fuel heuristic
     *
     * @param drone Drone ID
     * @param goal Goal node
     * @return True if drone has to refuel
     * @author Daniel Sánchez Alcaide
     */
    private boolean fuelHeuristic(int drone, Coord goal) throws Exception {
        boolean res = false;
        int consume = this.drones.get(drone).getConsumation();

        if (!this.zoneGoalFound && this.drones.get(drone).getFuel() <= consume) {
            res = true;
        } else if (this.zoneGoalFound) {
            Node current = this.myMap.getAccessibleMap().get(this.drones.get(drone).getPosition());

            // TODO: Better heuristic for this case
            
            if (this.drones.get(drone).getFuel() <= consume) {
                res = true;
            }
        }

        return res;
    }
    
    /**
     * Heuristic to decide who goes and who has to die
     * 
     * @param droneNumber Drone ID
     * @param worldFuel Amount of fuel left
     * @return Indicator for the current drone: false-don't move
     * @author Alexander Straub
     */
    private boolean deathHeuristic(int droneNumber, int worldFuel) {
        // Execute find way algorithm for all drones
        int wayLength = 0, minWayLength = 0;
        
        for (int i = 0; i < 4; i++) {
            if (droneNumber != i) {
                this.drones.get(i).findWay(this.drones.get(i).getPosition(), 
                        this.drones.get(i).getMyGoal(), true);
            }
            
            // Get actual path length and optimal path length
            if (this.drones.get(i).getRole() != 0 && this.drones.get(i).findWay_getWayLength() != 0) {
                wayLength += this.drones.get(i).findWay_getWayLength();
                minWayLength += this.drones.get(i).findWay_getMinWayLength();
            }
        }
        
        // Calculate ratio (or guess)
        double ratio;
        
        if (wayLength == 0) {
            ratio = Math.sqrt(2.0);
        } else {
            ratio = (double)wayLength / (double)minWayLength;
        }
        
        // Get path costs assumed for drones
        int[] costs = new int[4];
        
        for (int i = 0; i < 4; i++) {
            if (this.drones.get(i).findWay_getWayLength() != 0) {
                costs[i] = this.drones.get(i).findWay_getWayLength() * this.drones.get(i).getConsumation();
            } else {
                costs[i] = (int)(this.drones.get(i).findWay_getMinWayLength() * ratio) * this.drones.get(i).getConsumation();
            }
        }
        
        // Substract local battery from path costs
        for (int i = 0; i < 4; i++) {
            costs[i] -= this.drones.get(i).getFuel();
        }
        
        // For the costs we have to consider that the drone is not at full fuel
        // so a recharge will at least restore 100 - current fuel
        for (int i = 0; i < 4; i++) {
            if (costs[i] > this.drones.get(i).getFuel() && costs[i] < 100) {
                costs[i] = 100;
            }
        }
        
        // Kill drones who will not be able to reach the target (absolutely sure)
        for (int i = 0; i < 4; i++) {
            if (this.drones.get(i).findWay_getMinWayLength() * this.drones.get(i).getConsumation() 
                    > this.drones.get(i).getFuel() + worldFuel) {
                this.drones.get(i).setDead();
            }
        }
        
        // Sort drones: first the ones needing less fewel
        int[] indices = new int[4];
        
        for (int i = 0; i < 4; i++) {
            indices[i] = i;
        }
        
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (costs[i] > costs[j]) {
                    int temp = indices[j];
                    indices[j] = indices[i];
                    indices[i] = temp;
                }
            }
        }
        
        // Fill until world fuel reached
        int fuelNeeded = 0;
        
        for (int i = 0; i < 4; i++) {
            fuelNeeded += Math.max(0, costs[indices[i]]);
            
            if (fuelNeeded > worldFuel) {
                this.drones.get(indices[i]).setStandby();
            }
        }
        
        return !this.drones.get(droneNumber).isOnStandby();
    }

    /**
     * Coord goal assignment
     *
     * @param drone Drone that found the target zone
     * @author Jesús Cobo Sánchez, Alexander Straub
     */
    public void parking(int drone) {
        ArrayList<Node> targetNodes = new ArrayList<>(4);
        Collection<Node> localMap = this.myMap.getAccessibleMap().values();

        // Extract target nodes from map
        for (Node node : localMap) {
            if (node.getRadar() == 3) {
                targetNodes.add(node);
            }
        }

        // If not enough nodes have yet been explored, create more
        if (targetNodes.size() < 4) {
            if (targetNodes.size() == 1) {
                if (targetNodes.get(0).getCoord().getY() < this.drones.get(drone).getPosition().getY()) {
                    if (targetNodes.get(0).getCoord().getX() < this.drones.get(drone).getPosition().getX()) {
                        targetNodes.add(new Node(targetNodes.get(0).N(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).W(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).NW(), 3));
                    } else {
                        targetNodes.add(new Node(targetNodes.get(0).N(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).E(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).NE(), 3));
                    }
                } else {
                    if (targetNodes.get(0).getCoord().getX() < this.drones.get(drone).getPosition().getX()) {
                        targetNodes.add(new Node(targetNodes.get(0).S(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).W(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).SW(), 3));
                    } else {
                        targetNodes.add(new Node(targetNodes.get(0).S(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).E(), 3));
                        targetNodes.add(new Node(targetNodes.get(0).SE(), 3));
                    }
                }
            } else if (targetNodes.size() == 2) {
                // Find target that is not diagonal to the current position
                Node targetStraight;
                
                if (targetNodes.get(0).getCoord().getY() != this.drones.get(drone).getPosition().getY() && 
                        targetNodes.get(0).getCoord().getX() != this.drones.get(drone).getPosition().getX()) {
                    
                    targetStraight = targetNodes.get(1);
                } else {
                    targetStraight = targetNodes.get(0);
                }
                
                // If the straight target is to the north, add the north of both targets
                // Do so analogously for the other directions
                if (targetStraight.getY() < this.drones.get(drone).getPosition().getY()) {
                    targetNodes.add(new Node(targetNodes.get(0).N(), 3));
                    targetNodes.add(new Node(targetNodes.get(1).N(), 3));
                } else if (targetStraight.getY() > this.drones.get(drone).getPosition().getY()) {
                    targetNodes.add(new Node(targetNodes.get(0).S(), 3));
                    targetNodes.add(new Node(targetNodes.get(1).S(), 3));
                } else if (targetStraight.getX() < this.drones.get(drone).getPosition().getX()) {
                    targetNodes.add(new Node(targetNodes.get(0).W(), 3));
                    targetNodes.add(new Node(targetNodes.get(1).W(), 3));
                } else {
                    targetNodes.add(new Node(targetNodes.get(0).E(), 3));
                    targetNodes.add(new Node(targetNodes.get(1).E(), 3));
                }
            } else {
                // Find target that is not diagonal to the current position
                Node targetStraight = targetNodes.get(0);
                
                for (Node targetNode : targetNodes) {
                    if (targetNode.getCoord().getY() == this.drones.get(drone).getPosition().getY() ||
                            targetNode.getCoord().getX() == this.drones.get(drone).getPosition().getX()) {
                        
                        targetStraight = targetNode;
                    }
                }
                
                // If the straight target is to the north, add its north
                // Do so analogously for the other directions
                if (targetStraight.getY() < this.drones.get(drone).getPosition().getY()) {
                    targetNodes.add(new Node(targetStraight.N(), 3));
                } else if (targetStraight.getY() > this.drones.get(drone).getPosition().getY()) {
                    targetNodes.add(new Node(targetStraight.S(), 3));
                } else if (targetStraight.getX() < this.drones.get(drone).getPosition().getX()) {
                    targetNodes.add(new Node(targetStraight.W(), 3));
                } else {
                    targetNodes.add(new Node(targetStraight.E(), 3));
                }
            }

            // Add new found nodes to map
            for (Node node : targetNodes) {
                this.myMap.addNode(node);
            }
        }

        // Set goal nodes for drones
        for (int i = 0; i < 4; i++) {
            this.drones.get(i).setMyGoal(targetNodes.get(i).getCoord());
        }
    }
    /**
     * Finds the node where a drone will be after following a plan of actions
     * @param start position where the drone is
     * @param path of actions the drone will follow
     * @returns node the end of the road
     * @author Antonio Troitiño del Río
     */
    private Node endOfPath(Coord start, Stack<Action> path){
        HashMap<Coord, Node> localMap = myMap.getMap();
        Coord end=start;
        while(!path.isEmpty()){
            Action step = path.pop();
            if(step==Action.N)
                end=end.N();
            else if (step==Action.NE)
                end=end.NE();
            else if (step==Action.E)
                end=end.E();
            else if (step==Action.SE)
                end=end.SE();
            else if (step==Action.S)
                end=end.S();
            else if (step==Action.SW)
                end=end.SW();
            else if (step==Action.W)
                end=end.W();
            else if (step==Action.NW)
                end=end.NW();
        }
        Node solution=null;
        if(localMap.containsKey(end))
           solution=localMap.get(end);
        else System.err.println("Node given by aStar is not contained at Map");
        return solution;
    }
    
    /** Estimates the number of steps to go from A to B in an unknown map
     * @param A Coordinates of the beginning point
     * @param B Coordinates of the ending
     * @return Estimated number of steps betwen those points
     * @author Antonio Troitiño del Río
     */
    private int stepsEstimation(Coord A,Coord B){
        int xa=A.getX(),xb=B.getX(),ya=A.getY(),yb=B.getY();
        int res;
        if(Math.abs(xa-xb)<Math.abs(ya-yb))
            res=Math.abs(ya-yb);
        else res=Math.abs(xa-xb);
        res=(int) Math.ceil(res*PENALTY);
        return res;
    
    
    }
    
    /** Estimates energy consumption for a drone to reach the goal
     * @param drone whose consumption has to be estimated
     * @return estimated value for that drone. 
     * (Integer.MAX_VALUE if drone is already on target)
     * @author Antonio Troitiño del Río
     */
    private int consummationToGoal( DataDecepticon drone){
        HashMap<Coord, Node> localMap = myMap.getMap();
        if(localMap.get(drone.getPosition()).getRadar()==3)
            return Integer.MAX_VALUE;
        else {
            int steps=0;
            try {
                Stack<Action> toGoal=drone.aStar(localMap.get(drone.getPosition()), localMap.get(drone.getMyGoal()));
                steps+=toGoal.size();
                Node fin = endOfPath(drone.getPosition(),toGoal);
                if (fin.getRadar()==3) return steps*drone.getConsumation();
                toGoal=drone.aStar(localMap.get(drone.getMyGoal()), localMap.get(drone.getPosition()));
                steps+=toGoal.size();
                Node fin2 = endOfPath(drone.getMyGoal(),toGoal);
                steps+=stepsEstimation(fin.getCoord(),fin2.getCoord());
            } catch (Exception ex) {
                Logger.getLogger(Megatron.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Error while receiving aStar actions stack");
            }
               
            return steps*drone.getConsumation();
        }
    }
    
    /**
     * Decides next drone to reach the goal
     * @return position of chosen drone in the "drones" array
     * @author Antonio Troitiño del Río
     */
    private int nextDrone(){
        ArrayList<Integer> values = new ArrayList();
        for(int i=0;i<4;i++) values.add(consummationToGoal(drones.get(i)));
        int min = values.get(0);
        for(int i=1;i<4;i++) if(values.get(i)<values.get(min)) min=i;
        return min;
    }
}
