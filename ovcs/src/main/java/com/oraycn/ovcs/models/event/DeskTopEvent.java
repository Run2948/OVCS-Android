package com.oraycn.ovcs.models.event;

public class DeskTopEvent {
    private String targetUid;
    private Boolean isShare;

    public String getTargetUid() {
        return targetUid;
    }
    public boolean getIsShare()
    {
        return this.isShare;
    }

    public DeskTopEvent(String uid, Boolean isShare)
    {
        this.targetUid=uid;
        this.isShare = isShare;
    }
}


