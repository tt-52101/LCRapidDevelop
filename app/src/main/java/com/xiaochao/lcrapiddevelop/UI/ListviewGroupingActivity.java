package com.xiaochao.lcrapiddevelop.UI;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaochao.lcrapiddevelop.Adapter.SectionAdapter;
import com.xiaochao.lcrapiddevelop.Data.Constant;
import com.xiaochao.lcrapiddevelop.Data.JsonData;
import com.xiaochao.lcrapiddevelop.R;
import com.xiaochao.lcrapiddevelop.Volley.VolleyInterface;
import com.xiaochao.lcrapiddevelop.Volley.VolleyReQuest;
import com.xiaochao.lcrapiddevelop.entity.IsError;
import com.xiaochao.lcrapiddevelop.entity.MySection;
import com.xiaochao.lcrapiddevelop.entity.UniversityListDto;
import com.xiaochao.lcrapiddeveloplibrary.BaseQuickAdapter;
import com.xiaochao.lcrapiddeveloplibrary.viewtype.ProgressActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListviewGroupingActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,BaseQuickAdapter.RequestLoadMoreListener {
    RecyclerView mRecyclerView;
    ProgressActivity progress;
    SwipeRefreshLayout swipeLayout;
    private Toolbar toolbar;
    private SectionAdapter mQuickAdapter;
    private int PageIndex=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview_grouping);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initView();
    }
    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(this);
        progress = (ProgressActivity) findViewById(R.id.progress);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        progress.showLoading();
        initdate(PageIndex,false);
        mQuickAdapter = new SectionAdapter(R.layout.list_view_item_layout,R.layout.def_section_head,null);
        mQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mQuickAdapter.setOnLoadMoreListener(this);
        mQuickAdapter.openLoadMore(3,true);
        mRecyclerView.setAdapter(mQuickAdapter);
        mQuickAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MySection mySection = (MySection) mQuickAdapter.getData().get(position);
                if (!mySection.isHeader)
                Toast.makeText(ListviewGroupingActivity.this,mySection.t.getCnName(), Toast.LENGTH_SHORT).show();
            }
        });
        mQuickAdapter.setOnRecyclerViewItemChildClickListener(new BaseQuickAdapter.OnRecyclerViewItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                Toast.makeText(ListviewGroupingActivity.this, "没有更多", Toast.LENGTH_LONG).show();
            }
        });
    }
    public void initdate(int PageIndex,final Boolean isJz){
        Map<String,String> map=new HashMap<String,String>();
        map.put("ProvinceIds","");
        map.put("Classify","");
        map.put("CollegeLevel","");
        map.put("IsBen","");
        map.put("PageIndex",PageIndex+"");
        map.put("PageSize","4");
        JSONObject json=new JSONObject(map);
        VolleyReQuest.ReQuestPost_null(this, Constant.DATA_URL, "school_list_post", json, new VolleyInterface(this, VolleyInterface.mLisener, VolleyInterface.mErrorLisener) {
            @Override
            public JSONObject onMySuccess(JSONObject response) {
                Log.d("FindUniversityActivity", response.toString());
                httpDate(response, isJz);
                return response;
            }

            @Override
            public String onMyError(VolleyError error) {
                toError();
                return null;
            }
        });
    }
    private void httpDate(JSONObject response,Boolean isJz) {
        IsError error = JsonData.josnToObj(response);
        if (error.getCode() == 1) {
            try {
                JSONArray array1 = response.getJSONArray("Results");
                Gson gson = new Gson();
                if (isJz) {
                    List<UniversityListDto> expertLists = gson.fromJson(array1.toString(),
                            new TypeToken<List<UniversityListDto>>() {
                            }.getType());
                    if (expertLists.size() == 0) {
                        mQuickAdapter.notifyDataChangedAfterLoadMore(false);
                        View view = getLayoutInflater().inflate(R.layout.not_loading, (ViewGroup) mRecyclerView.getParent(), false);
                        mQuickAdapter.addFooterView(view);
                    } else {
                        mQuickAdapter.notifyDataChangedAfterLoadMore(getSampleData(expertLists), true);
                    }
                } else {
                    List<UniversityListDto> expertLists  = gson.fromJson(array1.toString(),
                            new TypeToken<List<UniversityListDto>>() {
                            }.getType());
                    if(expertLists.size()==0) {
                        toEmpty();
                    }else{
                        mQuickAdapter.setNewData(getSampleData(expertLists));
                        mQuickAdapter.openLoadMore(4,true);
                        swipeLayout.setRefreshing(false);
                        progress.showContent();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                toEmpty();
            }
        } else {
            toError();
        }
    }
    public void toError(){
        try {
            mQuickAdapter.notifyDataChangedAfterLoadMore(false);
            View view = getLayoutInflater().inflate(R.layout.not_loading, (ViewGroup) mRecyclerView.getParent(), false);
            mQuickAdapter.addFooterView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        progress.showError(getResources().getDrawable(R.mipmap.monkey_cry), Constant.ERROR_TITLE, Constant.ERROR_CONTEXT, Constant.ERROR_BUTTON, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.showLoading();
                initdate(1,false);
            }
        });
    }
    public void toEmpty(){
        progress.showEmpty(getResources().getDrawable(R.mipmap.monkey_cry),Constant.EMPTY_TITLE,Constant.EMPTY_CONTEXT);
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        PageIndex=1;
        initdate(PageIndex,false);
    }
    //自动加载
    @Override
    public void onLoadMoreRequested() {
        PageIndex++;
        initdate(PageIndex,true);
    }
    public List<MySection> getSampleData(List<UniversityListDto> expertLists) {
        List<MySection> list = new ArrayList<>();
        if(PageIndex%2==0){
            list.add(new MySection(true, "分组"+PageIndex, false));
        }else{
            list.add(new MySection(true, "分组"+PageIndex, true));
        }
        for (UniversityListDto UniversityListDto:expertLists) {
            list.add(new MySection(UniversityListDto));
        }
        return list;
    }
}
