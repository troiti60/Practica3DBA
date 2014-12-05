/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.Vector;

/**
 * Representa coordenadas en 2D
 *
 * @author Alexander Straub, Antonio Troitiño
 */
public class Coord {

    /**
     * Coordenadas
     */
    private int x, y;

    /**
     * Getter devolviendo la coordenada X
     *
     * @return Coordenada X
     * @author Alexander Straub
     */
    public int getX() {
        return this.x;
    }

    /**
     * Setter parar guardar un nuevo valor de X
     *
     * @param newX Nuevo valor de X
     * @author Alexander Straub
     */
    public void setX(int newX) {
        this.x = newX;
    }

    /**
     * Getter devolviendo la coordenada Y
     *
     * @return Coordenada Y
     * @author Alexander Straub
     */
    public int getY() {
        return this.y;
    }

    /**
     * Setter parar guardar un nuevo valor de Y
     *
     * @param newY Nuevo valor de Y
     * @author Alexander Straub
     */
    public void setY(int newY) {
        this.y = newY;
    }

    /**
     * Constructor
     *
     * @param x Coordenada X
     * @param y Coordenada Y
     * @author Alexander Straub
     */
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy constructor
     *
     * @param copy Crear nuevas coordenadas de copiando
     * @author Alexander Straub
     */
    public Coord(Coord copy) {
        this.x = copy.getX();
        this.y = copy.getY();
    }

    /**
     * Devolver la coordenada en el norte
     *
     * @return Coordenada en el norte
     * @author Alexander Straub
     */
    public Coord N() {
        return new Coord(x, y - 1);
    }

    /**
     * Devolver la coordenada en el este
     *
     * @return Coordenada en el este
     * @author Alexander Straub
     */
    public Coord E() {
        return new Coord(x + 1, y);
    }

    /**
     * Devolver la coordenada en el sur
     *
     * @return Coordenada en el sur
     * @author Alexander Straub
     */
    public Coord S() {
        return new Coord(x, y + 1);
    }

    /**
     * Devolver la coordenada en el oeste
     *
     * @return Coordenada en el oeste
     * @author Alexander Straub
     */
    public Coord O() {
        return new Coord(x - 1, y);
    }

    /**
     * Devolver la coordenada en el noreste
     *
     * @return Coordenada en el noreste
     * @author Alexander Straub
     */
    public Coord NE() {
        return new Coord(x + 1, y - 1);
    }

    /**
     * Devolver la coordenada en el sureste
     *
     * @return Coordenada en el sureste
     * @author Alexander Straub
     */
    public Coord SE() {
        return new Coord(x + 1, y + 1);
    }

    /**
     * Devolver la coordenada en el suroeste
     *
     * @return Coordenada en el suroeste
     * @author Alexander Straub
     */
    public Coord SO() {
        return new Coord(x - 1, y + 1);
    }

    /**
     * Devolver la coordenada en el noroeste
     *
     * @return Coordenada en el noroeste
     * @author Alexander Straub
     */
    public Coord NO() {
        return new Coord(x - 1, y - 1);
    }

    /**
     * Calcular la distancia entre las coordenadas
     *
     * @param otro Coordenadas con que comparar
     * @return Distancia entre las coordenadas
     * @author Alexander Straub
     */
    public double distanciaA(Coord otro) {
        return sqrt(pow(this.x - otro.getX(), 2.0) + pow(this.y - otro.getY(), 2.0));
    }

    /**
     * Comparar este objeto con otro, devolviendo true si las coordenadas
     * coinciden
     *
     * @param otro Objeto con que comparar
     * @return True si las coordenadas coinciden
     * @author Alexander Straub
     */
    @Override
    public boolean equals(Object otro) {
        if (otro == null) {
            return false;
        }
        if (otro == this) {
            return true;
        }
        if (!(otro instanceof Coord)) {
            return false;
        }

        Coord segundo = (Coord) otro;
        return (this.x == segundo.getX()
                && this.y == segundo.getY());
    }

    /**
     * Crear un hash code
     *
     * @return Hash code
     * @author Antonio Troitiño
     */
    @Override
    public int hashCode() {
        return (this.x + (this.y * 1000));
    }

    /**
     * Sustracción de dos coordenadas
     *
     * @param otro Otros coordenadas
     * @return Vector
     * @author Alexander Straub
     */
    public Vector sub(Coord otro) {
        int deltaX = this.x - otro.getX();
        int deltaY = this.y - otro.getY();
        double norm = Math.sqrt((double) (deltaX * deltaX + deltaY * deltaY));
        double scale = 0.5 / norm;

        Vector vec = new Vector(2);
        vec.add((double) deltaX * scale);
        vec.add((double) deltaY * scale);

        return vec;
    }

    /**
     * Adición con un vector
     *
     * @param vec Vector
     * @return Suma
     * @author Alexander Straub
     */
    public Coord add(Vector vec) {
        int newX = (int) Math.round((double) this.x + (double) vec.get(0));
        int newY = (int) Math.round((double) this.y + (double) vec.get(1));

        return new Coord(newX, newY);
    }

}