package com.oraycn.ovcs.models;

public enum BroadcastChannelMode {
    //
    // 摘要:
    //     如果和某个用户之间的P2P通道存在，则到该用户的广播经过P2P通道传送；否则，经过服务器中转。
    P2PChannelFirst(0),
    //
    // 摘要:
    //     全部经过服务器中转，不使用任何P2P通道。
    AllTransferByServer(1),
    //
    // 摘要:
    //     仅仅使用P2P通道。如果与某个用户之间的P2P通道不存在，则不发送广播到该用户。
    AllByP2PChannel(2);

    private int type;

    BroadcastChannelMode(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static BroadcastChannelMode getBroadcastChannelMode(int code) {
        for (BroadcastChannelMode type : BroadcastChannelMode.values()) {
            if (type.getType() == code) {
                return type;
            }
        }
        return null;
    }
}
