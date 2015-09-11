package voiddog.org.game24.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import voiddog.org.game24.R;

/**
 * 自定义头部bar
 * Created by Dog on 2015/5/8.
 */
@EViewGroup(R.layout.ui_title_bar)
public class TitleBar extends FrameLayout{

    @ViewById
    FontAwesomeText fat_left_icon, fat_right_icon;
    @ViewById
    TextView tv_left_text, tv_title, tv_right_text;
    @ViewById
    LinearLayout lin_btn_left, lin_btn_right;

    int leftIconColor, rightIconColor, bg, titleColor;
    String leftText, leftIcon, rightText, rightIcon, title;

    public TitleBar(Context context) {
        this(context, null, 0);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.TitleBar, defStyleAttr, 0);

        leftIconColor = a.getColor(R.styleable.TitleBar_leftColor, 0xffffffff);
        leftIcon = a.getString(R.styleable.TitleBar_leftIcon);
        leftText = a.getString(R.styleable.TitleBar_leftText);

        rightIconColor = a.getColor(R.styleable.TitleBar_rightColor, 0xffffffff);
        rightIcon = a.getString(R.styleable.TitleBar_rightIcon);
        rightText = a.getString(R.styleable.TitleBar_rightText);

        bg = a.getColor(R.styleable.TitleBar_titleBg, getResources().getColor(R.color.white));
        title = a.getString(R.styleable.TitleBar_titleText);
        titleColor = a.getColor(R.styleable.TitleBar_titleColor, 0xffffffff);

        a.recycle();
    }

    @AfterViews
    void initView(){
        //左边
        fat_left_icon.setTextColor(leftIconColor);
        tv_left_text.setTextColor(leftIconColor);
        if(leftIcon != null){
            fat_left_icon.setVisibility(VISIBLE);
            fat_left_icon.setIcon(leftIcon);
        }
        if(leftText != null){
            tv_left_text.setText(leftText);
        }

        //中间
        tv_title.setTextColor(titleColor);
        tv_title.setText(title);
        setBackgroundColor(bg);

        //右边
        fat_right_icon.setTextColor(rightIconColor);
        tv_right_text.setTextColor(rightIconColor);
        if(rightIcon != null){
            fat_right_icon.setVisibility(VISIBLE);
            fat_right_icon.setIcon(rightIcon);
        }
        if(rightText != null){
            tv_right_text.setText(rightText);
        }
    }

    /**
     * 设置点击左边按钮事件
     * @param clickListener 点击接口
     */
    public void setOnLeftClickListener(OnClickListener clickListener){
        lin_btn_left.setOnClickListener(clickListener);
    }

    /**
     * 设置点击右边按钮事件
     * @param clickListener 点击接口
     */
    public void setOnRightClickListener(OnClickListener clickListener){
        lin_btn_right.setOnClickListener(clickListener);
    }

    /**
     * 设置左边按钮的text
     * @param text 内容
     */
    public void setLeftText(String text){
        tv_left_text.setText(text);
    }

    /**
     * 设置左边按钮的图标
     * @param icon 图标编号 参见http://fortawesome.github.io/Font-Awesome/icons/
     *             例: fa-arrow-left
     */
    public void setLeftIcon(String icon){
        fat_left_icon.setVisibility(VISIBLE);
        fat_left_icon.setIcon(icon);
    }

    /**
     * 设置右边按钮的text
     * @param text 内容
     */
    public void setRightText(String text){
        tv_right_text.setText(text);
    }

    /**
     * 设置右边按钮的icon
     * @param icon 图标编号 参见http://fortawesome.github.io/Font-Awesome/icons/
     *             例: fa-arrow-left
     */
    public void setRightIcon(String icon){
        fat_right_icon.setVisibility(VISIBLE);
        fat_right_icon.setIcon(icon);
    }

    /**
     * 这只标题内容
     * @param text 内容
     */
    public void setTitle(String text){
        tv_title.setText(text);
    }

    /**
     * 设置标题内容
     */
    public void setTitle(Spanned spanned){
        tv_title.setText(spanned);
    }
}
