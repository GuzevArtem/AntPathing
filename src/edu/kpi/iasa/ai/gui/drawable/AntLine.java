package edu.kpi.iasa.ai.gui.drawable;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.gui.AntZone;

import java.awt.*;

public class AntLine implements Drawable{

    public Color pheromoneDiffuse = new Color(214,142,0);

    private AntCity from;
    private AntCity to;

    private double saturation;

    public static int width = 2;

    public AntLine(AntCity from, AntCity to) {
        this.from = from;
        this.to = to;
    }

    public AntLine(AntCity from, AntCity to, double saturation) {
        this.from = from;
        this.to = to;
        this.saturation = saturation;
    }

    @Override
    public void draw(Graphics graphics) {
        saturation = Utils.clamp(saturation, 0.0, 1.0);

        int r = (pheromoneDiffuse.getRed());
        int g = (pheromoneDiffuse.getGreen());
        int b = (pheromoneDiffuse.getBlue());
        //todo: use transition to background color
        //int rbg = (AntZone.BACKGROUND_COLOR.getRed());
        //int gbg = (AntZone.BACKGROUND_COLOR.getGreen());
        //int bbg = (AntZone.BACKGROUND_COLOR.getBlue());
        Color c = new Color(
                r,//Utils.clamp(Utils.lerp(1.0-saturation,r,rbg),0,255),
                g,//Utils.clamp(Utils.lerp(1.0-saturation,g,gbg),0,255),
                b,//Utils.clamp(Utils.lerp(1.0-saturation,b,bbg),0,255),
                Utils.lerp(saturation,0,255) //todo: use visibility parameter
        );
        Graphics2D g2 = (Graphics2D) graphics;
        Stroke defaultStroke = g2.getStroke();

        g2.setStroke(new BasicStroke(width));
        g2.setColor(c);
        g2.drawLine(from.getCoordsAbs().x, from.getCoordsAbs().y,
                    to.getCoordsAbs().x, to.getCoordsAbs().y);

        g2.setStroke(defaultStroke);//don't affect custom Strokes
    }

    @Override
    public void update(Graphics g) {
        draw(g);
        //redraw cities to be on top
        from.draw(g);
        to.draw(g);
    }

    public AntCity getFromCity() {
        return from;
    }

    public void setFromCity(AntCity from) {
        this.from = from;
    }

    public AntCity getToCity() {
        return to;
    }

    public void setToCity(AntCity to) {
        this.to = to;
    }

    public double getSaturation() {
        return saturation;
    }

    public void setSaturation(double saturation) {
        this.saturation = saturation;
    }

    public double length(){
        return from.coords.distance(to.coords);
    }

    //"dummy" edge could be used with cities as null
    public static class EndPointLine extends AntLine {

        public EndPointLine(){
            super(null,null,0);
        }

        private EndPointLine(AntCity from, AntCity to) {
            super(from, to);
        }

        private EndPointLine(AntCity from, AntCity to, double saturation) {
            super(from, to, saturation);
        }

        //prevent draw
        @Override
        public void draw(Graphics g) {}

        //prevent update
        @Override
        public void update(Graphics g) {}

        //hack?:make line length +inf to prevent using in algorithm
        //todo:might not be used. remove?
        @Override
        public double length() {
            return Double.POSITIVE_INFINITY;
        }
    }
}
