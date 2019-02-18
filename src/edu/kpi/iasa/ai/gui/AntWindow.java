package edu.kpi.iasa.ai.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

public class AntWindow {

    private JFrame frame;

    private LinkedList<JComponent> elements = new LinkedList<>();

    public void init(final String name, final int width, final int height) {
        init(name, width, height, new FlowLayout());
    }

    public void init(final String name, final int width, final int height, LayoutManager frameLayout) {
        // Create and set up a frame window
        frame = new JFrame(name);
        frame.setSize(width, height);
        frame.setLayout(frameLayout);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void add(final JComponent component) {
        elements.add(component);
    }

    public void add(final Collection<JComponent> components) {
        elements.addAll(components);
    }

    public void finish() {
        for(final JComponent elem : elements) {
            frame.add(elem);
        }
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public  LinkedList<JComponent> getComponents(){
        return elements;
    }

}
