package voiddog.org.game24.util;

import android.content.Context;

/**
 * 用户帮助类
 * Created by Dog on 2015/9/12.
 */
public class UserHelper {

    private static UserHelper instance;

    public static void init(Context context){
        instance = new UserHelper(context);
    }

    public static UserHelper getInstance(){
        return instance;
    }

    Context mContext;
    //用户id
    int uid;
    //用姓名
    String name;
    boolean hasUser = false;

    /**
     * 判断用户是否存在
     * @return 是否存在
     */
    public boolean hasUser(){
        return hasUser;
    }

    public int getUid(){
        return uid;
    }

    public String getName(){
        return name;
    }

    private UserHelper(Context context){
        mContext = context;
    }
}
