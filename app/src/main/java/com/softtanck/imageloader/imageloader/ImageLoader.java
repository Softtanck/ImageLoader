package com.softtanck.imageloader.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.softtanck.imageloader.imageloader.bean.ImageBeanHolder;
import com.softtanck.imageloader.imageloader.bean.ImageSize;
import com.softtanck.imageloader.utils.LruCacheUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author : Tanck
 * @Description : TODO
 * @date 10/19/2015
 */
public class ImageLoader {

    /**
     * 默认低内存
     */
    private static int defThreadCount = 1;

    /**
     * 队列的调度方式
     */
    public static Type mType = Type.LIFO;

    /**
     * 工作的线程
     */
    private Thread mThread;

    /**
     * 线程中的消息处理
     */
    private Handler mPoolThreadHandler;

    /**
     * 线程池
     */
    private ExecutorService mThreadPool;

    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTask;

    /**
     * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
     */
    private Semaphore mSemapHore = new Semaphore(0);

    /**
     * 引入一个值为threadCount的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private Semaphore mPoolSemaphore;

    /**
     * 图片展示处理
     */
    private Handler mDisPlayHandler;

    private static ImageLoader loader;


    /**
     * 队列调度模式
     */
    public enum Type {
        FIFO, LIFO
    }

    private ImageLoader() {

    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);
    }

    private void init(int threadCount, Type type) {
        //工作线程
        mThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        mThreadPool.execute(getTask());
                        try {
                            mPoolSemaphore.acquire();//信号量 + 1
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mSemapHore.release();//初始化完成后信号量 -1
                Looper.loop();
            }
        };
        mThread.start();
        //初始化线程池
        mThreadPool = Executors.newScheduledThreadPool(threadCount);
        //初始化信号量
        mPoolSemaphore = new Semaphore(threadCount);
        //初始化线程列表
        mTask = new LinkedList<>();
        mType = type == null ? Type.LIFO : type;
    }

    public static ImageLoader getInstance() {
        if (null == loader) {
            synchronized (ImageLoader.class) {
                if (null == loader) {
                    loader = new ImageLoader(defThreadCount, mType);
                }
            }
        }
        return loader;
    }

    public static ImageLoader getInstance(int threadCount, Type type) {
        if (null == loader) {
            synchronized (ImageLoader.class) {
                if (null == loader) {
                    loader = new ImageLoader(threadCount, type);
                }
            }
        }
        return loader;
    }

    /**
     * 加载图片
     *
     * @param path
     * @param imageview
     */
    public void load(final String path, final ImageView imageview) {

        if (null == path)
            throw new RuntimeException("this path is null");

        imageview.setTag(path);
        //1.从磁盘,2.从内存
        if (null == mDisPlayHandler)
            mDisPlayHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ImageBeanHolder holder = (ImageBeanHolder) msg.obj;
                    ImageView imgView = holder.getImageView();
                    Bitmap bitmap = holder.getBitmap();
                    if (imgView.getTag().toString().equals(path)) {
                        imgView.setImageBitmap(bitmap);
                    }
                }
            };

        addTask(path, imageview);

    }

    /**
     * 添加任务
     *
     * @param path
     * @param imageview
     */
    private synchronized void addTask(final String path, final ImageView imageview) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                //从内存中获取
                Bitmap bitmap = LruCacheUtils.getInstance().get(path);
                if (null == bitmap) {
                    //TODO 从磁盘中获取
                    ImageSize imageSize = getImageViewWidth(imageview);
                    int reqWidth = imageSize.width;
                    int reqHeight = imageSize.height;

                    bitmap = decodeSampledBitmapFromNetWork(path, reqWidth,
                            reqHeight);
                    //存储到缓存
                    LruCacheUtils.getInstance().put(path, bitmap);
                }

                ImageBeanHolder holder = new ImageBeanHolder();
                holder.setBitmap(bitmap);
                holder.setImageView(imageview);
                holder.setPath(path);
                Message message = Message.obtain();
                message.obj = holder;
                mDisPlayHandler.sendMessage(message);
            }
        };

        if (null == mPoolThreadHandler) {
            try {
                mSemapHore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mTask.add(runnable);
        mPoolThreadHandler.sendEmptyMessage(0x1000);
        mPoolSemaphore.release();//信号量 -1
    }


    /**
     * 根据ImageView获得适当的压缩的宽和高
     *
     * @param imageView
     * @return
     */
    private ImageSize getImageViewWidth(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        final DisplayMetrics displayMetrics = imageView.getContext()
                .getResources().getDisplayMetrics();
        final LayoutParams params = imageView.getLayoutParams();

        int width = params.width == LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getWidth(); // Get actual image width
        if (width <= 0)
            width = params.width; // Get layout width parameter
        if (width <= 0)
            width = getImageViewFieldValue(imageView, "mMaxWidth"); // Check
        // maxWidth
        // parameter
        if (width <= 0)
            width = displayMetrics.widthPixels;
        int height = params.height == LayoutParams.WRAP_CONTENT ? 0 : imageView
                .getHeight(); // Get actual image height
        if (height <= 0)
            height = params.height; // Get layout height parameter
        if (height <= 0)
            height = getImageViewFieldValue(imageView, "mMaxHeight"); // Check
        // maxHeight
        // parameter
        if (height <= 0)
            height = displayMetrics.heightPixels;
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;

    }


    /**
     * 反射获得ImageView设置的最大宽度和高度
     *
     * @param object
     * @param fieldName
     * @return
     */
    private static int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;
        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = (Integer) field.get(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
                Log.d("Tanck", "" + value);
            }
        } catch (Exception e) {
        }
        return value;
    }


    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromResource(String pathName,
                                                   int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        // 调用上面定义的方法计算inSampleSize值
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);

        return bitmap;
    }


    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampledBitmapFromNetWork(String url,
                                                  int reqWidth, int reqHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = getNetWorkInputStream(url);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//        inputStream.close();
//        temp = inputStream;
//        BitmapFactory.decodeStream(inputStream);
//        // 调用上面定义的方法计算inSampleSize值
//        options.inSampleSize = calculateInSampleSize(options, reqWidth,
//                reqHeight);
//        // 使用获取到的inSampleSize值再次解析图片
//        options.inJustDecodeBounds = false;
//        Bitmap bitmap = BitmapFactory.decodeStream(temp);
//        try {
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return bitmap;
    }


    /**
     * 获取网络图片
     *
     * @param urlString
     * @return
     */
    private InputStream getNetWorkInputStream(String urlString) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(urlString);
            // 使用HttpURLConnection打开连接
            HttpURLConnection urlConn = (HttpURLConnection) imgUrl
                    .openConnection();
            urlConn.setDoInput(true);
            urlConn.connect();
            // 将得到的数据转化成InputStream
            InputStream is = urlConn.getInputStream();
            return is;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 计算inSampleSize，用于压缩图片
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // 源图片的宽度
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;

        if (width > reqWidth && height > reqHeight) {
            // 计算出实际宽度和目标宽度的比率
            int widthRatio = Math.round((float) width / (float) reqWidth);
            int heightRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }
        return inSampleSize;
    }


    /**
     * 获取任务
     *
     * @return
     */
    private Runnable getTask() {
        if (0 < mTask.size()) {
            if (mType == Type.LIFO)
                return mTask.removeFirst();
            else
                return mTask.removeLast();
        }
        return null;
    }
}
