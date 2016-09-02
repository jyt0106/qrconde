package com.hs.qrconde.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Hashtable;

/**
 * Created by Huang hai-sen on 2016/9/1.
 */
public class RGBLuminanceSource extends LuminanceSource{
    private final byte[] luminances;
    public RGBLuminanceSource(Bitmap bitmap) {
        super(bitmap.getWidth(), bitmap.getHeight());
        //得到图片的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //得到图片的像素
        int[] pixels = new int[width * height];
        //
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        //为了测量纯解码速度,我们将整个图像灰度阵列前面,这是一样的通道
        // YUVLuminanceSource在现实应用。
        //得到像素大小的字节数
        luminances = new byte[width * height];
        //得到图片每点像素颜色
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int pixel = pixels[offset + x];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                //当某一点三种颜色值相同时，相应字节对应空间赋值为其值
                if (r == g && g == b) {
                    luminances[offset + x] = (byte) r;
                }
                //其它情况字节空间对应赋值为：
                else {
                    luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
                }
            }
        }
    }

    /**
     * 解析二维码图片,返回结果封装在Result对象中
     * @param bitmapPath 包含二维码图片所在的路径
     * @return
     */
    public Result parseQRcodeBitmaps(String bitmapPath) {
        Bitmap bitmap = getBitmap(bitmapPath);
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        RGBLuminanceSource rgbLuminanceSource = new RGBLuminanceSource(bitmap);
        //将图片转换成二进制图片
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rgbLuminanceSource));
        //初始化解析对象
        QRCodeReader reader = new QRCodeReader();
        //开始解析
        Result result = null;
        try {
            result = reader.decode(binaryBitmap, hints);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

    public Bitmap getBitmap(String bitmapPath){
        //获取到待解析的图片
        BitmapFactory.Options options = new BitmapFactory.Options();
        //如果我们把inJustDecodeBounds设为true，那么BitmapFactory.decodeFile(String path, Options opt)
        //并不会真的返回一个Bitmap给你，它仅仅会把它的宽，高取回来给你
        options.inJustDecodeBounds = true;
        //此时的bitmap是null，这段代码之后，options.outWidth 和 options.outHeight就是我们想要的宽和高了
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //我们现在想取出来的图片的边长（二维码图片是正方形的）设置为400像素
        /**
         options.outHeight = 400;
         options.outWidth = 400;
         options.inJustDecodeBounds = false;
         bitmap = BitmapFactory.decodeFile(bitmapPath, options);
         */
        //以上这种做法，虽然把bitmap限定到了我们要的大小，但是并没有节约内存，如果要节约内存，我们还需要使用inSimpleSize这个属性
        options.inSampleSize = options.outHeight / 400;
        if (options.inSampleSize <= 0) {
            options.inSampleSize = 1; //防止其值小于或等于0
        }
        /**
         * 辅助节约内存设置
         *
         * options.inPreferredConfig = Bitmap.Config.ARGB_4444;    // 默认是Bitmap.Config.ARGB_8888
         * options.inPurgeable = true;
         * options.inInputShareable = true;
         */
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(bitmapPath, options);
    }

    @Override
    public byte[] getRow(int arg0, byte[] bytes) {
        if (arg0 < 0 || arg0 >= getHeight()) {
            throw new IllegalArgumentException("Requested row is outside the image:  "+ arg0);
        }
        int width = getWidth();
        if (bytes == null || bytes.length < width) {
            bytes = new byte[width];
        }
        System.arraycopy(luminances, arg0 * width, bytes, 0, width);
        return bytes;
    }

    @Override
    public byte[] getMatrix() {
        return luminances;
    }
}
