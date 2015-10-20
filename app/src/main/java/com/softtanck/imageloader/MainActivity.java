package com.softtanck.imageloader;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.softtanck.imageloader.imageloader.ImageLoader;
import com.softtanck.imageloader.imageloader.listener.LoadListener;


public class MainActivity extends AppCompatActivity {


    private ListView listView;

    private Myadapter myadapter;


    // 一堆图片链接
    public final String[] IMAGES = new String[]{
            "http://e.hiphotos.baidu.com/image/pic/item/2fdda3cc7cd98d10b510fdea233fb80e7aec9021.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2512767624,584707746&fm=21&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=3876669459,1702100736&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=2071405091,342931090&fm=21&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=3468695763,2338290854&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=3661837012,4085429241&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1575536525,1104812014&fm=21&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=394441947,1761554849&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=1818036152,2714012184&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2266598397,645131245&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=440447379,308109475&fm=21&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=768844916,3097393866&fm=21&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=274839962,119107672&fm=21&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2327452287,2289737437&fm=21&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=2452896114,2600307190&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1671473477,1404439474&fm=21&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=1868085000,3976235118&fm=21&gp=0.jpg",
            "http://img5.imgtn.bdimg.com/it/u=303566868,2154026315&fm=21&gp=0.jpg",
            "http://img0.imgtn.bdimg.com/it/u=698396519,2554150123&fm=21&gp=0.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2266598397,645131245&fm=21&gp=0.jpg",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        listView = (ListView) findViewById(R.id.lv);
        myadapter = new Myadapter();
        listView.setAdapter(myadapter);

    }

    private class Myadapter extends BaseAdapter {

        @Override
        public int getCount() {
            return IMAGES.length;
        }

        @Override
        public Object getItem(int position) {
            return IMAGES[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                holder = new ViewHolder();

                convertView = View.inflate(MainActivity.this, R.layout.item, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.main_iv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.imageView.setImageResource(R.drawable.pictures_no);
//            ImageLoader.getInstance(MainActivity.this, 3, ImageLoader.Type.LIFO).load(IMAGES[position], holder.imageView);

            ImageLoader.getInstance(MainActivity.this, 3, ImageLoader.Type.LIFO).load(IMAGES[position], holder.imageView, new LoadListener<View>() {
                @Override
                public <T> void Loading(View view, String path) {

                }

                @Override
                public <T> void LoadSuccess(View view, Bitmap bitmap, String path) {
                    if (view.getTag().toString().equals(path)) {
                        ((ImageView) view).setImageBitmap(bitmap);
                    }
                }

                @Override
                public <T> void LoadError(View view, String path, String errorMsg) {

                }
            });
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
        }
    }
}
