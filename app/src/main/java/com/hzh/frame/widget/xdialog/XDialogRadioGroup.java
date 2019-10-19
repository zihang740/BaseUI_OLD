package com.hzh.frame.widget.xdialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.hzh.frame.R;

import java.util.HashMap;
import java.util.List;

/**
 * 单选按钮组Dialog
 * */
public class XDialogRadioGroup{

	private Dialog alertDialog;
	private List<HashMap<String, Object>> items;//所有单选项
	private HashMap<String, Object> item=new HashMap<String, Object>();//当前选中项
	private String[] itemNames;//名称
    Context mContext;
	
	/**
	 * 单选按钮
	 * @param context
	 * @param list 单选按钮{name名称  isSelected是否选中(不传默认未选中)}
	 * @param callback 单选按钮组选择结果回调
	 * */
	public void show(Context context,List<HashMap<String, Object>> list,final IRadioGroupDialogCallBack callback) {
	    mContext=context;
		//防止网络延迟情况下多次点击弹出多个窗口
		dismiss();
		items=list;
		//默认选中的项
		int selectedIndex=0;
		//单选项
		itemNames=new String[items.size()];
		for(int i = 0;i<items.size();i++){
			itemNames[i]=(String) items.get(i).get("name");
			if(items.get(i).get("isSelected")!=null){
				if((Boolean) items.get(i).get("isSelected")){
					selectedIndex=i;
				}
			}
		}
		//设置默认选中值
		item=items.get(selectedIndex);
        alertDialog = new AlertDialog.Builder(context)
                .setSingleChoiceItems(itemNames, selectedIndex, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int index) {
			        	item=items.get(index);
		            }
		        })
                .setPositiveButton(mContext.getString(R.string.base_confirm), new DialogInterface.OnClickListener() {   
                    @Override   
                    public void onClick(DialogInterface dialog, int which) {   
                    	callback.confirm(item);
                    }   
                })
                .setNegativeButton(mContext.getString(R.string.base_cancel), new DialogInterface.OnClickListener() {   
   
                    @Override   
                    public void onClick(DialogInterface dialog, int which) {   
                    	callback.cancel();
                    }   
                })
                .create();   
        alertDialog.show();
	}
	
	/**
	 * 关闭当前Dialog
	 * */
	public void dismiss(){
		if(alertDialog!=null && alertDialog.isShowing()){
			alertDialog.dismiss();
		}
	}
	
	/**
	 * 单选按钮组选择结果回调
	 * */
	public interface IRadioGroupDialogCallBack{
		  /**
		   * 确定
		   * @param selectedItem 返回选择的结果项
		   */
		  void confirm(HashMap<String, Object> selectedItem);
		  
		  /**
		   * 取消
		   */
		  void cancel();
	}
}
