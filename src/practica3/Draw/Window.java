package practica3.Draw;

import javax.swing.*;
import javax.swing.border.Border;
import practica3.DataAccess;

/**
 * Window for realtime observation
 * 
 * @author José Carlos Alfaro
 */
public class Window extends javax.swing.JFrame {
    
    /**
     * Window controls
     */
    JPMap jpanel;
    JProgressBar batteryDrone0;
    JProgressBar batteryDrone1;
    JProgressBar batteryDrone2;
    JProgressBar batteryDrone3;
    JProgressBar batteryTotal;
    JLabel coordXDr0,coordYDr0,labelXDr0,labelYDr0;
    JLabel coordXDr1,coordYDr1,labelXDr1,labelYDr1;
    JLabel coordXDr2,coordYDr2,labelXDr2,labelYDr2;
    JLabel coordXDr3,coordYDr3,labelXDr3,labelYDr3;
    JPanel coordPanelDr0,coordPanelDr1,coordPanelDr2,coordPanelDr3;
    
    /**
     * Creates new form Window
     * 
     * @author José Carlos Alfaro
     */
    public Window() {
        initComponents();
        
        // Panel for drawing the map
        this.jpanel = new JPMap();
        
        // Progress bars for the batteries
        this.batteryDrone0 = new JProgressBar();
        this.batteryDrone1 = new JProgressBar();
        this.batteryDrone2 = new JProgressBar();
        this.batteryDrone3 = new JProgressBar();
        this.batteryTotal  = new JProgressBar();
        
        // Labels for the coordinates
        this.coordXDr0 = new JLabel(); this.coordYDr0 = new JLabel(); this.labelXDr0 = new JLabel(); this.labelYDr0 = new JLabel();
        this.coordXDr1 = new JLabel(); this.coordYDr1 = new JLabel(); this.labelXDr1 = new JLabel(); this.labelYDr1 = new JLabel();
        this.coordXDr2 = new JLabel(); this.coordYDr2 = new JLabel(); this.labelXDr2 = new JLabel(); this.labelYDr2 = new JLabel();
        this.coordXDr3 = new JLabel(); this.coordYDr3 = new JLabel(); this.labelXDr3 = new JLabel(); this.labelYDr3 = new JLabel();
        this.coordPanelDr0 = new JPanel(); this.coordPanelDr1 = new JPanel(); this.coordPanelDr2 = new JPanel(); this.coordPanelDr3 = new JPanel();

        // Set controls for first drone
        Border border = BorderFactory.createTitledBorder(DataAccess.createInstance().getNameDrone1() + " Drone Battery");
        this.batteryDrone0.setStringPainted(true);
        this.batteryDrone0.setBorder(border);
        this.batteryDrone0.setBounds(540, 10, 200, 100);
        
        Border borderCordPanel = BorderFactory.createTitledBorder("Current Position");
        this.coordPanelDr0.setBorder(borderCordPanel);
        this.coordPanelDr0.setBounds(750, 10, 200, 50); 
        
        // Set controls for second drone
        Border borderDr1 = BorderFactory.createTitledBorder(DataAccess.createInstance().getNameDrone2() + " Drone Battery");
        this.batteryDrone1.setStringPainted(true);
        this.batteryDrone1.setBorder(borderDr1);
        this.batteryDrone1.setBounds(540, 110, 200, 100);
        
        Border borderCordPanelDr1 = BorderFactory.createTitledBorder("Current Position");
        this.coordPanelDr1.setBorder(borderCordPanelDr1);
        this.coordPanelDr1.setBounds(750, 110, 200, 50);
        
        // Set controls for third drone
        Border borderDr2 = BorderFactory.createTitledBorder(DataAccess.createInstance().getNameDrone3() + " Drone Battery");
        this.batteryDrone2.setStringPainted(true);
        this.batteryDrone2.setBorder(borderDr2);
        this.batteryDrone2.setBounds(540, 210, 200, 100);
        
        Border borderCordPanelDr2 = BorderFactory.createTitledBorder("Current Position");
        this.coordPanelDr2.setBorder(borderCordPanelDr2);
        this.coordPanelDr2.setBounds(750, 210, 200, 50);
        
        // Set controls for fourth drone
        Border borderDr3 = BorderFactory.createTitledBorder(DataAccess.createInstance().getNameDrone4() + " Drone Battery");
        this.batteryDrone3.setStringPainted(true);
        this.batteryDrone3.setBorder(borderDr3);
        this.batteryDrone3.setBounds(540, 310, 200, 100);
        
        Border borderCordPanelDr3 = BorderFactory.createTitledBorder("Current Position");
        this.coordPanelDr3.setBorder(borderCordPanelDr3);
        this.coordPanelDr3.setBounds(750, 310, 200, 50);
        
        // Set controls for world battery
        Border borderBatTot = BorderFactory.createTitledBorder("World's Total Battery");
        this.batteryTotal.setStringPainted(true);
        this.batteryTotal.setBorder(borderBatTot);
        this.batteryTotal.setBounds(540, 410, 200, 100);
               
        // Initialize positions to (0,0) for all drones
        this.labelXDr0.setText("X: "); this.labelXDr1.setText("X: "); this.labelXDr2.setText("X: "); this.labelXDr3.setText("X: ");
        this.coordXDr0.setText("0");   this.coordXDr1.setText("0");   this.coordXDr2.setText("0");   this.coordXDr3.setText("0");
        this.labelYDr0.setText("Y: "); this.labelYDr1.setText("Y: "); this.labelYDr2.setText("Y: "); this.labelYDr3.setText("Y: ");
        this.coordYDr0.setText("0");   this.coordYDr1.setText("0");   this.coordYDr2.setText("0");   this.coordYDr3.setText("0");
        
        // Add coordinate labels to the panel
        this.coordPanelDr0.add(this.labelXDr0); this.coordPanelDr1.add(this.labelXDr1); this.coordPanelDr2.add(this.labelXDr2); this.coordPanelDr3.add(this.labelXDr3);
        this.coordPanelDr0.add(this.coordXDr0); this.coordPanelDr1.add(this.coordXDr1); this.coordPanelDr2.add(this.coordXDr2); this.coordPanelDr3.add(this.coordXDr3);
        this.coordPanelDr0.add(this.labelYDr0); this.coordPanelDr1.add(this.labelYDr1); this.coordPanelDr2.add(this.labelYDr2); this.coordPanelDr3.add(this.labelYDr3);
        this.coordPanelDr0.add(this.coordYDr0); this.coordPanelDr1.add(this.coordYDr1); this.coordPanelDr2.add(this.coordYDr2); this.coordPanelDr3.add(this.coordYDr3);
        
        // Add all previously created controls to the window
        this.add(this.jpanel);
        this.add(this.batteryDrone0); this.add(this.batteryDrone1); this.add(this.batteryDrone2); this.add(this.batteryDrone3);
        this.add(this.coordPanelDr0); this.add(this.coordPanelDr1); this.add(this.coordPanelDr2); this.add(this.coordPanelDr3);
        this.add(this.batteryTotal);      
        
        // Set window title
        String world = DataAccess.createInstance().getWorld();
        world = world.substring(0, 1).toUpperCase() + world.substring(1);
        this.setTitle("Practica DBA: " + world);
    }
    
