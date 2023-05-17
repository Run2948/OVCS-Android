package com.oraycn.ovcs.models;

/**
 * Created by ZN on 2017/3/6.
 */

public enum CommunicateMediaType {
    Video(0),
    Audio(1),
    RemoteHelp(2),
    RemoteControl(3),
    RemoteDisk(4),
    Online(5),
    Offline(6);
    private int type;

    CommunicateMediaType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static CommunicateMediaType getCommunicateMediaType(int code) {
        for (CommunicateMediaType type : CommunicateMediaType.values()) {
            if (type.getType() == code) {
                return type;
            }
        }
        return null;
    }
}
