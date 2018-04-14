package com.linghu.gridview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author linghu
 * @date 2018/04/14
 * <p>横向，纵向gridview</p>
 *
 */
public class PageGridView extends RecyclerView {
    private int rowNum = 0;
    private int columNum = 0;
    private int pageSize = 0;
    private int onePageSize = 0;
    private int mWidth = -1;
    //是否需要重排序
    private boolean needReorder = false;

    public PageGridView(Context context) {
        this(context, null);
    }

    public PageGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //根据行数和列数判断是否
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PageGridView);
        rowNum = array.getInteger(R.styleable.PageGridView_PageRows, 0);
        columNum = array.getInteger(R.styleable.PageGridView_PageColums, 0);
        Drawable divider = array.getDrawable(R.styleable.PageGridView_PageDivider);
        array.recycle();

        if (rowNum < 0 || columNum < 0) {
            return;
        }
        if (rowNum == 0 && columNum == 0) {
            return;
        }
        LayoutManager layoutManager;
        if (rowNum > 0) {
            if (columNum > 0) {
//                needReorder = true;
                needReorder = false;
                //设置滚动监听器
                addOnScrollListener(new PagingScrollListener());
            }
            layoutManager = new StaggeredGridLayoutManager(rowNum, HORIZONTAL);
        } else {
            layoutManager = new StaggeredGridLayoutManager(columNum, VERTICAL);
        }
        setLayoutManager(layoutManager);
        //添加分割线
        if (divider != null) {
            addItemDecoration(new DividerGridItemDecoration(divider));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mWidth = getWidth();
    }

    @Override
    public final void setAdapter(Adapter adapter) {
        //对数据进行重排序
        if (needReorder) {
            if (!(adapter instanceof PageGridAdapter)) {
                throw new RuntimeException("must use PageGridAdapter");
            }
            PageGridAdapter pageGridAdapter = (PageGridAdapter) adapter;
            List data = pageGridAdapter.getData();
            List formatData = new ArrayList();

            onePageSize = rowNum * columNum;
            pageSize = data.size() / onePageSize;
            if (data.size() % onePageSize != 0) {
                pageSize++;
            }
            for (int p = 0; p < pageSize; p++) {
                for (int r = 0; r < rowNum; r++) {
                    for (int c = 0; c < columNum; c++) {
                        int index = p * onePageSize + r * columNum + c;
                        if (index > data.size() - 1) {
                            formatData.add(pageGridAdapter.getEmpty());
                        } else {
                            formatData.add(data.get(index));
                        }
                    }
                }
            }
            data.clear();
            data.addAll(formatData);
        }
        super.setAdapter(adapter);
        if (pageIndicator != null && pageIndicaotrNeedInit) {
            pageIndicator.InitIndicatorItems(pageSize);
            pageIndicator.onPageSelected(0);
            pageIndicaotrNeedInit = false;
        }
        if (onPageChangeListenerList != null) {
            for (OnPageChangeListener listener : onPageChangeListenerList) {
                listener.onPageChanged(0);
            }
        }
    }


    private int dX, dY;
    private long duringTime;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (onItemClickListener != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dX = (int) ev.getRawX();
                    dY = (int) ev.getRawY();
                    duringTime = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    int mx = (int) Math.abs(ev.getRawX() - dX);
                    int my = (int) Math.abs(ev.getRawY() - dY);
                    int time = (int) (System.currentTimeMillis() - duringTime);
                    if (mx <= 10 && my <= 10 && time < 200) {
                        int position = getPositionByXY((int) ev.getRawX(), (int) ev.getRawY());
                        if (position != -1) {
                            onItemClickListener.onItemClick(this, position);
                        }
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private int getPositionByXY(int x, int y) {
        int position = -1;
        Rect rect = new Rect();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.getGlobalVisibleRect(rect);
            if (rect.contains(x, y)) {
                position = i;
                break;
            }
        }
        if (rowNum > 0) {
            int offset = getChildPosition(getLayoutManager().getChildAt(0));
            position += offset;
        }
        return position;
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    private PageIndicator pageIndicator;
    //需要初始化pageIndicator
    private boolean pageIndicaotrNeedInit = false;

    public void setPageIndicator(PageIndicator pageIndicator) {
        this.pageIndicator = pageIndicator;
        pageIndicaotrNeedInit = true;
        if (getAdapter() != null && needReorder) {
            pageIndicator.InitIndicatorItems(pageSize);
            pageIndicator.onPageSelected(currentPage);
            pageIndicaotrNeedInit = false;
        }
    }

    //分页指示器
    public interface PageIndicator {

        void InitIndicatorItems(int itemsNumber);

        void onPageSelected(int pageIndex);

        void onPageUnSelected(int pageIndex);
    }

    private List<OnPageChangeListener> onPageChangeListenerList;

    public interface OnPageChangeListener {
        void onPageChanged(int index);
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (onPageChangeListenerList == null) {
            onPageChangeListenerList = new ArrayList();
        }
        onPageChangeListenerList.add(listener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (onPageChangeListenerList != null) {
            onPageChangeListenerList.remove(listener);
        }
    }

    public int getPageSize() {
        return pageSize;
    }

    public interface OnItemClickListener {
        void onItemClick(PageGridView pageGridView, int position);
    }

    public static abstract class PageGridAdapter<T extends ViewHolder> extends Adapter<T> {
        //获取数据集
        public abstract List getData();
        //获取空对象
        public abstract Object getEmpty();
    }

    private int scrollX = 0;
    private boolean isAuto = false;
    private int Target = 0;
    private int currentPage = 0;
    private int lastPage = 0;

    public class PagingScrollListener extends OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == 0) {
                if (!isAuto) {
                    int p = scrollX / getWidth();
                    int offset = scrollX % getWidth();
                    if (offset > getWidth() / 2) {
                        p++;
                    }
                    Target = p * getWidth();
                    isAuto = true;
                    currentPage = p;
                    if (pageIndicator != null) {
                        pageIndicator.onPageUnSelected(lastPage);
                        pageIndicator.onPageSelected(currentPage);
                    }
                    if (onPageChangeListenerList != null) {
                        for (OnPageChangeListener listener : onPageChangeListenerList) {
                            listener.onPageChanged(currentPage);
                        }
                    }
                    recyclerView.smoothScrollBy(Target - scrollX, 0);
                }
            } else if (newState == 2) {
                isAuto = false;
                lastPage = currentPage;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            scrollX += dx;
        }
    }

    //分割线
    public static class DividerGridItemDecoration extends ItemDecoration {
        private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
        private Drawable mDivider;

        public DividerGridItemDecoration(Drawable diver) {
            mDivider = diver;
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            drawHorizontal(c, parent);
            drawVertical(c, parent);

        }

        private int getSpanCount(RecyclerView parent) {
            // 列数
            int spanCount = -1;
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
            }
            return spanCount;
        }

        public void drawHorizontal(Canvas c, RecyclerView parent) {
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);
                final LayoutParams params = (LayoutParams) child.getLayoutParams();
                final int left = child.getLeft() - params.leftMargin;
                final int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
                final int top = child.getBottom() + params.bottomMargin;
                final int bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        public void drawVertical(Canvas c, RecyclerView parent) {
            final int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = parent.getChildAt(i);

                final LayoutParams params = (LayoutParams) child.getLayoutParams();
                final int top = child.getTop() - params.topMargin;
                final int bottom = child.getBottom() + params.bottomMargin;
                final int left = child.getRight() + params.rightMargin;
                final int right = left + mDivider.getIntrinsicWidth();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }

        private boolean isLastColum(RecyclerView parent, int pos, int spanCount, int childCount) {
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                if ((pos + 1) % spanCount == 0){ // 如果是最后一列，则不需要绘制右边
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    if ((pos + 1) % spanCount == 0){ // 如果是最后一列，则不需要绘制右边
                        return true;
                    }
                } else {
                    childCount = childCount - childCount % spanCount;
                    if (pos >= childCount)// 如果是最后一列，则不需要绘制右边
                        return true;
                }
            }
            return false;
        }

        private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount) {
            LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                childCount = childCount - childCount % spanCount;
                if (pos >= childCount)// 如果是最后一行，则不需要绘制底部
                    return true;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
                // StaggeredGridLayoutManager 且纵向滚动
                if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                    childCount = childCount - childCount % spanCount;
                    // 如果是最后一行，则不需要绘制底部
                    if (pos >= childCount)
                        return true;
                } else
                // StaggeredGridLayoutManager 且横向滚动
                {
                    // 如果是最后一行，则不需要绘制底部
                    if ((pos + 1) % spanCount == 0) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            int spanCount = getSpanCount(parent);
            int childCount = parent.getAdapter().getItemCount();
            if (isLastRaw(parent, itemPosition, spanCount, childCount)){ // 如果是最后一行，则不需要绘制底部
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
            } else if (isLastColum(parent, itemPosition, spanCount, childCount)){// 如果是最后一列，则不需要绘制右边
                outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            } else {
                outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
            }
        }
    }

}