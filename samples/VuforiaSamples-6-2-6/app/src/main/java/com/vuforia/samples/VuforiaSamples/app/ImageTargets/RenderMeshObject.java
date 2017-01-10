package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Matrix44F;
import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.MeshObject;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;

import java.nio.Buffer;
import java.util.Vector;

/**
 * Created by Unicorn on 2017/1/3.
 */
public class RenderMeshObject extends MeshObject{
    protected Vec3F mPosition = new Vec3F(0, 0, 0);
    protected float[] mRotateMatrix = new float[16];
    protected static final int DESTROY=-1;
    protected static final int LIVE=0;

    protected Vec3F mScale = new Vec3F(1, 1, 1);

    protected Buffer mVertBuff;
    protected Buffer mTexCoordBuff;
    protected Buffer mNormBuff;
    protected Buffer mIndBuff;

    protected int mIndicesNumber = 0;
    protected int mVerticesNumber = 0;

    protected String mShaderName;
    protected Vector<Integer> mTextureID = null;
    protected int mTextureIndex = 0;
    protected BBox mBBox = new BBox();
    protected float[] mBBoxCenter = new float[3];

    public int hit = 0;
    public boolean invincible = false;
    protected int level;
    protected float explodeRadius;
    protected double explodeTimeStamp = -1.0;
    protected boolean show = true;
    public int state; // 0: alive 1: explode 2: end

    public RenderMeshObject(){
        Matrix.setIdentityM(mRotateMatrix, 0);
    }

    public RenderMeshObject clone(){
        RenderMeshObject obj = new RenderMeshObject();
        obj.mPosition.setData(mPosition.getData());
        obj.mRotateMatrix = mRotateMatrix.clone();
        obj.mScale.setData(mScale.getData());
        obj.copyFrom(this);
        return obj;
    }

    public void copyFrom(RenderMeshObject obj){
        setMeshFrom(obj);
        mShaderName = obj.mShaderName;
        mTextureID = (Vector<Integer>) obj.mTextureID.clone();
    }

    public void setMeshFrom(RenderMeshObject obj){
        mVertBuff = obj.mVertBuff;
        mTexCoordBuff = obj.mTexCoordBuff;
        mNormBuff = obj.mNormBuff;
        mIndBuff = obj.mIndBuff;
        mIndicesNumber = obj.mIndicesNumber;
        mVerticesNumber = obj.mVerticesNumber;
        mBBox = obj.mBBox.clone();
        mBBoxCenter = obj.mBBoxCenter.clone();
    }

