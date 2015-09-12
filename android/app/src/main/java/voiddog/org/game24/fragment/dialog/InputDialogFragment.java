package voiddog.org.game24.fragment.dialog;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

import de.greenrobot.event.EventBus;
import voiddog.org.game24.R;
import voiddog.org.game24.event.UserInfoUpdateEvent;
import voiddog.org.game24.net.HttpClientManager;
import voiddog.org.game24.net.request.RegisterCmd;
import voiddog.org.game24.net.response.RegisterUserResponse;
import voiddog.org.game24.util.SizeUtil;
import voiddog.org.game24.util.TypefaceHelper;
import voiddog.org.game24.util.UserHelper;

/**
 * 输入dialog
 * Created by Dog on 2015/9/12.
 */
@EFragment(R.layout.fragment_dialog_input)
public class InputDialogFragment extends BaseDialogFragment{
    @ViewById
    TextView tv_title, tv_msg;
    @ViewById
    EditText et_input_name;
    @ViewById
    ProgressWheel circle_progress;
    @AnimationRes
    Animation shake;

    @Click({R.id.fat_close, R.id.fat_check})
    void onMenuClick(View view){
        switch (view.getId()){
            case R.id.fat_close:{
                dismiss();
                break;
            }
            case R.id.fat_check:{
                String name = et_input_name.getText().toString();
                if(TextUtils.isEmpty(name)){
                    et_input_name.startAnimation(shake);
                    return;
                }

                circle_progress.setVisibility(View.VISIBLE);
                tv_msg.setVisibility(View.GONE);

                HttpClientManager.getInstance().post(new RegisterCmd(name), new HttpClientManager.HttpCallback() {
                    @Override
                    public void onSuccess(String msg, String data) {
                        circle_progress.setVisibility(View.GONE);
                        Gson gson = new Gson();
                        RegisterUserResponse userResponse = gson.fromJson(data, new TypeToken<RegisterUserResponse>(){}.getType());
                        UserHelper.getInstance().setUser(userResponse.id, userResponse.name);
                        EventBus.getDefault().post(new UserInfoUpdateEvent());
                        dismiss();
                    }

                    @Override
                    public void onFailure(int code, String msg, String data) {
                        circle_progress.setVisibility(View.GONE);
                        tv_msg.setVisibility(View.VISIBLE);
                        tv_msg.setText(msg);
                    }
                });

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
        setCancelable(false);
        tv_title.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
        tv_msg.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
    }
}
