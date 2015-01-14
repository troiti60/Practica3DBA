package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;

/**
 * Birdron class (pájaro)
 *
 * @author Alexander Straub
 */
public final class Birdron extends Decepticon {

    /**
     * Constructor
     *
     * @param aid ID of the new decepticon
     * @param megatron ID of megatron
     * @param key Key for communication
     * @param map Reference to the map
     * @throws Exception
     * @author Alexander Straub
     */
    public Birdron(AgentID aid, AgentID megatron, String key, Map map) throws Exception {
        super(aid, megatron, 1, 5, key, map);
        this.name = "Birdron " + this.getName();

        System.out.println(this.name + ": Instantiated");
    }

    
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
    protected final void mapv3_getBorderCells(Coord position, ArrayList<Nodo> borderCells, ArrayList<Megatron.Action> actions) {
        borderCells.ensureCapacity(16);
        actions.ensureCapacity(16);

        // Different order of directions, depending on start position
        if (this.getStartPosition().getY() == 0) {
            borderCells.add(this.map.getMap().get(position.addX(2)));
            actions.add(Megatron.Action.E);
            borderCells.add(this.map.getMap().get(position.addY(-2)));
            actions.add(Megatron.Action.N);
            borderCells.add(this.map.getMap().get(position.addX(-2)));
            actions.add(Megatron.Action.W);
            borderCells.add(this.map.getMap().get(position.addY(2)));
            actions.add(Megatron.Action.S);

            borderCells.add(this.map.getMap().get(position.add(2, -1)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(2, -2)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(1, -2)));
            actions.add(Megatron.Action.NE);

            borderCells.add(this.map.getMap().get(position.add(-1, -2)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-2, -2)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-2, -1)));
            actions.add(Megatron.Action.NW);

            borderCells.add(this.map.getMap().get(position.add(-2, 1)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-2, 2)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-1, 2)));
            actions.add(Megatron.Action.SW);

            borderCells.add(this.map.getMap().get(position.add(1, 2)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(2, 2)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(2, 1)));
            actions.add(Megatron.Action.SE);
        } else {
            borderCells.add(this.map.getMap().get(position.addX(-2)));
            actions.add(Megatron.Action.W);
            borderCells.add(this.map.getMap().get(position.addY(2)));
            actions.add(Megatron.Action.S);
            borderCells.add(this.map.getMap().get(position.addX(2)));
            actions.add(Megatron.Action.E);
            borderCells.add(this.map.getMap().get(position.addY(-2)));
            actions.add(Megatron.Action.N);

            borderCells.add(this.map.getMap().get(position.add(-2, 1)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-2, 2)));
            actions.add(Megatron.Action.SW);
            borderCells.add(this.map.getMap().get(position.add(-1, 2)));
            actions.add(Megatron.Action.SW);

            borderCells.add(this.map.getMap().get(position.add(1, 2)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(2, 2)));
            actions.add(Megatron.Action.SE);
            borderCells.add(this.map.getMap().get(position.add(2, 1)));
            actions.add(Megatron.Action.SE);

            borderCells.add(this.map.getMap().get(position.add(2, -1)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(2, -2)));
            actions.add(Megatron.Action.NE);
            borderCells.add(this.map.getMap().get(position.add(1, -2)));
            actions.add(Megatron.Action.NE);

            borderCells.add(this.map.getMap().get(position.add(-1, -2)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-2, -2)));
            actions.add(Megatron.Action.NW);
            borderCells.add(this.map.getMap().get(position.add(-2, -1)));
            actions.add(Megatron.Action.NW);
        }
    }

}
