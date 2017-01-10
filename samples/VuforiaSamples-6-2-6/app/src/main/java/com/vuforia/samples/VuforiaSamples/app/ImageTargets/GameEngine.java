package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;

import com.vuforia.Matrix44F;
import com.vuforia.Vec3F;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.utils.SampleMath;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Texture;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Unicorn on 2017/1/4.
 */
public class GameEngine {
    private static final int DESTROY=-1;
    private static final int UPDATE=0;
    private static final int SUCCESS = 1;
    private static final int FAIL = 2;
    private static final int LEVELUP = 3;

    private static final int MAXINTERVAL = 6;
    private static final int MAXCOUNTER = 3;
    private boolean LevelUpState = false;
    private double IntervalTimestamp;

    private int level;
    private int updateframe=5;
    private TestPlane[] enemyPlanes;
    private TestPlane hostPlane;
    private GameCamera mCamera;
    private HostCraft mHostCraft;
    private EnemyCraft mEnemyCraft;
    private TestTeapot mTeapot;
    private TestCube mCube;
    private TestCube mhCube;
    private TestFloor mFloor;
    private Map<String, ShaderProgram> mShaderPrograms;
    private Map<String, Texture> mTextures;

    private Matrix44F mModelViewMatrx;
    private Matrix44F mProjMatrix;
    private double mPreTimestamp = -1;

    private Vector<Bullet> mBullets;
    private Vector<EnemyBullet> mEBullets;
    private Object mLock = new Object();

    private int mBulletsToBeAppend = 0;
    public boolean win = false;
    public boolean fail = false;

    public GameEngine(){
        mCamera = new GameCamera();
    }

