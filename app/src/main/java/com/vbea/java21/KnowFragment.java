package com.vbea.java21;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.AdapterView;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.DividerItemDecoration;

import com.vbea.java21.list.Knowledges;
import com.vbea.java21.list.KnowledgeAdapter;
import com.vbea.java21.classes.Common;
import com.vbea.java21.classes.Util;

public class KnowFragment extends Fragment
{
	private RecyclerView recyclerView;
	private KnowledgeAdapter mAdapter;
	private View rootView;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        if (rootView == null)
			rootView = inflater.inflate(R.layout.chapter, container, false);
		return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
        super.onViewCreated(view, savedInstanceState);
		if (recyclerView == null)
		{
			recyclerView = (RecyclerView) view.findViewById(R.id.cpt_recyclerView);
			recyclerView.setHasFixedSize(true);
			recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
			DividerItemDecoration decoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
			decoration.setDrawable(getResources().getDrawable(R.drawable.ic_divider));
			recyclerView.addItemDecoration(decoration);
			
			ArrayList<Knowledges> list = new ArrayList<Knowledges>();
			String[] titles = getResources().getStringArray(R.array.array_shizhan);
			String[] review = getResources().getStringArray(R.array.array_baodian);
			for (int i=0; i<titles.length; i++)
			{
				list.add(new Knowledges(titles[i], "实战"));
			}
			for (int i=0; i<review.length; i++)
			{
				list.add(new Knowledges(review[i], "宝典"));
			}
			mAdapter = new KnowledgeAdapter(list);
			recyclerView.setAdapter(mAdapter);
			mAdapter.setOnItemClickListener(new KnowledgeAdapter.OnItemClickListener()
			{
				@Override
				public void onItemClick(String title, int id)
				{
					/*if (Common.isNotLogin())
					{
						Util.toastShortMessage(getContext(), "请先登录！");
						return;
					}*/
					Intent intent = new Intent(getActivity(), Knowledge.class);
					intent.putExtra("id", id);
					intent.putExtra("title", title);
					Common.startActivityOptions(getActivity(), intent);
				}
			});
		}
	}
}
