package com.softtanck.imageloader.imageloader;


import android.graphics.Bitmap.Config;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.softtanck.imageloader.imageloader.anim.LoadAnimCore;
import com.softtanck.imageloader.imageloader.bean.ViewBeanHolder;
import com.softtanck.imageloader.imageloader.bean.ImageSize;
import com.softtanck.imageloader.imageloader.listener.LoadListener;
import com.softtanck.imageloader.imageloader.disklrucahe.DiskLruCacheHelper;
import com.softtanck.imageloader.imageloader.utils.LruCacheUtils;
import com.softtanck.imageloader.imageloader.utils.MD5Code;
import com.softtanck.imageloader.imageloader.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
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
     * 加载成功
     */
    public static final int LOAD_SUCCESS = 0x111;

    /**
     * 加载失败
     */
    public static final int LOAD_FAILE = 0x222;

    /**
     * 正在加载
     */
    public static final int LOAD_ING = 0x333;

    /**
     * 关键地址,用来判断是本地还是网络
     */
    public static final String DEF_KEY = "http";

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

    /**
     * 设置存储路径
     */
    private String saveLocation;

    /**
     * 加载监听
     */
    private LoadListener<View> loadListener;

    /**
     * 加载动画核心类
     */
    private LoadAnimCore loadAnimCore;

    /**
     * 默认加载动画
     */
    private boolean isNeedAnim = true;

    /**
     * 默认加载Config.Rgb_565
     */
    private Config loadConfig = Config.RGB_565;

    /**
     * 磁盘存储帮助类
     */
    private DiskLruCacheHelper helper;

    /**
     * 缓存目录名字
     */
    private String caCheName = "picCaChe";

    /**
     * 连接超时时间
     */
    private int mTimeOut = 1000;

    private static ImageLoader loader;

    /**
     * 队列调度模式
     */
    public enum Type {
        FIFO, LIFO
    }

    /**
     * 设置网络请求超时
     *
     * @param mTimeOut
     */
    public void setTimeOut(int mTimeOut) {
        this.mTimeOut = mTimeOut;
    }

    /**
     * 设置加载方式<b>RAGB_8888适合大图.RGB_565适合小图</b>
     *
     * @param loadConfig
     */
    public void setLoadConfig(Config loadConfig) {
        this.loadConfig = loadConfig;
    }

    /**
     * 设置是否需要动画.
     *
     * @param isneed
     */
    public void setNeedAnim(boolean isneed) {
        this.isNeedAnim = isneed;
    }

    /**
     * 设置加载监听
     *
     * @param loadListener
     */
    public void setLoadListener(LoadListener<View> loadListener) {
        this.loadListener = loadListener;
    }

    private ImageLoader() {

    }

    private ImageLoader(Context context, int threadCount, Type type) {
        init(context, threadCount, type);
    }

    private void init(Context context, int threadCount, Type type) {
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
        //初始化调度队列类型
        mType = type == null ? Type.LIFO : type;
        //设置默认缓存路径
        if (null != context) {
            saveLocation = context.getExternalCacheDir().getAbsolutePath();
            //初始化磁盘缓存
            try {
                helper = new DiskLruCacheHelper(context, caCheName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ImageLoader getInstance(Context context) {
        if (null == loader) {
            synchronized (ImageLoader.class) {
                if (null == loader) {
                    loader = new ImageLoader(context, defThreadCount, mType);
                }
            }
        }
        return loader;
    }

    public static ImageLoader getInstance(Context context, int threadCount, Type type) {
        if (null == loader) {
            synchronized (ImageLoader.class) {
                if (null == loader) {
                    loader = new ImageLoader(context, threadCount, type);
                }
            }
        }
        return loader;
    }

    /**
     * 加载图片
     *
     * @param path
     * @param view
     */
    public void load(final String path, final View view) {

        if (null == path)
            throw new RuntimeException("this path is null");

        view.setTag(path);
        //1.从磁盘,2.从内存
        if (null == mDisPlayHandler)
            mDisPlayHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ViewBeanHolder holder = (ViewBeanHolder) msg.obj;
                    final View view = holder.view;
                    Bitmap bm = holder.bitmap;
                    String path = holder.path;
                    if (view.getTag().toString().equals(path) && null != bm) {
                        ((ImageView) view).setImageBitmap(bm);
                        if (isNeedAnim)
                            new LoadAnimCore(view);
                    }
                }
            };

        addTask(path, view);

    }


    /**
     * 加载图片
     *
     * @param path
     * @param view
     */
    public void load(final String path, final View view, final LoadListener<View> loadListener) {

        if (null == path)
            throw new RuntimeException("this path is null");

        if (null == loadListener)
            throw new RuntimeException("this loadListener is null");

        view.setTag(path);
        //1.从磁盘,2.从内存
        if (null == mDisPlayHandler)
            mDisPlayHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    int code = msg.what;
                    ViewBeanHolder holder = (ViewBeanHolder) msg.obj;
                    final View view = holder.view;
                    Bitmap bm = holder.bitmap;
                    String path = holder.path;
                    switch (code) {
                        case LOAD_SUCCESS://加载成功
                            if (view.getTag().toString().equals(path)) {
                                loadListener.LoadSuccess(view, bm, path);
                                if (isNeedAnim)
                                    new LoadAnimCore(view);
                            }
                            break;
                        case LOAD_ING://加载中
                            if (view.getTag().toString().equals(path)) {
                                loadListener.Loading(view, path);
                            }
                            break;
                        case LOAD_FAILE://加载失败
                            if (view.getTag().toString().equals(path)) {
                                loadListener.LoadError(view, path, null);//暂时消息为空
                            }
                            break;
                    }
                }
            };

        addTask(path, view);

    }

    /**
     * 添加任务
     *
     * @param path
     * @param view
     */
    private synchronized void addTask(final String path, final View view) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ViewBeanHolder holder = new ViewBeanHolder();
                holder.view = view;
                holder.path = path;
                sendMsg(LOAD_ING, holder);
                //TODO 从内存中获取
                Bitmap bitmap = LruCacheUtils.getInstance().get(path);
                if (null == bitmap) {
                    //TODO 从磁盘中获取
                    if (urlIsNetWork(path)) {
                        //网络图片缓存
                        bitmap = decodeSampledBitmapFromDisk(path, (ImageView) view);
                    } else {
                        //本地图片缓存
                        bitmap = decodeSampledBitmapFromResource(path, (ImageView) view);
                    }
                    // TODO 从网络中获取
                    if (null == bitmap) {
                        bitmap = decodeSampledBitmapFromNetWork(path, (ImageView) view);
                    }
                }
                //加载成功
                if (null != bitmap) {
                    LruCacheUtils.getInstance().put(path, bitmap);
                    holder.bitmap = bitmap;//唯一的
                    sendMsg(LOAD_SUCCESS, holder);
                } else {
                    //加载失败
                    sendMsg(LOAD_FAILE, holder);
                }
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
     * 发送消息
     *
     * @param code
     */
    private void sendMsg(int code, ViewBeanHolder<View> holder) {
        Message message = Message.obtain();
        message.obj = holder;
        message.what = code;
        mDisPlayHandler.sendMessage(message);
    }

    /**
     * 判断地址是本地还是网络
     *
     * @param path
     */
    private boolean urlIsNetWork(String path) {
        if (path.toLowerCase().contains(DEF_KEY)) {//判断文件获取方式
            return true;
        }
        return false;
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
            }
        } catch (Exception e) {
        }
        return value;
    }


    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param pathName
     * @param imageview
     * @return
     */
    private Bitmap decodeSampledBitmapFromResource(String pathName, ImageView imageview) {
        ImageSize imageSize = getImageViewWidth(imageview);
        int reqWidth = imageSize.width;
        int reqHeight = imageSize.height;
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = loadConfig;
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
     * @param pathNmae
     * @param view
     * @return
     */
    private Bitmap decodeSampledBitmapFromDisk(String pathNmae, ImageView view) {
        // TODO 因为DiskLruCaChe已经判断了文件是否存在,所以不再需要判断文件了.
        byte[] data = helper.getAsBytes(pathNmae);
        if (null == data)
            return null;
        ImageSize imageSize = getImageViewWidth(view);
        int reqWidth = imageSize.width;
        int reqHeight = imageSize.height;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = loadConfig;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        return bitmap;
    }


    /**
     * 根据计算的inSampleSize，得到压缩后图片
     *
     * @param url
     * @param imageView
     * @return
     */
    private Bitmap decodeSampledBitmapFromNetWork(String url, ImageView imageView) {
        Bitmap bitmap = null;
        String path = getNetWork2Save(url);
        if (null != path) {
            bitmap = decodeSampledBitmapFromDisk(path, imageView);
        }
        return bitmap;
    }


    /**
     * 获取网络图片
     *
     * @param urlString
     * @return
     */
    private String getNetWork2Save(String urlString) {
        URL imgUrl = null;
        Bitmap bitmap = null;
        try {
            imgUrl = new URL(urlString);
            // 使用HttpURLConnection打开连接
            HttpURLConnection urlConn = (HttpURLConnection) imgUrl.openConnection();
            urlConn.setConnectTimeout(mTimeOut);//10秒
            urlConn.connect();
            if (200 == urlConn.getResponseCode()) {
                // 将得到的数据转化成InputStream
                InputStream is = urlConn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                helper.put(urlString, Utils.bitmap2Bytes(bitmap));
                is.close();
                if (!bitmap.isRecycled())
                    bitmap.recycle();
                return urlString;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
    private synchronized Runnable getTask() {
        if (0 < mTask.size()) {
            if (mType == Type.LIFO)
                return mTask.removeFirst();
            else
                return mTask.removeLast();
        }
        return null;
    }

    /**
     * 设置存储路径
     *
     * @param location
     */
    public void setCaCheName(String location) {
        this.caCheName = location;
    }
}
