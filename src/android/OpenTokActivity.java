package com.fitbase.TokBox;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.content.res.Configuration;
import android.os.Bundle;

import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import android.widget.RelativeLayout;
import android.widget.TextView;



import com.fitbasetrainer.MainActivity;
import com.fitbasetrainer.R;

import com.opentok.android.Publisher;



import org.json.JSONObject;



import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class OpenTokActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,  View.OnTouchListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final int RC_SETTINGS_SCREEN_PERM = 123;
  private static final int RC_VIDEO_APP_PERM = 124;


  long time;

  int screenWidth,screenHeight;
  private String tokBoxData, apiKey, token, sessionId, publisherId, duration, startdate;

  private   ProgressDialog dialog;
  private Handler hidehandler;

  ImageButton btnPausevideo, btnPauseaudio, btn_exit;
  LinearLayout llcontrols;
  private TextView tvtimer,init_info,mAlert;

  private ViewGroup mPreview;
  private ViewGroup mLastParticipantView;
  private LinearLayout mParticipantsView;
  private ProgressBar mLoadingSub;
  private static final String FORMAT_2 = "%02d";
  private Room    mRoom;
  private RelativeLayout scrollView;
  private ProgressDialog mProgressDialog;
  private Publisher.CameraCaptureResolution mCapturerRes = Publisher.CameraCaptureResolution.MEDIUM;
  private Publisher.CameraCaptureFrameRate  mCapturerFps = Publisher.CameraCaptureFrameRate.FPS_30;
  private boolean FLAG_KEEP_SCREEN_ON=true;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    btnPausevideo = (ImageButton) findViewById(R.id.btn_pausevideo);

    btnPauseaudio = (ImageButton) findViewById(R.id.btn_pauseaudio);
    btn_exit = (ImageButton) findViewById(R.id.btn_exit);
    llcontrols = (LinearLayout) findViewById(R.id.llcontrols);
    tvtimer = (TextView) findViewById(R.id.tvtimer);
    mPreview = (ViewGroup) findViewById(R.id.publisherview);
    mParticipantsView = (LinearLayout) findViewById(R.id.gallery);
    mLastParticipantView = (ViewGroup) findViewById(R.id.mainsubscriberView);
    mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);
    init_info=(TextView)findViewById(R.id.init_info);
    mAlert = (TextView) findViewById(R.id.quality_warning);
    scrollView=(RelativeLayout)findViewById(R.id.scrollView);

    hidehandler = new Handler();
    mPreview.setVisibility(View.GONE);
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    screenWidth = metrics.widthPixels;
    screenHeight = metrics.heightPixels;
    mLastParticipantView.getLayoutParams().width=screenWidth;
    mLastParticipantView.getLayoutParams().height=screenHeight;
    //mPreview.setOnTouchListener(OpenTokActivity.this);
    mPreview.setOnTouchListener(new OnDragTouchListener(mPreview));
    tokBoxData = getIntent().getStringExtra("tokbox_obj");
    try {
      JSONObject jobj = new JSONObject(tokBoxData);
      apiKey = jobj.getString("apiKey");
      token = jobj.getString("tokenId");
      sessionId = jobj.getString("liveSessionId");
      publisherId = jobj.getString("trainerUserid");
      duration = jobj.getString("duration");
      startdate = jobj.getString("startDate");
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // missing line
      Date date = sdf.parse(startdate.split("\\.")[0]);
      SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
      writeDate.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
      String s = writeDate.format(date);
      Date date1 = writeDate.parse(s);
      Calendar c = Calendar.getInstance();
      long millis = (date1.getTime() + (Long.parseLong(duration) * 60 * 1000)) - c.getTimeInMillis();

      new CountDownTimer(millis, 1000) { // adjust the milli seconds here

        public void onTick(long millisUntilFinished) {
          time = millisUntilFinished;
          tvtimer.setText("" + TimeUnit.MILLISECONDS.toHours(millisUntilFinished) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
        }

        public void onFinish() {

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (mRoom != null) {
                mRoom.onPause();
                if (isFinishing()) {
                 disconnectSession();
                }
              }
              if (hidehandler != null && hideControllerThread != null)
                hidehandler.removeCallbacks(hideControllerThread);
              finish();
            }
          });

        }

      }.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    requestPermissions();
  }

  private Runnable hideControllerThread = new Runnable() {

    public void run() {

      llcontrols.setVisibility(View.GONE);
      Configuration newConfig=getResources().getConfiguration();
      if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
        DisplayMetrics metrics=getDisplay();
        mLastParticipantView.getLayoutParams().height = (int) (metrics.heightPixels / 1.25);
        mLastParticipantView.requestLayout();;
      }else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
        DisplayMetrics metrics=getDisplay();
        mLastParticipantView.getLayoutParams().height = (int) (metrics.heightPixels / 1.5);
        mLastParticipantView.requestLayout();;
      }

    }
  };


  public void hideControllers() {
    hidehandler.postDelayed(hideControllerThread, 10000);
  }

  public void showControllers() {
    Configuration newConfig=getResources().getConfiguration();
    if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
      DisplayMetrics metrics=getDisplay();
      mLastParticipantView.getLayoutParams().height = (int) (metrics.heightPixels / 1.35);
      mLastParticipantView.requestLayout();;
    }else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
      DisplayMetrics metrics=getDisplay();
      mLastParticipantView.getLayoutParams().height = (int) (metrics.heightPixels / 1.6);
      mLastParticipantView.requestLayout();;
    }
    llcontrols.setVisibility(View.VISIBLE);
    hidehandler.removeCallbacks(hideControllerThread);
    hideControllers();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    screenWidth = metrics.widthPixels;
    screenHeight = metrics.heightPixels;
    if(mRoom.getParticipants().size()>1) {
      if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        mLastParticipantView.getLayoutParams().width = screenWidth;
        mLastParticipantView.getLayoutParams().height = (int) (screenHeight / 1.5);
      } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        mLastParticipantView.getLayoutParams().width = screenWidth;
        mLastParticipantView.getLayoutParams().height = (int) (screenHeight / 1.35);
      }
    }else{
      mLastParticipantView.getLayoutParams().width = screenWidth;
      mLastParticipantView.getLayoutParams().height = screenHeight;
    }
  }
