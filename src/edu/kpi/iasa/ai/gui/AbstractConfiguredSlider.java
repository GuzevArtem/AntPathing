package edu.kpi.iasa.ai.gui;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public abstract class AbstractConfiguredSlider<T extends Object> extends JPanel implements ConfiguredSlider<T> {

    protected JSlider slider = new JSlider();

    protected JLabel label;

    protected JFormattedTextField textValue;

    protected int textFieldColumnCount;

    protected Consumer<T> changeValueListener;

    protected T valueMinimum;
    protected T valueMaximum;

    protected boolean isModifiable = true;

    @Override
    public ConfiguredSlider<T> finish() {
        //setup layout and adding drawable childs
        this.setLayout(new GridLayout(2,1));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.add(slider, BorderLayout.NORTH);
        panel.add(textValue, BorderLayout.SOUTH);
        label.setHorizontalAlignment(JLabel.CENTER);
        this.add(label);
        this.add(panel);
        return this;
    }

    @Override
    public void setChangeValueListener(Consumer<T> changeValueListener) {
        this.changeValueListener = changeValueListener;
    }

    @Override
    public Consumer<T> getChangeValueListener() {
        return changeValueListener;
    }

    @Override
    public JSlider getSlider() {
        return slider;
    }

    @Override
    public JTextField getTextField() {
        return textValue;
    }

    @Override
    public JComponent toJComponent() {
        return this;//Hack: class extends JPanel so it automatically converts to JComponent
    }

    @Override
    public void setModifiable(boolean state) {
        isModifiable = state;
        slider.setEnabled(state);
        textValue.setEditable(state);
    }

    @Override
    public boolean isModifiable() {
        return isModifiable;
    }
}
