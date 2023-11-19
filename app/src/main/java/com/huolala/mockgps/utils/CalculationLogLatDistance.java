package com.huolala.mockgps.utils;

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import com.baidu.mapapi.model.LatLng;

/**
 * @author jiayu.liu
 */
public class CalculationLogLatDistance {

    private static double M_F = (1 / 298.2572236);
    private static double EARTH_RADIUS = 6371000;
    private static double WGS84_L_RADIUS = 6378137;
    private static double WGS84_S_RADIUS = 6356752.3142;

    public static synchronized double getDistance(LatLng coor1, LatLng coor2) {
        double radLat1 = A2R(coor1.latitude);
        double radLat2 = A2R(coor2.latitude);
        double a = radLat1 - radLat2;
        double b = A2R(coor1.longitude) - A2R(coor2.longitude);
        double s = 2 * asin(sqrt(pow(sin(a / 2), 2) + cos(radLat1) * cos(radLat2) * pow(sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s;
    }

    public static synchronized double getYaw(LatLng coor1, LatLng coor2) {
        double result = 0.0;

        int ilat1 = (int) (0.50 + coor1.latitude * 360000.0);
        int ilat2 = (int) (0.50 + coor2.latitude * 360000.0);
        int ilon1 = (int) (0.50 + coor1.longitude * 360000.0);
        int ilon2 = (int) (0.50 + coor2.longitude * 360000.0);

        double latitude1 = A2R(coor1.latitude);
        double longitude1 = A2R(coor1.longitude);
        double latitude2 = A2R(coor2.latitude);
        double longitude2 = A2R(coor2.longitude);

        if ((ilat1 == ilat2) && (ilon1 == ilon2)) {
            return result;
        } else if (ilon1 == ilon2) {
            if (ilat1 > ilat2) {
                result = 180.0;
            }
        } else {
            double c =
                    Math.acos(sin(latitude2) * sin(latitude1)
                            + cos(latitude2) * cos(latitude1) * cos((longitude2 - longitude1)));
            double A = asin(cos(latitude2) * sin((longitude2 - longitude1)) / sin(c));

            result = R2A(A);

            if ((ilat2 <= ilat1) || (ilon2 <= ilon1)) {
                if ((ilat2 < ilat1) && (ilon2 < ilon1)) {
                    result = 180.0 - result;
                } else if (ilat2 < ilat1) {
                    result = 180.0 - result;
                } else if (ilat2 > ilat1) {
                    result += 360.0;
                }
            }
        }

        if (result < 0) {
            result += 360.0;
        }
        if (result > 360) {
            result -= 360.0;
        }

        return result;
    }

    /**
     * 3、已知经纬度A,并知道A到B经纬度的距离、方位角(偏北角)。计算B经纬度)
     */
    public static synchronized LatLng getNextLonLat(LatLng coor, double yaw, double dist) {

        double alpha1 = A2R(yaw);
        double sinAlpha1 = sin(alpha1);
        double cosAlpha1 = cos(alpha1);

        double tanU1 = (1.0 - M_F) * tan(A2R(coor.latitude));
        double cosU1 = 1.0 / sqrt((1.0 + tanU1 * tanU1));
        double sinU1 = tanU1 * cosU1;
        double sigma1 = atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
        double uSq = cosSqAlpha * (WGS84_L_RADIUS * WGS84_L_RADIUS - WGS84_S_RADIUS * WGS84_S_RADIUS)
                / (WGS84_S_RADIUS * WGS84_S_RADIUS);
        double A = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320 - 175.0 * uSq)));
        double B = uSq / 1024.0 * (256.0 + uSq * (-128 + uSq * (74.0 - 47.0 * uSq)));

        double cos2SigmaM = 0;
        double sinSigma = 0;
        double cosSigma = 0;
        double sigma = dist / (WGS84_S_RADIUS * A), sigmaP = 2.0 * Math.PI;
        while (abs(sigma - sigmaP) > 1e-12) {
            cos2SigmaM = cos(2.0 * sigma1 + sigma);
            sinSigma = sin(sigma);
            cosSigma = cos(sigma);
            double deltaSigma = B * sinSigma
                    * (cos2SigmaM
                    + B / 4.0
                    * (cosSigma * (-1 + 2.0 * cos2SigmaM * cos2SigmaM)
                    - B / 6.0 * cos2SigmaM * (-3 + 4.0 * sinSigma * sinSigma)
                    * (-3 + 4.0 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = dist / (WGS84_S_RADIUS * A) + deltaSigma;
        }

        double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
        double lat2 = atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1 - M_F) * sqrt(sinAlpha * sinAlpha + tmp * tmp));
        double lambda = atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
        double C = M_F / 16.0 * cosSqAlpha * (4 + M_F * (4 - 3.0 * cosSqAlpha));
        double L =
                lambda
                        - (1 - C) * M_F * sinAlpha
                        * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2.0 * cos2SigmaM * cos2SigmaM)));

//        double revAz = atan2(sinAlpha, -tmp);
        // 输出经纬度
        return new LatLng(R2A(lat2), coor.longitude + R2A(L));
    }

    public static synchronized LatLng longLatOffset(LatLng coor, double yaw, double dist) {
        double lon = coor.longitude;
        double lat = coor.latitude;

        yaw = A2R(yaw);
        double arc = 6371.393 * 1000;
        lon += dist * sin(yaw) / (arc * cos(lat) * 2 * Math.PI / 360);
        lat += dist * cos(yaw) / (arc * 2 * Math.PI / 360);

        return new LatLng(lat, lon);
    }

    public static boolean isCheckNaN(LatLng location) {
        return location.latitude <= 0.0 || location.longitude <= 0.0 || Double.isNaN(location.latitude) || Double.isNaN(location.longitude);
    }


    /**
     * 角度转弧度
     */
    public static double A2R(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 弧度转角度
     */
    public static double R2A(double d) {
        return d / Math.PI * 180.0;
    }


}
