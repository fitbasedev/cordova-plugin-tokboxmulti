package com.fitbase.TokBox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
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

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


import com.fitbasetrainer.MainActivity;
import com.fitbasetrainer.R;

/**
 * Created by Anshul Nigam .
 *
 */



public class OpenTokActivity extends AppCompatActivity
                          implements EasyPermissions.PermissionCallbacks,
                                     Publisher.PublisherListener,
                                     Session.SessionListener,  Session.ReconnectionListener ,Subscriber.VideoListener {

     private static final String TAG = "simple-multiparty " + OpenTokActivity.class.getSimpleName();

    private final int MAX_NUM_SUBSCRIBERS = 4;
    private ArrayList<Subscriber> mParticipantsList = new ArrayList<Subscriber>();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private Publisher mPublisher;
    private Menu menu;
    private boolean isRecylerViewPresent =false;
    private ArrayList<Subscriber> mSubscribers = new ArrayList<Subscriber>();

    public HashMap<Stream, Subscriber> getmSubscriberStreams() {
        return mSubscriberStreams;
    }

    private HashMap<Stream, Subscriber> mSubscriberStreams = new HashMap<Stream, Subscriber>();
    private HashMap<Integer, RelativeLayout> controlsLayoutView = new HashMap<Integer, RelativeLayout>();

    public RecyclerView getmParticipantsGrid() {
        return mParticipantsGrid;
    }

    //Participants Grid management
    private RecyclerView mParticipantsGrid;

    public StaggeredGridLayoutManager getmLayoutManager() {
        return mLayoutManager;
    }

    private StaggeredGridLayoutManager mLayoutManager;
    private ParticipantsAdapter mParticipantsAdapter;
    private RelativeLayout mPublisherViewContainer,recycler_parent;
    long time;
    //------------------------------------
    private View mScreen1,mScreen3,mScreen4;
    RelativeLayout subscriberViewContainer,maingallery_parent;

    public RelativeLayout getGalleryMainView() {
        return galleryMainView;
    }

    RelativeLayout galleryMainView;
    private RelativeLayout screen1sub0;
    private ToggleButton toggleAudioScreen1Subscriber0;
    private RelativeLayout screen1sub1,screen3sub0,screen3sub1,screen3sub2,screen4sub0,screen4sub1,screen4sub2,screen4sub3,subscriberAudio0;
    private ToggleButton toggleAudioScreen1Subscriber1;
    private ImageView mLocalAudioOnlyImage,avatar;
    //---------------------screen2------------------------
    private String tokBoxData, apiKey, token, sessionId, publisherId, duration, startdate;
    private Handler hidehandler;
    private static final String FORMAT_2 = "%02d";
    ImageButton btnPausevideo, btnPauseaudio, btn_exit,swapCam;

    public RelativeLayout getLlcontrols() {
        return llcontrols;
    }

    RelativeLayout llcontrols;
    private TextView  init_info,  mAlert;
    private ImageButton remoteAudio0;

    public ImageButton getGalleryaudio() {
        return galleryaudio;
    }

    private ImageButton galleryaudio;
    private  Toolbar toolbar;
    private String mCurrentRemote;
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
        recycler_parent=(RelativeLayout)findViewById(R.id.recycler_parent);
        btnPausevideo = (ImageButton) findViewById(R.id.btn_pausevideo);
        btnPauseaudio = (ImageButton) findViewById(R.id.btn_pauseaudio);
        btn_exit = (ImageButton) findViewById(R.id.btn_exit);
        llcontrols = (RelativeLayout) findViewById(R.id.llcontrols);
        maingallery_parent=(RelativeLayout)findViewById(R.id.maingallery_parent);

        init_info=(TextView)findViewById(R.id.init_info);
        swapCam=(ImageButton)findViewById(R.id.swapCam);
        galleryaudio =(ImageButton) findViewById(R.id.galleryRemote);
        //mPublisherViewContainer.setOnTouchListener(new OnDragTouchListener(mPublisherViewContainer));
        mAlert = (TextView) findViewById(R.id.quality_warning);
        mSessionReconnectDialog =new ProgressDialog(this,R.style.MyAlertDialogStyle);
        DisplayMetrics maMetrics  =getDisplay();
         galleryMainView=(RelativeLayout)findViewById(R.id.maingallery);
        if(OpenTokActivity.this.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            mPublisherViewContainer.getLayoutParams().height=maMetrics.widthPixels/9;
            mPublisherViewContainer.getLayoutParams().width=maMetrics.widthPixels/9;
            mPublisherViewContainer.requestLayout();
           // tvtimer.setTextSize(maMetrics.widthPixels/170);

        }else{
            mPublisherViewContainer.getLayoutParams().height=maMetrics.widthPixels/5;
            mPublisherViewContainer.getLayoutParams().width=maMetrics.widthPixels/5;
            mPublisherViewContainer.requestLayout();
            //tvtimer.setTextSize(maMetrics.widthPixels/95);

        }
        toolbar = (Toolbar) findViewById(R.id.toolbar); // get the reference of Toolbar
        setSupportActionBar(toolbar);
        RelativeLayout mainLayout=(RelativeLayout)findViewById(R.id.activity_main);
        mainLayout.setSoundEffectsEnabled(false);
        mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
         if(llcontrols.getVisibility()==View.VISIBLE){
                    llcontrols.setVisibility(View.GONE);
                    toolbar.setVisibility(View.GONE);
                }else {
                    llcontrols.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);
                    hidehandler.removeCallbacks(hideControllerThread);
                    hideControllers();


                }
            }
        });
        mParticipantsGrid = (RecyclerView) findViewById(R.id.recycler_view);
         mLayoutManager = new StaggeredGridLayoutManager(
                1, LinearLayoutManager.HORIZONTAL);
        mParticipantsGrid.setLayoutManager(mLayoutManager);
        try {
            mParticipantsAdapter = new ParticipantsAdapter(OpenTokActivity.this, mParticipantsList);
            if (mParticipantsAdapter != null) {
                mParticipantsGrid.setAdapter(mParticipantsAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //----screen2 ---------------------------
        mScreen1 = findViewById(R.id.screen1);
        screen1sub0 =(RelativeLayout)findViewById(R.id.screen1sub0);

        screen1sub1=(RelativeLayout)findViewById(R.id.screen1sub1);

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
        //-------------------audio 0-------------------------------
        subscriberAudio0=(RelativeLayout)findViewById(R.id.subscriberAudio0);
        remoteAudio0=(ImageButton)findViewById(R.id.remoteAudio0);
        //--------------------------------------------------------
        //--------------------tollbar--------------------------
       // Setting/replace toolbar as the ActionBar

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
            String s = writeDate.format(new Date());
            Date date1 = writeDate.parse(s);
            Calendar c = Calendar.getInstance();
            long millis=(date1.getTime()+(Long.parseLong(duration)*60*1000))- c.getTimeInMillis();

            new CountDownTimer(millis, 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {
                    time=millisUntilFinished;
                    //tvtimer.setText("" + TimeUnit.MILLISECONDS.toHours(millisUntilFinished) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                    //tvtimer.setVisibility(View.GONE);
                     toolbar.setTitle("" + TimeUnit.MILLISECONDS.toHours(millisUntilFinished) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) + " : " + String.format(FORMAT_2, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                  toolbar.bringToFront();
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
     public void onDisableRemoteVideo(boolean video ,RelativeLayout ViewContainer ){
        if (!video) {
             RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            avatar = new ImageView(this);
            avatar.setImageResource(R.mipmap.avatar);
            avatar.setBackgroundResource(R.drawable.bckg_audio_only);
         ViewContainer.addView(avatar,layoutParams);
        } else {

           ViewContainer.removeAllViews();
        }
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        this.menu=menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if(mSubscribers.size()<=1){
            menu.findItem(R.id.muteall).setVisible(false);
        }
        return true;
    }
    // Activity's overrided method used to perform click events on menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        MenuItem settingsItem = menu.findItem(id);
        if (id == R.id.gallery ) {
            settingsItem.setVisible(false);
            menu.findItem(R.id.grid).setVisible(true);
            setRecylerView(true);
            return true;
        }else  if(id == R.id.grid){
            settingsItem.setVisible(false);
            menu.findItem(R.id.gallery).setVisible(true);
            setRecylerView(false);

            return true;
        }else if(id==R.id.muteall){
            if(!item.isChecked()){
                item.setChecked(true);
                muteAllParticipinats(false);
            }else{
                item.setChecked(false);
                muteAllParticipinats(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
/*     @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //------------------- Add new Menu Item ---------------------------------
        // Specify an id for the new menu item
        // Check the menu item already added or not
        for (int subscriber=0;subscriber<=mSubscribers.size()-1;subscriber++) {
            if (menu.findItem(subscriber) == null && mSubscribers.size() > 0) {
                MenuItem purple = menu.add(
                        Menu.NONE, // groupId
                        subscriber, // itemId
                        subscriber, // order
                        mSubscribers.get(subscriber).getStream().getName() // title
                );

                purple.setCheckable(true);


                purple.setShowAsActionFlags(
                        MenuItem.SHOW_AS_ACTION_NEVER
                );


                purple.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Toast.makeText(MainActivity.this, menuItem.getTitle() + " Muted", Toast.LENGTH_SHORT).show();
                        if(!menuItem.isChecked()){
                            menuItem.setChecked(true);
                            mSubscribers.get(menuItem.getItemId()).setSubscribeToAudio(false);
                        }else{
                            menuItem.setChecked(false);
                            mSubscribers.get(menuItem.getItemId()).setSubscribeToAudio(true);
                        }
                        changeRemoteAudioIcons();
                        return true;
                    }
                });

                //Toast.makeText(MainActivity.this, "Purple MenuItem Added", Toast.LENGTH_SHORT).show();
            }
        }

        super.onPrepareOptionsMenu(menu);
        return true;
    }*/
    private void setRecylerView(boolean val) {
        isRecylerViewPresent=val;
        if(val){
            maingallery_parent.setVisibility(View.VISIBLE);
            galleryMainView.setVisibility(View.VISIBLE);
            recycler_parent.setVisibility(View.VISIBLE);
            mParticipantsGrid.setVisibility(View.VISIBLE);
            recycler_parent.bringToFront();
            mParticipantsGrid.bringToFront();
            mParticipantsAdapter.notifyDataSetChanged();
            mParticipantsGrid.scrollToPosition(0);
            hidelayout(true);
        }else{
            maingallery_parent.setVisibility(View.GONE);
            recycler_parent.setVisibility(View.GONE);
            mParticipantsGrid.setVisibility(View.GONE);
            galleryMainView.setVisibility(View.GONE);
            galleryaudio.setVisibility(View.GONE);
            hidelayout(false);
        }

    }


    private void hidelayout(boolean val) {
        if(val){
            if(mSubscribers.size()==1){
                subscriberViewContainer.setVisibility(View.GONE);
            }else if(mSubscribers.size()==2){
                mScreen1.setVisibility(View.GONE);
            }else if(mSubscribers.size()==3){
                mScreen3.setVisibility(View.GONE);
            }else if(mSubscribers.size()==4 || mSubscribers.size()>4 ){
                mScreen4.setVisibility(View.GONE);
            }

        }else{
            if(mSubscribers.size()==1){
                subscriberViewContainer.setVisibility(View.VISIBLE);
            }else if(mSubscribers.size()==2){
                mScreen1.setVisibility(View.VISIBLE);
            }else if(mSubscribers.size()==3){
                mScreen3.setVisibility(View.VISIBLE);
            }else if(mSubscribers.size()==4 || mSubscribers.size()>4 ){
                mScreen4.setVisibility(View.VISIBLE);
            }
            updateView();
            calculateLayout( );
        }

    }
  private void muteAllParticipinats(boolean val){
      for (int subscriber=0;subscriber<=mSubscribers.size()-1;subscriber++){
          boolean unMute=mSubscribers.get(subscriber).getSubscribeToAudio();
          if(unMute && !val){
              mSubscribers.get(subscriber).setSubscribeToAudio(val);

          }else{
              mSubscribers.get(subscriber).setSubscribeToAudio(val);
          }



      }
      changeRemoteAudioIcons();
      getGalleryaudio().setImageResource(val ? R.drawable.audio:R.drawable.no_audio);
  }

    private void changeRemoteAudioIcons() {
        if(mSubscribers.size()==1){
            boolean isMuted=mSubscribers.get(0).getSubscribeToAudio();
            remoteAudio0.setTag(mSubscribers.get(0).getStream());
            remoteAudio0.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
        }else if(mSubscribers.size()==2){
            for (int i=0;i<mSubscribers.size();i++){
                boolean isMuted=mSubscribers.get(i).getSubscribeToAudio();
               int remoteId=getResources().getIdentifier("screen1remoteAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                ImageButton remoteAudio=(ImageButton)findViewById(remoteId);
                remoteAudio.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
            }
        }else if(mSubscribers.size()==3){
            for (int i=0;i<mSubscribers.size();i++){
                boolean isMuted=mSubscribers.get(i).getSubscribeToAudio();
                int remoteId=getResources().getIdentifier("screen3remoteAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                ImageButton remoteAudio=(ImageButton)findViewById(remoteId);
                remoteAudio.setTag(mSubscribers.get(i).getStream());
                remoteAudio.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
            }
        }else if(mSubscribers.size()==4){
            for (int i=0;i<mSubscribers.size();i++){
                boolean isMuted=mSubscribers.get(i).getSubscribeToAudio();
            int remoteId=getResources().getIdentifier("screen4remoteAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
             ImageButton remoteAudio=(ImageButton)findViewById(remoteId);
                remoteAudio.setOnClickListener(clickListener);
                remoteAudio.setTag(mSubscribers.get(i).getStream());
                remoteAudio.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
            }
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
            llcontrols.setVisibility(View.GONE);
        }
    };


    public void hideControllers() {

        hidehandler.postDelayed(hideControllerThread, 10000);
    }

    public void showControllers() {
        llcontrols.setVisibility(View.VISIBLE);
        hidehandler.removeCallbacks(hideControllerThread);
        hideControllers();
    }

      public View.OnClickListener muteSubscriberAudio =new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            int id=Integer.parseInt(galleryMainView.getTag().toString());
            Subscriber subscriber=mParticipantsList.get(id);
        boolean enableAudioOnly = subscriber.getSubscribeToAudio();
            if(enableAudioOnly){
                    subscriber.setSubscribeToAudio(false);
           ((ImageButton)view).setImageResource(R.drawable.no_audio);
      }else{
          subscriber.setSubscribeToAudio(true);
            ((ImageButton)view).setImageResource(R.drawable.audio);
}
}
    };


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
           mProgressDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
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
        mPublisher.setPublisherListener(this);
        mSession.setReconnectionListener(this);
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView());
        mPublisherViewContainer.setBackgroundResource(R.drawable.publisher_bckg);
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
         //   Toast.makeText(this, "New subscriber ignored. MAX_NUM_SUBSCRIBERS limit reached.", Toast.LENGTH_LONG).show();
            return;
        }

        final Subscriber subscriber = new Subscriber.Builder(OpenTokActivity.this, stream).build();
        subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        subscriber.setVideoListener(this);
        subscriber.setPreferredResolution(Participant.High_VIDEO_RESOLUTION);
        subscriber.setPreferredFrameRate(Participant.MAX_FPS);
        mSession.subscribe(subscriber);
        mSubscribers.add(subscriber);
        mParticipantsList.add(subscriber);
        mSubscriberStreams.put(stream, subscriber);
        mParticipantsGrid.scrollToPosition(mParticipantsAdapter.getItemCount() - 1);
        galleryMainView.setTag(0);
        if(mSubscribers.size()>=2 && !isRecylerViewPresent){
            menu.findItem(R.id.gallery).setVisible(true);
            menu.findItem(R.id.muteall).setVisible(true);
        }
       if(!isRecylerViewPresent){
           updateView();
           calculateLayout();
       }

        mParticipantsAdapter.notifyDataSetChanged();

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
     @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics maMetrics  =getDisplay();
        if (mSubscribers.size() == 1) {
            subscriberViewContainer.getLayoutParams().height = maMetrics.heightPixels;
            subscriberViewContainer.getLayoutParams().width = maMetrics.widthPixels;
            subscriberViewContainer.requestLayout();
        }else if(mSubscribers.size()==2){
            screen1sub0.getLayoutParams().height=maMetrics.heightPixels/2;
            screen1sub1.getLayoutParams().height=maMetrics.heightPixels/2;
        }else if(mSubscribers.size()==3){
            screen3sub0.getLayoutParams().height=maMetrics.heightPixels/2;
            screen3sub1.getLayoutParams().width=maMetrics.widthPixels/2;
            screen3sub2.getLayoutParams().width=maMetrics.widthPixels/2;
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


        }
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mPublisherViewContainer.getLayoutParams().height=maMetrics.widthPixels/9;
            mPublisherViewContainer.getLayoutParams().width=maMetrics.widthPixels/9;
            mPublisherViewContainer.requestLayout();

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mPublisherViewContainer.getLayoutParams().height=maMetrics.widthPixels/5;
            mPublisherViewContainer.getLayoutParams().width=maMetrics.widthPixels/5;
            mPublisherViewContainer.requestLayout();

        }

    }

    private void calculateLayout( ) {
        DisplayMetrics maMetrics  =getDisplay();
        if(mSubscribers.size()==1){
            boolean isMuted=mSubscribers.get(0).getSubscribeToAudio();
            mPublisherViewContainer.setVisibility(View.VISIBLE);
          ViewGroup parent = (ViewGroup) mParticipantsList.get(0).getView().getParent();
          if(parent!=null){
            parent.removeView(mParticipantsList.get(0).getView());
          }
          RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            subscriberViewContainer.addView(mSubscribers.get(0).getView(),layoutParams);
             ((GLSurfaceView) mSubscribers.get(0).getView()).setZOrderOnTop(false);
            subscriberAudio0.setVisibility(View.VISIBLE);
            subscriberAudio0.bringToFront();;
            remoteAudio0.setOnClickListener(clickListener);
            remoteAudio0.setTag(mSubscribers.get(0).getStream());
            remoteAudio0.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
            subscriberViewContainer.getLayoutParams().height=maMetrics.heightPixels;
            subscriberViewContainer.getLayoutParams().width=maMetrics.widthPixels;
            subscriberViewContainer.requestLayout();
          //  tvtimer.bringToFront();

        }else if(mSubscribers.size()==2){
            mScreen1.setVisibility(View.VISIBLE);
            screen1sub0.getLayoutParams().height=maMetrics.heightPixels/2;
            screen1sub1.getLayoutParams().height=maMetrics.heightPixels/2;
            for (int i=0;i<mSubscribers.size();i++){
              RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                boolean isMuted=mSubscribers.get(i).getSubscribeToAudio();
                int id = getResources().getIdentifier("screen1sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                int controlsId=getResources().getIdentifier("screen1subscriberAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
               int remoteId=getResources().getIdentifier("screen1remoteAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout ViewContainer = (RelativeLayout) findViewById(id);
                RelativeLayout controls=(RelativeLayout)findViewById(controlsId);
                ImageButton remoteAudio=(ImageButton)findViewById(remoteId);
                remoteAudio.setOnClickListener(clickListener);
                remoteAudio.setTag(mSubscribers.get(i).getStream());
              ViewGroup parent = (ViewGroup) mParticipantsList.get(i).getView().getParent();
              if(parent!=null){
                parent.removeView(mParticipantsList.get(i).getView());
              }
                ViewContainer.addView(mSubscribers.get(i).getView(),layoutParams);
                 ((GLSurfaceView) mSubscribers.get(i).getView()).setZOrderOnTop(false);
                controls.setVisibility(View.VISIBLE);
                remoteAudio.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
                controls.bringToFront();
            }

        }else if(mSubscribers.size()==3){
            mScreen3.setVisibility(View.VISIBLE);
            screen3sub0.getLayoutParams().height=maMetrics.heightPixels/2;
            screen3sub1.getLayoutParams().width=maMetrics.widthPixels/2;
            screen3sub2.getLayoutParams().width=maMetrics.widthPixels/2;

            for (int i=0;i<mSubscribers.size();i++){
              RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                boolean isMuted=mSubscribers.get(i).getSubscribeToAudio();
                int id = getResources().getIdentifier("screen3sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                int controlsId=getResources().getIdentifier("screen3subscriberAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                int remoteId=getResources().getIdentifier("screen3remoteAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout controls=(RelativeLayout)findViewById(controlsId);
                ImageButton remoteAudio=(ImageButton)findViewById(remoteId);
                remoteAudio.setOnClickListener(clickListener);
                remoteAudio.setTag(mSubscribers.get(i).getStream());
                remoteAudio.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                ViewGroup parent = (ViewGroup) mParticipantsList.get(i).getView().getParent();
                if(parent!=null){
                parent.removeView(mParticipantsList.get(i).getView());
               }
                subscriberViewContainer.addView(mSubscribers.get(i).getView(),layoutParams);
                 ((GLSurfaceView) mSubscribers.get(i).getView()).setZOrderOnTop(false);
                controls.setVisibility(View.VISIBLE);
                controls.bringToFront();
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
              RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                boolean isMuted=mSubscribers.get(i).getSubscribeToAudio();
                int id = getResources().getIdentifier("screen4sub" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                int controlsId=getResources().getIdentifier("screen4subscriberAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                int remoteId=getResources().getIdentifier("screen4remoteAudio" + (new Integer(i)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout controls=(RelativeLayout)findViewById(controlsId);
                ImageButton remoteAudio=(ImageButton)findViewById(remoteId);
                remoteAudio.setOnClickListener(clickListener);
                remoteAudio.setTag(mSubscribers.get(i).getStream());
                remoteAudio.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
              ViewGroup parent = (ViewGroup) mParticipantsList.get(i).getView().getParent();
              if(parent!=null){
                parent.removeView(mParticipantsList.get(i).getView());
              }
                subscriberViewContainer.addView(mSubscribers.get(i).getView(),layoutParams);
                 ((GLSurfaceView) mSubscribers.get(i).getView()).setZOrderOnTop(false);
                controls.setVisibility(View.VISIBLE);
                controls.bringToFront();
            }

        }
    }

    public DisplayMetrics getDisplay(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }
     private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Subscriber participant = mSubscriberStreams.get(view.getTag());
            boolean enableAudioOnly = participant.getSubscribeToAudio();
            if (enableAudioOnly) {
                participant.setSubscribeToAudio(false);
                ((ImageButton)view).setImageResource(R.drawable.no_audio);
            } else {
                participant.setSubscribeToAudio(true);
                ((ImageButton)view).setImageResource(R.drawable.audio);
            }
        }
    };

    @Override
    public void onStreamDropped(Session session, Stream stream) {
         Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

            clearView();


        Subscriber subscriber = mSubscriberStreams.get(stream);
        if (subscriber == null) {
            return;
        }

        int position = mParticipantsList.indexOf(subscriber);
        mSubscribers.remove(subscriber);
        mParticipantsList.remove(subscriber);
        mSubscriberStreams.remove(stream);
        mParticipantsAdapter.notifyItemRemoved(position);
        if(mSubscribers.size()<2 && !isRecylerViewPresent){
            menu.findItem(R.id.gallery).setVisible(false);
        }
        if(mSubscribers.size()==0) {
            init_info.setBackgroundResource(R.color.quality_warning);
            init_info.setTextColor(this.getResources().getColor(R.color.white));
            init_info.bringToFront();
            init_info.setVisibility(View.VISIBLE);
            isRecylerViewPresent=false;
            maingallery_parent.setVisibility(View.GONE);
            recycler_parent.setVisibility(View.GONE);
            mParticipantsGrid.setVisibility(View.GONE);
            galleryMainView.setVisibility(View.GONE);
            galleryaudio.setVisibility(View.GONE);
            galleryMainView.removeAllViews();
            menu.findItem(R.id.muteall).setVisible(false);

        }
        if(mSubscribers.size()==1){
            maingallery_parent.setVisibility(View.GONE);
            recycler_parent.setVisibility(View.GONE);
            mParticipantsGrid.setVisibility(View.GONE);
            galleryMainView.setVisibility(View.GONE);
            galleryaudio.setVisibility(View.GONE);
            galleryMainView.removeAllViews();
            menu.findItem(R.id.grid).setVisible(false);
            menu.findItem(R.id.muteall).setVisible(false);
            setRecylerView(false);
        }
        if(!isRecylerViewPresent) {
            calculateLayout();
        }
        mParticipantsAdapter.notifyDataSetChanged();
    }

      private void clearView() {
        if(mSubscribers.size()==1){
            subscriberViewContainer.removeView(mSubscribers.get(0).getView());
            subscriberAudio0.setVisibility(View.GONE);
            if(avatar!=null)
                subscriberViewContainer.removeView(avatar);
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
            Iterator<Subscriber> iter = mSubscribers.iterator();
            while (iter.hasNext()) {
                Subscriber subscriber = iter.next();
                if (subscriber!=null)
                    iter.remove();
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
        }else if(reason.equals("publishVideo")){
             if(isRecylerViewPresent){
               return;
            }
            Subscriber subscriber = mSubscriberStreams.get(subscriberKit.getStream());
            boolean isMuted=subscriber.getSubscribeToAudio();
            int position = mSubscribers.indexOf(subscriber);
            if (mSubscribers.size() == 1) {
                onDisableRemoteVideo(false,subscriberViewContainer);
               int id = getResources().getIdentifier("subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
               RelativeLayout layout=(RelativeLayout)findViewById(id);
                if(layout!=null) {
                    layout.bringToFront();
                }else if(controlsLayoutView.size()>0) {
                    RelativeLayout control=controlsLayoutView.get(id);
                    control.bringToFront();
                    control.requestLayout();
                   // addControls( position,subscriberViewContainer, id,isMuted);
                }
            }else if(mSubscribers.size()==2){
                int id = getResources().getIdentifier("screen1sub" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
              int controlsId=getResources().getIdentifier("screen1subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout controls=(RelativeLayout)findViewById(controlsId);
                onDisableRemoteVideo(false,subscriberViewContainer);
                if(controls!=null) {
                    controls.bringToFront();
                }else if(controlsLayoutView.size()>0)  {
                    RelativeLayout control=controlsLayoutView.get(id);
                    control.bringToFront();
                    control.requestLayout();
                    // addControls( position,subscriberViewContainer, id,isMuted);
                }
            }else if(mSubscribers.size()==3){
                int id = getResources().getIdentifier("screen3sub" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                int controlsId=getResources().getIdentifier("screen3subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout controls=(RelativeLayout)findViewById(controlsId);
                onDisableRemoteVideo(false,subscriberViewContainer);
                if(controls!=null) {
                    controls.bringToFront();
                }else if(controlsLayoutView.size()>0)  {
                    RelativeLayout control=controlsLayoutView.get(id);
                    control.bringToFront();
                    control.requestLayout();
                   // addControls( position,subscriberViewContainer, id,isMuted);
                }
            }else if(mSubscribers.size()==4){
                int id = getResources().getIdentifier("screen4sub" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                int controlsId=getResources().getIdentifier("screen4subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout controls=(RelativeLayout)findViewById(controlsId);
                onDisableRemoteVideo(false,subscriberViewContainer);
                if(controls!=null) {
                    controls.bringToFront();
                }else if(controlsLayoutView.size()>0)  {
                    RelativeLayout control=controlsLayoutView.get(id);
                    control.bringToFront();
                    control.requestLayout();
                    //addControls( position,subscriberViewContainer, id,isMuted);
                }
            }
        }
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String reason) {
        if (reason.equals("publishVideo")) {
             if(isRecylerViewPresent){
               return;
            }
            Subscriber subscriber = mSubscriberStreams.get(subscriberKit.getStream());
            boolean isMuted=subscriber.getSubscribeToAudio();
            int position = mSubscribers.indexOf(subscriber);
            if (mSubscribers.size() == 1) {
                onDisableRemoteVideo(true, subscriberViewContainer);
                subscriberViewContainer.addView(subscriber.getView());
                int id = getResources().getIdentifier("subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                addControls(position,subscriberViewContainer,id,isMuted);

            } else if (mSubscribers.size() == 2) {
                int id = getResources().getIdentifier("screen1sub" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                int controlsId=getResources().getIdentifier("screen1subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                onDisableRemoteVideo(true,subscriberViewContainer);
                subscriberViewContainer.addView(subscriber.getView());
                addControls(position,subscriberViewContainer,controlsId,isMuted);
            } else if (mSubscribers.size() == 3) {
                int id = getResources().getIdentifier("screen3sub" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                int controlsId=getResources().getIdentifier("screen3subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                onDisableRemoteVideo(true,subscriberViewContainer);
                subscriberViewContainer.addView(subscriber.getView());
                addControls(position,subscriberViewContainer,controlsId,isMuted);
            } else if (mSubscribers.size() == 4) {
                int id = getResources().getIdentifier("screen4sub" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                RelativeLayout subscriberViewContainer = (RelativeLayout) findViewById(id);
                int controlsId=getResources().getIdentifier("screen4subscriberAudio" + (new Integer(position)).toString(), "id", OpenTokActivity.this.getPackageName());
                onDisableRemoteVideo(true,subscriberViewContainer);
                subscriberViewContainer.addView(subscriber.getView());
                addControls(position,subscriberViewContainer,controlsId,isMuted);

            }
        }
    }

    private void addControls(int position,RelativeLayout subscriberViewContainer,int id,boolean isMuted) {
        RelativeLayout audioControl=new RelativeLayout(this);
        audioControl.setId(id);
        RelativeLayout.LayoutParams imParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        imParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imParams.setMargins(0,40,30,0);
        audioControl.setLayoutParams(imParams);
        ImageButton imgaButton = new ImageButton(this);
        imgaButton.setImageResource(isMuted ? R.drawable.audio : R.drawable.no_audio);
        imgaButton.setBackgroundResource(R.drawable.bckg_icon);
        imgaButton.setOnClickListener(clickListener);
        imgaButton.setTag(mSubscribers.get(position).getStream());
        audioControl.addView(imgaButton);
        subscriberViewContainer.addView(audioControl,imParams);
        controlsLayoutView.put(id,audioControl);
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

        }
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


}
