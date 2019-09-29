package com.hzh.frame.ui.fragment;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.activeandroid.Model;
import com.activeandroid.query.Delete;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.hzh.frame.R;
import com.hzh.frame.comn.ItemDecoration.BaseItemDecoration;
import com.hzh.frame.comn.callback.HttpCallBack;
import com.hzh.frame.core.HttpFrame.BaseHttp;
import com.hzh.frame.util.Util;
import com.hzh.frame.widget.xlistview.XListViewFooter;
import com.hzh.frame.widget.xrecyclerview.BaseRecyclerAdapter;
import com.hzh.frame.widget.xrecyclerview.RecyclerViewHolder;
import com.hzh.frame.widget.xrefresh.XSwipeRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存只做第一次显示数据用(其他加载及时响应)
 * */
public abstract class AbsRecyclerViewFM<T extends Model> extends BaseFM implements OnRefreshListener {

	private Class<T> modelClass;
	private RecyclerView mRecyclerView;
	private LinearLayout mRecyclerViewBg;
	public XSwipeRefreshLayout mSwipeRefreshLayout;
	private BaseRecyclerAdapter<T> mAdapter;
	private int[] pageInfo;//int[0]:起始页码;int[1]:当前页码 ;int[2]:每页显示数
	private boolean httpState=true;
    /**
     * 加载模式 <br />
     * 0:1.先加载本地数据;
     *   2.再加载网络数据 <br />
     * 1:1.只加载本地数据 <br />
     * 2:1.只加载网络数据 <br />
     * 3:1.先下载网络数据到本地;
     *   2.再加载本地数据 <br />
     * 4:1.先加载本地数据;
     *   2.再下载网络数据到本地;
     *   3.再加载本地数据  <br />
     * */
	private int loadPattern=0;
	/**设置框架加载网络数据后是否更新本地数据库 <br />false:不更新 <br />true:更新*/
	private boolean updLocalData=true;
	
