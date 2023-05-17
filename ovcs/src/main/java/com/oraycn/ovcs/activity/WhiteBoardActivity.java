package com.oraycn.ovcs.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.oraycn.omcs.ConnectResult;
import com.oraycn.omcs.ConnectorDisconnectedType;
import com.oraycn.omcs.IConnectorEventListener;
import com.oraycn.omcs.whiteboard.WhiteBoardConnector;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.utils.ToastUtils;

public class WhiteBoardActivity extends Activity {

    WhiteBoardConnector whiteBoardConnector;
    ImageView resetSize_btn, curve_btn ,revokeBtn,exitBtn;
    boolean curve_btn_activated =false;//是否选中划曲线
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_board);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        whiteBoardConnector =(WhiteBoardConnector)findViewById(R.id.whiteBoardConnector);
        whiteBoardConnector.setPointer(BitmapFactory.decodeResource(getResources(),R.drawable.cursor));//设置鼠标指针图片
        whiteBoardConnector.setConnectorEventListener(new IConnectorEventListener() {
            @Override
            public void connectEnded(ConnectResult connectResult) {
                Message msg = Message.obtain(); // 实例化消息对象
                msg.what = 1; // 消息标识
                if( connectResult!= ConnectResult.Succeed){
                    msg.obj = "连接失败：" + connectResult.toString(); // 消息内容存放
                }else {
                    msg.obj = "连接白板成功！";
                }
                mHandler.sendMessage(msg);
            }

            @Override
            public void disconnected(ConnectorDisconnectedType connectorDisconnectedType) {

            }
        });
        Bundle bundle = getIntent().getExtras();
        String targetUid = bundle.getString("roomID");
        whiteBoardConnector.beginConnect(targetUid);

        resetSize_btn=(ImageView) (findViewById(R.id.resetSizeBtn));
        resetSize_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteBoardConnector.resetScale();
            }
        });


        curve_btn =(ImageView)(findViewById(R.id.gestureBtn));
        curve_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curve_btn_activated =!curve_btn_activated;
                curve_btn.setActivated(curve_btn_activated);
                whiteBoardConnector.setDrawCurveEnable(curve_btn_activated);
            }
        });

        revokeBtn=(ImageView)(findViewById(R.id.revokeBtn));
        revokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteBoardConnector.removeMyLastView();
            }
        });

        exitBtn=(ImageView)findViewById(R.id.exitBtn);
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        whiteBoardConnector.disconnect();
        super.onDestroy();
    }

    private Handler mHandler = new  Handler()  {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 1:
                    ToastUtils.showLong(WhiteBoardActivity.this,msg.obj.toString());
                    break;
            }
        }
    };
}