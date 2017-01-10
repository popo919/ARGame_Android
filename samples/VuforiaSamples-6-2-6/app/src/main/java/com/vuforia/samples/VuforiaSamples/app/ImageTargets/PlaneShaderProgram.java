package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;

import com.vuforia.samples.SampleApplication.utils.SampleUtils;

/**
 * Created by PC20150531 on 2017/1/9.
 */

public class PlaneShaderProgram extends ShaderProgram {
    @Override
    public void initProgram(){
        setShaderSrc();
        mProgramID = SampleUtils.createProgramFromShaderSrc(mVertexShaderSrc, mFragmentShaderSrc);
        int handler = GLES30.glGetAttribLocation(mProgramID, "vertexPosition");
        mHandlers.put("vertexPosition", handler);
        handler = GLES30.glGetUniformLocation(mProgramID, "modelViewProjectionMatrix");
        mHandlers.put("modelViewProjectionMatrix", handler);
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
                + "void main() \n"
                + "{ \n" + " \n"
                + "   gl_FragColor = vec4(0.9804, 0.9804, 0.82353, 1); \n"
                + "} \n";
    }
}
