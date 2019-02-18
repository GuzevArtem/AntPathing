package edu.kpi.iasa.ai;

public class Utils {

    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value,max),min);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value,max),min);
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(Math.min(value,max),min);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value,max),min);
    }

    public static double toRange(double value, double min, double max) {
        return value*(max - min) + min;
    }

    public static int toRange(int value, int min, int max) {
        return value*(max - min) + min;
    }

    public static double fromRange(double value, double min, double max) {
        return (value-min)/(max-min);
    }

    public static double fromRange(int value, int min, int max) {
        return (value-min)/(double)(max-min);
    }

    public static double intRangedDoubleToFloatRange(int value, int min, int max, double mind, double maxd) {
        return toRange(fromRange(value,min,max), mind, maxd);
    }

    public static int doubleRangedValueToIntRange(double value, double min, double max, int mini, int maxi) {
        return (int) Math.round(toRange(fromRange(value,min,max),mini,maxi));
    }

    public static double lerp(double alpha, double a, double b){
        return b*alpha+(1-alpha)*a;
    }

    public static int lerp(double alpha, int a, int b){
        return (int)(b*alpha+(1-alpha)*a);
    }


}
