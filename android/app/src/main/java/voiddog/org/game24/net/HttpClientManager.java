package voiddog.org.game24.net;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.Header;

import java.io.UnsupportedEncodingException;

import voiddog.org.game24.data.Constance;
import voiddog.org.game24.net.request.BaseCmd;
import voiddog.org.game24.net.response.BaseResponseData;
import voiddog.org.game24.sharedpreference.Config_;
import voiddog.org.game24.util.LogUtil;

/**
 * http client 管理者
 * Created by Dog on 2015/9/12.
 */
public class HttpClientManager {
    private static HttpClientManager instance;

    public static void init(Context context){
        instance = new HttpClientManager(context);
    }

    public static HttpClientManager getInstance(){
        return instance;
    }

    AsyncHttpClient mClient;
    Context mContext;
    String _token;

    private HttpClientManager(Context context){
        //初始化
        mContext = context;
        mClient = new AsyncHttpClient();
        PersistentCookieStore cookieStore = new PersistentCookieStore(context);
        mClient.setCookieStore(cookieStore);
        updateToken();
    }

    /**
     * 更新token
     */
    public void updateToken(){
        final Config_ config = new Config_(mContext);
        _token = config.csrfToken().get();
        mClient.get(Constance.API_HOST + "get_csrf_token", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody, "UTF-8");
                    Gson gson = new Gson();
                    BaseResponseData data = gson.fromJson(
                            resp,
                            new TypeToken<BaseResponseData>(){}.getType()
                    );
                    if(data.code == 0){
                        _token = data.data;
                        config.csrfToken().put(_token);
                    }
                    else{
                        LogUtil.E("获取token失败: " + data.msg);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                LogUtil.E("获取token失败");
            }
        });
    }

    public void post(BaseCmd cmd, final HttpCallback callback){
        cmd._token = _token;
        mClient.post(cmd.getUrl(), cmd.getParams(), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody, "UTF-8");
                    Gson gson = new Gson();
                    BaseResponseData data = gson.fromJson(
                            resp,
                            new TypeToken<BaseResponseData>(){}.getType()
                    );
                    if(data.code == 0){
                        callback.onSuccess(data.msg, data.data);
                    }
                    else{
                        callback.onFailure(data.code, data.msg, data.msg);
                    }
                } catch (UnsupportedEncodingException e) {
                    callback.onFailure(-1, "转换错误", null);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                callback.onFailure(-statusCode, "网络错误", null);
            }
        });
    }

    public interface HttpCallback{
        void onSuccess(String msg, String data);
        void onFailure(int code, String msg, String data);
    }
}
