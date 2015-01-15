/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3.Draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import practica3.Coord;
import practica3.Decepticon;
import practica3.Map;
import practica3.Nodo;

/**
 *
 * @author JotaC
 */
public class JPMap extends javax.swing.JPanel {

    Map map;
    String world;
    int numDron,perception;
    Coord dronPos;
    Coord nodeMapCoord;
    Coord lastDronPos;

    public JPMap() {
        System.out.println("Iniciando constructor JPMAP");
        initComponents();
        map = new Map();
        dronPos = null;
        lastDronPos = null;
        nodeMapCoord = null;
        System.out.println("Finalizando constructor JPMAP");
        
    }
    public void setDronPosition(Coord pos){
        dronPos = pos;
    }
    public void setLastDronPosition(Coord pos){
        lastDronPos = pos;
    }
    
    public void setWorld(String world){
        this.world = world;
    }
    public void updateDraw(Coord coord, int nDron, int perception) {
        this.perception = perception;
        nodeMapCoord = coord;       
        numDron = nDron;              
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        System.out.println("Pintando en el panel con JPMAP");
 
            if (perception == 0) {
                g.setColor(Color.WHITE);
            }else if ((perception == 1 || perception == 2 )) {
                g.setColor(Color.BLACK);
            }else if (perception == 3) {
                g.setColor(Color.MAGENTA);
            }
            g.fillRect((nodeMapCoord.getX()*5)+7, (nodeMapCoord.getY()*5)+7, 5, 5); 

            if(lastDronPos != null){
                switch(numDron){
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
                g.fillRect((lastDronPos.getX()*5)+7, (lastDronPos.getY()*5)+7, 5, 5);
            }                 
            if(dronPos!= null){
                switch(numDron){
                    case 0:
                        g.setColor(Color.BLUE);                      
                        break;
                    case 1:
                        g.setColor(Color.RED);                       
                        break;
                    case 2:
                        g.setColor(Color.GREEN);                       
                        break;
                    case 3:
                        g.setColor(Color.YELLOW);                       
                        break;
                }
                g.fillRect((dronPos.getX()*5)+7, (dronPos.getY()*5)+7, 5, 5);
            }
        
        System.out.println("Fin del metodo paint() de JPMap");
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
