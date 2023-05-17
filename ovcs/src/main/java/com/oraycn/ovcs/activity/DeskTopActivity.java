package com.oraycn.ovcs.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.oraycn.omcs.ConnectResult;
import com.oraycn.omcs.ConnectorDisconnectedType;
import com.oraycn.omcs.IConnectorEventListener;
import com.oraycn.omcs.core.DesktopConnector;

import com.oraycn.ovcs.R;
import com.oraycn.ovcs.View.DesktopSurfaceView2;
import com.oraycn.ovcs.models.event.ControlDesktopEvent;
import com.oraycn.ovcs.models.event.DeskTopEvent;
import com.oraycn.ovcs.utils.ToastUtils;

import de.greenrobot.event.EventBus;

public class DeskTopActivity extends Activity {

    private static final int REQUEST_CODE = 1000;
    DesktopSurfaceView2 otherView = null;
    ImageView hang_up,resetSize_btn,gesture_btn;
    ViewGroup btn_container_deskTop;
    String targetUid = "";
    DesktopConnector desktopConnector;
    private boolean dragMoveEnabled =false;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desk_top);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Bundle bundle = getIntent().getExtras();
        targetUid = bundle.getString("targetUid");
        boolean isControl=bundle.getBoolean("isControl",false);
        getScreenBaseInfo();
        btn_container_deskTop=(ViewGroup)findViewById(R.id.btn_deskTop_container);
        otherView = (DesktopSurfaceView2) findViewById(R.id.Desk_surface_remote);
        otherView.setPointer(BitmapFactory.decodeResource(getResources(),R.drawable.cursor));//设置鼠标指针图片
        hang_up = (ImageView) findViewById(R.id.desk_hung_up);
        resetSize_btn=(ImageView) (findViewById(R.id.resetSizeBtn));
        resetSize_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherView.resetScale();
            }
        });

        gesture_btn=(ImageView)(findViewById(R.id.gestureBtn));
        gesture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dragMoveEnabled =!dragMoveEnabled;
                String tips=dragMoveEnabled?"开启拖动":"关闭拖动";
                ToastUtils.showLong(DeskTopActivity.this, tips);
                otherView.setDragMoveEnabled(dragMoveEnabled);//设置是否拖动控制
                gesture_btn.setActivated(dragMoveEnabled);
            }
        });

//        EditText textCommand=(EditText)findViewById(R.id.keyBoard);
//        otherView.setControlEditText(textCommand);


        desktopConnector = new DesktopConnector();
        this.setControlStatus(isControl);

        //挂断
        hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePage();
            }
        });
        setVideo();
    }

    private void setControlStatus(boolean isControl)
    {
        desktopConnector.setWatchingOnly(!isControl);//设置是否仅仅只能观看。 true只能观看；false：可以操作（手势操作的前提）
        if(isControl)
        {
            dragMoveEnabled =false;
            gesture_btn.setVisibility(View.VISIBLE);
        }else {
            gesture_btn.setVisibility(View.INVISIBLE);
            dragMoveEnabled =true;
         }
        gesture_btn.requestLayout();//解决界面未及时刷新的问题
    }

    @Override
    protected void onResume() {
        //设置为可以横竖平自由切换
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        RelativeLayout.LayoutParams params=null;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏操作
            int width= otherView.getHeight();
            int newHeight= width*9/16;
            params=new RelativeLayout.LayoutParams(width,newHeight);
        }
        else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //  横屏操作
            params =new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        }
        otherView.setLayoutParams(params);
    }

    private boolean curPageColse=false;
    private void closePage()
    {
        if(desktopConnector.isConnected())
        {
            desktopConnector.disconnect();
        }
        finish();
        curPageColse=true;
    }

    private void getScreenBaseInfo() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = 640;//metrics.widthPixels;
        mScreenHeight =480;//metrics.heightPixels;
        mScreenDensity = metrics.densityDpi;
    }

    class mHandler extends Handler {

        // 通过复写handlerMessage() 从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {

            ToastUtils.showLong(DeskTopActivity.this,msg.obj.toString());
        }
    }
    private mHandler myHandler=new mHandler();

    private void setVideo() {
        desktopConnector.setOtherVideoPlayerSurfaceView(otherView);
        desktopConnector.setConnectorEventListener(new IConnectorEventListener() {
            @Override
            public void connectEnded(ConnectResult connectResult) {
                if( connectResult!= ConnectResult.Succeed){
                    Message msg = Message.obtain(); // 实例化消息对象
                    msg.what = 1; // 消息标识
                    msg.obj = "连接失败：" + connectResult.toString(); // 消息内容存放
                    myHandler.sendMessage(msg);
                }
            }

            @Override
            public void disconnected(ConnectorDisconnectedType connectorDisconnectedType) {
                if(connectorDisconnectedType==ConnectorDisconnectedType.OwnerActiveDisconnect||connectorDisconnectedType==ConnectorDisconnectedType.GuestActiveDisconnect)
                {
                    return;
                }
                Message msg = Message.obtain(); // 实例化消息对象
                msg.what = 2; // 消息标识
                msg.obj = "连接断开：" + connectorDisconnectedType.toString();// 消息内容存放
                myHandler.sendMessage(msg);
            }
        });
        desktopConnector.beginConnect(targetUid);
    }

    public void onEventMainThread(DeskTopEvent event) {
        if(event==null)
        {
            return;
        }
        if(!event.getIsShare()&&this.targetUid.equals(event.getTargetUid()))
        {
            closePage();
        }
    }

    public void onEventMainThread(ControlDesktopEvent event) {
        if(event==null)
        {
            return;
        }
        if(this.targetUid.equals(event.getTargetUid())&&!curPageColse)
        {
            if(event.getIsControl())
            {
                ToastUtils.showLong(DeskTopActivity.this,"对方已开启了授权控制！");
                this.setControlStatus(true);
            }
            else {
                ToastUtils.showLong(DeskTopActivity.this,"对方已关闭了授权关闭！");
                this.setControlStatus(false);
            }
        }
    }

}
