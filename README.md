# ImageLoader
<br>
图片快速加载框架.
三级缓存策略.
LruCache内存缓存算法.
DiskLruCache磁盘缓存算法.
支持:1.本地图片.2.网络图片.3.加载淡入淡出动画.可以关闭开启.
自带框架动画.解决错位问题.<br>
![image](https://github.com/q422013/ImageLoader/blob/master/test.gif)

#<b>How to use:</b>
<br>
type1:<br>
ImageLoader.getInstance(MainActivity.this, 3, ImageLoader.Type.LIFO).load(url, holder.imageView, new LoadListener<View>() {
                @Override
                public <T> void Loading(View view, String path) {

                }

                @Override
                public <T> void LoadSuccess(View view, Bitmap bitmap, String path) {
                        ((ImageView) view).setImageBitmap(bitmap);
                }

                @Override
                public <T> void LoadError(View view, String path, String errorMsg) {

                }
            });
            
<br>
type2:<br>
ImageLoader.getInstance(MainActivity.this, 3, ImageLoader.Type.LIFO).load(url, holder.imageView);

#<b>Lincense</b>
<br>
Copyright 2015 Tanck

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


