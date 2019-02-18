package edu.kpi.iasa.ai.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.function.Consumer;

public class ConfiguredIntegerSlider extends AbstractConfiguredSlider<Integer> {

    public ConfiguredIntegerSlider(Consumer<Integer> changeValueListener) {
        this.changeValueListener = changeValueListener;
        this.textFieldColumnCount = 0;
        this.textValue = new JFormattedTextField(NumberFormat.getIntegerInstance());
    }

    public ConfiguredIntegerSlider(Consumer<Integer> changeValueListener, int textFieldColumnCount) {
        this.changeValueListener = changeValueListener;
        this.textFieldColumnCount = textFieldColumnCount;
        this.textValue = new JFormattedTextField(NumberFormat.getIntegerInstance());
    }


    @Override
    public ConfiguredIntegerSlider init(final String labelName, final Integer minimum, final Integer maximum, final Integer value) {

        return init(labelName, minimum, maximum, value, 0, 0, false, null,
                null, null);
    }

    @Override
    public ConfiguredIntegerSlider init(final String labelName, final Integer minimum, final Integer maximum, final Integer value,
                                        final int majorTicksSpacing, final int minorTicksSpacing,
                                        final boolean snapToTicks, final Hashtable<Integer, JLabel> ticks) {

        return init(labelName, minimum, maximum, value, majorTicksSpacing, minorTicksSpacing, snapToTicks, ticks,
                null, null);
    }

    @Override
    public ConfiguredIntegerSlider init(final String labelName, final Integer minimum, final Integer maximum, final Integer value,
                                        final ChangeListener sliderChangeListener, final PropertyChangeListener textValueChangeListener) {
        return init(labelName, minimum, maximum, value, 0, 0, false, null,
                sliderChangeListener, textValueChangeListener);
    }

    @Override
    public ConfiguredIntegerSlider init(final String labelName, final Integer minimum, final Integer maximum, final Integer value,
                                        final int majorTicksSpacing, final int minorTicksSpacing,
                                        final boolean snapToTicks, final Hashtable<Integer, JLabel> ticks,
                                        final ChangeListener sliderChangeListener, final PropertyChangeListener textValueChangeListener) {

        valueMinimum = minimum;
        valueMaximum = maximum;
        label = new JLabel(labelName, JLabel.LEFT);
        if(textFieldColumnCount <= 0) {
            textValue.setColumns(Math.max(("" + maximum).length(), 5));
        }
        else {
            textValue.setColumns(textFieldColumnCount);
        }
        textValue.addPropertyChangeListener(textValueChangeListener);

        slider.setMinimum(valueMinimum);
        slider.setMaximum(valueMaximum);

        slider.setValue(value);
        textValue.setValue(value);

        if (majorTicksSpacing > 0) {
            slider.setMajorTickSpacing(majorTicksSpacing);
        }

        if (minorTicksSpacing > 0) {
            slider.setMinorTickSpacing(minorTicksSpacing);
        }

        slider.setPaintTicks((minorTicksSpacing > 0 || majorTicksSpacing > 0));

        slider.setSnapToTicks(snapToTicks);

        if(ticks != null) {
            setTicksLabels(ticks);
        }

        // Add change listener to the slider

        //if slider value is changed
        slider.addChangeListener(
                sliderChangeListener == null ? e -> {
                    int newValue = ((JSlider) e.getSource()).getValue();
                    textValue.setValue(newValue);
                    changeValueListener.accept(newValue);
                }
                        : sliderChangeListener);

        //if JTextField value is changed
        textValue.addPropertyChangeListener(
                textValueChangeListener == null ? evt -> {
                    Object eventValue = ((JFormattedTextField) evt.getSource()).getValue();
                    Integer newValue;
                    if(eventValue instanceof Long) {
                        newValue = ((Long) eventValue).intValue();
                    }
                    else if(eventValue instanceof Integer) {
                        newValue = (Integer) eventValue;
                    }
                    else {
                        newValue = valueMinimum;
                    }
                    if(newValue < valueMinimum) {
                        newValue = valueMinimum;
                        textValue.setValue(valueMinimum);
                    }
                    else if(newValue > valueMaximum) {
                        newValue = valueMaximum;
                        textValue.setValue(valueMaximum);
                    }
                    slider.setValue(newValue);
                    changeValueListener.accept(newValue);
                }
                        : textValueChangeListener
        );

        return this;
    }

    public void setTicksLabels(Hashtable<Integer, JLabel> positions) {
        if (positions != null) {
            slider.setPaintLabels(true);
            // Set the label to be drawn
            slider.setLabelTable(positions);
        }
    }

    @Override
    public void setValue(Integer value) {
        if(value < valueMinimum) {
            value = valueMinimum;
        }
        else if(value > valueMaximum) {
            value = valueMaximum;
        }
        textValue.setValue(value);
        slider.setValue(value);
    }

    @Override
    public Integer getValue() {
        return slider.getValue();
    }

}
