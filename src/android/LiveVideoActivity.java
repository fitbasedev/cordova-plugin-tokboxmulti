package com.fitbase.TokBox;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ionicframework.trainermobile381261.R;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static javafx.scene.input.KeyCode.R;


/**
 * Created by Priya on 9/10/2018.
 */

public class LiveVideoActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, com.fitbase.TokBox.LiveVideoActionBarFragment.PreviewControlCallbacks,
        Publisher.PublisherListener,
        Session.SessionListener,  Session.ReconnectionListener ,Subscriber.VideoListener, com.fitbase.TokBox.ParticipantsAdapter.ParticipantAdapterListener, com.fitbase.TokBox.ParticipantsListAdapter.ParticipantListAdapterListener{
  private static final String TAG = "liveVideoActivity " + LiveVideoActivity.class.getSimpleName();
  private static final int RC_SETTINGS_SCREEN_PERM = 123;
  private static final int RC_VIDEO_APP_PERM = 124;
  private Session mSession;
  private ArrayList<Subscriber> mSubscribers = new ArrayList<Subscriber>();
  private String publisherId;
  //private SessionManager localsession;
  private String sessionId,token, apikey,duration, startdate,tokBoxData;

    /*
    ---new

     */



  com.fitbase.TokBox.ParticipantsListAdapter mparticipantsListAdapter;


  String sessionStartDate;
  String sessionEndDate;

  long time;

  private GridLayoutManager mLayoutManager;

  public com.fitbase.TokBox.ParticipantsAdapter getmParticipantsAdapter() {
    return mParticipantsAdapter;
  }

  private com.fitbase.TokBox.ParticipantsAdapter mParticipantsAdapter;
  private List<com.fitbase.TokBox.Participant> mParticipantsList = new ArrayList<com.fitbase.TokBox.Participant>();


  private Publisher mPublisher;

  private static final String FORMAT_2 = "%02d";
  private ProgressDialog mProgressDialog,mSessionReconnectDialog;
  private boolean defaultView=true; //default view is grid layout
  private Handler hidehandler;
  private HashMap<Stream, Subscriber> mSubscriberStreams = new HashMap<Stream, Subscriber>();
  //Fragments and containers
  private com.fitbase.TokBox.LiveVideoActionBarFragment mActionBarFragment;
  private FragmentTransaction mFragmentTransaction;
  //menu item
  private Menu menu;
  private RelativeLayout.LayoutParams mLayoutParamsPreview;
  DisplayMetrics maMetrics;
  int height,width;
  private String joinMsg=null;
  View sheetView;
  int actionBarHeight;
  int count=0;
  public RelativeLayout getMainViewGallery() {
    return mainViewGallery;
  }

  /**
   *
   * Views
   **/

  //Participants Grid management
  RecyclerView mParticipantsGrid;
  //publisher container
  RelativeLayout mPublisherViewContainer;
  //controll action bar fragment
  RelativeLayout mActionBarContainer;
  //main gallery view
  RelativeLayout mainViewGallery;

  RelativeLayout mainConatiner;
  //Toolbar
  Toolbar toolbar;
  TextView textMsg;
  RelativeLayout mainsub;
  RelativeLayout smallViewLocal;
  ImageView pubAvatar;
  RelativeLayout parentPublisher;
  RelativeLayout recyclercontainer;

  public TextView getMaingalleryuserName() {
    return maingalleryuserName;
  }

  private TextView maingalleryuserName;
  private  ImageView mLocalAudioOnlyImage;

  public ImageView getMainGalleryAvatar() {
    return mainGalleryAvatar;
  }


  private ImageView mainGalleryAvatar; ;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live_video);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    com.fitbase.TokBox.Util.selectedposition=0;
    initView();

    tokBoxData = getIntent().getStringExtra("tokbox_obj");
    try {
      JSONObject jobj = new JSONObject(tokBoxData);
      apikey = jobj.getString("apiKey");
      token = jobj.getString("tokenId");
      sessionId = jobj.getString("liveSessionId");
      publisherId = jobj.getString("trainerUserid");
      duration = jobj.getString("duration");
      sessionStartDate = jobj.getString("startDate");

    } catch (Exception e) {
      e.printStackTrace();
    }

    //  localsession = new SessionManager(this);
   /* publisherId = localsession.getUserId();
    sessionId = getIntent().getStringExtra(AppUtils.LIVESESSIONID);
    token = getIntent().getStringExtra(AppUtils.OPENTOKTOKEN);
    apikey = getIntent().getStringExtra(AppUtils.OPENTOKAPIKEY);
    sessionStartDate = getIntent().getStringExtra("live_session_sdate");
    sessionEndDate = getIntent().getStringExtra("live_session_edate");*/
    hidehandler = new Handler();
    calculateLocalViewHeightWidth();

    //init controls fragments
    if (savedInstanceState == null) {
      mFragmentTransaction = getSupportFragmentManager().beginTransaction();
      initActionBarFragment(); //to enable/disable local media

      mFragmentTransaction.commitAllowingStateLoss();
    }

    requestPermissions();
    runLiveTimmer(sessionStartDate, duration);
    try {
      mparticipantsListAdapter = new com.fitbase.TokBox.ParticipantsListAdapter(LiveVideoActivity.this, mParticipantsList, LiveVideoActivity.this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  private void initActionBarFragment() {
    mActionBarFragment = new com.fitbase.TokBox.LiveVideoActionBarFragment();
    getSupportFragmentManager().beginTransaction()
            .add(R.id.actionbar_fragment_container, mActionBarFragment).commit();


  }
  public void initView() {
    mParticipantsGrid = (RecyclerView) findViewById(R.id.grid_container);
    mPublisherViewContainer =(RelativeLayout) findViewById(R.id.publisher_container);
    mActionBarContainer=(RelativeLayout) findViewById(R.id.actionbar_fragment_container);
    mainViewGallery=(RelativeLayout) findViewById(R.id.mainviewgallery);
    mainConatiner=(RelativeLayout) findViewById(R.id.mainview);
    toolbar=(Toolbar) findViewById(R.id.live_toolbar);
    textMsg=(TextView) findViewById(R.id.textmsg);
    mainsub=(RelativeLayout) findViewById(R.id.mainsub);
    smallViewLocal=(RelativeLayout) findViewById(R.id.smallviewlocal);
    parentPublisher=(RelativeLayout) findViewById(R.id.parent_publisher);
    pubAvatar=(ImageView) findViewById(R.id.pubavatar);
    recyclercontainer=(RelativeLayout) findViewById(R.id.recyclercontainer);
    mainGalleryAvatar =(ImageView)findViewById(R.id.mainAvatar);
    maingalleryuserName=(TextView)findViewById(R.id.maingalleryuserName);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.live_session_count_icon);
    mainConatiner.setSoundEffectsEnabled(false);
    mainConatiner.setOnTouchListener(null);
    actionBarHeight= mActionBarContainer.getLayoutParams().height;
    mainsub.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideShowControls();
      }
    });
    mPublisherViewContainer.setVisibility(View.GONE);
    mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
    setupMultipartyLayout();
    mParticipantsGrid.setHasFixedSize(true);
    mParticipantsGrid.setLayoutManager(mLayoutManager);
    try {
      mParticipantsAdapter = new com.fitbase.TokBox.ParticipantsAdapter(LiveVideoActivity.this, mParticipantsList, LiveVideoActivity.this);
      if (mParticipantsAdapter != null) {
        mParticipantsGrid.setAdapter(mParticipantsAdapter);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private Runnable hideControllerThread = new Runnable() {

    public void run() {
      mActionBarContainer.setVisibility(View.GONE);
      toolbar.setVisibility(View.GONE);
      setRecyclerViewOnBottom(true);


    }
  };


  public void hideControllers() {

    hidehandler.postDelayed(hideControllerThread, 10000);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
    this.menu=menu;
    getMenuInflater().inflate(R.menu.live_video_menu_item, menu);
    return true;
  }
  // Activity's overrided method used to perform click events on menu items
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    MenuItem settingsItem = menu.findItem(id);
    if (id == R.id.gallery ) {
      settingsItem.setVisible(false);
      menu.findItem(R.id.grid).setVisible(true);
      com.fitbase.TokBox.Util.selectedposition=0;
      changeView();
      return true;
    }else  if(id == R.id.grid){
      settingsItem.setVisible(false);
      menu.findItem(R.id.gallery).setVisible(true);
      changeView();
      return true;
    }else if(id==R.id.swipecamera){
      mPublisher.cycleCamera();
      //flipView(mPublisher.getView());
      return true;
    }else if(id==R.id.options) {
      final  BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(LiveVideoActivity.this);
      sheetView = LiveVideoActivity.this.getLayoutInflater().inflate(R.layout.bottom_sheet, null);
      mBottomSheetDialog.setContentView(sheetView);
      mBottomSheetDialog.show();
      updateMuteAll();
      final TextView tv = (TextView) sheetView.findViewById(R.id.muteText);
      final ImageView imageView= (ImageView) sheetView.findViewById(R.id.tv_muteImage);
      LinearLayout participants= (LinearLayout) sheetView.findViewById(R.id.tv_participants);
      int participents=mParticipantsList.size();
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          switch (view.getId()) {
            case R.id.muteText:
              if (tv.getText().toString().equals("Mute All")) {
                for (int i = 0; i < mParticipantsList.size(); i++) {
                  com.fitbase.TokBox.Participant participant = mParticipantsList.get(i);
                  participant.getStatus().setSubscribeToAudio(false);
                  tv.setText("Unmute All");
                  imageView.setImageResource(R.drawable.mute_all_off);
                }
              } else if (tv.getText().toString().equals("Unmute All")) {
                for (int i = 0; i < mParticipantsList.size(); i++) {
                  com.fitbase.TokBox.Participant participant = mParticipantsList.get(i);
                  participant.getStatus().setSubscribeToAudio(true);
                  tv.setText("Mute All");
                  imageView.setImageResource(R.drawable.mute_mic);
                }
              }
              break;
          }
          mParticipantsAdapter.notifyDataSetChanged();
          mBottomSheetDialog.dismiss();
        }

      });
      participants.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          try {
            participants();
            mBottomSheetDialog.dismiss();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });



    }

    return super.onOptionsItemSelected(item);
  }

  private void flipView(View mPublisherViewContainer) {
    ObjectAnimator animation = ObjectAnimator.ofFloat(mPublisherViewContainer, "rotationY", 0.0f, 180f);
    animation.setDuration(1000);
    animation.setRepeatCount(0);
    animation.setInterpolator(new AccelerateDecelerateInterpolator());
    animation.start();
  }

  public void updateMuteAll(){
    int count=0;
    TextView tv = (TextView) sheetView.findViewById(R.id.muteText);
    ImageView imageView= (ImageView) sheetView.findViewById(R.id.tv_muteImage);
    for (int i = 0; i < mParticipantsList.size(); i++) {
      com.fitbase.TokBox.Participant subscriber=mParticipantsList.get(i);
      boolean enableAudioOnly = subscriber.getStatus().getSubscribeToAudio();
      if(enableAudioOnly){
        count--;
      }else{
        count++;
      }
    }
    if(count==mParticipantsList.size()){
      tv.setText("Unmute All");
      imageView.setImageResource(R.drawable.mute_all_off);
    }else{
      tv.setText("Mute All");
      imageView.setImageResource(R.drawable.mute_mic);
    }if(mParticipantsList.size()==0){
      tv.setText("Mute All");
      imageView.setImageResource(R.drawable.mute_mic);
    }
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
      mSession = new Session.Builder(LiveVideoActivity.this, apikey, sessionId).build();
      mSession.setSessionListener(this);
      mProgressDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
      mProgressDialog.setCanceledOnTouchOutside(false);
      mProgressDialog.setTitle("Please wait");
      mProgressDialog.setMessage("Connecting...");
      mProgressDialog.show();
      mSession.connect(token);
    } else {
      EasyPermissions.requestPermissions(LiveVideoActivity.this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onConnected(Session session) {
    Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());
    mPublisher = new Publisher.Builder(LiveVideoActivity.this).name("publisher").build();
    mPublisher.setPublisherListener(this);
    mSession.setReconnectionListener(this);
    mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
    mSession.publish(mPublisher);
    setLocalView(mPublisher.getView(),true);
  }

  @Override
  public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
//        Log.d(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");

  }

  @Override
  public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
