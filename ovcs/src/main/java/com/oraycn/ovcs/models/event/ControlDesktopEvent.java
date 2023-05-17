package com.oraycn.ovcs.models.event;

public class ControlDesktopEvent {
    private String targetUid;
    private Boolean isControl;

    public String getTargetUid() {
        return targetUid;
    }
    public boolean getIsControl()
    {
        return this.isControl;
    }

    public ControlDesktopEvent(String uid, Boolean isControl)
    {
        this.targetUid=uid;
        this.isControl = isControl;
    }
}
