package com.hzh.frame.comn.Function;

import com.hzh.frame.comn.Excption.FunctionExcption;
import com.hzh.frame.util.Util;

import java.util.HashMap;

/**
 * 帮助理解泛型 http://blog.csdn.net/s10461/article/details/53941091
 * @version 1.0
 * @date 2018/1/31
 */

public class FunctionsManager {
    
    private static FunctionsManager _instance;
    
    private HashMap<String,FunctionNoParamNoResult> mFunctionNoParamNoResult;
    private HashMap<String,FunctionWithParamOnly> mFunctionWithParamOnly;
    private HashMap<String,FunctionWithResultOnly> mFunctionWithResultOnly;
    private HashMap<String,FunctionWithParamAndResult> mFunctionWithParamAndResult;

    //单例模式获取FunctionsManager
    public static FunctionsManager getInstance(){
        if(null==_instance){
            _instance=new FunctionsManager();
        }
        return _instance;
    }

    private FunctionsManager(){
        mFunctionNoParamNoResult=new HashMap<String,FunctionNoParamNoResult>();
        mFunctionWithParamOnly=new HashMap<String,FunctionWithParamOnly>();
        mFunctionWithResultOnly=new HashMap<String,FunctionWithResultOnly>();
        mFunctionWithParamAndResult=new HashMap<String,FunctionWithParamAndResult>();
    }
    
    /**
     * 调用无参无返回值接口
     * */
    public void invoke(String funcName){
        if (Util.isEmpty(funcName)){
            return;
        }
        if (null!=mFunctionNoParamNoResult){
            FunctionNoParamNoResult f = mFunctionNoParamNoResult.get(funcName);
            if (null!=f){
                f.function();
            }else{
                try {
                    throw new FunctionExcption("Has no this Function:"+funcName);
                } catch (FunctionExcption e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 调用无参有返回值接口
     * */
    public <Result> Result invoke(String funcName,Class<Result> c){
        if (Util.isEmpty(funcName)){
            return null;
        }
        if (null!=mFunctionWithResultOnly){
            FunctionWithResultOnly f = mFunctionWithResultOnly.get(funcName);
            if (null!=f){
                if (null!=c){
                    return c.cast(f.function());
                }else{
                    return (Result)f.function();
                }
            }else{
                try {
                    throw new FunctionExcption("Has no this Function:"+funcName);
                } catch (FunctionExcption e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    /**
     * 调用有参无返回值接口
     * */
    public <Param> void invoke(String funcName,Param data){
        if (Util.isEmpty(funcName)){
            return;
        }
        if (null!=mFunctionWithParamOnly){
            FunctionWithParamOnly f = mFunctionWithParamOnly.get(funcName);
            if (null!=f){
                f.function(data);
            }else{
                try {
                    throw new FunctionExcption("Has no this Function:"+funcName);
                } catch (FunctionExcption e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 调用有参有返回值接口
     * */
    public <Param,Result> Result invoke(String funcName,Param data,Class<Result> c){
        if (Util.isEmpty(funcName)){
            return null;
        }
        if (null!=mFunctionWithParamAndResult){
            FunctionWithParamAndResult f = mFunctionWithParamAndResult.get(funcName);
            if (null!=f){
                if (null!=c){
                    return c.cast(f.function(data));
                }else{
                    return (Result)f.function(data);
                }
            }else{
                try {
                    throw new FunctionExcption("Has no this Function:"+funcName);
                } catch (FunctionExcption e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public FunctionsManager addFunction(FunctionNoParamNoResult function){
        mFunctionNoParamNoResult.put(function.mFunctionName,function);
        return this;
    }

    public FunctionsManager addFunction(FunctionWithParamOnly function){
        mFunctionWithParamOnly.put(function.mFunctionName,function);
        return this;
    }

    public FunctionsManager addFunction(FunctionWithResultOnly function){
        mFunctionWithResultOnly.put(function.mFunctionName,function);
        return this;
    }

    public FunctionsManager addFunction(FunctionWithParamAndResult function){
        mFunctionWithParamAndResult.put(function.mFunctionName,function);
        return this;
    }
}
