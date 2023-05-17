package com.oraycn.ovcs.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.oraycn.ovcs.OrayApplication;
import com.oraycn.ovcs.utils.GlobalResourceCache;
import com.squareup.picasso.Picasso;
import com.oraycn.ovcs.R;
import com.oraycn.ovcs.models.ChatInfo;
import com.oraycn.ovcs.utils.SDKUtil;
import com.oraycn.ovcs.utils.io.BitmapUtil;


import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressLint("NewApi")
public class ChatLVAdapter extends BaseAdapter {
    /**
     * 执行动画的时间
     */
    protected long mAnimationTime = 150;
    private Context mContext;
    private int donw;
    private List<ChatInfo> list;
    /**
     * 弹出的更多选择框
     */
    private PopupWindow popupWindow;
    /**
     * 复制，删除
     */
    private View partition_line1;
    private TextView copy, delete,share;
    private LayoutInflater inflater;
    private ListView mListView;
    private SimpleDateFormat dateFormat24 = new SimpleDateFormat("MM-dd HH:mm");
    private ChatInfo chatInfo;
    private ViewHolder holder;

    Logger log = Logger.getLogger(ChatLVAdapter.class);

    public ChatLVAdapter(Context mContext, List<ChatInfo> list, ListView mlistv) {
        super();
        this.mContext = mContext;
        this.list = list;
        this.mListView = mlistv;
        inflater = LayoutInflater.from(mContext);
    }



