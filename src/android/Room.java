package com.fitbase.TokBox;

import android.content.Context;

import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;



import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anshul on 1/12/2018.
 */


  public class Room extends Session {

    private static final String LOGTAG = "opentok-room";

    private Context mContext;
    private String mToken;
    private Publisher mPublisher;
    private Participant mLastParticipant;
    private String mPublisherName = null;
    private Publisher.CameraCaptureResolution mPublisherRes =
      Publisher.CameraCaptureResolution.HIGH;
    private Publisher.CameraCaptureFrameRate mPublisherFps =
      Publisher.CameraCaptureFrameRate.FPS_30;
    private HashMap<Stream, Participant> mParticipantStream = new HashMap<Stream, Participant>();
    private HashMap<String, Participant> mParticipantConnection = new HashMap<String, Participant>();
    private ArrayList<Participant> mParticipants = new ArrayList<Participant>();

    private ViewGroup mPreview;
    private LinearLayout mParticipantsViewContainer;
    private ViewGroup mLastParticipantView;
    private OpenTokActivity mActivity;




    public Room(Context context, String sessionId, String token, String apiKey) {
      super(context, apiKey, sessionId);
      mToken = token;
      mContext = context;

      mActivity = (OpenTokActivity) this.mContext;


    }

    public void setParticipantsViewContainer(
      LinearLayout container,
      ViewGroup lastParticipantView,
      View.OnClickListener onSubscriberUIClick) {
      mParticipantsViewContainer = container;
      mLastParticipantView = lastParticipantView;
    }

    public void setPreviewView(ViewGroup preview) {
      mPreview = preview;
    }

    public void connect() {
      connect(mToken);
    }

    public void setPublisherSettings(Publisher.CameraCaptureResolution resolution,
                                     Publisher.CameraCaptureFrameRate fps) {
      mPublisherRes = resolution;
      mPublisherFps = fps;
    }


    public Publisher getPublisher() {
      return mPublisher;
    }

    public Participant getLastParticipant() {
      return mLastParticipant;
    }

    public ArrayList<Participant> getParticipants() {
      return mParticipants;
    }

    public LinearLayout getParticipantsViewContainer() {
      return mParticipantsViewContainer;
    }

    public ViewGroup getLastParticipantView() {
      return mLastParticipantView;
    }

    @Override
    public void disconnect() {
      super.disconnect();

    }

    private Publisher createPublisher() {
      return (new Publisher.Builder(mContext))
        .name(mPublisherName)
        .resolution(mPublisherRes)
        .frameRate(mPublisherFps)
        .build();
    }

    @Override
    protected void onConnected() {
      //check simulcast case for publisher
      mActivity.getmProgressDialog().dismiss();
      mPublisher = createPublisher();
      mPublisher.setAudioFallbackEnabled(true);
      mPublisher.setPublisherListener(new PublisherKit.PublisherListener() {
        @Override
        public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

          Log.d(LOGTAG, "onStreamCreated!!");
        }

        @Override
        public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
          Log.d(LOGTAG, "onStreamDestroyed!!");
        }

        @Override
        public void onError(PublisherKit publisherKit, OpentokError opentokError) {
          mActivity.getmProgressDialog().dismiss();
          Log.d(LOGTAG, "onError!!");
        }
      });
      publish(mPublisher);

      // Add video preview
      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.MATCH_PARENT
      );
      ((SurfaceView) mPublisher.getView()).setZOrderOnTop(true);

      mPreview.addView(mPublisher.getView(), lp);
      mPreview.setVisibility(View.VISIBLE);
      // mPublisher.getView().setOnClickListener(mActivity.onPubViewClick);
      //   mPublisher.getView().setOnLongClickListener(mActivity.onPubStatusClick);
      mPublisher.setStyle(
        BaseVideoRenderer.STYLE_VIDEO_SCALE,
        BaseVideoRenderer.STYLE_VIDEO_FILL
      );


    }

    @Override
    protected void onReconnecting() {
      super.onReconnecting();
      mActivity.showReconnectingDialog(true);
    }

    @Override
    protected void onReconnected() {
      super.onReconnected();
      mActivity.showReconnectingDialog(false);
    }

    @Override
    protected void onStreamReceived(Stream stream) {

      Participant p = new Participant(mContext, stream);

      if (mParticipants.size() > 0) {
        final Participant lastParticipant = mParticipants.get(mParticipants.size() - 1);
        this.mLastParticipantView.removeView(lastParticipant.getView());

        final LinearLayout.LayoutParams lp = mActivity.getQVGALayoutParams();
        lastParticipant.setPreferredResolution(Participant.High_VIDEO_RESOLUTION);
        lastParticipant.setPreferredFrameRate(Participant.MAX_FPS);
        this.mParticipantsViewContainer.addView(lastParticipant.getView(), lp);
        lastParticipant.setSubscribeToVideo(true);
        lastParticipant.getView().setOnLongClickListener(longClickListener);
       // lastParticipant.getView().setOnClickListener(clickListener);
      }

      mActivity.getLoadingSub().setVisibility(View.VISIBLE);
      p.setPreferredResolution(Participant.High_VIDEO_RESOLUTION);
      p.setPreferredFrameRate(Participant.MAX_FPS);
     // p.getView().setOnClickListener(clickLastParticipantListener);
      mLastParticipant = p;

      //Subscribe to this participant
      this.subscribe(p);

      mParticipants.add(p);
      p.getView().setTag(stream);

      mParticipantStream.put(stream, p);
      mParticipantConnection.put(stream.getConnection().getConnectionId(), p);
      if(mParticipants.size()>1){
      DisplayMetrics metrics=mActivity.getDisplay();
        Configuration newConfig=mActivity.getResources().getConfiguration();
        if(newConfig.orientation==Configuration.ORIENTATION_PORTRAIT){
          mLastParticipantView.getLayoutParams().height = (int) (metrics.heightPixels / 1.35);
          mLastParticipantView.requestLayout();;
        }else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
          mLastParticipantView.getLayoutParams().height = (int) (metrics.heightPixels / 1.6);
          mLastParticipantView.requestLayout();;
        }

      }

    }

    @Override
    protected void onStreamDropped(Stream stream) {
      Participant p = mParticipantStream.get(stream);
      if (p != null) {

        mParticipants.remove(p);
        mParticipantStream.remove(stream);
        mParticipantConnection.remove(stream.getConnection().getConnectionId());

        mLastParticipant = null;

        int index = mParticipantsViewContainer.indexOfChild(p.getView());
        if (index != -1) {
          mParticipantsViewContainer.removeViewAt(index);
        } else {
          mLastParticipantView.removeView(p.getView());
          if (mParticipants.size() > 0) {
            //add last participant to this view
            Participant currentLast = mParticipants.get(mParticipants.size() - 1);
            mParticipantsViewContainer.removeView(currentLast.getView());
            mLastParticipantView.addView(currentLast.getView(), mActivity.getMainLayoutParams());
          }
        }
      }
      if(mParticipants.size()==0)
        mActivity.getInfoMessage().setVisibility(View.VISIBLE);
      if(mParticipants.size()==1){
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mLastParticipantView.getLayoutParams().height=metrics.heightPixels;
        mLastParticipantView.requestLayout();
      }

    }


    @Override
    protected void onError(OpentokError error) {
      super.onError(error);
      Toast.makeText(this.mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
      mActivity.getmProgressDialog().dismiss();
    }

    public void loadSubscriberView() {
      //stop loading spinning
      if (mActivity.getLoadingSub().getVisibility() == View.VISIBLE) {
        mActivity.getLoadingSub().setVisibility(View.GONE);
        mActivity.getInfoMessage().setVisibility(View.GONE);
        this.mLastParticipantView.addView(mLastParticipant.getView());
      }

    }

    private void swapSubPriority(View view) {
      int index = this.mParticipantsViewContainer.indexOfChild(view);

      //the last participant view will go to the index
      this.mParticipantsViewContainer.removeView(view);
      this.mLastParticipantView.removeView(mLastParticipant.getView());

      //update lastParticipant view
      LinearLayout.LayoutParams lp = mActivity.getMainLayoutParams();
      Participant currentSelected = mParticipantStream.get(view.getTag());
      currentSelected.setPreferredResolution(Participant.High_VIDEO_RESOLUTION);
      currentSelected.setPreferredFrameRate(Participant.MAX_FPS);
    //  currentSelected.getView().setOnClickListener(clickLastParticipantListener);
      currentSelected.getView().setOnLongClickListener(null);
      this.mLastParticipantView.addView(currentSelected.getView(), lp);

      lp = mActivity.getQVGALayoutParams();
    //  mLastParticipant.getView().setOnClickListener(clickListener);
      mLastParticipant.getView().setOnLongClickListener(longClickListener);
      mLastParticipant.setPreferredResolution(Participant.High_VIDEO_RESOLUTION);
      mLastParticipant.setPreferredFrameRate(Participant.MAX_FPS);
      this.mParticipantsViewContainer.addView(mLastParticipant.getView(), index, lp);


      mLastParticipant = currentSelected;

    }

    View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
      public boolean onLongClick(View view) {
        if (!view.equals(mLastParticipantView)) {
          swapSubPriority(view);
        }

        return true;
      }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        Participant participant = mParticipantStream.get(view.getTag());

        boolean enableAudioOnly = participant.getSubscribeToVideo();

        if (enableAudioOnly) {
          participant.setSubscribeToVideo(false);
        } else {
          participant.setSubscribeToVideo(true);
        }
        int index = mParticipantsViewContainer.indexOfChild(participant.getView());
        if (index == -1) {
          index = view.getId();
        }
        mActivity.setAudioOnlyViewListPartcipants(enableAudioOnly, participant, index, this);

      }
    };


    private View.OnClickListener clickLastParticipantListener = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean enableAudioOnly = mLastParticipant.getSubscribeToVideo();
        if (enableAudioOnly) {
          mLastParticipant.setSubscribeToVideo(false);
        } else {
          mLastParticipant.setSubscribeToVideo(true);
        }
        mActivity.setAudioOnlyViewLastParticipant(enableAudioOnly, mLastParticipant, this);
      }
    };



}

