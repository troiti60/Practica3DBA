package practica3.megatron;

import java.util.ArrayList;

/**
 * Falcdron class (halcón)
 *
 * @author Alexander Straub
 */
public final class DataFalcdron extends DataDecepticon {

    private final int visualRange;
    private final int consumation;

    /**
     * Constructor
     *
     * @param map Reference to the map
     * @throws Exception
     * @author Alexander Straub
     */
    public DataFalcdron(Map map) throws Exception {
        super(2, map);
        this.visualRange = 11;
        this.consumation = 4;
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
//        borderCells.ensureCapacity(40);
//        actions.ensureCapacity(40);
//
//        // Different order of directions, depending on start position
//        if (this.getStartPosition().getY() == 0) {
//            borderCells.add(this.map.getMap().get(position.addX(5)));
//            actions.add(Megatron.Action.E);
//            borderCells.add(this.map.getMap().get(position.addY(-5)));
//            actions.add(Megatron.Action.N);
//            borderCells.add(this.map.getMap().get(position.addX(-5)));
//            actions.add(Megatron.Action.W);
//            borderCells.add(this.map.getMap().get(position.addY(5)));
//            actions.add(Megatron.Action.S);
//
//            borderCells.add(this.map.getMap().get(position.add(5, -1)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -2)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -3)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -4)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(4, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(3, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(2, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(1, -5)));
//            actions.add(Megatron.Action.NE);
//
//            borderCells.add(this.map.getMap().get(position.add(-1, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-2, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-3, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-4, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -4)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -3)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -2)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -1)));
//            actions.add(Megatron.Action.NW);
//
//            borderCells.add(this.map.getMap().get(position.add(-5, 1)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 2)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 3)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 4)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-4, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-3, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-2, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-1, 5)));
//            actions.add(Megatron.Action.SW);
//
//            borderCells.add(this.map.getMap().get(position.add(1, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(2, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(3, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(4, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 4)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 3)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 2)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 1)));
//            actions.add(Megatron.Action.SE);
//        } else {
//            borderCells.add(this.map.getMap().get(position.addX(-5)));
//            actions.add(Megatron.Action.W);
//            borderCells.add(this.map.getMap().get(position.addY(5)));
//            actions.add(Megatron.Action.S);
//            borderCells.add(this.map.getMap().get(position.addX(5)));
//            actions.add(Megatron.Action.E);
//            borderCells.add(this.map.getMap().get(position.addY(-5)));
//            actions.add(Megatron.Action.N);
//
//            borderCells.add(this.map.getMap().get(position.add(-5, 1)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 2)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 3)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 4)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-5, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-4, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-3, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-2, 5)));
//            actions.add(Megatron.Action.SW);
//            borderCells.add(this.map.getMap().get(position.add(-1, 5)));
//            actions.add(Megatron.Action.SW);
//
//            borderCells.add(this.map.getMap().get(position.add(1, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(2, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(3, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(4, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 5)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 4)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 3)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 2)));
//            actions.add(Megatron.Action.SE);
//            borderCells.add(this.map.getMap().get(position.add(5, 1)));
//            actions.add(Megatron.Action.SE);
//
//            borderCells.add(this.map.getMap().get(position.add(5, -1)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -2)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -3)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -4)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(5, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(4, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(3, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(2, -5)));
//            actions.add(Megatron.Action.NE);
//            borderCells.add(this.map.getMap().get(position.add(1, -5)));
//            actions.add(Megatron.Action.NE);
//
//            borderCells.add(this.map.getMap().get(position.add(-1, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-2, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-3, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-4, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -5)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -4)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -3)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -2)));
//            actions.add(Megatron.Action.NW);
//            borderCells.add(this.map.getMap().get(position.add(-5, -1)));
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
        borderCells.ensureCapacity(40);
        actions.ensureCapacity(40);

        // Different order of directions, depending on start position
        if (this.getStartPosition().getY() == 0) {
            borderCells.add(this.map.getMap().get(position.add(5, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(-5, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(5, 5)));
            actions.add(Megatron.Action.SE);
            
            borderCells.add(this.map.getMap().get(position.addX(5)));
            actions.add(Megatron.Action.E);
            borderCells.add(this.map.getMap().get(position.add(5, -1)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(5, -2)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(5, -3)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(5, -4)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(4, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(3, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(2, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(1, -5)));
            actions.add(Megatron.Action.NE);
            
            borderCells.add(this.map.getMap().get(position.addY(-5)));
            actions.add(Megatron.Action.N);
            borderCells.add(this.map.getMap().get(position.add(-1, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-2, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-3, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-4, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -4)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -3)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -2)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -1)));
            actions.add(Megatron.Action.NW);
            
            borderCells.add(this.map.getMap().get(position.addX(-5)));
            actions.add(Megatron.Action.W);
            borderCells.add(this.map.getMap().get(position.add(-5, 1)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-5, 2)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-5, 3)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-5, 4)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-4, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-3, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-2, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-1, 5)));
            actions.add(Megatron.Action.SW);
            
            borderCells.add(this.map.getMap().get(position.addY(5)));
            actions.add(Megatron.Action.S);
            borderCells.add(this.map.getMap().get(position.add(1, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(2, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(3, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(4, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 4)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 3)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 2)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 1)));
            actions.add(Megatron.Action.SE);
        } else {
            borderCells.add(this.map.getMap().get(position.add(-5, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(5, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(-5, -5)));
            actions.add(Megatron.Action.NW);
            
            borderCells.add(this.map.getMap().get(position.addX(-5)));
            actions.add(Megatron.Action.W);
            borderCells.add(this.map.getMap().get(position.add(-5, 1)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-5, 2)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-5, 3)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-5, 4)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-4, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-3, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-2, 5)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-1, 5)));
            actions.add(Megatron.Action.SW);
            
            borderCells.add(this.map.getMap().get(position.addY(5)));
            actions.add(Megatron.Action.S);
            borderCells.add(this.map.getMap().get(position.add(1, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(2, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(3, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(4, 5)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 4)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 3)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 2)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(5, 1)));
            actions.add(Megatron.Action.SE);
            
            borderCells.add(this.map.getMap().get(position.addX(5)));
            actions.add(Megatron.Action.E);
            borderCells.add(this.map.getMap().get(position.add(5, -1)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(5, -2)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(5, -3)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(5, -4)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(4, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(3, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(2, -5)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(1, -5)));
            actions.add(Megatron.Action.NE);
            
            borderCells.add(this.map.getMap().get(position.addY(-5)));
            actions.add(Megatron.Action.N);
            borderCells.add(this.map.getMap().get(position.add(-1, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-2, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-3, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-4, -5)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -4)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -3)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -2)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-5, -1)));
            actions.add(Megatron.Action.NW);
        }
    }

}
