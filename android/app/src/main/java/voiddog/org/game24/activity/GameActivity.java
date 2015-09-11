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
import voiddog.org.game24.event.GameOverEvent;
import voiddog.org.game24.fragment.GameFragment;
import voiddog.org.game24.fragment.GameFragment_;
import voiddog.org.game24.fragment.dialog.GameOverDialogFragment;
import voiddog.org.game24.fragment.dialog.GameOverDialogFragment_;

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
        setupGameoverDialog();
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
        gameOverDialog.show(getFragmentManager(), gameFragment.getClass().getName());
    }

    void setupGameoverDialog(){
        gameOverDialog = GameOverDialogFragment_.builder().build();

        gameOverDialog.setOnMenuClickListener(new GameOverDialogFragment.OnMenuClick() {
            @Override
            public void onRestartClick() {
                restartGame();
            }

            @Override
            public void onBackToHomeClick() {
                finish();
            }
        });
    }

    /**
     * 重新开始游戏
     */
    void restartGame(){
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
