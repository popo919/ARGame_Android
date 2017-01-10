package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.vuforia.Matrix44F;
import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

/**
 * Created by Unicorn on 2017/1/4.
 */
public class GameCamera {
    private Vec3F mPosition = new Vec3F(0, 0, 0);
    private Vec3F mDirection = GameMath.DEFAULT_FRONT;
    private Vec3F mUp = GameMath.DEFAULT_UP;
    private Vec3F mRight = GameMath.DEFAULT_RIGHT;
    private final float mFarZ = 2.5f;
    private Matrix44F mViewToWorld = new Matrix44F();
    private Vec3F mLastPosition = new Vec3F(0, 0, 0);

    public void update(Matrix44F modelView){
        mLastPosition.setData(mPosition.getData());
        Matrix44F invMV = SampleMath.Matrix44FInverse(modelView);
        mViewToWorld = invMV;
        Matrix44F invTranspMV = SampleMath.Matrix44FTranspose(invMV);
        float[] data = invTranspMV.getData();
        float[] pos = { data[12], data[13], data[14] };
        float[] dir = { data[8], data[9], data[10] };
        float[] right = { data[0], data[1], data[2] };
        float[] up = { data[4], data[5], data[6] };

        mPosition.setData(pos);
        mDirection.setData(dir);
        mDirection = SampleMath.Vec3FNormalize(mDirection);
        mUp.setData(up);
        mUp = SampleMath.Vec3FNormalize(mUp);
        mRight.setData(right);
        mRight = SampleMath.Vec3FNormalize(mRight);
    }

    public Vec3F getPosition() { return new Vec3F(mPosition); }
    public Vec3F getDirection() { return new Vec3F(mDirection); }
    public Vec3F getUp() { return new Vec3F(mUp); }
    public Vec3F getRight() { return new Vec3F(mRight); }
    public float getFarZ() { return mFarZ; }
    public Matrix44F getViewToWorld() {
        return new Matrix44F(mViewToWorld);
    }
    public float getPosDeltaX(){
        return mPosition.getData()[0] - mLastPosition.getData()[0];
    }

}