//        Log.d(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
  }

  @Override
  public void onError(PublisherKit publisherKit, OpentokError opentokError) {
//        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");

    Toast.makeText(this, "Session error.", Toast.LENGTH_LONG).show();
    finish();
  }


  @Override
  public void onDisconnected(Session session) {
    Log.d(TAG, "onDisconnected: disconnected from session " + session.getSessionId());
    mSession = null;
  }

  @Override
  public void onError(Session session, OpentokError opentokError) {
    Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());
    Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());
    mProgressDialog.dismiss();
    Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
    finish();
  }



  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onStreamReceived(Session session, Stream stream) {
    Log.d(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());
    final Subscriber subscriber = new Subscriber.Builder(LiveVideoActivity.this, stream).build();
    subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
    subscriber.setVideoListener(this);
    mSubscribers.add(subscriber);
    com.fitbase.TokBox.Participant newParticipant = new com.fitbase.TokBox.Participant(com.fitbase.TokBox.Participant.Type.REMOTE, subscriber,getParticipantSize(mSubscribers.size()),stream.getStreamId());
    if(mParticipantsList.size()==0){
      setLocalView(mPublisher.getView(),false);
      // ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
    }
    addNewParticipant(newParticipant);

    mSession.subscribe(subscriber);

    //    mParticipantsList.add(newParticipant);
    mSubscriberStreams.put(stream, subscriber);
    if(mParticipantsList.size()>=2 && defaultView){
      menu.findItem(R.id.gallery).setVisible(true);
    }if(mParticipantsList.size()==1){
      menu.findItem(R.id.options).setVisible(true);
    }
    joinMsg=stream.getName()+" Joined the session";
    showAlert(joinMsg);
    mparticipantsListAdapter.notifyDataSetChanged();//update participants list adapter
  }
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void addNewParticipant(com.fitbase.TokBox.Participant newParticipant) {
    mParticipantsList.add(newParticipant);
    updateParticipantList();
  }
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onStreamDropped(Session session, Stream stream) {
    Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());
    Subscriber subscriber = mSubscriberStreams.get(stream);
    if (subscriber == null) {
      return;
    }

    removeParticipant(com.fitbase.TokBox.Participant.Type.REMOTE,stream.getStreamId());
    joinMsg =stream.getName() +" left the session";
    showAlert(joinMsg);
  }



  @Override
  protected void onResume() {
    super.onResume();
    if (mSession == null) {
      return;
    }
    mSession.onResume();
    //  hideControllers();
  }


  @Override
  protected void onPause() {
    super.onPause();
    if (mSession == null) {
      return;
    }
    mSession.onPause();

    if (isFinishing()) {
      disconnectSession();
    }
  }

  public Publisher getmPublisher() {
    return mPublisher;
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
  protected void onDestroy() {
    disconnectSession();
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }
    super.onDestroy();

  }

  // Check live session duration,run the count down
  public void runLiveTimmer(String sessionStartDate, String duration) {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // missing line
      Date date = sdf.parse(sessionStartDate.split("\\.")[0]);
      SimpleDateFormat writeDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.ENGLISH);
      writeDate.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
      String s = writeDate.format(date);
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
  }
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void removeParticipant(com.fitbase.TokBox.Participant.Type type, String id) {
    for (int i = 0; i < mParticipantsList.size(); i++) {
      com.fitbase.TokBox.Participant participant = mParticipantsList.get(i);
      if (participant.getType().equals(type)) {
        if (type.equals(com.fitbase.TokBox.Participant.Type.REMOTE)) {
          //remote participant
          if (participant.getId().equals(id)) {
            mParticipantsList.remove(i);
          }

        } else {
          //local participant
          mParticipantsList.remove(i);
        }
      }
    }
    //update list
    if(defaultView)
      updateParticipantList();
    //Collections.reverse(mParticipantsList);
    mParticipantsAdapter.notifyDataSetChanged();
    if(mParticipantsList.size()==1){
      menu.findItem(R.id.gallery).setVisible(false);
      menu.findItem(R.id.grid).setVisible(false);
      defaultView=false;
      changeView();
    }
    mparticipantsListAdapter.notifyDataSetChanged();
    if(mParticipantsList.size()==0){
      menu.findItem(R.id.options).setVisible(false);
      setLocalView(mPublisher.getView(),true);
      ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(false);
    }
  }

  /**
   * set the span size based on position
   */
  private void setupMultipartyLayout() {
    mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override
      public int getSpanSize(int position) {
        if (mParticipantsList.size() == 1) {
          return 2;
        } else {
          if (mParticipantsList.size() == 2) {
            if (position == 0) {
              return 2;
            }
            return 1;
          } else {
            if (mParticipantsList.size() == 3) {
              if (position == 0 || position == 1) {
                return 1;
              } else {
                return 2;
              }
            } else {
              if (mParticipantsList.size() == 4) {
                return 1;
              } else {
                if (mParticipantsList.size() > 4) {
                  if (mParticipantsList.size() % 2 != 0) {
                    if (position == mParticipantsList.size() - 1) {
                      return 2;
                    } else {
                      return 1;
                    }
                  } else {
                    return 1;
                  }
                }
              }
            }
          }
        }
        return 1;
      }
    });
  }

  @Override
  public void mediaControlChanged(String remoteId) {

  }



  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void showViewOnBigScreen(int pos) {
    com.fitbase.TokBox.Util.selectedposition=pos;
    changeView();
  }

  @Override
  public void onTapshowHideControls() {
    hideShowControls();
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

    }else if(reason.equals("publishVideo")) {
      int getPostion=findPostion(subscriberKit.getStream().getStreamId());
      if(getPostion!=-1){
        mParticipantsAdapter.notifyItemChanged(getPostion);
        mparticipantsListAdapter.notifyItemChanged(getPostion);
      }

    }
  }

  @Override
  public void onVideoEnabled(SubscriberKit subscriberKit, String reason) {

    if (reason.equals("publishVideo")) {
      int getPostion=findPostion(subscriberKit.getStream().getStreamId());
      if(getPostion!=-1){
        mParticipantsAdapter.notifyItemChanged(getPostion);
      }
    }
  }

  @Override
  public void onVideoDisableWarning(SubscriberKit subscriberKit) {

  }

  @Override
  public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

  }

  private int findPostion(String streamId) {
    for (int sub=0;sub<=mParticipantsList.size();sub++){
      com.fitbase.TokBox.Participant participant=mParticipantsList.get(sub);
      if(streamId.equals(participant.getId())){
        return sub;
      }
    }
    return -1;
  }

  @Override
  public void onDisableLocalVideo(boolean video) {
    if (mPublisher != null) {
      mPublisher.setPublishVideo(video);
      RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
              width, height);
      if (!video) {
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT,RelativeLayout.TRUE);
        mLocalAudioOnlyImage = new ImageView(this);
        mLocalAudioOnlyImage.setImageResource(R.drawable.mute_subscriber_video);
        if(mPublisherViewContainer.getVisibility()==View.VISIBLE) {

          mPublisherViewContainer.addView(mLocalAudioOnlyImage, layoutParams);
        }else{
          pubAvatar.getLayoutParams().height=height/4;
          pubAvatar.getLayoutParams().width=height/4;
          smallViewLocal.addView(pubAvatar );
        }

      } else {
        mPublisherViewContainer.removeView(mLocalAudioOnlyImage);
        smallViewLocal.removeView(pubAvatar);

      }
    }
  }



  @Override
  public void onDisableLocalAudio(boolean audio) {

    if (mPublisher != null) {
      mPublisher.setPublishAudio(audio);
    }
  }




  /**
   * filter view -Recycler to grid  visa versa
   */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public void changeView() {
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mParticipantsGrid.getLayoutParams();
    //    RelativeLayout.LayoutParams recyclerViewContainer= (RelativeLayout.LayoutParams)recyclercontainer.getLayoutParams();
    if(defaultView){
      defaultView=false;
      mLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
      mLayoutManager.setSpanCount(1);
      params.setMargins(width+30,10,10,actionBarHeight+20);
      params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
      params.addRule(RelativeLayout.ABOVE,
              R.id.actionbar_fragment_container);
      params.height= RelativeLayout.LayoutParams.WRAP_CONTENT;
      mParticipantsGrid.setLayoutParams(params);
      mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
          return 1;
        }
      });
      updateSizeForRecyclerView();

    }else{
      defaultView=true;
      mLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
      mLayoutManager.setSpanCount(2);
      params.setMargins(0,0,0,0);
      params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
      params.addRule(RelativeLayout.ABOVE, 0);
      mParticipantsGrid.setLayoutParams(params);
      setupMultipartyLayout();
      updateParticipantList();
    }

    //    mPublisherViewContainer.setOnTouchListener(defaultView ? new OnDragTouchListener(mPublisherViewContainer) : null);
    mParticipantsAdapter.notifyDataSetChanged();
    mparticipantsListAdapter.notifyDataSetChanged();

  }


  /**
   * exit button
   */
  @Override
  public void leaveRoom() {
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    onBackPressed();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public void updateSizeForRecyclerView(){
    for (int i = 0; i < mParticipantsList.size(); i++) {
      com.fitbase.TokBox.Participant participant = mParticipantsList.get(i);
      DisplayMetrics metrics = new DisplayMetrics();
      getWindowManager().getDefaultDisplay().getMetrics(metrics);
      Configuration config=LiveVideoActivity.this.getResources().getConfiguration();
      int width,height;
      if(config.orientation==Configuration.ORIENTATION_PORTRAIT)
      {
        width = metrics.widthPixels/5;
        height = metrics.widthPixels/5;
      }else{
        width = metrics.widthPixels/9;
        height = metrics.widthPixels/9;
      }

      participant.setContainer(new Size(width,height ));

      mParticipantsList.set(i, participant);
    }

    // Collections.reverse(mParticipantsList);
    mParticipantsAdapter.notifyDataSetChanged();
    mParticipantsGrid.scrollToPosition(com.fitbase.TokBox.Util.selectedposition);
    mparticipantsListAdapter.notifyDataSetChanged();
  }
  /**
   * set the participant conatier size when any participant will join
   */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void updateParticipantList() {
    //update list
    if(defaultView) {
      for (int i = 0; i < mParticipantsList.size(); i++) {
        com.fitbase.TokBox.Participant participant = mParticipantsList.get(i);
        if (i == 0) {
          DisplayMetrics metrics = new DisplayMetrics();
          getWindowManager().getDefaultDisplay().getMetrics(metrics);
          Size newSize = getParticipantSize(i);
          int width;
          if (mParticipantsList.size() > 2) {
            width = mParticipantsList.size() % 2 != 0 ? metrics.widthPixels : metrics.widthPixels / 2;
          } else {
            width = metrics.widthPixels;
          }
          participant.setContainer(new Size(width, newSize.getHeight()));
        } else {
          participant.setContainer(getParticipantSize(i));
        }
        mParticipantsList.set(i, participant);
      }
      Collections.reverse(mParticipantsList);
      mParticipantsAdapter.notifyDataSetChanged();
      mparticipantsListAdapter.notifyDataSetChanged();

    }else {
      updateSizeForRecyclerView();
    }
  }

  /**
   *
   * @param pos
   * @return Size
   *  it will set the size of  particiapnt size dynamically
   */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private Size getParticipantSize(int pos) {
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    int width = metrics.widthPixels; // ancho absoluto en pixels
    int height = metrics.heightPixels; // alto absoluto en pixels
    if (mParticipantsList.size() == 2) {
      return new Size(width, height / 2);
    } else if (mParticipantsList.size() > 2 && mParticipantsList.size() <=4) {
      return new Size(width / 2, height / 2);
    }else  if ( mParticipantsList.size() >4 && mParticipantsList.size() <=6 ) {
      return new Size(width / 2, height / 3);
    }else  if( mParticipantsList.size() >6 && mParticipantsList.size() <=8){
      return new Size(width / 2, height / 4);
    }else if( mParticipantsList.size() >8 && mParticipantsList.size() <=10){
      return new Size(width / 2, height / 5);
    }
    return new Size(width, height);
  }

  /**
   * get display
   * @return
   */
  public DisplayMetrics getDisplay(){
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    return metrics;
  }

  /**
   * show reconnecting dialog
   */
  private void showReconnectionDialog(boolean show) {
    if (show) {
      mSessionReconnectDialog = new ProgressDialog(this,R.style.MyAlertDialogStyle);
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

  public GridLayoutManager getmLayoutManager() {
    return mLayoutManager;
  }

  /**
   *
   * @param localView local stream
   * @param isLocalView boolean value full screen -true,small size- false
   */
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void setLocalView(View localView, boolean isLocalView){
    if (localView != null) {
      mProgressDialog.dismiss();
      mPublisherViewContainer.removeAllViews();
      smallViewLocal.removeAllViews();
      textMsg.setVisibility(localView.VISIBLE);
      localView.setBackgroundResource(0);
      localView.setOnTouchListener(null);
      mLayoutParamsPreview = new RelativeLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      if (!isLocalView) {
        textMsg.setVisibility(localView.GONE);
        mLayoutParamsPreview = new RelativeLayout.LayoutParams(width, height);

             /*       mLayoutParamsPreview.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                            RelativeLayout.TRUE);
                    mLayoutParamsPreview.addRule(RelativeLayout.ABOVE,
                            R.id.actionbar_fragment_container);
                    mLayoutParamsPreview.addRule(RelativeLayout.LEFT_OF, R.id.grid_container);*/
        mLayoutParamsPreview.setMargins(20, 0, 20,    30);
        localView.setBackground(getDrawable(R.drawable.trainer_border));
        smallViewLocal.addView(localView,mLayoutParamsPreview);
        smallViewLocal.setVisibility(View.VISIBLE);
      /*  ((GLSurfaceView)localView).setZOrderOnTop(false);
        ((GLSurfaceView) localView).setZOrderMediaOverlay(false);*/
        mPublisherViewContainer.setVisibility(View.GONE);
        return;
      }
      //localView.setBackgroundResource(0);

      mPublisherViewContainer.addView(localView, mLayoutParamsPreview);
      mPublisherViewContainer.setVisibility(View.VISIBLE);
     /* ((GLSurfaceView)localView).setZOrderOnTop(  true  );
      ((GLSurfaceView) localView).setZOrderMediaOverlay(  false );*/
      smallViewLocal.setVisibility(View.GONE);

    }
    else {
      mPublisherViewContainer.removeAllViews();
      textMsg.setVisibility(localView.VISIBLE);
      localView.setBackgroundResource(0);
      localView.setOnTouchListener(null);
    }
  }
  //set the hight and width of publisher view dynamically
  private void calculateLocalViewHeightWidth(){
    maMetrics  =getDisplay();
    if (LiveVideoActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      height = maMetrics.widthPixels / 9;
      width = maMetrics.widthPixels / 9;
    } else {
      height = maMetrics.widthPixels / 5;
      width = maMetrics.widthPixels / 5;
    }
  }

  public void onRemoteAudioChanged(View view) {

    handelMuteOnParticipants (view);


  }
  //Alert Message Toaster For Participant joining,left,muteaudio,unmuteaudio
  private void showAlert(String joinMsg){
    Toast toast = Toast.makeText(this, joinMsg, Toast.LENGTH_SHORT);
    View view = toast.getView();
    view.setBackgroundResource(R.drawable.toaster);
    if(joinMsg.contains("Mic off") || joinMsg.contains("Mic on") ){
      toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,0,2*width+70);
    }else{
      toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL,0, 0);
    }
    toast.show();
  }
  public RecyclerView getmParticipantsGrid() {
    return mParticipantsGrid;
  }
  //show and hide controll
  private void hideShowControls(){
    if(mActionBarContainer.getVisibility()==View.VISIBLE){
      mActionBarContainer.setVisibility(View.GONE);
      toolbar.setVisibility(View.GONE);
      setRecyclerViewOnBottom(true);
    }else {
      mActionBarContainer.setVisibility(View.VISIBLE);
      toolbar.setVisibility(View.VISIBLE);
      setRecyclerViewOnBottom(false);
      hidehandler.removeCallbacks(hideControllerThread);
      hideControllers();


    }
  }


  public void participants() throws Exception {
    final Dialog dialog = new Dialog(this, android.R.style.Theme_Light);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.participants_video_screen);
    dialog.show();
    ImageView close_btn=(ImageView) dialog.findViewById(R.id.close_btn);
    close_btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dialog.dismiss();
      }
    });
    RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.participants_list_name);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    recyclerView.setHasFixedSize(true);
    recyclerView.setLayoutManager(linearLayoutManager);
    if (mparticipantsListAdapter != null) {
      mparticipantsListAdapter.notifyDataSetChanged();
      if(mparticipantsListAdapter==null){
        dialog.dismiss();
      }
      recyclerView.setAdapter(mparticipantsListAdapter);
    }

  }
  @Override
  public void muteFromListOfParticipants(View view) {
    handelMuteOnParticipants (view);
    mParticipantsAdapter.notifyItemChanged(view.getId());
  }



  private void handelMuteOnParticipants(View view){
    com.fitbase.TokBox.Participant subscriber=mParticipantsList.get(view.getId());
    boolean enableAudioOnly = subscriber.getStatus().getSubscribeToAudio();
    if (enableAudioOnly) {
      subscriber.getStatus().setSubscribeToAudio(false);
      ((ImageView)view).setImageResource(R.drawable.muted_mic_red_icon);
    } else {
      subscriber.getStatus().setSubscribeToAudio(true);
      ((ImageView)view).setImageResource(R.drawable.mic_green_icon);
    }
    showAlert(enableAudioOnly ? "Mic off" : " Mic on");
  }

  private void setRecyclerViewOnBottom(boolean value){
    if(!defaultView) {
      RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mParticipantsGrid.getLayoutParams();
      RelativeLayout.LayoutParams pubParma=(RelativeLayout.LayoutParams) smallViewLocal.getLayoutParams();
      if (value) {
        params.setMargins(width+30,10,10,30);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ABOVE, 0);
        mParticipantsGrid.setLayoutParams(params);
        mParticipantsGrid.requestLayout();

      } else {
        params.setMargins(width + 30, 10, 10, actionBarHeight + 20);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ABOVE, R.id.actionbar_fragment_container);
        params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
        mParticipantsGrid.setLayoutParams(params);
        mParticipantsGrid.requestLayout();

      }
    }
  }


}

