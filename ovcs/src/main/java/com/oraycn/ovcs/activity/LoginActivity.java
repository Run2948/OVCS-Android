package com.oraycn.ovcs.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.oraycn.esframework.common.LogonResponse;
import com.oraycn.esframework.core.IRapidPassiveEngine;
import com.oraycn.esframework.core.RapidEngineFactory;
import com.oraycn.omcs.MultimediaManagerFactory;
import com.oraycn.omcs.communicate.common.LogonResult;
import com.oraycn.ovcs.MainActivity;
import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.utils.GlobalResourceCache;
import com.oraycn.ovcs.utils.IntentUtils;
import com.oraycn.ovcs.utils.StringHelper;
import com.oraycn.ovcs.utils.ToastUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;
import me.weyye.hipermission.PermissionItem;


public class LoginActivity extends Activity {

    private EditText userName, passWord, serverIP,et_roomID;
    private TextView tv_error_tips;
    private Animation shake;
    private IRapidPassiveEngine engine;
    private Button login_btn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//        byte maxUserIDLen=20;//设置用户长度
//        EsfGlobalUtil.setMaxLengthOfUserId(maxUserIDLen);
//        OmcsGlobalUtil.setMaxLengthOfUserId(maxUserIDLen);

        shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
        initView();
    }

    /**
     * 初始化控件
     */
    public void initView() {

        login_btn=(Button)findViewById(R.id.btn_login);
        userName = (EditText) findViewById(R.id.p_id).findViewById(R.id.et_input);
        passWord = (EditText) findViewById(R.id.p_password).findViewById(R.id.et_input);
        passWord.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);//设置为密码格式显示 *
        serverIP = (EditText) findViewById(R.id.p_ip).findViewById(R.id.et_input);
        serverIP.setInputType(InputType.TYPE_CLASS_TEXT);
        et_roomID=(EditText) findViewById(R.id.p_roomID).findViewById(R.id.et_input);
        serverIP.setHint("请输入IP地址");
        et_roomID.setHint("请输入房间号");
        passWord.setHint("请输入密码");
        userName.setHint("请输入用户名");
        ((ImageView) findViewById(R.id.p_ip).findViewById(R.id.iv_ico)).setImageResource(R.drawable.ip_icon);
        ((ImageView) findViewById(R.id.p_roomID).findViewById(R.id.iv_ico)).setImageResource(R.drawable.room_icon);
        ((ImageView) findViewById(R.id.p_id).findViewById(R.id.iv_ico)).setImageResource(R.drawable.user_icon);
        ((ImageView) findViewById(R.id.p_password).findViewById(R.id.iv_ico)).setImageResource(R.drawable.password_icon);
        ((TextView)findViewById(R.id.p_id).findViewById(R.id.tv_tips)).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.p_password).findViewById(R.id.tv_tips)).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.p_ip).findViewById(R.id.tv_tips)).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.p_roomID).findViewById(R.id.tv_tips)).setVisibility(View.GONE);

        tv_error_tips =(TextView)findViewById(R.id.tv_error_tips);
        SharedPreferences sp = this.getSharedPreferences("loginInfo", MODE_PRIVATE);
        String ip = sp.getString("ip", getString(R.string.host));
        String userID = sp.getString("userID", "aa02");
        serverIP.setText(ip);
        et_roomID.setText("R001");
        userName.setText(userID);
        passWord.setText("1");
    }

    private Handler msgHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    LogonResponse resp = (LogonResponse) msg.obj;
                    tv_error_tips.setText("登录失败:" + resp.getFailureCause());
                    login_btn.setEnabled(true);
                    break;
                case 2:
                    tv_error_tips.setText("已在其他地方登录");
                    login_btn.setEnabled(true);
                    break;
                case 3:
                    tv_error_tips.setText( "客户端与服务端的框架不匹配");
                    login_btn.setEnabled(true);
                    break;
                case 4:
                    tv_error_tips.setText( "连接服务器无响应");
                    login_btn.setEnabled(true);
                    break;
                case 5:
                    com.oraycn.omcs.communicate.common.LogonResponse omcsResp = (com.oraycn.omcs.communicate.common.LogonResponse) msg.obj;
                    if (omcsResp == null) {
                        tv_error_tips.setText( "OMCS连接服务器无响应");
                    } else {

                        tv_error_tips.setText( "登录OMCS失败:" + omcsResp.getLogonResult());
                    }
                    login_btn.setEnabled(true);
                    break;
                case 6:
                    login_btn.setEnabled(true);
                    break;
                case 99:
                    tv_error_tips.setText( "正在登录ESF");
                    break;
                case 100:
                    tv_error_tips.setText( "正在登录OMCS");
                    break;
                case 101:
                    IntentUtils.startActivityForString(LoginActivity.this, ConnectActivity.class, "roomID", et_roomID.getText().toString());
