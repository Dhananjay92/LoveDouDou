package com.ecity.wangfeng.lovedoudou.fragment.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ecity.wangfeng.lovedoudou.R;
import com.ecity.wangfeng.lovedoudou.activity.NewsChannelActivity;
import com.ecity.wangfeng.lovedoudou.adapter.PostFragmentPagerAdapter;
import com.ecity.wangfeng.lovedoudou.base.BaseFragment;
import com.ecity.wangfeng.lovedoudou.entity.news.NewsChannelTable;
import com.ecity.wangfeng.lovedoudou.event.ChannelChangeEvent;
import com.ecity.wangfeng.lovedoudou.presenter.news.NewsPresenter;
import com.ecity.wangfeng.lovedoudou.presenter.news.NewsPresenterImpl;
import com.ecity.wangfeng.lovedoudou.util.RxBus;
import com.ecity.wangfeng.lovedoudou.util.TabLayoutUtil;
import com.ecity.wangfeng.lovedoudou.view.news.NewsView;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;


/**
 * @version 1.0
 */
public class NewsFragment extends BaseFragment<NewsPresenter> implements NewsView {

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	@Bind(R.id.tab_layout)
	TabLayout mTabLayout;
	@Bind(R.id.view_pager)
	ViewPager mViewPager;

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;


	@OnClick({R.id.add_channel_iv,
			  R.id.fab})
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.add_channel_iv:
				startActivity(new Intent(getActivity(), NewsChannelActivity.class));
				break;
			case R.id.fab:
				((NewsListFragment) mNewsFragmentList.get(mViewPager.getCurrentItem())).scrollToTop();
				break;
			default:
				break;
		}
	}

	private ArrayList<Fragment> mNewsFragmentList = new ArrayList<>();
	private String       mCurrentViewPagerName;
	private List<String> mChannelNames;

	public NewsFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment NewsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static NewsFragment newInstance(String param1, String param2) {
		NewsFragment fragment = new NewsFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getLayoutId() {
		return R.layout.fragment_news;
	}

	@Override
	public void initViews(View view) {
		mListener.onNewsTitle(getResources().getString(R.string.str_news));
		initPresenter();
	}

	private void initPresenter() {
		mPresenter = new NewsPresenterImpl();
		mPresenter.attachView(this);
		mPresenter.onCreate();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
		mSubscription = RxBus.getInstance().toObservable(ChannelChangeEvent.class).subscribe(new Action1<ChannelChangeEvent>() {
			@Override
			public void call(ChannelChangeEvent channelChangeEvent) {
				KLog.d("NewsChannelPresenterImpl", "GET ---------------");
				if (channelChangeEvent.isChannelChanged()) {
					mPresenter.loadNewsChannels();
					if (channelChangeEvent.getChannelName() != null) {
						mCurrentViewPagerName = channelChangeEvent.getChannelName();
					}
				} else {
					if (channelChangeEvent.getChannelName() != null) {
						mCurrentViewPagerName = channelChangeEvent.getChannelName();
						mViewPager.setCurrentItem(getCurrentViewPagerPosition());
					}
				}
			}
		});
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnNewsFIListener) {
			mListener = (OnNewsFIListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}


	@Override
	public void initViewPager(List<NewsChannelTable> newsChannels) {
		final List<String> channelNames = new ArrayList<>();
		if (newsChannels != null) {
			setNewsList(newsChannels, channelNames);
			setViewPager(channelNames);
		}
	}


	private void setNewsList(List<NewsChannelTable> newsChannels, List<String> channelNames) {
		mNewsFragmentList.clear();
		for (NewsChannelTable newsChannelTable : newsChannels) {
			NewsListFragment newsListFragment = createListFragment(newsChannelTable);
			mNewsFragmentList.add(newsListFragment);
			channelNames.add(newsChannelTable.getNewsChannelName());
		}
	}

	private NewsListFragment createListFragment(NewsChannelTable newsChannelTable) {
		NewsListFragment fragment = NewsListFragment.newInstance(newsChannelTable.getNewsChannelType(), newsChannelTable.getNewsChannelId(),
																 newsChannelTable.getNewsChannelIndex());
		return fragment;
	}

	private void setViewPager(List<String> channelNames) {
		PostFragmentPagerAdapter adapter = new PostFragmentPagerAdapter(getChildFragmentManager(), channelNames, mNewsFragmentList);
		mViewPager.setAdapter(adapter);
		mTabLayout.setupWithViewPager(mViewPager);
		TabLayoutUtil.dynamicSetTabLayoutMode(mTabLayout);
		setPageChangeListener();

		mChannelNames = channelNames;
		int currentViewPagerPosition = getCurrentViewPagerPosition();
		mViewPager.setCurrentItem(currentViewPagerPosition, false);
	}

	private void setPageChangeListener() {
		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mCurrentViewPagerName = mChannelNames.get(position);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	private int getCurrentViewPagerPosition() {
		int position = 0;
		if (mCurrentViewPagerName != null) {
			for (int i = 0; i < mChannelNames.size(); i++) {
				if (mCurrentViewPagerName.equals(mChannelNames.get(i))) {
					position = i;
				}
			}
		}
		return position;
	}

	@Override
	public void showProgress() {

	}

	@Override
	public void hideProgress() {

	}

	@Override
	public void showMsg(String message) {

	}

	private OnNewsFIListener mListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO: inflate a fragment view
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		ButterKnife.bind(this, rootView);
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.unbind(this);
	}

	public interface OnNewsFIListener {

		void onNewsTitle(String newsTitle);
	}
}