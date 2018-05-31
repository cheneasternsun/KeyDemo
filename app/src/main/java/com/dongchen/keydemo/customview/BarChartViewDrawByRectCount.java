package com.dongchen.keydemo.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 最强走势的条形图(直角)
 * 据条形图总数量画、有动画效果
 */

public class BarChartViewDrawByRectCount extends View {
    private Context context;

    /*BarChartView 数据*/
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    private int mViewWidth; //控件宽度
    private int mViewHeight;    //控件高度

    /*条形图数据*/
    private final float RATE = (float) 0.5;  //条形图总共占据的空间比例
    private int mBarCount;  //条形图数量
    private float mRectWidth;   //条形图宽度
    private int mRectHeight;    //条形图高度
    private float offset;   //条形图间的间距
//    private int minHeightDip = 20;//条形图的最小高度

    /*Paint*/
    //条形图 和 文字
    private Paint mFillPaint;
    //线
    private Paint mPathPaint;//直线
    private PathEffect mPathEffect;
    private int pathsNum;//实线 + 虚线 的总数,决定 verticalUnitDisdance 、 verticalUnit
    private Path[] paths;//底部1条实线,上面的都为虚线,  paths[0] 为实线
    //水平、垂直方向刻度
    private float verticalUnitDisdance;//垂直方向方向上的每单位的实际距离
    private int verticalUnit;//垂直方向的刻度单位
    private int horizontalUnit;//水平方向的刻度单位
//    private int textStartNum;//水平方向最左边刻度值

    /* 外部传过来的数据 */
//    private float[] data;//外部传过来的数据
    private int[] rectHeightValues;//条形图的高度值,单位: verticalUnit
    private float[] tops;//条形图的高度值,单位:px
    private float topest;//所有条形图的最高高度值,单位:px

    /* 动画 */
    private final int duration = 1000;//View高度绘制完所需的时间,单位:毫秒
    private final int durationFraction = 20;//每个隔多久重绘一次,单位:毫秒
    private float averageHeight;//每次绘制增加的高度,单位:px
    private float currentTop;//当前条形图的高度,单位:px


    public BarChartViewDrawByRectCount(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        /*BarChartView 数据*/
        paddingLeft = (int) (20 * context.getResources().getDisplayMetrics().density);
        paddingTop = (int) (10 * context.getResources().getDisplayMetrics().density);
        paddingRight = (int) (10 * context.getResources().getDisplayMetrics().density);
        paddingBottom = (int) (20 * context.getResources().getDisplayMetrics().density);

        /*条形图数据*/
        pathsNum = 6;
        mBarCount = 19;
        horizontalUnit = 1;//默认
//        textStartNum = -(mBarCount / 2);

        /*Paint*/
        //条形图 和 文字
        mFillPaint = new Paint();
        mFillPaint.setAntiAlias(true);
        mFillPaint.setTextSize(12 * context.getResources().getDisplayMetrics().density);
        //线
        mPathPaint = new Paint();
        mPathPaint.setStyle(Paint.Style.STROKE);//注：必须设置，默认的是Fill 样式
        mPathPaint.setStrokeWidth(2);
        mPathPaint.setColor(Color.parseColor("#a6a6a6"));
        mPathEffect = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);//均匀虚线。arg1：数组，设置点的间距。 arg2：控制数组偏移量
        paths = new Path[pathsNum];

        /* 动画 */
    }

