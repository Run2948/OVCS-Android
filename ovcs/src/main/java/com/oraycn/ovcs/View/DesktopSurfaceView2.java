package com.oraycn.ovcs.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.view.GestureDetectorCompat; //之前找不到这个类，需要在build.gradle中添加：implementation 'androidx.appcompat:appcompat:1.1.0'

import com.oraycn.omcs.core.IDesktopCommandSender;
import com.oraycn.omcs.core.IDesktopSurfaceView;
import com.oraycn.omcs.core.OMCSSurfaceView;

/**
 *
 */
public class DesktopSurfaceView2 extends ViewGroup implements IDesktopSurfaceView, View.OnTouchListener {
    private RectF iniViewRectF;
    private OMCSSurfaceView omcsSurfaceView;
    private EditText inputBox = new EditText(getContext());
    private InputMethodManager inputMethodManager = null;

    public DesktopSurfaceView2(Context context) {
        super(context);
        init(context);
    }

    public DesktopSurfaceView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DesktopSurfaceView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.iniViewRectF = new RectF(left, top, right, bottom);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.layout(0, 0, childView.getMeasuredWidth(), childView.getMeasuredHeight());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            //这个很重要，没有就不显示
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void init(Context context) {
        inputBox.setLayoutParams(new LayoutParams(10, 10));

        this.setControlEditText(inputBox);
        this.addView(inputBox);
        inputBox.setVisibility(INVISIBLE);
        omcsSurfaceView = new OMCSSurfaceView(context);
        omcsSurfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        this.addView(omcsSurfaceView);

        this.setOnTouchListener(this);
        this.inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        GestureListener gestureListener = new GestureListener();
        gestureDetectorCompat = new GestureDetectorCompat(context, gestureListener);
        gestureDetectorCompat.setOnDoubleTapListener(gestureListener);

    }

