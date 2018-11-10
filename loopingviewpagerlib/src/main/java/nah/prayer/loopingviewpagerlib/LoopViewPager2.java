package nah.prayer.loopingviewpagerlib;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

public class LoopViewPager2 extends ViewPager {

    private static final boolean DEFAULT_BOUNDARY_CASHING = false;

    OnPageChangeListener mOuterPageChangeListener;
    private LoopPagerAdapterWrapper2 mAdapter;
    private boolean mBoundaryCaching = DEFAULT_BOUNDARY_CASHING;

    private PositionListerner listerner;
    public interface PositionListerner{
        void onReceivedEvent(int position);
    }
    public void setOnReceivedEvent(PositionListerner listener){
        this.listerner = listener;
    }


    /**
     * helper function which may be used when implementing FragmentPagerAdapter
     *
     * @param position
     * @param count
     * @return (position - 1)%count
     */
    public static int toRealPosition(int position, int count) {
        position = position - 1;
        if (position < 0) {
            position += count;
        } else {
            position = position % count;
        }
        return position;
    }

    /**
     * If set to true, the boundary views (i.e. first and last) will never be destroyed
     * This may help to prevent "blinking" of some views
     *
     * @param flag
     */
    public void setBoundaryCaching(boolean flag) {
        mBoundaryCaching = flag;
        if (mAdapter != null) {
            mAdapter.setBoundaryCaching(flag);
        }
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = new LoopPagerAdapterWrapper2(adapter);
        mAdapter.setBoundaryCaching(mBoundaryCaching);
        super.setAdapter(mAdapter);
        setCurrentItem(0, isBorderAnimation);


        init();
    }

    @Override
    public PagerAdapter getAdapter() {
        return mAdapter != null ? mAdapter.getRealAdapter() : mAdapter;
    }

