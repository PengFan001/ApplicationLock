package com.example.applicationlock.lock.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Point {
    private int id;
    private float x;
    private float y;
    private float radius;
    private boolean isTouch;
    private boolean isError;
    private Paint mPaint;
    private int line;
    private int column;

    private int strokeWidth = 40;
    private int defaultColor = Color.GRAY;
    private int chooseColor = Color.GREEN;
    private int errorColor = Color.RED;
    private int alpha = 255;

    public Point() {

    }

    public Point(int id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.isError = false;
        this.isTouch = false;
        this.radius = 60;
        mPaint = new Paint();
    }

    public void draw(Canvas canvas) {
        if (isTouch) {
            mPaint.setColor(isError ? errorColor : chooseColor);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, radius, mPaint);
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
        } else {
            mPaint.setColor(defaultColor);
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(alpha);
        canvas.drawCircle(x, y, radius, mPaint);
    }

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isTouch() {
        return isTouch;
    }

    public void setTouch(boolean touch) {
        isTouch = touch;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setChooseColor(int chooseColor) {
        this.chooseColor = chooseColor;
    }

    public void setErrorColor(int errorColor) {
        this.errorColor = errorColor;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
}
