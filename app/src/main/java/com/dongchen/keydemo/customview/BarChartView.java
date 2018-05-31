package com.dongchen.keydemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 最强走势的条形图(直角)
 * 用刻度画、有动画效果、淘金路中用到
 */

public class BarChartView extends View {
//    private Context context;
//    private float screenDensity;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    private int mViewWidth; //控件宽度
    private int mViewHeight;    //控件高度

    private final int maxPoit = 200;//-10到10总共200份
    private float horizontalUnit;//水平方向的刻度单位
    private float verticalUnit;//垂直方向的刻度单位

    private Paint mFillPaint;
    private Paint mStrokePaint;
    private Paint mPathPaint;//直线
    //线
    private final int pathsNum = 6;//实线 + 虚线 的总数,决定 verticalUnitDisdance 、 verticalUnit
    private int verticalY = 1;
    private PathEffect mPathEffect;
    private Path[] paths;//底部1条实线,上面的都为虚线,  paths[0] 为实线

    /* 动画 */
    private final int duration = 600;//View高度绘制完所需的时间,单位:毫秒
    private final int durationFraction = 20;//每个隔多久重绘一次,单位:毫秒
    private float averageHeight;//每次绘制增加的高度,单位:px
    private float currentTop;//当前条形图的高度,单位:px
    private float topest;//所有条形图的最高高度值,单位:px

    private float disX;
    private final Map<Double, Integer> xyMap = new HashMap<Double, Integer>();
    private final static Object lock = new Object();

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float screenDensity = context.getResources().getDisplayMetrics().density;
        paddingLeft = (int) (20 * screenDensity);
        paddingTop = (int) (10 * screenDensity);
        paddingRight = (int) (10 * screenDensity);
        paddingBottom = (int) (20 * screenDensity);

        upVerUint(0);
        /*Paint*/
        //条形图 和 文字
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setTextSize(10 * screenDensity);
        mStrokePaint = new Paint();
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setColor(Color.WHITE);
        //线
        mPathPaint = new Paint();
        mPathPaint.setStyle(Paint.Style.STROKE);//注：必须设置，默认的是Fill 样式
        mPathPaint.setStrokeWidth(1);
        mPathPaint.setColor(Color.parseColor("#a6a6a6"));
        mPathEffect = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);//均匀虚线。arg1：数组，设置点的间距。 arg2：控制数组偏移量
        paths = new Path[pathsNum];
    }

    /**
     * 初始化 宽 高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        /* BarChartView 数据*/
//        mViewWidth = getMeasuredWidth();
//        mViewHeight = getMeasuredHeight();
        /* BarChartView 数据*/

        /*条形图数据*/
        //TODO mBarChartWidth <= 0 抛异常 or =0?
        //整个条形图宽高 (注:不含 padding)
        mViewWidth = w - paddingLeft - paddingRight;
        mViewHeight = h - paddingTop - paddingBottom;


        //水平方向的刻度单位
        horizontalUnit = mViewWidth / (maxPoit * 1.0f);
        //垂直方向的刻度单位
        verticalUnit = mViewHeight / ((pathsNum - 1) * 1.0f);

        /* 线 */
        float x = mViewWidth + paddingLeft;//一个固定的x坐标
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path();
            paths[i].moveTo(paddingLeft, paddingTop + verticalUnit * i);
            paths[i].lineTo(x, paddingTop + verticalUnit * i);
        }

        /* 动画 */
        averageHeight = mViewHeight / ((float) duration / (durationFraction));
