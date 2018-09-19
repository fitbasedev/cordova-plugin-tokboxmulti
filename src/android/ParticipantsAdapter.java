package com.fitbase.TokBox;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ionicframework.trainermobile381261.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Priya on 9/10/2018.
 */

public class ParticipantsAdapter  extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder> {
  private List<com.fitbase.TokBox.Participant> mParticipantsList = new ArrayList<com.fitbase.TokBox.Participant>();
  private ParticipantAdapterListener mListener;
  private com.fitbase.TokBox.LiveVideoActivity context;



  public interface ParticipantAdapterListener {
    void mediaControlChanged(String remoteId);
    void showViewOnBigScreen(int pos);
    void onTapshowHideControls();
  }

  public ParticipantsAdapter(Context context, List<com.fitbase.TokBox.Participant> participantsList, ParticipantAdapterListener listener) throws Exception {
    if (participantsList == null) {
      throw new Exception("ParticipantsList cannot be null");
    }
    this.mParticipantsList = participantsList;
    this.mListener = listener;
    this.context= (com.fitbase.TokBox.LiveVideoActivity) context;
  }

  @Override
  public ParticipantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

    return new ParticipantViewHolder(view);
  }

  @Override
  public int getItemViewType(int position) {
    return R.layout.participants_grid_item;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void onBindViewHolder(ParticipantViewHolder holder, int position) {
    com.fitbase.TokBox.Participant participant = mParticipantsList.get(position);
    holder.container.removeAllViews();
    boolean isgalleryViewPresent= ((com.fitbase.TokBox.LiveVideoActivity)context).getmLayoutManager().getOrientation()== GridLayoutManager.HORIZONTAL ? true : false;

    //add id
    holder.id = participant.getId();
    holder.type = participant.getType();
    holder.listener = mListener;
    holder.muteAudioIcon.getChildAt(0).setId(position);
    holder.audioIcon.setImageResource(participant.getStatus().getSubscribeToAudio() ? R.drawable.mic_green_icon : R.drawable.muted_mic_red_icon);
    if(!isgalleryViewPresent) {
      holder.muteIconVideo.getLayoutParams().height = context.getResources().getDisplayMetrics().widthPixels / 5;
      holder.muteIconVideo.getLayoutParams().width = context.getResources().getDisplayMetrics().widthPixels / 5;

    }else {
      holder.muteIconVideo.getLayoutParams().height=participant.getContainer().getWidth()/5;
      holder.muteIconVideo.getLayoutParams().width=participant.getContainer().getWidth()/5;

      holder.muteAudioIcon.getLayoutParams().width= (int) (participant.getContainer().getWidth()/2.3);
      holder.muteAudioIcon.getLayoutParams().height= (int) (participant.getContainer().getWidth()/2.3);

    }

    RelativeLayout.LayoutParams params = null; // (width, height)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      params = new RelativeLayout.LayoutParams(participant.getContainer().getWidth(), participant.getContainer().getHeight());
      if(isgalleryViewPresent){ params.setMargins(10,10,10,10);holder.muteAudioIcon.setVisibility(View.VISIBLE);holder.border.setVisibility(View.VISIBLE);}else{
        params.setMargins(0,0,0,0);holder.muteAudioIcon.setVisibility(View.GONE);holder.border.setVisibility(View.GONE);

      }
    }
    holder.container.setLayoutParams(params);


    if(position== com.fitbase.TokBox.Util.selectedposition && isgalleryViewPresent){
      context.getMainViewGallery().setTag(position);
      ViewGroup parent = (ViewGroup) participant.getStatus().getView().getParent();
      if (parent != null) {
        parent.removeView(participant.getStatus().getView());
      }
      context.getMainViewGallery().addView(participant.getStatus().getView());
      context.getMainViewGallery().setVisibility(View.VISIBLE);
      params = new RelativeLayout.LayoutParams(participant.getContainer().getWidth(), participant.getContainer().getHeight());
      ((GLSurfaceView) participant.getStatus().getView()).setZOrderOnTop(false);
      holder.audiOnlyView.setBackgroundResource(  R.drawable.gradient_light_audionly);
      holder.audiOnlyView.setVisibility(View.VISIBLE);
      holder.name.setText(  participant.getStatus().getStream().getName()  );
      holder.name.setTextColor(context.getResources().getColor(R.color.colorWhite));
      holder.name.setVisibility(  View.VISIBLE);
      holder.muteIconVideo.setVisibility(View.GONE);
      holder.usernamesm.setVisibility( View.GONE );
      holder.container.addView(holder.audiOnlyView,params);
      holder.container.bringToFront();
      holder.muteAudioIcon.bringToFront();
    }
    if (!participant.getStatus().getStream().hasVideo() || (participant.getType().equals(com.fitbase.TokBox.Participant.Type.REMOTE) && !participant.getStatus(). getSubscribeToVideo())) {
      holder.container.removeAllViews();
      holder.usernamesm.setVisibility(isgalleryViewPresent ?   View.VISIBLE :  View.GONE );
      holder.muteIconVideo.setVisibility(View.VISIBLE);
      holder.audiOnlyView.setBackgroundResource(  R.drawable.gradient_grey_audionly);
      holder.audiOnlyView.setVisibility(View.VISIBLE);
      ViewGroup parent = (ViewGroup) participant.getStatus().getView().getParent();
      if (parent != null) {
        parent.removeView(participant.getStatus().getView());
      }if(isgalleryViewPresent){
        params = new RelativeLayout.LayoutParams(participant.getContainer().getWidth(), participant.getContainer().getHeight());
        holder.container.addView(holder.audiOnlyView,params);
      }else{
        holder.container.addView(holder.audiOnlyView, params);
      }
      holder.name.setText(isgalleryViewPresent ? participant.getStatus().getStream().getName() : participant.getStatus().getStream().getName()+" muted video");
      holder.usernamesm.setText(isgalleryViewPresent ? participant.getStatus().getStream().getName() : participant.getStatus().getStream().getName()+" muted video");
      holder.name.setVisibility( isgalleryViewPresent ? View.GONE: View.VISIBLE);
      holder.name.setTextColor(context.getResources().getColor(R.color.username));
      holder.container.bringToFront();

    } else if(position!= com.fitbase.TokBox.Util.selectedposition || !isgalleryViewPresent) {
      //  holder.audiOnlyView.setVisibility(View.GONE);
      if (participant.getStatus().getView() != null) {
        ViewGroup parent = (ViewGroup) participant.getStatus().getView().getParent();
        if (parent != null) {
          parent.removeView(participant.getStatus().getView());
        }
        holder.container.addView(participant.getStatus().getView());
        ((GLSurfaceView) participant.getStatus().getView()).setZOrderOnTop(isgalleryViewPresent ? true :  false);
        ((GLSurfaceView) participant.getStatus().getView()).setZOrderMediaOverlay(isgalleryViewPresent ? true :  false);

      }
    }
    params = new RelativeLayout.LayoutParams(participant.getContainer().getWidth()+40, participant.getContainer().getHeight()+40);

    //   ((GLSurfaceView) participant.getStatus().getView()).setZOrderOnTop(position!= Util.selectedposition && isgalleryViewPresent ? true : false);
    holder.container.addView(holder.muteAudioIcon);
    holder.container.addView(holder.border,params);
    holder.container.bringToFront();
    holder.muteAudioIcon.bringToFront();

  }

  @Override
  public int getItemCount() {
    return (null != mParticipantsList ? mParticipantsList.size() : 0);
  }

  class ParticipantViewHolder extends RecyclerView.ViewHolder {
    protected RelativeLayout audiOnlyView;
    protected RelativeLayout container;
    //protected RelativeLayout controls;
    protected String id;
    protected com.fitbase.TokBox.Participant.Type type;
    protected ImageView muteIconVideo,audioIcon;
    protected ParticipantAdapterListener listener;
    protected RelativeLayout muteAudioIcon,border;
    protected TextView name,usernamesm;

    public ParticipantViewHolder(View view) {
      super(view);
      this.audiOnlyView = (RelativeLayout) view.findViewById(R.id.audioOnlyView);
      this.container = (RelativeLayout) view.findViewById(R.id.itemView);
      this.muteIconVideo=(ImageView)view.findViewById(R.id.avatar) ;
      this.muteAudioIcon=(RelativeLayout) view.findViewById(R.id.muteIcon);
      this.audioIcon=(ImageView)view.findViewById(R.id.audio_mic) ;
      //  this.controls = (RelativeLayout) view.findViewById(R.id.remoteControls);
      this.name=(TextView)view.findViewById(R.id.userName);
      this.border=(RelativeLayout)view.findViewById(R.id.border);
      this.usernamesm=(TextView)view.findViewById(R.id.usernamesm);

      com.fitbase.TokBox.ItemClickSupport.addTo(context.getmParticipantsGrid())
              .setOnItemClickListener(new com.fitbase.TokBox.ItemClickSupport.OnItemClickListener() {

                @Override
                public void onItemClicked(RecyclerView recyclerView, int newPosition, View v) {
                  boolean isgalleryViewPresent= ((com.fitbase.TokBox.LiveVideoActivity)context).getmLayoutManager().getOrientation()== GridLayoutManager.HORIZONTAL ? true : false;
                  Log.d("ITEM CLICK", "Item single clicked " + newPosition );
                  if(!isgalleryViewPresent) {
                    listener.onTapshowHideControls();

                    return;
                  }
                  int oldPosition=Integer.parseInt(context.getMainViewGallery().getTag().toString());
                  if(oldPosition==newPosition){
                    return;
                  }
                  context.getMainViewGallery().removeAllViews();
                  ViewGroup itemView = (ViewGroup) mParticipantsList.get(newPosition).getStatus().getView().getParent();
                  com.fitbase.TokBox.Participant participant =mParticipantsList.get(newPosition);
                  boolean hasVideo=participant.getStatus().getStream().hasVideo();
                  if (itemView != null) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(itemView.getWidth()+40, itemView.getHeight()+40);
                    itemView.removeView(mParticipantsList.get(newPosition).getStatus().getView());
                    itemView.removeView(ParticipantViewHolder.this.audiOnlyView);
                    ParticipantViewHolder.this.audiOnlyView.setBackgroundResource(  R.drawable.gradient_light_audionly);
                    ParticipantViewHolder.this.audiOnlyView.setVisibility(View.VISIBLE);
                    ParticipantViewHolder.this.name.setText(  mParticipantsList.get(newPosition).getStatus().getStream().getName()  );
                    ParticipantViewHolder.this.name.setTextColor(context.getResources().getColor(R.color.colorWhite));
                    ParticipantViewHolder.this.name.setVisibility(  View.VISIBLE);
                    ParticipantViewHolder.this.muteIconVideo.setVisibility(View.GONE);
                    ParticipantViewHolder.this.usernamesm.setVisibility( View.GONE );
                    ViewGroup audioonly= (ViewGroup) ParticipantViewHolder.this.audiOnlyView.getParent();
                    if(audioonly!=null){
                      audioonly.removeView(ParticipantViewHolder.this.audiOnlyView);
                    }
                    itemView.addView( ParticipantViewHolder.this.audiOnlyView,params);
                  }
                  ((GLSurfaceView) mParticipantsList.get(newPosition).getStatus().getView()).setZOrderOnTop(false);
                  ((GLSurfaceView) mParticipantsList.get(newPosition).getStatus().getView()).setZOrderMediaOverlay(false);
                  if(hasVideo) {
                    context.getMainGalleryAvatar().setVisibility(View.GONE);
                  }else {
                    context.getMainGalleryAvatar().setVisibility(View.VISIBLE);
              /*context.getMaingalleryuserName().setVisibility(View.VISIBLE);
              context.getMaingalleryuserName().setText( participant.getStatus().getStream().getName()+" muted video");*/
                  }
                  context.getMainViewGallery().addView(hasVideo ? mParticipantsList.get(newPosition).getStatus().getView(): context.getMainGalleryAvatar());
                  context.getMainViewGallery().setTag(newPosition);
                  ViewGroup galleryParent= (ViewGroup) context.getmLayoutManager().findViewByPosition(oldPosition);
                  boolean hasVideoInOldView=mParticipantsList.get(oldPosition).getStatus().getStream().hasVideo();
                  if(galleryParent!=null && hasVideoInOldView) {
                    galleryParent.removeView(mParticipantsList.get(oldPosition).getStatus().getView());
                    ((GLSurfaceView)mParticipantsList.get(oldPosition).getStatus().getView()).setZOrderMediaOverlay(true);
                    galleryParent.addView(mParticipantsList.get(oldPosition).getStatus().getView());
                  }
                }

                @Override
                public void onItemDoubleClicked(RecyclerView recyclerView, int position, View v) {
                  boolean isgalleryViewPresent= ((com.fitbase.TokBox.LiveVideoActivity)context).getmLayoutManager().getOrientation()== GridLayoutManager.HORIZONTAL ? true : false;
                  Log.d("ITEM CLICK", "Item double clicked " +position );

                  if(!isgalleryViewPresent){
                    listener.showViewOnBigScreen(position);
                  }

                }
              });

    }
  }
}
