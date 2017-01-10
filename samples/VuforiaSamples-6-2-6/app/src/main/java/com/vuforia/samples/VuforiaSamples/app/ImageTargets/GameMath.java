package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

/**
 * Created by Unicorn on 2017/1/4.
 */
public class GameMath {
    public static final float PI = 3.1415926f;
    public static final Vec3F UNIT_VEC_X = new Vec3F(1, 0, 0);
    public static final Vec3F UNIT_VEC_Y = new Vec3F(0, 1, 0);
    public static final Vec3F UNIT_VEC_Z = new Vec3F(0, 0, 1);
    public static final Vec3F DEFAULT_UP = new Vec3F(0, 0, 1);
    public static final Vec3F DEFAULT_FRONT = new Vec3F(0, 1, 0);
    public static final Vec3F DEFAULT_RIGHT = new Vec3F(1, 0, 0);

    public static final float[] IDENTITY_MATRIX44_DATA = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };


    public static <T extends Number> T min(T a, T b){
        return a.doubleValue() < b.doubleValue() ? a : b;
    }

    public static <T extends Number> T max(T a, T b){
        return a.doubleValue() > b.doubleValue() ? a : b;
    }

    public static float length(Vec3F v){
        float[] data = v.getData();
        return (float)Math.sqrt(data[0]*data[0] + data[1]*data[1] + data[2]*data[2]);
    }

    public static float distance(Vec3F v1, Vec3F v2){
        Vec3F delta = SampleMath.Vec3FSub(v1, v2);
        return length(delta);
    }

    public static float radianToAngle(float radian){
        return radian * 180.0f / PI;
    }

    public static float angleToRadian(float angle){
        return angle * PI / 180.0f;
    }
}
