package com.example.applicationlock.lock.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class ImageLockView extends View {
    private static final String TAG = "ImageLockView";

    private ArrayList<Point> mPointList;
    private ArrayList<Point> mChoosePointList;
    private boolean isMatch;
    private float radius;
    private int chooseColor;
    private int errorColor;
    private int defaultColor;

    private Point eventPoint;
    private Paint mLinePaint;

    private int alpha;
    private int paintWidth;
    private boolean isTouch;
    private boolean enable;
    private OnGraphChangedListener mOnTextChangedListener;

    public interface OnGraphChangedListener{
        void onGraphFinish(String password);
    }

    public void setOnGraphChangedListener(OnGraphChangedListener listener) {
        this.mOnTextChangedListener = listener;
    }

    public ImageLockView(Context context) {
        super(context);
        init();
    }

    public ImageLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPointList = new ArrayList<Point>();
        mChoosePointList = new ArrayList<Point>();
        isMatch = true;
        radius = 60;
        mLinePaint = new Paint();
        eventPoint = new Point();
        defaultColor = Color.GRAY;
        chooseColor = Color.GREEN;
        errorColor = Color.RED;
        paintWidth = 20;
        alpha = 255;
        enable = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int height = getHeight();
        int width = getWidth();
        mPointList.clear();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int id = i * 3 + j;
                float x =  (float) (width / 6) * (2 * j + 1);
                float y = (float) (height / 6) * (2 * i + 1);
                Point point = new Point(id, x, y);
                point.setLine(i);
                point.setColumn(j);
                mPointList.add(point);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mPointList.size(); i++) {
            Point point = mPointList.get(i);
            point.setDefaultColor(defaultColor);
            point.setChooseColor(chooseColor);
            point.setErrorColor(errorColor);
            point.setAlpha(alpha);
            point.draw(canvas);
        }

        if (mChoosePointList.size() > 0) {
            drawTouchedLine(canvas);
        }
    }

    private void drawTouchedLine(Canvas canvas) {
        mLinePaint.setStrokeWidth(paintWidth);
        if (isMatch) {
            mLinePaint.setColor(chooseColor);
        } else {
            mLinePaint.setColor(errorColor);
        }
        mLinePaint.setAlpha(100);
        for (int i = 0; i < mChoosePointList.size() - 1; i++) {
            Point currentPoint = mChoosePointList.get(i);
            Point nextPoint = mChoosePointList.get(i + 1);
            canvas.drawLine(currentPoint.getX(), currentPoint.getY(),
                    nextPoint.getX(), nextPoint.getY(), mLinePaint);
        }

        if (isMatch && isTouch) {
            Point lastPoint = mChoosePointList.get(mChoosePointList.size() - 1);
            canvas.drawLine(lastPoint.getX(), lastPoint.getY(),
                    eventPoint.getX(), eventPoint.getY(), mLinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (enable) {
                    eventPoint.setLocation(event.getX(), event.getY());
                    isTouch = true;
                    pointTouchEvent(eventPoint);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (enable) {
                    isTouch = false;
                    if (mOnTextChangedListener != null) {
                        mOnTextChangedListener.onGraphFinish(getGraphicalPassword());
                    }
                    invalidate();
                }
                break;

            default:
                break;
        }
        return true;
    }


    public void pointTouchEvent(Point eventPoint) {
        for (int i=0; i < mPointList.size(); i++) {
            Point point = mPointList.get(i);
            if (Math.sqrt(Math.pow(eventPoint.getX() - point.getX(), 2)
                    + Math.pow(eventPoint.getY() - point.getY(), 2)) <= radius) {
                //The current touch point is within the radius of the previous circle,
                //indicating that we have selected the circle
                for (int j = 0; j < mChoosePointList.size(); j++) {
                    if (mChoosePointList.get(j).getId() == point.getId()) {
                        //this circle is added
                        return;
                    }
                }

                //Determine if there is a circle that needs to be added
                //between lastPoint and currentPoint
                if (mChoosePointList.size() > 0) {
                    Point lastPoint = mChoosePointList.get(mChoosePointList.size() - 1);
                    if (isAddPoint(point, lastPoint)) {
                        int id = (point.getId() + lastPoint.getId()) / 2;
                        mPointList.get(id).setTouch(true);
                        mChoosePointList.add(mPointList.get(id));
                    }
                }

                mPointList.get(i).setTouch(true);
                point.setTouch(true);
                mChoosePointList.add(point);
            }
        }
    }

    /**
     * Determine if there is a circle that needs to be added between lastPoint and currentPoint
     * @param currentPoint current Point
     * @param lastPoint last Point
     * @return if need add point return true
     */
    private boolean isAddPoint(Point currentPoint, Point lastPoint) {
        if (currentPoint.getLine() == lastPoint.getLine()
                && Math.abs(currentPoint.getColumn() - lastPoint.getColumn()) == 2) {
            return true;
        } else if (currentPoint.getColumn() == currentPoint.getColumn()
                && Math.abs(currentPoint.getLine() - lastPoint.getLine()) == 2) {
            return true;
        } else if (Math.abs(currentPoint.getLine() - lastPoint.getLine()) == 2
                && Math.abs(currentPoint.getColumn() - lastPoint.getColumn()) == 2) {
            return true;
        }

        return false;
    }

    public String getGraphicalPassword() {
        StringBuilder builder = new StringBuilder();
        for (Point point : mChoosePointList) {
            builder.append(point.getId());
        }
        return builder.toString();
    }

    public void resetGraphicalPassword() {
        mChoosePointList.clear();
        for (Point point : mPointList) {
            point.setTouch(false);
            point.setError(false);
        }

        isMatch = true;
        invalidate();
    }

    public void setMatch(boolean match) {
        isMatch = match;
        for (Point point : mChoosePointList) {
            point.setError(!isMatch);
        }
        invalidate();
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setChooseColor(int chooseColor) {
        this.chooseColor = chooseColor;
    }

    public void setErrorColor(int errorColor) {
        this.errorColor = errorColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setPaintWidth(int paintWidth) {
        this.paintWidth = paintWidth;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
