package com.oraycn.ovcs.models;

import com.oraycn.esbasic.objectManagement.managers.ObjectManager;

import com.oraycn.ovcs.models.contract.BlobFragmentContract;

public class Blob {
    private ObjectManager<Integer, BlobFragmentContract> fragmentDictionary = new ObjectManager<Integer, BlobFragmentContract>(); //fragment Index - BlobFragmentContract
    private int lastFragmentIndex = -1 ;

    public Blob(int _blobID, String _sourceID, String _destID, int _informationType)
    {
        this.BlobID = _blobID;
        this.SourceID = _sourceID;
        this.DestID = _destID;
        this.InformationType = _informationType;
    }


    /// <summary>
    /// 大数据块唯一编号。从1开始逐个递增。
    /// </summary>
    public int BlobID=0;

    /// <summary>
    /// 信息的发送者。可以为UserID或者NetServer.SystemUserID。
    /// </summary>
    public String SourceID="";

    /// <summary>
    /// 信息的接收者。可以为UserID或者NetServer.SystemUserID或GroupID（广播消息）。
    /// </summary>
    public String DestID="";

    public int InformationType=0;

    private Object locker = new Object();
    public Information AddFragment(BlobFragmentContract fragment)
    {
        synchronized (this.locker)
        {
            if (fragment.IsLast)
            {
                this.lastFragmentIndex = fragment.FragmentIndex;
            }

            this.fragmentDictionary.add(fragment.FragmentIndex, fragment);
            return this.ConstructBlob();
        }
    }

    private Information ConstructBlob()
    {
        if (this.lastFragmentIndex < 0)
        {
            return null;
        }

        int totalSize = 0;
        for (int i = 0; i <= this.lastFragmentIndex; i++)
        {
            BlobFragmentContract fragment = this.fragmentDictionary.get(i) ;
            if (fragment == null)
            {
                return null;
            }

            totalSize += fragment.Fragment.length;
        }

        byte[] blob = new byte[totalSize];
        int offset = 0;
        for (int i = 0; i <= this.lastFragmentIndex; i++)
        {
            BlobFragmentContract fragment = this.fragmentDictionary.get(i);
           // Buffer.BlockCopy(fragment.Fragment, 0, blob, offset, fragment.Fragment.Length);
            System.arraycopy(fragment.Fragment, 0, blob,offset, fragment.Fragment.length);
            offset += fragment.Fragment.length;
        }

        return new Information(this.SourceID, this.DestID, this.InformationType, blob);
    }
}
