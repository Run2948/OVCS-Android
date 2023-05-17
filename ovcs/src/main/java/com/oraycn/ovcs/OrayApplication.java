package com.oraycn.ovcs;
import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.oraycn.esbasic.helpers.StringHelper;
import com.oraycn.esframework.common.LogonResponse;
import com.oraycn.esframework.common.LogonResult;
import com.oraycn.esframework.core.Basic.GroupInformation;
import com.oraycn.esframework.core.BasicEventListener;
import com.oraycn.esframework.core.ClientType;
import com.oraycn.esframework.core.DynamicGroupEventListener;
import com.oraycn.esframework.core.EngineEventListener;
import com.oraycn.esframework.core.ICustomizeHandler;
import com.oraycn.esframework.core.IDynamicGroupOutter;
import com.oraycn.esframework.core.IRapidPassiveEngine;
import com.oraycn.omcs.MultimediaManagerFactory;
import com.oraycn.omcs.utils.BufferUtils;

import com.oraycn.ovcs.activity.LoginActivity;
import com.oraycn.ovcs.models.BlobReceiver;
import com.oraycn.ovcs.models.ChatBoxElemnetType;
import com.oraycn.ovcs.models.ChatMessageRecord;
import com.oraycn.ovcs.models.event.ChatEvent;
import com.oraycn.ovcs.models.CommunicateMediaType;
import com.oraycn.ovcs.models.event.DeskTopEvent;
import com.oraycn.ovcs.models.InformationTypes;
import com.oraycn.ovcs.models.event.RequestEvent;
import com.oraycn.ovcs.models.event.SystemChatEvent;
import com.oraycn.ovcs.utils.GlobalConfig;
import com.oraycn.ovcs.utils.SerializeHelper;
import com.oraycn.ovcs.utils.ToastUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import de.greenrobot.event.EventBus;
import io.netty.buffer.ByteBuf;

/**
 *
 */
public class OrayApplication extends Application implements ICustomizeHandler, EngineEventListener, BasicEventListener {
    private IRapidPassiveEngine engine;
    private static OrayApplication instance;

    public static OrayApplication getInstance()
    {
        return instance;
    }

    public void setEngine(IRapidPassiveEngine engine)
    {
        this.engine=engine;
    }
    public IRapidPassiveEngine getEngine()
    {return this.engine;}

    public String getCurUserID() {
        return this.engine.getCurrentUserID();
    }

