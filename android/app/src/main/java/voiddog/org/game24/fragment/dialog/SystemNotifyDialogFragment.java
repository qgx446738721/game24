package voiddog.org.game24.fragment.dialog;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.FontAwesomeText;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import voiddog.org.game24.R;
import voiddog.org.game24.util.SizeUtil;
import voiddog.org.game24.util.TypefaceHelper;

/**
 * 系统通知dialog
 * Created by Dog on 2015/6/27.
 */
@EFragment(R.layout.fragment_dialog_system_notify)
public class SystemNotifyDialogFragment extends BaseDialogFragment{
    @ViewById
    TextView tv_content;
    @ViewById
    FontAwesomeText fat_check;
    @FragmentArg
    String content;

    OnMenuSureClickListener clickListener;

    /**
     * 设置内容
     * @param content
     */
    public void setContent(String content){
        this.content = content;
    }

    public void setOnMenuCheckClickListener(OnMenuSureClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Click(R.id.fat_check)
    void onBtnClick(){
        dismiss();
        if(clickListener != null){
            clickListener.onMenuSureClick();
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

    public interface OnMenuSureClickListener{
        void onMenuSureClick();
    }
}
