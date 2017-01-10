package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.util.Log;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

/**
 * Created by Administrator on 2017/1/7.
 */

public class EnemyPlane extends Craft {

    private static final double MAXSAVE=10;
    private double savetime=0;
    public double harm = 5;
    EnemyPlane(HostCraft hostCraft, int i, float HP)
    {
        this.HP = HP;
        this.hostCraft = hostCraft;
        mFirePower = 2.0f;
        mFireOff = 0.4f * i;
    }

    public boolean Fire() {
        if (mFireOff >= mFirePower) {
            Log.i("EBullets", "Fire: mFireOff=" + mFireOff + "mFirePower=" + mFirePower);
            mFireOff = 0;
            return true;
        } else {
            return false;
        }
    }

    public boolean Derive(){
        if(savetime > MAXSAVE)
            return true;
        return false;
    }

    @Override
    public void update(double deltaTime, int level){
        this.level = level;
        Vec3F deltaDir = SampleMath.Vec3FScale(mDirection, (float)(mSpeed * deltaTime));
        mPosition = SampleMath.Vec3FAdd(mPosition, deltaDir);
        this.getBBox().setAbsPos(mPosition);
        this.mFireOff += deltaTime;
        if(level==1)
           savetime += deltaTime;
        //Log.i("plane",mPosition.getData()[0]+" , "+mPosition.getData()[1]+" , "+mPosition.getData()[2]+" , ");
    }

    public double beHit(Bullet b){
        // if sub enemies exist , should first destroy them before you can do harm to the main enemy
            Log.i("craftHP", "beHit: hp=" + HP);
            HP = HP - b.getHarm();
            if (HP <= 0)
                return DESTROY;
        return HP;
    }

    private HostCraft hostCraft;
    private double mFirePower;
    private double mFireOff;
}
