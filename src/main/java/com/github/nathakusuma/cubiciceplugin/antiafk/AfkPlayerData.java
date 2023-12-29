package com.github.nathakusuma.cubiciceplugin.antiafk;

public class AfkPlayerData {
    private double yaw;
    private double pitch;
    private double x;
    private double y;
    private double z;

    AfkPlayerData(double yaw, double pitch, double x, double y, double z) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
