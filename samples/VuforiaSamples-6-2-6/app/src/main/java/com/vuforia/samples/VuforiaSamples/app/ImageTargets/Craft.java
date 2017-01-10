package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.vuforia.Vec3F;

/**
 * Created by Unicorn on 2017/1/3.
 */
public class Craft extends RenderMeshObject {

    protected int mHealthPower;         // Health
    protected Vec3F mDirection = new Vec3F(0, 0, 0);
    protected float mSpeed;
    protected float mAccelaration;
    protected float mAccelarationDir;
    protected double HP=10;

    public float getSpeed() { return mSpeed; }
    public void setSpeed(float speed) { mSpeed = speed; }
    public void setmDirection(Vec3F dir){mDirection = dir;}
    public void setAccelaration(float accelaration) { mAccelaration = accelaration; }
    public void setAccelarationDir(float dir) { mAccelarationDir = dir; }


    public Bullet openFire(){
        // ...
        return new Bullet(this);
    }

    public void update(double timestamp, int level){
        // ...
    }

    public double getHP(){return HP;}

    public boolean explode(double timeStamp) // return true if explode end
    {
        if(explodeTimeStamp < 0)
            explodeTimeStamp = timeStamp;

        double deltaTime = timeStamp - explodeTimeStamp;
        int t = (int)(deltaTime / 0.2);
        this.show = t % 2 == 0;

        if(deltaTime > 2.0)
            return true;
        else
            return false;
    }

    public Vec3F getDirection() { return new Vec3F(mDirection); }
}
