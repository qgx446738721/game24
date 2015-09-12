package voiddog.org.game24.net.request;

import voiddog.org.game24.data.Constance;

/**
 * 上传困难模式分数
 * Created by Dog on 2015/9/13.
 */
public class UploadNervousScoreCmd extends UploadRelaxModeScoreCmd{
    public UploadNervousScoreCmd(int user_id, int mark) {
        super(user_id, mark);
    }

    @Override
    public String getUrl() {
        return Constance.API_HOST + "upload_nervous_mark";
    }
}
