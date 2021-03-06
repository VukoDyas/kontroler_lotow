package com.company;

import com.sun.javafx.geom.Vec2d;
import java.util.LinkedList;
import java.util.ArrayList;
import static com.company.fun.*;


/**
 * Statek objects represent real
 * objects flying over the map.
 *
 * @author Daniel Skórczyński
 * @author Magdalena Sawicka
 * @author Paweł Raglis
 */
public abstract class statek {
    /**
     * Identification number.
     */
    public int id;
    /**
     * Object's trajectory.
     */
    public LinkedList<waypoint> trasa;
    /**
     * Current position.
     */
    private Vec2d pozycja;
    /**
     * Current altitude measured in meters.
     */
    private double wysokosc;
    /**
     * Current velocity measured in kilometers per hour.
     */
    private double predkosc;
    /**
     * Waypoint that object is currently heading for.
     */
    private int n_kurs;
    /**
     * This method simulates flight of the object.
     * It changes its parameters and position accordingly.
     * @param time Delta time.
     * @throws IllegalArgumentException if time is negative or equal zero
     * @author Magdalena Sawicka
     */
    public void lec(double time) throws IllegalArgumentException{
        if(time <= 0) throw new IllegalArgumentException("Time cannot be negative");

        if(in_bounds(DLUGOSCMAPY,SZEROKOSCMAPY,pozycja)||trasa.size()-n_kurs != 1)
        {

            waypoint s = new waypoint(trasa.get(n_kurs-1).coord,trasa.get(n_kurs-1).wysokosc,
                    trasa.get(n_kurs-1).predkosc);
            s.coord = new Vec2d(pozycja);

            waypoint e = trasa.get(n_kurs);

            predkosc = (s.predkosc+e.predkosc)/2;
            wysokosc = (s.wysokosc+e.wysokosc)/2;


            double droga = predkosc * time * 10/36; // odleglosc pokonana przez statek w czasie 'time' wyrazona w metrach
            double piksele = droga / 500; // droga wyrazona w pikselach, 1 piksel = 500m
            double skala = piksele / vector_length(s.coord, e.coord);

            if (vector_length(pozycja, e.coord) < piksele) {
                double l = vector_length(pozycja, e.coord);

                s = trasa.get(n_kurs);
                pozycja = s.coord;
                if (trasa.size()-trasa.indexOf(s) >= 2) {
                    n_kurs = n_kurs + 1;
                    e = trasa.get(n_kurs);
                    skala = (piksele - l) / vector_length(s.coord, e.coord);
                    pozycja = shift_vector(pozycja, new Vec2d(skala * (e.coord.x - s.coord.x), skala * (e.coord.y - s.coord.y)));
                }
            } else pozycja = shift_vector(pozycja, new Vec2d(skala * (e.coord.x - s.coord.x), skala * (e.coord.y - s.coord.y)));
        }
    }
    /**
     * This method generates randomly arc-shaped trajectory
     * for the object.
     * @param v Object's average velocity measured in kilometers per hour.
     * @param h Object's average altitude measured in meters.
     * @param dlugosc Map's height value.
     * @param szerokosc Map's width value.
     * @param budynki List of the buildings placed on the map.
     * @throws IllegalArgumentException if constants values are negative or equal zero
     * @author Daniel Skórczyński
     */
    private void losujTrase(double v, double h, double dlugosc, double szerokosc, ArrayList<budynek> budynki) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dlugosc <= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( szerokosc <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        double randWys = rand_avg(h, 0.1);
        double randPred = rand_avg(v, 0.1);
        int odcinek = (int) Math.floor(dlugosc / 8), i = 1;

        trasa.add(new waypoint(safe_place(dlugosc,szerokosc,randWys,budynki), randWys, randPred));
        double sgn = rand_sgn();

        trasa.add(new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)),rand_avg(h, 0.1),  rand_avg(v, 0.1)));

        while ( in_bounds( dlugosc, szerokosc, trasa.get(i).coord )) {
            Vec2d dxdy = new Vec2d( create_shift_vector(trasa.get(i - 1).coord, trasa.get(i).coord) );
            Vec2d nowa_pozycja = shift_vector(trasa.get(i).coord, dxdy, Math.toRadians(normalize_radius(sgn)));
            trasa.add(new waypoint(nowa_pozycja, rand_avg(h, 0.1),rand_avg(v, 0.1))); ++i;
        }
    }
    /**
     * This method generates randomly arc-shaped trajectory
     * for the object.
     * @param v Object's average velocity measured in kilometers per hour.
     * @param h Object's average altitude measured in meters.
     * @param dlugosc Map's height value.
     * @param szerokosc Map's width value.
     * @param budynki List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @throws IllegalArgumentException if constants values are negative or equal zero
     * @author Daniel Skórczyński
     */
    private void losujTrase(double v, double h, double dlugosc, double szerokosc, ArrayList<budynek> budynki, double variation) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dlugosc <= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( szerokosc <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        double randWys = rand_avg(h, variation);
        double randPred = rand_avg(v, variation);
        int odcinek = (int) Math.floor(dlugosc / 8), i = 1;

        trasa.add(new waypoint(safe_place(dlugosc,szerokosc,randWys,budynki), randWys, randPred));
        double sgn = rand_sgn();
        trasa.add(new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)), rand_avg(h, variation), rand_avg(v, variation)));

        while ( in_bounds( dlugosc, szerokosc, trasa.get(i).coord )) {
            Vec2d dxdy = new Vec2d( create_shift_vector(trasa.get(i - 1).coord, trasa.get(i).coord) );
            Vec2d nowa_pozycja = shift_vector(trasa.get(i).coord, dxdy, Math.toRadians(normalize_radius(sgn)));
            trasa.add(new waypoint(nowa_pozycja, rand_avg(h, variation),rand_avg(v, variation))); ++i;
        }
    }

    /**
     * This method generates randomly arc-shaped trajectory
     * for the object.
     * @param v Object's average velocity measured in kilometers per hour.
     * @param h Object's average altitude measured in meters.
     * @param dlugosc Map's height value.
     * @param szerokosc Map's width value.
     * @param budynki List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     * @author Daniel Skórczyński
     * @throws IllegalArgumentException if constants values are negative or equal zero
     */
    private void losujTrase(double v, double h, double dlugosc, double szerokosc, ArrayList<budynek> budynki,double variation,boolean przylatuje) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dlugosc <= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( szerokosc <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        double randWys = rand_avg(h, variation);
        double randPred = rand_avg(v, variation);
        int odcinek = (int) Math.floor(dlugosc / 8), i = 1; double sgn;

        if (przylatuje){
            trasa.add(new waypoint(safe_place(dlugosc,szerokosc,randWys,budynki,true),randWys,randPred));
            sgn = rand_sgn();
            trasa.add(new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)), rand_avg(h, variation), rand_avg(v, variation)));
            while(!in_bounds(new Vec2d(dlugosc,szerokosc),trasa.getLast().coord)){
                sgn = rand_sgn();
                trasa.set(trasa.indexOf(trasa.getLast()),
                        new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)), rand_avg(h, variation),rand_avg(v, variation)));
            }
        } else {
            trasa.add(new waypoint(safe_place(dlugosc,szerokosc,randWys,budynki), rand_avg(h, variation), rand_avg(v, variation)));
            sgn = rand_sgn();
            trasa.add(new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)), rand_avg(h, variation), rand_avg(v, variation)));
        }

        while ( in_bounds( dlugosc, szerokosc, trasa.get(i).coord )) {
            Vec2d dxdy = new Vec2d( create_shift_vector(trasa.get(i - 1).coord, trasa.get(i).coord) );
            Vec2d nowa_pozycja = shift_vector(trasa.get(i).coord, dxdy, Math.toRadians(normalize_radius(sgn)));
            trasa.add(new waypoint(nowa_pozycja, rand_avg(h, variation), rand_avg(v, variation))); ++i;
        }
    }


    /**
     *This method generates randomly arc-shaped trajectory
     * for the object.
     *
     * @param v Object's average velocity measured in kilometers per hour.
     * @param h Object's average altitude measured in meters.
     * @param dlugosc Map's height value.
     * @param szerokosc Map's width value.
     * @param budynki List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     * @throws IllegalArgumentException if constants values are negative or equal zero
     * @author Daniel Skórczyński
     */
    private void losujTrase(double v, double h, double dlugosc, double szerokosc, ArrayList<budynek> budynki,boolean przylatuje) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dlugosc <= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( szerokosc <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        double randWys = rand_avg(h, 0.1);
        double randPred = rand_avg(v, 0.1);
        int odcinek = (int) Math.floor(dlugosc / 8), i = 1; double sgn;

        if (przylatuje){
            trasa.add(new waypoint(safe_place(dlugosc,szerokosc,randWys,budynki,true),randWys,randPred));
            sgn = rand_sgn();
            trasa.add(new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)), rand_avg(h, 0.1),rand_avg(v, 0.1)));
            while(!in_bounds(new Vec2d(dlugosc,szerokosc),trasa.getLast().coord)){
                sgn = rand_sgn();
                trasa.set(trasa.indexOf(trasa.getLast()),
                        new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)), rand_avg(h, 0.1), rand_avg(v, 0.1)));
            }
        } else {
            trasa.add(new waypoint(safe_place(dlugosc,szerokosc,randWys,budynki), rand_avg(h, 0.1),rand_avg(v, 0.1)));
            sgn = rand_sgn();
            trasa.add(new waypoint((rand_point_on_ring(trasa.getFirst().coord, odcinek, sgn)),rand_avg(h, 0.1),rand_avg(v, 0.1)));
        }

        while ( in_bounds( dlugosc, szerokosc, trasa.get(i).coord )) {
            Vec2d dxdy = new Vec2d( create_shift_vector(trasa.get(i - 1).coord, trasa.get(i).coord) );
            Vec2d nowa_pozycja = shift_vector(trasa.get(i).coord, dxdy, Math.toRadians(normalize_radius(sgn)));
            trasa.add(new waypoint(nowa_pozycja, rand_avg(h, 0.1), rand_avg(v, 0.1))); ++i;
        }
    }

    /* Sprawdzanie kolizji  */
    /**
     * This method checks if the given object collides
     * with any building or with any other object on the map.
     *
     * @param P Object's current position.
     * @param h Object's current altitude measured in meters.
     * @return True if there is a collision.
     * @throws IllegalArgumentException if height is negative
     * @author Daniel Skórczyński
     */
    public boolean kolizja(Vec2d P, double h) throws IllegalArgumentException{
        if( h < 0 ) throw new IllegalArgumentException("Height cannot be negative");
        if(P.distance(this.pozycja) <= 10 && Math.abs(h - this.wysokosc) <= 10)
            return true;
        else
            return false;
    }

    /* Sprawdzanie zagrozen */
    /**
     * This method checks if the given object is in the danger
     * of collision with any building or with any other object on the map.
     *
     * @param P Object's current position.
     * @param h Object's current altitude measured in meters.
     * @return True if there is danger of collision.
     * @throws IllegalArgumentException if height is negative
     * @author Daniel Skórczyński
     */
    public boolean zagrozenie(Vec2d P, double h) throws IllegalArgumentException{
        if( h < 0 ) throw new IllegalArgumentException("Height cannot be negatvie");
        if(P.distance(this.pozycja) > 10 && P.distance(this.pozycja) <= 30 &&
                Math.abs(h - this.wysokosc) > 10 && Math.abs(h - this.wysokosc) <= 30)
            return true;
        else
            return false;
    }

    /* Zmiana położenia następnego waypointu */
    /**
     * Changes location of the waypoint that given object is
     * heading for.
     * @param przesuniecie Shift's x and y values.
     * @author Daniel Skórczyński
     */
    public void zmienKierunek(Vec2d przesuniecie){
        int i = n_kurs;
        double x = trasa.get(i).coord.x;
        double y = trasa.get(i).coord.y;
        double vel = trasa.get(i).predkosc;
        double h = trasa.get(i).wysokosc;
        x = x + przesuniecie.x; y = y + przesuniecie.y;
        trasa.set(i,new waypoint(x,y,h,vel));
    }

    /* Zmiana całej trasy zaczynając od następnego punktu o dany KĄT */
    /**
     * Changes location of the waypoint that given object is
     * heading for.
     * @param kat Shift's angle.
     * @author Daniel Skórczyński
     */
    public void zmienKierunek(double kat){
        int i = n_kurs;
        int last = trasa.indexOf(trasa.getLast());
        for(int j = i; j < last; j++){
            trasa.set(j,new waypoint(new Vec2d(rotate(trasa.get(j-1).coord,trasa.get(j).coord,Math.toRadians(kat))),
                    trasa.get(j).wysokosc,trasa.get(j).predkosc));
            if(j == i) {
                //kurs = trasa.get(j);
                n_kurs = trasa.indexOf(trasa.get(j));
            }
        }
    }

    /* Funkcje GET */
    /**
     * @return Object's current position.
     */
    public Vec2d getPozycja(){return pozycja;}
    /**
     * @return Object's current altitude measured in meters.
     */
    public double getWysokosc(){return wysokosc;}

    /**
     * @return Object's current velocity measured in kilometers per hour.
     */
    public double getPredkosc(){return predkosc;}

    /**
     * @return Waypoint that given object is
     * heading for.
     */
    public int getN_kurs(){return n_kurs;}


    /* Konstruktory */
    /**
     * Creates new object with the given parameters.
     *
     * @param i Identification number.
     * @param v Average  velocity measured in kilometers per hour.
     * @param h Average altitude measured in meters.
     * @param dl Map's height value.
     * @param sz Map's width value.
     * @param bud List of the buildings placed on the map.
     * @throws IllegalArgumentException if given parameters are negative or equal zero
     * @author Magdalena Sawicka
     */
    public statek(int i, double v, double h, double dl, double sz, ArrayList<budynek> bud) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dl <= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( sz <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        id = i;
        trasa = new LinkedList<waypoint>();
        losujTrase(v, h,dl,sz,bud);
        pozycja = trasa.getFirst().coord;
        wysokosc = trasa.getFirst().wysokosc;
        predkosc = trasa.getFirst().predkosc;
        n_kurs = 1;
    }
    /**
     * Creates new object with the given parameters.
     *
     * @param i Identification number.
     * @param v Average  velocity measured in kilometers per hour.
     * @param h Average altitude measured in meters.
     * @param dl Map's height value.
     * @param sz Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @throws IllegalArgumentException if given parameters are negative or equal zero
     * @author Magdalena Sawicka
     */
    public statek(int i, double v, double h, double dl, double sz, ArrayList<budynek> bud, double variation) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dl<= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( sz <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        id = i;
        trasa = new LinkedList<waypoint>();
        losujTrase(v, h,dl,sz,bud,variation);
        pozycja = trasa.getFirst().coord;
        wysokosc = trasa.getFirst().wysokosc;
        predkosc = trasa.getFirst().predkosc;
        n_kurs = 1;
    }
    /**
     * Creates new object with the given parameters.
     *
     * @param i Identification number.
     * @param v Average  velocity measured in kilometers per hour.
     * @param h Average altitude measured in meters.
     * @param dl Map's height value.
     * @param sz Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     * @throws IllegalArgumentException if given parameters are negative or equal zero
     * @author Magdalena Sawicka
     */
    public statek(int i, double v, double h, double dl, double sz, ArrayList<budynek> bud, boolean przylatuje) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dl <= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( sz <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        id = i;
        trasa = new LinkedList<waypoint>();
        losujTrase(v, h,dl,sz,bud,przylatuje);
        pozycja = trasa.getFirst().coord;
        wysokosc = trasa.getFirst().wysokosc;
        predkosc = trasa.getFirst().predkosc;
        n_kurs = 1;
    }
    /**
     * Creates new object with the given parameters.
     *
     * @param i Identification number.
     * @param v Average  velocity measured in kilometers per hour.
     * @param h Average altitude measured in meters.
     * @param dl Map's height value.
     * @param sz Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     * @throws IllegalArgumentException if given parameters are negative or equal zero
     * @author Magdalena Sawicka
     */
    public statek(int i, double v, double h, double dl, double sz, ArrayList<budynek> bud, double variation, boolean przylatuje) throws IllegalArgumentException{
        if( v <= 0 ) throw new IllegalArgumentException("Velocity cannot be equal zero or less");
        if( h <= 0 ) throw new IllegalArgumentException("Height cannot be equal zero or less");
        if( dl<= 0 ) throw new IllegalArgumentException("Long cannot be equal zero or less");
        if( sz <= 0 ) throw new IllegalArgumentException("Width cannot be equal zero or less");
        id = i;
        trasa = new LinkedList<waypoint>();
        losujTrase(v, h,dl,sz,bud,variation,przylatuje);
        pozycja = trasa.getFirst().coord;
        wysokosc = trasa.getFirst().wysokosc;
        predkosc = trasa.getFirst().predkosc;
        n_kurs = 1;
    }

    public statek(statek s){
        this.id = s.id;
        this.trasa = s.trasa;
        this.pozycja = s.pozycja;
        this.wysokosc = s.wysokosc;
        this.predkosc = s.predkosc;
        this.n_kurs = s.n_kurs;
    }
}


