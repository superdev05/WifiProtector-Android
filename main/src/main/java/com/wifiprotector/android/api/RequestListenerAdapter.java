package com.wifiprotector.android.api;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.wifiprotector.android.log.SecuredLogManager;

import timber.log.Timber;

/**
 * RequestListener which does not force you to implement onRequestFailure. By default it prints the exception.
 *
 * @param <RESULT>
 */
public abstract class RequestListenerAdapter<RESULT> implements RequestListener<RESULT> {

    public void onRequestFailure(SpiceException spiceException) {
        SecuredLogManager.getLogManager().writeLog(6, "WFProctector", spiceException.getMessage());
    }

    public abstract void onRequestSuccess(RESULT result);
}
