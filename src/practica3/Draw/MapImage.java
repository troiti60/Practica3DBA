package practica3.Draw;

import practica3.megatron.Coord;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import practica3.DataAccess;

/**
 * Class to visualize the map and the movement of its drones
 * 
 * @author Alexander Straub
 */
public class MapImage {
    
    /**
     * Image of the map
     */
    private final BufferedImage image;
    
    /**
     * Terrain colors
     */
    private final Color unexploredColor = Color.LIGHT_GRAY;
    private final Color exploredColor = Color.WHITE;
    private final Color wallColor = Color.BLACK;
    private final Color targetColor = Color.MAGENTA;
    
    /**
     * Drone colors
     */
    private final Color drone0Color = Color.CYAN;
    private final Color drone1Color = Color.PINK;
    private final Color drone2Color = Color.GREEN;
    private final Color drone3Color = Color.YELLOW;
    
    /**
     * Frame to show image in (for realtime)
     */
    JFrame frame = null;
    
    /**
     * Constructor
     * 
     * @param resolution Horizontal and vertical resolution of the map (quadratic)
     * @author Alexander Straub
     */
    public MapImage(int resolution) {
        // Create image and set default background color for unexplored terrain
        this.image = new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                this.image.setRGB(x, y, this.unexploredColor.getRGB());
            }
        }
        
        // Create a frame with the image
        this.frame = new JFrame();
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.getContentPane().add(new JLabel(new ImageIcon(this.image)));
        this.frame.pack();
        this.frame.setVisible(true);
    }
    
    /**
     * Set color for this drone at the given position
     * 
     * @param drone Drone ID
     * @param position Position of the drone
     * @author Alexander Straub
     */
    public void setDronePosition(int drone, Coord position) {
        Color color;
        switch (drone) {
            case 0: color = this.drone0Color; break;
            case 1: color = this.drone1Color; break;
            case 2: color = this.drone2Color; break;
            default: color = this.drone3Color; break;            
        }
        
        if (this.image.getRGB(position.getX(), position.getY()) != this.targetColor.getRGB())
            this.image.setRGB(position.getX(), position.getY(), color.getRGB());
        
        update();
    }
    
    /**
     * Set color of the terrain
     * 
     * @param radar Type of terrain
     * @param position Position of the cell
     */
    public void setCell(int radar, Coord position) {
        if (position.getX() < 0 || position.getY() < 0 || 
                position.getX() >= this.image.getWidth() || position.getY() >= this.image.getHeight()) return;
        
        Color color;
        switch (radar) {
            case 0: color = this.exploredColor; break;
            case 1: color = this.wallColor; break;
            default: color = this.targetColor; break;            
        }
        
        if (this.image.getRGB(position.getX(), position.getY()) != this.drone0Color.getRGB() 
                && this.image.getRGB(position.getX(), position.getY()) != this.drone1Color.getRGB() 
                && this.image.getRGB(position.getX(), position.getY()) != this.drone2Color.getRGB() 
                && this.image.getRGB(position.getX(), position.getY()) != this.drone3Color.getRGB())
            this.image.setRGB(position.getX(), position.getY(), color.getRGB());
        
        update();
    }
    
    /**
     * Save the image on the hard drive
     *
     * @throws IOException
     * @author Alexander Straub
     */
    public void saveToFile() throws IOException {
        File outputfile = new File(DataAccess.createInstance().getWorld() + "_0.bmp");
        
        int fileNumber = 1;
        while (outputfile.exists()) {
            outputfile = new File(DataAccess.createInstance().getWorld() + "_" + fileNumber + ".bmp");
            fileNumber++;
        }
        
        ImageIO.write(this.image, "bmp", outputfile);
    }
    
    /**
     * Update image and frame
     */
    private void update() {
        this.frame.repaint();
    }
    
}
