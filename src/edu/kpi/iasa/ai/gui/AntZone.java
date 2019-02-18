package edu.kpi.iasa.ai.gui;

import edu.kpi.iasa.ai.model.AntWorker;

import javax.swing.*;
import java.awt.*;

public class AntZone extends JPanel{

    public static final Color BACKGROUND_COLOR = Color.GRAY;
    public static final Color BORDER_COLOR = Color.BLUE;

    private Rectangle antZoneSize;
    private int padding;

    private AntWorker worker;

    public AntZone(AntWorker worker) {
        this.antZoneSize = new Rectangle(0,0,500,500);
        this.worker = worker;
        init();
    }

    public AntZone(Rectangle antZoneSize, AntWorker worker) {
        this.antZoneSize = antZoneSize;
        this.worker = worker;
        init();
    }

    public AntZone(int x1, int y1, int x2, int y2, AntWorker worker) {
        this.antZoneSize = new Rectangle(x1,y1,x2-x1, y2-y1);
        this.worker = worker;
        init();
    }

    public AntZone(int padding, AntWorker worker) {
        this.antZoneSize = new Rectangle(padding,padding,500,500);
        this.padding = padding;
        this.worker = worker;
        init();
    }

    public AntZone(Rectangle antZoneSize, int padding, AntWorker worker) {
        this.antZoneSize = antZoneSize;
        this.padding = padding;
        this.worker = worker;
        init();
    }

    public AntZone(int x1, int y1, int x2, int y2, int padding) {
        this.antZoneSize = new Rectangle(x1,y1,x2-x1, y2-y1);
        this.padding = padding;
        init();
    }

    public void init() {
        this.setBounds(antZoneSize.x-padding,antZoneSize.y-padding,antZoneSize.width+padding,antZoneSize.height+padding);
        this.setSize(antZoneSize.width+2*padding,antZoneSize.height+2*padding);
        this.setMinimumSize(this.getSize());
        this.setPreferredSize(this.getSize());
    }

    @Override
    public void paint(Graphics g) {
        paintComponent(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setClip(antZoneSize.x-padding,antZoneSize.y-padding,antZoneSize.width+2*padding,antZoneSize.height+2*padding);

        g.setColor(Color.GRAY);
        g.drawRect(antZoneSize.x-padding,antZoneSize.y-padding,antZoneSize.width+2*padding-1,antZoneSize.height+2*padding-1);
        g.clearRect(antZoneSize.x, antZoneSize.y, antZoneSize.width, antZoneSize.height);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(antZoneSize.x, antZoneSize.y, antZoneSize.width, antZoneSize.height);

        g.setColor(BORDER_COLOR);
        g.drawRect (antZoneSize.x, antZoneSize.y, antZoneSize.width, antZoneSize.height);
        g.drawRect (antZoneSize.x-1, antZoneSize.y-1, antZoneSize.width+2, antZoneSize.height+2);

        worker.draw(g);
        //worker.drawBestPath(g);
    }

    public Rectangle getAntZoneSize() {
        return antZoneSize;
    }

    public void setAntZoneSize(Rectangle antZoneSize) {
        this.antZoneSize = antZoneSize;
    }

    public AntWorker getWorker() {
        return worker;
    }

    public void setWorker(AntWorker worker) {
        this.worker = worker;
    }
}
