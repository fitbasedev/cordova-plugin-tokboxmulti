package com.fitbase.TokBox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.VideoUtils;

/**
 * Created by Anshul Nigam .
 *  
 */

public class Participant extends Subscriber {

    private static final String LOGTAG = "Participant";

    protected static final VideoUtils.Size VGA_VIDEO_RESOLUTION = new VideoUtils.Size(640, 480);
    protected static final VideoUtils.Size QVGA_VIDEO_RESOLUTION = new VideoUtils.Size(320, 240);
  protected static final VideoUtils.Size High_VIDEO_RESOLUTION = new VideoUtils.Size(1280,720);
    protected static final int MAX_FPS = 30;
    protected static final int MID_FPS = 15;

    private final String mName;
    private final OpenTokActivity mActivity;
    private Boolean mSubscriberVideoOnly = false;

    public Participant(Context context, Stream stream) {
        super(context, stream);

        mActivity   = (OpenTokActivity) context;
        mName       = "User" + ((int) (Math.random() * 1000));
        setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
    }

    public String getName() {
        return mName;
    }

    public Boolean getSubscriberVideoOnly() {
        return mSubscriberVideoOnly;
    }







}
