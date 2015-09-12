package voiddog.org.game24.net.request;

import com.loopj.android.http.RequestParams;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 基本访问命令
 * Created by Dog on 2015/9/12.
 */
abstract public class BaseCmd {
    //csrf token
    public String _token;

    //获取访问url
    abstract public String getUrl();

    //获取访问数据
    public RequestParams getParams(){
        RequestParams params = new RequestParams();
        Field[] fields = getClass().getFields();
        for(Field field : fields){
            try {
                boolean accessFlag = field.isAccessible();
                field.setAccessible(true);
                Object o = field.get(this);
                if(o != null && !Modifier.isStatic(field.getModifiers())){
                    params.put(field.getName(), String.valueOf(o));
                }
                field.setAccessible(accessFlag);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return params;
    }
}