	@Override
	protected void onCreateBase() {
		pageInfo=setPageInfo();
    	//获取T.Class
    	modelClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    	View view = setContentView(setLayoutId());
    	//下拉刷新、加载更多
    	mSwipeRefreshLayout=(XSwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
    	mSwipeRefreshLayout.setColorSchemeResources(R.color.base_color);
    	mSwipeRefreshLayout.setOnRefreshListener(this);
    	mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
    	mRecyclerView.setHasFixedSize(true);
    	mRecyclerView.setLayoutManager(setRecyclerViewLayoutManager());
        mRecyclerView.addItemDecoration(setRecyclerViewItemDecoration());
        mRecyclerView.addOnScrollListener(new MyOnScrollListener());
        mAdapter=setAdapter();
		mRecyclerView.setAdapter(mAdapter);
		//显示背景图片
    	mRecyclerViewBg=(LinearLayout) view.findViewById(R.id.recyclerViewBg);
    	mRecyclerViewBg.setVisibility(View.VISIBLE);
		bindView(view);
		if(setHeadLayoutId() != 0) {
            mAdapter.setHeaderView(inflater.inflate(setHeadLayoutId(),mRecyclerView,false));
        }
		if(setFooterLayoutId() != 0){
            mAdapter.setFooterView(inflater.inflate(setFooterLayoutId(),mRecyclerView,false));
        }
        loadData();
	}
	
	@Override
	public void onRefresh() {
		//下拉刷新
		pageInfo[1]=pageInfo[0];
        loadData();
	}

    //统一加载数据入口
    public void loadData(){
        switch (loadPattern) {
            case 0://先加载本地再加载网络数据
                loadLocalData();
                loadNetworkData(pageInfo[1],pageInfo[2]);
                break;
            case 1://只加载本地数据
                loadLocalData();
                break;
            case 2://只加载网络数据
                loadNetworkData(pageInfo[1],pageInfo[2]);
                break;
            case 3://先加载网络数据在加载本地数据
                loadNetworkData(pageInfo[1],pageInfo[2]);
                break;
            case 4://1.先加载本地数据;2.再下载网络数据到本地;3.再加载本地数据
                loadLocalData();
                loadNetworkData(pageInfo[1],pageInfo[2]);
                break;
        }
    }

    // 加载本地缓存数据(这里只做第一次点击进来第一个页面的缓存)
    private void loadLocalData() {
        From from=new Select().from(modelClass);
        //获取查询条件
        from=setSqlParams(from);
        //得到缓存数据
        List<T> mDatas=from.execute();

        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter.setDatas(mDatas);
        
        if(mDatas.size()>0){
            //有缓存数据,去除背景图片
            mRecyclerViewBg.setVisibility(View.GONE);
        }else{
            //无缓存数据,显示背景图片
            mRecyclerViewBg.setVisibility(View.VISIBLE);
        }
    }

    // 加载网络数据
    public void loadNetworkData(int page, int limit) {
        JSONObject params=setHttpParams();
        try {
            params.put("page", page);
            params.put("limit", limit);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        BaseHttp.getInstance().query(setHttpPath(), params, new CallBack(page,limit));
    }
    
    class CallBack extends HttpCallBack{
        public int page;
        public int limit;
	    
	    public CallBack(int page, int limit){
	        this.page=page;
	        this.limit=limit;
        }
	    
        @Override
        public void onSuccess(JSONObject response) {
            //只要有请求回来就关闭加载中状态
            if(null!=mAdapter.getFooterView()){
                ((XListViewFooter)mAdapter.getFooterView()).setState(XListViewFooter.STATE_NORMAL);
            }
            httpState=true;
            List<T> mDatas=handleHttpData(response);
            if(httpState){
                //请求正常前端UI开始更新
                if(mDatas!=null && mDatas.size()>0){
                    //接口返回正常
                    if(updLocalData){
                        //放本地数据库缓存
                        From from=new Delete().from(modelClass);
                        from=setDeleteSqlParams(from);
                        from.execute();
                        for(T model:mDatas){
                            model.save();
                        }
                    }
                    if(pageInfo[0]==pageInfo[1]){
                        //下拉刷新
                        if(loadPattern==3 || loadPattern==4){
                            loadLocalData();
                        }else{
                            mAdapter.setDatas(mDatas);
                        }
                        if(mDatas.size()>=limit){
                            mAdapter.setFooterView(createFooterView());
                        }
                    }else{
                        //加载更多
                        if(mDatas.size()<limit){
                            mAdapter.removeFooterView();
                        }
                        mAdapter.getDatas().addAll(mDatas);
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    //去除背景图片
                    mRecyclerViewBg.setVisibility(View.GONE);
                }else{
                    //接口返回无数据或错误
                    mSwipeRefreshLayout.setRefreshing(false);
                    if(pageInfo[0]==pageInfo[1]){
                        //下拉刷新
                        From from=new Delete().from(modelClass);
                        from=setDeleteSqlParams(from);
                        from.execute();
                        if(mDatas==null){
                            mDatas=new ArrayList<T>();
                        }
                        if(loadPattern==3 || loadPattern==4){
                            loadLocalData();
                        }else{
                            mAdapter.setDatas(mDatas);
                        }
                        //显示背景图片
                        mRecyclerViewBg.setVisibility(View.VISIBLE);
                    }else{
                        //加载更多
                        mAdapter.removeFooterView();
                    }
                }
            }else{
                //请求异常前端UI不做任何响应
                mSwipeRefreshLayout.setRefreshing(false);
                if(pageInfo[0]!=pageInfo[1]){
                    //加载更多  恢复到之前页码
                    pageInfo[1]=pageInfo[1]-1;
                }
            }
            if(mAdapter!=null && mAdapter.getFooterView()!=null){
                mAdapter.getFooterView().setClickable(true);
            }
        }
        @Override
        public void onFail() {
            handleHttpDataFailure();
            //只要有请求回来就关闭加载中状态
            if(null!=mAdapter.getFooterView()){
                ((XListViewFooter)mAdapter.getFooterView()).setState(XListViewFooter.STATE_NORMAL);
            }
            mSwipeRefreshLayout.setRefreshing(false);
            //加载更多  恢复到之前页码
            if(pageInfo[0]!=pageInfo[1]){
                pageInfo[1]=pageInfo[1]-1;
            }
            if(mAdapter!=null && mAdapter.getFooterView()!=null){
                mAdapter.getFooterView().setClickable(true);
            }
        }
    }

    //RecyclerView适配器
    protected class MyAdapter extends BaseRecyclerAdapter<T> {

        public MyAdapter(Context ctx, List<T> list) {
            super(ctx, list);
        }
        @Override
        public int setItemChildViewType(int position) {
            return setItemViewType(position);
        }
        @Override
        public int getItemLayoutId(int viewType) {
            return setItemLayoutId(viewType);
        }
        @Override
        public void bindData(RecyclerViewHolder holder, int position, final T item) {
            bindItemData(holder,position,item);
        }

        public List<T> getDatalist(){return super.getDatas();}

        @Override
        //重写此方法可实现动态设置每个item项在行、列中所占的份数
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            setSpanSize(recyclerView);
        }

    }

    /**
     * 创建脚布局
     * */
    public XListViewFooter createFooterView(){
        XListViewFooter mFooterView = new XListViewFooter(getActivity());
        mFooterView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.WRAP_CONTENT));
        mFooterView.setState(XListViewFooter.STATE_NORMAL);//设置加载更多状态
        mFooterView.setBackgroundColor(ContextCompat.getColor(mRecyclerView.getContext(),R.color.white));
        mFooterView.setOnClickListener(new View.OnClickListener() {//绑定脚监听
            @Override
            public void onClick(View v) {
                //保证多次响应只响应一次
                if(mAdapter.getFooterView().isClickable()==true) {
                    ((XListViewFooter) v).setState(XListViewFooter.STATE_LOADING);
                    pageInfo[1]++;
                    loadNetworkData(pageInfo[1],pageInfo[2]);
                }
            }
        });
        return mFooterView;
    }

