package practica3.megatron;

import java.util.ArrayList;

/**
 * Flytron class (mosca)
 *
 * @author Alexander Straub
 */
public final class DataFlytron extends DataDecepticon {

    private final int visualRange;
    private final int consumation;

    /**
     * Constructor
     *
     * @param map Reference to the map
     * @throws Exception
     * @author Alexander Straub
     */
    public DataFlytron(Map map) throws Exception {
        super(0, map);
        this.visualRange = 3;
        this.consumation = 2;
    }

    /**
     * Returns the visual range of the Decepticon
     *
     * @return Visual range
     * @author Alexander Straub
     */
    @Override
    public final int getVisualRange() {
        return this.visualRange;
    }

    /**
     * Returns the battery consumation per step
     *
     * @return Battery consumation
     * @author Alexander Straub
     */
    @Override
    public final int getConsumation() {
        return this.consumation;
    }
    
    /**
     * Return the border cells in the right order, together with the respective
     * actions.
     *
     * @param position Current position of the drone
     * @param outBorderCells Array to fill with border cells
     * @param outActions Array to fill with actions for the border cells
     * @author Alexander Straub
     */
    @Override
    protected final void mapv3_getBorderCells(Coord position, ArrayList<Node> outBorderCells, ArrayList<Megatron.Action> outActions) {
        outBorderCells.ensureCapacity(8);
        outActions.ensureCapacity(8);

        // Different order of directions, depending on start position
        if (this.getStartPosition().getY() == 0) {
            outBorderCells.add(this.map.getMap().get(position.NE()));
            outActions.add(Megatron.Action.NE);
            outBorderCells.add(this.map.getMap().get(position.NW()));
            outActions.add(Megatron.Action.NW);
            outBorderCells.add(this.map.getMap().get(position.SW()));
            outActions.add(Megatron.Action.SW);
            outBorderCells.add(this.map.getMap().get(position.SE()));
            outActions.add(Megatron.Action.SE);
            
            outBorderCells.add(this.map.getMap().get(position.E()));
            outActions.add(Megatron.Action.E);
            outBorderCells.add(this.map.getMap().get(position.N()));
            outActions.add(Megatron.Action.N);
            outBorderCells.add(this.map.getMap().get(position.W()));
            outActions.add(Megatron.Action.W);
            outBorderCells.add(this.map.getMap().get(position.S()));
            outActions.add(Megatron.Action.S);
        } else {
            outBorderCells.add(this.map.getMap().get(position.SW()));
            outActions.add(Megatron.Action.SW);
            outBorderCells.add(this.map.getMap().get(position.SE()));
            outActions.add(Megatron.Action.SE);
            outBorderCells.add(this.map.getMap().get(position.NE()));
            outActions.add(Megatron.Action.NE);
            outBorderCells.add(this.map.getMap().get(position.NW()));
            outActions.add(Megatron.Action.NW);
            
            outBorderCells.add(this.map.getMap().get(position.W()));
            outActions.add(Megatron.Action.W);
            outBorderCells.add(this.map.getMap().get(position.S()));
            outActions.add(Megatron.Action.S);
            outBorderCells.add(this.map.getMap().get(position.E()));
            outActions.add(Megatron.Action.E);
            outBorderCells.add(this.map.getMap().get(position.N()));
            outActions.add(Megatron.Action.N);
        }
    }
    
    /**
     * Try to cross the map
     * 
     * @return Next action
     * @author Alexander Straub
     */
    @Override
    protected final Megatron.Action mapv4_crossMap() {
        // Only execute once
        if (this.map4_stop) {
            return null;
        }
        
        this.map4_stop = true;
        
        // Get direction
        Megatron.Action direction;
        
        if (this.getPosition().getY() == 0) {
            if (this.getLastAction() == Megatron.Action.E) {
                direction = Megatron.Action.SW;
            } else {
                direction = Megatron.Action.SE;
            }
        } else {
            if (this.getLastAction() == Megatron.Action.E) {
                direction = Megatron.Action.NW;
            } else {
                direction = Megatron.Action.NE;
            }
        }
        
        // Fly over the map
        for (int i = 1; i < this.map.getResolution() - 1; i++) {
            this.map4_pathToUnexploredCell.push(direction);
        }
        return direction;
    }

    /**
     * Tries to find a way to the target position, updating the way lengths
     * 
     * @param start Current position
     * @param target Target position
     * @param keep Keep the top of the stack, returning null
     * @return Next action for the decepticon
     * @author Alexander Straub
     */
    @Override
    public final Megatron.Action findWay(Coord start, Coord target, boolean keep) {
        if (this.findWay_pathToTarget.isEmpty()) {
            // Update path lengths
            this.findWay_wayLength = Math.max(Math.abs(start.getX() - target.getX()), Math.abs(start.getY() - target.getY()));
            this.findWay_minWayLength = this.findWay_wayLength;

            // Go diagonal
            int diagonal = Math.min(Math.abs(start.getX() - target.getX()), Math.abs(start.getY() - target.getY()));        
            Megatron.Action action;
            Coord current = start;

            if (start.getX() < target.getX()) {
                if (start.getY() < target.getY()) {
                    action = Megatron.Action.SE;
                } else {
                    action = Megatron.Action.NE;
                }
            } else {
                if (start.getY() < target.getY()) {
                    action = Megatron.Action.SW;
                } else {
                    action = Megatron.Action.NW;
                }
            }
            
            for (int i = 0; i < diagonal; i++) {
                this.findWay_pathToTarget.add(action);
                current = current.neighbour(action);
            }

            // Go straight to the target
            if (current.getX() < target.getX()) {
                action = Megatron.Action.E;
            } else if (current.getX() > target.getX()) {
                action = Megatron.Action.W;
            } else if (current.getY() < target.getY()) {
                action = Megatron.Action.S;
            } else {
                action = Megatron.Action.N;
            }
            
            for (int i = 0; i < this.findWay_wayLength - diagonal; i++) {
                this.findWay_pathToTarget.add(action);
                current = current.neighbour(action);
            }
            
            // Check if the target has the same coordinates as the final destination
            if (!target.equals(current)) {
                System.err.println(this.name + ": Find way algorithm failed");
                return null;
            }
        }
        
        // Return next action
        if (keep) {
            return null;
        }
        return this.findWay_pathToTarget.pop();
    }
    
}
