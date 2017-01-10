package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

/**
 * Created by Unicorn on 2017/1/4.
 */
// All directions and positions is set in the camera coordinate
public class HostCraft extends Craft {
    public static final double FiringInterval = 0.3;
    private static final double HoverInterval = 0.083;
    private static final float DeltaXAngleFactor = 1000f;
    private static final float MaxRotateAngle = 45.0f;
    private static final float BulletSpeed = 0.5f;

    private static final int MAXHP = 100;

    private Vec3F mDefaultViewPos;
    private Vec3F mViewPos;

    private float[] mDefaultRotate;

    private GameCamera mCamera;
    private float mCurAngle = 0.0f;
    private double craftHitTS = -1;

    // Be hit by assistive stuffs
    // beHit(Daoju kk);
    private static final int DESTROY=-1;
    private static final int LIVE=0;

    private double mLastFireTime;       // 上一次开火时间
    private double mAccDeltaTime = 0;
    private float mLastDeltaX = 0;
    private int mUpdateTimes = 0;
    private float mAvgDeltaX = 0;

    public HostCraft(GameCamera camera){
        HP=MAXHP;
        mCamera = camera;
        mDefaultViewPos = new Vec3F(0, 0.03f, 0.2f);
        mViewPos = new Vec3F(mDefaultViewPos.getData());

        //mScale = new Vec3F(0.04f, 0.001f, 0.04f);

        mDefaultRotate = new float[16];
        Vec3F view_dir = SampleMath.Vec3FNormalize(SampleMath.Vec3FSub(new Vec3F(0, 0, camera.getFarZ()), mViewPos));
        float radian = (float) Math.acos(SampleMath.Vec3FDot(view_dir, GameMath.UNIT_VEC_Z));
        Matrix.setRotateM(mDefaultRotate, 0, GameMath.radianToAngle(radian), 1, 0, 0);
    }

    // Direction deviation as to default
    public void deviate(float dx, float dy){
        // ...
    }

    // Bias of body as to default
    public void hover(float dx, float dy){
        // ...
    }

    public void restart(int level){

    }

    public double beHit(Bullet b){
        Log.i("updateinfo", "beforeHit: "+HP);
        HP = HP - b.getHarm();
        Log.i("updateinfo", "beHit: "+HP);
        if(HP<=0)
            return DESTROY;
        return HP;
    }

    public double beHit(EnemyCraft enemyCraft, double ts)
    {
        if(craftHitTS < 0)
            craftHitTS = ts;
        else if((ts - craftHitTS) > 0.5) {
            HP = HP - enemyCraft.harm;
            craftHitTS = ts;
        }

        if (HP <= 0)
            return DESTROY;
        return 1;
    }

    public double beHit(EnemyPlane enemyPlane, double ts)
    {
        if(craftHitTS < 0)
            craftHitTS = ts;
        else if((ts - craftHitTS) > 0.5) {
            HP = HP - enemyPlane.harm;
            craftHitTS = ts;
        }
        if(HP<=0)
            return DESTROY;
        return 1;
    }

    // Adjust the position and direction according to the shift of camera
    @Override
    public void update(double deltaTime, int level){
        mAccDeltaTime += deltaTime;
        mUpdateTimes ++;
        mAvgDeltaX += mCamera.getPosDeltaX();
        if(mAccDeltaTime >= HoverInterval) {
            mAccDeltaTime = 0;
            mLastDeltaX = mAvgDeltaX / mUpdateTimes;
            mAvgDeltaX = 0;
            mUpdateTimes = 0;
        }
        int sign = mLastDeltaX > 0 ? -1 : 1;
        float to_angle = sign * GameMath.min(MaxRotateAngle, Math.abs(mLastDeltaX) * DeltaXAngleFactor);
        float angle = mCurAngle + GameMath.min(1.0f, (float) (deltaTime / HoverInterval)) * (to_angle - mCurAngle);
        mCurAngle = angle;
        float[] default_vpos = mDefaultViewPos.getData();
        mViewPos = new Vec3F(-default_vpos[1] * (float) Math.sin(GameMath.angleToRadian(angle)),
                default_vpos[1] * (float) Math.cos(GameMath.angleToRadian(angle)), default_vpos[2]);
        Vec3F view_dir = SampleMath.Vec3FNormalize(SampleMath.Vec3FSub(new Vec3F(0, 0, mCamera.getFarZ()), mViewPos));

        float[] rot = new float[16];
        Matrix.setRotateM(rot, 0, angle, 0, 0, 1);
        Matrix.multiplyMM(mRotateMatrix, 0, rot, 0, mDefaultRotate, 0);

        mPosition = SampleMath.Vec3FTransform(mViewPos, mCamera.getViewToWorld());
        mDirection = SampleMath.Vec3FTransformNormal(view_dir, mCamera.getViewToWorld());
        mDirection = SampleMath.Vec3FNormalize(mDirection);
        this.mBBox.setAbsPos(mPosition);
    }

    @Override
    public Bullet openFire(){
        float[] size = mBBox.getSize().getData();
        //float[] scale = mScale.getData();
        float len = 0.5f * size[2];
        //Vec3F pos = SampleMath.Vec3FAdd(mPosition, SampleMath.Vec3FScale(mDirection, len));
        Bullet bullet = new Bullet(this);
        bullet.set(mPosition, mDirection, BulletSpeed);
        return bullet;
    }

    @Override
    protected float[] getMVPMatrix(GameEngine engine){
        float[] modelView = new float[16];
        Matrix.setIdentityM(modelView, 0);
        float[] pos = mViewPos.getData();
        Matrix.translateM(modelView, 0, pos[0], pos[1], pos[2]);
        // Rotate
        float[] afterRotate = new float[16];
        Matrix.multiplyMM(afterRotate, 0, modelView, 0, mRotateMatrix, 0);
        // Scale
        float[] scale = mScale.getData();
        Matrix.scaleM(afterRotate, 0, scale[0], scale[1], scale[2]);
        // 移动到中心
        Matrix.translateM(afterRotate, 0, mBBoxCenter[0], mBBoxCenter[1], mBBoxCenter[2]);
        float[] mvp = new float[16];
        Matrix.multiplyMM(mvp, 0, engine.getProjMatrix().getData(), 0, afterRotate, 0);
        return mvp;
    }

    @Override
    public double getHP(){return HP/MAXHP;}
    public double getLastFireTime() { return mLastFireTime; }
    public void setFireTime(double time) { mLastFireTime = time; }
}
