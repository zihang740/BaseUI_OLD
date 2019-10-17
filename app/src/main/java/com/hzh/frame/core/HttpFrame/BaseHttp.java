package com.hzh.frame.core.HttpFrame;

import android.app.Activity;

import com.activeandroid.query.Select;
import com.hzh.frame.R;
import com.hzh.frame.comn.callback.HttpCallBack;
import com.hzh.frame.comn.model.BaseHttpCache;
import com.hzh.frame.comn.model.BaseHttpRequest;
import com.hzh.frame.core.BaseSP;
import com.hzh.frame.core.HttpFrame.api.ApiRequest;
import com.hzh.frame.core.HttpFrame.config.BaseHttpConfig;
import com.hzh.frame.util.Util;
import com.hzh.frame.widget.toast.BaseToast;
import com.hzh.frame.widget.xdialog.XDialogSubmit;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

public class BaseHttp {
    private BaseHttpConfig config;
    private static BaseHttp _instance;

    public static BaseHttp getInstance(){
        synchronized(BaseHttp.class){
            if(_instance==null){
                _instance=new BaseHttp();
            }
            return _instance;
        }
    }

    public void init(BaseHttpConfig config){
        this.config=config;
    }

    public BaseHttpConfig getConfig() {
        return config;
    }

    /**----------------------------start--- 使用Retrofit2发送网络请求 ---start----------------------------**/
    private static Map<Class, ApiRequest> requestPool = new HashMap<>();
    public <T extends ApiRequest> T getRequest(Class<T> clazz){
        ApiRequest request=requestPool.get(clazz);
        if(request==null){
            try {
                request= (ApiRequest) clazz.newInstance();
                request.setServer(config.getRetrofit().create(ApiRequest.Server.class));
                requestPool.put(clazz,request);
            } catch (Exception e) {
                throw new Error("instance api error:" + e.getMessage());
            }
        }
        return (T) request;
    }
    /**----------------------------end--- 使用Retrofit2发送网络请求 ---end----------------------------**/

    /**----------------------------start--- 直接调用OKhttp3发送网络请求 ---start----------------------------**/

    /**
     * 查询接口(post)
     * @param port 接口编号
     * @param params 查询参数
     * @param callBack 回调方法
     * **/
    public void query(Integer port, JSONObject params, HttpCallBack callBack) {
        callBack.setPort(port);//接口编号
        callBack.setRequestType(HttpCallBack.REQUEST_QUERY);
        post(config.getBaseUrl()+config.getQueryPath(),createFormBody(port,params,callBack),callBack);
    }

    /**
     * 写入接口(post) | 默认拦截重复请求并弹出请求窗口(这里拦截只判断port,有需求可以把requestParams加进判断中)
     * @param activity 当前活动(主要作用于弹出请求加载框,传null表示不需要请求加载框)
     * @param port 接口编号
     * @param params 写入参数
     * @param callBack 回调方法
     * **/
    public <T extends Activity> void write(T activity,int port, JSONObject params, HttpCallBack callBack) {
        repeatHttpInterceptor(port,activity,callBack);
        callBack.setPort(port);//接口编号
        callBack.setRequestType(HttpCallBack.REQUEST_WRITE);
        post(config.getBaseUrl()+config.getWritePath(),createFormBody(port,params,callBack),callBack);
    }


    /**
     * 写入接口(post) | 绿色通道 | 不拦截重复请求不弹出请求窗口
     * @param port 接口编号
     * @param params 写入参数
     * @param callBack 回调方法
     * **/
    public void writeGreen(int port, JSONObject params,HttpCallBack callBack) {
        post(config.getBaseUrl()+config.getWritePath(),createFormBody(port,params,callBack),callBack);
    }


    /**
     * 发送Get请求
     * @param url 请求路径
     * @param callback 本地封装的继承至Okhttp3的Callback
     * **/
    public void get(String url,HttpCallBack callback){
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        config.getClient().newCall(request).enqueue(new OkhttpCallback(callback));
    }

    /**
     * 发送Post请求
     * @param url 请求路径
     * @param body 请求体
     * @param callback 自己写的一个回调抽象类HttpCallBack
     * **/
    private void post(String url,FormBody body,HttpCallBack callback){
        Request request = new Request.Builder()
                .header("user-agent", "android")
                .addHeader("language", BaseSP.getInstance().getString("language"))
                .addHeader("token", BaseSP.getInstance().getString("token"))
                .post(body)
                .url(url)
                .build();
        config.getClient().newCall(request).enqueue(new OkhttpCallback(callback));
    }

