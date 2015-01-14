package cn.edu.cqupt.nmid.headline.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.LinkedList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.cqupt.nmid.headline.R;
import cn.edu.cqupt.nmid.headline.support.Constant;
import cn.edu.cqupt.nmid.headline.support.controller.Controller;
import cn.edu.cqupt.nmid.headline.support.controller.bean.NewsBean;
import cn.edu.cqupt.nmid.headline.ui.adapter.FeedAdapter;
import cn.edu.cqupt.nmid.headline.utils.PreferenceUtils;

import static cn.edu.cqupt.nmid.headline.utils.LogUtils.LOGD;
import static cn.edu.cqupt.nmid.headline.utils.LogUtils.makeLogTag;


/**
 * Created by leon on 14/9/19.
 */

public class FeedFragment extends Fragment {


    String TAG = makeLogTag(FeedFragment.class);


    /**
     * Injected Vies
     */
    @InjectView(R.id.feed_listview)
    ListView mListView;

    @InjectView(R.id.feed_swiperefreshlayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Data
     */

    LinkedList<NewsBean> newsBeans = new LinkedList<>();
    LinkedList<NewsBean> tmpnewsBeans = new LinkedList<>();
    Controller controller;
    FeedAdapter adapter;

    Handler handler;

    HashMap<String, Object> currentQueryMap = new HashMap<String, Object>();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE = "title";
    private static final String ARG_CATEGORY = "slug";
    private static final String ARG_FAV = "favorite";

    private String title;
    private int feed_type = Constant.TYPE_COLLEGE;
    private boolean favorite = false;
    private int limit = 15;

    public static FeedFragment newInstance(String title, int type) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_CATEGORY, type);
        fragment.setArguments(args);
        return fragment;
    }

    public static FeedFragment newFavInstance(Boolean isFav) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FAV, isFav);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            feed_type = getArguments().getInt(ARG_CATEGORY);
            favorite = getArguments().getBoolean(ARG_FAV);

            Log.d(TAG, "getArguments " + feed_type);
        } else {
            Log.d(TAG, "getArguments == null!");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.inject(this, view);
        limit = PreferenceUtils.getQueryLimit(getActivity());
        controller = new Controller(getActivity());
        mSwipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN);
        adapter = new FeedAdapter(newsBeans, getActivity());
        mListView.setAdapter(adapter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    //mSwipeRefreshLayout.setRefreshing(false);
                    newsBeans.addAll(tmpnewsBeans);
                    adapter.notifyDataSetChanged();
                }
                if (msg.what == 2) {
                    //mSwipeRefreshLayout.setRefreshing(false);
                    newsBeans.clear();
                    newsBeans.addAll(tmpnewsBeans);
                    adapter.notifyDataSetChanged();
                }
            }
        };

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                controller.RefreshData(handler, tmpnewsBeans, feed_type, limit);
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean loading = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //what is the bottom iten that is visible
                int lastInScreen = firstVisibleItem + visibleItemCount;
                //is the bottom item visible & not loading more already ? Load more !
                if ((lastInScreen == totalItemCount)) {
                    controller.OldData(handler, tmpnewsBeans, feed_type, limit);
                }
            }
        });

        initialData();
        return view;
    }

    private void initialData() {
        if (favorite) {
            tmpnewsBeans = controller.getFavoriteList();
            handler.sendEmptyMessage(1);
        } else {
            //controller.InitData(handler, tmpnewsBeans, feed_type, limit);

        }
    }

    void loadMockData(LinkedList<NewsBean> newsBeans) {
        NewsBean newsBean = new NewsBean();
        newsBean.set_id(11);
        newsBean.setCategory(1);
        newsBean.setImage1("http://www.baidu.com/img/bdlogo.png");
        newsBean.setImage2("http://www.baidu.com/img/bdlogo.png");
        newsBean.setImage3("http://www.baidu.com/img/bdlogo.png");
        newsBean.setSimpleContent("simple content");
        newsBean.setTitle("title");
        newsBean.setTimeRelease("2015");
        for (int i = 0; i < 10; i++) {
            newsBeans.add(newsBean);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LOGD(TAG, "onDestroyView");
        ButterKnife.reset(this);
    }


}
