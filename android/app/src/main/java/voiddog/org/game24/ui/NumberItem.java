package voiddog.org.game24.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.util.Random;

import de.greenrobot.event.EventBus;
import voiddog.org.game24.data.OperationEnum;
import voiddog.org.game24.event.MarginItemFinishEvent;

/**
 * 数字item
 * Created by Dog on 2015/9/6.
 */
public class NumberItem extends ImageView
        implements ValueAnimator.AnimatorUpdateListener, SpringListener{
    final int MAX_RANDOM_VELOCITY = 2;
    final int MARGIN_ANIM_TIME = 300;
    //对应数值的颜色
    final int valueToColor[] = {
            0xffF9DA01, 0xffF5C300, 0xffEC9A04, 0xffED6C01, 0xffE8002D, 0xffA2008B, 0xff531D82, 0xff0E4FA5, 0xff0568AB, 0xff0FA1AC, 0xff15A632, 0xffACC60B
    };

    /**
     * 速度数据结构
     */
    class Velocity{
        /**
         * x 轴方向速度和y轴方向速度
         */
        float x, y;

        Velocity(){}
        Velocity(float x, float y){this.x = x; this.y = y;}
    }

    /**
     * item的状态
     */
    public enum Status{
        NORMAL,     //普通状态
        PADDING,    //等待匹配状态
        MARGIN,     //合并状态
        SEPARA,     //分离
        DIE         //死亡状态，表示item不在随机运动，停止，即将被销毁
    }

    //物体的具体速度
    Velocity mV = new Velocity(0, 0);
    //item状态
    Status mStatus = Status.NORMAL;
    //边界
    Rect mRect = new Rect(0, 0, 0, 0);
    //手势中
    boolean mIsDrag = false;
    //随机事件
    Random mRandom = new Random();
    //合并点，开始合并的时候的点
    float marginX, marginY, startX, startY;
    //合并后的颜色
    int marginColor, startColor;
    //合并动画
    ValueAnimator mMovingAnimator = new ValueAnimator();
    //出现动画
    Spring mSpring;
    //数值
    int mValue = 1;
    String mStringValue = "1";
    //是否需要create新的item
    boolean mNeedCreateNew = false;
    //尺寸大小
    int mSize = 0;
    //操作设置
    OperationEnum mOperation;
    //被合并者
    NumberItem mMarginNumber;
    //是否是展示状态
    boolean mIsAppear = false;
    //背景圆
    GradientDrawable mCircle;
    //padding圆
    GradientDrawable mPaddingCircle;
    Paint mTextPaint;
    //球的最大大小 150dp
    int MAX_SIZE = 150;

    public NumberItem(Context context) {
        super(context);

        init();
    }

    /**
     * 播放出现动画
     */
    public void playAppear(){
        mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40, 6));
        mSpring.setCurrentValue(0.0);
        mSpring.setEndValue(1.0);
        mIsAppear = true;
    }

    /**
     * 播放消失动画
     */
    public void playDisappearAndRemove(){
        //标记为-1 表示没用了
        setTag(-1);
        mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40, 7));
        mSpring.setCurrentValue(1.0);
        mSpring.setEndValue(0.0);
        mIsAppear = false;
    }

    /**
     * 设置成普通模式
     */
    public void setToNormal(){
        if(mStatus == Status.NORMAL){
            return;
        }
        mStatus = Status.NORMAL;
        setScaleX(1);
        setScaleY(1);

        setColor(getColorByValue(mValue));
    }

    /**
     * 根据value获取color
     * @param value 数值
     * @return 对应的颜色
     */
    public int getColorByValue(int value){
        int colorIndex = value % (valueToColor.length);
        return valueToColor[colorIndex];
    }

    /**
     * item 死亡
     */
    public void setDie(){
        if(mStatus == Status.DIE){
            return;
        }
        mStatus = Status.DIE;
        MarginItemFinishEvent event = new MarginItemFinishEvent();
        event.currentItem = this;
        EventBus.getDefault().post(event);

        invalidate();
    }

    /**
     * 设置为正在等待碰撞对象状态
     */
    public void setToPadding(){
        if(mStatus == Status.PADDING){
            return;
        }
        mStatus = Status.PADDING;

        invalidate();
    }

    /**
     * 是否在合并后需要重新创建一个item
     * @param needCreateNew 是否需要新建item
     */
    public void setNeedCreateNew(boolean needCreateNew){
        mNeedCreateNew = needCreateNew;
    }

    /**
     * 是否需要重新创建一个item
     */
    public boolean isNeedCreateNew(){
        return mNeedCreateNew;
    }

    /**
     * 设置value
     */
    public void setValue(int value){
        mValue = value;
        mStringValue = Integer.toString(mValue);
        mSize = dp2px(getContext(), value*3 + 60);

        if(mSize > MAX_SIZE){
            mSize = MAX_SIZE;
        }

        setColor(getColorByValue(value));

        mPaddingCircle.setGradientRadius(mSize);

        mTextPaint.setColor(0xffffffff);
        Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        mTextPaint.setTypeface(font);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mSize / 3);
        invalidate();
    }

    /**
     * 设置颜色
     */
    public void setColor(int color){
        startColor = color;
        mCircle.setColor(startColor);
        int colors[] = {
                startColor,
                startColor&0x00ffffff,
                0x00ffffff
        };
        mPaddingCircle.setColors(colors);
        invalidate();
    }

    /**
     * 获取数值
     * @return item数值
     */
    public int getValue(){
        return mValue;
    }

    /**
     * 获取到item的size
     * @return item的大小
     */
    public int getSize(){
        return mSize;
    }

    /**
     * 设置合并的点
     * @param x 合并点的x坐标
     * @param y 合并点的y坐标
     */
    public void setMarginPoint(float x, float y, int marginColor){
        marginX = x;
        marginY = y;
        this.marginColor = marginColor;
        mV.x = 0;
        mV.y = 0;
        startX = getX();
        startY = getY();

        mStatus = Status.MARGIN;

        mMovingAnimator.setInterpolator(new AccelerateInterpolator());
        playMovingAnimation();
    }

    public void setSeparationPoint(float x, float y, int color){
        marginX = x;
        marginY = y;
        marginColor = color;
        mV.x = 0;
        mV.y = 0;
        startX = getX();
        startY = getY();

        mStatus = Status.SEPARA;

        mMovingAnimator.setInterpolator(new DecelerateInterpolator());
        playMovingAnimation();
    }

    public void setOperation(OperationEnum operation, NumberItem numberItem){
        mOperation = operation;
        mMarginNumber = numberItem;
    }
    
    public OperationEnum getOperation(){
        return mOperation;
    }

    public NumberItem getMarginItem(){
        return mMarginNumber;
    }

    /**
     * 返回状态
     * @return item状态
     */
    public Status getStatus(){
        return mStatus;
    }

    public void setIsDrag(boolean drag){
        mIsDrag = drag;
    }

    /**
     * 刷新ui函数，更新item的状态
     */
    public void update(){
        if(mIsDrag){
            setToNormal();
            return;
        }

        switch (mStatus){
            case PADDING:{
                randomVelocity();

                this.setX(this.getX() + mV.x);
                this.setY(this.getY() + mV.y);
            }
            case NORMAL:{
                randomVelocity();

                this.setX(this.getX() + mV.x);
                this.setY(this.getY() + mV.y);
                break;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mSize, mSize);
        //设置缩放中心
        setPivotY(getMeasuredHeight() >> 1);
        setPivotX(getMeasuredWidth() >> 1);
        //设置移动区域
        mRect.right = MeasureSpec.getSize(widthMeasureSpec);
        mRect.bottom = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBelow(canvas);
        super.onDraw(canvas);
        drawAbove(canvas);
    }

    /**
     * 在image底部draw
     */
    void drawBelow(Canvas canvas){
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        mCircle.setBounds(
                getPaddingLeft(), getPaddingTop(),
                width - getPaddingRight(),
                height - getPaddingBottom()
        );

        mCircle.draw(canvas);

        if(mStatus == Status.PADDING
                || mStatus == Status.MARGIN) {
            width = getMeasuredWidth();
            height = getMeasuredHeight();

            mPaddingCircle.setBounds(0, 0, width, height);
            mPaddingCircle.draw(canvas);
        }

        width = getMeasuredWidth();
        height = getMeasuredHeight();
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float fontHeight = fm.bottom - fm.top;
        float fontWidth = mTextPaint.measureText(mStringValue);
        int baseX = (int) ((width - fontWidth)/2);
        int baseY = (int) ((height - fontHeight)/2 + fontHeight - fm.descent);
        canvas.drawText(mStringValue, baseX, baseY, mTextPaint);
    }

    /**
     * 在image前部draw
     */
    void drawAbove(Canvas canvas){}

    /**
     * 初初始化
     */
    void init(){
        //初始化 circle
        mCircle = new GradientDrawable();
        mCircle.setShape(GradientDrawable.OVAL);
        mCircle.setAlpha(200);
        mPaddingCircle = new GradientDrawable();
        mPaddingCircle.setShape(GradientDrawable.OVAL);
        mPaddingCircle.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        //初始化画笔
        mTextPaint = new Paint();

        mMovingAnimator.setDuration(MARGIN_ANIM_TIME);
        mMovingAnimator.setFloatValues(0.0f, 1.0f);
        mMovingAnimator.addUpdateListener(this);

        MAX_SIZE = dp2px(getContext(), MAX_SIZE);

        SpringSystem springSystem = SpringSystem.create();
        mSpring = springSystem.createSpring();
        mSpring.addListener(this);

        int padding = dp2px(getContext(), 10);
        setPadding(padding, padding, padding, padding);
    }

    /**
     * 播放合并动画
     */
    void playMovingAnimation(){
        mMovingAnimator.start();
    }

    /**
     * 产生一个随机的速度
     */
    void randomVelocity(){
        if(mRect.left == 0 && mRect.right == 0){
            mV.x = 0;
            mV.y = 0;
        }
        else{
            int measureWidth = getMeasuredWidth();
            int measureHeight = getMeasuredHeight();

            float vx = mV.x;
            float vy = mV.y;

            // 1/6的概率改变速度
            if(mRandom.nextInt(6) < 1){
                vx = mRandom.nextFloat() * MAX_RANDOM_VELOCITY;
                vy = mRandom.nextFloat() * MAX_RANDOM_VELOCITY;
            }

            float aimX = getX() + vx;
            if(aimX < mRect.left || (aimX + measureWidth) > mRect.right){
                vx *= -1;
            }

            float aimY = getY() + vy;
            if(aimY < mRect.top || (aimY + measureHeight) > mRect.bottom){
                vy *= -1;
            }

            mV.x = vx;
            mV.y = vy;
        }
    }

    /**
     * ValueAnimation
     */
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if(mStatus == Status.MARGIN
                || mStatus == Status.SEPARA){
            float value = (float) animation.getAnimatedValue();
            this.setX(value*(marginX - startX) + startX);
            this.setY(value*(marginY - startY) + startY);

            int r, g, b, rr, gg, bb;

            rr = Color.red(marginColor);
            gg = Color.green(marginColor);
            bb = Color.blue(marginColor);
            r = Color.red(startColor);
            g = Color.green(startColor);
            b = Color.blue(startColor);

            r = (int) (value*(rr - r) + r);
            g = (int) (value*(gg - g) + g);
            b = (int) (value*(bb - b) + b);

            int finalColor = Color.rgb(r, g, b);
            mCircle.setColor(finalColor);

            int colors[] = {
                    finalColor,
                    finalColor&0x00ffffff,
                    0x00ffffff
            };
            mPaddingCircle.setColors(colors);

            invalidate();

            if(value == 1.0f){
                if(mStatus == Status.MARGIN) {
                    setDie();
                }
                else{
                    setToNormal();
                }
            }
        }
    }

    /**
     * Spring 动画
     */
    @Override
    public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        setScaleX(value);
        setScaleY(value);
    }

    @Override
    public void onSpringAtRest(Spring spring) {
        if(!mIsAppear){
            if(getParent() != null
                    && getParent() instanceof ViewGroup){
                ViewGroup viewGroup = (ViewGroup) getParent();
                viewGroup.removeView(this);
            }
        }
    }

    @Override
    public void onSpringActivate(Spring spring) {}

    @Override
    public void onSpringEndStateChange(Spring spring) {}

    /**
     * 输入dp 转换为 px
     * @param context 上下文
     * @param dp 要转换的dp的数值
     * @return dp 转换成 px 后的数值
     */
    public static int dp2px(Context context, float dp){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}