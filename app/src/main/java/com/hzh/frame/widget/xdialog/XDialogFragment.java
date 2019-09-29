package com.hzh.frame.widget.xdialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hzh.frame.R;

/**
 * 全屏无弹窗DialogFragment
 * */
public abstract class XDialogFragment extends DialogFragment {

    public static final String TAG="x_dialog_fragment";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.Theme_AppCompat_NoActionBar);//设置全屏无弹窗
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        return getLayout(inflater,container);
    }


    @Override
    public void show(FragmentManager manager, String tag) {
        if(getDialog()==null){
            super.show(manager, tag);
        } else
        if(getDialog().isShowing()){
            super.dismiss();
            super.show(manager, tag);
        }else{ 
            super.show(manager, tag);
        }
    }
    
    //获取布局文件
    protected abstract View getLayout(LayoutInflater inflater,ViewGroup root);
}
