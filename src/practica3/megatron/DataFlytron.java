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

//    /**
//     * Return the border cells in the right order, together with the respective
//     * actions.
//     *
//     * @param position Current position of the drone
//     * @param borderCells Array to fill with border cells
//     * @param actions Array to fill with actions for the border cells
//     * @author Alexander Straub
//     */
//    @Override
//    protected final void mapv3_getBorderCells(Coord position, ArrayList<Node> borderCells, ArrayList<Megatron.Action> actions) {
//        borderCells.ensureCapacity(8);
//        actions.ensureCapacity(8);
//
//        // Different order of directions, depending on start position
//        if (this.getStartPosition().getY() == 0) {
//            borderCells.add(this.map.getMap().get(position.E()));
//            actions.add(Megatron.Action.E);
//            borderCells.add(this.map.getMap().get(position.N()));
//            actions.add(Megatron.Action.N);
//            borderCells.add(this.map.getMap().get(position.W()));
//            actions.add(Megatron.Action.W);
//            borderCells.add(this.map.getMap().get(position.S()));
//            actions.add(Megatron.Action.S);
//
//            borderCells.add(this.map.getMap().get(position.NE()));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.NW()));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.SW()));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.SE()));
//            actions.add(Megatron.Action.SE);
//        } else {
//            borderCells.add(this.map.getMap().get(position.W()));
//            actions.add(Megatron.Action.W);
//            borderCells.add(this.map.getMap().get(position.S()));
//            actions.add(Megatron.Action.S);
//            borderCells.add(this.map.getMap().get(position.E()));
//            actions.add(Megatron.Action.E);
//            borderCells.add(this.map.getMap().get(position.N()));
//            actions.add(Megatron.Action.N);
//
//            borderCells.add(this.map.getMap().get(position.SW()));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.SE()));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.NE()));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.NW()));
//            actions.add(Megatron.Action.NW);
//        }
//    }
    
    /**
     * Return the border cells in the right order, together with the respective
     * actions.
     *
     * @param position Current position of the drone
     * @param borderCells Array to fill with border cells
     * @param actions Array to fill with actions for the border cells
     * @author Alexander Straub
     */
    @Override
    protected final void mapv3_getBorderCells(Coord position, ArrayList<Node> borderCells, ArrayList<Megatron.Action> actions) {
        borderCells.ensureCapacity(8);
        actions.ensureCapacity(8);

        // Different order of directions, depending on start position
        if (this.getStartPosition().getY() == 0) {
            borderCells.add(this.map.getMap().get(position.NE()));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.NW()));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.SW()));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.SE()));
            actions.add(Megatron.Action.SE);
            
            borderCells.add(this.map.getMap().get(position.E()));
            actions.add(Megatron.Action.E);
            borderCells.add(this.map.getMap().get(position.N()));
            actions.add(Megatron.Action.N);
            borderCells.add(this.map.getMap().get(position.W()));
            actions.add(Megatron.Action.W);
            borderCells.add(this.map.getMap().get(position.S()));
            actions.add(Megatron.Action.S);
        } else {
            borderCells.add(this.map.getMap().get(position.SW()));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.SE()));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.NE()));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.NW()));
            actions.add(Megatron.Action.NW);
            
            borderCells.add(this.map.getMap().get(position.W()));
            actions.add(Megatron.Action.W);
            borderCells.add(this.map.getMap().get(position.S()));
            actions.add(Megatron.Action.S);
            borderCells.add(this.map.getMap().get(position.E()));
            actions.add(Megatron.Action.E);
            borderCells.add(this.map.getMap().get(position.N()));
            actions.add(Megatron.Action.N);
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

}
