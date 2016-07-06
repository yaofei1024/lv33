package com.it.lv3;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ImageLruCache extends LruCache<String, Bitmap> implements
        ImageLoader.ImageCache {

    private static DiskLruCache mDiskLruCache;

    public ImageLruCache(Context context, int maxSize, String diskCacheFodler
            , int diskCacheSize) {
        super(maxSize);
        try {
            File cacheDir = getDiskCacheDir(context, diskCacheFodler);
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            mDiskLruCache = DiskLruCache.open(cacheDir,
                    getAppVersion(context), 1, diskCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int sizeOf(String key, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    //先从LruCache中拿，再从DiskLruCache中拿，最后从网上拿
    @Override
    public Bitmap getBitmap(String s) { //感觉应该先从LruCache中取，取不到再从硬盘中取
        String key = hashKeyForDisk(s);
        try {
            if (mDiskLruCache.get(key) == null) {//当图片不在LruCache时，再次
                return this.get(s);
            } else { //查询DiskLruCache中是否存在，存在的话取出然后转换成Bitmap并返回
                DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                Bitmap bitmap = null;
                if (snapShot != null) {
                    InputStream is = snapShot.getInputStream(0);
                    bitmap = BitmapFactory.decodeStream(is);
                }
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void putBitmap(String s, Bitmap bitmap) {
        this.put(s, bitmap);//先放到二级 缓存中
        //往三级缓存中放
        String key = hashKeyForDisk(s);
        try {
            if (null == mDiskLruCache.get(key)) {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                }
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static File getDiskCacheDir(Context context, String folder) {//选择缓存地址
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + folder);
    }

    public int getAppVersion(Context context) {   //获得应用version号码
        try {
            PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //根据key生成md5值，保证缓存文件名称的合法化
    public String hashKeyForDisk(String key) {//MD5Utils.MD5();
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
