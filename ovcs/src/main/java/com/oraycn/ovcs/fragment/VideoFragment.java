package com.oraycn.ovcs.fragment;
import android.annotation.SuppressLint;
import android.graphics.Camera;
import android.media.AudioManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.oraycn.esframework.core.Basic.GroupInformation;
import com.oraycn.omcs.ConnectResult;
import com.oraycn.omcs.ConnectorDisconnectedType;
import com.oraycn.omcs.ICamOpenOverCallback;
import com.oraycn.omcs.ICameraConnectorCallback;
import com.oraycn.omcs.IConnectorEventListener;
import com.oraycn.omcs.MachineType;
import com.oraycn.omcs.MultimediaManagerFactory;
import com.oraycn.omcs.RotateAngle;
import com.oraycn.omcs.core.CameraSurfaceView;
import com.oraycn.omcs.core.OMCSSurfaceView;
import com.oraycn.omcs.mulitChat.ChatType;
import com.oraycn.omcs.mulitChat.IChatGroup;
import com.oraycn.omcs.mulitChat.IChatGroupEventListener;
import com.oraycn.omcs.mulitChat.IChatUnit;

import com.oraycn.ovcs.MainActivity;
import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.View.OMCSSurfaceViewPlus;
import com.oraycn.ovcs.models.GroupExtension;
import com.oraycn.ovcs.utils.AutoNewLineLayout;
import com.oraycn.ovcs.utils.GlobalConfig;
import com.oraycn.ovcs.utils.android.AndroidUtil;

import java.util.HashMap;


public class VideoFragment extends Fragment implements ICamOpenOverCallback {

