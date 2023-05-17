package com.oraycn.ovcs.View;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.oraycn.omcs.ConnectResult;
import com.oraycn.omcs.ConnectorDisconnectedType;
import com.oraycn.omcs.ICameraConnectorCallback;
import com.oraycn.omcs.IConnectorEventListener;
import com.oraycn.omcs.IMicrophoneConnectorCallback;
import com.oraycn.omcs.MachineType;
import com.oraycn.omcs.RotateAngle;
import com.oraycn.omcs.core.OMCSSurfaceView;
import com.oraycn.omcs.mulitChat.IChatUnit;

import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.models.CommunicateMediaType;
import com.oraycn.ovcs.models.event.RequestEvent;
import com.oraycn.ovcs.utils.ToastUtils;

import android.os.Handler;
import java.util.logging.LogRecord;

import de.greenrobot.event.EventBus;

public class OMCSSurfaceViewPlus extends RelativeLayout {
    private Context mContext;
    private View mView;
    private TextView tv_UserID;
    private OMCSSurfaceView omcsSurfaceView;
    private ImageView camera_ico, mic_ico;
    private IChatUnit chatUnit;
    private boolean myself;
    ConnectResult connectCameraResult ,connectMicResult;

    public OMCSSurfaceViewPlus(Context context) {
        this(context, null);
    }

    public OMCSSurfaceViewPlus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OMCSSurfaceViewPlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.omcs_surface_plus, this, true);
        tv_UserID = (TextView) mView.findViewById(R.id.tv_userID);
        omcsSurfaceView = (OMCSSurfaceView) mView.findViewById(R.id.group_surface_remote);
        camera_ico = (ImageView) mView.findViewById(R.id.camera_ico);
        mic_ico = (ImageView) mView.findViewById(R.id.mic_ico);
    }

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                setcameraState();
            }else if(msg.what==1)
            {
                setMicState();
            }
        }
    };


    //初始化成员视频显示控件。
    public void initialize(IChatUnit unit, boolean myself)
    {
        this.chatUnit=unit;
        this.myself=myself;
        unit.cameraConnector().setConnectorEventListener(new IConnectorEventListener() {
            @Override
            public void connectEnded(ConnectResult result) {
                Log.i(chatUnit.getMemberID(), result.toString());
                if(chatUnit.cameraConnector().getOwnerMachineType()== MachineType.IOS)
                {
                    chatUnit.cameraConnector().setRotateAngle(RotateAngle.R90);
                }
                chatUnit.cameraConnector().setVideoUniformScale(true,true);
                connectCameraResult=result;
                mhandler.sendEmptyMessage(0);
            }
            @Override
            public void disconnected(ConnectorDisconnectedType connectorDisconnectedType) {

            }
        });
        unit.microphoneConnector().setConnectorEventListener(new IConnectorEventListener() {
            @Override
            public void connectEnded(ConnectResult result) {
                Log.i(chatUnit.getMemberID(), result.toString());
                connectMicResult=result;
                mhandler.sendEmptyMessage(1);
            }

            @Override
            public void disconnected(ConnectorDisconnectedType connectorDisconnectedType) {

            }
        });
        unit.cameraConnector().setCameraConnectorCallback(new ICameraConnectorCallback() {
            @Override
            public void OnOwnerOutputChanged(boolean b) {
                mhandler.sendEmptyMessage(0);
            }

            @Override
            public void OnOwnerCameraVideoSizeChanged(Size size) {

            }
        });
        unit.microphoneConnector().SetConnectorCallback(new IMicrophoneConnectorCallback() {
            @Override
            public void OnOwnerOutputChanged(boolean b) {
                mhandler.sendEmptyMessage(1);
            }

            @Override
            public void OnAudioDataReceived(short[] shorts) {

            }
        });
        unit.cameraConnector().setOtherVideoPlayerSurfaceView(omcsSurfaceView);
        this.tv_UserID.setText(unit.getMemberID());
        unit.cameraConnector().beginConnect(unit.getMemberID());
        unit.microphoneConnector().beginConnect(unit.getMemberID());
    }

    public OMCSSurfaceView getOmcsSurfaceView() {
        return this.omcsSurfaceView;
    }

    public IChatUnit getChatUnit()
    {
        return this.chatUnit;
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        this.tv_UserID.setText(title);
    }

    public void setMicState()
    {
        if (this.connectMicResult != ConnectResult.Succeed)
        {
            this.mic_ico.setBackgroundResource(R.drawable.mic_fail);
            this.mic_ico.setVisibility(VISIBLE);
        }
        else
        {
            this.mic_ico.setVisibility(!this.chatUnit.microphoneConnector().getOwnerOutput()?VISIBLE:GONE);
            if (!this.chatUnit.microphoneConnector().getOwnerOutput())//麦克风被主人禁用
            {
                this.mic_ico.setBackgroundResource(R.drawable.mic_fail);
                return;
            }
            this.mic_ico.setVisibility(!myself?VISIBLE:GONE);
            if (this.chatUnit.microphoneConnector().mute)//静音
            {
                this.mic_ico.setBackgroundResource(R.drawable.mic_dis);
            }
            else
            {
                this.mic_ico.setVisibility(GONE);
            }
        }
    }

    public void setcameraState() {
        if (this.connectCameraResult != ConnectResult.Succeed) {
            this.camera_ico.setBackgroundResource(R.drawable.camera_fail);
            this.camera_ico.setVisibility(VISIBLE);
        } else {
            if (this.chatUnit.cameraConnector().getOwnerOutput()) {
                this.camera_ico.setVisibility(INVISIBLE);
            } else {
                this.camera_ico.setVisibility(VISIBLE);
                this.camera_ico.setBackgroundResource(R.drawable.camera_dis);//摄像头被主人禁用
            }
        }
    }
}
