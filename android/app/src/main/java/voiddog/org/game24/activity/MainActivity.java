package voiddog.org.game24.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

import voiddog.org.game24.R;
import voiddog.org.game24.ui.MainHeadView;
import voiddog.org.game24.util.SizeUtil;
import voiddog.org.game24.util.UIHandler;

@Fullscreen
@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity{

    @ViewById
    LinearLayout lin_menu;
    @ViewById
    MainHeadView main_head;
    @ViewById
    Button rcb_start;

    //按钮移动动画
    Spring mMovingSpring;
    MenuMovingController menuMovingController = new MenuMovingController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpringSystem springSystem = SpringSystem.create();
        mMovingSpring = springSystem.createSpring();
        mMovingSpring.setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(25, 6));
        mMovingSpring.addListener(menuMovingController);
    }

    @Override
    protected void onResume() {
        super.onResume();
        main_head.startThread();
    }

    @Override
    protected void onStop() {
        super.onStop();
        main_head.stopThread();
    }

    @AfterViews
    void setupViews(){

        //300ms后调用
        lin_menu.setVisibility(View.INVISIBLE);
        lin_menu.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lin_menu.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                UIHandler.sendEmptyMessageDelayed(0, 1000, new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        playMoveButtonAnim();
                        return true;
                    }
                });
            }
        });
    }

    /**
     * 开始播放按钮移动动画
     */
    void playMoveButtonAnim(){
        lin_menu.setVisibility(View.VISIBLE);

        menuMovingController.startX = (SizeUtil.getScreenWidth(this) - lin_menu.getMeasuredWidth())/2.0f;
        menuMovingController.startY = SizeUtil.getScreenHeight(this);
        menuMovingController.endX = lin_menu.getX();
        menuMovingController.endY = lin_menu.getY();

        mMovingSpring.setCurrentValue(0);
        mMovingSpring.setEndValue(1);
    }

    @Click(R.id.rcb_start)
    void startGame(){
        //只能点击一次
        rcb_start.setOnClickListener(null);
        main_head.playExitAnimation(new MainHeadView.OnExitCallBack() {
            @Override
            public void onExit() {
                GameActivity_.intent(MainActivity.this).start();
                finish();
            }
        });
    }

    class MenuMovingController extends SimpleSpringListener{

        public float endX, endY, startX, startY;

        @Override
        public void onSpringUpdate(Spring spring) {
            float value = (float) spring.getCurrentValue();

            lin_menu.setX(startX + (endX - startX)*value);
            lin_menu.setY(startY + (endY - startY)*value);
        }
    }
}
