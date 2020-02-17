package com.example.opencvimageexample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

// 빌드된 공유 라이브러리는 자바 코드에서 로드함
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }


    ImageView imageVIewInput;
    ImageView imageVIewOuput;
    private Mat img_input;
    private Mat img_output;
    private int threshold1=50;
    private int threshold2=150;

    private static final String TAG = "opencv";
    private final int GET_GALLERY_IMAGE = 200;

    boolean isReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageVIewInput = (ImageView)findViewById(R.id.imageViewInput);
        imageVIewOuput = (ImageView)findViewById(R.id.imageViewOutput);

        Button Button = (Button)findViewById(R.id.button);
        Button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

//                버튼 클릭시 쓰레솔드를 아규먼트로 하여 imageprocess_and_showResult를 호출
                imageprocess_and_showResult(threshold1, threshold2);
            }
        });


        imageVIewInput.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);// 이미지뷰 클릭시 갤러리에서 이미지 선택 할 수 있도록 함
            }
        });


        final TextView textView1 = (TextView)findViewById(R.id.textView_threshold1);
        SeekBar seekBar1=(SeekBar)findViewById(R.id.seekBar_threshold1); // 캐니의 스레쑐드를 조정하기 위해 Seekbar를 사용함
        seekBar1.setProgress(threshold1); // SeekBar의 초기값
        seekBar1.setMax(200); //최대값
        seekBar1.setMin(0); // 최소값
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                threshold1 = progress; // SeekBar를 움직일 때 에지를 검출하여 화면에 보여지도록 함
                textView1.setText(threshold1+"");
                imageprocess_and_showResult(threshold1, threshold2);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

// 두번째 SeekBar도 위와 동일하게 동작
        final TextView textView2 = (TextView)findViewById(R.id.textView_threshold2);
        SeekBar seekBar2=(SeekBar)findViewById(R.id.seekBar_threshold2);
        seekBar2.setProgress(threshold2);
        seekBar2.setMax(200);
        seekBar2.setMin(0);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold2 = progress;
                textView2.setText(threshold2+"");
                imageprocess_and_showResult(threshold1, threshold2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//앱을 실핼할 때마다 퍼미션 체크
        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
// onResume가 호출된 이후에 SeekBar를 사용할 수 있도록 함
        isReady = true;
    }

//    자바 코드에 선언해둔 네이티브 함수를 통해 공유 라이브러리에 있는 함수를 호출
    public native void imageprocessing(long inputImage, long outputImage, int th1, int th2);

//    결과를 저장할 img_output에 Mat 객체를 생성해야 함
    private void imageprocess_and_showResult(int th1, int th2) {

        if (isReady==false) return;

        if (img_output == null)
            img_output = new Mat();

//        네이티브 함수에 두 개의 Mat 객체 주소(img_input.getNativeObjAddr(), img_output.getNativeObjAddr())와
//         두 개의 스레쏠드(th1, th2)를 넘겨주면 cpp 파일에 있는 함수에 전달됨
        imageprocessing(img_input.getNativeObjAddr(), img_output.getNativeObjAddr(), th1, th2);

// 에지 검출 결과를 이미지뷰에 보여줌
        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageVIewOuput.setImageBitmap(bitmapOutput);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE) {


            if (data.getData() != null) {
                Uri uri = data.getData();

                try {
                    String path = getRealPathFromURI(uri);
                    int orientation = getOrientationOfImage(path); // 런타임 퍼미션 필요
                    Bitmap temp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap bitmap = getRotatedBitmap(temp, orientation);
                    imageVIewInput.setImageBitmap(bitmap);

                    img_input = new Mat(); // 갤러리에서 가져온 이미지를 Mat 객체로 변환
                    Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Utils.bitmapToMat(bmp32, img_input);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }


    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    // 출처 - http://snowdeer.github.io/android/2016/02/02/android-image-rotation/
    public int getOrientationOfImage(String filepath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            Log.d("@@@", e.toString());
            return -1;
        }

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

        if (orientation != -1) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
            }
        }

        return 0;
    }

    public Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) throws Exception {
        if(bitmap == null) return null;
        if (degrees == 0) return bitmap;

        Matrix m = new Matrix();
        m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }



    // 퍼미션 코드
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};//매니페스트에도 추가

    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }
}