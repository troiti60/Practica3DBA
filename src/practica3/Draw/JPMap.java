package practica3.Draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import practica3.Coord;
import practica3.Decepticon;
import practica3.Map;
import practica3.Node;

/**
 * Drawing area
 *
 * @author José Carlos Alfaro
 */
public class JPMap extends javax.swing.JPanel {

    Map map;
    Coord dronePos;
    Coord lastDronePos;

    /**
     * Constructor
     *
     * @author José Carlos Alfaro
     */
    public JPMap() {
        initComponents();
        map = new Map(100);
        dronePos = null;
        lastDronePos = null;
    }

    /**
     * Updates the local references
     *
     * @param map Map
     * @param drone Drone
     * @author José Carlos Alfaro
     */
    public void updateDraw(Map map, Decepticon drone) {
        this.map = map;
        this.dronePos = drone.getPosition();
        this.lastDronePos = drone.getLastPosition();
        this.repaint();
    }

    /**
     * Paint method
     *
     * @param g Graphic
     * @author José Carlos Alfaro
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Draw map
        if (!this.map.getMap().isEmpty()) {
            Iterator it = this.map.getMap().entrySet().iterator();
            Coord c;
            Node n;

            while (it.hasNext()) {
                java.util.Map.Entry e = (java.util.Map.Entry) it.next();
                c = new Coord((Coord) e.getKey());
                n = new Node((Node) e.getValue());
                if (n.getRadar() == 0) {
                    g.setColor(Color.WHITE);
                }
                if (n.getRadar() == 1) {
                    g.setColor(Color.GREEN);
                }
                if (n.getRadar() == 2) {
                    g.setColor(Color.BLACK);
                }
                if (n.getRadar() == 3) {
                    g.setColor(Color.MAGENTA);
                }
                g.fillRect((c.getX() * 5) + 7, (c.getY() * 5) + 7, 5, 5);
            }
        }
        
        // Draw drone position
        if (this.dronePos != null) {
            g.setColor(Color.BLUE);
            g.fillRect((this.dronePos.getX() * 5) + 7, (this.dronePos.getY() * 5) + 7, 5, 5);
        }
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
