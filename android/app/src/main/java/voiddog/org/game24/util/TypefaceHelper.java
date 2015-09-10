package voiddog.org.game24.util;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * 字体加载帮助类
 * Created by Dog on 2015/9/10.
 */
public class TypefaceHelper {
    static TypefaceHelper instance;

    /**
     * 初始化帮助类
     */
    public static void init(Context context){
        instance = new TypefaceHelper(context);
    }

    public static TypefaceHelper getInstance(){
        if(instance == null){
            throw new IllegalArgumentException("必须初始化");
        }
        return instance;
    }

    Map<String, Typeface> typefaceMap;
    Context mContext;

    private TypefaceHelper(Context context){
        mContext = context;
        typefaceMap = new HashMap<>();
    }

    public Typeface loadTypeface(String name){
        Typeface typeface = typefaceMap.get(name);
        if(typeface == null){
            typeface = Typeface.createFromAsset(mContext.getAssets(), name);
            if(typeface != null){
                typefaceMap.put(name, typeface);
            }
        }

        return typeface;
    }
}