    // RecyclerView的滑动监听事件
    private class MyOnScrollListener extends RecyclerView.OnScrollListener {
        /**
         * SCROLL_STATE_IDLE表示当前并不处于滑动状态 
         * SCROLL_STATE_DRAGGING表示当前RecyclerView处于滑动状态（手指在屏幕上） 
         * SCROLL_STATE_SETTLING表示当前RecyclerView处于滑动状态，（手已经离开屏幕）
         * */
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState==RecyclerView.SCROLL_STATE_IDLE && isVisBottom(recyclerView)){
                if(mAdapter.getFooterView()!=null && mAdapter.getFooterView().getVisibility()==View.VISIBLE){
                    mAdapter.getFooterView().performClick();
                }
            }
        }
    }

    //判断是否到达底部
    public static boolean isVisBottom(RecyclerView recyclerView){
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        //屏幕中最后一个可见子项的position
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        //当前屏幕所看到的子项个数
        int visibleItemCount = layoutManager.getChildCount();
        //当前RecyclerView的所有子项个数
        int totalItemCount = layoutManager.getItemCount();
        //RecyclerView的滑动状态
        int state = recyclerView.getScrollState();
        if(visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == recyclerView.SCROLL_STATE_IDLE){
            return true;
        }else {
            return false;
        }
    }
	
		
	/**
	 * 设置RecyclerView分页信息
	 * @return int[0]:起始页码 <br />
	 *         int[1]:当前页码 <br />
	 *         int[2]:每页显示数
	 * */
	public int[] setPageInfo() {
		return new int[]{1,1,20};
	}

    /**
     * 获取RecyclerView分页信息
     * @return int[0]:起始页码 <br />
     *         int[1]:当前页码 <br />
     *         int[2]:每页显示数
     * */
    public int[] getPageInfo(){
        return pageInfo;
    }
	
	/**
	 * 获取RecyclerView对象
	 * */
	public RecyclerView getRecyclerView() {
		return mRecyclerView;
	}
	/**
	 * 获取RecyclerView适配器
	 * */
	public BaseRecyclerAdapter<T> getAdapter(){
		return mAdapter;
	}
	/**
	 * 设置布局文件
	 * */
	protected abstract int setLayoutId();
    /**
     * 设置Item的layout资源文件ID
     * */
    protected abstract int setItemLayoutId(int viewType);
    /**
     * 设置除开头尾外的Item类型
     * */
    protected int setItemViewType(int position){return BaseRecyclerAdapter.TYPE_NORMAL;}
	/**
	 * 设置RecyclerView布局管理器(不设置默认LinearLayoutManager)
	 * */
	protected RecyclerView.LayoutManager setRecyclerViewLayoutManager(){return new LinearLayoutManager(getActivity()){
		@Override
		public boolean canScrollHorizontally() {
			return false;
		}
	};};
    /**
     * 设置RecyclerView条目装饰
     * */
    protected RecyclerView.ItemDecoration setRecyclerViewItemDecoration(){return new BaseItemDecoration(getActivity(),R.color.base_bg);};
	/**
	 * 绑定布局文件关联
	 * */
	protected void bindView(View view){};

    /**
     * 绑定头布局文件关联
     * */
	protected int setHeadLayoutId(){return 0;}
    /**
     * 绑定脚布局文件关联
     * */
    protected int setFooterLayoutId(){return 0;}
	
	/**
	 * 设置查询本地数据库SQL条件()
	 * */
	protected From setSqlParams(From from){return from;};
    /**
     * 设置删除本地数据库SQL条件()
     * */
    protected From setDeleteSqlParams(From from){return from;};
    /**
     * 设置请求路径
     * */
    protected String setHttpPath(){return "";}
	/**
	 * 设置请求参数
	 * */
	protected JSONObject setHttpParams(){return new JSONObject();};
	/**
	 * 处理HTTP请求成功回参数据
	 * */
    protected List<T> handleHttpData(JSONObject response){return null;}
	/**
	 * 处理HTTP请求失败回参数据
	 * */
	protected void handleHttpDataFailure(){
        switch (loadPattern){
            case 0:
                break;
            case 1:
                break;
            case 2:
                showLodingFail();
                break;
            case 3:
                break;
            case 4:
                break;
        }
	}
    /**
     * 处理HTTP请求状态
     * @param httpState 默认正常:true;终止页面响应:false
     * */
    protected void setHttpState(boolean httpState){this.httpState=httpState;}
    /**
     * 设置加载模式 <br />
     * 0:先加载本地再加载网络数据 <br />
     * 1:只加载本地数据 <br />
     * 2:只加载网络数据
     * 3:先加载网络数据在加载本地数据
     * */
    protected void setLoadPattern(int loadPattern){this.loadPattern=loadPattern;}
    /**
     * 设置框架加载网络数据后是否更新本地数据库 <br />
     * @param updLocalData false:不更新,true:更新
     */
    protected void setUpdLocalData(boolean updLocalData){this.updLocalData=updLocalData;}
	/**
	 * 绑定ItemView交互
	 * */
	protected abstract void bindItemData(RecyclerViewHolder holder, int position, T model);

    protected BaseRecyclerAdapter setAdapter(){
        return new MyAdapter(getActivity(), new ArrayList<T>());
    }

    //重写此方法可实现动态设置每个item项在行、列中所占的份数
    protected void setSpanSize(RecyclerView recyclerView){
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if(manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                /** 这个方法的返回值决定了我们每个position上的item占据的单元格个数 */
                public int getSpanSize(int position) {
                    //0xff18就是Constant.TYPE_RECOMMENDED_WARE,0xff19就是Constant.TYPE_SHOP_ITEM,凑合着用吧,骚年
                    if(mAdapter.getItemViewType(position) == 0xff18 || mAdapter.getItemViewType(position) == 0xff19){
                        return 2;
                    }else
                    if(mAdapter.getItemViewType(position) == mAdapter.TYPE_NORMAL){
                        return 1;
                    }else{
                        return gridManager.getSpanCount();
                    }
                }
            });
        }
    }
}