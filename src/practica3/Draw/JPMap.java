package practica3.Draw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    String world;
    int numDron;
    Coord dronPos;
    ArrayList<Node> visited;
    

    /**
     * Constructor
     *
     * @author José Carlos Alfaro
     */
    public JPMap() {
        System.out.println("Iniciando constructor JPMAP");
        initComponents();
        visited = new ArrayList<>();
        map = new Map(100);
        dronPos = null;        
        System.out.println("Finalizando constructor JPMAP");
        
    }
    public void setDronPosition(Coord pos){
        dronPos = pos;
    }
    
    public void setWorld(String world){
        this.world = world;
    }
    public void updateDraw(Map map, int nDron,Coord lastPos){
        
        if(lastPos!=null){ 
            
            Node n = new Node((new Coord(lastPos.getX(),lastPos.getY())),0);
            n.setVisited(nDron);
            visited.add(n);      
        }    
        this.map = map;    
        numDron = nDron;              
        this.repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Draw map
        if (!this.map.getMap().isEmpty()) {
           
            Iterator it = this.map.getMap().entrySet().iterator();
            Coord c;
            Node n;
            System.out.println("[JPANEL] El nodo con coordenadas"
                                +"["+map.getMap().get(dronPos).getX()+","
                                +map.getMap().get(dronPos).getY()+"]"+
                                "ha sido visitado por dron: "
                                +map.getMap().get(dronPos).isVisited());
            while (it.hasNext()) {
                java.util.Map.Entry e = (java.util.Map.Entry) it.next();       
                n = new Node((Node) e.getValue());
                c = n.getCoord();                
               
                if (n.getRadar() == 0) {
                g.setColor(Color.WHITE);
                }else if ((n.getRadar() == 1 || n.getRadar() == 2 )) {
                    g.setColor(Color.BLACK);
                }else if (n.getRadar() == 3) {
                    g.setColor(Color.MAGENTA);
                }
                g.fillRect((n.getX()*5)+7, (n.getY()*5)+7, 5, 5); 
                //g.drawImage(Image, (nodeMapCoord.getX()*5)+7, (nodeMapCoord.getY()*5)+7, this);
                
            }
            for(int i=0;i<visited.size();i++){

                    switch(visited.get(i).isVisited()){
                       case 0:
                        g.setColor(Color.CYAN);
                        System.out.println("[Pintando camino del dron 0]");
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
                    g.fillRect((visited.get(i).getX()*5)+7, (visited.get(i).getY()*5)+7, 5, 5);
                    //g.drawImage(Image, (nodeMapCoord.getX()*5)+7, (nodeMapCoord.getY()*5)+7, this);
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
                //g.drawImage(Image, (dronPos.getX()*5)+7, (dronPos.getY()*5)+7, this);
            }
            
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
