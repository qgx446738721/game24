package voiddog.org.game24.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import voiddog.org.game24.R;
import voiddog.org.game24.util.TypefaceHelper;
import voiddog.org.game24.util.UIHandler;

/**
 * 主页head view
 * Created by Dog on 2015/9/9.
 */
public class MainHeadView extends FrameLayout implements Runnable{
    //帧数 48ms/帧
    private final long DELAY = 60l;

    BallItem[] ball;
    TextView title;
    //ball数值
    int values[] = {29, 8, 10, 12, 14};
    //第一次加载
    boolean isFirstLoad = true;
    //弹簧动画
    Spring spring;
    //text 退出动画
    ValueAnimator textExitAnim = new ValueAnimator();

    //线程是否继续运行
    boolean mIsThreadRunning = false;
    //退出call back
    OnExitCallBack exitCallBack;

    public MainHeadView(Context context) {
        super(context);
        init();
    }

    public MainHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 播放退出动画
     */
    public void playExitAnimation(OnExitCallBack onExitCallBack){
        this.exitCallBack = onExitCallBack;

        int width = getMeasuredWidth();

        int endX[] = new int[5];
        int endY[] = new int[5];

        endX[0] = width >> 1;
        endY[0] = -ball[0].getSize();

        endX[3] = width - (ball[3].getSize()<<1);
        endY[3] = -ball[3].getSize();

        endX[4] = width;
        endY[4] = -ball[4].getSize();

        endX[1] = ball[1].getSize();
        endY[1] = -ball[1].getSize();

        endX[2] = -ball[2].getSize();
        endY[2] = -ball[2].getSize();

        for(int i = 0; i < ball.length; i++){
            ball[i].setVisibility(VISIBLE);
            ball[i].setMarginPoint(endX[i], endY[i], ball[i].getColorByValue(ball[i].getValue()));
        }

        textExitAnim.start();
    }

    /**
     * 开始线程
     */
    public void startThread(){
        if(mIsThreadRunning){
            mIsThreadRunning = false;

            UIHandler.sendEmptyMessageDelayed(0, 500, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    createAndRunThread();
                    return true;
                }
            });
        }
        else{
            createAndRunThread();
        }
    }

    /**
     * 结束线程
     */
    public void stopThread(){
        mIsThreadRunning = false;
    }

    void createAndRunThread(){
        Thread thread = new Thread(this);
        mIsThreadRunning = true;
        thread.start();
    }

    /**
     * 初始化
     */
    void init(){
        SpringSystem springSystem = SpringSystem.create();
        spring = springSystem.createSpring();
        spring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(25, 6));
        spring.addListener(new TextExpandingSpring());

        textExitAnim.setFloatValues(1.0f, 0.0f);
        textExitAnim.setDuration(300);
        textExitAnim.setInterpolator(new AccelerateInterpolator());
        textExitAnim.addUpdateListener(new TextExitAnimListener());

        ball = new BallItem[5];
        title = new TextView(getContext());

        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.main_title_text_size));
        title.setTextColor(Color.WHITE);
        title.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/plurp.ttf"));
        title.setText("24");

        for(int i = ball.length - 1; i >= 0; i--){
            ball[i] = new BallItem(getContext());
            ball[i].setValue(values[i]);
            ball[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
            ball[i].setImageResource(R.drawable.ball);
        }

        for(int i = ball.length - 1; i >= 0; i--){
            addView(
                    ball[i],
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            ball[i].setVisibility(INVISIBLE);
            ball[i].setDie();
        }
        addView(
                title,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        title.setVisibility(INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(isFirstLoad){
            isFirstLoad = false;
            UIHandler.sendEmptyMessageDelayed(0, 300, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    playEnterAnimation();
                    return true;
                }
            });

            UIHandler.sendEmptyMessageDelayed(0, 500, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    title.setVisibility(VISIBLE);
                    title.setPivotX(title.getMeasuredWidth() / 2);
                    title.setPivotY(title.getMeasuredHeight()/2);
                    title.setX(getMeasuredWidth() / 2 - title.getMeasuredWidth()/2);
                    title.setY(getMeasuredHeight()/2 - title.getMeasuredHeight()/2);
                    spring.setCurrentValue(0);
                    spring.setEndValue(1);
                    return true;
                }
            });
        }
    }

    /**
     * 播放动画
     */
    void playEnterAnimation(){
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int startX[] = new int[5];
        int startY[] = new int[5];
        int endX[] = new int[5];
        int endY[] = new int[5];

        startX[0] = width >> 1;
        startY[0] = height - ball[0].getSize();

        startX[1] = 0;
        startY[1] = height >> 1;

        startX[2] = 0;
        startY[2] = height - ball[2].getSize();

        startX[3] = width - ball[3].getSize();
        startY[3] = height >> 1;

        startX[4] = width - ball[4].getSize();
        startY[4] = height - ball[4].getSize();

        width >>= 1;
        height >>= 1;

        endX[0] = width - (ball[0].getSize()>>1);
        endY[0] = height - (ball[0].getSize()>>1);

        endX[1] = endX[0] - (ball[1].getSize()>>1);
        endY[1] = height - ball[1].getSize()*3/4;

        endX[2] = endX[0] - (ball[2].getSize()>>1);
        endY[2] = height;

        endX[3] = width + (ball[0].getSize()>>1) - (ball[3].getSize()>>1);
        endY[3] = height - ball[3].getSize()*3/4;

        endX[4] = width + (ball[0].getSize()>>1) - (ball[4].getSize()>>1);
        endY[4] = height;

        for(int i = 0; i < ball.length; i++){
            ball[i].setVisibility(VISIBLE);
            ball[i].setX(startX[i]);
            ball[i].setY(startY[i]);
            ball[i].playAppear();
            ball[i].setSeparationPoint(endX[i], endY[i], ball[i].getColorByValue(ball[i].getValue()));
        }
    }

    @Override
    public void run() {
        while(mIsThreadRunning){
            try {
                UIHandler.sendEmptyMessage(0, new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        for(int i = getChildCount() - 1; i >= 0; i--){
                            View view = getChildAt(i);
                            if(view instanceof NumberItem){
                                NumberItem numberItem = (NumberItem) view;
                                numberItem.update();
                            }
                        }
                        return true;
                    }
                });
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class TextExpandingSpring extends SimpleSpringListener{
        @Override
        public void onSpringUpdate(Spring spring) {
            title.setScaleY((float) spring.getCurrentValue());
            title.setScaleX((float) spring.getCurrentValue());
        }
    }

    class TextExitAnimListener implements ValueAnimator.AnimatorUpdateListener{

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            title.setScaleX(value);
            title.setScaleY(value);
            if(value == 0.0f && exitCallBack != null){
                exitCallBack.onExit();
            }
        }
    }

    public interface OnExitCallBack{
        void onExit();
    }
}
