package  com.fitbase.TokBox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.fitbasetrainer.MainActivity;
import com.fitbasetrainer.R;
/**
 * Created by Anshul Nigam .
 *  
 */
public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder> {
    private List<Subscriber> mParticipantsList = new ArrayList<Subscriber>();
    private HashMap<Stream, Subscriber> mSubscriberStreams = new HashMap<Stream, Subscriber>();
    private final OpenTokActivity mActivity;
    RelativeLayout temImage=null,temImg2=null;;

    public ParticipantsAdapter(Context context, List<Subscriber> participantsList) throws Exception {
        if (participantsList == null) {
            throw new Exception("ParticipantsList cannot be null");
        }
        this.mParticipantsList = participantsList;
        this.mActivity= (OpenTokActivity) context;

    }

    @Override
    public ParticipantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);

        return new ParticipantViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.grid_item;
    }

    @Override
    public void onBindViewHolder(ParticipantViewHolder holder1, int position) {
        final ParticipantViewHolder holder=holder1;

        Subscriber participant = mParticipantsList.get(position);

        //add id
        holder.id = participant.getStream().getStreamId();
          LinearLayout.LayoutParams params = getQVGALayoutParams();
        // holder.container.setTag(participant.getStream());
        holder.mainItemView.setTag(participant.getStream());
        if(position==Util.selectedposition) {
            holder.mainItemView.setBackgroundResource(R.drawable.recycler_bacg);
            temImg2= holder.mainItemView;
            holder.userName.setText( mParticipantsList.get(position).getStream().getName());
            holder.userName.setTextColor(Color.BLACK);
            holder.audiOnlyView.setVisibility(View.VISIBLE);
            holder.userName.setTextColor(Color.BLACK);
            ViewGroup parentUsr=(ViewGroup)holder.audiOnlyView.getParent();
            if(parentUsr!=null){
                parentUsr.removeView(holder.audiOnlyView);
            }
            holder.container.addView( holder.audiOnlyView,params);

            ViewGroup viewGroup =  (ViewGroup) mActivity.getmLayoutManager().findViewByPosition(position);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            ViewGroup parent = (ViewGroup) participant.getView().getParent();
            if (parent != null) {
                parent.removeView(participant.getView());
            }
            mActivity.getGalleryMainView().addView(participant.getView(),layoutParams);
             ((GLSurfaceView) participant.getView()).setZOrderOnTop(false);
            mActivity.getGalleryMainView().setTag(position);
            mActivity.getGalleryaudio().setVisibility(View.VISIBLE);
            mActivity.getGalleryaudio().setOnClickListener(mActivity.muteSubscriberAudio);
            mActivity.getGalleryaudio().setImageResource(participant.getSubscribeToAudio() ? R.drawable.audio:R.drawable.no_audio);
            mActivity.getGalleryaudio().bringToFront();
        }else
            holder.mainItemView.setBackgroundResource(0);
        if(temImage!=null)
            temImage.setBackgroundResource(0);
        holder.mainItemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // userPosition=pos;
                if(temImage!=null)
                    temImage.setBackgroundResource(0);
                     holder.mainItemView.setBackgroundResource(R.drawable.recycler_bacg);
                     temImage =  holder.mainItemView;
                if(temImg2!=null){
                    temImg2.setBackgroundResource(0);
                    temImg2=null;
                }
            }
        });

       if (!participant.getStream().hasVideo()) {
           holder.userName.setText( mParticipantsList.get(position).getStream().getName());
           holder.userName.setTextColor(Color.BLACK);
           holder.audiOnlyView.setVisibility(View.VISIBLE);
           holder.userName.setTextColor(Color.BLACK);
           ViewGroup parentUsr=(ViewGroup)holder.audiOnlyView.getParent();
           if(parentUsr!=null){
               parentUsr.removeView(holder.audiOnlyView);
           }
            ViewGroup parent = (ViewGroup) participant.getView().getParent();
           if (parent != null) {
               parent.removeView(participant.getView());
           }
           holder.container.addView( holder.audiOnlyView,params);
        } else if(position!=Util.selectedposition){
            holder.audiOnlyView.setVisibility(View.GONE);
            if (participant.getView() != null) {
                ViewGroup parent = (ViewGroup) participant.getView().getParent();
                if (parent != null) {
                    parent.removeView(participant.getView());
                }
                holder.container.addView(participant.getView(),params);
                ((GLSurfaceView) participant.getView()).setZOrderOnTop(true);


            }
        }

    }
   private  LinearLayout.LayoutParams getQVGALayoutParams(){
        DisplayMetrics metrics=mActivity.getDisplay();
        Configuration config=mActivity.getResources().getConfiguration();
        if(config.orientation==Configuration.ORIENTATION_PORTRAIT)
            return new LinearLayout.LayoutParams(metrics.widthPixels/5, metrics.widthPixels/5);
        else
            return new LinearLayout.LayoutParams(metrics.widthPixels/9, metrics.widthPixels/9);
    }

    @Override
    public int getItemCount() {
        return (null != mParticipantsList ? mParticipantsList.size() : 0);
    }

     class ParticipantViewHolder extends RecyclerView.ViewHolder {
       protected RelativeLayout audiOnlyView;
        protected RelativeLayout container;
       protected RelativeLayout controls;
        protected RelativeLayout mainItemView;
         protected  TextView userName;
        protected String id;


        public ParticipantViewHolder(View view) {
            super(view);
           this.audiOnlyView = (RelativeLayout) view.findViewById(R.id.user_name);
            this.container = (RelativeLayout) view.findViewById(R.id.itemView);
            this.userName=(TextView)view.findViewById(R.id.avatar);
          // this.controls = (RelativeLayout) view.findViewById(R.id.remoteControls);
            this.mainItemView=(RelativeLayout)view.findViewById(R.id.itemmain) ;
           mActivity.getmParticipantsGrid().addOnItemTouchListener(new RecyclerTouchListener(
                    mActivity, (ArrayList<Subscriber>) mParticipantsList, new RecyclerTouchListener.ClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View view, int position) {
                       int id=Integer.parseInt(mActivity.getGalleryMainView().getTag().toString());
                    if(id==position){
                        return;
                    }

                    ParticipantViewHolder.this.userName.setText( mParticipantsList.get(position).getStream().getName());
                    ParticipantViewHolder.this.userName.setTextColor(Color.BLACK);
                    ParticipantViewHolder.this.audiOnlyView.setVisibility(View.VISIBLE);
                    LinearLayout.LayoutParams params = getQVGALayoutParams();

                    ViewGroup parent = (ViewGroup) mParticipantsList.get(position).getView().getParent();
                    ViewGroup Lastparent = (ViewGroup) mParticipantsList.get(id).getView().getParent();
                    ViewGroup galleryParent= (ViewGroup) mActivity.getmLayoutManager().findViewByPosition(id);
                    ViewGroup parentUsr=(ViewGroup)ParticipantViewHolder.this.audiOnlyView.getParent();
                    if(parentUsr!=null){
                        parentUsr.removeView(ParticipantViewHolder.this.audiOnlyView);
                    }
                    if (parent != null) {
                        parent.removeView(mParticipantsList.get(position).getView());
                        parent.addView( ParticipantViewHolder.this.audiOnlyView,params);
                    }
                    mActivity.getGalleryMainView().removeView(mParticipantsList.get(id).getView());
                    if (galleryParent != null) {
                        Lastparent.removeView(mParticipantsList.get(id).getView());
                        galleryParent.addView( mParticipantsList.get(id).getView(),params);
                       // scroll hideing element ((GLSurfaceView) mParticipantsList.get(id).getView()).setZOrderOnTop(true);
                        galleryParent.requestLayout();
                    }
                    mActivity.getGalleryMainView().removeAllViews();
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
                    Subscriber subscriber=mParticipantsList.get(position);
                    boolean isMuted=subscriber.getSubscribeToAudio();
                    subscriber.setPreferredResolution(Participant.High_VIDEO_RESOLUTION);
                    subscriber.setPreferredFrameRate(Participant.MAX_FPS);
                    subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
                    mActivity.getGalleryMainView().addView(subscriber.getView(),layoutParams);
                    ((GLSurfaceView) subscriber.getView()).setZOrderOnTop(false);
                    mActivity.getGalleryMainView().setTag(position);
                    mActivity.getGalleryMainView().requestLayout();
                    mActivity.getGalleryaudio().setVisibility(View.VISIBLE);
                    mActivity.getGalleryaudio().setOnClickListener(mActivity.muteSubscriberAudio);
                    mActivity.getGalleryaudio().setImageResource(isMuted ? R.drawable.audio:R.drawable.no_audio);
                    mActivity.getGalleryaudio().bringToFront();
                    mActivity.getLlcontrols().bringToFront();
                }
            }));



        }


    }


}