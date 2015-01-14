/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3.Draw;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.*;
import javax.swing.border.Border;


/**
 *
 * @author JotaC
 */
public class Ventana extends javax.swing.JFrame {
    
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
     */
    public Ventana() {
        initComponents();
        jpanel = new JPMap();
        batteryDrone0 = new JProgressBar();
        batteryDrone1 = new JProgressBar();
        batteryDrone2 = new JProgressBar();
        batteryDrone3 = new JProgressBar();
        batteryTotal  = new JProgressBar();
        coordXDr0        = new JLabel();
        coordYDr0        = new JLabel();
        labelXDr0        = new JLabel();
        labelYDr0        = new JLabel();
        coordPanelDr0    = new JPanel();
        
        // ********DRONE 1***********
        batteryDrone0.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Spectro Drone Battery");
        batteryDrone0.setBorder(border);
        batteryDrone0.setBounds(540, 10, 200, 100);
        
        Border borderCordPanel = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr0.setBorder(borderCordPanel);
        coordPanelDr0.setBounds(650, 10, 150, 50); 
        
        //********DRONE 2***********
        batteryDrone1.setStringPainted(true);
        Border borderDr1 = BorderFactory.createTitledBorder("Viewfinder Drone Battery");
        batteryDrone1.setBorder(borderDr1);
        batteryDrone1.setBounds(540, 110, 200, 100);
        
        Border borderCordPanelDr1 = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr0.setBorder(borderCordPanelDr1);
        coordPanelDr0.setBounds(650, 110, 150, 50);
        
        //********DRONE 3***********
        batteryDrone2.setStringPainted(true);
        Border borderDr2 = BorderFactory.createTitledBorder("Nightbird Drone Battery");
        batteryDrone2.setBorder(borderDr2);
        batteryDrone2.setBounds(540, 210, 200, 100);
        
        Border borderCordPanelDr2 = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr2.setBorder(borderCordPanelDr2);
        coordPanelDr2.setBounds(650, 210, 150, 50);
        
        //********DRONE 4***********
        batteryDrone3.setStringPainted(true);
        Border borderDr3 = BorderFactory.createTitledBorder("Squawktalk Drone Battery");
        batteryDrone3.setBorder(borderDr3);
        batteryDrone3.setBounds(540, 310, 200, 100);
        
        Border borderCordPanelDr3 = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr0.setBorder(borderCordPanelDr3);
        coordPanelDr0.setBounds(650, 310, 150, 50);
        
        
        //********WORLD's TOTAL BATTERY***********
        batteryTotal.setStringPainted(true);
        Border borderBatTot = BorderFactory.createTitledBorder("World's Total Battery");
        batteryTotal.setBorder(borderBatTot);
        batteryTotal.setBounds(540, 410, 200, 100);
               
        //********ADD START TEXT***********
        labelXDr0.setText("X: "); labelXDr0.setText("X: "); labelXDr0.setText("X: "); labelXDr0.setText("X: ");
        coordXDr1.setText("0");   coordXDr1.setText("0");   coordXDr1.setText("0");   coordXDr1.setText("0");
        labelYDr2.setText("Y: "); labelYDr2.setText("Y: "); labelYDr2.setText("Y: "); labelYDr2.setText("Y: ");
        coordYDr3.setText("0");   coordYDr3.setText("0");   coordYDr3.setText("0");   coordYDr3.setText("0");
        
        
        //********ADD COORD TO THE PANEL***********
        coordPanelDr0.add(labelXDr0); coordPanelDr0.add(labelXDr0); coordPanelDr0.add(labelXDr0);   coordPanelDr0.add(labelXDr0);
        coordPanelDr1.add(coordXDr1); coordPanelDr1.add(coordXDr1); coordPanelDr1.add(coordXDr1);   coordPanelDr1.add(coordXDr1);
        coordPanelDr2.add(labelYDr2); coordPanelDr2.add(labelYDr2); coordPanelDr2.add(labelYDr2);   coordPanelDr2.add(labelYDr2);
        coordPanelDr3.add(coordYDr3); coordPanelDr3.add(coordYDr3); coordPanelDr3.add(coordYDr3);   coordPanelDr3.add(coordYDr3);
        
        //********ADD ALL TO THE WINDOW***********
        this.add(jpanel);
        this.add(batteryDrone0); this.add(batteryDrone1); this.add(batteryDrone2); this.add(batteryDrone3);
        this.add(batteryTotal);
        this.add(coordPanelDr0); this.add(coordPanelDr1); this.add(coordPanelDr2); this.add(coordPanelDr3);
        this.setTitle("Practica DBA");  
    }
    
    public JPMap getJpanel(){
        return jpanel; 
    }
    
    public void setBatteryDroneValue(int dron,int value){
        switch(dron){
            case 0:
                batteryDrone0.setValue(value);
                batteryDrone0.repaint();
                break;
            case 1:
                batteryDrone1.setValue(value);
                batteryDrone1.repaint();
                break;
            case 2:
                batteryDrone2.setValue(value);
                batteryDrone2.repaint();
                break;
            case 3:
                batteryDrone3.setValue(value);
                batteryDrone3.repaint();
                break;
        }
    }
    
    public void setTotalBatteryValue(int value){
        Double d = value*0.1;
        batteryTotal.setValue(d.intValue());
        batteryTotal.repaint();
    }
    
    public void setLabelCoordinate(int x,int y,int dron){
        
        String cX = Integer.toString(x);
        String cY = Integer.toString(y);
        switch(dron){
            case 0:
                coordXDr0.setText("["+cX+"]");
                coordXDr0.repaint();
                coordYDr0.setText("["+cY+"]");
                coordYDr0.repaint();
                break;
            case 1:
                coordXDr1.setText("["+cX+"]");
                coordXDr1.repaint();
                coordYDr1.setText("["+cY+"]");
                coordYDr1.repaint();
                break;
            case 2:
                coordXDr2.setText("["+cX+"]");
                coordXDr2.repaint();
                coordYDr2.setText("["+cY+"]");
                coordYDr2.repaint();
                break;
            case 3:
                coordXDr3.setText("["+cX+"]");
                coordXDr3.repaint();
                coordYDr3.setText("["+cY+"]");
                coordYDr3.repaint();
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
        setPreferredSize(new java.awt.Dimension(850, 570));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 850, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 570, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ventana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Ventana().setVisible(true);
            }
        });
        
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
