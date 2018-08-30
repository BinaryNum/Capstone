package capstone.android.com.whattoeat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
/**
 * Created by JinSu on 2018-04-09.
 */
public class PaintView extends View {
    Paint paint = new Paint();
    Path path  = new Path();    // 자취를 저장할 객체
    float x1=0, x2=0, y1=0, y2=0;
    float startX,startY,finishX,finishY;

    public PaintView(Context context) {
        super(context);
        paint.setStyle(Paint.Style.STROKE); // 선이 그려지도록
        paint.setStrokeWidth(10f); // 선의 굵기 지정
    }
    @Override
    public boolean performClick() {
        super.performClick();
        // do what you want
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) { // 화면을 그려주는 메서드
        canvas.drawPath(path, paint); // 저장된 path 를 그려라
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.performClick();
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                x1 = x; y1 = y;
                path.reset();
                path.moveTo(x, y); // 자취에 그리지 말고 위치만 이동해라
                break;
            case MotionEvent.ACTION_MOVE :
                x2 = x; y2 = y;
                path.reset();
                if(x1>x2){
                    startX = x2;
                    finishX = x1;
                }else {
                    startX = x1;
                    finishX = x2;
                }
                if(y1>y2){
                    startY = y2;
                    finishY = y1;
                }else {
                    startY = y1;
                    finishY = y2;
                }
                path.addRect(startX,startY,finishX,finishY, Path.Direction.CW);
                // path.lineTo(x, y); // 자취에 선을 그려라
                break;
            case MotionEvent.ACTION_UP :
                x2 = x; y2 = y;

                if(x1>x2){
                    startX = x2;
                    finishX = x1;
                }else {
                    startX = x1;
                    finishX = x2;
                }
                if(y1>y2){
                    startY = y2;
                    finishY = y1;
                }else {
                    startY = y1;
                    finishY = y2;
                }
                //path.addRect(startX,startY,finishX,finishY, Path.Direction.CW);
                path.reset();
                break;
        }
        invalidate(); // 화면을 다시그려라
        return true;
    }
}
