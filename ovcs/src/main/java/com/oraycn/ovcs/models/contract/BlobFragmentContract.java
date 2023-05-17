package com.oraycn.ovcs.models.contract;

import com.oraycn.omcs.utils.BufferUtils;
import com.oraycn.omcs.utils.SerializeUtils;

import com.oraycn.ovcs.utils.SerializeHelper;

import java.io.IOException;

import io.netty.buffer.ByteBuf;

public class BlobFragmentContract {
    public BlobFragmentContract()
    {
    }

    public BlobFragmentContract(String _sourceUserID, int _blobID, int _informationType, int _fragmentIndex, byte[] _fragment, boolean _isLast, String destUser)
    {
        this.SourceUserID = _sourceUserID;
        this.BlobID = _blobID;
        this.InformationType = _informationType;
        this.FragmentIndex = _fragmentIndex;
        this.Fragment = _fragment;
        this.IsLast = _isLast;
        this.DestUserID = destUser;
    }

    public int BlobID;
    public String SourceUserID;
    public int FragmentIndex;
    public byte[] Fragment;
    public int InformationType;
    public boolean IsLast;
    public String DestUserID;

    public byte[] serialize() throws Exception {
        ByteBuf body = BufferUtils.newBuffer();
        byte[] bDestUserID = DestUserID.getBytes("utf-8");
        byte[] bSourceUserID = SourceUserID.getBytes("utf-8");
        int bodyLen = 4 + 4 + 4 + bDestUserID.length+ 4  +4 +1+4 + bSourceUserID.length ;
        body.writeInt(bodyLen);
        body.writeInt(BlobID);
        body.writeInt(bDestUserID.length);
        body.writeBytes(bDestUserID);
        boolean fragmentisNull = Fragment == null || Fragment.length == 0;
        if (fragmentisNull) {
            body.writeInt(-1);
        } else {
            body.writeInt(Fragment.length);
            body.writeBytes(Fragment);
        }
        body.writeInt(FragmentIndex);
        body.writeInt(InformationType);
        body.writeBoolean(IsLast);
        body.writeInt(bSourceUserID.length);
        body.writeBytes(bSourceUserID);
        byte[] result = new byte[body.readableBytes()];
        System.arraycopy(body.array(), 0, result, 0, result.length);
        return result;
    }

    public void deserialize(byte[] info) throws IOException
    {
        ByteBuf buffer = SerializeHelper.wrappedBuffer(info);
        buffer.readInt();
        this.BlobID=buffer.readInt();
        this.DestUserID = SerializeUtils.readStrIntLen(buffer);
        int fragmentLenght = buffer.readInt();
        if(fragmentLenght>0)
        {
            Fragment = new byte[fragmentLenght];
            buffer.readBytes(Fragment);
        }
        FragmentIndex=buffer.readInt();
        InformationType=buffer.readInt();
        IsLast=buffer.readBoolean();
        this.SourceUserID=SerializeUtils.readStrIntLen(buffer);
    }
}
