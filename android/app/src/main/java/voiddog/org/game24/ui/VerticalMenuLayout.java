package voiddog.org.game24.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import voiddog.org.game24.R;
import voiddog.org.game24.util.SizeUtil;

/**
 *
 * Created by Dog on 2015/6/26.
 */
public class VerticalMenuLayout extends ViewGroup{
    //超过了这个数值就拦截点击事件
    private static final int MIN_MOVE_DIS = 50;
    //阻尼参数
    private final int C;

    //容器绘制的宽度和高度
    private int mDrawWidth, mDrawHeight;
    //开始绘制的头部
    private int mStartTop = 0;
    //开始绘制的左侧
    private int mStartLeft = 0;
    //按下时候的坐标
    private Point mDownP = null;
    //最后手指移动的坐标
    private Point mLastP = null;
    //是否弹回
    private boolean isBounce = false;
    //menu点击事件
    private OnMenuClickListener clickListener;
    //弹回runnable
    BounceRunnable bounceRunnable = new BounceRunnable();
    //进入进出动画数值
    int enterAnimTopDis[];
    float mSpringValue = 0.0f;
    //在播放进入进出动画
    boolean isPlayingEnterExitAnimation = false;
    //需要播放进场动画
    boolean needToPlayEnterAnim = true;
    //进入动画Runnable
    EnterSpringController enterSpringController;
    //布局方向
    private int orientation = 0;
    //弹簧系统
    private SpringSystem mSpringSystem = SpringSystem.create();


    public VerticalMenuLayout(Context context){
        this(context, null);
    }

