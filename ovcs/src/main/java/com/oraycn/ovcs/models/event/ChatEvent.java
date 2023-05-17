package com.oraycn.ovcs.models.event;

import com.oraycn.ovcs.models.ChatMessageRecord;

import java.util.Date;

public class ChatEvent {
    public ChatEvent(){}

    public ChatEvent(String groupID, String sourceID, ChatMessageRecord msg, boolean isGroupChat) {
        this.chatMessageRecord = msg;
        this.groupID = groupID;
        this.isGroupChat = isGroupChat;
        this.sourceID = sourceID;
    }

    //组id（如果不是组消息，sourceID）接收者ID
    public String groupID;
    public ChatMessageRecord chatMessageRecord;
    public boolean isGroupChat;
    //发送者id
    public String sourceID;
    public boolean isSystemMsg=false;

    public Date occurrenceTime = new Date();//消息发生时间
}
