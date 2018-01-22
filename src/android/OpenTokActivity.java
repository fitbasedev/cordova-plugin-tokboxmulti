package com.fitbase.TokBox;
 

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;



import com.fitbasetrainer.MainActivity;
import com.fitbasetrainer.R;


 


public class OpenTokActivity extends AppCompatActivity
                          implements EasyPermissions.PermissionCallbacks,
                                     Publisher.PublisherListener,
                                     Session.SessionListener,  Session.ReconnectionListener ,Subscriber.VideoListener {

    private static final String TAG = "simple-multiparty " + MainActivity.class.getSimpleName();

    private final int MAX_NUM_SUBSCRIBERS = 4;

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private Publisher mPublisher;

    private ArrayList<Subscriber> mSubscribers = new ArrayList<Subscriber>();
    private HashMap<Stream, Subscriber> mSubscriberStreams = new HashMap<Stream, Subscriber>();

    private RelativeLayout mPublisherViewContainer;
    long time;
    //------------------------------------
    private View mScreen1,mScreen3,mScreen4;
    RelativeLayout subscriberViewContainer;
    private RelativeLayout screen1sub0;
    private ToggleButton toggleAudioScreen1Subscriber0;
    private RelativeLayout screen1sub1,screen3sub0,screen3sub1,screen3sub2,screen4sub0,screen4sub1,screen4sub2,screen4sub3;
    private ToggleButton toggleAudioScreen1Subscriber1;
    private ImageView mLocalAudioOnlyImage,avatar;
    //---------------------screen2------------------------
    private String tokBoxData, apiKey, token, sessionId, publisherId, duration, startdate;
    private Handler hidehandler;
    private static final String FORMAT_2 = "%02d";
    ImageButton btnPausevideo, btnPauseaudio, btn_exit;
    LinearLayout llcontrols;
    private TextView tvtimer,init_info,  mAlert;
    private ProgressDialog mProgressDialog,mSessionReconnectDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisher_container);
        mPublisherViewContainer.setVisibility(View.INVISIBLE);
        subscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview0);

        btnPausevideo = (ImageButton) findViewById(R.id.btn_pausevideo);
        btnPauseaudio = (ImageButton) findViewById(R.id.btn_pauseaudio);
        btn_exit = (ImageButton) findViewById(R.id.btn_exit);
        llcontrols = (LinearLayout) findViewById(R.id.llcontrols);
        tvtimer = (TextView) findViewById(R.id.tvtimer);
        init_info=(TextView)findViewById(R.id.init_info);
        mPublisherViewContainer.setOnTouchListener(new OnDragTouchListener(mPublisherViewContainer));
        mAlert = (TextView) findViewById(R.id.quality_warning);


        //----screen2 ---------------------------
        mScreen1 = findViewById(R.id.screen1);
        screen1sub0 =(RelativeLayout)findViewById(R.id.screen1sub0);
      //  toggleAudioScreen1Subscriber0=(ToggleButton)findViewById(R.id.toggleAudioScreen1Subscriber0);
        screen1sub1=(RelativeLayout)findViewById(R.id.screen1sub1);
      //  toggleAudioScreen1Subscriber1=(ToggleButton)findViewById(R.id.toggleAudioScreen1Subscriber1);
        //---------------------------------------------
        //----------------screen3------------------------
        mScreen3 = findViewById(R.id.screen3);
        screen3sub0=(RelativeLayout)findViewById(R.id.screen3sub0);
        screen3sub1=(RelativeLayout)findViewById(R.id.screen3sub1);
        screen3sub2=(RelativeLayout)findViewById(R.id.screen3sub2);
        //-----------------------------------------------------------
        //---------------screen4----------------------------
        mScreen4 = findViewById(R.id.screen4);
        screen4sub0=(RelativeLayout)findViewById(R.id.screen4sub0);
        screen4sub1=(RelativeLayout)findViewById(R.id.screen4sub1);
        screen4sub2=(RelativeLayout)findViewById(R.id.screen4sub2);
        screen4sub3=(RelativeLayout)findViewById(R.id.screen4sub3);
        //---------------------------------------------
        hidehandler = new Handler();
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
        btnPausevideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPublisher.getPublishVideo()) {
                    mPublisher.setPublishVideo(false);
                     onDisableLocalVideo(false);
                    btnPausevideo.setImageResource(R.drawable.no_video_icon);
                } else {
                    mPublisher.setPublishVideo(true);
                     onDisableLocalVideo(true);
                    btnPausevideo.setImageResource( R.drawable.video_icon);
                }
            }
        });
        btnPauseaudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPublisher.getPublishAudio()) {
                    mPublisher.setPublishAudio(false);

                    btnPauseaudio.setImageResource(R.drawable.muted_mic_icon);
                } else {
                    mPublisher.setPublishAudio(true);

                    btnPauseaudio.setImageResource(R.drawable.mic_icon);
                }

            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                onBackPressed();
            }
        });

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // missing line
            Date date = sdf.parse(startdate.split("\\.")[0]);
            SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.ENGLISH);
            writeDate.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
            String s = writeDate.format(date);
            Date date1 = writeDate.parse(s);
            Calendar c = Calendar.getInstance();
            long millis=(date1.getTime()+(Long.parseLong(duration)*60*1000))- c.getTimeInMillis();

            new CountDownTimer(millis, 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {
                    time=millisUntilFinished;
                    tvtimer.setText("" + TimeUnit.MILLISECONDS.toHours(millisUntilFinished) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }

                public void onFinish() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSession != null) {
                                mSession.onPause();
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
    public void onDisableLocalVideo(boolean video) {
        if (!video) {

            mLocalAudioOnlyImage = new ImageView(this);
            mLocalAudioOnlyImage.setImageResource(R.mipmap.avatar);
            mLocalAudioOnlyImage.setBackgroundResource(R.drawable.bckg_audio_only);
            mPublisherViewContainer.addView(mLocalAudioOnlyImage);
        } else {
            mPublisherViewContainer.removeView(mLocalAudioOnlyImage);
        }


    }
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");

        super.onStart();
    }
    public void swapCamera(View view) {

        mPublisher.cycleCamera();

    }
    private Runnable hideControllerThread = new Runnable() {

        public void run() {
//      volumeBar.setVisibility(View.GONE);
//      audioControllView.setVisibility(View.GONE);
//      topBar.setVisibility(View.GONE);
            llcontrols.setVisibility(View.GONE);
//      RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//      params.setMargins(10, 0, 0, 10);
//      mSubscriberViewContainer.setLayoutParams(params);
//      btnPauseaudio.setVisibility(View.GONE);
        }
    };


    public void hideControllers() {
        
        hidehandler.postDelayed(hideControllerThread, 10000);
    }

    public void showControllers() {
//    volumeBar.setVisibility(View.VISIBLE);
//    topBar.setVisibility(View.VISIBLE);
//    audioControllView.setVisibility(View.VISIBLE);
        llcontrols.setVisibility(View.VISIBLE);
//    RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//    params.setMargins(10, 0, 0, 60);
//    mSubscriberViewContainer.setLayoutParams(params);
//    btnPauseaudio.setVisibility(View.VISIBLE);
        hidehandler.removeCallbacks(hideControllerThread);
        hideControllers();
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


    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");

        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        if (mSession == null) {
            return;
        }
        mSession.onResume();
        hideControllers();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        if (mSession == null) {
            return;
        }
        mSession.onPause();

        if (isFinishing()) {
            disconnectSession();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onPause");

        super.onStop();
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

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = {
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            mSession = new Session.Builder(OpenTokActivity.this, apiKey, sessionId).build();
            mSession.setSessionListener(this);
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setTitle("Please wait");
            mProgressDialog.setMessage("Connecting...");
            mProgressDialog.show();
            mSession.connect(token);
        } else {
            EasyPermissions.requestPermissions(OpenTokActivity.this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }
    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());
        mProgressDialog.dismiss();
        mPublisher = new Publisher.Builder(OpenTokActivity.this).name("publisher").build();
        //mPublisher.setRenderer(new BasicCustomVideoRenderer(this));

        mPublisher.setPublisherListener(this);
        mSession.setReconnectionListener(this);
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

        mPublisherViewContainer.addView(mPublisher.getView());

        mPublisherViewContainer.setVisibility(View.VISIBLE);
        init_info.setBackgroundResource(R.color.quality_warning);
        init_info.setTextColor(this.getResources().getColor(R.color.white));
        init_info.bringToFront();
        init_info.setVisibility(View.VISIBLE);
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: disconnected from session " + session.getSessionId());

        mSession = null;
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());
        mProgressDialog.dismiss();
        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());
        init_info.setVisibility(View.INVISIBLE);
        if (mSubscribers.size() + 1 > MAX_NUM_SUBSCRIBERS) {
            Toast.makeText(this, "New subscriber ignored. MAX_NUM_SUBSCRIBERS limit reached.", Toast.LENGTH_LONG).show();
            return;
        }

        final Subscriber subscriber = new Subscriber.Builder(OpenTokActivity.this, stream).build();
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        subscriber.setVideoListener(this);
        mSession.subscribe(subscriber);
        mSubscribers.add(subscriber);
        mSubscriberStreams.put(stream, subscriber);


        updateView();
       /* int position = mSubscribers.size()-1  ;  ;
        int id = getResources().getIdentifier("subscriberview" + (new Integer(position)).toString(), "id", MainActivity.this.getPackageName());
        RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);


        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        subscriberViewContainer.addView(subscriber.getView());

        id = getResources().getIdentifier("toggleAudioSubscriber" + (new Integer(position)).toString(), "id", MainActivity.this.getPackageName());
        final ToggleButton toggleAudio = (ToggleButton) findViewById(id);
        toggleAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    subscriber.setSubscribeToAudio(true);
                } else {
                    subscriber.setSubscribeToAudio(false);
                }
            }
        });
        toggleAudio.setVisibility(View.VISIBLE);
        subscriberViewContainer.setVisibility(View.VISIBLE);*/
       calculateLayout();
//updateLayout(subscriberViewContainer);
    }

    private void updateView() {
        if(mSubscribers.size()==2){
            subscriberViewContainer.removeView(mSubscribers.get(0).getView());
        }else if(mSubscribers.size()==3){
            subscriberViewContainer.removeView(mSubscribers.get(0).getView());
            for (int i=0;i<mSubscribers.size()-1;i++){
                int id = getResources().getIdentifier("screen1sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout ViewContainer = (RelativeLayout) findViewById(id);
                ViewContainer.removeView(mSubscribers.get(i).getView());
            }
        }else if(mSubscribers.size()==4){
            subscriberViewContainer.removeView(mSubscribers.get(0).getView());
            for (int i=0;i<mSubscribers.size()-1;i++){
                int id = getResources().getIdentifier("screen3sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout ViewContainer = (RelativeLayout) findViewById(id);
                ViewContainer.removeView(mSubscribers.get(i).getView());
            }
        }
    }

    private void calculateLayout() {
        DisplayMetrics maMetrics  =getDisplay();
    if(mSubscribers.size()==1){
        mPublisherViewContainer.setVisibility(View.VISIBLE);
        subscriberViewContainer.addView(mSubscribers.get(0).getView());
        subscriberViewContainer.getLayoutParams().height=maMetrics.heightPixels;
        subscriberViewContainer.getLayoutParams().width=maMetrics.widthPixels;
        subscriberViewContainer.requestLayout();

    }else if(mSubscribers.size()==2){

        mScreen1.setVisibility(View.VISIBLE);
        screen1sub0.getLayoutParams().height=maMetrics.heightPixels/2;
        screen1sub1.getLayoutParams().height=maMetrics.heightPixels/2;
       for (int i=0;i<mSubscribers.size();i++){
           int id = getResources().getIdentifier("screen1sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
           RelativeLayout ViewContainer = (RelativeLayout) findViewById(id);
           ViewContainer.addView(mSubscribers.get(i).getView());
       }

    }else if(mSubscribers.size()==3){
        mScreen3.setVisibility(View.VISIBLE);
        screen3sub0.getLayoutParams().height=maMetrics.heightPixels/2;
        screen3sub1.getLayoutParams().width=maMetrics.widthPixels/2;
        screen3sub2.getLayoutParams().width=maMetrics.widthPixels/2;

        for (int i=0;i<mSubscribers.size();i++){
            int id = getResources().getIdentifier("screen3sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
            RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
            subscriberViewContainer.addView(mSubscribers.get(i).getView());
        }
    }else if(mSubscribers.size()==4){
        mScreen4.setVisibility(View.VISIBLE);
        screen4sub0.getLayoutParams().width=maMetrics.widthPixels/2;
        screen4sub0.getLayoutParams().height=maMetrics.heightPixels/2;
        screen4sub1.getLayoutParams().width=maMetrics.widthPixels/2;
        screen4sub1.getLayoutParams().height=maMetrics.heightPixels/2;
        screen4sub2.getLayoutParams().height=maMetrics.heightPixels/2;
        screen4sub2.getLayoutParams().width=maMetrics.widthPixels/2;
        screen4sub3.getLayoutParams().height=maMetrics.heightPixels/2;
        screen4sub3.getLayoutParams().width=maMetrics.widthPixels/2;
        for (int i=0;i<mSubscribers.size();i++){
            int id = getResources().getIdentifier("screen4sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
            RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
            subscriberViewContainer.addView(mSubscribers.get(i).getView());
        }

    }
    }

    /*private void updateLayout(RelativeLayout subscriberViewContainer) {
        DisplayMetrics maMetrics  =getDisplay();
    if(mSubscribers.size()==2){
        RelativeLayout aboveViewContainer = (RelativeLayout) findViewById(R.id.subscriberview0);
        aboveViewContainer.getLayoutParams().height= (int) (maMetrics.heightPixels/2);
        aboveViewContainer.requestLayout();
    }else if(mSubscribers.size()==3){
        RelativeLayout aboveViewContainer = (RelativeLayout) findViewById(R.id.subscriberview1);
        subscriberViewContainer.getLayoutParams().height=maMetrics.heightPixels/2;
        subscriberViewContainer.getLayoutParams().width=aboveViewContainer.getWidth()/2;
        aboveViewContainer.getLayoutParams().width=maMetrics.widthPixels/2;
    }else if(mSubscribers.size()==4){
        RelativeLayout aboveViewContainer = (RelativeLayout) findViewById(R.id.subscriberview0);
        aboveViewContainer.getLayoutParams().width= (int) (maMetrics.widthPixels/2);
        aboveViewContainer.requestLayout();
        subscriberViewContainer.getLayoutParams().height=maMetrics.heightPixels/2;
        subscriberViewContainer.getLayoutParams().width=aboveViewContainer.getWidth();
        subscriberViewContainer.requestLayout();
    }
    }*/


    public DisplayMetrics getDisplay(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());
        clearView();
        Subscriber subscriber = mSubscriberStreams.get(stream);
        if (subscriber == null) {
            return;
        }

       /* int position = mSubscribers.indexOf(subscriber);
        int id = getResources().getIdentifier("subscriberview" + (new Integer(position)).toString(), "id", MainActivity.this.getPackageName());
*/
        mSubscribers.remove(subscriber);
        mSubscriberStreams.remove(stream);

        //RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);

       // subscriberViewContainer.removeView(subscriber.getView());


      /*  id = getResources().getIdentifier("toggleAudioSubscriber" + (new Integer(position)).toString(), "id", MainActivity.this.getPackageName());
        final ToggleButton toggleAudio = (ToggleButton) findViewById(id);
        toggleAudio.setOnCheckedChangeListener(null);
        toggleAudio.setVisibility(View.INVISIBLE);*/

          if(mSubscribers.size()==0) {
              init_info.setBackgroundResource(R.color.quality_warning);
              init_info.setTextColor(this.getResources().getColor(R.color.white));
              init_info.bringToFront();
              init_info.setVisibility(View.VISIBLE);
          }

        calculateLayout();
    }

    private void clearView() {
        if(mSubscribers.size()==1){
            subscriberViewContainer.removeView(mSubscribers.get(0).getView());
        }else if(mSubscribers.size()==2){
            for (int i=0;i<mSubscribers.size();i++){
                int id = getResources().getIdentifier("screen1sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                subscriberViewContainer.removeView(mSubscribers.get(i).getView());

            }
            mScreen1.setVisibility(View.GONE);
        }else if(mSubscribers.size()==3){
            for (int i=0;i<mSubscribers.size();i++){
                int id = getResources().getIdentifier("screen3sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                subscriberViewContainer.removeView(mSubscribers.get(i).getView());

            }
            mScreen3.setVisibility(View.GONE);
        }else if(mSubscribers.size()==4){
            for (int i=0;i<mSubscribers.size();i++){
                int id = getResources().getIdentifier("screen4sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                subscriberViewContainer.removeView(mSubscribers.get(i).getView());

            }
            mScreen4.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");

        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void disconnectSession() {
        if (mSession == null) {
            return;
        }

        if (mSubscribers.size() > 0) {
            for (Subscriber subscriber : mSubscribers) {
                if (subscriber != null) {
                    mSession.unsubscribe(subscriber);
                    subscriber.destroy();
                }
            }
        }

        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
    }

    @Override
    public void onReconnecting(Session session) {
        showReconnectionDialog(true);
    }

    @Override
    public void onReconnected(Session session) {
        showReconnectionDialog(false);
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String reason) {
        if (reason.equals("quality")) {

            showNetworkWarning();
        }
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }
    private void showReconnectionDialog(boolean show) {
        if (show) {
            mSessionReconnectDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mSessionReconnectDialog.setMessage("Reconnecting. Please wait...");
            mSessionReconnectDialog.setIndeterminate(true);
            mSessionReconnectDialog.setCanceledOnTouchOutside(false);
            mSessionReconnectDialog.show();
        }
        else {
            mSessionReconnectDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Session has been reconnected")
                    .setPositiveButton(android.R.string.ok, null);
            builder.create();
            builder.show();
        }
    }
    public void showNetworkWarning(){
        mAlert.setBackgroundResource(R.color.quality_warning);
        mAlert.setTextColor(this.getResources().getColor(R.color.white));
        mAlert.bringToFront();
        mAlert.setVisibility(View.VISIBLE);
        mAlert.postDelayed(new Runnable() {
            public void run() {
                mAlert.setVisibility(View.GONE);
            }
        }, 7000);
    }
   
}