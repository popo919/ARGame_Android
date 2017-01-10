package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

import java.util.Vector;

/**
 * Created by Administrator on 2017/1/6.
 */

public class EnemyCraft extends Craft{
    private static final int MAXHP=15;
    private static final float defaultBulletspeed=0.2f;
    private static final float defaultFirePower=0.5f;
    private float bulletspeed=defaultBulletspeed;
    private GameCamera mCamera;
    private HostCraft hostCraft;
    public Vector<EnemyPlane> mPlanes;
    private double mFirePower;
    private double mFireOff;
    private double mPlanePower;
    private double mPlaneOff;
    private int Sta=1;
    private boolean initFlag = false;
    private int flag = 1;
    private int behaviour = 0;
    public float attenuation = 1.0f;
    public double harm = 10;
    private int rotateDir = 1;
    private float dx=0.01f,dy=0.01f;
    private float maxx=0.2f,maxy=0.2f,minx=-0.2f,miny=-0.2f;
    public double levelTS = 0.0;
    // Be hit by assistive stuffs
    // beHit(Daoju kk);

    public EnemyCraft(GameCamera camera, HostCraft hostCraft){
        mPlanes = new Vector<EnemyPlane>();
        this.hostCraft = hostCraft;
        mCamera = camera;
        Vec3F farPos = new Vec3F(0, 0, camera.getFarZ());
        mPosition = new Vec3F(0.0f,0.0f,0.0f);
        mDirection = SampleMath.Vec3FScale(mCamera.getDirection(),-1);
        mFirePower = defaultFirePower;
        mPlanePower = 8;
        mFireOff = 0;
        mPlaneOff = 8;
        HP=MAXHP;
    }

    // Direction deviation as to default
    public void deviate(float dx, float dy){
        // ...
    }

    // Bias of body as to default
//    public EnemyPlane hover(float dx, float dy){
//        // ...
//        EnemyPlane plane = new EnemyPlane();
//        Vec3F dir = SampleMath.Vec3FAdd(mDirection,new Vec3F(dx,dy,0.0f));
//        plane.setPosition(mPosition);
//        plane.setmDirection(new Vec3F(dx,dy,0.0f));
//        plane.setSpeed(0.0001f);
//        return plane;
//    }

    public boolean Fire(){
        //Log.i("EBullets", "Fire: mFireOff="+mFireOff + "mFirePower="+mFirePower);
        if(mFireOff >= mFirePower){
            Log.i("EBullets", "Fire: mFireOff="+mFireOff + "mFirePower="+mFirePower);
            return true;
        }else {
            return false;
        }
    }

    public void setFirePower(double power){
        mFirePower = power;
    }

    public double beHit(Bullet b){
        // if sub enemies exist , should first destroy them before you can do harm to the main enemy
        if(this.mPlanes.size() <= 0) {
            if(level != 0) {
                this.invincible = false;
                this.attenuation = 1.0f;
            }
            Log.i("craftHP", "beHit: hp=" + HP);
            if (HP <= 0)
                return DESTROY;
        }
        else
        {
            this.invincible = true;
            this.attenuation = 0.1f;
        }

        HP = HP - b.getHarm() * attenuation;

        return HP;
    }

    public int getState(){return Sta;}

    public void setState(int i){Sta=i;}

    public void restart(int level){
        this.show = true;
        this.explodeTimeStamp = -1;
        mPosition = new Vec3F(0.0f,0.0f,0.0f);
        mDirection = SampleMath.Vec3FScale(mCamera.getDirection(),-1);
        mFirePower = 1;
        mFireOff = 0;
        mPlaneOff = 0;
        mPlanes.clear();
        HP = MAXHP;
        initFlag=false;
    }

    @Override
    public void update(double deltaTime, int level){
// ...
        for(int i = 0; i < mPlanes.size(); ++i) // each update set all hits to zero
            mPlanes.get(i).hit = 0;
        this.level = level;

        if(this.state == 1)
            return;

        if(3 == level)
            LevelThree(deltaTime);
        else if(level==1)
            LevelOne(deltaTime);
        else if(level == 2)
            LevelTwo(deltaTime);
        else if(level==0)
            LevelZero(deltaTime);
    }

    @Override
    public EnemyBullet openFire(){
        //Vec3F pos = SampleMath.Vec3FAdd(mPosition, SampleMath.Vec3FScale(mDirection, len));
        Vec3F pos = mPosition;
        EnemyBullet bullet = new EnemyBullet(this);
        bullet.set(pos, mDirection, bulletspeed);
        mFireOff=0;
        return bullet;
    }

    public void SubPlaneFire(Vector<EnemyBullet> mEBullets, TestCube mCube)
    {
        for(int i = 0; i < mPlanes.size(); ++i)
        {
            EnemyPlane e = mPlanes.get(i);
            if(e.Fire())
            {
                Vec3F pos = e.getPosition();
                Vec3F dir = SampleMath.Vec3FNormalize(SampleMath.Vec3FSub(hostCraft.getPosition(), pos));
                EnemyBullet bullet = new EnemyBullet(this);
                bullet.set(pos, dir, 0.5f*bulletspeed);
                bullet.setMeshFrom(mCube);
                bullet.setShaderName("bullet");
                bullet.getBBox().setPos(pos);
                mEBullets.add(bullet);
            }
        }
    }

