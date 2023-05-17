package com.oraycn.ovcs.models;

import java.util.Date;
import java.util.UUID;

public class ChatMessageRecord {

    public ChatMessageRecord(){}
    public ChatMessageRecord(String speakerID, String listenerID,ChatMessageType type,String contentStr) {
        this.Guid = UUID.randomUUID().toString();
        this.MessageTime = new Date();
        this.SpeakerID = speakerID;
        this.ListenerID = listenerID;
        this.ChatMessageType = type;
        this.ContentStr = contentStr;
    }

    /**
     * 唯一GUID
     */
    public String Guid ;

    /**
     * 发送消息时间
     */
    public Date MessageTime ;

    /**
     * 说话者ID
     */
    public String SpeakerID ;

    /**
     * 收听者ID
     */
    public String ListenerID ;

    /**
     * 聊天类型 （暂仅支持文本、表情）
     */
    public ChatMessageType ChatMessageType ;

    /**
     * 发送内容 （表情用 [012] 表示，其中 中间三位数代表表情的索引）
     */
    public String ContentStr ;
}


