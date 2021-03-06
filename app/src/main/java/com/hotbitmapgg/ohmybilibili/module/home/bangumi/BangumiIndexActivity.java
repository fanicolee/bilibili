package com.hotbitmapgg.ohmybilibili.module.home.bangumi;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.ohmybilibili.adapter.BangumiIndexAdapter;
import com.hotbitmapgg.ohmybilibili.adapter.helper.HeaderViewRecyclerAdapter;
import com.hotbitmapgg.ohmybilibili.base.RxAppCompatBaseActivity;
import com.hotbitmapgg.ohmybilibili.entity.bangumi.BangumiIndex;
import com.hotbitmapgg.ohmybilibili.entity.bangumi.BangumiIndexTag;
import com.hotbitmapgg.ohmybilibili.network.RetrofitHelper;
import com.hotbitmapgg.ohmybilibili.widget.CircleProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hcc on 16/8/4 14:12
 * 100332338@qq.com
 * <p/>
 * 番剧索引界面
 */
public class BangumiIndexActivity extends RxAppCompatBaseActivity
{

    @Bind(R.id.recycle)
    RecyclerView mRecyclerView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.circle_progress)
    CircleProgressView mCircleProgressView;

    private List<BangumiIndexTag> bangumiIndexTags = new ArrayList<>();

    private HeaderViewRecyclerAdapter mHeaderViewRecyclerAdapter;

    private GridLayoutManager mGridLayoutManager;

    private List<BangumiIndex.ResultBean.CategoriesBean> categories;

    private List<BangumiIndex.ResultBean.RecommendCategoryBean> recommendCategory;


    @Override
    public int getLayoutId()
    {

        return R.layout.activity_bangumi_index;
    }

    @Override
    public void initViews(Bundle savedInstanceState)
    {

        getBangumiIndex();
    }

    private void initRecyclerView()
    {

        mRecyclerView.setHasFixedSize(true);
        mGridLayoutManager = new GridLayoutManager(BangumiIndexActivity.this, 3);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {

            @Override
            public int getSpanSize(int position)
            {

                return (0 == position) ? mGridLayoutManager.getSpanCount() : 1;
            }
        });
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        BangumiIndexAdapter mAdapter = new BangumiIndexAdapter(mRecyclerView, bangumiIndexTags);
        mHeaderViewRecyclerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
        createHeadLayout();
        mRecyclerView.setAdapter(mHeaderViewRecyclerAdapter);
    }

    @Override
    public void initToolBar()
    {

        mToolbar.setTitle("番剧索引");
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        if (item.getItemId() == android.R.id.home)
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }

    public void getBangumiIndex()
    {

        RetrofitHelper.getBangumiIndexApi()
                .getBangumiIndex()
                .compose(this.bindToLifecycle())
                .doOnSubscribe(this::showProgressBar)
                .subscribeOn(Schedulers.io())
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bangumiIndex -> {

                    categories = bangumiIndex.getResult().getCategories();
                    recommendCategory = bangumiIndex.getResult().getRecommendCategory();
                    finishTask();
                }, throwable -> {
                    hideProgressBar();
                });
    }


    private void showProgressBar()
    {

        mCircleProgressView.setVisibility(View.VISIBLE);
        mCircleProgressView.spin();
    }

    private void hideProgressBar()
    {

        mCircleProgressView.setVisibility(View.GONE);
        mCircleProgressView.stopSpinning();
    }

    private void finishTask()
    {

        mergerIndexTags();
        initRecyclerView();
        hideProgressBar();
    }

    private void mergerIndexTags()
    {

        BangumiIndexTag bangumiIndexTag;
        for (int i = 0, size = categories.size(); i < size; i++)
        {
            BangumiIndex.ResultBean.CategoriesBean.CategoryBean category = categories.get(i).getCategory();
            bangumiIndexTag = new BangumiIndexTag();
            bangumiIndexTag.setPic(category.getCover());
            bangumiIndexTag.setTitle(category.getTag_name());
            bangumiIndexTags.add(bangumiIndexTag);
        }

        for (int i = 0, size = recommendCategory.size(); i < size; i++)
        {
            BangumiIndex.ResultBean.RecommendCategoryBean recommendCategoryBean = recommendCategory.get(i);
            bangumiIndexTag = new BangumiIndexTag();
            bangumiIndexTag.setPic(recommendCategoryBean.getCover());
            bangumiIndexTag.setTitle(recommendCategoryBean.getTag_name());
            bangumiIndexTags.add(bangumiIndexTag);
        }
    }

    private void createHeadLayout()
    {

        View headView = LayoutInflater.from(this).inflate(R.layout.layout_bangumi_index_head, mRecyclerView, false);
        mHeaderViewRecyclerAdapter.addHeaderView(headView);
    }
}