    GestureDetectorCompat gestureDetectorCompat;
    public void setControlEditText(EditText _inputBox) {
        inputBox = _inputBox;
        inputBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        inputBox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        inputBox.setMaxLines(1);
        inputBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputBox.setFocusable(true);
                String str = s.toString();
                if (TextUtils.isEmpty(str)) {
                    return;
                }

                if (desktopCommandSender != null) {
                    sendTextCommand(str);
                    inputBox.setText("");
                }
            }
        });

        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (EditorInfo.IME_ACTION_DONE == actionId || EditorInfo.IME_ACTION_UNSPECIFIED == actionId)//当点击键盘上的完成时，关闭键盘输入框
                {
                    sendkeyCodeCommand(13);//回车命令
                    //setCanSendTextCommand(false);
                    return true;
                }
                return false;

            }
        });

        inputBox.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //只有按钮按下才处理后续事件
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return false;
                }
                if (keyCode == 67) {
                    keyCode = 8;//若按钮为退回键（可能检测为67），强制转为8 退回命令
                }
                sendkeyCodeCommand(keyCode);
                return false;
            }
        });
    }

    /**
     * 发送文本命令
     */
    private void sendTextCommand(String content) {
        if (this.desktopCommandSender == null) {
            return;
        }
        this.desktopCommandSender.keyboardCommand2020(content);
    }

    /**
     * 发送按键命令
     */
    private void sendkeyCodeCommand(int keyCode) {
        if (desktopCommandSender == null) {
            return;
        }
        desktopCommandSender.keyboardCommand(true, keyCode);
        desktopCommandSender.keyboardCommand(false, keyCode);
    }

    //region IDesktopSurfaceView
    private IDesktopCommandSender desktopCommandSender;

    @Override
    public void setDesktopCommandSender(IDesktopCommandSender iDesktopCommandSender) {
        this.desktopCommandSender = iDesktopCommandSender;
    }

    @Override
    public OMCSSurfaceView getOMCSSurfaceView() {
        return this.omcsSurfaceView;
    }

    @Override
    public Bitmap getMouseCursorImage() {
        return null;
    }
    //endregion

    private boolean dragMoveEnabled = false;

    //单指移动时，识别为手势（false），还是作为远程控制（true）？
    public void setDragMoveEnabled(boolean enabled) {
        this.dragMoveEnabled = enabled;
    }

    /**
     * 重置界面到最初状态
     */
    public void resetScale() {
        this.omcsSurfaceView.setGuestureOffsetX(0);
        this.omcsSurfaceView.setGuestureOffsetY(0);
        this.omcsSurfaceView.setGuestureScale(1.0f,new PointF(0,0));
        offsetX = 0;
        offsetY = 0;
        offsetScale = 1;
    }

    //region OnTouchListener
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private int mode = NONE;
    /**
     * 当前触摸点相对于屏幕的坐标
     */
    private float mCurrentInScreenX;
    private float mCurrentInScreenY;
    /**
     * 上一次触摸点相对于屏幕的坐标
     */
    private float mLastInScreenX;
    private float mLastInScreenY;
    /**
     * 触摸点按下时的相对于屏幕的坐标
     */
    private float mDownInScreenX;
    private float mUpInScreenX;
    private float mDownInScreenY;
    private float distance;
    private float preDistance;
    private PointF mid = new PointF();

    private static final float MAX_ZOOM_SCALE = 4.0f;
    private static final float MIN_ZOOM_SCALE = 1.0f;

    private float offsetX, offsetY = 0;
    private float offsetScale = 1;
    private boolean scaling = false;
    private  float tmpScale = 1 ;
    private float lastDistance;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getPointerCount()==1)
        {
            this.gestureDetectorCompat.onTouchEvent(event);
        }
        //region 手势操作（放大、缩小、偏移）

        mCurrentInScreenX = event.getRawX();
        mCurrentInScreenY = event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //单个手指触摸
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                mDownInScreenX = event.getRawX();
                mDownInScreenY = event.getRawY();
                break;
            //两指触摸
            case MotionEvent.ACTION_POINTER_DOWN:
                preDistance = getDistance(event);
                //当两指间距大于10时，计算两指中心点
                if (preDistance > 10f) {
                    mid = getMid(event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                mCurrentInScreenX = 0;
                mCurrentInScreenY = 0;
                mDownInScreenX = 0;
                mDownInScreenY = 0;
                mLastInScreenX = 0;
                mLastInScreenY = 0;

                center();  //回弹，令图片居中
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                this.scaling = false ;
                this.tmpScale = this.offsetScale;
                this.lastDistance=0f;
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                //当两指缩放，计算缩放比例
                if (mode == ZOOM) {
                    if(!this.scaling) {
                        this.scaling = true;
                    }
                    distance = getDistance(event);
                    if (distance > 10f) {
                        float scale_increment = (distance -preDistance)/ preDistance;
                        this.offsetScale = this.tmpScale + scale_increment ;
                        if (this.offsetScale < MIN_ZOOM_SCALE) {
                            this.offsetScale = MIN_ZOOM_SCALE;
                        } else if (this.offsetScale > MAX_ZOOM_SCALE) {
                            this.offsetScale = MAX_ZOOM_SCALE;
                        }
                        float tempDistance=this.lastDistance==0f?preDistance:this.lastDistance;
//                        float scale= distance / tempDistance;
//                        offsetX -= (scale - 1) * mid.x;//(scale-1)*iniViewRectF.width();
//                        offsetY -= (scale - 1) * mid.y;//(scale-1)*iniViewRectF.height()
                        //getRevisedPointF(event.getX(), event.getY());
                        this.omcsSurfaceView.setGuestureScale(this.offsetScale,mid);//1
                        if(this.offsetScale==MIN_ZOOM_SCALE)
                        {
                            this.omcsSurfaceView.setGuestureOffsetX(0);
                            this.omcsSurfaceView.setGuestureOffsetY(0);
                        }
                        this.lastDistance=distance;
                    }
                } else if (mode == DRAG)//单指移动
                {
                    if (!this.dragMoveEnabled && offsetScale != MIN_ZOOM_SCALE) {
                        offsetX += mCurrentInScreenX - mLastInScreenX;
                        offsetY += mCurrentInScreenY - mLastInScreenY;

                        this.omcsSurfaceView.setGuestureOffsetX((int) (offsetX));//1
                        this.omcsSurfaceView.setGuestureOffsetY((int) (offsetY));//1
                    }
                }
                break;
        }
        mLastInScreenX = event.getRawX();
        mLastInScreenY = event.getRawY();
//        invalidate();

        //endregion

        //region 若不能手势操作，且是仅仅观看的权限 ，直接返回，
        if (desktopCommandSender.isWatchingOnly()) {
            return true;
        }
        //endregion

        //region 触控操作（可点击、移动、发送文字命令）
        //根据对方分辨率获取对方的X,Y位置
        // float y =mImageWidth- event.getX();
        // float x = event.getY();
        //横屏x
        float phoneX = event.getX();
        //横屏y
        float phoneY = event.getY();

        cur_x = phoneX;
        cur_y = phoneY;
        PointF pointF = this.getRevisedPointF(phoneX, phoneY);
        float xRevised = pointF.x;
        float yRevised = pointF.y;

        // Log.i("DeskTopSurfaceView", "x=" + x + ",y=" + y);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (desktopCommandSender != null && (this.dragMoveEnabled || offsetScale == MIN_ZOOM_SCALE))
                {
                    desktopCommandSender.mousePressCommand(true, true, xRevised, yRevised);
                    drawMouse();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!offsetZone(cur_x, cur_y, phoneX, phoneY)) {
                    drawMouse();
                }
                //当滑动时背景为选中状态 //检测是否长按,在非长按时检测
                if (desktopCommandSender != null && (this.dragMoveEnabled || offsetScale == MIN_ZOOM_SCALE)) {
                    desktopCommandSender.mouseMove(xRevised, yRevised);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (desktopCommandSender != null && (this.dragMoveEnabled || offsetScale == MIN_ZOOM_SCALE)) {
                    desktopCommandSender.mousePressCommand(false, true, xRevised, yRevised);
                }
                break;
            }
        }

        //endregion
        return true;
    }

    private float cur_x, cur_y;//当前坐标（相对于view）
    private Bitmap pointer = null;

    private PointF getRevisedPointF(float phoneX, float phoneY) {
        Size videoSize = this.omcsSurfaceView.getVideoSize();
        Matrix m = new Matrix();
        m.set(this.createMatrix());
        //绘制图片矩形
        //这样rect.left，rect.right,rect.top,rect.bottom分别就就是当前屏幕离图片的边界的距离
        RectF rect = new RectF(getLeft(), getTop(), getWidth(), getHeight());
        m.mapRect(rect);//rect为canvas画的实际位置与大小

        float scaleWidth = videoSize.getWidth() * 1.0f / rect.width();
        float scaleHeight = videoSize.getHeight() * 1.0f / rect.height();

        float xRevised = (phoneX - rect.left) * scaleWidth;
        float yRevised = (phoneY - rect.top) * scaleHeight;
        return new PointF(xRevised, yRevised);
    }

    /**
     * 设置控制点图像
     */
    public void setPointer(Bitmap bitmap) {
        this.pointer = bitmap;
    }

    private void drawMouse() {
        if (!desktopCommandSender.isWatchingOnly()) {

            Canvas canvas = null;
            SurfaceHolder holder = this.omcsSurfaceView.getSurfaceView().getHolder();
            try {
                canvas = holder.lockCanvas();
                // 绘制背景图片
                if (canvas != null) {
                    synchronized (holder) {
                        //计算图片的中心位置
                        int pointerWidth = pointer.getWidth();
                        int pointerHeight = pointer.getHeight();

                        float newX = cur_x - pointerWidth / 2;
                        float newY = cur_y - pointerHeight / 2;

                        canvas.drawBitmap(pointer, newX, newY, null);
                        holder.unlockCanvasAndPost(canvas);//绘制完成，释放画布，提交修改
                    }
                }
            } catch (Exception e) {
                Log.e("DeskTopSurfaceView", e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (canvas != null) {
                        // Log.i("H264DecodeThread", "unlockCanvasAndPost");
                        //PeerSurfaceHolder.unlockCanvasAndPost(canvas);//绘制完成，释放画布，提交修改
                    }
                } catch (Exception e) {
                    Log.e("DeskTopSurfaceView", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /* 判断是否有长按动作发生
     * @param lastX 按下时X坐标
     * @param lastY 按下时Y坐标
     * @param thisX 移动时X坐标
     * @param thisY 移动时Y坐标
     * @param lastDownTime 按下时间
     * @param thisEventTime 移动时间
     * @param longPressTime 判断长按时间的阀值
     */
    private boolean isLongPressed(float lastX, float lastY,
                                  float thisX, float thisY,
                                  long lastDownTime, long thisEventTime,
                                  long longPressTime) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        long intervalTime = thisEventTime - lastDownTime;
        if (offsetX <= 10 && offsetY <= 10 && intervalTime >= longPressTime) {
            return true;
        }
        return false;
    }

    /**
     * 两个坐标是否有间距  （x,y 差都小于5 当做没有间距）
     */
    private boolean offsetZone(float lastX, float lastY,
                               float thisX, float thisY) {
        float offsetX = Math.abs(thisX - lastX);
        float offsetY = Math.abs(thisY - lastY);
        if (offsetX <= 5 && offsetY <= 5) {
            return true;
        }
        return false;
    }

    /*获取两指之间的距离*/
    private float getDistance(MotionEvent event) {
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        float distance = (float) Math.sqrt(x * x + y * y);//两点间的距离
        return distance;
    }

    /*取两指的中心点坐标*/
    public static PointF getMid(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }

    private Matrix createMatrix() {
        Matrix matrix_new = new Matrix();
        int _offsetX = this.omcsSurfaceView.getGuestureOffsetX();
        int _offsetY = this.omcsSurfaceView.getGuestureOffsetY();
        float _offsetScale = this.omcsSurfaceView.getGuestureScale();
        PointF scaleCenterPoint=this.omcsSurfaceView.getGuestureScaleCenter();
        matrix_new.postScale(_offsetScale, _offsetScale, scaleCenterPoint.x, scaleCenterPoint.y);
        matrix_new.postTranslate(_offsetX, _offsetY);
        return matrix_new;
    }

    /*使图片居中*/
    private void center() {
        Matrix m = new Matrix();
        m.set(this.createMatrix());
        //绘制图片矩形
        //这样rect.left，rect.right,rect.top,rect.bottom分别就就是当前屏幕离图片的边界的距离
        RectF rect = new RectF(getLeft(), getTop(), getWidth(), getHeight());
        m.mapRect(rect);
        float deltaX = 0, deltaY = 0;

        if (this.iniViewRectF.contains(rect) || rect.contains(this.iniViewRectF))//判断所画的图是否与整个View 为包含的关系
        {
            return;
        }
        float delta_left = Math.abs(rect.left - iniViewRectF.left);//左边距 距离差
        float delta_right = Math.abs(rect.right - iniViewRectF.right);//右边距 距离差
        float delta_top = Math.abs(rect.top - iniViewRectF.top);//上边距 距离差
        float delta_bottom = Math.abs(rect.bottom - iniViewRectF.bottom);//下边距 距离差

        if ((this.iniViewRectF.left >= rect.left && this.iniViewRectF.right <= rect.right) || (this.iniViewRectF.left < rect.left && this.iniViewRectF.right > rect.right))//左右都超出View边界或者都在View内部
        {
        } else if (this.iniViewRectF.left < rect.left) {
            deltaX = delta_right > delta_left ? -delta_left : -delta_right;
        } else {
            deltaX = delta_right > delta_left ? delta_left : delta_right;
        }

        if ((this.iniViewRectF.top >= rect.top && this.iniViewRectF.bottom <= rect.bottom) || (this.iniViewRectF.top < rect.top && this.iniViewRectF.bottom > rect.bottom))//上下都超出了View边界或者都在View内部
        {
        } else if (this.iniViewRectF.top < rect.top) {
            deltaY = delta_bottom > delta_top ? -delta_top : -delta_bottom;
        } else {
            deltaY = delta_bottom > delta_top ? delta_top : delta_bottom;
        }
        offsetX += deltaX;
        offsetY += deltaY;
        this.omcsSurfaceView.setGuestureOffsetX((int) offsetX);//1
        this.omcsSurfaceView.setGuestureOffsetY((int) offsetY);//1
    }

    private boolean isMove() {
        //允许有10的偏差 在判断是否移动的时候
        if (Math.abs(mDownInScreenX - mCurrentInScreenX) <= 10 && Math.abs(mDownInScreenY - mCurrentInScreenY) <= 10) {
            return false;
        } else {
            return true;
        }
    }

    private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{

        //region GestureDetector.OnDoubleTapListener
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (desktopCommandSender != null) {
                PointF pointF = getRevisedPointF(e.getX(), e.getY());
                desktopCommandSender.mouseDoubleClick(pointF.x, pointF.y);
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {


            return false;
        }
        //endregion

        //region GestureDetector.OnGestureListener
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (desktopCommandSender != null) {
                PointF pointF = getRevisedPointF(e.getX(), e.getY());
                desktopCommandSender.mousePressCommand(true, true, pointF.x, pointF.y);
                desktopCommandSender.mousePressCommand(false, true, pointF.x, pointF.y);
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
        //endregion


    }

    //endregion
}
