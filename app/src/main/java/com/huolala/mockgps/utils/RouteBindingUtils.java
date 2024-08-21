package com.huolala.mockgps.utils;

import com.baidu.mapapi.model.LatLng;

import java.util.List;

/**
 * 绑路优化工具
 * @author jiayu.liu
 */
public class RouteBindingUtils {

    public static LatLng snapToPath(LatLng point, List<LatLng> path) {
        LatLng closestPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < path.size() - 1; i++) {
            LatLng segmentStart = path.get(i);
            LatLng segmentEnd = path.get(i + 1);

            LatLng projectedPoint = projectPointOntoSegment(point, segmentStart, segmentEnd);

            double distance = haversine(point.latitude, point.longitude,
                    projectedPoint.latitude, projectedPoint.longitude);

            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = projectedPoint;
            }
        }

        return closestPoint;
    }

    // 将点投影到线段上，返回投影点
    private static LatLng projectPointOntoSegment(LatLng point, LatLng segmentStart, LatLng segmentEnd) {
        double lat1 = Math.toRadians(segmentStart.latitude);
        double lon1 = Math.toRadians(segmentStart.longitude);
        double lat2 = Math.toRadians(segmentEnd.latitude);
        double lon2 = Math.toRadians(segmentEnd.longitude);
        double lat3 = Math.toRadians(point.latitude);
        double lon3 = Math.toRadians(point.longitude);

        double a = lat3 - lat1;
        double b = lon3 - lon1;
        double c = lat2 - lat1;
        double d = lon2 - lon1;

        double dot = a * c + b * d;
        double lenSq = c * c + d * d;
        double param = dot / lenSq;

        if (param < 0) {
            return segmentStart;
        } else if (param > 1) {
            return segmentEnd;
        }

        double projectedLat = lat1 + param * c;
        double projectedLon = lon1 + param * d;

        return new LatLng(Math.toDegrees(projectedLat), Math.toDegrees(projectedLon));
    }

    // Haversine公式计算两个经纬度点之间的距离
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6378137.0; // 地球半径（米）
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance;
    }
}
