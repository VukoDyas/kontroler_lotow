package com.company;

import com.sun.javafx.geom.Vec2d;

/** Object's trajectory consists of waypoints,
 * each waypoint has precised 3 values: location,
 * height and velocity.
 *
 * @author Magdalena Sawicka
 * @see Vec2d
 */

public class waypoint {
    /**
     * Coordinates of <i>waypoint</i>, x and y values describing its position on the map.
     */
    public Vec2d coord;
    /**
     * Altitude measured in meters.
     */
    public double wysokosc;
    /**
     * Velocity measured in kilometers per hour.
     */
    public double predkosc;

    @Override
    public String toString(){
        return fun.round(coord.x, 2) + "   " + fun.round(coord.y, 2);
    }

    /**
     * Creates new waypoint with the given parameters.
     * @param c Location on the map.
     * @param h Altitude measured in meters.
     * @param v Velocity measured in kilometers per hour.
     */
    public waypoint(Vec2d c,double h,double v){
        coord = new Vec2d(c);
        wysokosc = h;
        predkosc = v;
    }

    /**
     * Creates new waypoint with the given parameters.
     * @param x Location's x value.
     * @param y Location's y value.
     * @param h Altitude measured in meters.
     * @param v Velocity measured in kilometers per hour.
     */
    public waypoint(double x, double y,double h,double v){
        coord = new Vec2d(x,y);
        wysokosc = h;
        predkosc = v;
    }
}
