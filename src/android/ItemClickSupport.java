package com.fitbase.TokBox;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.ionicframework.trainermobile381261.R;

/**
 * Created by Priya on 9/11/2018.
 */

public class ItemClickSupport  {
  private final RecyclerView mRecyclerView;
  private OnItemClickListener mOnItemClickListener;
  private com.fitbase.TokBox.OnDoubleClickListener mOnDoubleClickListener = new com.fitbase.TokBox.OnDoubleClickListener() {
    @Override
    public void onDoubleClick(View v) {
      if (mOnItemClickListener != null) {
        RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
        mOnItemClickListener.onItemDoubleClicked(mRecyclerView, holder.getAdapterPosition(), v);
      }
    }

    @Override
    public void onSingleClick(View v) {
      if (mOnItemClickListener != null) {
        RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
        mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
      }
    }
  };

  private RecyclerView.OnChildAttachStateChangeListener mAttachListener
    = new RecyclerView.OnChildAttachStateChangeListener() {
    @Override
    public void onChildViewAttachedToWindow(View view) {
      if (mOnItemClickListener != null) {
        view.setOnClickListener(mOnDoubleClickListener);
      }
    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {

    }
  };

  private ItemClickSupport(RecyclerView recyclerView) {
    mRecyclerView = recyclerView;
    mRecyclerView.setTag(R.string.uniqueId, this);
    mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
  }

  public static ItemClickSupport addTo(RecyclerView view) {
    ItemClickSupport support = (ItemClickSupport) view.getTag(R.string.uniqueId);
    if (support == null) {
      support = new ItemClickSupport(view);
    }
    return support;
  }

  public static ItemClickSupport removeFrom(RecyclerView view) {
    ItemClickSupport support = (ItemClickSupport) view.getTag(R.string.uniqueId);
    if (support != null) {
      support.detach(view);
    }
    return support;
  }

  public ItemClickSupport setOnItemClickListener(OnItemClickListener listener) {
    mOnItemClickListener = listener;
    return this;
  }

  private void detach(RecyclerView view) {
    view.removeOnChildAttachStateChangeListener(mAttachListener);
    view.setTag(R.string.uniqueId, null);
  }

  public interface OnItemClickListener {
    void onItemClicked(RecyclerView recyclerView, int position, View v);
    void onItemDoubleClicked(RecyclerView recyclerView, int position, View v);
  }
}

