package voiddog.org.game24.ui;

import android.content.Context;
import android.graphics.Canvas;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.util.Random;

/**
 * 球体，没有数字
 * Created by Dog on 2015/9/9.
 */
public class BallItem extends NumberItem{
    //最大移动距离10dp
    private int MIN_MOVE_DIS = 20;

    Spring mBallMovingSpring;

    public BallItem(Context context) {
        super(context);
    }

    @Override
    public void setToNormal() {
        super.setToNormal();
        startX = getX();
        startY = getY();
    }

    @Override
    public void setDie() {
        if(mStatus == Status.DIE){
            return;
        }
        mStatus = Status.DIE;

        invalidate();
    }

    @Override
    void init() {
        super.init();

        mCircle.setAlpha(255);
        MIN_MOVE_DIS = dp2px(getContext(), MIN_MOVE_DIS);

        SpringSystem springSystem = SpringSystem.create();
        mBallMovingSpring = springSystem.createSpring();
        Random random = new Random();
        mBallMovingSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(11, 4 + (1 - random.nextInt(3))));
        mBallMovingSpring.addListener(new BallMovingSpringListener());
    }

    @Override
    public void playAppear() {
        Random random = new Random();
        mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(11, 4 + (1 - random.nextInt(3))));
        mSpring.setCurrentValue(0.0);
        mSpring.setEndValue(1.0);
        mIsAppear = true;
    }

    @Override
    void playMovingAnimation() {
        mBallMovingSpring.setCurrentValue(0.0f);
        mBallMovingSpring.setEndValue(1.0f);
    }

    @Override
    void drawBelow(Canvas canvas) {
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
    }

    @Override
    void randomVelocity() {
        if(mRect.left == 0 && mRect.right == 0){
            mV.x = 0;
            mV.y = 0;
        }
        else{
            int measureWidth = getMeasuredWidth();
            int measureHeight = getMeasuredHeight();

            float vx = mV.x;
            float vy = mV.y;

            // 1/10的概率改变速度
            if(mRandom.nextInt(10) < 1){
                vx = mRandom.nextFloat();
                vy = mRandom.nextFloat();
            }

            float aimX = getX() + vx;
            if(aimX < mRect.left || (aimX + measureWidth) > mRect.right
                    || Math.abs(aimX - startX) > MIN_MOVE_DIS){
                vx *= -0.5;
            }

            float aimY = getY() + vy;
            if(aimY < mRect.top || (aimY + measureHeight) > mRect.bottom
                    || Math.abs(aimY - startY) > MIN_MOVE_DIS){
                vy *= -0.5;
            }

            mV.x = vx;
            mV.y = vy;
        }
    }

    class BallMovingSpringListener implements SpringListener{

        @Override
        public void onSpringUpdate(Spring spring) {
            if(mStatus == Status.MARGIN
                    || mStatus == Status.SEPARA){
                float value = (float) spring.getCurrentValue();
                setX(value*(marginX - startX) + startX);
                setY(value * (marginY - startY) + startY);

                invalidate();
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {
            setToNormal();
        }

        @Override
        public void onSpringActivate(Spring spring) {}

        @Override
        public void onSpringEndStateChange(Spring spring) {}
    }
}
