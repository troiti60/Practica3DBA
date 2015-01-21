package practica3.megatron;

import java.util.ArrayList;

/**
 * Birdron class (pájaro)
 *
 * @author Alexander Straub
 */
public final class DataBirdron extends DataDecepticon {

    private final int visualRange;
    private final int consumation;

    /**
     * Constructor
     *
     * @param map Reference to the map
     * @throws Exception
     * @author Alexander Straub
     */
    public DataBirdron(Map map) throws Exception {
        super(1, map);
        this.visualRange = 5;
        this.consumation = 1;
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
        outBorderCells.ensureCapacity(16);
        outActions.ensureCapacity(16);

        // Different order of directions, depending on start position
        if (this.getStartPosition().getY() == 0) {
            outBorderCells.add(this.map.getMap().get(position.add(2, -2)));
            outActions.add(Megatron.Action.NE);
            outBorderCells.add(this.map.getMap().get(position.add(-2, -2)));
            outActions.add(Megatron.Action.NW);
            outBorderCells.add(this.map.getMap().get(position.add(-2, 2)));
            outActions.add(Megatron.Action.SW);
            outBorderCells.add(this.map.getMap().get(position.add(2, 2)));
            outActions.add(Megatron.Action.SE);

            outBorderCells.add(this.map.getMap().get(position.addX(2)));
            outActions.add(Megatron.Action.E);
            outBorderCells.add(this.map.getMap().get(position.add(2, -1)));
            outActions.add(Megatron.Action.NE);

            outBorderCells.add(this.map.getMap().get(position.add(1, -2)));
            outActions.add(Megatron.Action.NE);
            outBorderCells.add(this.map.getMap().get(position.addY(-2)));
            outActions.add(Megatron.Action.N);
            outBorderCells.add(this.map.getMap().get(position.add(-1, -2)));
            outActions.add(Megatron.Action.NW);

            outBorderCells.add(this.map.getMap().get(position.add(-2, -1)));
            outActions.add(Megatron.Action.NW);
            outBorderCells.add(this.map.getMap().get(position.addX(-2)));
            outActions.add(Megatron.Action.W);
            outBorderCells.add(this.map.getMap().get(position.add(-2, 1)));
            outActions.add(Megatron.Action.SW);

            outBorderCells.add(this.map.getMap().get(position.add(-1, 2)));
            outActions.add(Megatron.Action.SW);
            outBorderCells.add(this.map.getMap().get(position.addY(2)));
            outActions.add(Megatron.Action.S);
            outBorderCells.add(this.map.getMap().get(position.add(1, 2)));
            outActions.add(Megatron.Action.SE);

            outBorderCells.add(this.map.getMap().get(position.add(2, 1)));
            outActions.add(Megatron.Action.SE);
        } else {
            outBorderCells.add(this.map.getMap().get(position.add(-2, 2)));
            outActions.add(Megatron.Action.SW);
            outBorderCells.add(this.map.getMap().get(position.add(2, 2)));
            outActions.add(Megatron.Action.SE);
            outBorderCells.add(this.map.getMap().get(position.add(2, -2)));
            outActions.add(Megatron.Action.NE);
            outBorderCells.add(this.map.getMap().get(position.add(-2, -2)));
            outActions.add(Megatron.Action.NW);

            outBorderCells.add(this.map.getMap().get(position.addX(-2)));
            outActions.add(Megatron.Action.W);
            outBorderCells.add(this.map.getMap().get(position.add(-2, 1)));
            outActions.add(Megatron.Action.SW);

            outBorderCells.add(this.map.getMap().get(position.add(-1, 2)));
            outActions.add(Megatron.Action.SW);
            outBorderCells.add(this.map.getMap().get(position.addY(2)));
            outActions.add(Megatron.Action.S);
            outBorderCells.add(this.map.getMap().get(position.add(1, 2)));
            outActions.add(Megatron.Action.SE);

            outBorderCells.add(this.map.getMap().get(position.add(2, 1)));
            outActions.add(Megatron.Action.SE);
            outBorderCells.add(this.map.getMap().get(position.addX(2)));
            outActions.add(Megatron.Action.E);
            outBorderCells.add(this.map.getMap().get(position.add(2, -1)));
            outActions.add(Megatron.Action.NE);

            outBorderCells.add(this.map.getMap().get(position.add(1, -2)));
            outActions.add(Megatron.Action.NE);
            outBorderCells.add(this.map.getMap().get(position.addY(-2)));
            outActions.add(Megatron.Action.N);
            outBorderCells.add(this.map.getMap().get(position.add(-1, -2)));
            outActions.add(Megatron.Action.NW);

            outBorderCells.add(this.map.getMap().get(position.add(-2, -1)));
            outActions.add(Megatron.Action.NW);
        }
    }

}
