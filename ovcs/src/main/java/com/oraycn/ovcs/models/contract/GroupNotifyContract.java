package com.oraycn.ovcs.models.contract;

import com.oraycn.omcs.utils.BufferUtils;
import com.oraycn.omcs.utils.SerializeUtils;

import com.oraycn.ovcs.utils.SerializeHelper;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

public class GroupNotifyContract {
    public GroupNotifyContract() { }

    public GroupNotifyContract(String grID)
    {
    }

    public GroupNotifyContract(String grID, String userID)
    {
        this.GroupID = grID;
        this.MemberID = userID;
    }

    public String GroupID;
    public String MemberID;

    public byte[] serialize() throws Exception {
        byte[] bGroupID = GroupID.getBytes("utf-8");
        byte[] bMemberID = MemberID.getBytes("utf-8");
        int contractLen = 4 + 4 +bMemberID.length+4 +bGroupID.length;
        ByteBuf body = BufferUtils.newBuffer();
        body.writeInt(contractLen);
        body.writeInt(bGroupID.length);
        body.writeBytes(bGroupID);
        body.writeInt(bMemberID.length);
        body.writeBytes(bMemberID);
        byte[] result = new byte[body.readableBytes()];
        System.arraycopy(body.array(),0,result,0,result.length);
        return result;
    }

    public void deserialize(byte[] info) throws IOException
    {
        ByteBuf buffer = SerializeHelper.wrappedBuffer(info);
        buffer.readInt();
        this.GroupID = SerializeUtils.readStrIntLen(buffer);
        this.MemberID = SerializeUtils.readStrIntLen(buffer);
    }
}
