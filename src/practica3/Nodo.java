/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import static java.lang.Math.sqrt;
import java.util.ArrayList;

/**
 * Clase nodo que representa un nodo del mapa
 *
 * @author Antonio y Alexander Straub
 */
public class Nodo implements Comparable<Nodo> {

    // Coordenadas que ocupa el nodo
    private final Coord coord;
    
    // Indica si está conectado al nodo que ocupa el bot principal
    private boolean conectado = false;
    
    // Valor de radar del nodo: 0=libre 1=muro 2=objetivo
    private final int radar;
   
    
    // Array de nodos (no muros) adyacentes
    private final ArrayList<Nodo> adyacentes = new ArrayList<Nodo>();
    
    // Array de muros adyacentes al nodo
    private final ArrayList<Nodo> murosAdyacentes = new ArrayList<Nodo>();
    
    // Explorado es la suma de los tamaños de adyacentes y murosAdyacentes.
    // Si vale 8, se toma el nodo como explorado
    private int explorado;
    
    // Indica si el nodo ya era visitado
    private int visitado = -1;

    // Variables usadas en la búsqueda
    private Nodo camino = null;
    private double distancia = Double.MAX_VALUE;

    /**
     * Constructor de Nodos a partir de:
     *
     * @param x coordenada x del nodo
     * @param y coordenada y del nodo
     * @param radar valor de radar del nodo
     * @author Antonio Troitiño
     */
    public Nodo(int x, int y, int radar) {
        this.coord = new Coord(x, y);
        this.radar = radar;
        this.explorado = 0;
    }

    /**
     * Constructor de Nodos a partir de:
     *
     * @param coord coordenadas que ocupa el nodo
     * @param radar valor de radar del nodo
     * @author Alexander Straub
     */
    public Nodo(Coord coord, int radar) {
        this.coord = coord;
        this.radar = radar;
        this.explorado = 0;
    }
    
    /**
     * Constructor por copia
     * @param  n nodo a copiar;
     * @author JC
     */
    public Nodo(Nodo n){
        this.coord = n.coord;
        this.radar = n.radar;
        this.explorado = n.explorado;
    }

    /**
     * Función booleana que devuelve si el nodo está completamente explorado o
     * no
     *
     * @return True si ya están descubiertas la cajillas vecinas
     * @author Antonio Troitiño
     */
    public boolean explored() {
        return this.radar != 1 && this.explorado == 8;
    }

    /**
     * Marcar nodo como visitado
     *
     * @author Alexander Straub
     */
    public void setVisitado(int numDron) {
        this.visitado = numDron;
    }

    /**
     * Indicar si el nodo ya era visitado
     *
     * @return True si ya era visitado
     * @author Alexander Straub
     */
    public int isVisitado() {
        return this.visitado;
    }

    /**
     * Adds an adjacent node to the right collection
     *
     * @param aNode Node to be added
     * @author Antonio Troitiño
     */
    public void add(Nodo aNode) {
        if (aNode.getRadar() == 0||aNode.getRadar()==3) {
            this.adyacentes.add(aNode);
            this.explorado++;
        } else if(aNode.getRadar()==1||aNode.getRadar()==2){
            this.explorado++;
            this.murosAdyacentes.add(aNode);
        }
    }

    /**
     * Devuelve la coordenada x del nodo
     *
     * @return Coordenada X del nodo
     * @author Antonio Troitiño
     */
    public int getX() {
        return this.coord.getX();
    }

    /**
     * Devuelve la coordenada y del nodo
     *
     * @return Coordenada Y del nodo
     * @author Antonio Troitiño
     */
    public int getY() {
        return this.coord.getY();
    }

    /**
     * Devuelve el valor de radar del nodo
     *
     * @return Valor del radar
     * @author Antonio Troitiño
     */
    public int getRadar() {
        return this.radar;
    }

    /**
     * Devuelve la lista de nodos adyacentes
     *
     * @return Lista de nodos adyacentes
     * @author Antonio Troitiño
     */
    public ArrayList<Nodo> getAdy() {
        return this.adyacentes;
    }

    /**
     * Devuelve la lista de muros adyacentes
     *
     * @return Lista de muros adyacentes
     * @author Antonio Troitiño
     */
    public ArrayList<Nodo> getMuros() {
        return this.murosAdyacentes;
    }

    /**
     * Sobreescribe el valor de atributo conectado.
     *
     * @param con booleano con el que sobreescribir el atributo "conectado"
     * @author Antonio Troitiño
     */
    public void setConectado(boolean con) {
        this.conectado = con;
    }

    /**
     * Devuelve si el nodo está en conectados o no
     *
     * @return True si el nodo está conectado
     * @author Antonio Troitiño
     */
    public boolean getConectado() {
        return this.conectado;
    }

    /**
     * Devuelve las coordenadas del nodo
     *
     * @return Coordenadas del nodo
     * @author Antonio Troitiño
     */
    public Coord getCoord() {
        return this.coord;
    }



