package capstone.android.com.whattoeat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity  {
    private static final String TAG = "ResultActivity";
    PlaceInfo info;
    ArrayList<String> menuList;
    ArrayList<String> reviewList;
    ArrayList<String> tReviewList = new ArrayList<>();
    String title, id, lnt, lat;
    HP hp;
    GPA gpa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        //결과값 가져오기
        title = intent.getExtras().getString("title");
        lat = intent.getExtras().getString("lat");
        lnt = intent.getExtras().getString("lnt");
        id = intent.getExtras().getString("id");

        //파싱
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    gpa = new GPA(title, lat, lnt, "100");
                    info = gpa.DetailSearch(id);
                    hp = new HP(info.getName());
                    menuList = hp.getMenu();
                    reviewList = info.getReviews();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        }catch (InterruptedException e){
          e.printStackTrace();
        }
        //화면에 출력
        TextView resultTitle = (TextView)findViewById(R.id.resultTitle);
        TextView resultRating = (TextView)findViewById(R.id.resultRating);
        TextView resultPriceLevel = (TextView)findViewById(R.id.resultPriceLevel);
        RatingBar resultStars = (RatingBar)findViewById(R.id.resultStar);
        final ListView listM = (ListView)findViewById(R.id.menu);
        final ListView listR = (ListView)findViewById(R.id.review);


        //리스트뷰와 리스트를 연결하기 위해 사용되는 어댑터
        ArrayAdapter<String> adapterMenu = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, menuList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)   // 글자 하얀색으로 만들어주기
            {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);
                return view;
            }
        };
        final ArrayAdapter<String> adapterReview = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, reviewList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);
                return view;
            }
        };

        final ArrayAdapter<String> adapterTranslateReview = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, tReviewList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);
                return view;
            }
        };

        resultTitle.setText(info.getName());
        resultPriceLevel.setText(info.getPriceLevel());
        resultRating.setText(info.getRating()+" / 5.0");
        float rating = 0;
        rating = Float.parseFloat(info.getRating());
        resultStars.setRating(rating);

        //리스트뷰의 어댑터를 지정해준다.
        listM.setAdapter(adapterMenu);
        listR.setAdapter(adapterReview);

        findViewById(R.id.returnTranslation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listR.setAdapter(adapterReview);
            }
        });
        findViewById(R.id.translation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tReviewList.isEmpty()){
                    Thread tThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0 ;i<reviewList.size();i++){
                                try {
                                    tReviewList.add(gpa.Translate(reviewList.get(i)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    tThread.start();
                    try{
                        tThread.join();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                listR.setAdapter(adapterTranslateReview);
            }
        });
    }
}
