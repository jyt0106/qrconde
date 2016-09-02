package com.hs.qrconde.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.hs.activity.qrconde.R;
import com.hs.qrconde.camera.RGBLuminanceSource;
import com.hs.qrconde.encoding.DecodeFormatManager;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

/*
* 本使用二维码demo，只需在Activity中配置、改写即可。对于学习的建议不要改写其他。
* 本例仅仅是一个demo，本人也是在别人的基础上改写的，如果需要在项目中使用，请对其中代码仔细分析，以免造成不必要的损失。
* */

public class MainActivity extends Activity implements Window.Callback {
    private Button myQR, scan, read;
    private ImageView ivMyqr;
    private EditText text;
    private TextView tvresult;

    private ImageView imageview;
    private Bitmap mBitmap;

    Bitmap bitmap = null;


    //中间小图片的宽和高
    private final static int WIDTH = 40;
    private final static int HEIGHT = 40;
    //图片宽度的一半
    private final static int IMAGE_HALFWIDTH = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化默认的二维码内容
        String str = initStr();
        initview(str);

    }

    private void initview(final String str) {
        myQR = (Button) findViewById(R.id.btnMyQR);
        scan = (Button) findViewById(R.id.btnScan);
        read = (Button) findViewById(R.id.btnRead);
        ivMyqr = (ImageView) findViewById(R.id.ivMyqr);
        text = (EditText) findViewById(R.id.etText);
        tvresult = (TextView) findViewById(R.id.tvresult);

        // 构造对象
        //imageview = new ImageView(this);
        // 构造需要插入的图片对象
        mBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.ww_40)).getBitmap();
        // 缩放图片
        Matrix m = new Matrix();
        float sx = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getWidth();
        float sy = (float) 2 * IMAGE_HALFWIDTH / mBitmap.getHeight();
        m.setScale(sx, sy);
        // 重新构造一个w*h的图片
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
                mBitmap.getHeight(), m, false);


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        myQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String texts = text.getText().toString();
                if (TextUtils.isEmpty(texts)) {
                    texts = str;
                }

                try {
                    bitmap = createBitmap(texts, 350);
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                if (bitmap != null) {
                    ivMyqr.setImageBitmap(bitmap);
                    ivMyqr.setVisibility(View.VISIBLE);
                }
            }
        });
       //final Result r;
        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    //String r = parseQRcodeBitmap(bitmap).getText().toString();
                   String r = parseQRcodeBitmaps(Environment.getExternalStorageDirectory()+"/abc1.png").getText().toString();
                    if(r!=null)
                    Log.e("tga",r);
                }
            }
        });
    }

    public Bitmap createBitmap(String str, int qrWidth) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        try {     //处理中文乱码，将字符串转换成ISO-8859-1编码
            str = new String(str.getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, qrWidth, qrWidth);//正方形
        //获取图片的尺寸
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int halfW = width / 2;
        int halfH = height / 2;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //将图片插入到二维码正中间
                if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH && y > halfH - IMAGE_HALFWIDTH && y < halfH + IMAGE_HALFWIDTH) {
                    pixels[y * width + x] = mBitmap.getPixel(x - halfW + IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
                    // pixels[y * width + x] = 0xffffffff;//如果没有图片可用其他颜色替代
                } else {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 2) {
            String result = data.getExtras().get("result").toString();
            tvresult.setText(result);
            tvresult.setVisibility(View.VISIBLE);
        }
    }

    private String initStr() {
        return "秋天是金黄的，院子里树木变黄了，渐渐枯萎了。金灿灿的秋阳暖烘烘地照着大地，把人们、草地、树木照得都变黄了。\n" +
                "秋天是丰收的，田野里到处是丰收的歌声，一阵风拂过，大豆摇起响亮的铜铃，高粱举起火红的火把，麦子在一旁不住地点头……农民伯伯们看到了一年的成果，更是笑个不停。\n" +
                "秋天是香甜的，果园里果子熟了，苹果像小姑娘害羞的脸，香蕉全身金灿灿的 ，像一个初五初六的月亮，梨子呢，也是黄澄澄的，大概也是喜欢黄色罢了。葡萄紫檀檀的，像有个个紫色的小气球……大家你挤我挤，都准备让人们去摘呢!";

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

    //  //解析二维码图片,返回结果封装在Result对象中
    private Result parseQRcodeBitmap(String bitmapPath) {
        //解析转换类型UTF-8
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
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
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);
        //新建一个RGBLuminanceSource对象，将bitmap图片传给此对象
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

    //解析二维码图片,返回结果封装在Result对象中
    private Result parseQRcodeBitmaps(String bitmapPath) {
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
}

