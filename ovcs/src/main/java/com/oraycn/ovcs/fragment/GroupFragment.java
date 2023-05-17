package com.oraycn.ovcs.fragment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.View.MyEditText;
import com.oraycn.ovcs.adapter.ChatLVAdapter;
import com.oraycn.ovcs.adapter.FaceGVAdapter;
import com.oraycn.ovcs.adapter.FaceVPAdapter;
import com.oraycn.ovcs.models.ChatMessageRecord;
import com.oraycn.ovcs.models.ChatMessageType;
import com.oraycn.ovcs.models.event.ChatEvent;
import com.oraycn.ovcs.models.ChatInfo;
import com.oraycn.ovcs.models.InformationTypes;
import com.oraycn.ovcs.utils.FileUtils;
import com.oraycn.ovcs.utils.GlobalConfig;
import com.oraycn.ovcs.utils.GlobalResourceCache;
import com.oraycn.ovcs.utils.SDKUtil;
import com.oraycn.ovcs.utils.ToastUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class GroupFragment extends Fragment implements View.OnClickListener {
    private static final int SELECT_IMG = 201;
    private static final int TAKE_PICTURE = 202;
    private MyEditText input;
    private Button send;
    private ListView mListView;
    private ChatLVAdapter mLvAdapter;
    private LinearLayout bottom;
    private LinearLayout bottom_ban;
    private ViewGroup input_box ,chat_face_container,mDotsLayout;
    private TextView tv_banTips;
    private ImageView chatFullImg ,image_face;
    ImageButton downLoad_btn;//下载图片按钮
    private ViewGroup chart_all;
    private List<View> views = new ArrayList<View>();
    private LinkedList<ChatInfo> infos = new LinkedList<ChatInfo>();
    private SimpleDateFormat sd;
    private LinearLayout btnContainer;
    private Button btnMore;
    private ViewPager mViewPager;
    private int columns = 7;
    private int rows = 5;
    private List<View> emotionViews = new ArrayList<View>();
    Uri imageUri;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_group, container, false);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.initViews();
        return mView;
    }


    //初始化界面
    private void initViews() {
        try {

            input_box =(ViewGroup)mView.findViewById(R.id.input_box);
            btnContainer = (LinearLayout) mView.findViewById(R.id.ll_btn_container);
            btnMore = (Button) mView.findViewById(R.id.btn_more);
            bottom = (LinearLayout) mView.findViewById(R.id.bottom);
            sd = new SimpleDateFormat();

            chart_all = (ViewGroup) mView.findViewById(R.id.chart_all);
            chatFullImg = (ImageView) mView.findViewById(R.id.chatAll);
            downLoad_btn=(ImageButton) mView.findViewById(R.id.image_download);
            downLoad_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("点击下载", "onClick: ");
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    String path=downLoad_btn.getTag().toString();
                    String picName= FileUtils.getFileName(path);
                    Bitmap bitmap= BitmapFactory.decodeFile(path,options);
                    FileUtils.saveBitmapToDCIM(getActivity(),bitmap,picName);
                    downLoad_btn.setVisibility(View.GONE);
                }
            });

            // 表情图标
            image_face = (ImageView) mView.findViewById(R.id.image_face);
            // 表情按钮
            image_face.setOnClickListener(this);
            // 表情布局
            chat_face_container = (LinearLayout) mView.findViewById(R.id.chat_face_container);
            mViewPager = (ViewPager) mView.findViewById(R.id.face_viewpager);
            mViewPager.setOnPageChangeListener(new PageChange());
            // 表情下小圆点
            mDotsLayout = (LinearLayout) mView.findViewById(R.id.face_dots_container);
            this.initViewPager();

            mListView = (ListView) mView.findViewById(R.id.message_chat_listview);
            mLvAdapter = new ChatLVAdapter(getActivity(), infos, mListView);
            mLvAdapter.setList(infos);
            mListView.setAdapter(mLvAdapter);


            input = (MyEditText) mView.findViewById(R.id.input_sms);
            input.setOnClickListener(this);
            send = (Button) mView.findViewById(R.id.send_sms);

            // 发送
            send.setOnClickListener(this);
            mListView.setSelection(infos.size() - 1);

            mListView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                        if (chat_face_container.getVisibility() == View.VISIBLE) {
                            chat_face_container.setVisibility(View.GONE);
                        }
                        btnContainer.setVisibility(View.GONE);
                        hideSoftInputView();// 隐藏软键盘
                    }
                    return false;
                }
            });
            btnMore.setVisibility(View.GONE);
            send.setVisibility(View.VISIBLE);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //初始化表情框
    private void initViewPager() {
        for (int i = 0; i < getPagerCount(); i++) {
            emotionViews.add(viewPagerItem(i));
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(16, 16);
            mDotsLayout.addView(dotsItem(i), params);
        }
        FaceVPAdapter mVpAdapter = new FaceVPAdapter(emotionViews);
        mViewPager.setAdapter(mVpAdapter);
        mDotsLayout.getChildAt(0).setSelected(true);
    }

    private View viewPagerItem(int position) {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.face_gridview, null);// 表情布局
        GridView gridview = (GridView) layout.findViewById(R.id.chart_face_gv);
        List<String> subList = new ArrayList();
        List<String> staticFacesList = GlobalResourceCache.getInstance().getStaticFacesList();
        subList.addAll(staticFacesList.subList(position * (columns * rows),
                (columns * rows) * (position + 1) > staticFacesList.size() ? staticFacesList.size() : (columns * rows) * (position + 1)));
        FaceGVAdapter mGvAdapter = new FaceGVAdapter(subList, getActivity());
        gridview.setAdapter(mGvAdapter);
        gridview.setNumColumns(columns);
        // 单击表情执行的操作
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                try {
                    String png = ((TextView) ((LinearLayout) view)
                            .getChildAt(1)).getText().toString();
                    if (!png.contains("emotion_del_normal")) {// 如果不是删除图标
                        insert(getFace(png));
                    } else {
                        deleteFace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return gridview;
    }
    private void deleteFace() {
        if (input.getText().length() != 0) {
            int iCursorEnd = Selection.getSelectionEnd(input.getText());
            int iCursorStart = Selection.getSelectionStart(input.getText());
            if (iCursorEnd > 0) {
                if (iCursorEnd == iCursorStart) {
                    if (isDeletePng(iCursorEnd)) {
                        String st = "[000]";
                        input.getText().delete(
                                iCursorEnd - st.length(), iCursorEnd);
                    } else {
                        input.getText().delete(iCursorEnd - 1,
                                iCursorEnd);
                    }
                } else {
                    input.getText().delete(iCursorStart,
                            iCursorEnd);
                }
            }
        }
    }

    private void insert(CharSequence text) {
        int iCursorStart = Selection.getSelectionStart((input.getText()));
        int iCursorEnd = Selection.getSelectionEnd((input.getText()));
        if (iCursorStart != iCursorEnd) {
            input.getText().replace(iCursorStart, iCursorEnd, "");
        }
        int iCursor = Selection.getSelectionEnd((input.getText()));
        input.getText().insert(iCursor, text);
    }

    private boolean isDeletePng(int cursor) {
        String st = "[000]";
        String content = input.getText().toString().substring(0, cursor);
        if (content.length() >= st.length()) {
            String checkStr = content.substring(content.length() - st.length(), content.length());
            String regex = "(\\[)\\d{3}(\\])";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(checkStr);
            return m.matches();
        }
        return false;
    }
    private ImageView dotsItem(int position) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dot_image, null);
        ImageView iv = (ImageView) layout.findViewById(R.id.face_dot);
        iv.setId(position);
        return iv;
    }
    private int getPagerCount() {
        int count = GlobalResourceCache.getInstance().getStaticFacesList().size();
        return count % (columns * rows - 1) == 0 ? count / (columns * rows - 1)
                : count / (columns * rows - 1) + 1;
    }
    class PageChange implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
                mDotsLayout.getChildAt(i).setSelected(false);
            }
            mDotsLayout.getChildAt(arg0).setSelected(true);
        }
    }

    //点击输入框旁的+按钮，弹出
    public void more(View view) {
        if (btnContainer.getVisibility() == View.GONE) {
            hideSoftInputView();
            btnMore.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
        } else {
            btnContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View arg0) {
        if (SDKUtil.checkConnection()) {
            switch (arg0.getId()) {
                case R.id.input_sms:// 输入框
                    if (chat_face_container.getVisibility() == View.VISIBLE) {
                        chat_face_container.setVisibility(View.GONE);
                    }
                    if (btnContainer.getVisibility() == View.VISIBLE) {
                        btnContainer.setVisibility(View.GONE);
                    }
                    break;
                case R.id.image_face:
                    // 隐藏软键盘
                    hideSoftInputView();
                    // 表情
                    if (chat_face_container.getVisibility() == View.GONE) {
                        chat_face_container.setVisibility(View.VISIBLE);
                    } else {
                        chat_face_container.setVisibility(View.GONE);
                    }
                    btnContainer.setVisibility(View.GONE);
                    break;
                case R.id.send_sms:
                    // 发送
                    String reply = input.getText().toString();
                    if (!TextUtils.isEmpty(reply)) {
                        ChatMessageRecord chatContent=this.GetChatContent(reply);
                        if(this.sendMsg(chatContent))
                        {
                            input.setText("");
                        }
                    }
                    chat_face_container.setVisibility(View.GONE);
                    break;
                case R.id.btn_picture:
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    try {
                        startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的图片"), SELECT_IMG);
                        btnContainer.setVisibility(View.GONE);
                    } catch (android.content.ActivityNotFoundException ex) {
                        ToastUtils.showShort(getActivity(), "请安装文件管理器");
                    }
                    break;
                case R.id.btn_take_picture:
                    File outputImage = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera/" + System.currentTimeMillis() + ".jpg");
                    if (!outputImage.getParentFile().exists()) {
                        outputImage.getParentFile().mkdirs();
                    }
                     imageUri = Uri.fromFile(outputImage);//  FileProvider.getUriForFile(this,getPackageName()+".provider",outputImage);
                    Intent intentPic = new Intent();
                    intentPic.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intentPic.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    intentPic.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intentPic, TAKE_PICTURE);
                    btnContainer.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }
    private ChatMessageRecord GetChatContent(String chatStr) {
        ChatMessageRecord chatContent = new ChatMessageRecord(OrayApplication.getInstance().getCurUserID(),GlobalConfig.RoomID, ChatMessageType.TextEmotion,chatStr);
        return chatContent;
    }

    /**
     * 发送消息
     * */
    private boolean sendMsg(ChatMessageRecord record) {
        boolean sended = false;
        if (SDKUtil.checkConnection()) {
            try {
                byte[] datas = JSON.toJSONBytes(record, SerializerFeature.WriteDateUseDateFormat);
                OrayApplication.getInstance().getGroupOutter().broadcast(GlobalConfig.RoomID, InformationTypes.BroadcastChat, datas, "");
                sended = true;
            } catch (Exception e) {
                e.printStackTrace();
                sended = false;
            }
        }
        if (sended) {
            ChatInfo chatInfo =new ChatInfo(record);
            infos.add(chatInfo);
            this.scrollMessageToLast();
        } else {
            ToastUtils.showShort(getActivity(), "发送失败！");
        }
        return sended;
    }

    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) getActivity()
                .getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getActivity().getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getActivity().getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private SpannableStringBuilder getFace(String png) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        try {
            String tempText = "[" + png + "]";
            sb.append(tempText);
            Bitmap image= GlobalResourceCache.getInstance().getEmotion(png);
            image.setDensity(200);
            sb.setSpan(new ImageSpan(getActivity(), image), sb.length()- tempText.length(), sb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;
    }

    public void onEventMainThread(ChatEvent event) {
        ChatInfo chatInfo = null;
        if (event.isSystemMsg) {
            chatInfo = ChatInfo.getSystemInfo(event.groupID, event.chatMessageRecord.ContentStr);
        } else {
            chatInfo =  new ChatInfo(event.chatMessageRecord);
        }
        infos.add(chatInfo);
        this.scrollMessageToLast();
//        mLvAdapter.notifyDataSetChanged();
    }

    //滚动消息到最下方显示
    private void scrollMessageToLast()
    {
        Message message =new Message();
        message.what=0;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case 0:
                    mListView.setSelection(infos.size() - 1);
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //PreventKeyboardBlockUtil.getInstance(getActivity()).setBtnView(mListView).register();
    }

    @Override
    public void onPause() {
        super.onPause();
        //PreventKeyboardBlockUtil.getInstance(getActivity()).unRegister();
    }
}
