package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

/**
 * Created by Unicorn on 2017/1/4.
 */
public class BBox {
    private Vec3F mMin;
    private Vec3F mMax;
    private Vec3F originMin;
    private Vec3F originMax;
    private boolean mValid;

    public BBox(){
        mMin = new Vec3F(0, 0, 0);
        mMax = new Vec3F(0, 0, 0);
        originMin = new Vec3F(0, 0, 0);
        originMax = new Vec3F(0, 0, 0);
        mValid = false;
    }

    public BBox clone(){
        BBox box = new BBox();
        box.mMin.setData(mMin.getData());
        box.mMax.setData(mMax.getData());
        box.originMax.setData(box.mMax.getData());
        box.originMin.setData(box.mMin.getData());
        box.mValid = mValid;
        return box;
    }

    public void merge(Vec3F pos){
        if(!mValid){
            mMax.setData(pos.getData());
            mMin.setData(pos.getData());
            mValid = true;
        }
        else{
            float[] minData = mMin.getData();
            float[] maxData = mMax.getData();
            float[] posData = pos.getData();
            for(int i=0; i<3; i++) {
                minData[i] = GameMath.min(minData[i], posData[i]);
                maxData[i] = GameMath.max(maxData[i], posData[i]);
            }
            mMin.setData(minData);
            mMax.setData(maxData);
        }
    }

    public Vec3F getMin() { return mMin; }
    public Vec3F getMax() { return mMax; }
    public Vec3F getOriginMin() { return originMin; }
    public Vec3F getOriginMax() { return originMax; }
    public Vec3F getCenter() {
        Vec3F center = SampleMath.Vec3FAdd(mMin, mMax);
        center = SampleMath.Vec3FScale(center, 0.5f);
        return center;
    }
    public Vec3F getSize(){
        return SampleMath.Vec3FSub(mMax, mMin);
    }

    @Override
    public String toString() {
        float[] min = mMin.getData();
        float[] max = mMax.getData();
        String minInfo = min[0]+" "+min[1]+" "+min[2];
        String maxInfo = max[0]+" "+max[1]+" "+max[2];
        return minInfo + " " + maxInfo;
    }

    public void setScale(Vec3F scale)
    {
        float[] s = scale.getData();
        float[] min = this.mMin.getData();
        min[0] *= s[0];
        min[1] *= s[1];
        min[2] *= s[2];

        float[] max = this.mMax.getData();
        max[0] *= s[0];
        max[1] *= s[1];
        max[2] *= s[2];

        this.mMin.setData(min);
        this.mMax.setData(max);
        this.originMin.setData(min);
        this.originMax.setData(max);
    }

    public void setPos(Vec3F pos) // update position
    {
        mMax = SampleMath.Vec3FAdd(mMax, pos);
        mMin = SampleMath.Vec3FAdd(mMin, pos);
    }

    public void setAbsPos(Vec3F pos) // set position by origin bb
    {
        mMax = SampleMath.Vec3FAdd(originMax, pos);
        mMin = SampleMath.Vec3FAdd(originMin, pos);
    }

    //Separating Axis Theorem
    public boolean Collide(BBox aabb)
    {
        float[] min = this.mMin.getData();
        float[] max = this.mMax.getData();
        float[] tMin = aabb.getMin().getData();
        float[] tMax = aabb.getMax().getData();
        for(int i = 0; i < 3; ++i) //xy -> yz -> xz
        {
            //self
            double[][] s = new double[4][3];
            s[0][i] = min[i]; s[0][(i+1) % 3] = min[(i+1) % 3];
            s[1][i] = max[i]; s[1][(i+1) % 3] = min[(i+1) % 3];
            s[2][i] = max[i]; s[2][(i+1) % 3] =  max[(i+1) % 3];
            s[3][i] = min[i]; s[3][(i+1) % 3] = max[(i+1) % 3];

            //target
            double[][] t = new double[4][3];
            t[0][i] = tMin[i]; t[0][(i+1) % 3] = tMin[(i+1) % 3];
            t[1][i] = tMax[i]; t[1][(i+1) % 3] = tMin[(i+1) % 3];
            t[2][i] = tMax[i]; t[2][(i+1) % 3] =  tMax[(i+1) % 3];
            t[3][i] = tMin[i]; t[3][(i+1) % 3] = tMax[(i+1) % 3];

            boolean tempRes = false;
            for(int j = 0; j < 4; ++j)
            {
                if((s[j][i] > tMin[i] && s[j][i] < tMax[i] && s[j][(i+1) % 3] > tMin[(i+1) % 3] && s[j][(i+1) % 3] < tMax[(i+1) % 3])
                        ||
                        (t[j][i] > min[i] && t[j][i] < max[i] && t[j][(i+1) % 3] > min[(i+1) % 3] && t[j][(i+1) % 3] < max[(i+1) % 3]))
                    tempRes = true; // no axis found yet
            }
            if(!tempRes) // axis found in this dimension
                return false;
        }

        return true;
    }

    public boolean PointBoxCollide(Vec3F point)
    {
        float[] data = point.getData();
        boolean x = data[0] > mMin.getData()[0] && data[0] < mMax.getData()[0];
        boolean y = data[1] > mMin.getData()[1] && data[1] < mMax.getData()[1];
        boolean z = data[2] > mMin.getData()[2] && data[2] < mMax.getData()[2];
        return x && y && z;
    }

}
