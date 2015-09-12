package voiddog.org.game24.net.request;

import voiddog.org.game24.data.Constance;

/**
 * 注册用户名cmd
 * Created by Dog on 2015/9/12.
 */
public class RegisterCmd extends BaseCmd{

    //用户名
    public String user_name;

    public RegisterCmd(String name){
        user_name = name;
    }

    @Override
    public String getUrl() {
        return Constance.API_HOST + "register";
    }
}
