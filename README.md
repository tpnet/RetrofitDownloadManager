# RetrofitDownloadDemo
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
        DownInfo downInfo = DownInfo.builder()
                .savePath(getPath(url))   //文件保存的路径
                .downUrl(url)               //下载的url，要全路径
                .create();
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

注意要在startDown之后设置监听器，例如demo里面的ListViewHolder




