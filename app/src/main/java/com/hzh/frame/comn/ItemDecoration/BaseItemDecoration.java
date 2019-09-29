package com.hzh.frame.comn.ItemDecoration;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hzh.frame.widget.xrecyclerview.BaseRecyclerAdapter;

/**
 * @author hzh
 * @version 1.0
 * @date 2017/8/21 
 */
public class BaseItemDecoration extends RecyclerView.ItemDecoration {

    private Context mContext;
    private Paint mPaint;
    
    public BaseItemDecoration(Context context,int lineColor) {
        mContext=context;
        mPaint=new Paint();
        mPaint.setColor(ContextCompat.getColor(mContext, lineColor));
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemPosition = parent.getChildLayoutPosition(view);
        switch (parent.getAdapter().getItemViewType(itemPosition)){
            case BaseRecyclerAdapter.TYPE_HEADER:
                outRect.bottom= 2;
                break;
            case BaseRecyclerAdapter.TYPE_FOOTER:
                break;
            case BaseRecyclerAdapter.TYPE_NORMAL:
                if(itemPosition!=parent.getAdapter().getItemCount()-1){
                    outRect.bottom= 2;
                }
                break;
        }
    }
}
