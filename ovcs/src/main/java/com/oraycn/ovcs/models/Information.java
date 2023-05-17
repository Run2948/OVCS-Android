package com.oraycn.ovcs.models;

public class Information {

    public Information() { }
    public Information(String _sourceID, String _destID, int _infoType, byte[] _content)
    {
        this.SourceID = _sourceID;
        this.DestID = _destID;
        this.InformationType = _infoType;
        this.Content = _content;
    }

    //信息的发送者。可以为UserID或者NetServer.SystemUserID。
    public String SourceID;
    //信息的接收者。可以为UserID或者NetServer.SystemUserID或GroupID（广播消息）。
    public String DestID;
    public int InformationType;
    public byte[] Content;
}