    public VerticalMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.VerticalMenuLayout, defStyleAttr, 0);

        orientation = a.getInt(R.styleable.VerticalMenuLayout_menu_gravity, 0);

        a.recycle();
        C = SizeUtil.dp2px(context, 20);

        enterSpringController = new EnterSpringController();
    }

    public void setOnMenuClickListener(OnMenuClickListener clickListener){
        this.clickListener = clickListener;
    }

    /**
     * 开始播放进入动画
     */
    public void startPlayEnterAnimation(){
        needToPlayEnterAnim = true;
        requestLayout();
    }

    @Override
    public void addView(View child, final int index, LayoutParams params) {
        if(isPlayingEnterExitAnimation){
            return;
        }
        child.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onMenuClick(v, index);
                }
            }
        });
        super.addView(child, index, params);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
         */
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 计算出所有的childView的宽和高
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int cMaxWidth = 0, cHeightSum = 0;
        MarginLayoutParams cParams;

        for(int i = 0; i < getChildCount(); i++){
            View view = getChildAt(i);
            cParams = (MarginLayoutParams) view.getLayoutParams();

            int cDrawWidth = view.getMeasuredWidth() + cParams.leftMargin + cParams.rightMargin;
            int cDrawHeight = view.getMeasuredHeight() + cParams.topMargin + cParams.bottomMargin;
            int cWidth;
            int cHeight;

            cDrawWidth = Math.min(sizeWidth, cDrawWidth);
            cDrawHeight = Math.min(sizeHeight, cDrawHeight);

            cWidth = cDrawWidth - cParams.leftMargin - cParams.rightMargin;
            cHeight = cDrawHeight - cParams.topMargin - cParams.bottomMargin;

            view.measure(cWidth | MeasureSpec.EXACTLY, cHeight | MeasureSpec.EXACTLY);

            cHeightSum += cDrawHeight;
            cMaxWidth = Math.max(cMaxWidth, cDrawWidth);
        }

        /**
         * 计算绘制区域高宽
         */
        mDrawWidth = Math.min(sizeWidth - getPaddingLeft() - getPaddingRight(), cMaxWidth);
        mDrawHeight = Math.min(sizeHeight - getPaddingTop() - getPaddingBottom(), cHeightSum);

        setMeasuredDimension((widthMode == MeasureSpec.EXACTLY) ? sizeWidth
                : mDrawWidth + getPaddingLeft() + getPaddingRight(), (heightMode == MeasureSpec.EXACTLY) ? sizeHeight
                : mDrawHeight + getPaddingTop() + getPaddingBottom());

        /**
         * 计算起始绘制的左顶点
         */
        mStartTop = ((getMeasuredHeight() - mDrawHeight) >> 1) - getPaddingLeft();
        mStartLeft = ((getMeasuredWidth() - mDrawWidth) >> 1) - getPaddingTop();
        if((orientation & MenuGravity.LEFT) == MenuGravity.LEFT){
            mStartLeft = 0;
        }
        else if((orientation & MenuGravity.RIGHT) == MenuGravity.RIGHT){
            mStartLeft <<= 1;
        }
        if((orientation & MenuGravity.TOP) == MenuGravity.TOP){
            mStartTop = 0;
        }
        else if((orientation & MenuGravity.BOTTOM) == MenuGravity.BOTTOM){
            mStartTop <<= 1;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //绘制的点
        int startTop = mStartTop;
        MarginLayoutParams cParams;
        for(int i = 0; i < getChildCount(); i++){
            View view = getChildAt(i);
            cParams = (MarginLayoutParams) view.getLayoutParams();
            if(view.getVisibility() == GONE){
                continue;
            }
            int cDrawWidth = view.getMeasuredWidth() + cParams.leftMargin + cParams.rightMargin;
            int cDrawHeight = view.getMeasuredHeight() + cParams.topMargin + cParams.bottomMargin;
            int cWidth = view.getMeasuredWidth();
            int cHeight = view.getMeasuredHeight();

            int cStartLeft = mStartLeft + ((mDrawWidth - cDrawWidth) >> 1);
            int cStartTop = startTop;

            if(isPlayingEnterExitAnimation){
                cStartTop += enterAnimTopDis[i];
                view.setAlpha(mSpringValue);
                if(orientation != MenuGravity.BOTTOM) {
                    view.setScaleX(mSpringValue);
                    view.setScaleY(mSpringValue);
                }
            }
            else if(mDownP != null && mLastP != null){
                int dy = mLastP.y - mDownP.y;
                int dx = mLastP.x - mDownP.x;
                int cX = cStartLeft + (cDrawWidth >> 1);
                int cY = cStartTop + (cDrawHeight >> 1);
                int dis = (int) Math.round(Math.sqrt((mLastP.x - cX)*(mLastP.x - cX) + (mLastP.y - cY)*(mLastP.y - cY)));

                if(dis != 0){
                    cStartLeft += dx * C / dis;
                    cStartTop += dy * C / dis;
                }
            }

            cStartLeft += cParams.leftMargin;
            cStartTop += cParams.topMargin;

            view.layout(cStartLeft, cStartTop, cStartLeft + cWidth, cStartTop + cHeight);
            startTop += cDrawHeight;
        }

        if(needToPlayEnterAnim){
            needToPlayEnterAnim = false;
            enterSpringController.start();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isPlayingEnterExitAnimation){
            return true;
        }
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:{
                if(isBounce){
                    removeCallbacks(bounceRunnable);
                    isBounce = false;
                    //记录之前的数值
                    if(mDownP != null) {
                        int dx = mDownP.x - mLastP.x;
                        int dy = mDownP.y - mLastP.y;
                        mLastP = new Point((int) ev.getX(), (int) ev.getY());
                        mDownP.x = mLastP.x + dx;
                        mDownP.y = mLastP.y + dy;
                    }
                }
                else {
                    mDownP = new Point((int) ev.getX(), (int) ev.getY());
                }
                return false;
            }
            case MotionEvent.ACTION_MOVE:{
                float dis = (float) Math.sqrt(
                        (ev.getX() - mDownP.x)*(ev.getX() - mDownP.x) + (ev.getY() - mDownP.y)*(ev.getY() - mDownP.y)
                );
                return dis > MIN_MOVE_DIS;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(isPlayingEnterExitAnimation){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:{
                mLastP = new Point((int)event.getX(), (int)event.getY());
                requestLayout();
                break;
            }
            case MotionEvent.ACTION_UP:{
                bounceRunnable.start();
                break;
            }
        }
        return true;
    }

    public interface OnMenuClickListener{
        /**
         * menu点击事件
         * @param view 被点击的view
         * @param index 点击的view在viewGroup中的位置
         */
        void onMenuClick(View view, int index);
    }

    float getDistance(Point a, Point b){
        if(a == null || b == null){
            return 0;
        }
        return (float) Math.sqrt((a.x - b.x)*(a.x - b.x) + (a.y - b.y)*(a.y - b.y));
    }

    class BounceRunnable implements Runnable{
        //距离小于这个值 停止播放动画
        public static final int MIN_DIS = 10;
        //每次缩放的比例
        public static final float SCALE = 0.8F;
        //动画间隔17ms 接近60帧
        public static final int PLAY_DELAY = 17;

        public void start(){
            if(isBounce){
                return;
            }
            isBounce = true;
            post(this);
        }

        @Override
        public void run() {
            float dis = getDistance(mLastP, mDownP);
            if(dis < MIN_DIS){
                mDownP = null;
                mLastP = null;
                isBounce = false;
            }
            else{
                mLastP.x = (int) ((mDownP.x*(1 - SCALE) + mLastP.x*SCALE));
                mLastP.y = (int) ((mDownP.y*(1 - SCALE) + mLastP.y*SCALE));
                postDelayed(this, PLAY_DELAY);
            }
            requestLayout();
        }
    }

    class EnterSpringController implements SpringListener{
        Spring spring;
        float[] startTopDis;

        public EnterSpringController(){
            SpringConfig springConfig = new SpringConfig(150, 14);

            spring = mSpringSystem.createSpring();
            spring.setSpringConfig(springConfig);
            spring.addListener(this);
        }

        public void start(){
            enterAnimTopDis = new int[getChildCount()];
            startTopDis = new float[getChildCount()];
            //绘制的点
            int startTop = mStartTop;
            //给速度赋值
            for(int i = 0; i < getChildCount(); i++){
                View view = getChildAt(i);
                if(view.getVisibility() == GONE){
                    continue;
                }
                int cHeight = view.getMeasuredHeight();
                enterAnimTopDis[i] = getMeasuredHeight() - startTop;
                startTopDis[i] = enterAnimTopDis[i];

                startTop += cHeight;
            }
            isPlayingEnterExitAnimation = true;
            spring.setCurrentValue(0.0);
            spring.setEndValue(1.0);
        }

        @Override
        public void onSpringUpdate(Spring spring) {
            mSpringValue = (float) spring.getCurrentValue();
            for(int i = 0; i < enterAnimTopDis.length; i++){
                enterAnimTopDis[i] = Math.round((1.0f - mSpringValue) * startTopDis[i]);
            }
            requestLayout();
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            isPlayingEnterExitAnimation = false;
        }

        @Override
        public void onSpringActivate(Spring spring) {
        }

        @Override
        public void onSpringEndStateChange(Spring spring) {
        }
    }

    public static class MenuGravity{
        public static int CENTER = 0;
        public static int TOP = 1;
        public static int BOTTOM = 2;
        public static int LEFT = 4;
        public static int RIGHT = 8;
    }
}
