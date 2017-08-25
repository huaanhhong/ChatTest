package com.example.huaanhhong.chattest.Linphone;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.huaanhhong.chattest.ChatUtils;
import com.example.huaanhhong.chattest.R;

import org.linphone.core.LinphoneCall;
import org.linphone.mediastream.Log;
import org.linphone.mediastream.Version;
import org.linphone.mediastream.video.AndroidVideoWindowImpl;
import org.linphone.mediastream.video.capture.hwconf.AndroidCameraConfiguration;

public class CallVideoFragment extends Fragment implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, CompatibilityScaleGestureListener {

    private SurfaceView mVideoView;
    private SurfaceView mCaptureView;
    private GestureDetector mGestureDetector;
    private CompatibilityScaleGestureDetector mScaleDetector;
    private AndroidVideoWindowImpl androidVideoWindowImpl;
    private CallActivity inCallActivity;
    private float mZoomFactor = 1.f;
    private float mZoomCenterX, mZoomCenterY;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        android.util.Log.i("CNN","callvideofragment_oncreate");
       View view;
        if (LinphoneManager.getLc().hasCrappyOpenGL()) {
            view = inflater.inflate(R.layout.video_no_opengl, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_call_video, container, false);
        }

        mVideoView = (SurfaceView) view.findViewById(R.id.videoSurface);
        mCaptureView = (SurfaceView) view.findViewById(R.id.videoCaptureSurface);
        mCaptureView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        fixZOrder(mVideoView, mCaptureView);