    private IDynamicGroupOutter groupOutter;
    public void setDynamicGroupOutter(IDynamicGroupOutter groupOutter)
    {
        this.groupOutter=groupOutter;
        this.groupOutter.setDynamicGroupEventListener(new DynamicGroupEventListener() {
            @Override
            public void broadcastReceived(String broadcasterID, String groupID, int broadcastType, byte[] broadcastContent, String tag) {
                try {
                    if (!groupID.equals(GlobalConfig.RoomID)) {
                        return;
                    }
                    //region 即时通信消息的处理
                    if (broadcastType == InformationTypes.BroadcastChat) {
                        String json = new String(broadcastContent, "utf-8");
                        ChatMessageRecord chatMessageRecord = JSON.parseObject(json, ChatMessageRecord.class);
                        postChatEvent4RichChatMessageReceived(groupID, broadcasterID, chatMessageRecord);
                        return;
                    }
                    //endregion

                    //region 共享桌面消息的处理
                    if (broadcastType == InformationTypes.BroadcastShareDesk) {
                        ByteBuf buffer = SerializeHelper.wrappedBuffer(broadcastContent);
                        boolean isShare = buffer.readBoolean();
                        DeskTopEvent deskTopEvent = new DeskTopEvent(broadcasterID, isShare);
                        EventBus.getDefault().post(deskTopEvent);
                        String msg = isShare ? broadcasterID + "开启共享桌面" : broadcasterID + "关闭共享桌面";
                        sendSystemMessage(groupID, msg);
                        return;
                    }
                    //endregion

                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            @Override
            public void groupmateOffline(String s) {

            }

            @Override
            public void groupmateOnline(String s) {

            }

            @Override
            public void someoneJoinGroup(String groupID, String memberID) {
                if(groupID.equals(GlobalConfig.RoomID))
                {
                    sendSystemMessage(groupID,memberID+"上线");
                }
            }

            @Override
            public void someoneQuitGroup(String groupID, String memberID) {
                if(groupID.equals(GlobalConfig.RoomID)){
                    sendSystemMessage(groupID,memberID+"掉线");
                    DeskTopEvent deskTopEvent=new DeskTopEvent(memberID,false);//对方掉线也将移除远程桌面
                    EventBus.getDefault().post(deskTopEvent);
                }
            }

            @Override
            public void someoneBePulledIntoGroup(String s, List<String> list, String s1) {

            }

            @Override
            public void someoneBeRemovedFromGroup(String s, List<String> list, String s1) {

            }

            @Override
            public void bePulledIntoGroup(GroupInformation groupInformation, String s) {

            }

            @Override
            public void beRemovedFromGroup(String s, String s1) {

            }

            @Override
            public void beInvitedJoinGroup(GroupInformation groupInformation, String s) {

            }

            @Override
            public void groupTagChanged(String s, String s1, String s2) {

            }
        });
    }
    public IDynamicGroupOutter getGroupOutter()
    {
        return this.groupOutter;
    }

    private BlobReceiver blobReceiver =new BlobReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                ToastUtils.showLong(OrayApplication.this, "您已掉线,请检查相关网络设置");
                EventBus.getDefault().post(new RequestEvent(null,0, CommunicateMediaType.Offline));
                logout();
            } else if (msg.what == 1) {
                ToastUtils.showLong(OrayApplication.this, "正在重新连接……!");
            } else if (msg.what == 2) {
                ToastUtils.showLong(OrayApplication.this, "重连成功!");
                EventBus.getDefault().post(new RequestEvent(null,0,CommunicateMediaType.Online));
            } else if (msg.what == 3) {
                ToastUtils.showLong(OrayApplication.this, "您已退出，请重新登录!");
            } else if (msg.what == 4) {
                ToastUtils.showLong(OrayApplication.this, "账号在别处登录，下线!");
                logout();
            } else if (msg.what == 5) {
                ToastUtils.showLong(OrayApplication.this, "被其他用户踢出，下线!");
                logout();
            }
        }
    };

    //region ICustomizeHandler
    @Override
    public void handleInformation(String sourceUserID, ClientType clientType,int informationType, byte[] information) {

    }

    @Override
    public byte[] handleQuery(String sourceUserID, ClientType clientType,  int informationType, byte[] bytes) {
        return new byte[0];
    }
    //endregion

    //region EngineEventListener
    private boolean waitingConnected = false;
    @Override
    public void connectionInterrupted() {
        //由于移动网络的不稳定性，此事件被触发的几率比较高
        //引擎会自动（可设置）重连网络，此处适当延时触发掉线事件
     //   this.groupOutter.clearCache();
        if (!waitingConnected) {
            waitingConnected = true;
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10 * 1000);
                        if (!engine.connected()) {
                            uiHandler.obtainMessage(0).sendToTarget();
                        }
                        waitingConnected = false;
                    } catch (Exception e) {
                    }
                }
            }.start();
        }
    }

    @Override
    public void connectionRebuildStart() {
        uiHandler.obtainMessage(1);
    }

    @Override
    public void messageReceived(String s, ClientType clientType,  int i, byte[] bytes, String s1) {

    }

    @Override
    public void echoMessageReceived(ClientType clientType, String s, int i, byte[] bytes, String s1) {

    }

    @Override
    public void relogonCompleted(LogonResponse logonResp) {
        if (logonResp != null && logonResp.getLogonResult() == LogonResult.Succeed) {
            uiHandler.obtainMessage(2).sendToTarget();
        }
    }
    //endregion

    //region BasicEventListener
    @Override
    public void beingPushedOut() {
        logout();
        uiHandler.obtainMessage(4).sendToTarget();
    }

    @Override
    public void beingKickedOut() {
        logout();
        uiHandler.obtainMessage(5).sendToTarget();
    }

    @Override
    public void myDeviceOnline(ClientType clientType) {

    }

    @Override
    public void myDeviceOffline(ClientType clientType) {

    }


    //endregion

    //退出
    public void  logout()
    {
        this.closeEngine();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK );
        this.startActivity(intent);
    }

    //关闭引擎
    public void closeEngine()
    {
        this.engine.close();
        MultimediaManagerFactory.GetSingleton().close();
    }


    //region PostChatEvent4RichChatMessageReceived
    /**
     * 將PC端的多張圖片消息分成多條消息顯示
     * @param groupID 组id（如果不是组消息，sourceID）接收者ID
     * @param sourceID 发送者ID
     * @param chatMessageRecord 消息内容
     * */
    public void postChatEvent4RichChatMessageReceived(String groupID, String sourceID, ChatMessageRecord chatMessageRecord)
    {
        this.postChatEvent4RichChatMessageReceived(groupID,sourceID,chatMessageRecord,true);
    }


    /**
     * 將PC端的多張圖片消息分成多條消息顯示
     * @param groupID 组id（如果不是组消息，sourceID）接收者ID
     * @param sourceID 发送者ID
     * @param chatMessageRecord 消息内容
     * @param isGroupChat 是否为群消息
     * */
    public void postChatEvent4RichChatMessageReceived(String groupID,  String sourceID, ChatMessageRecord chatMessageRecord, boolean isGroupChat) {
        if (chatMessageRecord == null) {
            return;
        }
        try {
            ChatEvent chatEvent = new ChatEvent(groupID, sourceID, chatMessageRecord, isGroupChat);
            chatEvent.occurrenceTime = chatMessageRecord.MessageTime;
            EventBus.getDefault().post(chatEvent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //endregion



    /**
     * 发送系统消息（只是客户端自己的聊天框显示）
     * */
    private void sendSystemMessage(String groupID, String msg )
    {
        ChatEvent systemMessageEvent = new SystemChatEvent(groupID, msg);
        EventBus.getDefault().post(systemMessageEvent);
    }

}
