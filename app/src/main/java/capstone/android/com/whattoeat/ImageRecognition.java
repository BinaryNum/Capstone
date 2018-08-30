package capstone.android.com.whattoeat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayOutputStream;


public class ImageRecognition extends AppCompatActivity  {
    private static final String TAG = "ImageRecognition";

    Preview preview;
    Camera camera;
    Context ctx;
    private AppCompatActivity mActivity;
    ///
    float x1=0, x2=0, y1=0, y2=0; //사진자르기1
    float Dwidth, Dheight; //사진자르기2
    static Location myLocation; //나의 위치
    static String rest;
    String result = new String("");
    LocationListener locationListener;
    LocationManager locationManager;

    private static final String CLOUD_VISION_API_KEY = "AIzaSyCoiEEZdYhrNsaT8YHabMJ4NdVHzUGwQuE";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    ////
    private final static int PERMISSIONS_REQUEST_CODE = 100;
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; //카메라 앞뒤 설정

    public static void doRestart(Context c) {
        //http://stackoverflow.com/a/22345538
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted
                        // after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr =
                                (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG, "Was not able to restart application, " +
                                "mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }

    public void startCamera() {
        if ( preview == null ) {
            preview = new Preview(this, (SurfaceView) findViewById(R.id.surfaceView));
            preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            ((RelativeLayout) findViewById(R.id.layout)).addView(preview);

            PaintView m = new PaintView(this);
            ((RelativeLayout) findViewById(R.id.layout)).addView(m);
            preview.setKeepScreenOn(true);

            //뷰크기 구하기
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Dwidth = size.y;
            Dheight = size.x;

            m.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction()==MotionEvent.ACTION_DOWN){
                        camera.autoFocus (new Camera.AutoFocusCallback() {
                            public void onAutoFocus(boolean success, Camera camera) { }
                        });
                        x1 = event.getY();
                        y1 = Dheight - event.getX();
                    }
                    else if(event.getAction()==MotionEvent.ACTION_UP){
                        x2 = event.getY();
                        y2 = Dheight - event.getX();
                        //SWAP
                        float temp;
                        if(x1>x2) {
                            temp = x1;
                            x1 = x2;
                            x2 = temp;
                        }
                        if(y1>y2){
                            temp = y1;
                            y1 = y2;
                            y2 = temp;
                        }
                        // 작은 영역 지정시 다시 지정
                        if((y2-y1)*(x2-x1) > (Dheight*Dwidth) / 40) {
                            //사진 & 구글비전 & 결과창 이동
                            camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                        }
                        else Toast.makeText(ctx, "영역을 더 크게 지정하세요", Toast.LENGTH_LONG).show();
                    }

                    return false;
                }
            });
        }

        preview.setCamera(null);
        if (camera != null) {
            camera.release();
            camera = null;
        }

        int numCams = Camera.getNumberOfCameras();
        if (numCams > 0) {
            try {

                camera = Camera.open(CAMERA_FACING);
                // camera orientation
                camera.setDisplayOrientation(setCameraDisplayOrientation(this, CAMERA_FACING,
                        camera));
                // get Camera parameters
                Camera.Parameters params = camera.getParameters();
                // picture image orientation
                params.setRotation(setCameraDisplayOrientation(this, CAMERA_FACING, camera));
                camera.startPreview();

            } catch (RuntimeException ex) {
                Toast.makeText(ctx, "camera_not_found " + ex.getMessage().toString(),
                        Toast.LENGTH_LONG).show();
                Log.d(TAG, "camera_not_found " + ex.getMessage().toString());
            }
        }

        preview.setCamera(camera);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        mActivity = ImageRecognition.this;

        //상태바 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_image_recognition);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)&&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                // 런타임 퍼미션 처리 필요
                int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                int hasGPSPermission = ContextCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION );
                if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED
                        && hasGPSPermission==PackageManager.PERMISSION_GRANTED){
                    ;//이미 퍼미션을 가지고 있음
                }
                else {
                    //퍼미션 요청
                    ActivityCompat.requestPermissions( this,
                            new String[]{Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_CODE);
                }
            }
            else{
                ;
            }
        } else {
            Toast.makeText(this, "Camera not supported",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
        Intent intent = getIntent();
            String flag = intent.getExtras().getString("flag");
        if(flag.equals("1")) Toast.makeText(getApplicationContext(),"인식에 실패하였습니다.\n 다시 시도해주세요.",Toast.LENGTH_LONG).show();
        /////////////
        if(ContextCompat.checkSelfPermission( this,
                android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED){
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // GPS 프로바이더 사용가능여부
            Boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // 네트워크 프로바이더 사용가능여부
            Boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.d(TAG, "isGPSEnabled="+ isGPSEnabled);
            Log.d(TAG, "isNetworkEnabled="+ isNetworkEnabled);

            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) { myLocation = location;}
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                public void onProviderEnabled(String provider) {}
                public void onProviderDisabled(String provider) {}
            };
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
        ////////////
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Surface will be destroyed when we return, so stop the preview.
        if(camera != null) {
            // Call stopPreview() to stop updating the preview surface
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }

        locationManager.removeUpdates(locationListener);
        ((RelativeLayout) findViewById(R.id.layout)).removeView(preview);
        preview = null;

    }

    private void resetCam() {
        startCamera();
    }


    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };


    //참고 : http://stackoverflow.com/q/37135675
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            //이미지의 너비와 높이 결정////////////////////////////////////////////////////////
            int orientation = setCameraDisplayOrientation(mActivity, CAMERA_FACING, camera);

            //byte array를 bitmap으로 변환
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length, options);

            float w = bitmap.getWidth();
            float h = bitmap.getHeight();

            //이미지를 디바이스 방향으로 회전////////////////////////////////////////////////////////////////////
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            //bitmap =  Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

            //분할 촬영
            float nw = ((x2-x1)/Dwidth) * w * 1.2f;
            float nh = ((y2-y1)/Dheight) * h * 1.1f;
            if(nw>w) nw = w;
            if(nh>h) nh = h;
            float startX, startY;
            startX = ((x1)/Dwidth) * w * 0.8f;
            startY = ((y1)/Dheight) * h * 0.9f;
            if(nw<=w*0.8 && nh<=h*0.8 && (startX>=0 && startX<=w) && (startY>=0 && startY<=h)){
                bitmap =  Bitmap.createBitmap(bitmap, (int)startX, (int)startY, (int)nw, (int)nh, matrix, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);

                byte[] currentData = stream.toByteArray();
                LatLng currentLatLng;
                ///////결과창 이동
                if(myLocation != null) {
                    //위경도 수정
                    //currentLatLng = new LatLng(37.299061509421705, 127.04336207360029);
                    currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                        //일본 위경도
                        Double lat = currentLatLng.latitude;
                        Double lnt = currentLatLng.longitude;
                        Intent intent = new Intent(ctx, GoogleVisionActivity.class);
                        intent.putExtra("image", currentData);
                        intent.putExtra("lat", lat.toString());
                        intent.putExtra("lnt", lnt.toString());
                        startActivity(intent);
                }
                else Toast.makeText(getApplicationContext(),"위/경도가 잡히지 않았습니다.\n 다시 시도해 주세요.",Toast.LENGTH_SHORT).show();

                resetCam();
                Log.d(TAG, "onPictureTaken - jpeg");
            }
            else Toast.makeText(ctx, "영역을 지정 오류 다시 지정..",
                    Toast.LENGTH_LONG).show();
        }
    };


    /**
     *
     * @param activity
     * @param cameraId  Camera.CameraInfo.CAMERA_FACING_FRONT,
     *                    Camera.CameraInfo.CAMERA_FACING_BACK
     * @param camera
     *
     * Camera Orientation
     * reference by https://developer.android.com/reference/android/hardware/Camera.html
     */
    public static int setCameraDisplayOrientation(Activity activity,
                                                  int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length > 0) {
            int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED){
                //이미 퍼미션을 가지고 있음
                doRestart(this);
            }
            else{
                checkPermissions();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        boolean cameraRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA);
        if ( (hasCameraPermission == PackageManager.PERMISSION_DENIED && cameraRationale))
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
        else if ( (hasCameraPermission == PackageManager.PERMISSION_DENIED && !cameraRationale))
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        else if ( hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            doRestart(this);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //퍼미션 요청
                ActivityCompat.requestPermissions( mActivity,
                        new String[]{Manifest.permission.CAMERA,},
                        PERMISSIONS_REQUEST_CODE);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
}
