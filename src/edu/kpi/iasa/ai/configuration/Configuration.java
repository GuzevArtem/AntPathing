package edu.kpi.iasa.ai.configuration;

import java.util.HashMap;

public class Configuration {

    //store values in double buffer
    //top buffer - for "uncommited" changes
    //bottom - for "rollback" purposes

    //todo: add backreferences to components to automate rollback?

    //todo: create single buffer for any value type?
    //todo:By storing type as additional parameter?

    private HashMap<String,Float> floatValues = new HashMap<>();
    private HashMap<String,Integer> intValues = new HashMap<>();

    private HashMap<String,Float> changedFloatValues = new HashMap<>();
    private HashMap<String,Integer> changedIntValues = new HashMap<>();

    public void setIntValue(String param, Integer value) {
        changedIntValues.put(param, value);
    }

    public void setFloatValue(String param, Float value) {
        changedFloatValues.put(param, value);
    }

    //returns only saved parameters
    public Integer getActualIntValue(String param) {
        return intValues.get(param);
    }

    //returns only saved parameters
    public Float getActualFloatValue(String param) {
        return floatValues.get(param);
    }

    public Integer getIntValue(String param) {
        Integer value = changedIntValues.get(param);
        if(value == null) {
            value = intValues.get(param);
        }
        return value;
    }

    public Float getFloatValue(String param) {
        Float value = changedFloatValues.get(param);
        if(value == null) {
            value = floatValues.get(param);
        }
        return value;
    }

    //all top overwrites bottom
    public void save() {
        intValues.putAll(changedIntValues);
        floatValues.putAll(changedFloatValues);
        changedIntValues.clear();
        changedFloatValues.clear();
    }

    //clear top
    public void cancel() {
        changedIntValues.clear();
        changedFloatValues.clear();
    }

}
