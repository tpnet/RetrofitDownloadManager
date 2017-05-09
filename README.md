# RetrofitDownloadManager
Rxjava+Retrofit+SqlBrite+SqlDelight 实现多文件断点续传下载。

了解源码请看文章： http://blog.csdn.net/niubitianping/article/details/70599355

![下载列表界面](http://img.blog.csdn.net/20170424162245229?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbml1Yml0aWFucGluZw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

## **使用**


#### **1. 以module形式添加到项目**


#### **2. 在Application进行初始化下载器**

```
DownManager.init(this):
```

#### **3. 添加下载**

```
        //创建基本任务，参数为:文件保存的路径、下载的url要全路径、下载任务的名称
        DownInfo downInfo = DownInfo.builder().create(getPath(url), url, name);
        
        //开始下载
        DownManager.getInstance().startDown(downInfo);
```

#### **4. 查询当前的下载**

```
        //查询当前所有的下载
        DBUtil.getInstance().getAllDown()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter);
```

#### **5. 设置view的回调监听器**

注意要设置两个View的回调监听器
 - 第一个是ViewHold初始化时候设置Rxbus
 - 第二个是onBindHolder的时候addListener


```
   public ListViewHolder(View itemView) {
        super(itemView);
        // 初始化View

        mBtHandle.setOnClickListener(this);

        //因为(downInfo.downUrl()用来传递进度信息，这里使用两个(downInfo.downUrl()进行标识
        RxBus.with().setEvent(DownManager.DOWN_ADD_SUBSCRIBE)
                .onNext(new Action1<Events<?>>() {
                    @Override
                    public void call(Events<?> events) {
                        //添加监听器，在列表点击开始回调
                        String link = events.getContent();
                        link = link.replace(DownManager.DOWN_ADD_SUBSCRIBE, "");
                        DownManager.getInstance().addListener(link, listener);

                    }
                }).create();

    }



```


```
    @Override
    public void onBindViewHolder(final ListViewHolder holder, final int position) {
        //在Holder里面设置数据
 

        //添加View监听器
        DownManager.getInstance().addListener(downInfo.downUrl(), listener);

    }
```



