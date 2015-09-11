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
 * 确认或者退出选择项的dialog fragment
 * Created by Dog on 2015/9/11.
 */
@EFragment(R.layout.fragment_dialog_check_or_close)
public class CheckOrCloseDialogFragment extends BaseDialogFragment{
    @ViewById
    TextView tv_content;
    @FragmentArg
    String content;

    OnMenuClickListener clickListener;

    /**
     * 设置内容
     * @param content
     */
    public void setContent(String content){
        this.content = content;
    }

    public void setOnMenuCheckClickListener(OnMenuClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Click({R.id.fat_close, R.id.fat_check})
    void onMenuClick(View view){
        dismiss();
        switch (view.getId()){
            case R.id.fat_check:{
                if(clickListener != null){
                    clickListener.onMenuCheckClick();
                }
                break;
            }
            case R.id.fat_close:{
                if(clickListener != null){
                    clickListener.onMenuCloseClick();
                }
                break;
            }
        }
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
        tv_content.setText(content);
        tv_content.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
    }

    public interface OnMenuClickListener{
        void onMenuCheckClick();
        void onMenuCloseClick();
    }
}