    public void draw(GameEngine engine){
        // Model View Projection Matrix
        float[] modelViewProj = getMVPMatrix(engine);
        // Set Shader And Uniforms
        ShaderProgram program = engine.getShaderProgram(mShaderName);
        program.useProgram();
        SampleUtils.checkGLError("0");
        int vertexHandle = program.getHandler("vertexPosition");
        int vertexNormalHandle = program.getHandler("vertexNormal");
        int textureCoordHandle = program.getHandler("vertexTexCoord");
        Log.i("shaderInfo", ""+vertexNormalHandle);
        int mvpMatrixHandle = program.getHandler("modelViewProjectionMatrix");
        int texSampler2DHandle = program.getHandler("texSampler2D");
        int hitTagHandle = program.getHandler("hit");
        GLES30.glVertexAttribPointer(vertexHandle, 3, GLES30.GL_FLOAT, false, 0, mVertBuff);
        GLES30.glVertexAttribPointer(vertexNormalHandle, 3, GLES30.GL_FLOAT, false, 0, mNormBuff);
        GLES30.glVertexAttribPointer(textureCoordHandle, 2, GLES30.GL_FLOAT, false, 0, mTexCoordBuff);
        GLES30.glEnableVertexAttribArray(vertexHandle);
        GLES30.glEnableVertexAttribArray(vertexNormalHandle);
        GLES30.glEnableVertexAttribArray(textureCoordHandle);
        SampleUtils.checkGLError("1");
        //hit test
        if(invincible)
            hit = 0;
        GLES30.glUniform1i(hitTagHandle, hit);
        // activate texture 0, bind it, and pass to shader
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTextureID.get(mTextureIndex));
        GLES30.glUniform1i(texSampler2DHandle, 0);
        // pass the model view matrix to the shader
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProj, 0);
        SampleUtils.checkGLError("2");
        // finally draw
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        if(show)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndicesNumber, GLES30.GL_UNSIGNED_SHORT, mIndBuff);
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mVerticesNumber);
        // disable the enabled arrays
        GLES30.glDisableVertexAttribArray(vertexHandle);
        GLES30.glDisableVertexAttribArray(textureCoordHandle);
        GLES30.glDisableVertexAttribArray(vertexNormalHandle);
        SampleUtils.checkGLError("3");
    }

    protected float[] getMVPMatrix(GameEngine engine){
        // Model View Projection Matrix
        Matrix44F baseModelView = engine.getModelViewMatrix();
        float[] modelViewArr = baseModelView.getData().clone();
        float[] scale = mScale.getData();
        float[] pos = mPosition.getData();
        float[] afterRotate = new float[16];
        float[] modelViewProj = new float[16];

        // V x M x ...
        Matrix.translateM(modelViewArr, 0, pos[0], pos[1], pos[2]);
        Matrix.multiplyMM(afterRotate, 0, modelViewArr, 0, mRotateMatrix, 0);
        Matrix.scaleM(afterRotate, 0, scale[0], scale[1], scale[2]);
        Matrix.translateM(afterRotate, 0, -mBBoxCenter[0], -mBBoxCenter[1], -mBBoxCenter[2]); // 先移动到中心

        Matrix.multiplyMM(modelViewProj, 0, engine.getProjMatrix().getData(), 0, afterRotate, 0);
        return modelViewProj;
    }

    public void readMeshFromFile(String path){

    }

    public void rotate(float angle, Vec3F axi){
        float[] axiData = axi.getData();
        float[] rotateMatrixData = new float[16];
        Matrix.setRotateM(rotateMatrixData, 0, angle, axiData[0], axiData[1], axiData[2]);
        float[] afterRotate = new float[16];
        Matrix.multiplyMM(afterRotate, 0, rotateMatrixData, 0, mRotateMatrix, 0);
        mRotateMatrix = afterRotate;
        Matrix44F rotateMatrix = new Matrix44F();
        rotateMatrix.setData(rotateMatrixData);
        //mPosition = SampleMath.Vec3FTransform(mPosition, rotateMatrix);
    }

    public void recoverOrientation()
    {
        float[] rotateMatrixData = new float[16];
        Matrix.setRotateM(mRotateMatrix, 0, 0, 1, 0, 0);
    }

    public Vec3F getPosition() { return new Vec3F(mPosition); }
    public Vec3F getScale() { return mScale; }

    public void setScale(Vec3F scale)
    {
        this.mBBox.setScale(scale);
        mScale.setData(scale.getData());
    }
    public void setPosition(Vec3F pos) { mPosition.setData(pos.getData()); }
    public void addTextureID(int id) {
        if(mTextureID == null)
            mTextureID = new Vector<>();
        mTextureID.add(id);
    }
    public void setShaderName(String name) { mShaderName = name; }



    public int getNumObjectIndex()
    {
        return mIndicesNumber;
    }

    @Override
    public int getNumObjectVertex()
    {
        return mVerticesNumber;
    }

    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
            default:
                break;

        }

        return result;
    }

    // temp
    protected void setBBoxByVertices(double[] vertices){
        Vec3F v = new Vec3F();
        float[] data = new float[3];
        for(int i=0; i<vertices.length; i+=3){
            data[0] = (float)vertices[i];
            data[1] = (float)vertices[i + 1];
            data[2] = (float)vertices[i + 2];
            v.setData(data);
            mBBox.merge(v);
        }
        mBBoxCenter = mBBox.getCenter().getData();
    }

    public BBox getBBox()
    {
        return this.mBBox;
    }

    public boolean Collide(RenderMeshObject target)
    {
        Log.i("collisionDetection","teapot bb"+target.getBBox().toString());
        return this.mBBox.Collide(target.getBBox());
    }
}
