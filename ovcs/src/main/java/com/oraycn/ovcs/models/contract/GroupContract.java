package com.oraycn.ovcs.models.contract;

import com.oraycn.omcs.utils.BufferUtils;

import io.netty.buffer.ByteBuf;

public class GroupContract {
    public String GroupID;

    public GroupContract(){}

    public GroupContract(String groupID)
    {
        this.GroupID=groupID;
    }
    public byte[] serialize() throws Exception {
        ByteBuf body = BufferUtils.newBuffer();
        byte[] bGroupID = GroupID.getBytes("utf-8");
        int bodyLen = 4 + 4 + bGroupID.length ;
        body.writeInt(bodyLen);
        body.writeInt(bGroupID.length);
        body.writeBytes(bGroupID);
        byte[] result = new byte[body.readableBytes()];
        System.arraycopy(body.array(),0,result,0,result.length);
        return result;
    }
}
