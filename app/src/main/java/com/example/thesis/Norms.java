package com.example.thesis;


public class Norms {
    private static int temperatureMin= 17;
    private static int temperatureMax= 30;
    private static int groundHumidityMin= 40;
    private static int groundHumidityMax= 80;
    private static int airHumidityMin= 50;
    private static int airHumidityMax= 75;
    private static int insolation= 50;

    public static int getTemperatureMax()
    {
        return temperatureMax;
    }
    public static void setTemperatureMax(int arg)
    {
        temperatureMax=arg;
    }

    public static int getTemperatureMin()
    {
        return temperatureMin;
    }
    public static void setTemperatureMin(int arg)
    {
        temperatureMin=arg;
    }

    public static int getGroundHumidityMin()
    {
        return groundHumidityMin;
    }
    public static void setGroundHumidityMin(int arg)
    {
        groundHumidityMin=arg;
    }

    public static int getGroundHumidityMax()
    {
        return groundHumidityMax;
    }
    public static void setGroundHumidityMax(int arg)
    {
        groundHumidityMax=arg;
    }

    public static int getAirHumidityMin()
    {
        return airHumidityMin;
    }
    public static void setAirHumidityMin(int arg)
    {
        airHumidityMin=arg;
    }

    public static int getAirHumidityMax()
    {
        return airHumidityMax;
    }
    public static void setAirHumidityMax(int arg)
    {
        airHumidityMax=arg;
    }

    public static int getInsolation()
    {
        return insolation;
    }
    public static void setInsolation(int arg)
    {
        insolation=arg;
    }

    public static boolean groundHumidityOk(int measure)
    {
        if((groundHumidityMin<measure)&&(measure<groundHumidityMax)) return true;
        return false;
    }

    public static boolean airHumidityOk(int measure)
    {
        if((airHumidityMin<measure)&&(measure<airHumidityMax)) return true;
        return false;
    }

    public static boolean temperatureOk(int measure)
    {
        if((temperatureMin<measure)&&(measure<temperatureMax)) return true;
        return false;
    }

    public static boolean insolationOk(int measure)
    {
        if(insolation<measure) return true;
        return false;
    }

}
