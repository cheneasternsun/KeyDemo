package com.dongchen.keydemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 最强走势用到的线性图
 */

public class LinearGraphView extends View {
    //    private Context context;
    private float screenDensity;

    /*LinearGraphView 数据*/
    private int mViewWidth;
    private int mViewHeight;
    private int paddingLeft = 20;
    private int paddingTop = 40;
    private int paddingRight = 20;
    private int paddingBottom = 40;

    /*可视图片的数据*/
    private int mRectWidth;
    private int mRectHeight;
    private int mTextWidth;//数字的长度

    /*出去数字外可视图片的数据*/
    private int mLinearWidth;
    private int mLinearHeight;
    private float unitH;//水平方向上的单位长度

    /*Paint*/
    //文字 和 实心圆
    private Paint mTextAndCirclePaint;
    private final float RADIUS = 5;
    //    private double mTextHeight;//字体高度
    //线
    private Paint mPathPaint;
    private PathEffect mPathEffect;//虚线的
    private Path mVerticalPath;//垂直线
    private Path mHorizontalPath;//水平线
    private Path mPath;//折线

    /* 外部传入的数据 */
    private double[] data;
    private Point[] points;//每个 data 数据在view中的相对位置
    private double max;//绝对值最大的 data

    public LinearGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        this.context = context;
        screenDensity = context.getResources().getDisplayMetrics().density;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        /*LinearGraphView 数据*/
        paddingLeft = (int) (20 * screenDensity);
        paddingTop = (int) (40 * screenDensity);
        paddingRight = (int) (20 * screenDensity);
        paddingBottom = (int) (40 * screenDensity);

        /*Paint*/
        //文字 和 实心圆
        mTextAndCirclePaint = new Paint();
        mTextAndCirclePaint.setTextSize(11 * screenDensity);//11dip
        //线
        mPathPaint = new Paint();
        mPathPaint.setStyle(Paint.Style.STROKE);//注：必须设置，默认的是Fill 样式
        mPathPaint.setStrokeWidth(2);
        mPathPaint.setColor(Color.parseColor("#a6a6a6"));
//        mPathPaint.setColor(Color.parseColor("#0000ff"));
        mPathEffect = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);//均匀虚线。arg1：数组，设置点的间距。 arg2：控制数组偏移量
        mVerticalPath = new Path();
        mHorizontalPath = new Path();
        mPath = new Path();
    }

    /**
     * 初始化 View 的数据
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mViewWidth = w;
        mViewHeight = h;

        //TODO mRectWidth <= 0 || mRectHeight <= 0 抛出异常 还是等于 0?
        mRectWidth = mViewWidth - paddingLeft - paddingRight;
        mRectHeight = mViewHeight - paddingTop - paddingBottom;
        mTextWidth = (int) mTextAndCirclePaint.measureText("-9.9%") + 2;//默认

        mLinearWidth = mRectWidth - mTextWidth;
        mLinearHeight = mRectHeight;

        //垂直线
        mVerticalPath.moveTo(paddingLeft + mTextWidth, paddingTop);
        mVerticalPath.lineTo(paddingLeft + mTextWidth, paddingTop + mLinearHeight);
        //水平线
        mHorizontalPath.moveTo(paddingLeft + mTextWidth, paddingTop + mLinearHeight / 2);
        mHorizontalPath.lineTo(paddingLeft + mTextWidth + mLinearWidth, paddingTop + mLinearHeight / 2);
    }

    /**
     * 设置每个点的数据
     *
     * @param data
     */
    public void setData(double[] data) {
        if (null == data || data.length == 0) return;

        //设置与数据点相关的数据
        this.data = data;
        if (null != points) points = null;
        points = new Point[data.length];
        unitH = mLinearWidth / (data.length + 1);
        for (double aData : data) {
            if (max < Math.abs(aData)) max = Math.abs(aData);
        }
        mTextWidth = (int) mTextAndCirclePaint.measureText("-" + max + "%");
        int centerY = paddingTop + mLinearHeight / 2;
        for (int i = 0, j = data.length; i < j; i++) {
            points[i] = new Point();
            points[i].set((int) (paddingLeft + mTextWidth + unitH * (i + 1)), (int) (centerY - mLinearHeight / 2 * data[i] / max));
            if (0 == i) {
                mPath.reset();
                mPath.moveTo(points[i].x, points[i].y);
            } else {
                mPath.lineTo(points[i].x, points[i].y);
            }
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.e("LinearGraphView", "LinearGraphView--->onDraw");

        if (null != data && null != points && data.length == points.length) {//有数据
            //折线
            mPathPaint.setPathEffect(null);
            canvas.drawPath(mPath, mPathPaint);

            //实心圆
            for (int i = 0, j = points.length; i < j; i++) {
                if (0 == data[i]) {
                    mTextAndCirclePaint.setColor(Color.parseColor("#a6a6a6"));//灰色
                } else if (data[i] > 0) {
                    mTextAndCirclePaint.setColor(Color.parseColor("#ff623d"));//红色
                } else {
                    mTextAndCirclePaint.setColor(Color.parseColor("#14df74"));//绿色
                }

                canvas.drawCircle(points[i].x, points[i].y, RADIUS, mTextAndCirclePaint);
            }

            //数字 TODO 垂直方向的坐标根据后台的数据修改??
            mTextAndCirclePaint.setColor(Color.parseColor("#ff623d"));//红色
            canvas.drawText(String.valueOf(max) + "%", paddingLeft, paddingTop + mTextAndCirclePaint.getTextSize() * 2 / 3, mTextAndCirclePaint);
            mTextAndCirclePaint.setColor(Color.parseColor("#a6a6a6"));//灰色
            canvas.drawText("0%", paddingLeft, paddingTop + mLinearHeight / 2 + mTextAndCirclePaint.getTextSize() / 2, mTextAndCirclePaint);
            mTextAndCirclePaint.setColor(Color.parseColor("#14df74"));//绿色
            canvas.drawText("-" + String.valueOf(max) + "%", paddingLeft, paddingTop + mLinearHeight, mTextAndCirclePaint);
        }

//        //数字 TODO 垂直方向的坐标根据后台的数据修改??
//        mTextAndCirclePaint.setColor(Color.parseColor("#ff623d"));//红色
//        canvas.drawText(String.valueOf(max) + "%", paddingLeft, paddingTop + mTextAndCirclePaint.getTextSize() * 2 / 3, mTextAndCirclePaint);
//        mTextAndCirclePaint.setColor(Color.parseColor("#a6a6a6"));//灰色
//        canvas.drawText("0%", paddingLeft, paddingTop + mLinearHeight / 2 + mTextAndCirclePaint.getTextSize() / 2, mTextAndCirclePaint);
//        mTextAndCirclePaint.setColor(Color.parseColor("#14df74"));//绿色
//        canvas.drawText("-" + String.valueOf(max) + "%", paddingLeft, paddingTop + mLinearHeight, mTextAndCirclePaint);

        //垂直线
        mPathPaint.setPathEffect(null);
        canvas.drawPath(mVerticalPath, mPathPaint);
        //水平虚线
        mPathPaint.setPathEffect(mPathEffect);
        canvas.drawPath(mHorizontalPath, mPathPaint);
    }
}
