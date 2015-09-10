package voiddog.org.game24.activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;

import voiddog.org.game24.R;
import voiddog.org.game24.fragment.GameFragment;
import voiddog.org.game24.fragment.GameFragment_;

/**
 * 游戏activity
 * Created by Dog on 2015/9/7.
 */
@Fullscreen
@EActivity(R.layout.activity_game)
public class GameActivity extends BaseActivity{
    GameFragment gameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameFragment = GameFragment_.builder().build();
    }

    @AfterViews
    void setupViews(){
        addFragment(gameFragment);
    }

    @Click(R.id.rcb_back)
    void onButtonClick(){
        gameFragment.backStep();
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
