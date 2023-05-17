package com.oraycn.ovcs.models.contract;

import com.oraycn.esframework.common.ActionTypeOnChannelIsBusy;
import com.oraycn.omcs.utils.BufferUtils;
import com.oraycn.omcs.utils.SerializeUtils;

import com.oraycn.ovcs.utils.SerializeHelper;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

public class BroadcastContract {

    public BroadcastContract() { }
    public BroadcastContract(String _broadcasterID, String _groupID, int infoType ,byte[] info ,ActionTypeOnChannelIsBusy action)
    {
        this.BroadcasterID = _broadcasterID;
        this.GroupID = _groupID;
        this.Content = info;
        this.InformationType = infoType;
        this.ActionTypeOnChannelIsBusy = action;
    }

    public String BroadcasterID;
    public String GroupID;
    public int InformationType=0;
    public byte[] Content;
    public com.oraycn.esframework.common.ActionTypeOnChannelIsBusy ActionTypeOnChannelIsBusy= com.oraycn.esframework.common.ActionTypeOnChannelIsBusy.CONTINUE;

    public byte[] serialize() throws Exception {
        ByteBuf body = BufferUtils.newBuffer();
        byte[] bBroadcasterID = BroadcasterID.getBytes("utf-8");
        byte[] bGroupID = GroupID.getBytes("utf-8");
        int bodyLen = 4 + +4 + 4 + bGroupID.length + 4 + bBroadcasterID.length + 4 + 4;
        body.writeInt(bodyLen);
        body.writeInt(this.ActionTypeOnChannelIsBusy.getValue());
        body.writeInt(bBroadcasterID.length);
        body.writeBytes(bBroadcasterID);
        boolean contentisNull = Content == null || Content.length == 0;
        if (contentisNull) {
            body.writeInt(-1);
        } else {
            body.writeInt(Content.length);
            body.writeBytes(Content);
        }
        body.writeInt(bGroupID.length);
        body.writeBytes(bGroupID);
        body.writeInt(InformationType);
        byte[] result = new byte[body.readableBytes()];
        System.arraycopy(body.array(), 0, result, 0, result.length);
        return result;
    }

    public void deserialize(byte[] info) throws IOException
    {
        ByteBuf buffer = SerializeHelper.wrappedBuffer(info);
        buffer.readInt();
        this.ActionTypeOnChannelIsBusy = ActionTypeOnChannelIsBusy.getActionTypeByCode(buffer.readInt()) ;
        this.BroadcasterID = SerializeUtils.readStrIntLen(buffer);
        int contentLenght = buffer.readInt();
        if(contentLenght>0)
        {
            Content = new byte[contentLenght];
            buffer.readBytes(Content);
        }
        this.GroupID=SerializeUtils.readStrIntLen(buffer);
        this.InformationType = buffer.readInt();
    }
}
