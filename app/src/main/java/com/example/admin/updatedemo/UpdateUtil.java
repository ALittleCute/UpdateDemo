package com.example.admin.updatedemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by xiaojin on 2017/7/17.
 */

public class UpdateUtil {
    private File tempFile = null;
    private int download_precent = 0;
    private DownloadCallback downloadCallback = null;
    private WeakReference<Context> mContext;

    private static UpdateUtil instance = null;

    private UpdateUtil() {

    }

    public static synchronized UpdateUtil getInstance() {
        if (instance == null) {
            instance = new UpdateUtil();
        }
        return instance;
    }
    public interface DownloadCallback {
        //下载进度
        void downloadPrecent(int precent);

        //下载失败
        void downloadFail();

        //下载成功
        void downloadSuccess();
    }

    //下载更新文件
    public void downFile(final String url, final Context context, DownloadCallback downloadCallback) {
        this.downloadCallback = downloadCallback;
        this.mContext = new WeakReference<Context>(context);
        new Thread() {
            public void run() {
                try {
                    HttpClient client = new DefaultHttpClient();
                    // params[0]代表连接的url
                    HttpGet get = new HttpGet(url);
                    HttpResponse response = client.execute(get);
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                        long length = entity.getContentLength();
                        InputStream is = entity.getContent();
                        if (is != null) {
                            tempFile = makeFilePath(Environment.getExternalStorageDirectory().getAbsolutePath()+"/UpdateDemo" ,"/"+url.substring(url.lastIndexOf("/") + 1));
                            if (!tempFile.exists()) {
                                tempFile.getParentFile().mkdirs();
                                tempFile.createNewFile();
                            }
                            //已读出流作为参数创建一个带有缓冲的输出流
                            BufferedInputStream bis = new BufferedInputStream(is);

                            //创建一个新的写入流，讲读取到的图像数据写入到文件中
                            FileOutputStream fos = new FileOutputStream(tempFile);
                            //已写入流作为参数创建一个带有缓冲的写入流
                            BufferedOutputStream bos = new BufferedOutputStream(fos);

                            int read;
                            long count = 0;
                            int precent = 0;
                            byte[] buffer = new byte[1024];
                            while ((read = bis.read(buffer)) != -1) {
                                bos.write(buffer, 0, read);
                                count += read;
                                precent = (int) (((double) count / length) * 100);
                                Message message=mHandle.obtainMessage(1);
                                message.arg1 = precent;
                                mHandle.sendMessage(message);
                            }
                            bos.flush();
                            bos.close();
                            fos.flush();
                            fos.close();
                            is.close();
                            bis.close();
                        }
                        Message message=mHandle.obtainMessage(3);
                        mHandle.sendMessage(message);
                    }

                } catch (Exception e) {
                    Log.e("xiaojin error :",e.getMessage());
                    Message message=mHandle.obtainMessage(2);
                    mHandle.sendMessage(message);
                }
            }
        }.start();
    }
    private Handler mHandle = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what){
                case 1:
                    downloadPrecenting(msg.arg1);
                    break;
                case 2:
                    downloadFail();
                    break;
                case 3:
                    downloadFinish(mContext.get());
                    break;
            }
        }
    };
    private void downloadFail() {
        download_precent = 0;
        if (downloadCallback != null) {
            downloadCallback.downloadFail();
        }
    }

    private void downloadPrecenting(int precent) {
        if (downloadCallback != null && precent - download_precent >= 1) {
            download_precent = precent;
            downloadCallback.downloadPrecent(download_precent);
        }
    }

    private void downloadFinish(Context context) {
        download_precent = 0;
        if (downloadCallback != null) {
            downloadCallback.downloadSuccess();
        }
        instanll(tempFile, context);
    }


    //安装下载后的apk文件
    private void instanll(File file, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public DownloadCallback getDownloadCallback() {
        return downloadCallback;
    }

    public void setDownloadCallback(DownloadCallback downloadCallback) {
        this.downloadCallback = downloadCallback;
    }
    // 生成文件
    private File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("xiaojin error:", e.getMessage());
        }
        return file;
    }

    // 生成文件夹
    private  void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.e("xiaojin error:", e.getMessage());
        }
    }

}
