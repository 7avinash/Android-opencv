package com.avinash.myopencv;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Sample2 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final int VIEW_MODE_RGBA=0;
    private static final int VIEW_MODE_GRAY=1;
    private static final int VIEW_MODE_CARRY=2;
    private static final int VIEW_MODE_FEATURES=5;

    private int myViewMode;
    private Mat myRGBA;
    private Mat myIntermediateMat;
    private Mat myGray;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewGray;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewFeatures;

    private CameraBridgeViewBase cameraBridgeViewBase;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.loadLibrary("native-lib");
                    cameraBridgeViewBase.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_sample2);
        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.java_surface_view1);
        cameraBridgeViewBase.setVisibility(CameraBridgeViewBase.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview Gray");
        mItemPreviewCanny = menu.add("Preview Canny");
        mItemPreviewFeatures = menu.add("Preview Features");
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,baseLoaderCallback);
        }
        else{
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        myRGBA = new Mat(height, width, CvType.CV_8UC4);
        myIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        myGray = new Mat(height,width,CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        myRGBA.release();
        myGray.release();
        myIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        int viewMode = myViewMode;

        switch (viewMode){
            case VIEW_MODE_GRAY:
                Imgproc.cvtColor(inputFrame.gray(),myRGBA,Imgproc.COLOR_GRAY2RGBA,4);
                break;
            case VIEW_MODE_RGBA:
                myRGBA = inputFrame.rgba();
                break;
            case VIEW_MODE_CARRY:
                myRGBA = inputFrame.rgba();
                Imgproc.Canny(inputFrame.gray(),myIntermediateMat,80,100);
                Imgproc.cvtColor(myIntermediateMat,myRGBA,Imgproc.COLOR_GRAY2RGBA,4);
                break;
            case VIEW_MODE_FEATURES:
                myRGBA = inputFrame.rgba();
                myGray = inputFrame.gray();
                FindFeatures(myGray.getNativeObjAddr(),myRGBA.getNativeObjAddr());
                break;
        }

        return myRGBA;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item==mItemPreviewRGBA){
            myViewMode = VIEW_MODE_RGBA;
        }
        else if(item==mItemPreviewGray){
            myViewMode = VIEW_MODE_GRAY;
        }else if(item==mItemPreviewCanny){
            myViewMode = VIEW_MODE_CARRY;
        }else if(item==mItemPreviewFeatures){
            myViewMode=VIEW_MODE_FEATURES;
        }

        return true;
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
}

