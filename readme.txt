# PageGridView支持纵向，横向，水平分页的gridview
1.支持纵向，横向，水平分页三种布局方式。
2.支持点击事件。
3.支持分割线设置，支持自定义分页指示器。
4.使用简单方便

设置Adapter
  //设置adapter
  pageGridView.setAdapter(adapter1);
  //设置点击监听器
  pageGridView.setOnItemClickListener(adapter1);
  //设置分页指示器
   pageGridView2.setPageIndicator(pageIndicator);

注意！！！
   如果使用分页显示，由于会对数据进行重排序，所以点击事件的position只用和数据集合结合使用。