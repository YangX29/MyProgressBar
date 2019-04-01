package com.yang.myprogressbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class MyProgress extends View {
    /**
     * 画笔
     */
    private Paint mPaint;
    /**
     * 默认进度值
     */
    private int mProgress = 0;
    /**
     * 最大值
     */
    private int max = 100;
    /**
     * 是否为温度
     */
    private boolean isTemperature = false;
    /**
     * 进度边距
     */
    private int mProgressPadding = dp2px(5);
    /**
     * 控件高度
     */
    private int mHeight = 0;

    /**
     * 字体大小
     */
    float mTextSize = 18f;

    /**
     * 是否显示文字
     */
    boolean showText = true;

    /**
     * 进度条是否为圆角
     */
    boolean isRadius = false;

    /**
     * 左边进度条颜色
     */
    int mProgressColor = Color.RED;
    /**
     * 文字颜色
     */
    int mTextColor = Color.RED;
    /**
     * 右边进度条颜色
     */
    int mBackgroundColor = Color.RED;
    /**
     * 左边进度条高度
     */
    int mProgressHeight = dp2px(10);
    /**
     * 右边进度条高度
     */
    int mBackgroundHeight = dp2px(10);

    /**
     * 是否为渐变
     */
    boolean isGradient  = false;

    /**
     * 渐变色的结束色
     * @param context
     */
    int endColor = Color.YELLOW;

    public MyProgress(Context context) {
        this(context, null);
    }

    public MyProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化
     */
    private void init(AttributeSet attrs) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);//抗锯齿
        mPaint.setColor(Color.RED);//画笔颜色
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MyProgress);
        //获取参数
        mProgressColor = ta.getColor(R.styleable.MyProgress_progress_color, mProgressColor);
        mTextColor = ta.getColor(R.styleable.MyProgress_text_color, mTextColor);
        mBackgroundColor = ta.getColor(R.styleable.MyProgress_background_color, mBackgroundColor);
        mProgressHeight = (int) ta.getDimension(R.styleable.MyProgress_progress_height, mProgressHeight);
        mBackgroundHeight = (int) ta.getDimension(R.styleable.MyProgress_background_height, mBackgroundHeight);
        mTextSize = ta.getDimension(R.styleable.MyProgress_text_size, mTextSize);
        mProgress = ta.getInt(R.styleable.MyProgress_progress, mProgress);
        max = ta.getInt(R.styleable.MyProgress_maxValue, max);
        isTemperature = ta.getBoolean(R.styleable.MyProgress_isTemperature, isTemperature);
        showText = ta.getBoolean(R.styleable.MyProgress_showText, showText);
        isRadius = ta.getBoolean(R.styleable.MyProgress_progress_radius, isRadius);
        isGradient = ta.getBoolean(R.styleable.MyProgress_isGradient, isGradient);
        endColor = ta.getColor(R.styleable.MyProgress_end_color, endColor);
        ta.recycle();
        mPaint.setTextSize(mTextSize);//设定文字大小，后续好测量文字高度
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//得到测量模式
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);//默认用户需要给出明确值，所以不判断模式
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthVal, height);//设置了测量值后，可以获取测量值。
        //mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    /**
     * 测量高度
     *
     * @param heightMeasureSpec
     * @return
     */
    private int measureHeight(int heightMeasureSpec) {
        int result;
        int mode = MeasureSpec.getMode(heightMeasureSpec);//得到测量模式
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (mode == MeasureSpec.EXACTLY) {//用户给了精确值
            result = size;
        } else { //MeasureSpec.UNSPECIFIED  MeasureSpec.AT_MOST 未指定明确参数
            int textHeight = (int) (mPaint.descent() - mPaint.ascent());//得到文字高度
            result = getPaddingTop() + getPaddingBottom() + Math.max(mHeight, textHeight);//高度等于进度条高度和文字高度中最高的为准，并且加上padding值
            if (mode == MeasureSpec.AT_MOST) {//给定了最大值
                result = Math.min(result, size);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取高度
        mHeight = getMeasuredHeight();
        //设置文本格式
        String str = mProgress + "%";
        //设置文本为温度
        if (isTemperature) str = mProgress+"℃";
        //获取文本宽度
        float textWidth = mPaint.measureText(str);
        //不显示文字，文本宽度为0
        if (!showText) textWidth = 0;

        //控件宽度-文本宽度-padding=进度条总宽度，即已加载部分长度
float width = getMeasuredWidth() - textWidth - getPaddingLeft() - getPaddingRight() - mProgressPadding * 2 ;
//当前进度应该绘制的结束位置
float currentProgress = getPaddingLeft() + width * mProgress / max;
        //设置进度条的颜色和高度
        mPaint.setColor(mProgressColor);
        mPaint.setStrokeWidth(mProgressHeight);

        //直线为圆角时的偏差值
        int r = 0;
        if (isRadius) {
            //设置直线末端为圆角
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            //圆角的宽度
            r = mProgressHeight/2;
        }

        //直线是否为渐变色
        if (isGradient){
            //设置线性渐变，开始位置和结束位置于进度条相同
            Shader shader = new LinearGradient(getPaddingLeft(), mHeight / 2, currentProgress, mHeight / 2,
                    mProgressColor, endColor, Shader.TileMode.REPEAT);
            mPaint.setShader(shader);
        }
        //画进度条，当存在圆角时，需要考虑圆角，在画直线的时候，圆角的长度是不考虑进去的，所以在开始坐标要加上r,结束坐标要减去r
        canvas.drawLine(getPaddingLeft()+r, mHeight / 2, currentProgress-r, mHeight / 2, mPaint);
        int y = (int) (-(mPaint.descent() + mPaint.ascent()) / 2);
//        mPaint.setColor(mTextColor);
//        canvas.drawText(str, currentProgress + mProgressPadding, mHeight / 2 + y, mPaint);
        //设置画未加载部分的颜色和宽度
        mPaint.setColor(mBackgroundColor);
        mPaint.setStrokeWidth(mBackgroundHeight);
        //清除渐变
        mPaint.setShader(null);
//        canvas.drawLine(currentProgress + textWidth + mProgressPadding * 2, mHeight / 2, getMeasuredWidth() - getPaddingRight(), mHeight / 2, mPaint);
        //画未加载部分
        canvas.drawLine(currentProgress , mHeight / 2, currentProgress+width*(max-mProgress)/max, mHeight / 2, mPaint);

        //设置文字颜色
        mPaint.setColor(mTextColor);
        //是否存在文字部分
        if (showText)
        canvas.drawText(str, getMeasuredWidth() - getPaddingRight()-textWidth, mHeight / 2 + y, mPaint);
    }

    public void setProgress(int mProgress){
        this.mProgress = mProgress;
        invalidate();
    }

    public int getProgress(){
        return mProgress;
    }

    private int dp2px(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    private int sp2px(int val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, val, getResources().getDisplayMetrics());
    }

}
