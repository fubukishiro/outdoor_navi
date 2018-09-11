package com.example.fubuki.outdoor_navigation;

import android.util.Log;

import java.util.ArrayList;

public class GpsNode {
    private final double k1 = 96029;
    private final double k2 = 112000;
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
        Log.e("Lora","Lora:"+loraNode.nodePointArray.size());
        for(int i = 0;i < loraNode.nodePointArray.size(); i++){
            Log.e("Lora","Lora:"+loraNode.nodePointArray.get(i).getX()+" "+loraNode.nodePointArray.get(i).getY());
        }
        Point result = loraNode.getNodeLocation();
        return result;
    }

    public int getNodeNumber(){
        return gpsPointArray.size();
    }

    public void setAccuracy(double accuracy,double width){
        loraNode.accuracy = accuracy;
        loraNode.width = width;
    }
    public void getAccuracy(){
        Log.e("Node:","距离"+loraNode.accuracy);
    }
}

