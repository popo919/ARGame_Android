/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.vuforia.Device;
import com.vuforia.Matrix44F;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vec3F;
import com.vuforia.samples.SampleApplication.SampleAppRenderer;
import com.vuforia.samples.SampleApplication.SampleAppRendererControl;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Texture;

import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl
{
    private static final String LOGTAG = "ImageTargetRenderer";

    private static final int UPDATE=0;
    private static final int SUCCESS = 1;
    private static final int FAIL = 2;
    private static final int LEVELUP = 3;
    private static final int INTERVAL = 35;

    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;
    private SampleAppRenderer mSampleAppRenderer;

    private Map<String,Texture> mTextures;

    private GameEngine mEngine;

    private boolean mIsActive = false;
    private boolean mModelIsLoaded = false;


    public ImageTargetRenderer(ImageTargets activity, SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.01f , 5f);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();
    }

    public boolean processTouchEvent(MotionEvent event){
        if(mEngine != null)
            return mEngine.processTouchEvent(event);
        return false;
    }
    

    public void setActive(boolean active)
    {
        mIsActive = active;
        if(mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        initRendering();
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
        mEngine = new GameEngine();
        mEngine.init(mTextures);
        mSampleAppRenderer.setBackgroundTexID(mTextures.get("background").mTextureID[0]);

        if(!mModelIsLoaded) {

            mModelIsLoaded = true;

            // Hide the Loading Dialog
            mActivity.loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        }

    }

    public void updateConfiguration()
    {
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
    }

    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix)
    {
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES30.glEnable(GLES30.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);

        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());

            Matrix44F projMatrx = new Matrix44F();
            projMatrx.setData(projectionMatrix);

                int drawstate = mEngine.update(modelViewMatrix_Vuforia, projMatrx, state.getFrame().getTimeStamp());
                switch (drawstate) {
                    case UPDATE:
                        Log.i("updateinfo", "update");
                        break;
                    case SUCCESS:
                        Log.i("updateinfo", "succ");
                        mEngine.win = true;
                        //mActivity.win();
                        break;
                    case FAIL:
                        Log.i("updateinfo", "fail");
                        mEngine.fail = true;
                        //mActivity.fail();
                        break;
                    case LEVELUP:
                        Log.i("updateinfo", "levelup");
                        break;
                    default:
                        break;
                }
                GameCamera cam = mEngine.getCamera();

                Vec3F camPos = cam.getPosition();
                Log.i("cameraInfo", camPos.getData()[0] + " " + camPos.getData()[1] + " " + camPos.getData()[2]);

                if (!mActivity.isExtendedTrackingActive() && drawstate == UPDATE) {
                    mEngine.draw();
                    mActivity.updateUI(mEngine.getHP());
                }

                SampleUtils.checkGLError("Render Frame");
        }

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);

    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Map<String, Texture> textures)
    {
        mTextures = textures;

    }


}
