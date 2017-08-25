package com.example.huaanhhong.chattest.Linphone;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.huaanhhong.chattest.R;

public class CallAudioFragment extends Fragment {


    private CallActivity incallActvityInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i("CNN","callaudiofragment_oncreate");
        View view= inflater.inflate(R.layout.fragment_call_audio, container, false);
         return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i("CNN","callaudiofragment_ontach");
        incallActvityInstance = (CallActivity) activity;

        if (incallActvityInstance != null) {
            incallActvityInstance.bindAudioFragment(this);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.i("CNN","callaudiofragment_onstart");
        // Just to be sure we have incall controls
        if (incallActvityInstance != null) {
//            incallActvityInstance.removeCallbacks();
        }
    }
    class SwipeGestureDetector implements View.OnTouchListener


    {
        static final int MIN_DISTANCE = 100;
        private float downX, upX;
        private boolean lock;

        private SwipeListener listener;

		public SwipeGestureDetector(SwipeListener swipeListener) {
        super();
        listener = swipeListener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i("CNN","callaudiofragment_ontouch");
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                lock = false;
                downX = event.getX();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (lock) {
                    return false;
                }
                upX = event.getX();

                float deltaX = downX - upX;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    lock = true;
                    if (deltaX < 0) { listener.onLeftToRightSwipe(); return true; }
                    if (deltaX > 0) { listener.onRightToLeftSwipe(); return true; }
                }
                break;
        }
        return false;
    }
}

interface SwipeListener {
    void onRightToLeftSwipe();
    void onLeftToRightSwipe();
}


}
