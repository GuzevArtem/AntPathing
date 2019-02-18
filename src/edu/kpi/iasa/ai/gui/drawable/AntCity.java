package edu.kpi.iasa.ai.gui.drawable;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.gui.AntZone;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class AntCity implements Drawable {

    public static Color innerColor = Color.GRAY;
    public static Color outerColor = Color.BLACK;

    public static int radius = 6;
    public static int radiusInner = 3;

    private int id;

    //coords for draw in frame? coord system
    private Point coordsAbs = new Point();

    //coords in [0,1] for adaptive display
    public final Point2D.Double coords;

    //to get borders
    private AntZone parent;

    public AntCity(double x, double y, AntZone parent) {
        this.coords = new Point2D.Double(x,y);
        this.parent = parent;
        calcCoordsAbs();
    }

    public AntCity(Point2D.Double coords, AntZone parent) {
        this.coords = coords;
        this.parent = parent;
        calcCoordsAbs();
    }

    public AntCity(int id, double x, double y, AntZone parent) {
        this.id = id;
        this.coords = new Point2D.Double(x,y);
        this.parent = parent;
        calcCoordsAbs();
    }

    public AntCity(int id, Point2D.Double coords, AntZone parent) {
        this.id = id;
        this.coords = coords;
        this.parent = parent;
        calcCoordsAbs();
    }

    @Override
    public void draw(Graphics g) {
        calcCoordsAbs();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.BLACK);
        g2.drawString(""+id,coordsAbs.x-radius,coordsAbs.y-radius);
        g2.setColor(outerColor);
        Ellipse2D.Double circleOuter = new Ellipse2D.Double(coordsAbs.x-radius,coordsAbs.y-radius, 2*radius, 2*radius);
        g2.fill(circleOuter);
        g2.setColor(innerColor);
        Ellipse2D.Double circleInner = new Ellipse2D.Double(coordsAbs.x-radiusInner,coordsAbs.y-radiusInner, 2*radiusInner, 2*radiusInner);
        g2.fill(circleInner);
    }

    @Override
    public void update(Graphics g) {
        draw(g);
    }

    private void calcCoordsAbs(){
        //making abs coords relative to parent location
        coordsAbs.setLocation(
                Utils.doubleRangedValueToIntRange(coords.x,0.0,1.0,
                        0,//parent.getAntZoneSize().x,
                        //parent.getAntZoneSize().x+
                                parent.getAntZoneSize().width) + parent.getAntZoneSize().x,
                Utils.doubleRangedValueToIntRange(coords.y,0.0,1.0,
                        0,//parent.getAntZoneSize().y,
                        //parent.getAntZoneSize().y+
                                parent.getAntZoneSize().height) + parent.getAntZoneSize().y
                );
    }

    public double distance(AntCity city){
        //nevermind if distance isn't absolute, but relative value.
        // Could be changed just by multiplying on aspect koef.
        return coords.distance(city.coords);
    }

    public Point getCoordsAbs() {
        calcCoordsAbs();//lazy
        return coordsAbs;
    }

    public AntZone getParent() {
        return parent;
    }

    public void setParent(AntZone parent) {
        this.parent = parent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "AntCity{" +
                "coordsAbs=[" + coordsAbs.x + ", " + coordsAbs.y + "]" +
                ", coords=[" + coords.x + ", " + coords.y + "]"+
                '}';
    }
}
