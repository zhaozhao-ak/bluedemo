package com.example.rjyx.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * 会话适配器
 */
public class ChatListAdapter extends BaseAdapter {
	private ArrayList<DeviceBean> mDatas;
	private LayoutInflater mInflater;
	private BluetoothItemOnclick itemOnclick;

	public ChatListAdapter(Context context, ArrayList<DeviceBean> datas,BluetoothItemOnclick itemOnclick) {
		this.mDatas = datas;
		this.mInflater = LayoutInflater.from(context);
		this.itemOnclick = itemOnclick;
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
		ViewHolder mHolder = null;
		final DeviceBean item = mDatas.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			mHolder = new ViewHolder((View) convertView.findViewById(R.id.list_child),
					(TextView) convertView.findViewById(R.id.chat_msg),(TextView) convertView.findViewById(R.id.tv_pipei),(Button) convertView.findViewById(R.id.btn_connection),(Button) convertView.findViewById(R.id.btn_pipei));
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		if (item.isReceive) {
			mHolder.btn_pipei.setVisibility(View.GONE);
			mHolder.btn_connection.setVisibility(View.VISIBLE);
			mHolder.tv_pipei.setVisibility(View.VISIBLE);
			mHolder.child.setBackgroundResource(R.drawable.item_receive_bg);
		} else {
			mHolder.btn_pipei.setVisibility(View.VISIBLE);
			mHolder.btn_connection.setVisibility(View.GONE);
			mHolder.tv_pipei.setVisibility(View.GONE);
			mHolder.child.setBackgroundResource(R.drawable.item_not_receive_bg);
		}

		mHolder.btn_connection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				itemOnclick.OnclickConnection(item);
			}
		});

		mHolder.btn_pipei.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				itemOnclick.OnclickConnection(item);
			}
		});

		mHolder.msg.setText(item.name + "\n" + item.Address);

		return convertView;
	}

	class ViewHolder {
		protected View child;
		protected TextView msg,tv_pipei;
		protected Button btn_pipei,btn_connection;

		public ViewHolder(View child, TextView msg,TextView tv_pipei,Button btn_connection,Button btn_pipei) {
			this.child = child;
			this.msg = msg;
			this.tv_pipei = tv_pipei;
			this.btn_connection = btn_connection;
			this.btn_pipei = btn_pipei;

		}
	}
}