    @Override
    public int getCurrentItem() {
        return mAdapter != null ? mAdapter.toRealPosition(super.getCurrentItem()) : 0;
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        int realItem = mAdapter.toInnerPosition(item);
        super.setCurrentItem(realItem, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        if (getCurrentItem() != item) {
            setCurrentItem(item, true);
        }
    }


    public LoopViewPager2(Context context) {
        super(context);
        init();
    }

    public LoopViewPager2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    long timeDelay = 0;

    private void init() {

        addOnPageChangeListener(new OnPageChangeListener() {
            private float mPreviousOffset = -1;
            private float mPreviousPosition = -1;

            @Override
            public void onPageSelected(int position) {

                if (System.currentTimeMillis() > timeDelay + 50) {
                    timeDelay = System.currentTimeMillis();
                    return;
                }


                if(listerner != null) {
                    if(position == 0 || position >= mAdapter.getRealCount()){
                        listerner.onReceivedEvent(mAdapter.getRealCount()-1);
                    }else{
                        listerner.onReceivedEvent(position-1);
                    }

                }

                int realPosition = mAdapter.toRealPosition(position);
                if (mPreviousPosition != realPosition) {
                    mPreviousPosition = realPosition;
                    if (mOuterPageChangeListener != null) {
                        mOuterPageChangeListener.onPageSelected(realPosition);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                int realPosition = position;
                if (mAdapter != null) {
                    realPosition = mAdapter.toRealPosition(position);

                    if (positionOffset == 0
                            && mPreviousOffset == 0
                            && (position == 0 || position == mAdapter.getCount() - 1)) {
                        setCurrentItem(realPosition, false);
                    }
                }

                mPreviousOffset = positionOffset;
                if (mOuterPageChangeListener != null) {
                    if (realPosition != mAdapter.getRealCount() - 1) {
                        mOuterPageChangeListener.onPageScrolled(realPosition,
                                positionOffset, positionOffsetPixels);
                    } else {
                        if (positionOffset > .5) {
                            mOuterPageChangeListener.onPageScrolled(0, 0, 0);
                        } else {
                            mOuterPageChangeListener.onPageScrolled(realPosition,
                                    0, 0);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mAdapter != null) {
                    int position = LoopViewPager2.super.getCurrentItem();
                    int realPosition = mAdapter.toRealPosition(position);
                    if (state == ViewPager.SCROLL_STATE_IDLE
                            && (position == 0 || position == mAdapter.getCount() - 1)) {
                        setCurrentItem(realPosition, false);
                    }
                }
                if (mOuterPageChangeListener != null) {
                    mOuterPageChangeListener.onPageScrollStateChanged(state);
                }

            }
        });


        handler = new MyHandler(this);
        setViewPagerScroller();
    }


    public static final int        DEFAULT_INTERVAL            = 1500;

    public static final int        LEFT                        = 0;
    public static final int        RIGHT                       = 1;

    /** do nothing when sliding at the last or first item **/
    public static final int        SLIDE_BORDER_MODE_NONE      = 0;
    /** cycle when sliding at the last or first item **/
    public static final int        SLIDE_BORDER_MODE_CYCLE     = 1;
    /** deliver event to parent when sliding at the last or first item **/
    public static final int        SLIDE_BORDER_MODE_TO_PARENT = 2;

    /** auto scroll time in milliseconds, default is {@link #DEFAULT_INTERVAL} **/
    private long                   interval                    = DEFAULT_INTERVAL;
    /** auto scroll direction, default is {@link #RIGHT} **/
    private int                    direction                   = RIGHT;
    /** whether automatic cycle when auto scroll reaching the last or first item, default is true **/
    private boolean                isCycle                     = true;
    /** whether stop auto scroll when touching, default is true **/
    private boolean                stopScrollWhenTouch         = true;
    /** how to process when sliding at the last or first item, default is {@link #SLIDE_BORDER_MODE_NONE} **/
    private int                    slideBorderMode             = SLIDE_BORDER_MODE_NONE;
    /** whether animating when auto scroll at the last or first item **/
    private boolean                isBorderAnimation           = true;
    /** scroll factor for auto scroll animation, default is 1.0 **/
    private double                 autoScrollFactor            = 1.0;
    /** scroll factor for swipe scroll animation, default is 1.0 **/
    private double                 swipeScrollFactor           = 1.0;

    private Handler handler;
    private boolean                isAutoScroll                = false;
    private boolean                isStopByTouch               = false;
    private float                  touchX                      = 0f, downX = 0f;
    private CustomDurationScroller scroller                    = null;

    public static final int        SCROLL_WHAT                 = 0;



    /**
     * start auto scroll, first scroll delay time is {@link #getInterval()}
     */
    public void startAutoScroll() {
        isAutoScroll = true;
        sendScrollMessage((long)(interval + scroller.getDuration() / autoScrollFactor * swipeScrollFactor));
    }

    /**
     * start auto scroll
     *
     * @param delayTimeInMills first scroll delay time
     */
    public void startAutoScroll(int delayTimeInMills) {
        isAutoScroll = true;
        sendScrollMessage(delayTimeInMills);
    }

    /**
     * stop auto scroll
     */
    public void stopAutoScroll() {
        isAutoScroll = false;
        handler.removeMessages(SCROLL_WHAT);
    }

    /**
     * set the factor by which the duration of sliding animation will change while swiping
     */
    public void setSwipeScrollDurationFactor(double scrollFactor) {
        swipeScrollFactor = scrollFactor;
    }

    /**
     * set the factor by which the duration of sliding animation will change while auto scrolling
     */
    public void setAutoScrollDurationFactor(double scrollFactor) {
        autoScrollFactor = scrollFactor;
    }

    private void sendScrollMessage(long delayTimeInMills) {
        /** remove messages before, keeps one message is running at most **/
        handler.removeMessages(SCROLL_WHAT);
        handler.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills);
    }

    /**
     * set ViewPager scroller to change animation duration when sliding
     */
    private void setViewPagerScroller() {
        try {
            Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            Field interpolatorField = ViewPager.class.getDeclaredField("sInterpolator");
            interpolatorField.setAccessible(true);

            scroller = new CustomDurationScroller(getContext(), (Interpolator)interpolatorField.get(null));
            scrollerField.set(this, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * scroll only once
     */
    public void scrollOnce() {
        PagerAdapter adapter = getAdapter();
        int currentItem = getCurrentItem();
        int totalCount;
        if (adapter == null || (totalCount = adapter.getCount()) <= 1) {
            return;
        }

        int nextItem = (direction == LEFT) ? --currentItem : ++currentItem;
        if (nextItem < 0) {
            if (isCycle) {
                setCurrentItem(totalCount - 1, isBorderAnimation);
            }
        } else if (nextItem == totalCount) {
            if (isCycle) {
                setCurrentItem(0, isBorderAnimation);
            }
        } else {
            setCurrentItem(nextItem, isBorderAnimation);
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();

        if (stopScrollWhenTouch) {
            if ((action == MotionEvent.ACTION_DOWN) && isAutoScroll) {
                isStopByTouch = true;
                stopAutoScroll();
            } else if (ev.getAction() == MotionEvent.ACTION_UP && isStopByTouch) {
                startAutoScroll();
            }
        }

        if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT || slideBorderMode == SLIDE_BORDER_MODE_CYCLE) {
            touchX = ev.getX();
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                downX = touchX;
            }
            int currentItem = getCurrentItem();
            PagerAdapter adapter = getAdapter();
            int pageCount = adapter == null ? 0 : adapter.getCount();
            /**
             * current index is first one and slide to right or current index is last one and slide to left.<br/>
             * if slide border mode is to parent, then requestDisallowInterceptTouchEvent false.<br/>
             * else scroll to last one when current item is first one, scroll to first one when current item is last
             * one.
             */
            if ((currentItem == 0 && downX <= touchX) || (currentItem == pageCount - 1 && downX >= touchX)) {
                if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    if (pageCount > 1) {
                        setCurrentItem(pageCount - currentItem - 1, isBorderAnimation);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.dispatchTouchEvent(ev);
            }
        }
        getParent().requestDisallowInterceptTouchEvent(true);

        return super.dispatchTouchEvent(ev);
    }

    private static class MyHandler extends Handler {

        private final WeakReference<LoopViewPager2> loopViewPager;

        public MyHandler(LoopViewPager2 loopViewPager) {
            this.loopViewPager = new WeakReference<>(loopViewPager);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case SCROLL_WHAT:
                    LoopViewPager2 pager = this.loopViewPager.get();
                    if (pager != null) {
                        pager.scroller.setScrollDurationFactor(pager.autoScrollFactor);
                        pager.scrollOnce();
                        pager.scroller.setScrollDurationFactor(pager.swipeScrollFactor);
                        pager.sendScrollMessage(pager.interval + pager.scroller.getDuration());
                    }
                default:
                    break;
            }
        }
    }

    /**
     * get auto scroll time in milliseconds, default is {@link #DEFAULT_INTERVAL}
     *
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * set auto scroll time in milliseconds, default is {@link #DEFAULT_INTERVAL}
     *
     * @param interval the interval to set
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * get auto scroll direction
     *
     * @return {@link #LEFT} or {@link #RIGHT}, default is {@link #RIGHT}
     */
    public int getDirection() {
        return (direction == LEFT) ? LEFT : RIGHT;
    }

    /**
     * set auto scroll direction
     *
     * @param direction {@link #LEFT} or {@link #RIGHT}, default is {@link #RIGHT}
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * whether automatic cycle when auto scroll reaching the last or first item, default is true
     *
     * @return the isCycle
     */
    public boolean isCycle() {
        return isCycle;
    }

    /**
     * set whether automatic cycle when auto scroll reaching the last or first item, default is true
     *
     * @param isCycle the isCycle to set
     */
    public void setCycle(boolean isCycle) {
        this.isCycle = isCycle;
    }

    /**
     * whether stop auto scroll when touching, default is true
     *
     * @return the stopScrollWhenTouch
     */
    public boolean isStopScrollWhenTouch() {
        return stopScrollWhenTouch;
    }

    /**
     * set whether stop auto scroll when touching, default is true
     *
     * @param stopScrollWhenTouch
     */
    public void setStopScrollWhenTouch(boolean stopScrollWhenTouch) {
        this.stopScrollWhenTouch = stopScrollWhenTouch;
    }

    /**
     * get how to process when sliding at the last or first item
     *
     * @return the slideBorderMode {@link #SLIDE_BORDER_MODE_NONE}, {@link #SLIDE_BORDER_MODE_TO_PARENT},
     *         {@link #SLIDE_BORDER_MODE_CYCLE}, default is {@link #SLIDE_BORDER_MODE_NONE}
     */
    public int getSlideBorderMode() {
        return slideBorderMode;
    }

    /**
     * set how to process when sliding at the last or first item
     *
     * @param slideBorderMode {@link #SLIDE_BORDER_MODE_NONE}, {@link #SLIDE_BORDER_MODE_TO_PARENT},
     *        {@link #SLIDE_BORDER_MODE_CYCLE}, default is {@link #SLIDE_BORDER_MODE_NONE}
     */
    public void setSlideBorderMode(int slideBorderMode) {
        this.slideBorderMode = slideBorderMode;
    }

    /**
     * whether animating when auto scroll at the last or first item, default is true
     *
     * @return
     */
    public boolean isBorderAnimation() {
        return isBorderAnimation;
    }

    /**
     * set whether animating when auto scroll at the last or first item, default is true
     *
     * @param isBorderAnimation
     */
    public void setBorderAnimation(boolean isBorderAnimation) {
        this.isBorderAnimation = isBorderAnimation;
    }

}