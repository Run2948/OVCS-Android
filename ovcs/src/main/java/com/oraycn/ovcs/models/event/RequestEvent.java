package com.oraycn.ovcs.models.event;


import com.oraycn.ovcs.models.CommunicateMediaType;

import java.io.Serializable;

/**
 * Created by ZN on 2017/3/7.
 */

public class RequestEvent implements Serializable {
    public String requestID;

    public CommunicateMediaType mediaType;

    public int messageType;

    public RequestEvent(String requestID,  int messageType, CommunicateMediaType mediaType) {
        this.requestID = requestID;
        this.mediaType = mediaType;
        this.messageType = messageType;

    }

    public String getLoginID() {
        return  this.requestID;
    }
}
