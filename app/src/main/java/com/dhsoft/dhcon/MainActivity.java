package com.dhsoft.dhcon;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";


    private ImageView imageView;

    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.iv);

        CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Log.d(TAG, "CropImage onActivityReslt Uri : " + resultUri);

                if (resultUri == null) return;

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (bitmap == null) return;
                this.mBitmap = bitmap;

                final Bitmap textDrawedBitmap = drawTextOnBitmap(mBitmap, "TEST");
                imageView.setImageBitmap(textDrawedBitmap);

                saveBitmapInDirectory(getApplicationContext(), textDrawedBitmap);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "CropImage onActivityResult Exception : " + error.getMessage());
            }

        }
    }

    public Bitmap drawTextOnBitmap(Bitmap bitmap, String text) {

        Bitmap.Config bitmapConfig = bitmap.getConfig();
        if (bitmapConfig == null) bitmapConfig = Bitmap.Config.ARGB_8888;

        bitmap = bitmap.copy(bitmapConfig, true);


        float fontSize = bitmap.getWidth() / 4;
        if (text.length() > 4) {
            fontSize = bitmap.getWidth() / text.length();
        }


        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(fontSize);

        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(0.5f);
        strokePaint.setColor(Color.GRAY);
        strokePaint.setTextSize(fontSize);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        canvas.drawText(text, canvas.getWidth() / 2 - bounds.width() / 2, bitmap.getHeight() - bounds.height() / 2 + 2, paint);
        canvas.drawText(text, canvas.getWidth() / 2 - bounds.width() / 2, bitmap.getHeight() - bounds.height() / 2 + 2, strokePaint);

        return Bitmap.createScaledBitmap(bitmap, 128, 128, true);
    }

    public static void saveBitmapInDirectory(Context context, Bitmap bitmap) {
        final String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        final String folderName = "/DHCon/";
        final String fileName = new Date().toString() + ".jpg";

        final String path = externalPath + folderName;

        File filePath;
        try {
            filePath = new File(path);
            if (!filePath.isDirectory()) {
                Log.d("ImageEditor", "isDirectory : " + filePath.mkdirs());
            }
            FileOutputStream out = new FileOutputStream(path + fileName);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

            galleryAddPic(context, path + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void galleryAddPic(Context context, String currentPhotoPath) {
        Log.d("ImageEditor", "galleryAddPic");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Log.d("ImageEditor", "gallery exist : " + f.exists());

        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