    public void init(Map<String, Texture> textures){
        level = 0;
        initShaderPrograms();
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);
        initTextures(textures);
        initObjects();
        mBullets = new Vector<>();
        mEBullets = new Vector<>();
    }

    private void initShaderPrograms(){
        mShaderPrograms = new HashMap<>();
        mShaderPrograms.put("default", new CubeShaderProgram());
        mShaderPrograms.put("bullet", new BulletShaderProgram());
        for(ShaderProgram p : mShaderPrograms.values()){
            p.initProgram();
        }
    }

    private void initTextures(Map<String, Texture> textures){
        Log.i("textures", textures.size()+"");
        mTextures = textures;
        for (Texture t : mTextures.values())
        {
            GLES30.glGenTextures(1, t.mTextureID, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE, t.mData);
        }
    }

    private void initObjects(){
        File sdCard = Environment.getExternalStorageDirectory();

        ArrayList<String> names = new ArrayList<String>();
        names.add("mothership");
        names.add("simplecraft");
        names.add("plane"); // b2
        names.add("ufo");
        //String s = sdCard.getAbsolutePath() + "/arModel/b2/plane.obj";
        //String s = sdCard.getAbsolutePath() + "/arModel/ship/ufo.obj";

//        String s = sdCard.getAbsolutePath() + "/arModel/ship/simple_craft.obj";
//        int planeTex = mTextures.get("simpleCraft").mTextureID[0];
//        Vec3F planeScale = new Vec3F(0.3f, 0.3f, 0.3f);

        String hostCraftPath = sdCard.getAbsolutePath() + "/arModel/ship/hostcraft.obj";
        int hostCraftTex = mTextures.get("hostcraft").mTextureID[0];
        Vec3F hostCraftScale = new Vec3F(0.3f, 0.3f, 0.3f);

        hostPlane = new TestPlane(hostCraftPath);
        hostPlane.setShaderName("default");
        hostPlane.addTextureID(hostCraftTex);

        enemyPlanes = new TestPlane[4];
        for(int i = 0; i < 4; ++i) {
            String enemyCraftPath = sdCard.getAbsolutePath() + "/arModel/ship/" + names.get(i) + ".obj";
            TestPlane plane = new TestPlane(enemyCraftPath);
            int planeTex = mTextures.get(names.get(i)).mTextureID[0];
            plane.setShaderName("default");
            plane.addTextureID(planeTex);
            enemyPlanes[i] = plane;
            Log.i("loadOBJ", enemyPlanes[i].getBBox().toString());
        }

        mCube = new TestCube();
        mCube.setShaderName("default");
        mCube.addTextureID(mTextures.get("teapot").mTextureID[0]);

        mHostCraft = new HostCraft(mCamera);
        mHostCraft.copyFrom(hostPlane);
        mHostCraft.setScale(new Vec3F(0.1f, 0.1f, 0.1f));

        mEnemyCraft = new EnemyCraft(mCamera, mHostCraft);
        mEnemyCraft.copyFrom(enemyPlanes[level]);
        Vec3F planeScale = new Vec3F(0.3f, 0.3f, 0.3f);
        mEnemyCraft.setScale(planeScale);
        Log.i("enemyCraft", mEnemyCraft.getBBox().toString());
        mEnemyCraft.state = 0; //alive
    }

    public int update(Matrix44F modelViewMatrix, Matrix44F projMatrix, double timestamp){

        mModelViewMatrx = modelViewMatrix;
        mProjMatrix = projMatrix;
        mCamera.update(modelViewMatrix);
        if(mPreTimestamp < 0) {
            mPreTimestamp = timestamp;
            mHostCraft.setFireTime(timestamp);
            return UPDATE;
        }
        double deltaTime = timestamp - mPreTimestamp;

        //---------------result--------------
        if(win)
        {
            drawWin(timestamp);
            return SUCCESS;
        }
        else if(fail)
        {
            drawFail(timestamp);
            return FAIL;
        }

        if(LevelUpState){
            drawLevelUP(timestamp);
            return LEVELUP;
        }

        boolean explodeState = false;
        if(mEnemyCraft.state == 1) { // explode
            explodeState = mEnemyCraft.explode(timestamp);
            if (explodeState) {
                mEnemyCraft.state = 0;
                level++;
                if (level == 4)
                    return SUCCESS;
                onLevelUP(timestamp);
                return LEVELUP;
            }
            return UPDATE;
        }
        /*
        fire detection
         */
//----------------------------HostCraft Update Fire-------------------------
        mHostCraft.update(deltaTime, level);
        // Bullets update when deltaFireTime exceeds Firing Interval
        double deltaFireTime = timestamp - mHostCraft.getLastFireTime();
        if(deltaFireTime >= HostCraft.FiringInterval) {
            synchronized (mLock) {
                for (int i=0; i<mBulletsToBeAppend; i++) {
                    Bullet b = mHostCraft.openFire();
                    b.setMeshFrom(mCube);
                    b.setShaderName("bullet");
                    b.getBBox().setPos(b.getPosition());
                    mBullets.add(b);
                }
                mBulletsToBeAppend = 0;
            }
            mHostCraft.setFireTime(timestamp);
        }
//------------------------------Enemy Craft Update Fire-------------------
        mEnemyCraft.update(deltaTime, level);
        if(mEnemyCraft.Fire()){
            synchronized (mLock) {
                //Log.i("EBullets", "ebnum update: "+mEBullets.size());
                EnemyBullet b = mEnemyCraft.openFire();
                b.setMeshFrom(mCube);
                b.setShaderName("bullet");
                mEBullets.add(b);
            }
        }
        mEnemyCraft.SubPlaneFire(mEBullets, mCube);
        mEnemyCraft.SubPlaneDerive(mEBullets, mCube);
        mEnemyCraft.SubPlaneExplode(timestamp);
        /*
        collision detection
         */
        int hit = 0;

        //---------------my bullets test--------------

        for(int i=0; i<mBullets.size();){
            Bullet b = mBullets.get(i);
            b.update(deltaTime);
            if(b.state == 0 && b.Collide(mEnemyCraft)) { // hit enemy if still alive
                hit++;
                Log.i("collisionDetection", "collide");
                if(mEnemyCraft.beHit(b)==DESTROY){
                    mEnemyCraft.state = 1;
                }
                mBullets.remove(i);
            }
            else if(mEnemyCraft.SubPlaneCollide(b))
                mBullets.remove(i);
            else
            {
                Vec3F pos = b.getPosition();
                if(pos.getData()[2] < -1)
                    mBullets.remove(i);
                else
                    i++;
            }

        }
        mEnemyCraft.hit = hit;

        hit = 0;
        //---------------craft collision test--------------
        double hitInfo = mEnemyCraft.CollideCraft(timestamp);
        if(hitInfo != 0)
            hit++;
        if(DESTROY == hitInfo)
            return FAIL;

        //---------------enemy bullets test--------------

        for(int i=0; i<mEBullets.size();){
            EnemyBullet b = mEBullets.get(i);
            b.update(deltaTime);
            if(b.Collide(mHostCraft)){
                hit++;
                if(mHostCraft.beHit(b)==DESTROY){
                    return FAIL;
                }
                mEBullets.remove(i);
            }else {
                float[] pos = b.getPosition().getData();
                if (pos[2] > 3)
                    mEBullets.remove(i);
                else
                    i++;
            }
        }
        mHostCraft.hit = hit;
        mPreTimestamp = timestamp;
        return UPDATE;
    }

    public void draw(){
        mEnemyCraft.draw(this);
        SampleUtils.checkGLError("after mEnemyCraft");
        mHostCraft.draw(this);
        SampleUtils.checkGLError("after mHostCraft");
        for(Bullet b : mBullets)
        {
            b.draw(this);
        }
        SampleUtils.checkGLError("after mBullets");
        for(EnemyBullet b : mEBullets)
            b.draw(this);
        SampleUtils.checkGLError("after mEBullets");

    }

    public void onLevelUP(double timestamp){
        mEnemyCraft.copyFrom(enemyPlanes[level]);
        Vec3F planeScale = new Vec3F(0.3f, 0.3f, 0.3f);
        mEnemyCraft.setScale(planeScale);
        mEnemyCraft.resetState();

        LevelUpState = true;
        IntervalTimestamp = timestamp;
        mEnemyCraft.restart(level);
        mHostCraft.restart(level);
        mBullets.clear();
        mEBullets.clear();
    }

    public void drawLevelUP(double timestamp){
        double deltatime;
        int counter;
        deltatime = timestamp-IntervalTimestamp;
        if(deltatime > MAXINTERVAL){
            LevelUpState = false;
            return;
        }
        counter = 3;
        if(deltatime<2)
            counter = 3;
        else if (deltatime < 4)
            counter = 2;
        else if(deltatime < 6)
            counter = 1;
        Log.i("levelup", "drawLevelUP: delta time="+deltatime+" counter="+counter);
        TestCube up = new TestCube();
        up.setShaderName("default");
        up.addTextureID(mTextures.get(counter+"").mTextureID[0]);
        up.setScale(new Vec3F(0.05f, 0.05f, 0.05f));
        Vec3F Position = SampleMath.Vec3FTransform(new Vec3F(0, 0.0f, 0.2f), mCamera.getViewToWorld());
        up.setPosition(new Vec3F(0, 0.0f, 0.0f));
        up.draw(this);
    }

    public void drawWin(double timestamp)
    {
        TestCube up = new TestCube();
        up.setShaderName("default");
        up.addTextureID(mTextures.get("win").mTextureID[0]); // win.png
        up.setScale(new Vec3F(0.05f, 0.05f, 0.05f));
        Vec3F Position = SampleMath.Vec3FTransform(new Vec3F(0, 0.0f, 0.2f), mCamera.getViewToWorld());
        up.setPosition(new Vec3F(0, 0.0f, 0.0f));
        up.draw(this);
    }

    public void drawFail(double timestamp)
    {
        TestCube up = new TestCube();
        up.setShaderName("default");
        up.addTextureID(mTextures.get("fail").mTextureID[0]); // win.png
        up.setScale(new Vec3F(0.05f, 0.05f, 0.05f));
        Vec3F Position = SampleMath.Vec3FTransform(new Vec3F(0, 0.0f, 0.2f), mCamera.getViewToWorld());
        up.setPosition(new Vec3F(0, 0.0f, 0.0f));
        up.draw(this);
    }

    public double[] getHP(){
        double HP[] = new double[2];
        HP[0]=mHostCraft.getHP();
        HP[1]=mEnemyCraft.getHP();
        return HP;
    }

    public boolean processTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            synchronized (mLock){
                if(mBulletsToBeAppend == 0)
                    mBulletsToBeAppend++;
            }
            return true;
        }
        return false;
    }

    public HostCraft getHostCraft() { return mHostCraft; }
    public GameCamera getCamera(){ return mCamera; }
    public Matrix44F getModelViewMatrix() { return mModelViewMatrx; }
    public Matrix44F getProjMatrix() { return mProjMatrix; }
    public ShaderProgram getShaderProgram(String name) { return mShaderPrograms.get(name); }
}
