package edu.kpi.iasa.ai.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.function.Consumer;

public class ConfiguredFloatSlider extends AbstractConfiguredSlider<Float> {

    private static final int SLIDER_MIN = 0;
    private static final int SLIDER_MAX = 10000000; //just enough for accuracy e^-3 on float range of 10^5

    public ConfiguredFloatSlider(Consumer<Float> changeValueListener) {
        this.changeValueListener = changeValueListener;
        this.textFieldColumnCount = 0;
        this.textValue = new JFormattedTextField(NumberFormat.getNumberInstance());
    }

    public ConfiguredFloatSlider(Consumer<Float> changeValueListener, int textFieldColumnCount) {
        this.changeValueListener = changeValueListener;
        this.textFieldColumnCount = textFieldColumnCount;
        this.textValue = new JFormattedTextField(NumberFormat.getNumberInstance());
    }

    @Override
    public ConfiguredFloatSlider init(final String labelName, final Float minimum, final Float maximum, final Float value) {

        return init(labelName, minimum, maximum, value, 0, 0, false, null,
                null, null);
    }

    @Override
    public ConfiguredFloatSlider  init(final String labelName, final Float minimum, final Float maximum, final Float value,
                     final int majorTicksSpacing, final int minorTicksSpacing,
                     final boolean snapToTicks, final Hashtable<Float, JLabel> ticks) {

        return init(labelName, minimum, maximum, value, majorTicksSpacing, minorTicksSpacing, snapToTicks, ticks,
                null, null);
    }

    @Override
    public ConfiguredFloatSlider init(final String labelName, final Float minimum, final Float maximum, final Float value,
                     final ChangeListener sliderChangeListener, final PropertyChangeListener textValueChangeListener) {
        return init(labelName, minimum, maximum, value, 0, 0, false, null,
                sliderChangeListener, textValueChangeListener);
    }

    @Override
    public ConfiguredFloatSlider init(final String labelName, final Float minimum, final Float maximum, final Float value,
                     final int majorTicksSpacing, final int minorTicksSpacing,
                     final boolean snapToTicks, final Hashtable<Float, JLabel> ticks,
                     final ChangeListener sliderChangeListener, final PropertyChangeListener textValueChangeListener) {

        valueMinimum = minimum;
        valueMaximum = maximum;

        label = new JLabel(labelName, JLabel.CENTER);
        if(textFieldColumnCount <= 0) {
            textValue.setColumns(Math.max(("" + maximum).length(), 5));
        }
        else {
            textValue.setColumns(textFieldColumnCount);
        }
        textValue.addPropertyChangeListener(textValueChangeListener);

        slider.setMinimum(SLIDER_MIN);
        slider.setMaximum(SLIDER_MAX);

        slider.setValue(realFloatToSliderInt(value));
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

        slider.addChangeListener(
                sliderChangeListener == null ? e -> { //todo
                    float newValue = sliderIntToRealFloat(((JSlider) e.getSource()).getValue());
                    textValue.setValue(newValue);
                    changeValueListener.accept(newValue);
                }
                        : sliderChangeListener);

        textValue.addPropertyChangeListener(
                textValueChangeListener == null ? evt -> { //todo
                    Object eventValue = ((JFormattedTextField) evt.getSource()).getValue();
                    Float newValue;
                    if(eventValue instanceof Long) {
                        newValue = ((Long) eventValue).floatValue();
                    }
                    else if(eventValue instanceof Integer) {
                        newValue = ((Integer) eventValue).floatValue();
                    }
                    else if(eventValue instanceof Double) {
                        newValue = ((Double) eventValue).floatValue();
                    }
                    else if(eventValue instanceof Float) {
                        newValue = (Float) eventValue;
                    }
                    else if(eventValue instanceof Number) {
                        newValue = ((Number) eventValue).floatValue();
                    }
                    else {
                        newValue = minimum;
                    }
                    if(newValue < minimum) {
                        newValue = minimum;
                        textValue.setValue(minimum);
                    }
                    else if(newValue > maximum) {
                        newValue = maximum;
                        textValue.setValue(maximum);
                    }
                    slider.setValue(realFloatToSliderInt(newValue));
                    changeValueListener.accept(newValue);
                }
                        : textValueChangeListener
        );
        return this;
    }

    private float sliderIntToRealFloat(int value) {
        return (value/(float)SLIDER_MAX)*(valueMaximum - valueMinimum) + valueMinimum;
    }

    private int realFloatToSliderInt(float value) {
        return Math.round((value - valueMinimum)/(valueMaximum - valueMinimum)*SLIDER_MAX);
    }

    public void setTicksLabels(Hashtable<Float, JLabel> positions) {
        if (positions != null) {
            slider.setPaintLabels(true);
            // Set the label to be drawn
            slider.setLabelTable(positions);
        }
    }

    @Override
    public void setValue(Float value) {
        if(value < valueMinimum) {
            value = valueMinimum;
        }
        else if(value > valueMaximum) {
            value = valueMaximum;
        }
        textValue.setValue(value);
        slider.setValue(realFloatToSliderInt(value));
    }

    @Override
    public Float getValue() {
        return sliderIntToRealFloat(slider.getValue());
    }

}
