package com.example.rjyx.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {
	private ArrayList<DeviceBean> mDatas;
	private LayoutInflater mInflater;

	public DeviceListAdapter(Context context, ArrayList<DeviceBean> datas) {
		mDatas = datas;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return mDatas.size();
	}

	public Object getItem(int position) {
		return mDatas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		DeviceBean item = mDatas.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			viewHolder = new ViewHolder((View) convertView.findViewById(R.id.list_child),
					(TextView) convertView.findViewById(R.id.chat_msg));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (item.isReceive) {
			viewHolder.child.setBackgroundResource(R.drawable.item_receive_bg);
		} else {
			viewHolder.child.setBackgroundResource(R.drawable.item_not_receive_bg);
		}
		viewHolder.msg.setText(item.name + "\n" + item.Address);

		return convertView;
	}

	class ViewHolder {
		protected View child;
		protected TextView msg;

		public ViewHolder(View child, TextView msg) {
			this.child = child;
			this.msg = msg;

		}
	}
}
