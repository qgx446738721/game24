package voiddog.org.game24.net.request;

import voiddog.org.game24.data.Constance;

/**
 * 获取困难列表
 * Created by Dog on 2015/9/13.
 */
public class GetNervousRankListCmd extends BaseCmd{

    public int pageId = 0, pageSize = 20;

    @Override
    public String getUrl() {
        return Constance.API_HOST + "nervous_rank_list";
    }
}
