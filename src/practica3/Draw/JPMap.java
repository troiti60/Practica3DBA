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
    int numDron;
    Coord dronPos;
    Coord dronLastPos;

    public JPMap() {
        initComponents();
        map = new Map();
        dronPos = null;
        dronLastPos = null;
        
    }

    public void updateDraw(Map m, Decepticon dron, int nDron,String world) {
        map = m;
        this.world = world;
        numDron = nDron;
        dronPos = dron.getPosition();
        dronLastPos = dron.getLastPosition();
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        
        if (!map.getMap().isEmpty()) {

            Iterator it = map.getMap().entrySet().iterator();
            Coord c;
            Nodo n;

            while (it.hasNext()) {
                java.util.Map.Entry e = (java.util.Map.Entry) it.next();
                c = new Coord((Coord) e.getKey());
                n = new Nodo((Nodo) e.getValue());
                if (n.getRadar() == 0 && n.isVisitado()== -1) {
                    g.setColor(Color.WHITE);
                }else if ((n.getRadar() == 1 || n.getRadar() == 2 )  && n.isVisitado()== -1) {
                    g.setColor(Color.BLACK);
                }else if (n.getRadar() == 3 && n.isVisitado()== -1) {
                    g.setColor(Color.MAGENTA);
                }else if(n.isVisitado()==0 && numDron == 0){
                    g.setColor(Color.CYAN);
                }else if(n.isVisitado()==1 && numDron == 1){
                    g.setColor(Color.PINK);
                }else if(n.isVisitado()==2 && numDron == 2){
                    g.setColor(Color.GREEN);
                }else if(n.isVisitado()==3 && numDron == 3){
                    g.setColor(Color.YELLOW);
                }
                
                g.fillRect((c.getX()*5)+7, (c.getY()*5)+7, 5, 5);                
            }       
            if(dronPos!= null){
                switch(numDron){
                    case 0:
                        g.setColor(Color.BLUE);
                        g.fillRect((dronPos.getX()*5)+7, (dronPos.getY()*5)+7, 5, 5);
                        break;
                    case 1:
                        g.setColor(Color.RED);
                        g.fillRect((dronPos.getX()*5)+7, (dronPos.getY()*5)+7, 5, 5);
                        break;
                    case 2:
                        g.setColor(Color.GREEN);
                        g.fillRect((dronPos.getX()*5)+7, (dronPos.getY()*5)+7, 5, 5);
                        break;
                    case 3:
                        g.setColor(Color.YELLOW);
                        g.fillRect((dronPos.getX()*5)+7, (dronPos.getY()*5)+7, 5, 5);
                        break;
                }
                
            }
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
