package com.example.fubuki.outdoor_navigation;

import java.util.ArrayList;

public class Node {
    public ArrayList<Point> nodePointArray = new ArrayList<>();
    private double accuracy;
    private double width;
    private int angleIncrement;

    public Node(double pAccuracy,double pWidth)
    {
        accuracy = pAccuracy;
        width = pWidth;
    }
    public int getSize()
    {
        return  nodePointArray.size();
    }
    public Point get(int i)
    {
        return nodePointArray.get(i);
    }
    public void clear()
    {
        nodePointArray.clear();
    }
    public void addNode(Point pPoint)
    {
        nodePointArray.add(pPoint);
    }

    public Point getNodeLocation(Point pNewGpsPoint,double pDistance,double pRadius)
    {
        ArrayList<Point> areaPointGroup = new ArrayList<>();
        int pointCount = this.getSize();
        //TODO
        //增加角度增量的set方法
        angleIncrement = 3;
        //遍历所有角度
        int[] numberInCircle = new int[(int)(360.0/this.angleIncrement)+1] ;
        System.out.println("angleNumber"+numberInCircle.length);
        for(int angle = 0;angle< 360; angle+=this.angleIncrement)
        {
            double circleX = pNewGpsPoint.getX()+pDistance*Math.cos(angle/180.0*Math.PI)/GpsPoint.k1;
            double circleY = pNewGpsPoint.getY()+pDistance*Math.sin(angle/180.0*Math.PI)/GpsPoint.k2;
            for (int i = 0; i < pointCount; i++)
            {
                Point temp = this.get(i);
                double sum = (temp.getX()-circleX)*(temp.getX()-circleX)*GpsPoint.k1*GpsPoint.k1+(temp.getY()-circleY)*(temp.getY()-circleY)*GpsPoint.k2*GpsPoint.k2;
                //  System.out.println("sum"+sum);
                //  System.out.println("pRadius"+pRadius*pRadius);
                if(sum <= pRadius*pRadius)
                {
                    //    System.out.println("Get Point");
                    int position = angle/this.angleIncrement;
                    numberInCircle[position]++;
                    //  System.out.println("Position "+position);
                }
            }
        }

        //找到最大圆区间
        int maxNumberInCircle = numberInCircle[0];
        int anglePosition = 0;
        for(int angle = 0;angle< 360; angle+=this.angleIncrement)
        {
            if(numberInCircle[angle/this.angleIncrement]>=maxNumberInCircle)
            {
                anglePosition = angle/this.angleIncrement;
                maxNumberInCircle = numberInCircle[anglePosition];
                System.out.println("maxNumberInCircle in Circle"+maxNumberInCircle);
            }
        }

        //找到最大圆区间的点
        double circleX = pNewGpsPoint.getX()+pDistance*Math.cos(anglePosition*this.angleIncrement/180.0*Math.PI)/GpsPoint.k1;
        double circleY = pNewGpsPoint.getY()+pDistance*Math.sin(anglePosition*this.angleIncrement/180.0*Math.PI)/GpsPoint.k2;
        System.out.println("pointCount"+pointCount);
        for (int i = 0; i < pointCount; i++)
        {
            Point temp = this.get(i);
            double sum = (temp.getX()-circleX)*(temp.getX()-circleX)*GpsPoint.k1*GpsPoint.k1+(temp.getY()-circleY)*(temp.getY()-circleY)*GpsPoint.k2*GpsPoint.k2;
            if(sum <= pRadius*pRadius)
            {
                System.out.println("sum"+sum);
                System.out.println("pRadius"+pRadius*pRadius);
                areaPointGroup.add(new Point(temp.getX(),temp.getY(),0));
                System.out.println("Add result Lora Point");
            }
        }
        int resultLength = areaPointGroup.size();
        System.out.println("resultLength "+resultLength);
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

