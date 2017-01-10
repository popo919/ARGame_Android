package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;

import com.vuforia.samples.SampleApplication.utils.SampleUtils;

/**
 * Created by Unicorn on 2017/1/4.
 */
public class CubeShaderProgram extends ShaderProgram {
    @Override
    public void initProgram(){
        setShaderSrc();
        mProgramID = SampleUtils.createProgramFromShaderSrc(mVertexShaderSrc, mFragmentShaderSrc);
        int handler = GLES30.glGetAttribLocation(mProgramID, "vertexPosition");
        mHandlers.put("vertexPosition", handler);
        handler = GLES30.glGetAttribLocation(mProgramID, "vertexNormal");
        mHandlers.put("vertexNormal", handler);
        handler = GLES30.glGetAttribLocation(mProgramID, "vertexTexCoord");
        mHandlers.put("vertexTexCoord", handler);
        handler = GLES30.glGetUniformLocation(mProgramID, "modelViewProjectionMatrix");
        mHandlers.put("modelViewProjectionMatrix", handler);
        handler = GLES30.glGetUniformLocation(mProgramID, "texSampler2D");
        mHandlers.put("texSampler2D", handler);
        handler = GLES30.glGetUniformLocation(mProgramID, "hit");
        mHandlers.put("hit", handler);
    }

    @Override
    protected void setShaderSrc(){
        mVertexShaderSrc = " \n" + "\n"
                + "attribute vec4 vertexPosition; \n"+ "\n"
                + "attribute vec4 vertexNormal; \n"+ "\n"
                + "attribute vec2 vertexTexCoord; \n" + "\n"
                + "varying vec2 texCoord; \n" + "\n"
                + "varying vec3 fragNormal; \n" + "\n"
                + "uniform mat4 modelViewProjectionMatrix; \n" + "\n"
                + "void main() \n" + "{ \n"
                + "   fragNormal = vertexNormal.xyz; \n"
                + "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n"
                + "   texCoord = vertexTexCoord; \n"
                + "} \n";
        mFragmentShaderSrc = " \n" + "\n"
                + "precision mediump float; \n" + " \n"
                + "varying vec2 texCoord; \n"
                + "varying vec3 fragNormal; \n" + "\n"
                + "uniform int hit; \n" + "\n"
                + "uniform sampler2D texSampler2D; \n" + " \n"
                + "void main() \n"
                + "{ \n" + " \n"
                + "  if(hit == 0)  \n"
                + "     gl_FragColor = texture2D(texSampler2D, texCoord).xyzw;\n"
                + "  else gl_FragColor = vec4(1.0, 0.4, 0.4, 1.0); \n"
                + "} \n";
    }
}
