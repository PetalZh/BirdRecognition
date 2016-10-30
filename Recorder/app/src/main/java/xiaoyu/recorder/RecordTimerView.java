package xiaoyu.recorder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Xiaoyu on 8/24/2016.
 */
public class RecordTimerView extends View {
    private Paint circlePaint;
    private Paint ringPaint;
    private Paint textPaint;
    private int circleColor;
    private int ringColor;
    private float circleRadius;
    private float ringRadius;
    private float strokeWidth;
    private int xCenter;
    private int yCenter;
    private float txtWidth;
    private float txtHeight;
    private int totalProgress = 30;
    private int progress;


    public RecordTimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initVariable();
    }

    private void initAttrs(Context context, AttributeSet attrs)
    {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,R.styleable.RecordTimerView, 0, 0);
        circleRadius = typedArray.getDimension(R.styleable.RecordTimerView_radius, 50);
        strokeWidth = typedArray.getDimension(R.styleable.RecordTimerView_strokeWidth,10);
        circleColor = typedArray.getColor(R.styleable.RecordTimerView_circleColor,0);
        ringColor = typedArray.getColor(R.styleable.RecordTimerView_ringColor,0);

        ringRadius = circleRadius + strokeWidth/2;
    }

    private void initVariable()
    {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(circleColor);
        circlePaint.setStyle(Paint.Style.FILL);

        ringPaint = new Paint();
        ringPaint.setAntiAlias(true);
        ringPaint.setColor(ringColor);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(strokeWidth);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setARGB(255, 255, 255, 255);
        textPaint.setTextSize(circleRadius / 2);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        txtHeight = (int) Math.ceil(fm.descent - fm.ascent);
    }

    protected void onDraw(Canvas canvas)
    {
        //draw on the central of the view
        xCenter = getWidth() / 2;
        yCenter = getHeight() / 2;

        canvas.drawCircle(xCenter, yCenter, circleRadius, circlePaint);

        if(progress > 0)
        {
            RectF oval = new RectF();
            oval.left = (xCenter - ringRadius);
            oval.top = (yCenter - ringRadius);
            oval.right = ringRadius * 2 + (xCenter - ringRadius);
            oval.bottom = ringRadius * 2 + (yCenter - ringRadius);
            canvas.drawArc(oval, -90, ((float)progress / totalProgress) * 360, false, ringPaint);
            String txt = progress + "s";
            txtWidth = textPaint.measureText(txt, 0, txt.length());
            canvas.drawText(txt, xCenter - txtWidth / 2, yCenter + txtHeight / 4, textPaint);
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        postInvalidate();
    }
}
