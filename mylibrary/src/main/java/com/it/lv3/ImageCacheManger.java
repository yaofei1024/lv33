package com.it.lv3;

import android.content.Context;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;

public class ImageCacheManger {
    public ImageLoader mImageLoder;

    public ImageCacheManger(Context context) {
        // 取运行内存阈值的1/8作为图片缓存
        final int MAX_MEMORY = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int MEMORY_CACHE_SIZE = MAX_MEMORY / 8;

        ImageLruCache mImageLreCache = new ImageLruCache(context, MEMORY_CACHE_SIZE,
                "images", 10 * 1024 * 1024);

        VolleyManager manager = VolleyManager.getInstance(context);
        mImageLoder = new ImageLoader(manager.getmRequestQueue(),
                mImageLreCache);//采用二级缓存
    }

    public ImageLoader.ImageContainer loadImage(final String url, final ImageView view,
                                                final int defaultImageBitmap, final int errorImageBitmap) {
        ImageLoader.ImageListener listener =
                mImageLoder.getImageListener(view, defaultImageBitmap, errorImageBitmap);
        return mImageLoder.get(url, listener);
    }
}