    /**
     * Return the drawing area
     * 
     * @return Drawing area
     * @author José Carlos Alfaro
     */
    public JPMap getJpanel() {
        return this.jpanel;
    }

    /**
     * Set battery value for drone
     * 
     * @param drone Drone ID
     * @param value New value
     * @author José Carlos Alfaro
     */
    public void setBatteryDroneValue(int drone, int value) {
        switch (drone) {
            case 0:
                this.batteryDrone0.setValue(value);
                this.batteryDrone0.repaint();
                break;
            case 1:
                this.batteryDrone1.setValue(value);
                this.batteryDrone1.repaint();
                break;
            case 2:
                this.batteryDrone2.setValue(value);
                this.batteryDrone2.repaint();
                break;
            case 3:
                this.batteryDrone3.setValue(value);
                this.batteryDrone3.repaint();
                break;
        }
    }

    /**
     * Set world's battery value
     * 
     * @param value New value
     * @author José Carlos Alfaro
     */
    public void setTotalBatteryValue(int value,String world) {
        Double d;
        if(world != "newyork")
          d = value * 0.1;
        else
          d = value * 0.05;
        
        this.batteryTotal.setValue(d.intValue());
        this.batteryTotal.repaint();
    }

    /**
     * Set new coordinates
     * 
     * @param x New x coordinate
     * @param y New y coordinate
     * @param drone Drone for which to set new coordinates
     * @author José Carlos Alfaro
     */
    public void setLabelCoordinate(int x, int y, int drone) {
        String cX = Integer.toString(x);
        String cY = Integer.toString(y);
        switch (drone) {
            case 0:
                this.coordXDr0.setText("[" + cX + "]");
                this.coordXDr0.repaint();
                this.coordYDr0.setText("[" + cY + "]");
                this.coordYDr0.repaint();
                break;
            case 1:
                this.coordXDr1.setText("[" + cX + "]");
                this.coordXDr1.repaint();
                this.coordYDr1.setText("[" + cY + "]");
                this.coordYDr1.repaint();
                break;
            case 2:
                this.coordXDr2.setText("[" + cX + "]");
                this.coordXDr2.repaint();
                this.coordYDr2.setText("[" + cY + "]");
                this.coordYDr2.repaint();
                break;
            case 3:
                this.coordXDr3.setText("[" + cX + "]");
                this.coordXDr3.repaint();
                this.coordYDr3.setText("[" + cY + "]");
                this.coordYDr3.repaint();
                break;
        }
    }
    
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 570, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
