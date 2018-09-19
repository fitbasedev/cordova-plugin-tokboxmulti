package com.fitbase.TokBox;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class ParticipantsListAdapter extends RecyclerView.Adapter<ParticipantsListAdapter.ParticipantViewHolder> {

  private List<Participant> mParticipantsList = new ArrayList<Participant>();
  private ParticipantListAdapterListener mListener;
  private Context context;

  public interface ParticipantListAdapterListener {
    void muteFromListOfParticipants(View view);
  }

  public ParticipantsListAdapter(Context context, List<Participant> participantsList, ParticipantListAdapterListener listener) throws Exception {
    if (participantsList == null) {
      throw new Exception("ParticipantsList cannot be null");
    }
    this.mParticipantsList = participantsList;
    this.mListener = listener;
    this.context=context;
  }



  @Override
  public ParticipantViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participants_list, parent, false);
    return new ParticipantViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ParticipantViewHolder holder, int position) {
    Participant participant=mParticipantsList.get(position);
    holder.muteMic.setImageResource(participant.getStatus().getSubscribeToAudio() ?  R.drawable.mic_green_icon : R.drawable.muted_mic_red_icon);
    holder.participantsname.setText(participant.getStatus().getStream().getName());
    holder.muteMic.setId(position);
  }

  @Override
  public int getItemCount() {
    return (null != mParticipantsList ? mParticipantsList.size() : 0);
  }

  @Override
  public int getItemViewType(int position) {
    return R.layout.participants_list;
  }

  public class ParticipantViewHolder extends RecyclerView.ViewHolder {

    protected TextView participantsname;
    protected ImageView muteMic;
    protected RelativeLayout container;

    public ParticipantViewHolder(View itemView) {
      super(itemView);
      this.participantsname=(TextView)itemView.findViewById(R.id.participant_name);
      this.muteMic=(ImageView)itemView.findViewById(R.id.mutemic);

      this.muteMic.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mListener.muteFromListOfParticipants(view);
        }
      });
    }
  }
}

