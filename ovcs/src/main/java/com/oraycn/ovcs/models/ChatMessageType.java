package com.oraycn.ovcs.models;

public enum ChatMessageType
{
    None(0),
    TextEmotion (1),
    Image (2),
    File (3),
    Card (4),
    Snap (5),
    AudioMessage (6),
    Media (7),
    Referenced (8);

    private int type;

    ChatMessageType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static ChatMessageType getChatMessageType(int code) {
        for (ChatMessageType type : ChatMessageType.values()) {
            if (type.getType() == code) {
                return type;
            }
        }
        return null;
    }
}
