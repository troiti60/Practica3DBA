package practica3.Draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import practica3.megatron.Coord;
import practica3.megatron.Map;
import practica3.megatron.Node;

/**
 * Drawing area
 *
 * @author José Carlos Alfaro
 */
public class JPMap extends javax.swing.JPanel {

    /**
     * Information stored for painting the map and drones
     */
    Map map;
    Coord dronePos;
    ArrayList<Node> visited;
    int numDron;
    
    /**
     * Semaphore to restrict access
     */
    private final Semaphore lock;

    /**
     * Constructor
     *
     * @author José Carlos Alfaro
     */
    public JPMap() {
        initComponents();
        
        this.lock = new Semaphore(1, true);
        this.map = new Map(100);
        this.dronePos = null;
        this.visited = new ArrayList<>();
        this.numDron = -1;
    }

    /**
     * Sets the current position of the drone
     * 
     * @param pos Drone position
     * @author José Carlos Alfaro
     */
    public void setDronPosition(Coord pos) {
        this.dronePos = pos;
    }

    /**
     * Call to update the image
     * 
     * @param map Reference to the map
     * @param nDron ID of the drone
     * @param lastPos Last position of the drone
     * @author José Carlos Alfaro
     */
    public void updateDraw(Map map, int nDron, Coord lastPos) {
        if (lastPos != null) {
            Node n = new Node((new Coord(lastPos.getX(), lastPos.getY())), 0);
            n.setVisited(nDron);
            this.visited.add(n);
        }
        
        this.map = map;
        this.numDron = nDron;
        
        // Get lock and update, then wait till the process ended
        // --> like making the paint method synchron
        this.lock.acquireUninterruptibly();
        this.repaint();
        this.lock.acquireUninterruptibly();
        this.lock.release();
    }

    /**
     * Method to fill the graphic
     * 
     * @param g Graphic
     * @author José Carlos Alfaro
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (!this.map.getMap().isEmpty()) {
            // Draw map
            for (Node n : this.map.getMap().values()) {
                if (n.getRadar() == 0) {
                    g.setColor(Color.WHITE);
                } else if ((n.getRadar() == 1 || n.getRadar() == 2)) {
                    g.setColor(Color.BLACK);
                } else if (n.getRadar() == 3) {
                    g.setColor(Color.MAGENTA);
                }
                
                g.fillRect((n.getX() * 5) + 7, (n.getY() * 5) + 7, 5, 5);
            }
            
            // Draw visited path of the drones
            for (Node n : this.visited) {
                switch (n.isVisited()) {
                    case 0:
                        g.setColor(Color.CYAN);
                        break;
                    case 1:
                        g.setColor(Color.PINK);
                        break;
                    case 2:
                        g.setColor(Color.GREEN);
                        break;
                    case 3:
                        g.setColor(Color.YELLOW);
                        break;
                }
                g.fillRect((n.getX() * 5) + 7, (n.getY() * 5) + 7, 5, 5);
            }
            
            // Highlight position of currently moving drone
            if (this.dronePos != null && this.numDron != -1) {
                switch (this.numDron) {
                    case 0:
                        g.setColor(Color.BLUE);
                        break;
                    case 1:
                        g.setColor(Color.RED);
                        break;
                    case 2:
                        g.setColor(new Color(0, 51, 0));
                        break;
                    case 3:
                        g.setColor(Color.ORANGE);
                        break;
                }
                
                g.fillRect((this.dronePos.getX() * 5) + 7, (this.dronePos.getY() * 5) + 7, 5, 5);
            }

        }
        
        // Release lock
        this.lock.release();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(51, 102, 0), 2));
        setBounds(new java.awt.Rectangle(10, 10, 517, 517));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(506, 506));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 439, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 337, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