    /**
     * 重复发送http拦截器
     * @param port 接口编号
     * @param activity 当前活动窗口
     * @param callBack 继承至HttpCallBack
     * **/
    public void repeatHttpInterceptor(Integer port,Activity activity,HttpCallBack callBack){
        BaseHttpRequest model=new Select().from(BaseHttpRequest.class).where("port = "+port).executeSingle();
        if(model!=null){
            //当前port接口有请求记录
            if(model.getState()==2){//拦截
                BaseToast.getInstance().setView(R.layout.base_view_toast_yllow).setMsg(R.id.content,"别点了,请求正在途中...").show();
                return;
            }else{//正常发送
                model.setState(2).save();
                if(activity!=null){
                    callBack.setSubmit(new XDialogSubmit(activity).alert());
                }
            }
        }else{
            //当前port接口无请求记录
            new BaseHttpRequest().setPort(port).setState(2).setType(HttpCallBack.REQUEST_WRITE).save();
            if(activity!=null){
                callBack.setSubmit(new XDialogSubmit(activity).alert());
            }
        }
    }

    /**
     * 缓存调用拦截器
     * @param port 接口编号
     * @param params 传参
     * @param callBack 继承至HttpCallBack
     * **/
    public void cacheInterceptor(Integer port,JSONObject params,HttpCallBack callBack){
        if(callBack.getCache()){
            if(port!=null){
                if(params!=null && HttpCallBack.START_PAGE!=params.optInt("page")){
                    //有页码且非初始页码
                    callBack.setPage(params.optInt("page"));
                }else{
                    //无页码或是初始页码
                    callBack.setPage(HttpCallBack.START_PAGE);
                }
                if(callBack.getPage()==HttpCallBack.START_PAGE){
                    //第一页缓存数据
                    BaseHttpCache cache=new Select().from(BaseHttpCache.class).where("port = "+port+" and page = "+callBack.getPage()).executeSingle();
                    if(cache!=null){
                        try {
                            //先拿出缓存数据回调一次,稍后再回调一次服务器数据
                            callBack.onSuccess(new JSONObject(cache.getResponseParams()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 转换Params
     * @param port 接口编号
     * @param params 传参
     * @param callBack 继承至HttpCallBack
     * **/
    public FormBody createFormBody(Integer port,JSONObject params,HttpCallBack callBack){
        cacheInterceptor(port,params,callBack);
        FormBody.Builder body = new FormBody.Builder();
        body.add("num", port+"");
        body.add("version", config.getVersion()+"");
        try {
            if (params == null) {
                params=new JSONObject();
                params.put("random", Math.random());
            }
            params.put("language", BaseSP.getInstance().getString("language"));
            params.put("token", BaseSP.getInstance().getString("token"));
            String encodeParams=URLEncoder.encode(params.toString(), "UTF-8");
            body.add("data", encodeParams);
            body.add("key", Util.createHttpKey(encodeParams));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return body.build();
    }




    //这里的OKhttpCallback是一个子线程,不能在这里面直接调用UI线程View做修改(可改成Rxjava实现更简洁)
    class OkhttpCallback implements Callback{
        private HttpCallBack httpCallBack;

        OkhttpCallback(HttpCallBack httpCallBack){
            this.httpCallBack=httpCallBack;
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                if(HttpCallBack.RESPONSE_JSONOBJECT==httpCallBack.getResponseType()){
                    Flowable.just(response.body().string())
                            .onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<String>() {
                                @Override
                                public void accept(String response) throws IOException {
                                    httpCallBack.onResponse(response);
                                }
                            });
                } else
                if(HttpCallBack.RESPONSE_BYTEARRAY==httpCallBack.getResponseType()){
                    Flowable.just(response.body().bytes())
                            .onBackpressureBuffer()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<byte[]>() {
                                @Override
                                public void accept(byte[] bytes) throws Exception {
                                    httpCallBack.onResponse(bytes);
                                }
                            });
                }
            } else {
                Flowable.just("")
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws IOException {
                                httpCallBack.onFailure();
                            }
                        });
            }
        }

        @Override
        public void onFailure(Call call, IOException e) {
            Flowable.just("")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws IOException {
                            httpCallBack.onFailure();
                        }
                    });
        }
    }

    /**----------------------------end--- 直接调用OKhttp3发送网络请求 ---end----------------------------**/
}
