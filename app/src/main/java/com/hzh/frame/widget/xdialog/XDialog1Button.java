package com.hzh.frame.widget.xdialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hzh.frame.R;

/**
 * 1个按钮的选择框Dialog
 * */
public class XDialog1Button extends Dialog {
	
	private TextView content,confirm,line;
	private XDialog1ButtonCallBack callbackThis;

	public XDialog1Button(Context context) {
		super(context, R.style.XSubmitDialog);
		LayoutInflater in = LayoutInflater.from(context);
		View viewDialog = in.inflate(R.layout.base_xdialog_1button, null);
		content=(TextView) viewDialog.findViewById(R.id.content);
		confirm=(TextView) viewDialog.findViewById(R.id.confirm);
		line=(TextView) viewDialog.findViewById(R.id.line);
		confirm.setVisibility(View.GONE);
		line.setVisibility(View.GONE);
		setContentView(viewDialog);
		// 点击对话框外部取消对话框显示
		setCanceledOnTouchOutside(true);
	}

	/**
	 * @param context
	 * @param msg 提示内容
	 * @param callback 选择结果回调
	 * */
	public XDialog1Button(Context context,String msg,XDialog1ButtonCallBack callback) {
		super(context, R.style.XSubmitDialog);
		callbackThis=callback;
		LayoutInflater in = LayoutInflater.from(context);
		View viewDialog = in.inflate(R.layout.base_xdialog_1button, null);
		content=(TextView) viewDialog.findViewById(R.id.content);
		confirm=(TextView) viewDialog.findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callbackThis.confirm();
				dismiss();
			}
		});
		content.setText(msg);
		setContentView(viewDialog);
		// 点击对话框外部取消对话框显示
		setCanceledOnTouchOutside(true);
	}
	
	public XDialog1Button setButtonName(String name){
        confirm.setText(name);
        return this;
    }
	
	/**
	 * 选择结果回调
	 * */
	public interface XDialog1ButtonCallBack{
		  /**
		   * 确定
		   */
		  void confirm();
	}
}
