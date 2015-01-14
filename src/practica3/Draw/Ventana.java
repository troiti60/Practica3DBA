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
        System.out.println("Creando el panel para el mapa");
        jpanel = new JPMap();
        System.out.println("Creando las barras de bateria");
        batteryDrone0 = new JProgressBar();
        batteryDrone1 = new JProgressBar();
        batteryDrone2 = new JProgressBar();
        batteryDrone3 = new JProgressBar();
        batteryTotal  = new JProgressBar();
        System.out.println("Creando visualizacion de las coodenadas");
        coordXDr0 = new JLabel(); coordYDr0 = new JLabel(); labelXDr0 = new JLabel(); labelYDr0 = new JLabel();
        coordXDr1 = new JLabel(); coordYDr1 = new JLabel(); labelXDr1 = new JLabel(); labelYDr1 = new JLabel();
        coordXDr2 = new JLabel(); coordYDr2 = new JLabel(); labelXDr2 = new JLabel(); labelYDr2 = new JLabel();
        coordXDr3 = new JLabel(); coordYDr3 = new JLabel(); labelXDr3 = new JLabel(); labelYDr3 = new JLabel();
        coordPanelDr0 = new JPanel(); coordPanelDr1 = new JPanel(); coordPanelDr2 = new JPanel(); coordPanelDr3 = new JPanel();

        System.out.println("Posicionando los objetos en la ventana");
        
        // ********DRONE 1***********
        System.out.println("Posicionando barra de bateria dron 0");
        batteryDrone0.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Spectro Drone Battery");
        batteryDrone0.setBorder(border);
        batteryDrone0.setBounds(540, 10, 200, 100);
        
        System.out.println("Posicionando borde coordenadas dron 1 ");
        Border borderCordPanel = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr0.setBorder(borderCordPanel);
        coordPanelDr0.setBounds(750, 10, 200, 50); 
        
        
        //********DRONE 2***********
        System.out.println("Posicionando barra de bateria dron 2");
        batteryDrone1.setStringPainted(true);
        Border borderDr1 = BorderFactory.createTitledBorder("Viewfinder Drone Battery");
        batteryDrone1.setBorder(borderDr1);
        batteryDrone1.setBounds(540, 110, 200, 100);
        
        System.out.println("Posicionando borde de coordenadas dron 2 ");
        Border borderCordPanelDr1 = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr1.setBorder(borderCordPanelDr1);
        coordPanelDr1.setBounds(750, 110, 200, 50);
        
        //********DRONE 3***********
        System.out.println("Posicionando barra de bateria dron 3");
        batteryDrone2.setStringPainted(true);
        Border borderDr2 = BorderFactory.createTitledBorder("Nightbird Drone Battery");
        batteryDrone2.setBorder(borderDr2);
        batteryDrone2.setBounds(540, 210, 200, 100);
        
        System.out.println("Posicionando barra de coordenadas dron 3");
        Border borderCordPanelDr2 = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr2.setBorder(borderCordPanelDr2);
        coordPanelDr2.setBounds(750, 210, 200, 50);
        
        //********DRONE 4***********
        batteryDrone3.setStringPainted(true);
        Border borderDr3 = BorderFactory.createTitledBorder("Squawktalk Drone Battery");
        batteryDrone3.setBorder(borderDr3);
        batteryDrone3.setBounds(540, 310, 200, 100);
        
        Border borderCordPanelDr3 = BorderFactory.createTitledBorder("Current Position");
        coordPanelDr3.setBorder(borderCordPanelDr3);
        coordPanelDr3.setBounds(750, 310, 200, 50);
        
        System.out.println("Posicionando barra de bateria total");
        //********WORLD's TOTAL BATTERY***********
        batteryTotal.setStringPainted(true);
        Border borderBatTot = BorderFactory.createTitledBorder("World's Total Battery");
        batteryTotal.setBorder(borderBatTot);
        batteryTotal.setBounds(540, 410, 200, 100);
               
        //********ADD START TEXT***********
        labelXDr0.setText("X: "); labelXDr1.setText("X: "); labelXDr2.setText("X: "); labelXDr3.setText("X: ");
        coordXDr0.setText("0");   coordXDr1.setText("0");   coordXDr2.setText("0");   coordXDr3.setText("0");
        labelYDr0.setText("Y: "); labelYDr1.setText("Y: "); labelYDr2.setText("Y: "); labelYDr3.setText("Y: ");
        coordYDr0.setText("0");   coordYDr1.setText("0");   coordYDr2.setText("0");   coordYDr3.setText("0");
        
        System.out.println("Añadiendo coordenadas al panel de coordenadas");
        //********ADD COORD TO THE WINDOWS***********
        coordPanelDr0.add(labelXDr0); coordPanelDr1.add(labelXDr1); coordPanelDr2.add(labelXDr2);   coordPanelDr3.add(labelXDr3);
        coordPanelDr0.add(coordXDr0); coordPanelDr1.add(coordXDr1); coordPanelDr2.add(coordXDr2);   coordPanelDr3.add(coordXDr3);
        coordPanelDr0.add(labelYDr0); coordPanelDr1.add(labelYDr1); coordPanelDr2.add(labelYDr2);   coordPanelDr3.add(labelYDr3);
        coordPanelDr0.add(coordYDr0); coordPanelDr1.add(coordYDr1); coordPanelDr2.add(coordYDr2);   coordPanelDr3.add(coordYDr3);
        
        System.out.println("Añadiendo todos los elementos a la ventana");
        //********ADD ALL TO THE WINDOW***********
        this.add(jpanel);
        this.add(batteryDrone0); this.add(batteryDrone1); this.add(batteryDrone2); this.add(batteryDrone3);
        this.add(coordPanelDr0); this.add(coordPanelDr1); this.add(coordPanelDr2); this.add(coordPanelDr3);
        this.add(batteryTotal);      
        
        this.setTitle("Practica DBA");  
        System.out.println("Finalizado constructor de la ventana");
        
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
        setPreferredSize(new java.awt.Dimension(1000, 570));

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
