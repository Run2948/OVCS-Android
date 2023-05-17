package com.oraycn.ovcs.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.oraycn.esbasic.helpers.StringHelper;
import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.utils.FileUtils;
import com.oraycn.ovcs.utils.GlobalConfig;


import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class ChatInfo extends ChatMessageRecord {

    private String imageUrl;

    //系统消息
    public static ChatInfo getSystemInfo(String audienceID, String message)
    {
        ChatInfo info = new ChatInfo();
        info.ContentStr=message;
        info.SpeakerID = "";
        info.MessageTime =new Date();
        info.isSystemMessage=true;
        return info;
    }

    public ChatInfo(){
        super();
    }
    public ChatInfo(ChatMessageRecord record) {
        this.Guid = record.Guid;
        this.SpeakerID = record.SpeakerID;
        this.ListenerID = record.ListenerID;
        this.ContentStr = record.ContentStr;
        this.MessageTime = record.MessageTime;
        this.ChatMessageType = record.ChatMessageType;
    }

    public final static int chat_img_item = 0;
    public final static int chat_lv_item = 1;

    public Date getTime() {
        return this.MessageTime;
    }

    public String getSpeakerID() {
        if(StringHelper.isNullOrEmpty(SpeakerID))
        {
            return "";
        }
        return SpeakerID;
    }

    public void setSpeakerID(String speakerID) {
        this.SpeakerID = speakerID;
    }

    public String getContent() {
        return this.ContentStr;
    }

    /**
     * 0 是收到的消息，1是发送的消息
     * */
    public boolean getIsSender() {
        return this.SpeakerID.equals(OrayApplication.getInstance().getCurUserID());
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String _imageUrl) {
        this.imageUrl = _imageUrl;
    }

    public boolean isSystemMessage=false;
    public boolean isGroupChat = false;

}
