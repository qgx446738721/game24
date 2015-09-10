package voiddog.org.game24.application;

import android.app.Application;

import voiddog.org.game24.util.LogUtil;
import voiddog.org.game24.util.TypefaceHelper;

/**
 * 应用app实例
 * Created by Dog on 2015/9/6.
 */
public class GameApplication extends Application{
    protected static GameApplication mInstance = null;

    public static GameApplication getInstance(){
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        //初始化
        LogUtil.debug = true;

        TypefaceHelper.init(getApplicationContext());
    }
}
