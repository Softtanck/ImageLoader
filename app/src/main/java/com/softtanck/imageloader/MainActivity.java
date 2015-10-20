package com.softtanck.imageloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.softtanck.imageloader.imageloader.ImageLoader;


public class MainActivity extends AppCompatActivity {


    private ListView listView;

    private Myadapter myadapter;


    // 一堆图片链接
    public final String[] IMAGES = new String[]{
            "http://e.hiphotos.baidu.com/image/pic/item/2fdda3cc7cd98d10b510fdea233fb80e7aec9021.jpg",
            "http://c.hiphotos.baidu.com/image/h%3D300/sign=791a043fdbc451dae9f60aeb86fc52a5/dbb44aed2e738bd4d1d32e50a78b87d6267ff9e5.jpg",
            "http://h.hiphotos.baidu.com/image/h%3D300/sign=ada3d2a50cd162d99aee641c21dea950/b7003af33a87e9509a816f0d16385343fbf2b439.jpg",
            "http://a.hiphotos.baidu.com/image/h%3D300/sign=de2dfb37d6160924c325a41be406359b/a08b87d6277f9e2f6fd881691930e924b999f3e6.jpg",
            "http://c.hiphotos.baidu.com/image/h%3D360/sign=d4bd2930a96eddc439e7b2fd09dab6a2/377adab44aed2e73519d81f98301a18b86d6faeb.jpg",
            "http://d.hiphotos.baidu.com/image/h%3D360/sign=5af078735dee3d6d3dc681cd73146d41/902397dda144ad34524fec53d4a20cf430ad8575.jpg",
            "http://b.hiphotos.baidu.com/image/h%3D360/sign=1b962dee6f63f624035d3f05b745eb32/203fb80e7bec54e7df58bcc9bd389b504ec26ab5.jpg",
            "http://h.hiphotos.baidu.com/image/h%3D300/sign=50e842a91c4c510fb1c4e41a50582528/b8389b504fc2d56252994c8ce11190ef76c66c3a.jpg",
            "http://e.hiphotos.baidu.com/image/h%3D360/sign=c9341858b6b7d0a264c9029bfbee760d/b2de9c82d158ccbf0881c1d01dd8bc3eb135411e.jpg",
            "http://h.hiphotos.baidu.com/image/h%3D360/sign=87c962042ff5e0fef1188f076c6134e5/d788d43f8794a4c20ac88a360cf41bd5ac6e39c7.jpg",
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
//            ImageLoader.getInstance(3, ImageLoader.Type.LIFO).load("/storage/sdcard0/DCIM/Camera/IMG_20130907_145408.jpg", holder.imageView);
            ImageLoader.getInstance(MainActivity.this, 3, ImageLoader.Type.LIFO).load(IMAGES[position], holder.imageView);
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
        }
    }
}
