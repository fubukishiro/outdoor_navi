package com.example.fubuki.outdoor_navigation;

import android.util.Log;

import java.util.ArrayList;

public class GpsNode {
    private static final double k1 = 96029;
    private static final double k2 = 112000;
    private ArrayList<GpsPoint> gpsPointArray = new ArrayList<>();
    private Node loraNode = new Node(1.0/100000,5.0/100000);

    public void addGpsPoint(GpsPoint newPoint)
    {
        gpsPointArray.add(newPoint);
    }
    public Point getNodePoint(GpsPoint firstPoint,GpsPoint secondPoint,boolean number)
    {
        Point result = firstPoint.calculateNode(secondPoint,number);
        return result;
    }

    public Point getNodePosition()
    {
        int number = gpsPointArray.size();
        Point circleCenter = new Point(gpsPointArray.get(number-1).getLongitude(),gpsPointArray.get(number-1).getLatitude(),0);
        double distance = gpsPointArray.get(number-1).getDistance();
        loraNode.clear();
        for(int i=0;i<number;i++)
        {
            for(int j=0;j<number-i-1;j++)
            {
                Point temp0 = getNodePoint(gpsPointArray.get(j), gpsPointArray.get(j + i + 1), false);
                Point temp1 = getNodePoint(gpsPointArray.get(j), gpsPointArray.get(j + i + 1), true);
                loraNode.addNode(temp0);
                loraNode.addNode(temp1);
            }
        }
        System.out.println("loraNode number "+loraNode.getSize());
        for(int i = 0; i< loraNode.getSize();i++)
        {
            System.out.println(""+loraNode.get(i).getX()+","+loraNode.get(i).getY());
        }
        Point result = loraNode.getNodeLocation(circleCenter,distance,distance/3);
        return result;
    }

    public int getNodeNumber(){
        return gpsPointArray.size();
    }

}