    /**
     * Devolver la coordenada en el norte
     *
     * @return Coordenada en el norte
     * @author Alexander Straub,Antonio Troitiño
     */
    public Coord N() {
        return new Coord(this.coord.getX(), this.coord.getY() - 1);
    }

    /**
     * Devolver la coordenada en el este
     *
     * @return Coordenada en el este
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord E() {
        return new Coord(this.coord.getX() + 1, this.coord.getY());
    }

    /**
     * Devolver la coordenada en el sur
     *
     * @return Coordenada en el sur
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord S() {
        return new Coord(this.coord.getX(), this.coord.getY() + 1);
    }

    /**
     * Devolver la coordenada en el oeste
     *
     * @return Coordenada en el oeste
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord O() {
        return new Coord(this.coord.getX() - 1, this.coord.getY());
    }

    /**
     * Devolver la coordenada en el noreste
     *
     * @return Coordenada en el noreste
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord NE() {
        return new Coord(this.coord.getX() + 1, this.coord.getY() - 1);
    }

    /**
     * Devolver la coordenada en el sureste
     *
     * @return Coordenada en el sureste
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord SE() {
        return new Coord(this.coord.getX() + 1, this.coord.getY() + 1);
    }

    /**
     * Devolver la coordenada en el suroeste
     *
     * @return Coordenada en el suroeste
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord SO() {
        return new Coord(this.coord.getX() - 1, this.coord.getY() + 1);
    }

    /**
     * Devolver la coordenada en el noroeste
     *
     * @return Coordenada en el noroeste
     * @author Alexander Straub, Antonio Troitiño
     */
    public Coord NO() {
        return new Coord(this.coord.getX() - 1, this.coord.getY() - 1);
    }

    /**
     * Reinstaurar los valores necesitados para una nueva búsqueda
     *
     * @author Alexander Straub
     */
    public void resetBusqueda() {
        this.camino = null;
        this.distancia = Double.MAX_VALUE;
    }

    /**
     * Setter para guardar el nodo delante en el camino
     *
     * @param nodo Nodo delante en el camino
     * @author Alexander Straub
     */
    public void setCamino(Nodo nodo) {
        this.camino = nodo;
    }

    /**
     * Getter para devolver el nodo delante en el camino
     *
     * @return Nodo del camino
     * @author Alexander Straub
     */
    public Nodo getCamino() {
        return this.camino;
    }

    /**
     * Setter para guardar la distancia calculada en la búsqueda
     *
     * @param newDistancia Distancia
     * @author Alexander Straub
     */
    public void setDistancia(double newDistancia) {
        this.distancia = newDistancia;
    }

    /**
     * Getter para devolver la distancia calculada en la búsqueda
     *
     * @return Distancia
     * @author Alexander Straub
     */
    public double getDistancia() {
        return this.distancia;
    }

    /**
     * Comparar dos Nodos usando sus distancias
     *
     * @param otro Nodo con que comparar
     * @return -1: otro menos que this, +1: otro más que this, 0: igual
     * @author Alexander Straub
     */
    @Override
    public int compareTo(Nodo otro) {
        if (otro == null) {
            return 0; // TODO: Exception
        }
        if (otro == this) {
            return 0;
        }

        if (otro.getDistancia() < this.distancia) {
            return 1;
        }
        if (otro.getDistancia() > this.distancia) {
            return -1;
        }
        return 0;
    }

    /**
     * Devolver la distancia entre dos nodos que son vecinos !! Si no son
     * vecinos, devuelve un valor falso !!
     *
     * @param otro Nodo con que comparar
     * @return Distancia
     * @author Alexander Straub
     */
    public double distanciaA(Nodo otro) {
        if (otro.getCoord().getX() != this.coord.getX()
                && otro.getCoord().getY() != this.coord.getY()) {
            return sqrt(2.0);
        }
        return 1.0;
    }
    
    /**
     * Return the value of the heuristic function for actual Nodo
     * @param goal goal Nodo
     * @return Heuristic value
     * @author Daniel Sánchez Alcaide y Antonio Troitiño del Río
     */
    public double h(Nodo goal){
        return sqrt(((this.getX()-goal.getX())*(this.getX()-goal.getX()))+((this.getY()-goal.getY())*(this.getY()-goal.getY())));
    }
    
    /**
     * Return the value of the consumption function for actual Nodo
     * @param start starting Nodo
     * @return Consumption value
     * @author Daniel Sánchez Alcaide
     */
    public double g(Nodo start){
        return sqrt(((this.getX()-start.getX())*(this.getX()-start.getX()))+((this.getY()-start.getY())*(this.getY()-start.getY())));
    }
    
    /**
     * Return the value of the f function for actual Nodo
     * @param start starting Nodo
     * @param goal goal Nodo
     * @return Consumption value
     * @author Daniel Sánchez Alcaide
     */
    public double f(Nodo start, Nodo goal){
        return this.g(start)+this.h(goal);
    }
}

