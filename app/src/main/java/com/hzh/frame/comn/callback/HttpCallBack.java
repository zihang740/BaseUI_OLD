package com.hzh.frame.comn.callback;

import android.content.Intent;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.hzh.frame.BaseInitData;
import com.hzh.frame.comn.model.BaseHttpCache;
import com.hzh.frame.comn.model.BaseHttpRequest;
import com.hzh.frame.util.Util;
import com.hzh.frame.widget.xdialog.XDialogSubmit;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class HttpCallBack{
    public static final int RESPONSE_JSONOBJECT= 0;//返回类型为JSONObject
    public static final int RESPONSE_BYTEARRAY= 1;//返回类型为byte[]
    public static final int REQUEST_QUERY= 0;//请求类型 | 读取类请求
    public static final int REQUEST_WRITE= 1;//请求类型 | 写入类请求
    public static final int START_PAGE= 1;//保存缓存数据起始页码

    private XDialogSubmit submit;
    private Integer port;//接口编号
    private Integer page;//页码
    private Integer requestType;//请求类型 REQUEST_QUERY:读请求 REQUEST_WRITE:写请求
    private Integer responseType;//默认返回类型为JSONObject
    private Boolean cache;//是否缓存

    public HttpCallBack(){
        this(RESPONSE_JSONOBJECT);
    }

    public HttpCallBack(Integer responseType){
        this.cache=false;
        this.responseType=responseType;
    }

    public HttpCallBack setSubmit(XDialogSubmit submit) {
        this.submit = submit;
        return this;
    }

    public HttpCallBack setPort(Integer port) {
        this.port = port;
        return this;
    }

    public HttpCallBack setPage(Integer page) {
        this.page = page;
        return this;
    }

    public HttpCallBack setRequestType(Integer requestType) {
        this.requestType = requestType;
        return this;
    }

    //响应缓存JSON | 默认无分页缓存
    public HttpCallBack cache(){
        this.cache=true;
        this.page=START_PAGE;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public int getResponseType() {
        return responseType;
    }

    public Boolean getCache() { return cache; }

    /**---------------------------------------------------------------------------------------------**/

    public void onResponse(String response){
        JSONObject jsonObject = null;
        try {
            jsonObject=new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(Util.isEmpty(response) || Util.isEmpty(jsonObject)){
            onFailure();
        }else{
            //责任链模式加载拦截器
            closeInterceptor(jsonObject);
            submitInterceptor();
            httpInterceptor();
            cacheInterceptor(response);
            onSuccess(jsonObject);
        }
    }

    public void onResponse(byte[] byteArray){
        onSuccess(byteArray);
    }

    public void onFailure() {
        //责任链模式加载拦截器
        submitInterceptor();
        httpInterceptor();
        onFail();
    }

    //强制关闭APP拦截
    public void closeInterceptor(JSONObject jsonObject){
        if(jsonObject.optInt("result")==-6){
            BaseInitData.applicationContext.sendBroadcast(new Intent("com.hzh.frame.close_app"));
        }
    }

    //关闭请求动画
    public void submitInterceptor(){
        if(submit!=null && !submit.activity.isFinishing() /*&& requestType==REQUEST_WRITE*/){
            submit.dismiss();
        }
    }

    //解开写请求的多次调用限制
    public void httpInterceptor(){
        if(port!=null && requestType==REQUEST_WRITE){
            new Update(BaseHttpRequest.class).set("state = 1").where("port = "+port).execute();
        }
    }

    //缓存response
    public void cacheInterceptor(String response){
        if(cache){
            if(port!=null){
                if(page==START_PAGE){
                    //暂时只缓存缓存接口的第一页数据
                    BaseHttpCache cacheModel=new Select().from(BaseHttpCache.class).where("port = "+port+" and page = "+page).executeSingle();
                    if(cacheModel!=null){
                        //更新缓存
                        cacheModel.setType(REQUEST_QUERY).setResponseParams(response).setLastTime(System.currentTimeMillis()).save();
                    }else{
                        //创建缓存
                        new BaseHttpCache().setPort(port).setType(REQUEST_QUERY).setPage(page).setResponseParams(response).setLastTime(System.currentTimeMillis()).save();
                    }
                }else{
                    //缓存接口的其他页数据不与缓存
                }
            }else{
                //缓存的Response接口编号port不能为空
            }
        }
    }

    public void onSuccess(JSONObject response){};

    public void onSuccess(byte[] byteArray){};

    public void onFail(){};


}
