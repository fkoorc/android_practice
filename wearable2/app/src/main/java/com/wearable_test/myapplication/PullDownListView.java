package com.wearable_test.myapplication;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class PullDownListView extends ListView implements OnScrollListener {

    private ListViewTouchEventListener mTouchListener;
    private boolean pulledDown;

    public PullDownListView(Context context) {
        super(context);
        init();
    }

    public PullDownListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullDownListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnScrollListener(this);
    }

    private float lastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastY = ev.getRawY();
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float newY = ev.getRawY();
            setPulledDown((newY - lastY) > 0);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isPulledDown()) {
                        if (mTouchListener != null) {
                            mTouchListener.onListViewPulledDown();
                            setPulledDown(false);
                        }
                    }
                }
            }, 400);
            lastY = newY;
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            lastY = 0;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        setPulledDown(false);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public interface ListViewTouchEventListener {
        public void onListViewPulledDown();
    }

    public void setListViewTouchListener(
            ListViewTouchEventListener touchListener) {
        this.mTouchListener = touchListener;
    }

    public ListViewTouchEventListener getListViewTouchListener() {
        return mTouchListener;
    }


    public boolean isPulledDown() {
        return pulledDown;
    }

    public void setPulledDown(boolean pulledDown) {
        this.pulledDown = pulledDown;
    }
}