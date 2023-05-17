package com.oraycn.ovcs.models.event;

import com.oraycn.ovcs.models.ChatMessageRecord;

public class SystemChatEvent extends ChatEvent{

    //系统消息
    public SystemChatEvent(String senderID, String msg) {
        this.groupID = senderID;
        ChatMessageRecord record = new ChatMessageRecord();
        record.ContentStr = msg;
        this.chatMessageRecord = record;
        this.isSystemMsg = true;
        this.isGroupChat = true;
    }
}
