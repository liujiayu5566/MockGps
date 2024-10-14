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

import java.util.Random;

/**
 * @author jiayu.liu
 */
public class CalculationLogLatDistance {

    /**
     * 计算两点距离
     */
    public static synchronized double getDistance(LatLng coor1, LatLng coor2) {
        // WGS84椭球参数
        // 长半轴（赤道半径），单位：米
        double a = 6378137.0;
        // 扁率
        double f = 1 / 298.257223563;
        // 短半轴（极半径）
        double b = (1 - f) * a;

        double lat1 = Math.toRadians(coor1.latitude);
        double lon1 = Math.toRadians(coor1.longitude);
        double lat2 = Math.toRadians(coor2.latitude);
        double lon2 = Math.toRadians(coor2.longitude);

        double L = lon2 - lon1;
        double U1 = Math.atan((1 - f) * Math.tan(lat1));
        double U2 = Math.atan((1 - f) * Math.tan(lat2));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double lambda = L, lambdaP, iterLimit = 100;
        double sinSigma, cosSigma, sigma, sinAlpha, cos2Alpha, cos2SigmaM, C;

        do {
            double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda) +
                    (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));

            // co-incident points
            if (sinSigma == 0) {
                return 0;
            }

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cos2Alpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cos2Alpha;

            // equatorial line
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0;
            }

            C = f / 16 * cos2Alpha * (4 + f * (4 - 3 * cos2Alpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma *
                            (-1 + 2 * cos2SigmaM * cos2SigmaM)));

        } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        // formula failed to converge
        if (iterLimit == 0) {
            return Double.NaN;
        }

        double uSq = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 *
                (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                        B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) *
                                (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        double s = b * A * (sigma - deltaSigma);
        // 距离，单位：米
        return s;
    }

    /**
     * 计算 coor1->coor2 的方位角
     */
    public static synchronized double getYaw(LatLng coor1, LatLng coor2) {
        double lat1 = Math.toRadians(coor1.latitude);
        double lon1 = Math.toRadians(coor1.longitude);
        double lat2 = Math.toRadians(coor2.latitude);
        double lon2 = Math.toRadians(coor2.longitude);

        // 计算经度差
        double dLon = lon2 - lon1;

        // 计算 y 和 x
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        // 计算方位角
        double bearing = Math.atan2(y, x);

        // 将方位角转换为 [0, 360) 范围
        bearing = Math.toDegrees(bearing);
        bearing = (bearing + 360) % 360;

        return bearing;
    }

    /**
     * 3、已知经纬度A,并知道A到B经纬度的距离、方位角(偏北角)。计算B经纬度)
     */
    public static synchronized LatLng getNextLonLat(LatLng coor, double yaw, double dist) {
        // WGS84椭球参数
        // 长半轴（赤道半径），单位：米
        double a = 6378137.0;
        // 扁率
        double f = 1 / 298.257223563;
        // 短半轴（极半径）
        double b = (1 - f) * a;

        double lat1 = Math.toRadians(coor.latitude);
        double lon1 = Math.toRadians(coor.longitude);
        double alpha1 = Math.toRadians(yaw);

        double sinAlpha1 = Math.sin(alpha1);
        double cosAlpha1 = Math.cos(alpha1);

        double tanU1 = (1 - f) * Math.tan(lat1);
        double cosU1 = 1 / Math.sqrt(1 + tanU1 * tanU1);
        double sinU1 = tanU1 * cosU1;

        double sigma1 = Math.atan2(tanU1, cosAlpha1);
        double sinAlpha = cosU1 * sinAlpha1;
        double cos2Alpha = 1 - sinAlpha * sinAlpha;
        double uSq = cos2Alpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double sigma = dist / (b * A);
        double sigmaP = 2 * Math.PI;

        double cos2SigmaM = 0, sinSigma = 0, cosSigma = 0, deltaSigma = 0;

        while (Math.abs(sigma - sigmaP) > 1e-12) {
            cos2SigmaM = Math.cos(2 * sigma1 + sigma);
            sinSigma = Math.sin(sigma);
            cosSigma = Math.cos(sigma);
            deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)
                    - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
            sigmaP = sigma;
            sigma = dist / (b * A) + deltaSigma;
        }

        double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
        double lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1,
                (1 - f) * Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
        double lambda = Math.atan2(sinSigma * sinAlpha1,
                cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
        double C = f / 16 * cos2Alpha * (4 + f * (4 - 3 * cos2Alpha));
        double L = lambda - (1 - C) * f * sinAlpha *
                (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));

        double lon2 = lon1 + L;

        // 将结果转换为十进制度数
        double newLat = Math.toDegrees(lat2);
        double newLon = Math.toDegrees(lon2);

        return new LatLng(newLat, newLon);

    }

    /**
     * 随机经纬度
     * @param center 中心点
     * @param radiusInMeters 随机的半径
     */
    public static LatLng getRandomLatLng(LatLng center, double radiusInMeters) {
        // 地球半径，单位为米
        double earthRadius = 6371000;
        Random random = new Random();

        // 随机生成一个距离（0到radiusInMeters之间）
        double radius = random.nextDouble() * radiusInMeters;

        // 随机生成一个方位角（0到360度之间）
        double angle = random.nextDouble() * 360;

        // 将角度转换为弧度
        double angleRad = Math.toRadians(angle);

        // 计算新的经纬度
        double deltaLat = radius * Math.cos(angleRad) / earthRadius;
        double deltaLng = radius * Math.sin(angleRad) / (earthRadius * Math.cos(Math.toRadians(center.latitude)));

        double newLat = center.latitude + Math.toDegrees(deltaLat);
        double newLng = center.longitude + Math.toDegrees(deltaLng);

        return new LatLng(newLat, newLng);
    }

    public static boolean isCheckNaN(LatLng location) {
        return location.latitude <= 0.0 || location.longitude <= 0.0 || Double.isNaN(location.latitude) || Double.isNaN(location.longitude);
    }
}
