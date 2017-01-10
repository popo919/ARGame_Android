package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Unicorn on 2017/1/9.
 */
public abstract class ShaderProgram {
    protected int mProgramID = 0;
    protected Map<String, Integer> mHandlers = new HashMap<>();
    protected String mVertexShaderSrc;
    protected String mFragmentShaderSrc;

    public abstract void initProgram();

    public void useProgram(){
        GLES30.glUseProgram(mProgramID);
    }

    public int getHandler(String name){
        return mHandlers.get(name);
    }

    protected abstract void setShaderSrc();
}
