package com.example.applicationlock.lock.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.AttributeSet;

import java.util.ArrayList;

public class LockEditText extends AppCompatEditText {
    private static final String TAG = "LockEditText";
    private String mText;
    private int mMaxLength;
    private Paint mTextPaint;
    private Paint mDefaultPaint;
    private float radius;
    private ArrayList<Point> mPointList;
    private int mColor;
    private int strokeWidth;
    private OnTextChangedListener mOnTextChangedListener;

    public interface OnTextChangedListener {
        void onTextChanged(String text);
    }

    public void setOnTextChangedListener(OnTextChangedListener listener) {
        this.mOnTextChangedListener = listener;
    }

    public LockEditText(Context context) {
        super(context);
        init();
    }

    public LockEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LockEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTextColor(Color.TRANSPARENT);
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        mText = "";
        mMaxLength = 6;
        mTextPaint = new Paint();
        mDefaultPaint = new Paint();
        radius = 15;
        strokeWidth = 5;
        mPointList = new ArrayList<Point>();
        mColor = Color.BLACK;
        setLongClickable(false);
        setTextIsSelectable(false);
        setCursorVisible(false);
        setBackgroundDrawable(null);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int height = getHeight();
        int width = getWidth();
        mPointList.clear();

        //add the point
        float zeta = (float) width / (3 * mMaxLength);
        float pointWidth = (mMaxLength - 1) * zeta;
        float firstPointX = (width - pointWidth) / 2;

        for (int i = 0; i < mMaxLength; i++) {
            Point point = new Point(i, i * zeta + firstPointX, height / 2);
            point.setLine(0);
            point.setColumn(i);
            mPointList.add(point);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDefaultPaint.setColor(mColor);
        mDefaultPaint.setStyle(Paint.Style.STROKE);
        mDefaultPaint.setStrokeWidth(strokeWidth);

        mTextPaint.setColor(mColor);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(18);

        for (int i = 0; i < mMaxLength; i++) {
            if (mText.length() > i) {
                canvas.drawCircle(mPointList.get(i).getX(), mPointList.get(i).getY(),
                        radius, mTextPaint);
            } else {
                canvas.drawCircle(mPointList.get(i).getX(), mPointList.get(i).getY(),
                        radius, mDefaultPaint);
            }
        }
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (text.toString().length() <= mMaxLength) {
            mText = text.toString();
        }
        if (mOnTextChangedListener != null) {
            mOnTextChangedListener.onTextChanged(mText);
        }
    }

    public void clearText() {
        mText = "";
        setText(mText);
        setSelection(getText().toString().length());
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setColor(int color) {
        this.mColor = color;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
}
