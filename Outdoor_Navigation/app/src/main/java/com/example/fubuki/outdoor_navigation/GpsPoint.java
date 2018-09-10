package com.example.fubuki.outdoor_navigation;

import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class GpsPoint {
    private double angle;       //与北方向夹角，弧度值
    private double longitude;   //经度
    private double latitude;    //纬度
    private double distance;
    private final double k1 = 96029;
    private final double k2 = 112000;

    GpsPoint(double pLongitude,double pLatitude,double pAngle,double pDistance)
    {
        angle = pAngle;
        latitude = pLatitude;
        longitude = pLongitude;
        distance = pDistance;
    }
    public double getDistanceFromNextPoint(GpsPoint nextGpsPoint)
    {
        LatLng p1 = new LatLng(latitude,longitude);
        LatLng p2 = new LatLng(nextGpsPoint.getLatitude(),nextGpsPoint.getLongitude());

        //TODO
        //此处调用百度地图API获取两个点之间的距离，单位是米
        double distance = DistanceUtil.getDistance(p1,p2);
        Log.e("Distance","Distance:"+distance);
        return DistanceUtil.getDistance(p1,p2);
    }
    Point calculateNode(GpsPoint otherGpsPoint,boolean pointNumber)
    {
        double x = 0;
        double y = 0;
        double pointAngle = 0;
        angle = 0;
        double nextPointDistance = otherGpsPoint.getDistance();
        double distanceToNextPoint = this.getDistanceFromNextPoint(otherGpsPoint);
        double T = ((this.distance)*(this.distance)+(distanceToNextPoint)*(distanceToNextPoint)-(nextPointDistance)*(nextPointDistance))/(2*distance*distanceToNextPoint);
        if(T >= 0.9999)
        {
            T = 0.999999;
        }
        else if(T <= -0.999999)
        {
            T = -0.9999999;
        }
        pointAngle = Math.acos(T);
        //用gps点的数据计算夹角

        GpsPoint P0 = new GpsPoint(this.getLongitude(),otherGpsPoint.getLatitude(),0,0);
        double L01 = P0.getDistanceFromNextPoint(this);
        double L12 = distanceToNextPoint;
        double cosa = L01/L12;
        if(cosa>=0.999999999)
        {
            cosa = 0.99999999;
        }
        if(cosa<=-0.999999999)
        {
            cosa = -0.99999999;
        }
        if((otherGpsPoint.getLatitude()>=this.getLatitude()&&(otherGpsPoint.getLongitude()>=this.getLongitude())))        //1
        {
            angle = Math.acos(cosa);
        }
        else if((otherGpsPoint.getLatitude()>=this.getLatitude()&&(otherGpsPoint.getLongitude()<this.getLongitude())))   //2
        {
            angle = Math.PI - Math.acos(cosa);
        }
        else if((otherGpsPoint.getLatitude()<this.getLatitude()&&(otherGpsPoint.getLongitude()<this.getLongitude())))   //3
        {
            angle = Math.PI + Math.acos(cosa);
        }
        else if((otherGpsPoint.getLatitude()<this.getLatitude()&&(otherGpsPoint.getLongitude()>=this.getLongitude())))   //4
        {
            angle = 2 * Math.PI - Math.acos(cosa);
        }

        if(pointNumber)
        {
            x = this.longitude + this.distance*Math.cos(pointAngle+Math.PI/2-angle)/k1;
            y = this.latitude + this.distance*Math.sin(pointAngle+Math.PI/2-angle)/k2;
        }
        else
        {
            x = this.longitude + this.distance*Math.cos(Math.PI/2-angle-pointAngle)/k1;
            y = this.latitude + this.distance*Math.sin(Math.PI/2-angle-pointAngle)/k2;
        }
        return new Point(x,y);
    }
    public double getDistance()
    {
        return distance;
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
}

