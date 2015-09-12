package voiddog.org.game24.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.Button;

import com.beardedhen.androidbootstrap.FontAwesome;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import voiddog.org.game24.R;
import voiddog.org.game24.util.LogUtil;
import voiddog.org.game24.util.TypefaceHelper;

/**
 * 游戏按钮
 * Created by Dog on 2015/9/12.
 */
public class GameButton extends Button{

    int defaultColor, activeColor;
    Spring mSpring;
    ButtonState buttonState = ButtonState.HangUp;

    public GameButton(Context context) {
        super(context);
        init(null, 0);
    }

    public GameButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    /**
     * 初始化view
     */
    void init(AttributeSet attrs, int defStyleAttr){
        defaultColor = getCurrentTextColor();
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.
                        GameButton, defStyleAttr, 0);

        activeColor = a.getColor(R.styleable.GameButton_game_btn_text_active_color, defaultColor);
        String fontawesome = a.getString(R.styleable.GameButton_game_btn_fontawesome);

        if(fontawesome != null){
            setTypeface(FontAwesome.getFont(getContext()));
            setText(FontAwesome.getUnicode(fontawesome));
        }
        else{
            if(TypefaceHelper.getInstance() == null){
                TypefaceHelper.init(getContext());
            }
            setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
        }

        a.recycle();

        SpringSystem springSystem = SpringSystem.create();
        mSpring = springSystem.createSpring();
        mSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40, 3));
        mSpring.setCurrentValue(0);
        mSpring.addListener(new MySpringListener());
    }

    /**
     * 激活当前item
     */
    public void activateItem(){
        if(buttonState == ButtonState.HangUp){
            mSpring.setEndValue(0.5);
            buttonState = ButtonState.Active;
        }
    }

    /**
     * 挂起当前item
     */
    public void hangUpItem(){
        if(buttonState == ButtonState.Active){
            mSpring.setEndValue(0);
            buttonState = ButtonState.HangUp;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setPivotX(getMeasuredWidth() >> 1);
        setPivotY(getMeasuredHeight() >> 1);
    }

    enum ButtonState{
        Active,
        HangUp
    }

    class MySpringListener extends SimpleSpringListener{
        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();
            //缩放
            setScaleX(value + 1f);
            setScaleY(value + 1f);

            int r, g, b, rr, gg, bb;

            value /= 0.5f;

            if(value < 0){
                value = 0;
            }

            if(value > 1){
                value = 1;
            }

            rr = Color.red(activeColor);
            gg = Color.green(activeColor);
            bb = Color.blue(activeColor);
            r = Color.red(defaultColor);
            g = Color.green(defaultColor);
            b = Color.blue(defaultColor);

            r = (int) (value*(rr - r) + r);
            g = (int) (value*(gg - g) + g);
            b = (int) (value*(bb - b) + b);

            //设置颜色
            int finalColor = Color.rgb(r, g, b);
            setTextColor(finalColor);
        }
    }
}