    public boolean SubPlaneCollide(Bullet bullet)
    {

        for(int i = 0; i < mPlanes.size(); ++i)
        {
            EnemyPlane e = mPlanes.get(i);
            if(bullet.state == 0 && bullet.Collide(e))
            {
                if(DESTROY == e.beHit(bullet))
                    e.state = 1; // explode

                e.hit = 1;
                return true;
            }
        }

        return false;
    }

    public void SubPlaneExplode(double timeStamp)
    {
        for(int i = 0; i < mPlanes.size(); )
        {
            EnemyPlane e = mPlanes.get(i);
            boolean explodeState = false;
            if(e.state == 1)
                explodeState = e.explode(timeStamp);
            if(explodeState)
                mPlanes.remove(i);
            else
                ++i;
        }
    }

    public void SubPlaneDerive(Vector<EnemyBullet> mEBullets, TestCube mCube)
    {
        for(int i = 0; i < mPlanes.size();i++ )
        {
            EnemyPlane e = mPlanes.get(i);
            if(e.Derive()){
                for(int j=0;j<3;j++){
                    float randx,randy,randz;
                    randx = (float)Math.random()-0.5f;
                    randy = (float)Math.random()-0.5f;
                    randz = (float)Math.random();
                    Vec3F dir = SampleMath.Vec3FNormalize(new Vec3F(randx,randy,1.0f));
                    //dir = SampleMath.Vec3FNormalize(SampleMath.Vec3FAdd(dir, new Vec3F(randx,randy,randy)));
                    EnemyBullet b = new EnemyBullet(this);
                    b.set(e.getPosition(), dir, 0.7f*bulletspeed);
                    //b.setRenderInfoFrom(mCube);
                    b.copyFrom(mCube);
                    b.setShaderName("bullet");
                    b.getBBox().setPos(e.getPosition());
                    mEBullets.add(b);
                }
                mPlanes.remove(i);
                Log.i("plane", "SubPlaneDerive: ");
            }
        }
    }

    public double CollideCraft(double ts)
    {
        double hit = 0.0;
        if(this.hostCraft.Collide(this)) // main craft collide
        {
            hit = hostCraft.beHit(this, ts);
            if(hit == DESTROY)
                return DESTROY;
        }

        for(int i = 0; i < mPlanes.size(); ++i)
        {
            if(mPlanes.get(i).Collide(hostCraft))
            {
                hit = hostCraft.beHit(mPlanes.get(i), ts);
                if(hit == DESTROY)
                    return DESTROY;
            }
        }

        return hit;
    }

    @Override
    public void draw(GameEngine engine) {
        super.draw(engine); // draw main enemy craft
        for (EnemyPlane p : mPlanes) // draw sub crafts if any
        {
            p.draw(engine);
        }
    }

    public void resetState()
    {
        state = 0; //alive
        attenuation = 1.0f;
        recoverOrientation();
        invincible = false;
        bulletspeed = defaultBulletspeed;
        mFirePower = defaultFirePower;
        flag = 1;
    }

    private void LevelZero(double deltaTime)
    {
        // ...
        mDirection = SampleMath.Vec3FNormalize(SampleMath.Vec3FScale(hostCraft.getPosition(),1));
        //float acos = (float)Math.acos(SampleMath.Vec3FDot(mDirection, new Vec3F(0, 0, 1)));
        //this.rotate((float)(deltaTime * 90.0) ,new Vec3F(1, 0, 0));
        //int times = (int)(levelTS / 5.0);
        if(behaviour == 0) // guard state
        {
            this.bulletspeed = defaultBulletspeed;
            this.mFirePower = defaultFirePower;
            this.invincible = true;
            flag = 1;
            attenuation = 0.1f;
            if(levelTS > 3.0f)
            {
                behaviour = 1; // rotate
                rotateDir = 1;
                levelTS = 0;
            }
        }
        else if(behaviour == 2)// attack state
        {
            this.bulletspeed = defaultBulletspeed * 2;
            this.mFirePower = defaultFirePower * 0.5f;
            this.invincible = false;
            flag = 1;
            attenuation = 1.0f;

            if(levelTS > 3.0f)
            {
                behaviour = 1; // rotate
                rotateDir = -1;
                levelTS = 0;
            }
        }
        else // rotate
        {
            this.rotate((float)(deltaTime * -90.0) * rotateDir ,new Vec3F(1, 0, 0));

            if(flag == 1) {
                if (rotateDir > 0)
                    this.getBBox().setScale(new Vec3F(0.2f, 1.0f, 0.2f));
                else
                    this.getBBox().setScale(new Vec3F(5.0f, 1.0f, 5.0f));

                flag = 0;
            }

            if(levelTS > 1.0f)
            {
                behaviour = 1 + rotateDir; // rotate

                levelTS = 0;
            }
        }

        levelTS += deltaTime;
        mFireOff += deltaTime;
    }

