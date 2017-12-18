package com.vbea.java21.list;

import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.database.Cursor;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import com.vbea.java21.R;
import com.vbea.java21.classes.ExceptionHandler;

public class ModeAdapter extends RecyclerView.Adapter<ModeAdapter.MyViewHolder>
{
	private Cursor cursor;
	private OnItemClickListener onItemClickListener;
	public ModeAdapter(Cursor data)
	{
		cursor = data;
	}
	
	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup p1, int p2)
	{
		LayoutInflater inflate = LayoutInflater.from(p1.getContext());
		View v = inflate.inflate(R.layout.java9, p1, false);
		MyViewHolder holder = new MyViewHolder(v);
		return holder;
	}
	
	@Override
	public void onBindViewHolder(MyViewHolder holder, int p)
	{
		if (cursor.moveToPosition(p))
		{
			holder.title.setText(cursor.getString(cursor.getColumnIndex("name")));
			holder.sub.setText(cursor.getString(cursor.getColumnIndex("mode")));
			if (p == getItemCount() - 1)
				holder.end.setVisibility(View.VISIBLE);
			else
				holder.end.setVisibility(View.GONE);
			holder.layout.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					if(onItemClickListener != null)
					{
						onItemClickListener.onItemClick();
					}
				}
			});
		}
	}
	
	@Override
	public int getItemCount()
	{
		if (cursor != null)
			return cursor.getCount();
		return 0;
	}
	
	public void close()
	{
		if (cursor != null)
			cursor.close();
	}
	
	public void setOnItemClickListener(OnItemClickListener onItemClickListener)
	{
        this.onItemClickListener = onItemClickListener;
    }
	
	public class MyViewHolder extends ViewHolder
	{
		TextView title;
		TextView sub;
		LinearLayout layout;
		RelativeLayout end;
		public MyViewHolder(View v)
		{
			super(v);
			title = (TextView) v.findViewById(R.id.item_title);
			sub = (TextView) v.findViewById(R.id.item_sub);
			layout = (LinearLayout) v.findViewById(R.id.itemLayout);
			end = (RelativeLayout) v.findViewById(R.id.item_end);
		}
	}

	public interface OnItemClickListener
	{
        void onItemClick();
    }
}
