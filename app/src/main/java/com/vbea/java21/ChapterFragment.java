package com.vbea.java21;

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vbea.java21.list.Chapter;
import com.vbea.java21.list.ChapterAdapter;
import com.vbea.java21.classes.Common;
import com.vbea.java21.classes.Util;

public class ChapterFragment extends Fragment
{
	private RecyclerView recyclerView;
	private ChapterAdapter mAdapter;
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
			recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

			ArrayList<Chapter> list = new ArrayList<Chapter>();
			String[] titles = getResources().getStringArray(R.array.array_chapter);
			String[] review = getResources().getStringArray(R.array.array_review);
			for (int i=0; i<titles.length; i++)
			{
				list.add(new Chapter(titles[i], review[i]));
			}
			mAdapter = new ChapterAdapter(list);
			recyclerView.setAdapter(mAdapter);
			mAdapter.setOnItemClickListener(new ChapterAdapter.OnItemClickListener()
			{
				@Override
				public void onItemClick(View view, int resId)
				{
					/*if (Common.isNotLogin())
					{
						Util.toastShortMessage(getContext(), "请先登录！");
						return;
					}*/
					Common.AUDIO_STUDY_STATE+=2;
					Intent intent = new Intent(getActivity(), ChapterText.class);
					intent.putExtra("chapter", resId);
					Common.startActivityOptions(getActivity(), intent, view, "share_image2");
				}
			});
		}
	}
}
