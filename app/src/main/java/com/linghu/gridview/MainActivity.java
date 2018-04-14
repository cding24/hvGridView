package com.linghu.gridview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private int width;
    private int screenWidth;
    private  List<String> data = new ArrayList<>();
    private PageGridView pageGridView1, pageGridView2, pageGridView3;
    private MyPageIndicator pageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pageIndicator= (MyPageIndicator) findViewById(R.id.pageindicator);
        width = getResources().getDisplayMetrics().widthPixels / 4;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        pageGridView1 = (PageGridView) findViewById(R.id.pagingGridView1);
        pageGridView2 = (PageGridView) findViewById(R.id.pagingGridView2);
        pageGridView3 = (PageGridView) findViewById(R.id.pagingGridView3);

        for (int i = 1; i <= 17; i++) {
            data.add(i + "");
        }
        MAdapter adapter1 = new MAdapter(data);
        MAdapter adapter2 = new MAdapter(data);
        MAdapter adapter3 = new MAdapter(data);
        pageGridView1.setAdapter(adapter1);
        pageGridView2.setAdapter(adapter2);
        pageGridView3.setAdapter(adapter3);
        pageGridView1.setOnItemClickListener(adapter1);
        pageGridView2.setOnItemClickListener(adapter2);
        pageGridView3.setOnItemClickListener(adapter3);

        //设置分页指示器
        pageGridView2.setPageIndicator(pageIndicator);
    }

    public class MAdapter extends PageGridView.PageGridAdapter<MViewHolder> implements PageGridView.OnItemClickListener {
        private List<String> mData = new ArrayList<>();

        public MAdapter(List<String> data) {
            this.mData.addAll(data);
        }

        @Override
        public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.gridview_layout_item, parent, false);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = width;
            params.width = width;
            view.setLayoutParams(params);
            return new MViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MViewHolder holder, int position) {
            if(TextUtils.isEmpty(mData.get(position))){
                holder.icon.setVisibility(View.GONE);
            }else{
                holder.icon.setVisibility(View.VISIBLE);
            }
            holder.tv_title.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public List getData() {
            return mData;
        }

        @Override
        public Object getEmpty() {
            return "";
        }

        @Override
        public void onItemClick(PageGridView pageGridView, int position) {
            String gridview = "";
            if (pageGridView == pageGridView1) {
                gridview = "第一个GridView";
            }
            if (pageGridView == pageGridView2) {
                gridview = "第二个GridView";
            }
            if (pageGridView == pageGridView3) {
                gridview = "第三个GridView";
            }

            Toast.makeText(MainActivity.this, gridview + " 第" + (position + 1) + "个item 被点击" + " 值：" + mData.get(position), Toast.LENGTH_SHORT).show();
        }
    }

    public static class MViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_title;
        public ImageView icon;
        public MViewHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            icon= (ImageView) itemView.findViewById(R.id.icon);
        }
    }

    private int scrollX = 0;
    private boolean isAuto = false;
    private int Target = 0;
    public class MyScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            Log.i("zzz", "onScrollStateChanged state=" + newState + " isAuto=" + isAuto);
            // recyclerView.smoothScrollBy(10,0);
            if (newState == 0) {
                if (!isAuto) {
                    int p = scrollX / screenWidth;
                    int offset = scrollX % screenWidth;
                    if (offset > screenWidth / 2) {
                        p++;
                    }
                    Target = p * screenWidth;
                    isAuto = true;
                    recyclerView.smoothScrollBy(Target - scrollX, 0);
                }
            } else if (newState == 2) {
                isAuto = false;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            scrollX += dx;
            Log.i("zzz", "onScrolled dx=" + dx + " scrollX=" + scrollX);
        }
    }

}