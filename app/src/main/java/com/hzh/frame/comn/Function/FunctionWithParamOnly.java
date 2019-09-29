package com.hzh.frame.comn.Function;

/**
 * 帮助理解泛型 http://blog.csdn.net/s10461/article/details/53941091
 * @version 1.0
 * @date 2018/1/31
 */

public abstract class FunctionWithParamOnly<Param> extends Function{


    public FunctionWithParamOnly(String functionName) {
        super(functionName);
    }
    
    public abstract void function(Param param);
}
