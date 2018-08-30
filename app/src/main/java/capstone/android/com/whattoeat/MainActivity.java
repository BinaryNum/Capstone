package capstone.android.com.whattoeat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onImageButtonClicked(View v){
        Intent intent = new Intent(this, ImageRecognition.class);
        intent.putExtra("flag","0");
        startActivity(intent);
    }
    public void onVoiceButtonClicked(View v){
        Intent intent = new Intent(this, VoiceRecognition.class);
        startActivity(intent);
    }
    public void onMapButtonClicked(View v){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}
