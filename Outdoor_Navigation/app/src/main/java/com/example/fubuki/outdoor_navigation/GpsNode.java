package com.example.fubuki.outdoor_navigation;

import java.util.ArrayList;

public class GpsNode {
    private ArrayList<GpsPoint> gpsPointArray = new ArrayList<>();
    private Node loraNode = new Node();

    /*增加新的手机采样点*/
    public void addGpsPoint(GpsPoint newPoint)
    {
        gpsPointArray.add(newPoint);
    }
    public GpsPoint getGpsPoint(int index)
    {
        return gpsPointArray.get(index);
    }
    /*从手机采样点计算获取LoRa节点*/
    public Point[] getNodePoint(GpsPoint firstPoint, GpsPoint secondPoint)
    {
        Point[] result = firstPoint.calculateNode(secondPoint);
        return result;
    }
    /*找出真实的节点*/
    public Point getNodePosition()
    {
        int number = gpsPointArray.size();
        if(number>1)
        {
            Point temp[] = getNodePoint(gpsPointArray.get(number-2), gpsPointArray.get(number-1));
            loraNode.addPoint(temp[0]);
            loraNode.addPoint(temp[1]);
        }
        System.out.println("loraNode number "+loraNode.getSize());
        for(int i = 0; i< loraNode.getSize();i++)
        {
            System.out.println(""+loraNode.getPoint(i).getX()+","+loraNode.getPoint(i).getY());
        }
        /*按直线判断所有的点的合法性*/

        for(int i=0;i<number-1;i++)
        {
            Node nodeUp = new Node();
            Node nodeDown = new Node();
            int pointNumber = loraNode.getSize();
            for(int j=0;j<pointNumber;j++)
            {
                Point temp = loraNode.getPoint(j);
                if (GpsPoint.GetLineSide(this.getGpsPoint(i),this.getGpsPoint(i+1),temp))
                {
                    nodeUp.addPoint(temp);
                }
                else
                {
                    nodeDown.addPoint(temp);
                }
            }
            if(nodeUp.getSize()>nodeDown.getSize())
            {
                int dowmNumber = nodeDown.getSize();
                for(int k=0;k<dowmNumber;k++)
                {
                    loraNode.removePoint(nodeDown.getPoint(k));
                }
            }
            else if(nodeUp.getSize()<nodeDown.getSize())
            {
                int upNumber = nodeUp.getSize();
                for(int k=0;k<upNumber;k++)
                {
                    loraNode.removePoint(nodeUp.getPoint(k));
                }
            }
        }
        return loraNode.getNodeLocation();
    }

}
