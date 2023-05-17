package com.oraycn.ovcs.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oraycn.ovcs.R;
import com.oraycn.ovcs.utils.GlobalResourceCache;

import java.util.List;

public class FaceGVAdapter extends BaseAdapter {
	private static final String TAG = "FaceGVAdapter";
	private List<String> list;
	private Context mContext;

	public FaceGVAdapter(List<String> list, Context mContext) {
		super();
		this.list = list;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {

		return list.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		try {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(R.layout.face_image, null);
				holder.iv = (ImageView) convertView.findViewById(R.id.face_img);
				holder.tv = (TextView) convertView.findViewById(R.id.face_text);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Bitmap mBitmap = GlobalResourceCache.getInstance().getEmotion(list.get(position).substring(0,3));
			holder.iv.setImageBitmap(mBitmap);
			holder.tv.setText(list.get(position).substring(0,3));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return convertView;
	}

	class ViewHolder {
		ImageView iv;
		TextView tv;
	}
}