    private void LevelOne(double deltaTime)
    {
        // ...
        mDirection = SampleMath.Vec3FNormalize(SampleMath.Vec3FScale(hostCraft.getPosition(),1));
        //float acos = (float)Math.acos(SampleMath.Vec3FDot(mDirection, new Vec3F(0, 0, 1)));
        //this.rotate(acos,new Vec3F(0, 0, 1));
        mFireOff += deltaTime;
        mPlaneOff += deltaTime;
        for(int i=0;i<mPlanes.size();i++){
            EnemyPlane p = mPlanes.get(i);
            p.rotate((float)(90.0 * deltaTime), new Vec3F(0,0,1));
            p.update(deltaTime,1);
        }
        //Log.i("plane", "LevelOne: "+mPlaneOff);
        if(mPlaneOff > mPlanePower){
            EnemyPlane e1 = new EnemyPlane(hostCraft, 0, 5.0f);
            Vec3F pos1 = SampleMath.Vec3FAdd(mPosition,new Vec3F(0.06f * flag,-0.02f,0.0f));
            flag = -flag;
            e1.setPosition(pos1);
            e1.mScale = new Vec3F(0.06f, 0.06f, 0.06f);
            e1.copyFrom(this);
            e1.getBBox().setScale(new Vec3F(0.2f, 0.2f, 0.2f));
            e1.getBBox().setAbsPos(pos1);
            e1.setmDirection(new Vec3F(0, 0, 1));
            e1.setSpeed(0.05f);
            mPlanes.add(e1);

//            EnemyPlane e2 = new EnemyPlane(hostCraft, 0);
//            Vec3F pos2 = SampleMath.Vec3FAdd(mPosition,new Vec3F(0.1f,-0.02f,0.0f));
//            e2.setPosition(pos2);
//            e2.mScale = new Vec3F(0.15f, 0.15f, 0.15f);
//            e2.copyFrom(this);
//            e2.getBBox().setScale(new Vec3F(0.05f, 0.05f, 0.05f));
//            e2.getBBox().setAbsPos(mPosition);
//            e2.setmDirection(new Vec3F(0, 0, 1));
//            e2.setSpeed(0.03f);
//            mPlanes.add(e2);

            mPlaneOff=0;
        }
    }

    private void LevelTwo(double deltaTime)
    {
        if(initFlag==false) {
            mFirePower = mFirePower/2;
            initFlag = true;
        }
        Vec3F pos = SampleMath.Vec3FAdd(mPosition, new Vec3F(dx,dy,0.0f));
        if(pos.getData()[0]<minx||pos.getData()[0]>maxx){
            dx = -dx;
        }
        if(pos.getData()[1]<miny||pos.getData()[1]>maxy){
            dy = -dy;
        }
        float randx,randy;
        randx = (float)Math.random();
        randy = (float)Math.random();

        mPosition = SampleMath.Vec3FAdd(mPosition, new Vec3F(dx*randx,dy*randy,0.0f));
        mDirection = SampleMath.Vec3FNormalize(SampleMath.Vec3FSub(hostCraft.getPosition(), mPosition));
        mFireOff += deltaTime;
        mPlaneOff += deltaTime;
    }

    private void LevelThree(double deltaTime)
    {
        if(this.mPlanes.size() == 0 && initFlag == false)
        {
            for(int i = 0; i < 4; ++i) {
                EnemyPlane e = new EnemyPlane(hostCraft, i, 10.0f);
                float x = (i % 2 - 0.5f) * 2;
                float y = (i / 2 - 0.5f) * 2;
                Vec3F pos1 = SampleMath.Vec3FAdd(this.mPosition, new Vec3F(0.1f * x, 0.1f * y, 0.1f));
                e.setPosition(pos1);
                e.mScale = new Vec3F(0.15f, 0.15f, 0.15f);
                e.copyFrom(this);
                e.getBBox().setScale(new Vec3F(0.5f, 0.5f, 0.5f));
                e.getBBox().setAbsPos(pos1);
                mPlanes.add(e);
            }

            initFlag = true;
            return;
        }

        for(int i = 0; i < mPlanes.size(); ++i)
        {
            EnemyPlane e = mPlanes.get(i);
            e.update(deltaTime, 3);
            Vec3F pos = e.getPosition();
            float[] pos_vec4 = {pos.getData()[0], pos.getData()[1], pos.getData()[2], 1.0f};
            float[] axis = {0,0,1};
            float[] rotate = new float[16];
            float[] res = new float[4];
            Matrix.setRotateM(rotate, 0, (float)(30.0f * deltaTime), axis[0], axis[1], axis[2]);
            Matrix.multiplyMV(res, 0, rotate, 0, pos_vec4, 0);
            Vec3F res_pos = new Vec3F(res[0], res[1], res[2]);
            e.setPosition(res_pos);
            e.getBBox().setAbsPos(res_pos);
        }
    }
    @Override
    public double getHP(){return HP/MAXHP;}
}
