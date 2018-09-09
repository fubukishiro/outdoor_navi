package com.example.fubuki.outdoor_navigation;

public class Point {
    private double x;
    private double y;
    private int pointNumber;

    Point()
    {
        x = 0;
        y = 0;
        pointNumber = 0;
    }
    Point(double px,double py)
    {
        x = px;
        y = py;
        pointNumber = 0;
    }
    Point(double px,double py,int pPointNumber)
    {
        x = px;
        y = py;
        pointNumber = pPointNumber;
    }
    //get the sum of x + y
    public double getSum()
    {
        return x + y;
    }
    public double getSub()
    {
        return  x - y;
    }
    public double getX()
    {
        return x;
    }
    public double getY()
    {
        return y;
    }
}
