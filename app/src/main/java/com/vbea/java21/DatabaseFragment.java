package com.vbea.java21;

import java.util.List;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.content.DialogInterface;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.DividerItemDecoration;
import com.vbea.java21.data.Database;
import com.vbea.java21.list.DatabaseAdapter;
import com.vbea.java21.list.MyDividerDecoration;
import com.vbea.java21.classes.Util;
import com.vbea.java21.classes.Common;
import com.vbea.java21.classes.ExceptionHandler;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.exception.BmobException;

public class DatabaseFragment extends Fragment
{
	private SwipeRefreshLayout refreshLayout;
	private RecyclerView recyclerView;
	private TextView errorText;
	private ProgressBar proRefresh;
	private DatabaseAdapter mAdapter;
	private List<Database> mList;
	private View rootView;
	private int mCount = -1;
	//private ProgressDialog mPdialog;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (rootView == null)
			rootView = inflater.inflate(R.layout.android, container, false);
		return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
        super.onViewCreated(view, savedInstanceState);
		if (recyclerView == null)
		{
			mList = new ArrayList<Database>();
			mAdapter = new DatabaseAdapter();
			errorText = (TextView) view.findViewById(R.id.txt_andError);
			proRefresh = (ProgressBar) view.findViewById(R.id.refreshProgress);
        	recyclerView = (RecyclerView) view.findViewById(R.id.cpt_recyclerView);
			refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swp_refresh);

			DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
			decoration.setDrawable(getResources().getDrawable(R.drawable.ic_divider));
			recyclerView.addItemDecoration(decoration);
			recyclerView.setAdapter(mAdapter);
			recyclerView.setHasFixedSize(true);
			recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
			refreshLayout.setColorSchemeResources(MyThemes.getColorPrimary(), MyThemes.getColorAccent());
			//mList = new ArrayList<AndroidHtml>();
			//if (Common.isLogin())
				getCount();
			/*else
			{
				errorText.setVisibility(View.VISIBLE);
				errorText.setText("请登录后下拉刷新获取章节列表");
			}*/
			
			mAdapter.setOnItemClickListener(new DatabaseAdapter.OnItemClickListener()
			{
				@Override
				public void onItemClick(String id, String title, String sub, String url)
				{
					//更新数据
					/*update();
					 if (true)return;*/
					//Common.addJavaEeRead(id);
					Intent intent = new Intent(getActivity(), AndroidWeb.class);
					intent.putExtra("id", id);
					intent.putExtra("url", url);
					intent.putExtra("title", title);
					intent.putExtra("sub", sub);
					intent.putExtra("type", 6);
					Common.startActivityOptions(getActivity(), intent);
					mAdapter.notifyDataSetChanged();
				}
			});

			refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
			{
				@Override
				public void onRefresh()
				{
					/*if (!Common.isLogin())
					{
						recyclerView.setVisibility(View.GONE);
						errorText.setVisibility(View.VISIBLE);
						errorText.setText("加载失败，请登录后重试");
					}
					else*/
						getCount();
				}
			});

			recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
			{
				@Override
				public void onScrolled(RecyclerView view, int x, int y)
				{
					if (view != null)
					{
						if (view.computeVerticalScrollExtent() + view.computeVerticalScrollOffset() >= view.computeVerticalScrollRange())
							addItem();
					}
				}
			});
		}
	}

	private void getCount()
	{
		if (!Common.isNet(getContext()))
		{
			init();
			return;
		}
		BmobQuery<Database> query = new BmobQuery<Database>();
		query.addWhereEqualTo("enable", true);
		query.count(Database.class, new CountListener()
		{
			@Override
			public void done(Integer count, BmobException e)
			{
				if (e == null)
				{
					mCount = count;
					if (count == 0)
					{
						mHandler.sendEmptyMessage(3);
						return;
					}
				}
				else
					mCount = -1;
				refresh();
			}
		});
	}

	/*public void update()
	 {
	 Util.showConfirmCancelDialog(getActivity(), "数据更新", "你确定要更新数据吗", new DialogInterface.OnClickListener()
	 {
	 public void onClick(DialogInterface d, int s)
	 {
	 mPdialog = ProgressDialog.show(getActivity(), null, "请稍候...");
	 new UpdateThread().start();
	 }
	 });
	 }*/

	private void refresh()
	{
		if (mCount < 0 || !Common.isNet(getContext()) || mCount == mList.size())
		{
			init();
			return;
		}
		if (mList == null || mList.size() == 0)
		{
			errorText.setVisibility(View.VISIBLE);
			errorText.setText("正在加载，请稍候");
		}
		BmobQuery<Database> query = new BmobQuery<Database>();
		query.addWhereEqualTo("enable", true);
		query.order("order");
		query.setLimit(15);
		query.findObjects(new FindListener<Database>()
		{
			@Override
			public void done(List<Database> list, BmobException e)
			{
				if (e == null)
				{
					if (list.size() > 0)
					{
						mList = list;
						mAdapter.setEnd(false);
					}
				}
				mHandler.sendEmptyMessage(1);
			}
		});
	}

	private void addItem()
	{
		if (mCount > mList.size())
		{
			proRefresh.setVisibility(View.VISIBLE);
			BmobQuery<Database> query = new BmobQuery<Database>();
			query.addWhereEqualTo("enable", true);
			query.order("order");
			query.setLimit(15);
			query.setSkip(mList.size());
			query.findObjects(new FindListener<Database>()
			{
				@Override
				public void done(List<Database> list, BmobException e)
				{
					if (e == null)
					{
						if (list.size() > 0)
						{
							mList.addAll(list);
						}
					}
					mHandler.sendEmptyMessage(2);
				}
			});
		}
	}

	private void init()
	{
		errorText.setText("加载失败\n请检查你的网络连接");
		if (mList.size() > 0)
		{
			errorText.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);
		}
		else
			errorText.setVisibility(View.VISIBLE);
		if (refreshLayout.isRefreshing())
			refreshLayout.setRefreshing(false);
		mAdapter.setList(mList);
		mAdapter.notifyDataSetChanged();
	}

	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case 1:
					init();
					break;
				case 2:
					mAdapter.setList(mList);
					if (mList.size() == mCount)
						mAdapter.setEnd(true);
					mAdapter.notifyItemInserted(mAdapter.getItemCount());
					proRefresh.setVisibility(View.GONE);
					break;
				case 3:
					errorText.setText("敬请期待");
					errorText.setVisibility(View.VISIBLE);
					recyclerView.setVisibility(View.GONE);
					if (refreshLayout.isRefreshing())
						refreshLayout.setRefreshing(false);
					break;
				/*case 4:
					 Util.toastShortMessage(getActivity(), "更新成功");
					 mPdialog.dismiss();
					 break;
				 case 5:
					 Util.toastShortMessage(getActivity(), "更新失败");
					 mPdialog.dismiss();
					 break;*/
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onResume()
	{
		/*if (!Common.isLogin())
		{
			recyclerView.setVisibility(View.GONE);
		}*/
		if (refreshLayout.isRefreshing())
			refreshLayout.setRefreshing(false);
		super.onResume();
	}
}