//    /**
//     * 格式化小数,4舍5入
//     *
//     * @param i
//     * @param value
//     * @return
//     */
//    public static String formatNumber(final int i, final float value) {
//        try {
//            if ("".equals(value) || "nan".equals(String.valueOf(value).toLowerCase())) return "0";
//            BigDecimal bd = new BigDecimal(Float.toString(value));
//            bd = bd.setScale(i, BigDecimal.ROUND_HALF_UP);
//            return bd.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "0";
//        }
//    }

    /**
     * 初始化 宽 高
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        /* BarChartView 数据*/
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();

        /*条形图数据*/
        //TODO mBarChartWidth <= 0 抛异常 or =0?
        //整个条形图宽高 (注:不含 padding)
        int mBarChartWidth = mViewWidth - paddingLeft - paddingRight;
        int mBarChartHeigth = mViewHeight - paddingTop - paddingBottom;
        //单个条形图宽高
        mRectWidth = ((mBarChartWidth * RATE) / (float) mBarCount);
        mRectHeight = mBarChartHeigth;
        //条形图间的间距
        offset = (mBarChartWidth * (1 - RATE)) / (mBarCount - 1);
        //动画
        averageHeight = mRectHeight / ((float) duration / durationFraction);
        Log.i("BarChartView", "BarChartView---> mRectHeight = " +mRectHeight +"  ((float) duration / durationFraction) = " + ((float) duration / durationFraction) + "  averageHeight = " + averageHeight);

        /* 线 */
        float x = mViewWidth - paddingRight;//一个固定的x坐标
        verticalUnitDisdance = ((float) mBarChartHeigth) / ((float) (pathsNum - 1));//保留小数
        verticalUnit = 1;//默认
        for (int i = 0; i < paths.length; i++) {
            paths[i] = new Path();
            paths[i].moveTo(paddingLeft, paddingTop + verticalUnitDisdance * i);
            paths[i].lineTo(x, paddingTop + verticalUnitDisdance * i);
        }

