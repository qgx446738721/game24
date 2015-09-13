package voiddog.org.game24.activity;

import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import voiddog.org.game24.R;
import voiddog.org.game24.adapter.RankListAdapter;
import voiddog.org.game24.net.HttpClientManager;
import voiddog.org.game24.net.request.GetNervousRankListCmd;
import voiddog.org.game24.net.response.NervousRelaxResponse;
import voiddog.org.game24.ui.LoadMoreBounceListView;
import voiddog.org.game24.util.LogUtil;
import voiddog.org.game24.util.TypefaceHelper;

/**
 * 排行榜列表activity
 * Created by Dog on 2015/9/13.
 */
@Fullscreen
@EActivity(R.layout.activity_rank_list)
public class RankListActivity extends BaseActivity{
    @ViewById
    LoadMoreBounceListView lv_rank_list;
    @ViewById
    TextView tv_title;
    @Bean
    RankListAdapter mAdapter;

    GetNervousRankListCmd cmd;

    @AfterViews
    void setupViews(){
        lv_rank_list.setAdapter(mAdapter);
        tv_title.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
        cmd = new GetNervousRankListCmd();
        requestDataFromNet();
        setupLoadMore();
    }

    @Override
    public void finish() {
        MainActivity_.intent(this).start();
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_to_bottom);
        super.finish();
    }

    void setupLoadMore(){
        lv_rank_list.setOnLoadMoreListener(new LoadMoreBounceListView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                requestDataFromNet();
            }
        });
    }

    /**
     * 从网络获取数据
     */
    void requestDataFromNet(){
        HttpClientManager.getInstance().post(cmd, new HttpClientManager.HttpCallback() {
            @Override
            public void onSuccess(String msg, String data) {
                Gson gson = new Gson();
                List<NervousRelaxResponse> responseList = gson.fromJson(
                        data,
                        new TypeToken<List<NervousRelaxResponse>>(){}.getType()
                );

                fillData(responseList);
            }

            @Override
            public void onFailure(int code, String msg, String data) {
                LogUtil.E(msg);
                fillData(null);
            }
        });
    }

    /**
     * 填充数据
     */
    void fillData(List<NervousRelaxResponse> responseList){
        if(responseList == null){
            lv_rank_list.setLoadError();
            return;
        }
        if(cmd.pageId == 0){
            mAdapter.setDataList(responseList);
        }
        else{
            mAdapter.addDataList(responseList);
        }
        if(responseList.size() < cmd.pageSize){
            lv_rank_list.setLoadComplete(true);
        }
    }
}
