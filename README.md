# Android超长图片显示组件

------

### 一、目的
实现一个LargeImageView组件，支持显示超长图片，支持触摸上下左右滑动，支持双指缩放，支持双击放大/还原。
实现图片预览Activity，支持多图预览，支持本地保存，支持图片文字说明。

### 二、LargeImageView组件用法
```
    <com.puerlink.imagepreview.LargeImageView
        android:id="@+id/image_scale_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    </com.puerlink.imagepreview.LargeImageView>
```
    具体用法和ImageView相同。

### 三、图片预览Activity用法
```
    List<ImageItem> items = new ArrayList<ImageItem>();
    ImageItem item = new ImageItem("图片Url", "图片描述");
    items.add(item);
    
    Intent intent = new Intent(activity, ImagePreviewActivity.class);
    intent.putExtra("title", title);    //顶部标题
    intent.putExtra("show_desc", true); //是否显示图片描述
    intent.putExtra("show_dot", true);  //多张图时是否显示指示点
    intent.putExtra("images", (Serializable) items);    //图片信息
    intent.putExtrea("auto_hide", true);    //标题是否自动收起
    activity.startActivity(intent);
```

