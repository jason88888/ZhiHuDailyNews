package com.huchiwei.zhihudailynews.modules.news.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.huchiwei.zhihudailynews.R;
import com.huchiwei.zhihudailynews.common.support.recyclerview.SimpleRecyclerView;
import com.huchiwei.zhihudailynews.common.ui.ToolbarActivity;
import com.huchiwei.zhihudailynews.common.support.recyclerview.RecyclerItemClickListener;
import com.huchiwei.zhihudailynews.core.utils.DateUtil;
import com.huchiwei.zhihudailynews.modules.news.adapter.NewsAdapter;
import com.huchiwei.zhihudailynews.modules.news.contract.NewsContract;
import com.huchiwei.zhihudailynews.modules.news.contract.NewsPresenter;
import com.huchiwei.zhihudailynews.modules.news.entity.News;
import com.huchiwei.zhihudailynews.modules.news.entity.News4List;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 新闻列表Activity
 */
public class NewsActivity extends ToolbarActivity implements NewsContract.View {
    @BindView(R.id.news_recycler_view)
    SimpleRecyclerView mNewsListView;

    private NewsAdapter mNewsAdapter = null;
    private Date mNewsDate = new Date();
    private NewsPresenter mNewsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        // 绑定View
        ButterKnife.bind(NewsActivity.this);

        renderBottomNavigation(savedInstanceState);

        // List Adapter
        mNewsAdapter = new NewsAdapter(this);
        mNewsListView.setAdapter(mNewsAdapter);
        mNewsListView.getRecyclerView().setNestedScrollingEnabled(false);

        // 点击事件
        mNewsListView.addOnItemTouchListener(new RecyclerItemClickListener(mNewsListView.getRecyclerView()) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder holder) {
                if(holder instanceof  NewsAdapter.ViewNormalHolder){
                    NewsAdapter.ViewNormalHolder viewNormalHolder = (NewsAdapter.ViewNormalHolder) holder;
                    Intent detailIntent = new Intent(NewsActivity.this, NewsDetailActivity.class);
                    detailIntent.putExtra("newsId", viewNormalHolder.getNewsId());
                    startActivity(detailIntent);
                }
            }
        });

        // 下拉刷新
        mNewsListView.addOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNewsPresenter.fetchLatest();
            }
        });

        // 上拉加载更多
        mNewsListView.addScrollListener(
                new SimpleRecyclerView.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        mNewsPresenter.fetchHistory(mNewsDate);
                    }
                },
                new SimpleRecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled() {
                        RecyclerView.LayoutManager layoutManager = mNewsListView.getLayoutManager();
                        if(layoutManager instanceof LinearLayoutManager){
                            LinearLayoutManager linearLayoutManager = (LinearLayoutManager)layoutManager;
                            News news = mNewsAdapter.getDataItem(linearLayoutManager.findFirstCompletelyVisibleItemPosition());
                            if(null != news && null != getSupportActionBar()){
                                getSupportActionBar().setTitle(news.getPublishDate());
                            }
                        }

                    }
                });

        // 实例化Presenter
        mNewsPresenter = new NewsPresenter(this);
        mNewsPresenter.init();
    }

    @Override
    protected boolean showBackButton() {
        return false;
    }

    /**
     * 退到桌面重新打开，不再显示Launcher Screen
     */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void renderBottomNavigation(Bundle savedInstanceState){
        /*BottomBar mBottomBar = BottomBar.attach(this, savedInstanceState);
        // Instead of attach(), use attachShy():
        //BottomBar mBottomBar = BottomBar.attachShy((CoordinatorLayout) findViewById(R.id.news_coordinator), findViewById(R.id.news_scrolling_content), savedInstanceState);
        mBottomBar.useFixedMode();
        mBottomBar.setBackgroundColor( ContextCompat.getColor(this, R.color.white) );
        mBottomBar.setActiveTabColor(ContextCompat.getColor(this, R.color.colorAccent));
        mBottomBar.setItems(R.menu.menu_bottom_navigation);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.nav_home) {
                    // The user selected item number one.
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.nav_home) {
                    // The user reselected item number one, scroll your content to top.
                }
            }
        });*/

        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation, null);

        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));
        bottomNavigation.setForceTitlesDisplay(true);
        bottomNavigation.setBehaviorTranslationEnabled(true);
    }

    // ======================================================================================
    // view methods ===================================================================
    @Override
    public void onDataChanged(News4List news4List) {
        mNewsDate = DateUtil.parseDate(news4List.getDate());
        mNewsAdapter.addNewses(news4List, true);
        this.setRefreshing(false);
    }

    @Override
    public void onFetchFail() {
        this.setRefreshing(false);
        Toast.makeText(NewsActivity.this, "消息获取失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        mNewsListView.setRefreshing(refreshing);
    }
}