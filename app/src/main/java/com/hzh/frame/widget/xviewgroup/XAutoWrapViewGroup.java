package com.hzh.frame.widget.xviewgroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class XAutoWrapViewGroup extends ViewGroup {
    private final static int VIEW_MARGIN = 15;
    
    public XAutoWrapViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public XAutoWrapViewGroup(Context context) {
        super(context);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int stages = 1;
        int stageHeight = 0;
        int stageWidth = 0;

        int wholeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            stageWidth += (child.getMeasuredWidth() + VIEW_MARGIN);
            stageHeight = child.getMeasuredHeight();
            if (stageWidth >= wholeWidth) {
                stages++;
                stageWidth = child.getMeasuredWidth();
            }
        }
        int wholeHeight = (stageHeight + VIEW_MARGIN) * stages;

        setMeasuredDimension(resolveSize(wholeWidth, widthMeasureSpec), resolveSize(wholeHeight, heightMeasureSpec));
//        setMeasuredDimension(wholeWidth, wholeHeight);
    }

    private int jiange = 10;//按钮之间的间隔

    @Override
    protected void onLayout(boolean arg0, int l, int t, int r, int b) {

        final int count = getChildCount();
        int row = 0;// which row lay you view relative to parent
        int lengthX = l;    // right position of child relative to parent
        int lengthY ;    // bottom position of child relative to parent
        for (int i = 0; i < count; i++) {

            final View child = this.getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            if (i == 0) {
                lengthX += width + VIEW_MARGIN;//第一个的时候不需要加
            } else {
                lengthX += width + VIEW_MARGIN + jiange;//按钮之间的间隔
            }

            lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height;
            if (lengthX > r) {
                lengthX = width + VIEW_MARGIN + l;
                row++;
                lengthY = row * (height + VIEW_MARGIN) + VIEW_MARGIN + height;
            }
            child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
        }
    }
}
