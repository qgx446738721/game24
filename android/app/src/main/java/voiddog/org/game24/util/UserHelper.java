package voiddog.org.game24.util;

import android.content.Context;

import voiddog.org.game24.sharedpreference.Config_;

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
    Config_ mConf;

    /**
     * 判断用户是否存在
     * @return 是否存在
     */
    public boolean isHasUser(){
        return hasUser;
    }

    public int getUid(){
        return uid;
    }

    public String getName(){
        return name;
    }

    public void setUser(int uid, String name){
        this.uid = uid;
        this.name = name;
        this.hasUser = true;

        mConf.edit()
                .userId().put(uid)
                .userName().put(name)
                .apply();
    }

    private UserHelper(Context context){
        mContext = context;
        mConf = new Config_(context);
        uid = mConf.userId().getOr(-1);
        name = mConf.userName().get();
        if(uid == -1 || name == null){
            hasUser = false;
        }
        else{
            hasUser = true;
        }
    }
}
