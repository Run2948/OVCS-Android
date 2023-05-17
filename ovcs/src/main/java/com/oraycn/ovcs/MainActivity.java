package com.oraycn.ovcs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.oraycn.ovcs.activity.DeskTopActivity;
import com.oraycn.ovcs.activity.WhiteBoardActivity;
import com.oraycn.ovcs.fragment.DeskTopFragment;
import com.oraycn.ovcs.fragment.GroupFragment;
import com.oraycn.ovcs.fragment.VideoFragment;
import com.oraycn.ovcs.models.GroupExtension;
import com.oraycn.ovcs.models.InformationTypes;
import com.oraycn.ovcs.models.event.ChatEvent;
import com.oraycn.ovcs.models.event.ControlDesktopEvent;
import com.oraycn.ovcs.models.event.DeskTopEvent;
import com.oraycn.ovcs.utils.GlobalConfig;
import com.oraycn.ovcs.utils.StringHelper;
import com.oraycn.ovcs.utils.SystemBarTintManager;
import com.oraycn.ovcs.utils.ToastUtils;

import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class MainActivity extends FragmentActivity {

    private HashMap<Integer,Button> mTabs;
    private VideoFragment videoFragment;
    private GroupFragment groupFragment;
    private DeskTopFragment deskTopFragment;
    private Fragment[] fragments;
    private TextView editText_unread_msg_count;
    private int curTabId=0;
    private Button deskTopButton,whiteBoardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GlobalConfig.RoomID = getIntent().getStringExtra("roomID");
        initView();
        initFragment();
        this.initSystemBar();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupExtension groupExtension = this.getGroupExtension();
        if (groupExtension != null && !StringHelper.isNullOrEmpty(groupExtension.DesktopSharedUserID)) {
            this.onEventMainThread(new DeskTopEvent(groupExtension.DesktopSharedUserID, true));
        }
    }

    /**
     * 初始化组件
     */
    private void initView() {
        mTabs = new HashMap<>();
        mTabs.put(R.id.btn_video_list, (Button) findViewById(R.id.btn_video_list));
        mTabs.put(R.id.btn_groupChat, (Button) findViewById(R.id.btn_groupChat));
        deskTopButton=(Button)findViewById(R.id.btn_deskTop);
        mTabs.put(R.id.btn_deskTop,deskTopButton);
        whiteBoardButton=(Button)findViewById(R.id.btn_whiteBoard);
        mTabs.put(R.id.btn_whiteBoard,whiteBoardButton);
        mTabs.get(R.id.btn_video_list).setSelected(true);
        this.curTabId=R.id.btn_video_list;
//        Drawable drawable = getResources().getDrawable(
//                R.drawable.tab_contact_list_bg);
//        drawable.setBounds(0, 0, 100,
//                100);
//        mTabs.get(R.id.btn_video_list).setCompoundDrawables(drawable,null , null, null);
//
//        Drawable drawable2 = getResources().getDrawable(
//                R.drawable.tab_chat_bg);
//        drawable2.setBounds(0, 0, 100,
//                100);
//        mTabs.get(R.id.btn_groupChat).setCompoundDrawables(drawable2,null , null, null);
//
//        Drawable drawable3 = getResources().getDrawable(
//                R.drawable.tab_org_bg);
//        drawable3.setBounds(0, 0, 100,
//                100);
//        mTabs.get(R.id.btn_groupChat).setCompoundDrawables(drawable3,null , null, null);

        editText_unread_msg_count=(TextView) findViewById(R.id.unread_msg_count);
    }

    private void initFragment() {

        groupFragment = new GroupFragment();
        videoFragment = new VideoFragment();
        deskTopFragment =new DeskTopFragment();
        fragments = new Fragment[]{
                groupFragment,
                videoFragment,
                deskTopFragment};
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, groupFragment)
                .add(R.id.fragment_container, videoFragment)
                .add(R.id.fragment_container,deskTopFragment)
                .hide(groupFragment)
                .hide(videoFragment)
                .hide(deskTopFragment);
        fragmentTransaction.show(videoFragment).commit();
    }

    /**
     * 获取群组的扩展信息
     * @return
     */
    private GroupExtension getGroupExtension() {
        try {
            byte[] res = OrayApplication.getInstance().getEngine().getCustomizeOutter().query(InformationTypes.GetGroupExtension, GlobalConfig.RoomID.getBytes("utf-8"));
            String json = new String(res, "utf-8");
            return JSON.parseObject(json, GroupExtension.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 沉浸式状态栏
     */
    @SuppressLint("ResourceAsColor")
    private void initSystemBar() {
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(R.color.bar);
    }

    /**
     * button点击事件
     *
     * @param view
     */
    public void onTabClicked(View view) {

        if(R.id.btn_deskTop== view.getId()&& StringHelper.isNullOrEmpty(lastShareDeskTopUserID))
        {
            ToastUtils.showLong(this,"没有人分享远程桌面");
            return;
        }

        FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            trx.hide(fragments[i]);
        }
        for (Map.Entry<Integer,Button> entry:  mTabs.entrySet())
        {
            entry.getValue().setSelected(false);
        }
        switch (view.getId()) {
            case R.id.btn_groupChat:
                mTabs.get(R.id.btn_groupChat).setSelected(true);
                trx.show(groupFragment).commit();
                this.unReadMessageCount=0;
                this.showUnReadMessage();
                break;
            case R.id.btn_video_list:
                mTabs.get(R.id.btn_video_list).setSelected(true);
                trx.show(videoFragment).commit();
                break;
            case R.id.btn_deskTop:
                Intent intent = new Intent(MainActivity.this, DeskTopActivity.class);
                intent.putExtra("targetUid", lastShareDeskTopUserID);
                intent.putExtra("isControl",lastShareDeskTopControl);
                MainActivity.this.startActivityForResult(intent,0);
//                mTabs.get(R.id.btn_deskTop).setSelected(true);
//                trx.show(deskTopFragment).commit();
                break;
            case R.id.btn_whiteBoard:
                Intent intent1 = new Intent(MainActivity.this, WhiteBoardActivity.class);
                intent1.putExtra("roomID", GlobalConfig.RoomID);
                MainActivity.this.startActivityForResult(intent1,1);
                break;
        }
        this.curTabId=view.getId();
    }


    private int unReadMessageCount=0;
    public void onEventMainThread(ChatEvent event) {
        if(this.curTabId==R.id.btn_groupChat)
        {return;}
        ++unReadMessageCount;
        this.showUnReadMessage();
    }

    private void showUnReadMessage()
    {
        if (this.unReadMessageCount > 0) {
            editText_unread_msg_count.setVisibility(View.VISIBLE);
            editText_unread_msg_count.setText(String.valueOf(this.unReadMessageCount));
        } else {
            editText_unread_msg_count.setVisibility(View.INVISIBLE);
        }
    }
    private String lastShareDeskTopUserID ="";//最后一个发起远程桌面共享的用户ID
    private boolean lastShareDeskTopControl=false;//最后一个用户是否发起了对自己的授权控制

    @Override
    protected void onDestroy() {
//        OrayApplication.getInstance().closeEngine();
        super.onDestroy();
    }

    /**
     * @param event
     */
    public void onEventMainThread(DeskTopEvent event) {
        if (event == null) {
            return;
        }
        if (event.getIsShare()) {
            lastShareDeskTopUserID = event.getTargetUid();
        } else {
            if (!lastShareDeskTopUserID.equals(event.getTargetUid())) {
                return;
            }
            lastShareDeskTopUserID = "";
            lastShareDeskTopControl = false;
        }
        String showID = StringHelper.isNullOrEmpty(lastShareDeskTopUserID) ? "无" : lastShareDeskTopUserID;
        deskTopButton.setText(String.format("桌面（%s）", showID));
    }
    public void onEventMainThread(ControlDesktopEvent event) {
        if(event==null)
        {
            return;
        }
        if(this.lastShareDeskTopUserID.equals(event.getTargetUid()))
        {
            this.lastShareDeskTopControl=event.getIsControl();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0)
        {
            View view =new View(this);
            view.setId(R.id.btn_video_list);
            onTabClicked(view);//模拟点击 btn_video_list 菜单
        }
    }
}
