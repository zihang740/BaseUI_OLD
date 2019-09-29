package com.hzh.frame.core;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.baidu.mobstat.StatService;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.hzh.frame.BaseInitData;
import com.hzh.frame.core.HttpFrame.BaseHttp;
import com.hzh.frame.core.HttpFrame.config.BaseHttpConfig;

/**
 * @author
 * @version 1.0
 * @date 2018/5/9
 */
public class BaseFrame {

    // 程序创建的时候执行
    public static void init(Application application) {
        //数据库框架
        ActiveAndroid.initialize(application);
        //Facebook图片请求框架
        Fresco.initialize(application);
        //请求框架
        initHttp();
        //百度移动统计可视化
        initBaiduStatistics(application);
        //SharedPreferences
        initSP(application);
    }

    // 程序终止的时候执行
    public static void stop() {
        ActiveAndroid.dispose();
    }
    
    public static void initHttp(){
        BaseHttpConfig config = new BaseHttpConfig.Builder()
                .baseUrl(BaseInitData.http_client_url)
                .timeOut(20)
                .queryPath("query.do")
                .writePath("write.do")
                .version(BaseInitData.http_client_version)
                .build();
        BaseHttp.getInstance().init(config);
    }
    
    
    public static void initBaiduStatistics(Application application){
        // 打开调试开关，可以查看logcat日志。版本发布前，为避免影响性能，移除此代码
        // 查看方法：adb logcat -s sdkstat
//        StatService.setDebugOn(true);

        // 开启自动埋点统计，为保证所有页面都能准确统计，建议在Application中调用。
        // 第三个参数：autoTrackWebview：
        // 如果设置为true，则自动track所有webview；如果设置为false，则不自动track webview，
        // 如需对webview进行统计，需要对特定webview调用trackWebView() 即可。
        // 重要：如果有对webview设置过webchromeclient，则需要调用trackWebView() 接口将WebChromeClient对象传入，
        // 否则开发者自定义的回调无法收到。
        StatService.autoTrace(application, true, true);

        // 根据需求使用
        // StatService.autoTrace(this, true, false);
    }

    public static void initSP(Application application){
        BaseSP.getInstance().init(application);
    }
 
}
