package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;

import com.vuforia.samples.SampleApplication.utils.SampleUtils;

/**
 * Created by Unicorn on 2017/1/9.
 */
public class BulletShaderProgram extends ShaderProgram {
    @Override
    public void initProgram(){
        setShaderSrc();
        mProgramID = SampleUtils.createProgramFromShaderSrc(mVertexShaderSrc, mFragmentShaderSrc);
        int handler = GLES30.glGetAttribLocation(mProgramID, "vertexPosition");
        mHandlers.put("vertexPosition", handler);
        handler = GLES30.glGetUniformLocation(mProgramID, "modelViewProjectionMatrix");
        mHandlers.put("modelViewProjectionMatrix", handler);
        handler = GLES30.glGetUniformLocation(mProgramID,"bullet");
        mHandlers.put("bullet",handler);
    }

    @Override
    protected void setShaderSrc(){
        mVertexShaderSrc = " \n" + "\n"
                + "attribute vec4 vertexPosition; \n"+ "\n"
                + "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
                + "void main() \n" + "{ \n"
                + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
                + "} \n";
        mFragmentShaderSrc = " \n" + "\n"
                + "precision mediump float; \n" + " \n"
                + "uniform int bullet; \n " + "\n"
                + "void main() \n"
                + "{ \n" + " \n"
                + "   if (bullet == 1) gl_FragColor = vec4(1.0, 0.82, 0.02, 1); \n"
                + "   else gl_FragColor = vec4(1.0, 0.4, 0.4, 1); \n"
                + "} \n";
    }
}
