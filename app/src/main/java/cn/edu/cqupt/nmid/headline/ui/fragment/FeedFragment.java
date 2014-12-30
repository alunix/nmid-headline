package cn.edu.cqupt.nmid.headline.ui.fragment;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.HashMap;
import java.util.LinkedList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.cqupt.nmid.headline.R;
import cn.edu.cqupt.nmid.headline.api.HeadlineService;
import cn.edu.cqupt.nmid.headline.controller.Controller;
import cn.edu.cqupt.nmid.headline.controller.bean.HeadJson;
import cn.edu.cqupt.nmid.headline.controller.bean.NewsBean;
import cn.edu.cqupt.nmid.headline.support.Constant;
import cn.edu.cqupt.nmid.headline.ui.activity.DetailedActivity;
import cn.edu.cqupt.nmid.headline.ui.adapter.FeedAdapter;
import cn.edu.cqupt.nmid.headline.utils.UIutils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static cn.edu.cqupt.nmid.headline.utils.LogUtils.LOGD;
import static cn.edu.cqupt.nmid.headline.utils.LogUtils.LOGE;
import static cn.edu.cqupt.nmid.headline.utils.LogUtils.LOGW;
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

    private boolean isRefresh = false;
    HashMap<String, Object> currentQueryMap = new HashMap<String, Object>();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "title";
    private static final String ARG_PARAM2 = "slug";

    private String title;
    private int feed_type = Constant.TYPE_COLLEGE;

    public static FeedFragment newInstance(String title, int type) {
        FeedFragment fragment = new FeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, title);
        args.putInt(ARG_PARAM2, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            title = getArguments().getString(ARG_PARAM1);
            feed_type = getArguments().getInt(ARG_PARAM2);

            LOGD(TAG, "getArguments " + feed_type);
        } else {
            LOGE(TAG, "getArguments == null!");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.inject(this, view);
        adapter = new FeedAdapter(newsBeans, getActivity());
        mListView.setAdapter(adapter);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    newsBeans.clear();
                    newsBeans.addAll(tmpnewsBeans);
                    adapter.notifyDataSetChanged();
                }
                if (msg.what == 2) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    newsBeans.clear();
                    newsBeans.addAll(tmpnewsBeans);
                    adapter.notifyDataSetChanged();
                }
            }
        };

        controller = new Controller(getActivity());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LOGD(TAG, "onClick!");
                Intent intent = new Intent(getActivity(), DetailedActivity.class);
                intent.putExtra("content", newsBeans.get(position).getContent());
                startActivity(intent);
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //http://113.250.153.205/txtt/public/api/android/freshnews?id=10&category=1&limit=10
                new RestAdapter.Builder()
                        .setEndpoint(Constant.API_URL)
                        .setLogLevel(RestAdapter.LogLevel.FULL)
                        .build()
                        .create(HeadlineService.class)
                        .getFeeds(1,10,10, new Callback<HeadJson>() {
                            @Override
                            public void success(HeadJson headJson, Response response) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                newsBeans.clear();
                                newsBeans.addAll(tmpnewsBeans);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                UIutils.disErr(getActivity(), error);
                            }
                        });

            }
        });


        mSwipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN);
        mSwipeRefreshLayout.setRefreshing(true);
        //load beans from datebase
        controller.InitData(handler, tmpnewsBeans, feed_type, 15);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LOGD(TAG, "onDestroyView");
        ButterKnife.reset(this);
    }


    public void loadPage(HashMap<String, Object> stringObjectMap) {
        LOGD(TAG, "Http-Get Query is " + stringObjectMap.toString());
        if (!stringObjectMap.containsKey("page")) {
            stringObjectMap.put("page", 1);
            stringObjectMap.put("category_name", getArguments().getString(ARG_PARAM2));
            LOGW(TAG, "Http-Get Query is empty,try defalut query" + stringObjectMap.toString());
        }
        currentQueryMap = stringObjectMap;
    }


}