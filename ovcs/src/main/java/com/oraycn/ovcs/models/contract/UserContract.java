package com.oraycn.ovcs.models.contract;

import com.oraycn.omcs.utils.SerializeUtils;

import com.oraycn.ovcs.utils.SerializeHelper;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

public class UserContract {
    public String UserID;

    public UserContract() { }
    public UserContract(String _userID)
    {
        this.UserID = _userID;
    }
    public void deserialize(byte[] info) throws IOException
    {
        ByteBuf buffer = SerializeHelper.wrappedBuffer(info);
        buffer.readInt();
        this.UserID = SerializeUtils.readStrIntLen(buffer);
    }
}
