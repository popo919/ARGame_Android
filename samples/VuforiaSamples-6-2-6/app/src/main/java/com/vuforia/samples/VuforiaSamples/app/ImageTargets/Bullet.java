package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.utils.SampleMath;

import static com.vuforia.samples.SampleApplication.utils.SampleMath.Vec3FScale;

/**
 * Created by PC20150531 on 2017/1/2.
 */


public class Bullet extends RenderMeshObject{
    private Vec3F direction;
    private float speed;
    private Craft mBelong;
    private double harm=1.5;
    protected int bulletType; //1:enemy 2:myself
    public Bullet(Craft belong)
    {
        bulletType = 2;
        mBelong = belong;
        direction = new Vec3F();
        speed = 0.0f;
        mScale = new Vec3F(0.001f, 0.001f, 0.02f);
    }

    @Override
    public boolean Collide(RenderMeshObject target) {
        //Vec3F scaleDir = SampleMath.Vec3FScale(direction, mScale.getData()[2] * 0.5f);
        //Vec3F middlePoint = SampleMath.Vec3FAdd(mPosition, scaleDir);
        return target.mBBox.PointBoxCollide(mPosition);
    }

    public void set(Vec3F pos, Vec3F dir, float sp)
    {
        this.mPosition.setData(pos.getData());
        float[] dir_data = dir.getData();
        this.direction.setData(dir_data);
        // rotate
        float y_rot_angle = GameMath.radianToAngle(-(float)Math.atan(dir_data[0] / -dir_data[2]));
        Matrix.setIdentityM(mRotateMatrix, 0);

        Vec3F temp_dir = SampleMath.Vec3FNormalize(new Vec3F(dir_data[0], 0, dir_data[2]));
        float right_angle = GameMath.radianToAngle((float)Math.acos(SampleMath.Vec3FDot(temp_dir, direction)));
        Vec3F right = SampleMath.Vec3FNormalize(SampleMath.Vec3FCross(temp_dir, direction));
        float[] right_data = right.getData();
        Matrix.rotateM(mRotateMatrix, 0, right_angle, right_data[0], right_data[1], right_data[2]);

        Matrix.rotateM(mRotateMatrix, 0, y_rot_angle, 0, 1, 0);
        this.speed = sp;
    }

    public void update(double deltaTime)
    {
        Vec3F deltaDir = Vec3FScale(direction, (float)(speed * deltaTime));
        mPosition = SampleMath.Vec3FAdd(mPosition, deltaDir);
        this.mBBox.setPos(deltaDir);
    }
    public double getHarm(){return harm;}

    @Override
    public void draw(GameEngine engine){
        // Model View Projection Matrix
        float[] modelViewProj = getMVPMatrix(engine);
        // Set Shader And Uniforms
        ShaderProgram program = engine.getShaderProgram(mShaderName);
        program.useProgram();
        int vertexHandle = program.getHandler("vertexPosition");
        int mvpMatrixHandle = program.getHandler("modelViewProjectionMatrix");
        int bulletTypeHandle = program.getHandler("bullet");
        GLES30.glVertexAttribPointer(vertexHandle, 3, GLES30.GL_FLOAT, false, 0, mVertBuff);
        GLES30.glEnableVertexAttribArray(vertexHandle);
        // pass the model view matrix to the shader
        GLES30.glUniform1i(bulletTypeHandle, bulletType);
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProj, 0);
        // finally draw
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        //if(show)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndicesNumber, GLES30.GL_UNSIGNED_SHORT, mIndBuff);
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mVerticesNumber);
        // disable the enabled arrays
        GLES30.glDisableVertexAttribArray(vertexHandle);
    }

}