/**
 * Samolot is one of the types of <i>statek</i>.
 * @author Magdalena Sawicka
 */
class samolot extends statek{

    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     */
    public samolot(int i, double dlug, double szer, ArrayList<budynek> bud){
        super(i,900.0, 11000.0,dlug,szer,bud);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     */
    public samolot(int i, double dlug, double szer, ArrayList<budynek> bud, double variation){
        super(i,900.0, 11000.0,dlug,szer,bud,variation);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public samolot(int i, double dlug, double szer, ArrayList<budynek> bud, boolean przylatuje){
        super(i,900.0, 11000.0,dlug,szer,bud,przylatuje);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public samolot(int i, double dlug, double szer, ArrayList<budynek> bud, double variation, boolean przylatuje){
        super(i,900.0, 11000.0,dlug,szer,bud,variation,przylatuje);
    }

    public samolot(samolot s){
        super(s);
    }
}

/**
 * Helikopter is one of the types of <i>statek</i>.
 * @author Magdalena Sawicka
 */
class helikopter extends statek{

    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     */
    public helikopter(int i, double dlug, double szer, ArrayList<budynek> bud){
        super(i,300.0, 5100.0,dlug,szer,bud);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     */
    public helikopter(int i, double dlug, double szer, ArrayList<budynek> bud,double variation){
        super(i,300.0, 5100.0,dlug,szer,bud,variation);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public helikopter(int i, double dlug, double szer, ArrayList<budynek> bud,boolean przylatuje){
        super(i,300.0, 5100.0,dlug,szer,bud,przylatuje);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public helikopter(int i, double dlug, double szer, ArrayList<budynek> bud, double variation, boolean przylatuje){
        super(i,300.0, 5100.0,dlug,szer,bud,variation,przylatuje);
    }

    public helikopter(helikopter h){
        super(h);
    }
}

/**
 * Szybowiec is one of the types of <i>statek</i>.
 * @author Magdalena Sawicka
 */
class szybowiec extends statek{

    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     */
    public szybowiec(int i, double dlug, double szer, ArrayList<budynek> bud){
        super(i,300.0, 14000.0,dlug,szer,bud);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     */
    public szybowiec(int i, double dlug, double szer, ArrayList<budynek> bud, double variation){
        super(i,300.0, 14000.0,dlug,szer,bud,variation);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public szybowiec(int i, double dlug, double szer, ArrayList<budynek> bud, boolean przylatuje){
        super(i,300.0, 14000.0,dlug,szer,bud,przylatuje);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public szybowiec(int i, double dlug, double szer, ArrayList<budynek> bud,double variation, boolean przylatuje){
        super(i,300.0, 14000.0,dlug,szer,bud,variation,przylatuje);
    }

    public szybowiec(szybowiec sz){
        super(sz);
    }
}

/**
 * Sterowiec is one of the types of <i>statek</i>.
 * @author Magdalena Sawicka
 */
class sterowiec extends statek{

    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     */
    public sterowiec(int i, double dlug, double szer, ArrayList<budynek> bud){
        super(i,125.0, 2600.0,dlug,szer,bud);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     */
    public sterowiec(int i, double dlug, double szer, ArrayList<budynek> bud, double variation){
        super(i,125.0, 2600.0,dlug,szer,bud,variation);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public sterowiec(int i, double dlug, double szer, ArrayList<budynek> bud, boolean przylatuje){
        super(i,125.0, 2600.0,dlug,szer,bud,przylatuje);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public sterowiec(int i, double dlug, double szer, ArrayList<budynek> bud, double variation, boolean przylatuje){
        super(i,125.0, 2600.0,dlug,szer,bud,variation,przylatuje);
    }

    public sterowiec(sterowiec s){
        super(s);
    }
}

/**
 * Balon is one of the types of <i>statek</i>.
 * @author Magdalena Sawicka
 */
class balon extends statek{

    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     */
    public balon(int i, double dlug, double szer, ArrayList<budynek> bud){
        super(i,20.0, 600.0,dlug,szer,bud);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     */
    public balon(int i, double dlug, double szer, ArrayList<budynek> bud, double variation){
        super(i,20.0, 600.0,dlug,szer,bud,variation);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public balon(int i, double dlug, double szer, ArrayList<budynek> bud, boolean przylatuje){
        super(i,20.0, 600.0,dlug,szer,bud,przylatuje);
    }
    /**
     * Creates new object with the given parameters.
     * @param i Identification number.
     * @param dlug Map's height value.
     * @param szer Map's width value.
     * @param bud List of the buildings placed on the map.
     * @param variation The deviation from the mean.
     * @param przylatuje Determines object's starting position. If the value is true, first waypoint
     *                   is placed on the map's x or y axis - else it can be placed randomly anywhere on
     *                   the map.
     */
    public balon(int i, double dlug, double szer, ArrayList<budynek> bud, double variation, boolean przylatuje){
        super(i,20.0, 600.0,dlug,szer,bud,variation,przylatuje);
    }

    public balon(balon b){
        super(b);
    }
}

