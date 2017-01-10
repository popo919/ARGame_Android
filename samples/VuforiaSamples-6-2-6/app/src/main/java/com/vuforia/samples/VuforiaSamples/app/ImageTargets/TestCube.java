package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

/**
 * Created by Unicorn on 2017/1/5.
 */
public class TestCube extends RenderMeshObject {
    public TestCube(){
        setAsBox();
    }

    private void setAsBox(){
        double cubeVertices[]  = {
                -1.00f, -1.00f, 1.00f, // front
                1.00f, -1.00f, 1.00f,
                1.00f, 1.00f, 1.00f,
                -1.00f, 1.00f, 1.00f,
                -1.00f, -1.00f, -1.00f, // back
                1.00f, -1.00f, -1.00f,
                1.00f, 1.00f, -1.00f,
                -1.00f, 1.00f, -1.00f,
                -1.00f, -1.00f, -1.00f, // left
                -1.00f, -1.00f, 1.00f,
                -1.00f, 1.00f, 1.00f,
                -1.00f, 1.00f, -1.00f,
                1.00f, -1.00f, -1.00f, // right
                1.00f, -1.00f, 1.00f,
                1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, -1.00f,
                -1.00f, 1.00f, 1.00f, // top
                1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, -1.00f,
                -1.00f, 1.00f, -1.00f,
                -1.00f, -1.00f, 1.00f, // bottom
                1.00f, -1.00f, 1.00f,
                1.00f, -1.00f, -1.00f,
                -1.00f, -1.00f, -1.00f };
        double cubeTexcoords[] = {
                0, 0, 1, 0, 1, 1, 0, 1,
                1, 0, 0, 0, 0, 1, 1, 1,
                0, 0, 1, 0, 1, 1, 0, 1,
                1, 0, 0, 0, 0, 1, 1, 1,
                0, 0, 1, 0, 1, 1, 0, 1,
                1, 0, 0, 0, 0, 1, 1, 1 };
        double cubeNormals[]   = {
                0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
                1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
                0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
        };
        short  cubeIndices[]   = {
                0, 1, 2, 0, 2, 3, // front
                4, 6, 5, 4, 7, 6, // back
                8, 9, 10, 8, 10, 11, // left
                12, 14, 13, 12, 15, 14, // right
                16, 17, 18, 16, 18, 19, // top
                20, 22, 21, 20, 23, 22  // bottom
        };
        mVertBuff = fillBuffer(cubeVertices);
        mTexCoordBuff = fillBuffer(cubeTexcoords);
        mNormBuff = fillBuffer(cubeNormals);
        mIndBuff = fillBuffer(cubeIndices);
        mIndicesNumber = cubeIndices.length;
        mVerticesNumber = cubeVertices.length / 3;
        setBBoxByVertices(cubeVertices);
    }
}
