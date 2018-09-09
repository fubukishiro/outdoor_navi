package com.example.fubuki.outdoor_navigation;

import java.util.ArrayList;

public class Node {
    public ArrayList<Point> nodePointArray = new ArrayList<>();
    private double accuracy;
    private double width;

    public Node(double pAccuracy,double pWidth)
    {
        accuracy = pAccuracy;
        width = pWidth;
    }
    public void clear()
    {
        nodePointArray.clear();
    }
    public void addNode(Point pPoint)
    {
        nodePointArray.add(pPoint);
    }
    public void setAccuracy(double pAccuracy)
    {
        accuracy = pAccuracy;
    }
    public void setWidth(double pWidth)
    {
        width = pWidth;
    }
    public Point getNodeLocation()
    {
        int pointCount = nodePointArray.size();
        double sumPosition = getSumPosition();
        double subPosition = getSubPosition();
        ArrayList<Point> areaPointGroup = new ArrayList<>();

        for(int i=0;i<pointCount;i++)
        {
            Point temp = nodePointArray.get(i);
            double tempSum = temp.getSum();
            double tempSub = temp.getSub();
            if((tempSum>=sumPosition)&&(tempSum<=sumPosition+width)&&(tempSub>=subPosition)&&(tempSub<=subPosition+width))
            {
                areaPointGroup.add(temp);
            }
        }
        int resultLength = areaPointGroup.size();
        double xSum = 0;
        double ySum = 0;
        for(int i=0;i<resultLength;i++)
        {
            Point temp = areaPointGroup.get(i);
            xSum += temp.getX();
            ySum += temp.getY();
        }
        xSum = xSum/resultLength;
        ySum = ySum/resultLength;
        Point result = new Point(xSum,ySum,0);
        return  result;
    }
    private double getMaxSum()
    {
        int pointCount = nodePointArray.size();
        Point firstPoint = nodePointArray.get(0);
        double result = firstPoint.getSum();
        for(int i = 0;i<pointCount;i++)
        {
            Point tempPoint = nodePointArray.get(i);
            double temp = tempPoint.getSum();
            if(temp >= result)
            {
                result = temp;
            }
        }
        return result;
    }
    private double getMinSum()
    {
        int pointCount = nodePointArray.size();
        Point firstPoint = nodePointArray.get(0);
        double result = firstPoint.getSum();
        for(int i = 0;i<pointCount;i++)
        {
            Point tempPoint = nodePointArray.get(i);
            double temp = tempPoint.getSum();
            if(temp <= result)
            {
                result = temp;
            }
        }
        return result;
    }
    private double getMaxSub()
    {
        int pointCount = nodePointArray.size();
        Point firstPoint = nodePointArray.get(0);
        double result = firstPoint.getSub();
        for(int i = 0;i<pointCount;i++)
        {
            Point tempPoint = nodePointArray.get(i);
            double temp = tempPoint.getSub();
            if(temp >= result)
            {
                result = temp;
            }
        }
        return result;
    }
    private double getMinSub()
    {
        int pointCount = nodePointArray.size();
        Point firstPoint = nodePointArray.get(0);
        double result = firstPoint.getSub();
        for(int i = 0;i<pointCount;i++)
        {
            Point tempPoint = nodePointArray.get(i);
            double temp = tempPoint.getSub();
            if(temp <= result)
            {
                result = temp;
            }
        }
        return result;
    }
    private int getPointNumberInSum(double pStart)
    {
        int count = nodePointArray.size();
        int number = 0;
        for(int i=0;i<count;i++)
        {
            Point tempPoint = nodePointArray.get(i);
            double tempSum = tempPoint.getSum();
            if(tempSum>=pStart&&tempSum<=(pStart+width))
            {
                number++;
            }
        }
        return number;
    }
    private int getPointNumberInSub(double pStart)
    {
        int count = nodePointArray.size();
        int number = 0;
        for(int i=0;i<count;i++)
        {
            Point tempPoint = nodePointArray.get(i);
            double tempSub = tempPoint.getSub();
            if(tempSub>=pStart&&tempSub<=(pStart+width))
            {
                number++;
            }
        }
        return number;
    }

    public double getSumPosition()
    {
        double minSum = getMinSum();
        int length = (int)((getMaxSum()-getMinSum())/accuracy);
        int[] count = new int[length+1];
        for(int i=0;i<length+1;i++)
        {
            count[i] = getPointNumberInSum(minSum+i*accuracy);
        }
        int tmpMax = 0;
        for(int i=0;i<length+1;i++)
        {
            if(count[i]>=tmpMax)
            {
                tmpMax = count[i];
            }
        }
        int position = 0;
        for(int i=0;i<length+1;i++)
        {
            if(count[i]==tmpMax)
            {
                position = i;
            }
        }
        return minSum+position*accuracy;
    }

    public double getSubPosition()
    {
        double minSub = getMinSub();
        int length = (int)((getMaxSub()-getMinSub())/accuracy);
        int[] count = new int[length+1];
        for(int i=0;i<length+1;i++)
        {
            count[i] = getPointNumberInSub(minSub+i*accuracy);
        }
        int tmpMax = 0;
        for(int i=0;i<length+1;i++)
        {
            if(count[i]>=tmpMax)
            {
                tmpMax = count[i];
            }
        }
        int position = 0;
        for(int i=0;i<length+1;i++)
        {
            if(count[i]==tmpMax)
            {
                position = i;
            }
        }
        return minSub+position*accuracy;
    }
}