    public void setList(List<ChatInfo> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public int getItemViewType(int position) {
        chatInfo = (ChatInfo) getItem(position);
        if (chatInfo.getImageUrl() != null && !chatInfo.getImageUrl().equals(""))
        {
            return ChatInfo.chat_img_item;
        }
        return ChatInfo.chat_lv_item;

    }


    @Override
    public int getViewTypeCount() {
        return 4;
    }

    public void updateItem(int position, ListView parent)
    {
        try {
            if(position<0)
            {
                return;
            }
            /**第一个可见的位置  -1是减去第一个View HeaderView的位置 **/
            int firstVisiblePosition = parent.getFirstVisiblePosition()-1;
            /**最后一个可见的位置**/
            int lastVisiblePosition = parent.getLastVisiblePosition()-1;
            /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
            if ((position >= firstVisiblePosition && position <= lastVisiblePosition)) {
                /**获取指定位置view对象**/
                View view = parent.getChildAt(position - firstVisiblePosition);
                getView(position, view, parent);
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        try {
            int type = getItemViewType(position);
            convertView=this.getConvertView(position,type,convertView);
            holder = (ViewHolder) convertView.getTag();
            this.setViewSendTime(position,holder);
            // 收到消息 from显示
            if (!list.get(position).getIsSender()) {
                showView(position,convertView, type);
            } else {//发送消息
                setViewSend(position, convertView, type);
            }
            return convertView;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    private View getConvertView(int position, int type ,View convertView)
    {
        if (convertView == null) {
            if (type == ChatInfo.chat_img_item) {
                convertView = getImgView(position);
            }
            else if (type == ChatInfo.chat_lv_item) {
                convertView = getTextView();
            }
        }
        return convertView;
    }

    private void setViewSendTime(int position,ViewHolder holder) {
        Date lastRecordTime=new Date(0);
        Date positionDate = list.get(position).MessageTime;
        if(position-1>=0)
        {
            lastRecordTime=list.get(position-1).MessageTime;
        }
        long diff = positionDate.getTime() - lastRecordTime.getTime();

        if (Math.abs(diff) < 5 * 60 * 1000) {
            holder.time.setVisibility(View.GONE);
        } else {
            holder.time.setText(dateFormat24.format(positionDate));
            holder.time.setVisibility(View.VISIBLE);
        }

    }

    private void showView(int position,View convertView, int type) {
        try
        {
            holder.toContainer.setVisibility(View.GONE);
            holder.fromContainer.setVisibility(View.VISIBLE);
            holder.fromHead.setImageBitmap(GlobalResourceCache.getInstance().getUserHead4UserID(list.get(position).getSpeakerID()));
            if (type == ChatInfo.chat_img_item) {
                showImgView(position,convertView);
            }  else {
                showTextView(position ,convertView);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void setViewSend(int position, View convertView, int type) {
        try
        {
            holder.toContainer.setVisibility(View.VISIBLE);
            holder.fromContainer.setVisibility(View.GONE);
            holder.toHead.setImageBitmap(GlobalResourceCache.getInstance().getUserHead4UserID(OrayApplication.getInstance().getCurUserID()));
            if (type == ChatInfo.chat_img_item) {
                setImgViewSend(position,convertView);
            } else {
                setTextViewSend(position, convertView);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    private void setTextViewSend(int position, View convertView) {
        // 对内容做处理
          SpannableStringBuilder sb = handler(list.get(position).getContent());
          Log.i("position:","pos:"+position+ "count:"+list.size());
        holder.toContent.setText(sb);
        holder.toContent.setActivated(false);
    }


    private void setImgViewSend(int position, View convertView) {
        try {

            Uri uri=Uri.fromFile(new File(list.get(position).getImageUrl()));
            Size size=BitmapUtil.getImgSize(uri.getPath());
            Size newSize= BitmapUtil.getZoomSize(size.getWidth(),size.getHeight(),400);
            Picasso.get().load(uri).error(R.drawable.default_image).resize(newSize.getWidth(),newSize.getHeight()).into(holder.toImgContent);
            holder.toImgContent.setActivated(false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setTime(int position, int type) {
        holder.toContainer.setVisibility(View.VISIBLE);
        holder.fromContainer.setVisibility(View.GONE);
        holder.time.setVisibility(View.VISIBLE);
        holder.time.setText(dateFormat24.format(list.get(position).getTime()));

    }

    private void showTextView(final int position , View convertView) {
        SpannableStringBuilder sb = handler(list.get(position).getContent());
        //若是系统消息 不显示下方头像及气泡内容
        if(chatInfo.isSystemMessage)
        {
           holder.container_item.setVisibility(View.GONE);
           holder.tv_system_msg.setText(sb);
           holder.tv_system_msg.setVisibility(View.VISIBLE);
           return;
        }
        else {
            holder.container_item.setVisibility(View.VISIBLE);
            holder.tv_system_msg.setVisibility(View.GONE);
        }
        if (chatInfo.isGroupChat) {
            holder.chatTitle.setText(chatInfo.getSpeakerID());
            holder.chatTitle.setVisibility(View.VISIBLE);
        }
        else {
            holder.chatTitle.setVisibility(View.GONE);
        }
        holder.fromContent.setText(sb);
    }


    private void showImgView(final int position,View convertView) {
        try{
            Uri uri =Uri.fromFile(new File(list.get(position).getImageUrl()));
            Size size=BitmapUtil.getImgSize(uri.getPath());
            Size newSize= BitmapUtil.getZoomSize(size.getWidth(),size.getHeight(),400);
           // holder.fromImgContent.setImageURI(uri);
            holder.fromImgContent.setVisibility(View.VISIBLE);
            Picasso.get().load(uri).error(R.drawable.default_image).resize(newSize.getWidth(),newSize.getHeight()).into(holder.fromImgContent);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        if (chatInfo.isGroupChat) {
            holder.chatTitle.setText(chatInfo.getSpeakerID());
        }

    }

    private View getImgView(final int position) {
        View convertView;
        convertView = LayoutInflater.from(mContext).inflate(
                R.layout.chat_img_item, null);
        holder = new ViewHolder();
        holder.fromContainer = (ViewGroup) convertView
                .findViewById(R.id.chart_from_container);
        holder.toContainer = (ViewGroup) convertView
                .findViewById(R.id.chart_to_container);
        holder.fromImgContent = (ImageView) convertView
                .findViewById(R.id.chatfrom_img_content);
        holder.fromContent = (TextView) convertView
                .findViewById(R.id.chatfrom_content);
        holder.toImgContent = (ImageView) convertView
                .findViewById(R.id.chatto_img_content);
        holder.time = (TextView) convertView
                .findViewById(R.id.chat_time);
        holder.fromHead = (ImageView) convertView.findViewById(R.id.chatfrom_icon);
        holder.toHead = (ImageView) convertView.findViewById(R.id.chatto_icon);
        holder.chatTitle = (TextView) convertView.findViewById(R.id.chatTitle);
        holder.position = position;
        convertView.setTag(holder);
        return convertView;
    }


    private View getTextView() {
        View convertView;
        convertView = LayoutInflater.from(mContext).inflate(
                R.layout.chat_lv_item, null);
        holder = new ViewHolder();
        holder.container_item=(ViewGroup)convertView.findViewById(R.id.container_item) ;
        holder.fromContainer = (ViewGroup) convertView
                .findViewById(R.id.chart_from_container);
        holder.toContainer = (ViewGroup) convertView
                .findViewById(R.id.chart_to_container);
        holder.fromContent = (TextView) convertView
                .findViewById(R.id.chatfrom_content);

        holder.toContent = (TextView) convertView
                .findViewById(R.id.chatto_content);
        holder.time = (TextView) convertView
                .findViewById(R.id.chat_time);
        holder.fromHead = (ImageView) convertView.findViewById(R.id.chatfrom_icon);
        holder.toHead = (ImageView) convertView.findViewById(R.id.chatto_icon);
        holder.tv_system_msg=(TextView)convertView.findViewById(R.id.system_msg);
        holder.chatTitle = (TextView) convertView.findViewById(R.id.chatTitle);
        convertView.setTag(holder);
        return convertView;
    }


    private SpannableStringBuilder handler(String content) {

        SpannableStringBuilder sb = new SpannableStringBuilder(content);
        String regex = "(\\[)\\d{3}(\\])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(content);
        while (m.find()) {
            String tempText = m.group();
            try {
                int index = Integer.parseInt(tempText.substring("[".length(),
                        tempText.length() - "]".length()));

                String png = "face/" + String.format("%03d", index) + ".png";
                try {
                    Bitmap image = BitmapFactory.decodeStream(mContext.getAssets().open(png));
                    image.setDensity(200);
                    sb.setSpan(
                            new ImageSpan(mContext, image), m.start(), m.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb;
    }

    /**
     * 屏蔽listitem的所有事件
     */
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }



    /**
     * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
     *
     * @param dismissView
     * @param dismissPosition
     */
    private void performDismiss(final View dismissView,
                                final int dismissPosition) {
        final LayoutParams lp = dismissView.getLayoutParams();// 获取item的布局参数
        final int originalHeight = dismissView.getHeight();// item的高度

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0)
                .setDuration(mAnimationTime);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                list.remove(dismissPosition);
                notifyDataSetChanged();
                // 这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
                // 所以我们在动画执行完毕之后将item设置回来
                ViewHelper.setAlpha(dismissView, 1f);
                ViewHelper.setTranslationX(dismissView, 0);
                LayoutParams lp = dismissView.getLayoutParams();
                lp.height = originalHeight;
                dismissView.setLayoutParams(lp);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

    }
    @Override
    public void notifyDataSetChanged()
    {
//        convertViews.clear();
        super.notifyDataSetChanged();
    }

    class ViewHolder {
        ImageView fromImgContent, toImgContent,fromHead,toHead;
        TextView fromContent, toContent, time,  chatTitle,  tv_system_msg;
        ViewGroup container_item, fromContainer, toContainer;
        int position;
    }

}
