package com.hzh.frame;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hzh.frame.core.BaseFrame;
import com.hzh.frame.util.AndroidUtil;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;


public class BaseApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
        BaseInitData.applicationContext=this;
        if (AndroidUtil.isApkInDebug()) {           // 这两行必须写在init之前，否则这些配置在init过程中将无效
            ARouter.openLog();     // 打印日志
            ARouter.openDebug();   // 开启调试模式(如果在InstantRun模式下运行，必须开启调试模式！线上版本需要关闭,否则有安全风险)
        }
        //防止异常处理:RxJava OnErrorNotImplementedException
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                //异常处理
            }
        });
        ARouter.init(this);
		BaseFrame.init(this);
        ZXingLibrary.initDisplayOpinion(this);
    }

    
	@Override
    public void onTerminate() {
        super.onTerminate();
        BaseFrame.stop();
    }
	
}
