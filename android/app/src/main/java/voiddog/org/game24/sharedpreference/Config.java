package voiddog.org.game24.sharedpreference;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * 设置存储类
 * Created by Dog on 2015/9/12.
 */
@SharedPref
public interface Config {
    //访问token
    String csrfToken();
    //用户姓名
    String userName();
    //用户id
    int userId();
}
