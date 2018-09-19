package com.fitbase.TokBox;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ionicframework.trainermobile381261.R;

/**
 * Created by Priya on 9/10/2018.
 */

public class LiveVideoActionBarFragment extends Fragment {

  private static final String LOGTAG = LiveVideoActivity.class.getName();

  private LiveVideoActivity mActivity;
  private View rootView;

  private ImageButton mAudioBtn;
  private ImageButton mVideoBtn;
  private ImageButton mBtn_exit;
  private ImageButton mAnnotationsBtn;
  private ImageButton mTextChatBtn;
  private ImageButton mUnreadMessages;

  VectorDrawableCompat drawableVideoCallBtn;
  VectorDrawableCompat drawableEndCall;
  VectorDrawableCompat drawableMicBckBtn;

  private PreviewControlCallbacks mControlCallbacks = previewCallbacks;

  public interface PreviewControlCallbacks {

    public void onDisableLocalAudio(boolean audio);

    public void onDisableLocalVideo(boolean video);

    public void  leaveRoom();
  }

  private static PreviewControlCallbacks previewCallbacks = new PreviewControlCallbacks() {
    @Override
    public void onDisableLocalAudio(boolean audio) { }

    @Override
    public void onDisableLocalVideo(boolean video) { }




    @Override
    public void leaveRoom() {

    }


  };

  private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.localAudio:
          updateLocalAudio();
          break;

        case R.id.localVideo:
          updateLocalVideo();
          break;

        case R.id.btn_exit:
          leaveRoom();
          break;


      }
    }
  };

  private void leaveRoom() {
    mControlCallbacks.leaveRoom();
  }



  @Override
  public void onAttach(Context context) {
    Log.i(LOGTAG, "OnAttach ActionBarFragment");

    super.onAttach(context);

    this.mActivity = (LiveVideoActivity) context;
    this.mControlCallbacks = (PreviewControlCallbacks) context;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

      this.mActivity = (LiveVideoActivity) activity;
      this.mControlCallbacks = (PreviewControlCallbacks) activity;
    }
  }

  @Override
  public void onDetach() {
    Log.i(LOGTAG, "onDetach ActionBarFragment");

    super.onDetach();

    mControlCallbacks = previewCallbacks;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.i(LOGTAG, "OnCreate ActionBarFragment");

    rootView = inflater.inflate(R.layout.live_video_actionbar_fragment, container, false);
    mAudioBtn = (ImageButton) rootView.findViewById(R.id.localAudio);
    mVideoBtn = (ImageButton) rootView.findViewById(R.id.localVideo);

    mBtn_exit=(ImageButton)rootView.findViewById(R.id.btn_exit) ;


    drawableEndCall = VectorDrawableCompat.create(getResources(), R.drawable.end_call_button_backg, null);
    drawableMicBckBtn = VectorDrawableCompat.create(getResources(), R.drawable.audio_icon_backg, null);
    drawableVideoCallBtn= VectorDrawableCompat.create(getResources(), R.drawable.video_icon_backg, null);

    mAudioBtn.setImageResource(
      R.drawable.mic_icon
    );
    mAudioBtn.setBackground(drawableMicBckBtn);

    mVideoBtn.setImageResource( R.drawable.video_icon
    );
    mVideoBtn.setBackground(drawableVideoCallBtn);
    mBtn_exit.setImageResource(R.drawable.hang_up);
    mBtn_exit.setBackground(drawableEndCall);

    setEnabled(true);

    return rootView;
  }
  public void enableAnnotations(boolean enable){
    if (mAnnotationsBtn != null ) {
      mAnnotationsBtn.setOnClickListener(enable
        ? mBtnClickListener
        : null);
    }
  }
  public void updateLocalAudio() {
    if (!mActivity.getmPublisher().getPublishAudio()) {
      mControlCallbacks.onDisableLocalAudio(true);
      mAudioBtn.setImageResource(R.drawable.mic_icon);
    } else {
      mControlCallbacks.onDisableLocalAudio(false);
      mAudioBtn.setImageResource(R.drawable.mic_muted);
    }
  }

  public void updateLocalVideo() {
    if (!mActivity.getmPublisher().getPublishVideo()){
      mControlCallbacks.onDisableLocalVideo(true);
      mVideoBtn.setImageResource(R.drawable.video_icon);
    } else {
      mControlCallbacks.onDisableLocalVideo(false);
      mVideoBtn.setImageResource(R.drawable.no_video_icon);
    }
  }







  public void setEnabled(boolean enabled) {
    if (mVideoBtn != null && mAudioBtn != null) {
      if (enabled) {
        mAudioBtn.setOnClickListener(mBtnClickListener);
        mVideoBtn.setOnClickListener(mBtnClickListener);
        mBtn_exit.setOnClickListener(mBtnClickListener);
      } else {
        mAudioBtn.setOnClickListener(null);
        mVideoBtn.setOnClickListener(null);
        mAudioBtn.setImageResource(R.drawable.mic_icon);
        mVideoBtn.setImageResource(R.drawable.video_icon);

      }
    }
  }







}
