package com.example.administrator.selfview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by hongmaodan on 2016/7/26.
 */
public class MyHorScrollView extends ViewGroup{
    private Scroller scroller;
    private int touchSlop;
    private VelocityTracker velocityTracker;
    private float lastX=0,lastY=0;
    private float moveX=0,moveY=0;
    public MyHorScrollView(Context context) {
        super(context);
        init();
    }

    public MyHorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyHorScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        scroller = new Scroller(getContext());
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        velocityTracker = VelocityTracker.obtain();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
            setMeasuredDimension(widthSize,heightSize);
        }else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST){
            int widthTemp = 0;
            int heightTemp = 0;
            int count = getChildCount();
            for (int i=0;i<count;i++){
                View child = getChildAt(i);
                final MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                widthTemp += child.getMeasuredWidth()+layoutParams.rightMargin+layoutParams.leftMargin;
                if (child.getMeasuredHeight() > heightTemp){
                    heightTemp = child.getMeasuredHeight();
                }
            }
            setMeasuredDimension(widthTemp,heightTemp);
        }else if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.EXACTLY){
            int widthTemp = 0;
            int count = getChildCount();
            for (int i=0;i<count;i++){
                View child = getChildAt(i);
                final MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                widthTemp += child.getMeasuredWidth()+layoutParams.leftMargin+layoutParams.rightMargin;
            }
            setMeasuredDimension(widthTemp,heightSize);
        }else if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.AT_MOST){
            int heightTemp = 0;
            int count = getChildCount();
            for (int i=0;i<count;i++){
                View child = getChildAt(i);
                final MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                if ((child.getMeasuredHeight()+layoutParams.topMargin+layoutParams.bottomMargin) > heightTemp){
                    heightTemp = child.getMeasuredHeight()+layoutParams.topMargin+layoutParams.bottomMargin;
                }
            }
            setMeasuredDimension(widthSize,heightTemp);
        }else {
            setMeasuredDimension(widthSize,heightSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;
        int count = getChildCount();
        for (int i=0;i<count;i++){
            View view = getChildAt(i);
            final MarginLayoutParams  layoutParams;
            layoutParams = (MarginLayoutParams) view.getLayoutParams();
            view.layout(left+layoutParams.leftMargin,0+layoutParams.topMargin,left+view.getMeasuredWidth(),view.getMeasuredHeight());
            left += view.getMeasuredWidth()+layoutParams.rightMargin+layoutParams.leftMargin;
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        boolean isIntercept = false;
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                isIntercept = false;
                if (!scroller.isFinished()){
                    scroller.abortAnimation();
                    isIntercept = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float absX = Math.abs(x-lastX);
                float absY = Math.abs(y-lastY);
                if (absX>absY && absX>touchSlop){
                    isIntercept = true;
                }else{
                    isIntercept = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;
                break;
        }
        lastX = ev.getX();
        lastX = ev.getY();
        moveY = ev.getY();
        moveX = ev.getX();
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        velocityTracker.addMovement(event);
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()){
                    scroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float delX = x-moveX;
                float delY = y-moveY;
                scrollBy(-(int) delX, 0);
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.computeCurrentVelocity(1000);
                float veloX = velocityTracker.getXVelocity();
                if (Math.abs(veloX)>600){
                    smoothMoveTo(-veloX);
                }
                velocityTracker.clear();
                break;
        }
        moveX = x;
        moveY = y;
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()){
            scrollTo(scroller.getCurrX(),scroller.getCurrY());
            postInvalidate();
        }
    }


    public void smoothMoveTo(float delX){
        if (delX>3000){
            delX = 3000;
        }
        int time = (int)delX/1000;
        if (delX<1000){
            time = 1;
        }
        int dex = (int)(0.5*1000*time*time);
        if(delX<0){
            dex = -dex;
        }
        scroller.startScroll(getScrollX(),0,dex,0,time*1000);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        velocityTracker.recycle();
    }
}