//        setData(new float[]{-11, -10, -9.1f, -8.9f, -1.1f, -0.51f, 0, 0.49f, 0.51f, 15});
//        setData(new float[]{-11, -10, -9.1f, -1.1f});
    }

    /**
     * 设置条形图高度的数据
     *
     * @param data
     */
    public void setData(double[] data) {
        if (null == data) return;
//        this.data = data;
        verticalUnit = 1;
        verticalUnit = data.length / (pathsNum - 1);
        if (data.length % (pathsNum - 1) > 0) verticalUnit++;
        /* 处理数据 */
        //初始化
        if (null != rectHeightValues) rectHeightValues = null;
        rectHeightValues = new int[mBarCount];//每次都要重新创建,因为必须把值清零,否则会数值累积
        if (null == tops || tops.length != mBarCount) {
            tops = new float[mBarCount];
        }
        //设置
        for (double aData : data) {
//            if (aData < 0) {
//                if (aData <= -(mBarCount / 2)) rectHeightValues[0]++;//小于水平方向最左边刻度值时
//                else rectHeightValues[(int) (aData - 0.5f) + (mBarCount / 2)]++;//四舍五入
//            } else {
//                if (aData >= (mBarCount / 2)) rectHeightValues[rectHeightValues.length - 1]++;
//                else rectHeightValues[(int) (aData + 0.5f) + (mBarCount / 2)]++;//四舍五入
//            }

            if (aData <= -(mBarCount / 2)) rectHeightValues[0]++;//小于等于水平方向最左边刻度值时
            else if (aData >= (mBarCount / 2))
                rectHeightValues[rectHeightValues.length - 1]++;//大于等于水平方向最右边刻度值时
            else rectHeightValues[(int) (Math.round(aData) + (mBarCount / 2))]++;//四舍五入
        }
        for (int i = 0; i < rectHeightValues.length; i++) {
            tops[i] = paddingTop + (mRectHeight - verticalUnitDisdance * (rectHeightValues[i] / (float) verticalUnit));
        }
        topest = tops[0];
        for (float t : tops) {
            if (t < topest) topest = t;
        }

        currentTop = paddingTop + mRectHeight;//初始化矩形的当前高度,动画用到

        Log.i("BarChartView", "BarChartView--->data.length = " + data.length + " verticalUnit = " + verticalUnit
                + " mRectHeight = " + mRectHeight + " verticalUnitDisdance = " + verticalUnitDisdance);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /* 水平线、垂直方向的刻度*/
        for (int i = 0; i < pathsNum; i++) {
            //水平线
            if (pathsNum - 1 == i) {//底部水平实线
                mPathPaint.setPathEffect(null);
            } else {//水平虚线
                mPathPaint.setPathEffect(mPathEffect);
            }
            canvas.drawPath(paths[i], mPathPaint);

            //垂直方向的刻度
            if (pathsNum - 1 != i) {//底部水平实线"y = 0"不画
                mFillPaint.setColor(Color.parseColor("#a6a6a6"));//灰色
                canvas.drawText(String.valueOf(verticalUnit * (pathsNum - i - 1)),
                        paddingLeft - mFillPaint.measureText("100") / (float) 2
                                - mFillPaint.measureText(String.valueOf(verticalUnit * (pathsNum - i - 1))) / (float) 2,
                        paddingTop + verticalUnitDisdance * i + mFillPaint.getTextSize() / 2, mFillPaint);
            }
        }

        /* 条形图和水平方向的刻度 */
        float left, top, right, bottom;
        float textX, textY;//文字左下角的坐标
        int textStartNum = -(mBarCount / 2);//(textStartNum * horizontalUnit) 结果是水平方向最左边刻度值

        currentTop -= averageHeight;//每次动画减一次动画执行一次的高度
        Log.i("BarChartView", "BarChartView--->averageHeight = " + averageHeight);
        for (int i = 0; i < mBarCount; i++) {
            if (0 == i) {//使第一个条形图的前面没有 offset 空间
                left = paddingLeft;
            } else {
                left = (paddingLeft + mRectWidth) + (i - 1) * (offset + mRectWidth) + offset;
            }

            //条形图
            if (null != rectHeightValues && null != tops && tops.length == mBarCount &&
                    rectHeightValues.length == mBarCount) {
                if (mBarCount / 2 == i && 1 == mBarCount % 2)
                    mFillPaint.setColor(Color.parseColor("#a6a6a6"));// mBarCount 为奇数时中间那条显示 灰色0
                else if (i < mBarCount / 2) mFillPaint.setColor(Color.parseColor("#14df74"));//绿色
                else mFillPaint.setColor(Color.parseColor("#ff623d"));//红色

                if (currentTop > tops[i]) top = currentTop;
                else top = tops[i];//超过该条形图的实际高度后则保持该条形图的最高高度不变

//                top = paddingTop + (mRectHeight - verticalUnitDisdance * (rectHeightValues[i] / (float) verticalUnit));
//                top = paddingTop + verticalUnitDisdance * (pathsNum - 1)
//                        - verticalUnitDisdance * (rectHeightValues[i] / (float) verticalUnit);
                Log.i("BarChartView", "BarChartView--->rectHeightValues[" + i + "] = " + rectHeightValues[i]
                        + " verticalUnit = " + verticalUnit
                        + " (rectHeightValues[i] / verticalUnit) = " + (float) rectHeightValues[i] / verticalUnit
                        + " ");

                right = left + mRectWidth;
                bottom = mViewHeight - paddingBottom;
//                if (bottom - top < minHeightDip)
//                    top = bottom - minHeightDip * context.getResources().getDisplayMetrics().density;//值为 0 时条形图最小高度

                canvas.drawRect(left, top, right, bottom, mFillPaint);
            }

            //水平方向的刻度
            textX = left + mRectWidth / 2 - mFillPaint.measureText("9%") / 2;//默认
            textY = mViewHeight - paddingBottom / 3;
            if (mBarCount / 2 == i && 1 == mBarCount % 2) {//中间 && mBarCount为奇数。
                textX = left + mRectWidth / 2 - mFillPaint.measureText("0") / 2;
                mFillPaint.setColor(Color.parseColor("#a6a6a6"));//灰色
                canvas.drawText("0", textX, textY, mFillPaint);
            } else if (0 == i % 2) {//只显示奇数刻度, 如 -5%  -3%  -1%  1%  3%  5%
                if (i < (mBarCount / 2 + mBarCount % 2)) {//左半边
                    textX = left + mRectWidth / 2 - mFillPaint.measureText("-9%") / 2;
                    mFillPaint.setColor(Color.parseColor("#14df74"));//绿色
                } else {//右半边
                    mFillPaint.setColor(Color.parseColor("#ff623d"));//红色
                }
                canvas.drawText(textStartNum * horizontalUnit + "%", textX, textY, mFillPaint);
                textStartNum += 2;
            }

        }

        //动画,循环绘制
        if (currentTop > topest) postInvalidateDelayed(durationFraction);
    }

}
