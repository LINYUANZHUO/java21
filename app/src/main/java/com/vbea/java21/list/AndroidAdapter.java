package com.vbea.java21.list;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import com.vbea.java21.data.AndroidHtml;
import com.vbea.java21.R;
import com.vbea.java21.classes.Common;

public class AndroidAdapter extends RecyclerView.Adapter<AndroidAdapter.MyViewHolder>
{
	private OnItemClickListener onItemClickListener;
	private List<AndroidHtml> mList;
	private boolean isEnd = false;
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		LayoutInflater inflate = LayoutInflater.from(p1.getContext());
		View v = inflate.inflate(R.layout.java6, p1, false);
		MyViewHolder holder = new MyViewHolder(v);
		return holder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int p)
	{
		final AndroidHtml item = mList.get(p);
		if (item.prefix == null || item.prefix.equals(""))
			item.prefix = "┗ ";
		holder.title.setText(item.prefix + " " + item.title);
		holder.sub.setText(item.remark);
		if (item.isTitle)
		{
			holder.sub.setVisibility(View.GONE);
			holder.layout.setBackgroundResource(R.color.android_disable);
		}
		else
		{
			holder.sub.setVisibility(View.VISIBLE);
			holder.layout.setBackgroundResource(R.drawable.btn_menu);
		}
		if (isEnd && p == mList.size() - 1)
			holder.end.setVisibility(View.VISIBLE);
		else
			holder.end.setVisibility(View.GONE);
		holder.read.setVisibility(Common.READ_Android.contains(item.getObjectId()) ? View.VISIBLE : View.GONE);
		holder.layout.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				if(onItemClickListener != null)
				{
					onItemClickListener.onItemClick(item.getObjectId(), item.title, item.remark, item.url, item.isTitle);
				}
			}
		});
	}

	@Override
	public int getItemCount()
	{
		if (mList != null)
			return mList.size();
		return 0;
	}
	
	public AndroidAdapter (List<AndroidHtml> list)
	{
		mList = list;
		isEnd = false;
	}
	
	public void setList(List<AndroidHtml> list)
	{
		mList = list;
		isEnd = false;
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
        this.onItemClickListener = onItemClickListener;
    }
	
	@Override
	public long getItemId(int p1)
	{
		return 0;
	}
	
	public void clear()
	{
		if (mList != null)
			mList.clear();
	}
	
	public void setEnd(boolean end)
	{
		isEnd = end;
	}

	public class MyViewHolder extends ViewHolder
	{
		TextView title;
		TextView sub;
		ImageView read;
		LinearLayout layout;
		RelativeLayout end;
		public MyViewHolder(View v)
		{
			super(v);
			title = (TextView) v.findViewById(R.id.know_title);
			sub = (TextView) v.findViewById(R.id.know_subTitle);
			read = (ImageView) v.findViewById(R.id.item_imgread);
			layout = (LinearLayout) v.findViewById(R.id.itemLayout);
			end = (RelativeLayout) v.findViewById(R.id.item_end);
		}
	}
	
	public interface OnItemClickListener
	{
        void onItemClick(String id, String title, String sub, String url, boolean t);
    }
}
