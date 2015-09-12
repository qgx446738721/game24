package voiddog.org.game24.activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Fullscreen;

import de.greenrobot.event.EventBus;
import voiddog.org.game24.R;
import voiddog.org.game24.data.GameMode;
import voiddog.org.game24.event.GameClearEvent;
import voiddog.org.game24.event.AskGameExitEvent;
import voiddog.org.game24.event.GameOverEvent;
import voiddog.org.game24.fragment.GameFragment;
import voiddog.org.game24.fragment.GameFragment_;
import voiddog.org.game24.fragment.dialog.CheckOrCloseDialogFragment;
import voiddog.org.game24.fragment.dialog.CheckOrCloseDialogFragment_;
import voiddog.org.game24.fragment.dialog.GameCongraDialogFragment;
import voiddog.org.game24.fragment.dialog.GameCongraDialogFragment_;
import voiddog.org.game24.fragment.dialog.GameOverDialogFragment;
import voiddog.org.game24.fragment.dialog.GameOverDialogFragment_;
import voiddog.org.game24.net.HttpClientManager;
import voiddog.org.game24.net.request.BaseCmd;
import voiddog.org.game24.net.request.UploadNervousScoreCmd;
import voiddog.org.game24.net.request.UploadRelaxModeScoreCmd;
import voiddog.org.game24.util.LogUtil;
import voiddog.org.game24.util.UserHelper;

/**
 * 游戏activity
 * Created by Dog on 2015/9/7.
 */
@Fullscreen
@EActivity(R.layout.activity_game)
public class GameActivity extends BaseActivity{

    @Extra
    GameMode mGameMode = GameMode.Nervous;

    GameFragment gameFragment;
    GameOverDialogFragment gameOverDialog;
    GameCongraDialogFragment congraDialog;
    CheckOrCloseDialogFragment askGameExitDialog;
    //我的分数
    int myScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameFragment = GameFragment_.builder().build();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void finish() {
        MainActivity_.intent(this).start();
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_to_bottom);
        super.finish();
    }

    @AfterViews
    void setupViews(){
        addFragment(gameFragment);
        setupGameOverDialog();
        setupGameClearDialog();
        setupAskGameExitDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            gameFragment.backStep();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 接收到游戏结束事件
     */
    public void onEventMainThread(GameOverEvent event){
        myScore += event.score;
        if(congraDialog.isAdded()) {
            congraDialog.dismiss();
        }
        if(askGameExitDialog.isAdded()) {
            askGameExitDialog.dismiss();
        }
        gameOverDialog.setAnswer(event.anw);
        gameOverDialog.show(getFragmentManager(), gameFragment.getClass().getName());
    }

    /**
     * 接收到游戏解出
     */
    public void onEventMainThread(GameClearEvent event){
        myScore += event.score;
        congraDialog.show(getFragmentManager(), congraDialog.getClass().getName());
    }

    /**
     * 接收到请求游戏退出
     */
    public void onEventMainThread(AskGameExitEvent exitEvent){
        askGameExitDialog.show(getFragmentManager(), askGameExitDialog.getClass().getName());
    }

    /**
     * 设置游戏结束dialog
     */
    void setupGameOverDialog(){
        gameOverDialog = GameOverDialogFragment_.builder().build();

        gameOverDialog.setOnMenuClickListener(new GameOverDialogFragment.OnMenuClick() {
            @Override
            public void onRestartClick() {
                uploadScore();
                restartGame();
            }

            @Override
            public void onBackToHomeClick() {
                uploadScore();
                finish();
            }
        });
    }

    /**
     * 设置游戏解答出的dialog
     */
    void setupGameClearDialog(){
        congraDialog = GameCongraDialogFragment_.builder().build();

        congraDialog.setOnMenuClickListener(new GameCongraDialogFragment.OnMenuClick() {
            @Override
            public void onNextLevelClick() {
                nextLevel();
            }

            @Override
            public void onBackToHomeClick() {
                uploadScore();
                finish();
            }
        });
    }

    void setupAskGameExitDialog(){
        askGameExitDialog = CheckOrCloseDialogFragment_.builder()
                .content("确定退出当前游戏")
                .build();
        askGameExitDialog.setOnMenuCheckClickListener(new CheckOrCloseDialogFragment.OnMenuClickListener() {
            @Override
            public void onMenuCheckClick() {
                uploadScore();
                finish();
            }

            @Override
            public void onMenuCloseClick() {}
        });
    }

    /**
     * 提交成绩
     */
    void uploadScore(){
        if(UserHelper.getInstance().isHasUser()) {
            BaseCmd cmd;
            if(mGameMode == GameMode.Relax) {
                cmd = new UploadRelaxModeScoreCmd(
                        UserHelper.getInstance().getUid(),
                        myScore
                );
            }
            else {
                cmd = new UploadNervousScoreCmd(
                        UserHelper.getInstance().getUid(),
                        myScore
                );
            }
            HttpClientManager.getInstance().post(cmd, new HttpClientManager.HttpCallback() {
                @Override
                public void onSuccess(String msg, String data) {
                    LogUtil.I("upload score success");
                }

                @Override
                public void onFailure(int code, String msg, String data) {
                    LogUtil.E(msg);
                }
            });
        }
    }

    /**
     * 重新开始游戏
     */
    void restartGame(){
        myScore = 0;
        GameFragment gameFragment = GameFragment_.builder()
                .roundId(1)
                .mGameMode(mGameMode)
                .build();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fra_content, gameFragment)
                .commit();
        this.gameFragment = gameFragment;
    }

    /**
     * 下一关
     */
    void nextLevel(){
        GameFragment gameFragment = GameFragment_.builder()
                .roundId(this.gameFragment.getGameRound() + 1)
                .mGameMode(mGameMode)
                .build();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fra_content, gameFragment)
                .commit();
        this.gameFragment = gameFragment;
    }

    /**
     * 添加fragment
     */
    void addFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.fra_content, fragment)
                .commit();
    }
}