        androidVideoWindowImpl = new AndroidVideoWindowImpl(mVideoView, mCaptureView, new AndroidVideoWindowImpl.VideoWindowListener() {
            public void onVideoRenderingSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                mVideoView = surface;
                LinphoneManager.getLc().setVideoWindow(vw);
            }

            public void onVideoRenderingSurfaceDestroyed(AndroidVideoWindowImpl vw) {

            }

            public void onVideoPreviewSurfaceReady(AndroidVideoWindowImpl vw, SurfaceView surface) {
                mCaptureView = surface;
                LinphoneManager.getLc().setPreviewWindow(mCaptureView);
            }

            public void onVideoPreviewSurfaceDestroyed(AndroidVideoWindowImpl vw) {

            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mScaleDetector != null) {
                    mScaleDetector.onTouchEvent(event);
                }

                mGestureDetector.onTouchEvent(event);
                if (inCallActivity != null) {
//                    inCallActivity.displayVideoCallControlsIfHidden();
                }
                return true;
            }
        });

        return view;

    }

    private void fixZOrder(SurfaceView mVideoView, SurfaceView mCaptureView) {
        android.util.Log.i("CNN","callvideofragment_fixZOrder");
        mVideoView.setZOrderOnTop(false);
        mCaptureView.setZOrderOnTop(true);
        mCaptureView.setZOrderMediaOverlay(true); // Needed to be able to display control layout over
    }
    public void switchCamera() {
        try {
            android.util.Log.i("CNN","callvideofragment_switchCamera");
            int videoDeviceId = LinphoneManager.getLc().getVideoDevice();
            videoDeviceId = (videoDeviceId + 1) % AndroidCameraConfiguration.retrieveCameras().length;
            LinphoneManager.getLc().setVideoDevice(videoDeviceId);
            CallManager.getInstance().updateCall();

            // previous call will cause graph reconstruction -> regive preview
            // window
            if (mCaptureView != null) {
                LinphoneManager.getLc().setPreviewWindow(mCaptureView);
            }
        } catch (ArithmeticException ae) {
            Log.e("Cannot swtich camera : no camera");
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        android.util.Log.i("CNN","callvideofragment_onresume");
        if (androidVideoWindowImpl != null) {
            synchronized (androidVideoWindowImpl) {
                LinphoneManager.getLc().setVideoWindow(androidVideoWindowImpl);
            }
        }

        mGestureDetector = new GestureDetector(inCallActivity, this);

        if (Version.sdkAboveOrEqual(Version.API08_FROYO_22)) {
            CompatibilityScaleGestureDetector csgd = new CompatibilityScaleGestureDetector(getActivity());
            csgd.setOnScaleListener(this);
            mScaleDetector=csgd;
        }

    }

    @Override
    public void onPause() {
        android.util.Log.i("CNN","callvideofragment_onpause");
        if (androidVideoWindowImpl != null) {
            synchronized (androidVideoWindowImpl) {
				/*
				 * this call will destroy native opengl renderer which is used by
				 * androidVideoWindowImpl
				 */
                LinphoneManager.getLc().setVideoWindow(null);
            }
        }

        super.onPause();
    }
    @Override
    public void onDestroy() {
        android.util.Log.i("CNN","callvideofragment_ondetroy");
        inCallActivity = null;

        mCaptureView = null;
        if (mVideoView != null) {
            mVideoView.setOnTouchListener(null);
            mVideoView = null;
        }
        if (androidVideoWindowImpl != null) {
            // Prevent linphone from crashing if correspondent hang up while you are rotating
            androidVideoWindowImpl.release();
            androidVideoWindowImpl = null;
        }
        if (mGestureDetector != null) {
            mGestureDetector.setOnDoubleTapListener(null);
            mGestureDetector = null;
        }
        if (mScaleDetector != null) {
            mScaleDetector.destroy();
            mScaleDetector = null;
        }

        super.onDestroy();
    }
    public boolean onScale(CompatibilityScaleGestureDetector detector) {
        android.util.Log.i("CNN","callvideofragment_onscale");
        mZoomFactor *= detector.getScaleFactor();
        // Don't let the object get too small or too large.
        // Zoom to make the video fill the screen vertically
        float portraitZoomFactor = ((float) mVideoView.getHeight()) / (float) ((3 * mVideoView.getWidth()) / 4);
        // Zoom to make the video fill the screen horizontally
        float landscapeZoomFactor = ((float) mVideoView.getWidth()) / (float) ((3 * mVideoView.getHeight()) / 4);
        mZoomFactor = Math.max(0.1f, Math.min(mZoomFactor, Math.max(portraitZoomFactor, landscapeZoomFactor)));

        LinphoneCall currentCall = LinphoneManager.getLc().getCurrentCall();
        if (currentCall != null) {
            currentCall.zoomVideo(mZoomFactor, mZoomCenterX, mZoomCenterY);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        android.util.Log.i("CNN","callvideofragment_onscroll");
        if (ChatUtils.isCallEstablished(LinphoneManager.getLc().getCurrentCall())) {
            if (mZoomFactor > 1) {
                // Video is zoomed, slide is used to change center of zoom
                if (distanceX > 0 && mZoomCenterX < 1) {
                    mZoomCenterX += 0.01;
                } else if(distanceX < 0 && mZoomCenterX > 0) {
                    mZoomCenterX -= 0.01;
                }
                if (distanceY < 0 && mZoomCenterY < 1) {
                    mZoomCenterY += 0.01;
                } else if(distanceY > 0 && mZoomCenterY > 0) {
                    mZoomCenterY -= 0.01;
                }

                if (mZoomCenterX > 1)
                    mZoomCenterX = 1;
                if (mZoomCenterX < 0)
                    mZoomCenterX = 0;
                if (mZoomCenterY > 1)
                    mZoomCenterY = 1;
                if (mZoomCenterY < 0)
                    mZoomCenterY = 0;

                LinphoneManager.getLc().getCurrentCall().zoomVideo(mZoomFactor, mZoomCenterX, mZoomCenterY);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        android.util.Log.i("CNN","callvideofragment_ondoubletap");
        if (ChatUtils.isCallEstablished(LinphoneManager.getLc().getCurrentCall())) {
            if (mZoomFactor == 1.f) {
                // Zoom to make the video fill the screen vertically
                float portraitZoomFactor = ((float) mVideoView.getHeight()) / (float) ((3 * mVideoView.getWidth()) / 4);
                // Zoom to make the video fill the screen horizontally
                float landscapeZoomFactor = ((float) mVideoView.getWidth()) / (float) ((3 * mVideoView.getHeight()) / 4);

                mZoomFactor = Math.max(portraitZoomFactor, landscapeZoomFactor);
            }
            else {
                resetZoom();
            }

            LinphoneManager.getLc().getCurrentCall().zoomVideo(mZoomFactor, mZoomCenterX, mZoomCenterY);
            return true;
        }

        return false;
    }

    private void resetZoom() {
        android.util.Log.i("CNN","callvideofragment_resetzomm");
        mZoomFactor = 1.f;
        mZoomCenterX = mZoomCenterY = 0.5f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        android.util.Log.i("CNN","callvideofragment_onattach");
        inCallActivity = (CallActivity) activity;
        if (inCallActivity != null) {
            inCallActivity.bindVideoFragment(this);
        }
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return false;
    }


    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }


    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }


}
