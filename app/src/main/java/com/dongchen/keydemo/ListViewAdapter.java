package com.dongchen.keydemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * 首页ListView的适配器
 */
public class ListViewAdapter extends BaseAdapter {

    private Context context;
    private List list;

    public ListViewAdapter(Context context, List list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return (null == list) ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return (null == list || position >= list.size()) ? null : list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        JumpBean jumpBean;
        TextView tvItem;
        if (null == convertView) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_item_main, null);
            tvItem = (TextView) convertView.findViewById(R.id.tvItem);
            convertView.setTag(tvItem);
        } else {
            tvItem = (TextView) convertView.getTag();
        }

        jumpBean = (JumpBean)getItem(position);
        if (null != jumpBean) {
            tvItem.setText(jumpBean.getContent());
//            tvItem.setTag(JumpBean.JUMP_TYPE, jumpBean.getJumpType());
//            tvItem.setTag(JumpBean.URL, jumpBean.getAbsClsUrlOrAction());
        }

        return convertView;
    }

    public void setList(List list) {
        this.list = list;
    }
}
