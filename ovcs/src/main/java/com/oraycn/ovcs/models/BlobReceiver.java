package com.oraycn.ovcs.models;

import com.oraycn.esbasic.objectManagement.managers.ObjectManager;

import com.oraycn.ovcs.models.contract.BlobFragmentContract;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlobReceiver {
    private ObjectManager<String, Blob> blobManager = new ObjectManager<String ,Blob>(); //SourceUserID - blobID - Blob

    private String ConstructID(String sourceID, int blobID)
    {
        return String.format("%s#%s" ,sourceID ,blobID);
    }
    public Information Receive(String sourceID, String destID , BlobFragmentContract fragment)
    {
        if (fragment.BlobID == 1 && fragment.FragmentIndex == 0 ) //可能是掉线重新登录过
        {
            this.OnUserOffline(sourceID);
        }

        if (fragment.FragmentIndex == 0 && fragment.IsLast)
        {
            return new Information(sourceID, destID, fragment.InformationType, fragment.Fragment);
        }

        String id = this.ConstructID(sourceID, fragment.BlobID);
        Blob blob = this.blobManager.get(id);
        if (blob == null)
        {
            blob = new Blob(fragment.BlobID, sourceID, destID, fragment.InformationType);
            this.blobManager.add(id, blob);
        }

        Information info = blob.AddFragment(fragment);
        if (info != null)
        {
            this.blobManager.remove(id);
        }
        return info;
    }

    public void OnUserOffline(String userID)
    {
        HashMap<String,Blob> map=this.blobManager.ToHashMap();
        for (Map.Entry<String,Blob> kvp :map.entrySet()) {
            if(kvp.getValue().SourceID.equals(userID))
            {
                this.blobManager.remove(kvp.getKey());
                return;
            }
        }
    }
}