public DisplayMetrics getDisplay(){
  DisplayMetrics metrics = new DisplayMetrics();
  getWindowManager().getDefaultDisplay().getMetrics(metrics);
  return metrics;
}
  @Override
  public void onUserInteraction() {
    super.onUserInteraction();
    if (llcontrols.getVisibility() == View.VISIBLE) {
      hidehandler.removeCallbacks(hideControllerThread);
      hideControllers();
    } else {
      showControllers();
    }
  }

  @AfterPermissionGranted(RC_VIDEO_APP_PERM)
  private void requestPermissions() {
    String[] perms = {
      Manifest.permission.INTERNET,
      Manifest.permission.CAMERA,
      Manifest.permission.RECORD_AUDIO
    };
    if (EasyPermissions.hasPermissions(this, perms)) {
      mProgressDialog = new ProgressDialog(this);
      mProgressDialog.setCanceledOnTouchOutside(false);
      mProgressDialog.setTitle("Please wait");
      mProgressDialog.setMessage("Connecting...");
      mProgressDialog.show();
      init_info.setBackgroundResource(R.color.quality_warning);
      init_info.setTextColor(OpenTokActivity.this.getResources().getColor(R.color.white));
      init_info.bringToFront();
      init_info.setVisibility(View.VISIBLE);
      mRoom = new Room(this, sessionId, token, apiKey);
      mRoom.setPreviewView(mPreview);
      mRoom.setParticipantsViewContainer(mParticipantsView, mLastParticipantView, null);
      mRoom.setPublisherSettings(mCapturerRes, mCapturerFps);
      mRoom.connect();


//      mContainer.addView(mPublisher.getView());

    } else {
      EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
    }
  }


  @Override
  protected void onResume() {
    Log.d(TAG, "onResume");

    super.onResume();
    if (mRoom != null) {
      mRoom.onResume();
    }


  }

  @Override
  protected void onPause() {
    Log.d(TAG, "onPause");

    super.onPause();


    if (isFinishing()) {
      if (mRoom != null) {
        mRoom.onPause();
      }

    }
  }
  private void disconnectSession() {
    if (mRoom == null) {
      return;
    }





    mRoom.disconnect();
  }
  @Override
  protected void onDestroy() {
    Log.d(TAG, "onDestroy");

    disconnectSession();
    super.onDestroy();

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override
  public void onPermissionsGranted(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
  }

  @Override
  public void onPermissionsDenied(int requestCode, List<String> perms) {
    Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      new AppSettingsDialog.Builder(this)
        .setTitle(getString(R.string.title_settings_dialog))
        .setRationale(getString(R.string.rationale_ask_again))
        .setPositiveButton(getString(R.string.setting))
        .setNegativeButton(getString(R.string.cancel))
        .setRequestCode(RC_SETTINGS_SCREEN_PERM)
        .build()
        .show();
    }
  }
  public void updateLoadingSub() {
    mRoom.loadSubscriberView();
  }

  //Show audio only icon when video quality changed and it is disabled for the last subscriber
  public void setAudioOnlyViewLastParticipant(boolean audioOnlyEnabled, Participant participant, View.OnClickListener clickLastParticipantListener) {
    if (audioOnlyEnabled) {
      this.mRoom.getLastParticipantView().removeView(participant.getView());
      View audioOnlyView = getAudioOnlyIcon();
      this.mRoom.getLastParticipantView().addView(audioOnlyView);
      audioOnlyView.setOnClickListener(clickLastParticipantListener);
    } else {
      this.mRoom.getLastParticipantView().removeAllViews();
      this.mRoom.getLastParticipantView().addView(participant.getView());
    }
  }

  public void setAudioOnlyViewListPartcipants (boolean audioOnlyEnabled, Participant participant, int index , View.OnClickListener clickListener) {

    final LinearLayout.LayoutParams lp = getQVGALayoutParams();

    if (audioOnlyEnabled) {

      this.mRoom.getParticipantsViewContainer().removeViewAt(index);
      View audioOnlyView = getAudioOnlyIcon();
      audioOnlyView.setTag(participant.getStream());
      audioOnlyView.setId(index);
      audioOnlyView.setOnClickListener(clickListener);
      this.mRoom.getParticipantsViewContainer().addView(audioOnlyView, index, lp);
    } else {
      this.mRoom.getParticipantsViewContainer().removeViewAt(index);
      this.mRoom.getParticipantsViewContainer().addView(participant.getView(), index, lp);
    }

  }

  public ProgressBar getLoadingSub() {
    return mLoadingSub;
  }
  public void showNetworkWarning(){
    mAlert.setBackgroundResource(R.color.quality_alert);
    mAlert.setTextColor(this.getResources().getColor(R.color.white));
    mAlert.bringToFront();
    mAlert.setVisibility(View.VISIBLE);
    mAlert.postDelayed(new Runnable() {
      public void run() {
        mAlert.setVisibility(View.GONE);
      }
    }, 7000);
  }

  //Convert dp to real pixels, according to the screen density.
  public int dpToPx(int dp) {
    double screenDensity = this.getResources().getDisplayMetrics().density;
    return (int) (screenDensity * (double) dp);
  }

  private ImageView getAudioOnlyIcon() {

    ImageView imageView = new ImageView(this);

  //  imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    imageView.setBackgroundResource(R.drawable.avatar_borders);
    imageView.setImageResource(R.mipmap.avatar);

    return imageView;
  }

  protected LinearLayout.LayoutParams getVGALayoutParams(){
    return new LinearLayout.LayoutParams(640, 480);
  }

  LinearLayout.LayoutParams getQVGALayoutParams(){
    return new LinearLayout.LayoutParams(480, 320);
  }

  LinearLayout.LayoutParams getMainLayoutParams(){
    return new LinearLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    );
  }

  public View.OnLongClickListener onPubStatusClick = new View.OnLongClickListener() {

    @Override
    public boolean onLongClick(View v) {
      return false;
    }

  };


  public View.OnClickListener onPubViewClick = new View.OnClickListener() {
    @Override
    public void onClick(View v) {

      if (mRoom.getPublisher() != null) {
        if (mRoom.getPublisher().getPublishVideo()) {
          mRoom.getPublisher().setPublishVideo(false);
          View audioOnlyView = getAudioOnlyIcon();
          audioOnlyView.setOnClickListener(this);
          audioOnlyView.setOnLongClickListener(onPubStatusClick);
          mPreview.removeAllViews();
          mPreview.addView(audioOnlyView);
        }
        else {
          mRoom.getPublisher().setPublishVideo(true);
          mRoom.getPublisher().getView().setOnClickListener(this);
          mRoom.getPublisher().getView().setOnLongClickListener(onPubStatusClick);
          mPreview.addView(mRoom.getPublisher().getView());
        }
      }
    }

  };


  public void showReconnectingDialog(boolean show){
    if (show) {
      dialog = new ProgressDialog(this);
      dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      dialog.setMessage("Reconnecting. Please wait...");
      dialog.setIndeterminate(true);
      dialog.setCanceledOnTouchOutside(false);
      dialog.show();
    } else {
      dialog.dismiss();
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("Session has been reconnected")
        .setPositiveButton(android.R.string.ok, null);
      builder.create();
      builder.show();
    }
  }

  public void onCameraSwapClick(View view) {
    if (null != mRoom) {
      mRoom.getPublisher().cycleCamera();
    }
  }

  public void onEndCallClick(View view) {
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    finish();
    onBackPressed();
  }

  public void onPublisherMuteClick(View view) {
    if (null != mRoom) {
      if (mRoom.getPublisher().getPublishAudio()) {
        ((ImageButton)findViewById(R.id.btn_pauseaudio)).setImageResource(
            R.drawable.muted_mic_icon
        );
        mRoom.getPublisher().setPublishAudio(false);
      } else {
        ((ImageButton)findViewById(R.id.btn_pauseaudio)).setImageResource(
          R.drawable.mic_icon
        );
        mRoom.getPublisher().setPublishAudio(true);
      }
    }
  }
  public void onPubliherMuteVideo(View view) {
    if (mRoom.getPublisher() != null) {
      if (mRoom.getPublisher().getPublishVideo()) {
        mRoom.getPublisher().setPublishVideo(false);
        btnPausevideo.setImageResource(R.drawable.no_video_icon);
        View audioOnlyView = getAudioOnlyIcon();
     //   audioOnlyView.setOnClickListener(this);
      //  audioOnlyView.setOnLongClickListener(onPubStatusClick);
        mPreview.removeAllViews();
        mPreview.addView(audioOnlyView);
      }
      else {
        mRoom.getPublisher().setPublishVideo(true);
        btnPausevideo.setImageResource( R.drawable.video_icon);
      //  mRoom.getPublisher().getView().setOnClickListener(this);
       // mRoom.getPublisher().getView().setOnLongClickListener(onPubStatusClick);
        mPreview.addView(mRoom.getPublisher().getView());
      }
    }
  }


  public ProgressDialog getmProgressDialog() {
    return mProgressDialog;
  }
   public TextView getInfoMessage(){
     return init_info;
   }


  @Override
  public boolean onTouch(View v, MotionEvent event) {
    return false;
  }



}
