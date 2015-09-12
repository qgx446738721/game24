package voiddog.org.game24.fragment.dialog;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import voiddog.org.game24.R;
import voiddog.org.game24.util.SizeUtil;
import voiddog.org.game24.util.TypefaceHelper;

/**
 * 游戏结束dialog
 * Created by Dog on 2015/9/11.
 */
@EFragment(R.layout.fragment_dialog_game_over)
public class GameOverDialogFragment extends BaseDialogFragment{
    @ViewById
    TextView tv_game_over, tv_anw;
    @FragmentArg
    String answer;

    //按钮点击事件
    OnMenuClick menuClick;

    /**
     * 设置按钮点击事件
     */
    public void setOnMenuClickListener(OnMenuClick clickListener){
       this.menuClick = clickListener;
    }

    public void setAnswer(String anw){
        this.answer = anw;
    }

    @Override
    public void onStart() {
        //全屏显示
        Window window = getDialog().getWindow();
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT
                , SizeUtil.getScreenHeight(getActivity()) - SizeUtil.dp2px(getActivity(), 25)
        );
        super.onStart();
        setupViews();
    }

    void setupViews(){
        setCancelable(false);
        tv_game_over.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/plurp.ttf"));
        tv_anw.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
        if(answer == null){
            tv_anw.setText("无解");
        }
        else{
            tv_anw.setText(answer);
        }
    }

    @Click({R.id.rcb_once_more, R.id.rcb_back_to_home})
    void onButtonClick(View view){
        dismiss();
        switch (view.getId()){
            case R.id.rcb_once_more:{
                if(menuClick != null){
                    menuClick.onRestartClick();
                }
                break;
            }
            case R.id.rcb_back_to_home:{
                if(menuClick != null){
                    menuClick.onBackToHomeClick();
                }
                break;
            }
        }
    }

    /**
     * 按钮点击事件
     */
    public interface OnMenuClick{
        /**
         * 重新开始点击事件
         */
        void onRestartClick();

        /**
         * 返回主页面点击事件
         */
        void onBackToHomeClick();
    }
}
