package com.example.fubuki.outdoor_navigation;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

public class MyUtil {
    public static double calculateAngle(double p1X,double p1Y, double p2X, double p2Y){
        //X:latitude; Y:longitude
        //p2:current
        LatLng p1 = new LatLng(p1X, p1Y);
        LatLng p2 = new LatLng(p2X, p2Y);
        LatLng p3 = new LatLng(p1X-p2X,p1Y);

        //此处调用百度地图API获取两个点之间的距离，单位是米
        double r1 = DistanceUtil.getDistance(p1,p2);
        double r2 = DistanceUtil.getDistance(p1,p3);
        double cosa = r1/r2;
        double angle = 0;
        if(((p1X >= p2X) && (p1Y>=p2Y) ))        //1
        {
            angle = Math.acos(cosa);
        }
        else if((p1X >= p2X) && (p1Y < p2Y))   //2
        {
            angle = Math.PI - Math.acos(cosa);
        }
        else if((p1X < p2X) && (p1Y < p2Y))   //3
        {
            angle = Math.PI + Math.acos(cosa);
        }
        else if((p1X < p2X) && (p1Y >= p2Y))   //4
        {
            angle = 2 * Math.PI - Math.acos(cosa);
        }else{
            angle = 0;
        }
        return angle;
    }
}
