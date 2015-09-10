package voiddog.org.game24.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import voiddog.org.game24.util.MyViewDragHelper;
import voiddog.org.game24.util.UIHandler;

/**
 * 可以手势拖动的view
 * Created by Dog on 2015/9/7.
 */
public class DragGroupView extends FrameLayout implements Runnable{
    //帧数 48ms/帧
    private final long DELAY = 48l;
    private final int MIN_MOVE_DIS = 3;

    private MyViewDragHelper mViewDrag;
    //线程是否继续运行
    boolean mIsThreadRunning = false;

    public DragGroupView(Context context) {
        super(context);
        init();
    }

    public DragGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 开始线程
     */
    public void startThread(){
        if(mIsThreadRunning){
            mIsThreadRunning = false;

            UIHandler.sendEmptyMessageDelayed(0, 1000, new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    createAndRunThread();
                    return true;
                }
            });
        }
        else{
            createAndRunThread();
        }
    }

    /**
     * 结束线程
     */
    public void stopThread(){
        mIsThreadRunning = false;
    }

    void createAndRunThread(){
        Thread thread = new Thread(this);
        mIsThreadRunning = true;
        thread.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mViewDrag.shouldInterceptTouchEvent(ev);
        View view = mViewDrag.findTopChildUnder((int)ev.getX(), (int)ev.getY());
        if(view != null){
            if(view instanceof NumberItem){
                NumberItem item = (NumberItem) view;
                if(item.getStatus() == NumberItem.Status.NORMAL
                        || item.getStatus() == NumberItem.Status.PADDING){
                    item.performClick();
                }
            }
            else {
                view.performClick();
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDrag.processTouchEvent(event);
        return true;
    }

    /**
     * 初始化
     */
    void init(){

        mViewDrag = MyViewDragHelper.create(this, 1.0f, new MyViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                if(child instanceof NumberItem){
                    NumberItem item = (NumberItem) child;
                    return item.getStatus() != NumberItem.Status.MARGIN
                            && item.getStatus() != NumberItem.Status.SEPARA
                            && item.getStatus() != NumberItem.Status.DIE;
                }
                return false;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                if(changedView instanceof NumberItem){
                    NumberItem item = (NumberItem) changedView;
                    dx = dx < 0 ? -dx : dx;
                    dy = dy < 0 ? dy : dy;
                    if(Math.max(dx, dy) > MIN_MOVE_DIS) {
                        item.setIsDrag(true);
                    }
                }
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingLeft();
                final int rightBound = getWidth() - child.getWidth() - leftBound;
                if(left < leftBound){
                    return leftBound;
                }
                else if(left > rightBound){
                    return rightBound;
                }
                else{
                    return left;
                }
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                final int topBound = getPaddingTop();
                final int bottomBound = getHeight() - child.getHeight() - topBound;

                if(top < topBound){
                    return topBound;
                }
                else if(top > bottomBound){
                    return bottomBound;
                }
                else{
                    return top;
                }
            }

            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                if (releasedChild instanceof NumberItem) {
                    NumberItem item = (NumberItem) releasedChild;
                    item.setIsDrag(false);
                }
            }
        });
    }

    @Override
    public void run() {
        while(mIsThreadRunning){
            try {
                UIHandler.sendEmptyMessage(0, new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        for(int i = getChildCount() - 1; i >= 0; i--){
                            View view = getChildAt(i);
                            if(view instanceof NumberItem){
                                NumberItem numberItem = (NumberItem) view;
                                numberItem.update();
                            }
                        }
                        return true;
                    }
                });
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