//        CommonUtil.LogLa(2, "BarChartView---> mRectHeight 2 = " + mViewHeight + "  ((float) duration / durationFraction) = " + ((float) duration / durationFraction) + "  averageHeight = " + averageHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* 水平线、垂直方向的刻度*/
        float paddL = paddingLeft - mFillPaint.measureText("100") / 2.0f;
        mFillPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < pathsNum; i++) {
            //水平线
            if (pathsNum - 1 == i) {//底部水平实线
                mPathPaint.setPathEffect(null);
            } else {//水平虚线
                mPathPaint.setPathEffect(mPathEffect);
            }
            canvas.drawPath(paths[i], mPathPaint);
            if (pathsNum - 1 != i) {//底部水平实线"y = 0"不画
                mFillPaint.setColor(Color.parseColor("#a6a6a6"));//灰色
                canvas.drawText(String.valueOf(verticalY * (pathsNum - i - 1)),
                        paddL,
                        paddingTop + verticalUnit * i + mFillPaint.getTextSize() / 2, mFillPaint);
            }
        }
        //画负
        float textY = mViewHeight + paddingTop * 2;
        mFillPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 5; i > 0; i--) {
            canvas.drawText(String.valueOf(i * -2) + "%", textX(i * -2), textY, mFillPaint);
        }
        //画0
        mFillPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.valueOf(0) + "%", textX(0), textY, mFillPaint);
        //画正
        mFillPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 1; i < 6; i++) {
            canvas.drawText(formatNumWithSign(0, i * 2, false) + "%", textX(i * 2), textY, mFillPaint);
        }
        synchronized (lock) {
            float dx = disX == 0 ? textDisX(0.4f) : textDisX(disX);
            float left, top, right, bottom;
            currentTop -= averageHeight;//每次动画减一次动画执行一次的高度
            for (Map.Entry<Double, Integer> entry : xyMap.entrySet()) {
                left = textX(entry.getKey().floatValue());
                top = textY(entry.getValue()) + paddingTop;
                if (currentTop > top) top = currentTop;
                right = left + dx;
                bottom = mViewHeight + paddingTop;
                if (entry.getKey() < 0) {
                    mFillPaint.setColor(Color.parseColor("#14df74"));//绿色
                } else if (entry.getKey() == 0) {
//                    mFillPaint.setColor(Color.parseColor("#a6a6a6"));// mBarCount 为奇数时中间那条显示 灰色0
                } else {
                    left = left - dx;
                    right = right - dx;
                    mFillPaint.setColor(Color.parseColor("#ff623d"));//红色
                }
                canvas.drawRect(left, top, right, bottom, mFillPaint);
                canvas.drawRect(left, top, right, bottom, mStrokePaint);//画外框
            }
        }

        //动画,循环绘制
        if (currentTop > textY((int) topest) + paddingTop) postInvalidateDelayed(durationFraction);
    }

    public void setDateAndInvalidate(Map<Double, Integer> map, float disX) {
        if (map == null || map.size() == 0) return;
        synchronized (lock) {
            this.disX = disX;
            int maxY = pathsNum - 1;
            for (Integer y : map.values()) {
                maxY = Math.max(maxY, y);
            }
            upVerUint(maxY);
            xyMap.clear();
            xyMap.putAll(map);
        }

        for (Map.Entry<Double, Integer> entry : xyMap.entrySet()) {
            if (topest < entry.getValue()) topest = entry.getValue();
        }
        currentTop = paddingTop + mViewHeight;//初始化矩形的当前高度,动画用到

        invalidate();
    }

    private float textDisX(float v) {
        return v * horizontalUnit * 10;
    }

    private float textX(float v) {
        return (v + 10) * horizontalUnit * 10 + paddingLeft;
    }

    private float textY(int y) {
        return mViewHeight - y / (verticalY * 1.0f) * verticalUnit;
    }

    private void upVerUint(int maxY) {
        if (maxY == 0) {
            verticalY = 1;
            return;
        }
        if (maxY % (pathsNum - 1) == 0) verticalY = maxY / (pathsNum - 1);
        else verticalY = maxY / (pathsNum - 1) + 1;
    }

    private static String formatNumWithSign(int d, double val, boolean isSign) {
        Formatter f = null;
        try {
            f = new Formatter();
            if (isSign) return f.format("%+1." + d + "f", val).toString().trim();
            else return f.format("%1." + d + "f", val).toString().trim();
        } catch (Exception e) {
            return "--";
        } finally {
            if (f != null) f.close();
        }
    }
}
