package capstone.android.com.whattoeat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class VoiceRecognition extends Activity implements RecognitionListener {
    private final String TAG = "VoiceRecognition";
    private final static int PERMISSIONS_REQUEST_CODE = 101;
    private SpeechRecognizer speech;
    static Location myLocation; //나의 위치
    private Intent recognizerIntent;
    private final int RESULT_SPEECH = 1000;
    LocationListener locationListener;
    LocationManager locationManager;
    @Override
    protected void onResume() {
        super.onResume();
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
        locationManager.removeUpdates(locationListener);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognition);

        ///////
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //API 23 이상이면
                // 런타임 퍼미션 처리 필요
                int hasGPSPermission = ContextCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION );
                if ( hasGPSPermission==PackageManager.PERMISSION_GRANTED){
                    ;//이미 퍼미션을 가지고 있음
                }
                else {
                    //퍼미션 요청
                    ActivityCompat.requestPermissions( this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_CODE);
                }
            }
            else{
                ;
            }
        } else {
            Toast.makeText(this, "GPS not supported",
                    Toast.LENGTH_LONG).show();
        }
        //////

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);

        findViewById(R.id.voice_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko-KR"); //언어지정입니다.en-US=영어,zh-Hans=중국어 ko-KR=한국어
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);   //검색을 말한 결과를 보여주는 갯수
                startActivityForResult(recognizerIntent, RESULT_SPEECH);
            }
        });

}

    @Override
    public void onEndOfSpeech() {
        showProgressDialog();
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) { }

    @Override
    public void onResults(Bundle results) { }

    @Override
    public void onError(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "오디오 에러";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "클라이언트 에러";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "퍼미션없음";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "네트워크 에러";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "네트웍 타임아웃";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "찾을수 없음";;
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "바쁘대";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "서버이상";;
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "말하는 시간초과";
                break;
            default:
                message = "알수없음";
                break;
        }

        Log.e(TAG, "SPEECH ERROR : " + message);
    }

    @Override
    public void onRmsChanged(float v) { }

    @Override
    public void onBeginningOfSpeech() { }

    @Override
    public void onEvent(int i, Bundle bundle) { }

    @Override
    public void onPartialResults(Bundle bundle) { }

    @Override
    public void onBufferReceived(byte[] bytes) { }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH : {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if(myLocation != null){
                        //위경도 수정
                        //LatLng currentLatLng = new LatLng(37.299061509421705, 127.04336207360029);
                        LatLng currentLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                        Double lat = currentLatLng.latitude;
                        Double lnt = currentLatLng.longitude;
                        Intent intent = new Intent(this, ResultActivity.class);
                        intent.putExtra("title",text.get(0));
                        intent.putExtra("lat", lat.toString());
                        intent.putExtra("lnt",lnt.toString());
                        startActivity(intent);
                    }
                    else Toast.makeText(this,"위/경도가 잡히지 않았습니다.\n 다시 시도해주세요.",Toast.LENGTH_SHORT).show();

                }
                break;
            }
        }
    }
    private void showProgressDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("데이터 확인중");
        dialog.show();
    }
}
