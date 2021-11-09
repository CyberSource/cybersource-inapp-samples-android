package com.cybersource.webcheckout.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by fzubair on 10/22/2015.
 */

public class MessageSignatureResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public MessageSignatureResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}