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
            return 100;
        }

        @Override
        public Object getItem(int position) {
            return position;
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

                convertView = View.inflate(MainActivity.this, R.layout.activity_main, null);
                holder.imageView = (ImageView) convertView.findViewById(R.id.main_iv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
//            ImageLoader.getInstance(3, ImageLoader.Type.LIFO).load("/storage/sdcard0/DCIM/Camera/IMG_20130907_145408.jpg", holder.imageView);
            ImageLoader.getInstance(3, ImageLoader.Type.LIFO).load("https://upd13.sogoucdn.com/nstatic/img/logo.png", holder.imageView);
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
        }
    }
}
