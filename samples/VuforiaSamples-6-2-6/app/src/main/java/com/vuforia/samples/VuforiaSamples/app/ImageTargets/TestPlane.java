package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;
import android.util.Log;

import com.vuforia.samples.SampleApplication.utils.oobjloader.builder.Build;
import com.vuforia.samples.SampleApplication.utils.oobjloader.parser.Parse;

/**
 * Created by Unicorn on 2017/1/5.
 */
public class TestPlane extends RenderMeshObject {
    public TestPlane(){

    }

    public TestPlane(String path)
    {
        Build builder = new Build();
        Log.i("loadOBJ", "plane");
        try {
            Parse obj = new Parse(builder, path);
            Log.i("loadOBJ", "v size "+builder.verticesG.size());
            Log.i("loadOBJ", "n size "+builder.verticesN.size());
            Log.i("loadOBJ", "t size "+builder.verticesT.size());
            Log.i("loadOBJ", "face size "+builder.faceVerticeList.size());
//            for(int i = 0; i < builder.faceVerticeList.size(); ++i)
//                Log.i("loadOBJ", builder.faceVerticeList.get(i).index+"");
        } catch (java.io.FileNotFoundException e) {
            Log.i("loadOBJ", "file not found");
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        double[] vertices = new double[builder.faces.size() * 3 * 3];
        double[] normals = new double[builder.faces.size() * 3 * 3];
        double[] tex = new double[builder.faces.size() * 3 * 2];
        short[] ind = new short[builder.faces.size() * 3];
        //builder.faces.get(0).vertices.get(0).index;

        for(int i=0; i< builder.faces.size() * 3; i++){
            vertices[3 * i] = builder.faces.get(i / 3).vertices.get(i % 3).v.x;
            vertices[3 * i + 1] = builder.faces.get(i / 3).vertices.get(i % 3).v.y;
            vertices[3 * i + 2] = builder.faces.get(i / 3).vertices.get(i % 3).v.z;
        }
        for(int i=0; i< builder.faces.size() * 3; i++){
            normals[3 * i] = builder.faces.get(i / 3).vertices.get(i % 3).n.x;
            normals[3 * i + 1] = builder.faces.get(i / 3).vertices.get(i % 3).n.y;
            normals[3 * i + 2] = builder.faces.get(i / 3).vertices.get(i % 3).n.z;
        }
        for(int i=0; i< builder.faces.size() * 3; i++){
            tex[2 * i] = builder.faces.get(i / 3).vertices.get(i % 3).t.u;
            tex[2 * i + 1] = builder.faces.get(i / 3).vertices.get(i % 3).t.v;
        }

        for(int i = 0; i < builder.faces.size() * 3; ++i)
        {
            ind[i] = (short)i;
        }

//        for(int i = 0; i < builder.faces.size(); ++i)
//        {
//            Log.i("loadOBJ", "face vertex size: "+builder.faces.get(i).vertices.size());
//        }
//        for(int i = 0; i < builder.faceVerticeList.size() ; ++i)
//        {
//            vertices[3 * i] = builder.faceVerticeList.get(i).v.x;
//            vertices[3 * i + 1] = builder.faceVerticeList.get(i).v.y;
//            vertices[3 * i + 2] = builder.faceVerticeList.get(i).v.z;
//
//            normals[3 * i] = builder.faceVerticeList.get(i).n.x;
//            normals[3 * i + 1] = builder.faceVerticeList.get(i).n.y;
//            normals[3 * i + 2] = builder.faceVerticeList.get(i).n.z;
//
//            tex[2 * i] = builder.faceVerticeList.get(i).t.u;
//            tex[2 * i + 1] = builder.faceVerticeList.get(i).t.v;
//
//            ind[i] = (short)i;
//        }

        mVertBuff = fillBuffer(vertices);
        mNormBuff = fillBuffer(normals);
        mTexCoordBuff = fillBuffer(tex);
        mIndBuff = fillBuffer(ind);
        mIndicesNumber = ind.length;
        mVerticesNumber = vertices.length / 3;
        setBBoxByVertices(vertices);
    }

    @Override
    public void draw(GameEngine engine){
        // Model View Projection Matrix
        float[] modelViewProj = getMVPMatrix(engine);
        // Set Shader And Uniforms
        ShaderProgram program = engine.getShaderProgram(mShaderName);
        program.useProgram();
        int vertexHandle = program.getHandler("vertexPosition");
        int mvpMatrixHandle = program.getHandler("modelViewProjectionMatrix");
        GLES30.glVertexAttribPointer(vertexHandle, 3, GLES30.GL_FLOAT, false, 0, mVertBuff);
        GLES30.glEnableVertexAttribArray(vertexHandle);
        // pass the model view matrix to the shader
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProj, 0);
        // finally draw
        //GLES30.glDisable(GLES30.GL_CULL_FACE);
        //if(show)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, mIndicesNumber, GLES30.GL_UNSIGNED_SHORT, mIndBuff);
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, mVerticesNumber);
        // disable the enabled arrays
        GLES30.glDisableVertexAttribArray(vertexHandle);
    }

}
