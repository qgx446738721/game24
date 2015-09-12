package voiddog.org.game24.net.request;

import voiddog.org.game24.data.Constance;

/**
 * 提交easy模式分数cmd
 * Created by Dog on 2015/9/13.
 */
public class UploadRelaxModeScoreCmd extends BaseCmd{

    public int user_id;
    public int mark;

    public UploadRelaxModeScoreCmd(int user_id, int mark){
        this.user_id = user_id;
        this.mark = mark;
    }

    @Override
    public String getUrl() {
        return Constance.API_HOST + "upload_relax_mark";
    }
}