//                    finish();
                    login_btn.setEnabled(true);
                    break;
                case 102:
                    String cause=msg.obj.toString();
                    tv_error_tips.setText(cause);
                    login_btn.setEnabled(true);
                    break;
                default:
                    tv_error_tips.setText( "ESF不明原因登录失败");
                    login_btn.setEnabled(true);
                    break;
            }

            super.handleMessage(msg);
        }
    };


    private boolean isVisitor=true;
    public boolean IsVisitor()
    {
        return this.isVisitor;
    }
    //账号密码登陆
    public void Login(View v) {
        login_btn.setEnabled(false);
        this.isVisitor=false;

        if (userName.getText().length() == 0) {
            ToastUtils.showShort(this, "用户名不能为空！");
            userName.startAnimation(shake);
            login_btn.setEnabled(true);
            return;
        }
        if(userName.getText().length() > 11)
        {
            ToastUtils.showShort(this, "登录账号的长度不能超过11！");
            userName.startAnimation(shake);
            login_btn.setEnabled(true);
            return;
        }
        if (passWord.getText().length() == 0) {
            ToastUtils.showShort(this, "密码不能为空！");
            passWord.startAnimation(shake);
            login_btn.setEnabled(true);
            return;
        }
        if (et_roomID.getText().length() == 0) {
            ToastUtils.showShort(this, "房间号不能为空！");
            et_roomID.startAnimation(shake);
            login_btn.setEnabled(true);
            return;
        }
        new Thread() {
            @Override
            public void run() {
                loginThread();
            }
        }.start();
    }

    //游客身份登陆
    public void  Login2(View v)
    {
        this.isVisitor=true;
    }

    private void loginThread() {
        String userID = userName.getText().toString();
        String getPassWord = StringHelper.md5(passWord.getText().toString());
        String serverIP = this.serverIP.getText().toString();
        msgHandler.obtainMessage(99).sendToTarget();

        LogonResponse resp = loginESFramework(serverIP, 4530, userID, getPassWord);
        if (resp == null) {
            msgHandler.obtainMessage(6).sendToTarget();
            return;
        }
        if (resp.getLogonResult() != com.oraycn.esframework.common.LogonResult.Succeed) {
            msgHandler.obtainMessage(resp.getLogonResult().ordinal(), resp).sendToTarget();
            msgHandler.obtainMessage(6).sendToTarget();
            return;
        }
        msgHandler.obtainMessage(100).sendToTarget();
        SharedPreferences sp = this.getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("ip", serverIP);
        editor.putString("userID",userID);
        editor.commit();

        com.oraycn.omcs.communicate.common.LogonResponse omcsResp = loginOMCS(serverIP, 9900,  userID, getPassWord);
        if (omcsResp.getLogonResult() == LogonResult.Succeed) {
            //设置本地摄像头采集分辨率
            MultimediaManagerFactory.GetSingleton().setCameraVideoSize(1280, 720);//640*480
            //设置视频压缩时质量，数字越小质量越高，须大于0
            MultimediaManagerFactory.GetSingleton().setCameraEncodeQuality(20);
            //设置自动调整摄像头输出尺寸 默认false
            MultimediaManagerFactory.GetSingleton().getAdvanced().setAutoAdjustCameraOutputSize(true);
            MultimediaManagerFactory.GetSingleton().getAdvanced().setAmplifier4AudioPlay(2);//放大声音
            MultimediaManagerFactory.GetSingleton().getAdvanced().setUseDeviceAEC(false);//是否使用系统自带的回音消除， false：使用第三方的
            MultimediaManagerFactory.GetSingleton().setCameraDeviceIndex(1);// 0 - 后置摄像头， 1 - 前置
        } else {
            msgHandler.obtainMessage(5, omcsResp).sendToTarget();
            return;
        }
        GlobalResourceCache.getInstance().initialize(OrayApplication.getInstance());
        OrayApplication.getInstance().setEngine(engine);
        OrayApplication.getInstance().setDynamicGroupOutter(engine.getDynamicGroupOutter());
        msgHandler.obtainMessage(101).sendToTarget();
    }


    private LogonResponse loginESFramework(String esfServerIP, int esfServerPort, String userName, String password) {
        LogonResponse resp = null;
        try {
            engine = RapidEngineFactory.CreatePassiveEngine();
            engine.setEngineEventListener(OrayApplication.getInstance());
            engine.getBasicOutter().addBasicEventListener(OrayApplication.getInstance());
            engine.setWaitResponseTimeoutInSecs(30);
           // engine.setSystemToken(isVisitor ? "visitor" : "member");
//            engine.getContactsOutter().setContactsEventListener(OrayApplication.getInstance());
            resp = engine.initialize(userName, password, esfServerIP, esfServerPort, OrayApplication.getInstance(), getApplication());
        } catch (Exception ex) {
            Message msg = new Message();
            msg.obj = "登录ESF失败：" + ex.getMessage();
            msg.what = 102;
            msgHandler.sendMessage(msg);
        }
        return resp;
    }

    private com.oraycn.omcs.communicate.common.LogonResponse loginOMCS(String omcsServerIP, int omcsServerPort, String userName, String password) {
        com.oraycn.omcs.communicate.common.LogonResponse omcsResp = null;
        try {
            omcsResp = MultimediaManagerFactory.GetSingleton().initialize(userName,
                    password, omcsServerIP, omcsServerPort, getApplication());
        } catch (Exception ex) {
            Message msg = new Message();
            msg.obj = "登录OMCS失败：" + ex.getMessage();
            msg.what = 102;
            msgHandler.sendMessage(msg);
        }
        return omcsResp;
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.getPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_error_tips.setText("");
    }

    /**
     * 获取权限
     * */
    private void getPermission()
    {
        List<PermissionItem> permissionItems = new ArrayList<PermissionItem>();
        permissionItems.add(new PermissionItem(Manifest.permission.CAMERA,"相机",R.drawable.permission_ic_camera));
        permissionItems.add(new PermissionItem(Manifest.permission.RECORD_AUDIO,"麦克风",R.drawable.permission_ic_micro_phone));
        permissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE,"存储",R.drawable.permission_ic_storage));
        permissionItems.add(new PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE,"",0));
        try {
            HiPermission.create(LoginActivity.this)
                    .title("欢迎访问"+getString(R.string.app_name))
                    .permissions(permissionItems)
                    .checkMutiPermission(new PermissionCallback() {

                        String TAG=getString(R.string.app_name);
                        @Override
                        public void onClose() {
                            Log.i(TAG, "onClose");
                            // showToast(getString(R.string.permission_on_close));
                        }

                        @Override
                        public void onFinish() {
                            Log.i(TAG, "onFinish");
                            // showToast(getString(R.string.permission_completed));
                        }

                        @Override
                        public void onDeny(String permission, int position) {
                            Log.i(TAG, "onDeny- permission："+permission+"   position:"+position);
                        }

                        @Override
                        public void onGuarantee(String permission, int position) {
                            Log.i(TAG, "onGuarantee");
                        }
                    });
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
