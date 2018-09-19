package com.fitbase.TokBox;

import android.util.Size;

import com.opentok.android.Subscriber;

/**
 * Created by Priya on 9/10/2018.
 */

public class Participant  {
  public enum Type {
    LOCAL,
    REMOTE
  }

  public Type mType;
  public String id = null;
  public Subscriber mStatus;
  private Size mContainer;

  public Participant(Type type, Subscriber status, Size containerSize) {
    this.mType = type;
    this.mStatus = status;
    this.mContainer = containerSize;
  }

  public Participant(Type type, Subscriber status, Size containerSize, String id) {
    this.mType = type;
    this.mStatus = status;
    this.mContainer = containerSize;
    this.id = id;
  }

  public Size getContainer() {
    return mContainer;
  }

  public Subscriber getStatus() {
    return mStatus;
  }

  public String getId() {
    return id;
  }

  public Type getType() {
    return mType;
  }

  public void setContainer(Size mContainer) {
    this.mContainer = mContainer;
  }
}





