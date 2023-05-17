package com.oraycn.ovcs.models.contract;

import com.oraycn.omcs.utils.BufferUtils;

import io.netty.buffer.ByteBuf;

public class RecruitOrFireContract {
    public RecruitOrFireContract() { }
    public RecruitOrFireContract(String _groupID, String _memberID)
    {
        this.GroupID = _groupID;
        this.MemberID = _memberID;
    }

    public String GroupID;
    public String MemberID;

    public byte[] serialize() throws Exception {
        ByteBuf body = BufferUtils.newBuffer();
        byte[] bGroupID = GroupID.getBytes("utf-8");
        byte[] bMemberID = MemberID.getBytes("utf-8");
        int bodyLen = 4 + 4 + bGroupID.length + 4 + bMemberID.length;
        body.writeInt(bodyLen);
        body.writeInt(bGroupID.length);
        body.writeBytes(bGroupID);
        body.writeInt(bMemberID.length);
        body.writeBytes(bMemberID);
        byte[] result = new byte[body.readableBytes()];
        System.arraycopy(body.array(),0,result,0,result.length);
        return result;
    }
}
