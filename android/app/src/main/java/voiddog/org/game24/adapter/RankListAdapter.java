package voiddog.org.game24.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import voiddog.org.game24.R;
import voiddog.org.game24.net.response.NervousRelaxResponse;
import voiddog.org.game24.util.TypefaceHelper;
import voiddog.org.game24.util.UserHelper;

/**
 * 排名列表list
 * Created by Dog on 2015/9/13.
 */
@EBean
public class RankListAdapter extends CommonAdapter<NervousRelaxResponse>{
    @RootContext
    Context mContext;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            View view = View.inflate(mContext, R.layout.ui_rank_list_item, null);
            viewHolder = new ViewHolder(view);
            convertView = view;
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.bind((NervousRelaxResponse) getItem(position));

        return convertView;
    }

    class ViewHolder{
        public TextView tv_name, tv_rank;
        public ViewHolder(View view){
            tv_name = (TextView) view.findViewById(R.id.tv_name);
            tv_name.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
            tv_rank = (TextView) view.findViewById(R.id.tv_rank);
            tv_rank.setTypeface(TypefaceHelper.getInstance().loadTypeface("fonts/jianzhi.TTF"));
        }

        public void bind(NervousRelaxResponse relaxResponse){
            tv_name.setText(relaxResponse.name);
            tv_rank.setText(Integer.toString(relaxResponse.mark));
            if(UserHelper.getInstance().isHasUser()
                    && UserHelper.getInstance().getUid() == relaxResponse.uid){
                tv_name.setTextColor(mContext.getResources().getColor(R.color.green));
                tv_rank.setTextColor(mContext.getResources().getColor(R.color.green));
            }
            else{
                tv_name.setTextColor(Color.WHITE);
                tv_rank.setTextColor(Color.WHITE);
            }
        }
    }
}