    CameraSurfaceView myCameraSurfaceView = null;
    ImageView hang_up, convert_camera ;
    Button switch_to_mute,switch_to_Handsfree,switch_to_openCamera;
    int currentCamIndex = 1;
    String groupid = "";
    private int currVolume=0;//当前音量
    LinearLayout layout_buttom,layout_middle;
    IChatGroup chatGroup ;
    TextView stateText;
    AutoNewLineLayout camera_container;
    HashMap<String , ViewGroup> cameraMap=new HashMap<>();
    public VideoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_video, container, false);
        try {
            MultimediaManagerFactory.GetSingleton().setCameraOpenCallBack(this);
            currentCamIndex=MultimediaManagerFactory.GetSingleton().getCameraDeviceIndex();
            groupid = GlobalConfig.RoomID;
            //显示本地数据view
            myCameraSurfaceView = (CameraSurfaceView) mView.findViewById(R.id.group_camera_surfaceview_local);

            TextView mytext=(TextView) mView.findViewById(R.id.tv_userID);
            mytext.setText(MultimediaManagerFactory.GetSingleton().getCurrentUserID());
            stateText = (TextView) mView.findViewById(R.id.stateText);
            layout_buttom = (LinearLayout) mView.findViewById(R.id.layout_bottom);
            layout_middle =(LinearLayout) mView.findViewById(R.id.layout_middle);
            View view= mView.findViewById(R.id.group_video_session);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isGone=layout_buttom.getVisibility()== View.GONE;
                    layout_buttom.setVisibility(isGone? View.VISIBLE:View.GONE);
                    layout_middle.setVisibility(isGone? View.VISIBLE:View.GONE);
                }
            });
            camera_container =(AutoNewLineLayout)mView.findViewById(R.id.camera_container);
            convert_camera = (ImageView) mView.findViewById(R.id.group_convert_camera);
            switch_to_mute=(Button) mView.findViewById(R.id.switch_to_mute);
            switch_to_Handsfree=(Button)mView.findViewById(R.id.switch_to_Handsfree);
            switch_to_openCamera=(Button)mView.findViewById(R.id.switch_to_openCamera);

            // myView.setZ(1);
            //显示对方数据view
            setHangUp();
            setConvertCamera();
            setButtonEvent();
            setBtnConnect();

        } catch
        (Exception ex) {
            ex.printStackTrace();
        }
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    //region ICamOpenOverCallback
    @Override
    public void cameraHasOpened() {
        try {
            //预览camera所用holder,不可为null或是隐藏，否则不能从摄像头获取数据
            SurfaceHolder holder = myCameraSurfaceView.getSurfaceHolder();
            MultimediaManagerFactory.GetSingleton().startCameraPreview(holder);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void cameraOpenFailed(Exception e) {
        e.printStackTrace();
    }
    //endregion

    private void setHangUp() {
        hang_up = (ImageView) mView.findViewById(R.id.group_hung_up);
        //挂断
        hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultimediaManagerFactory.GetSingleton().getGroupEntrance().exit(ChatType.Video, groupid);
                OrayApplication.getInstance().getGroupOutter().quitGroup(groupid);
                getActivity().finish();
//                OrayApplication.getInstance().logout();
            }
        });
    }

    private void setConvertCamera() {

        //切换摄像头
        convert_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertCameraIndex();
            }
        });
    }

    private void setBtnConnect() {
        try {
            setChatGroup();
            //stateText.setText("正在连接中……");
            for ( IChatUnit unit : chatGroup.getOtherMembers()) {
                addMemberView(unit);
            }
        } catch (Exception e) {
            stateText.setText("出错了……");
            e.printStackTrace();
        }
    }

    //切换摄像头
    private void convertCameraIndex() {
        try {
            ++currentCamIndex;
            //切换摄像头
            MultimediaManagerFactory.GetSingleton().switchCameraDeviceIndex(currentCamIndex % 2);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setButtonEvent()
    {
        //静音
        switch_to_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMute = switch_to_mute.isActivated();
                MultimediaManagerFactory.GetSingleton().setOutputAudio(isMute);//true :开起声音， false：静音
                switch_to_mute.setActivated(!isMute);
            }
        });

        //免提
        switch_to_Handsfree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isHandsfree=false;
                if(currVolume==0){
                    currVolume = AndroidUtil.OpenSpeaker(getActivity());
                    isHandsfree=true;
                }else{
                    AndroidUtil.CloseSpeaker(getActivity(),currVolume);
                    currVolume=0;
                    isHandsfree=false;
                }
                switch_to_Handsfree.setActivated(isHandsfree);
            }
        });

        //打开摄像头
        switch_to_openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOpenCamera = !switch_to_openCamera.isActivated();
                MultimediaManagerFactory.GetSingleton().setOutputVideo(isOpenCamera);
                switch_to_openCamera.setActivated(isOpenCamera);
            }
        });
        switch_to_openCamera.performClick();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    removeMemberView((IChatUnit) msg.obj);
                    break;
                case 0: {
                    addMemberView((IChatUnit) msg.obj);
                    break;
                }
            }
        }

    };

    //设置并加入群聊组
    private void setChatGroup() {

        chatGroup = MultimediaManagerFactory.GetSingleton().getGroupEntrance().join(ChatType.Video, groupid);
        chatGroup.setChatGroupEventListener(new IChatGroupEventListener() {
            @Override
            public void someoneJoin(IChatUnit unit) {
                Message msg = new Message();
                msg.obj = unit;
                msg.what = 0;
                mHandler.sendMessage(msg);
            }

            @Override
            public void someoneExit(IChatUnit unit) {
                Message msg = new Message();
                msg.what = 1;
                msg.obj = unit;
                mHandler.sendMessage(msg);
            }
        });
        GroupInformation groupInformation = OrayApplication.getInstance().getGroupOutter().joinGroup(groupid);//ESF 动态组加入
    }

    //添加指定对象的视频
    private void addMemberView(final IChatUnit unit) {
        OMCSSurfaceViewPlus omcs_surface_plus = new OMCSSurfaceViewPlus(getActivity());
        omcs_surface_plus.initialize(unit,false);
        camera_container.addView(omcs_surface_plus);
        cameraMap.put(unit.getMemberID(), omcs_surface_plus);
    }

    //移除指定对象的视频
    private void removeMemberView(IChatUnit chatUnit)
    {
        if(chatUnit!=null)
        {
            chatUnit.cameraConnector().disconnect();
            chatUnit.microphoneConnector().disconnect();
        }
        ViewGroup layout= cameraMap.get(chatUnit.getMemberID());
        if(layout!=null)
        {
            layout.setVisibility(View.GONE);
            camera_container.removeView(layout);
        }
        cameraMap.remove(chatUnit.getMemberID());
    }
    //移除所有连接的视频
    private void removeAllMemberView()
    {
        for (IChatUnit chatUnit: chatGroup.getOtherMembers())
        {
            removeMemberView(chatUnit);
        }
        cameraMap.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        SurfaceHolder holder = myCameraSurfaceView.getSurfaceHolder();
        myCameraSurfaceView.surfaceCreated(holder);
    }
}
