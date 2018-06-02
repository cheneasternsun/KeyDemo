package com.dongchen.keydemo.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.dongchen.keydemo.R;


/**
 * 设置饼状图(实心和空心),可自定义饼状图碎片数量、对应的颜色、碎片的值。
 * 可设置有无动画效果
 * 碎片不为0时,有默认最小角度 MIN_A
 */

public class PieChartView extends View {
    //弧、圆的Paint
    private Paint mCirclePaint;
    private final int STROKE_WIDTH_DIP = 20;//默认 Paint 的Stroke宽度
    private float mHalfStrokeWidth;//StrokeWidth 的一半
    //线的paint
    private Paint mLinePaint;
    //弧、圆
    private RectF mArcRectF;    //注：存放矩形四条边的四个参数
    private float mRadius;//圆半径
    private float mPivotX;    //圆心横坐标值
    private float mPivotY;    //圆心纵坐标值
    //弧、圆、线动画
    private float mAnimHasGoneAngle;//动画已走过的角度
    private final int ANIM_TIME = 10;//10毫秒一次动画
    private final float ANIM_ANGLE = 360 / ((float) 1000 / ANIM_TIME);//每次增加的角度
    //碎片
    private String[] mPiecesColor;//各个碎片对应的颜色, 元素值如 "#55667788"
    private float[] mSweepAngles;

    /**
     * 用于代码创建View，Paint使用默认style
     *
     * @param context
     */
    public PieChartView(Context context) {
        this(context, null);//attrs为null，使用Paint的默认style
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //入参
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PieChartView);
        float strokeWidth = (int) (array.getFloat(R.styleable.PieChartView_strokeWidthDip, STROKE_WIDTH_DIP)
                * context.getResources().getDisplayMetrics().density);
        mHalfStrokeWidth = strokeWidth / 2;
        boolean isFillPaint = array.getBoolean(R.styleable.PieChartView_isFillPaint, false);//是 Fill 还是 Stroke 类型的 Paint。默认 Stroke
        boolean isAnimation = array.getBoolean(R.styleable.PieChartView_isAnimation, false);
        array.recycle();
        initAccording2Input(strokeWidth, isFillPaint, isAnimation);
    }

    public PieChartView(Context context, float strokeWidth, boolean isFillPaint, boolean isAnimation) {
        super(context);
        initAccording2Input(strokeWidth, isFillPaint, isAnimation);
    }

    private void initAccording2Input(float strokeWidth, boolean isFillPaint, boolean isAnimation) {
        //入参处理
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        if (isFillPaint) {
            mCirclePaint.setStyle(Paint.Style.FILL);
        } else {
            if (strokeWidth <= 0) throw new CustomViewException("StrokeWidth值必须大于0");
            mCirclePaint.setStyle(Paint.Style.STROKE);
            mCirclePaint.setStrokeWidth(strokeWidth);
        }
        mAnimHasGoneAngle = isAnimation ? 0 : 360;//有动画效果需从0度开始画。

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
//        mLinePaint.setColor(Color.parseColor("#a6a6a6"));
        mLinePaint.setColor(Color.WHITE);// 线的颜色
        mLinePaint.setStrokeWidth(3f);
    }

    /**
     * 设置各个碎片的数据
     *
     * @param piecesValue 各个碎片对应的值
     * @param piecesColor 各个碎片对应的颜色, 元素值如 "#55667788"
     */
    public void setPiecesParams(double[] piecesValue, String[] piecesColor) {
        //合法性判断：于入口处控制，避免后面出现太多判断
        if (piecesColor.length != piecesValue.length || 0 == piecesValue.length)
            throw new CustomViewException("碎片个数等于颜色个数,且个数大于0");
        for (double aPiecesValue : piecesValue) {
            if (aPiecesValue <= 0) throw new CustomViewException("碎片值必须大于0");
        }

        this.mPiecesColor = piecesColor;
        values2Angles(piecesValue);

        invalidate();
    }

    /**
     * 碎片值转换成相应角度值
     *
     * @param piecesValue 碎片值
     */
    private void values2Angles(double[] piecesValue) {
        double sum = 0;
        for (double aPiecesValue : piecesValue) {
            sum += aPiecesValue;
        }
        //按比例设置角度
        if (null != mSweepAngles) mSweepAngles = null;
        mSweepAngles = new float[piecesValue.length];
        float sumAngle = 0;
        for (int i = 0, j = piecesValue.length; i < j; i++) {
            if (i == j - 1) {
                mSweepAngles[i] = 360 - sumAngle;//剩余
            } else {
                mSweepAngles[i] = (float) (360 * piecesValue[i] / sum);
//                mSweepAngles[i] = (float) Math.floor((360 * piecesValue[i] / sum));//向下取整
                sumAngle += mSweepAngles[i];
            }
        }

        //设置最小角度
        final int MIN_A = 3;//碎片的最小角度
        float temp;
        for (int i = 0; i < mSweepAngles.length; i++) {
            if (mSweepAngles[i] < MIN_A) {
                //最大角度
                int index = 0;
                float maxA = mSweepAngles[index];
                for (int n = 1; n < mSweepAngles.length; n++) {
                    if (maxA < mSweepAngles[n]) {
                        maxA = mSweepAngles[n];
                        index = n;
                    }
                }

                temp = mSweepAngles[i];
                mSweepAngles[i] = MIN_A;//默认最小角度
                mSweepAngles[index] -= (MIN_A - temp);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = measure(widthMeasureSpec);
        int height = measure(heightMeasureSpec);

        Log.d("PieChartView", "width = " + width + " height = " + height);

        setMeasuredDimension(width, height);
    }

    /**
     * 处理wrap_content的情况
     *
     * @param measureSpec
     * @return
     */
    private int measure(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 200;

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(result, specSize);
                break;
            case MeasureSpec.EXACTLY:
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            default:
                result = specSize;
                break;
        }

        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        measureCircleParams(w, h);
    }

    /**
     * 据View的测量得到的宽高（包括padding）来设置Circle的参数
     *
     * @param measuredWidth  测量得到的宽
     * @param measuredHeight 测量得到的高
     */
    private void measureCircleParams(final int measuredWidth, final int measuredHeight) {
        //处理padding
        final int contentWidth;
        final int contentHeight;
        final int l = getPaddingLeft();
        final int t = getPaddingTop();
        final int r = getPaddingRight();
        final int b = getPaddingBottom();
        contentWidth = measuredWidth - l - r;//View内容的宽度
        contentHeight = measuredHeight - t - b;

//        mPivotX = Math.round(contentWidth / 2 + l);
//        mPivotY = Math.round(contentHeight / 2 + t);
        mPivotX = contentWidth / 2 + l;
        mPivotY = contentHeight / 2 + t;
        int min = Math.min(contentWidth, contentHeight) / 2;
        if (Paint.Style.FILL == mCirclePaint.getStyle()) {//实心圆
            if (min < 0) throw new CustomViewException("View的padding设置得太大或View的宽高设置得太小导致图形无法显示。");
            mRadius = min;
        } else if (Paint.Style.STROKE == mCirclePaint.getStyle() && min > mHalfStrokeWidth) {//弧
            mRadius = min - mHalfStrokeWidth;//注:要减去 Stroke 宽度的一半
        } else {
            throw new ArithmeticException("画笔的StrokeWidth必须小于View的宽和高，可加大View的宽高或减小padding值");
        }
        mArcRectF = new RectF(mPivotX - mRadius, mPivotY - mRadius, mPivotX + mRadius, mPivotY + mRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //一次动画
        if (null != mSweepAngles && mSweepAngles.length != 0) {
            mAnimHasGoneAngle += ANIM_ANGLE;//该操作必须在本次动画的最前面
            final int animPosition = getCurrentAnimPosition();//必须在mAnimHasGoneAngle += ANIM_ANGLE;后面，否则最后一碎片的角度若是小于ANIM_ANGLE则可能不会被画出
            drawCircle(canvas, animPosition);
            drawLine(canvas, animPosition);//在画circle后才画，这样线才能覆盖在circle上

            //重绘:进入下一个动画
            if (mAnimHasGoneAngle < 360) {
                postInvalidateDelayed(ANIM_TIME);
            }
        } else {//默认灰色环形图
            mCirclePaint.setColor(Color.parseColor("#a6a6a6"));//默认
//            canvas.drawCircle(mPivotX, mPivotY, mRadius, mCirclePaint);//此方法即可画出单一颜色的环形图或饼状图，但无法画出含多种颜色的图。
            canvas.drawArc(mArcRectF, 0, 360, false, mCirclePaint);
        }
    }

    /**
     * 获取本次动画终点所在碎片的位置（即下标）
     *
     * @return 动画终点所在在碎片的位置
     */
    private int getCurrentAnimPosition() {
        float sum = 0;

        for (int i = 0; i < mSweepAngles.length; i++) {
            sum += mSweepAngles[i];
            if (mAnimHasGoneAngle <= sum) {
                return i;
            }
        }

        return mSweepAngles.length - 1;
    }

    /**
     * 画弧或实心圆
     *
     * @param canvas
     * @param animPosition 本次动画终点所在碎片的位置（即下标）
     */
    private void drawCircle(Canvas canvas, final int animPosition) {
        float sumA = 0;//累计：已画过的完整碎片的角度和
        float startAngle = 270;//注：从最右边中点开始计算多少角度后开始画弧，方向顺时钟。
        final boolean useCenter = (Paint.Style.FILL == mCirclePaint.getStyle());
        for (int i = 0; i < animPosition; i++) {
            //画完整碎片
            mCirclePaint.setColor(Color.parseColor(mPiecesColor[i]));
            canvas.drawArc(mArcRectF, startAngle, mSweepAngles[i], useCenter, mCirclePaint);
            startAngle += mSweepAngles[i];//画完后才加
            sumA += mSweepAngles[i];
        }
        //画部分碎片
        if (mAnimHasGoneAngle > 360) mAnimHasGoneAngle = 360;
        final float lastA = mAnimHasGoneAngle - sumA;//当前碎片已绘制完成的角度
        mCirclePaint.setColor(Color.parseColor(mPiecesColor[animPosition]));
        canvas.drawArc(mArcRectF, startAngle, lastA, useCenter, mCirclePaint);
    }

    /**
     * 画线(可以是弧或实心圆的)
     *
     * @param canvas
     * @param animPosition 本次动画终点所在碎片的位置（即下标）
     */
    private void drawLine(Canvas canvas, final int animPosition) {
        if (mSweepAngles.length < 2) return;//碎片的总数量大于1 才画线

        float startA = 270;
        //画最后一个碎片后面的线
        if (mAnimHasGoneAngle >= 360) {//最后一个碎片 && 线的碎片大于0
            if (Paint.Style.STROKE == mCirclePaint.getStyle()) {
                canvas.drawLine(
                        getX(mPivotX, mRadius - mHalfStrokeWidth, startA),//绘制碎片后面的"线"
                        getY(mPivotY, mRadius - mHalfStrokeWidth, startA),
                        getX(mPivotX, mRadius + mHalfStrokeWidth, startA),
                        getY(mPivotY, mRadius + mHalfStrokeWidth, startA), mLinePaint);
            } else {
                canvas.drawLine(
                        mPivotX,//绘制碎片后面的"线"
                        mPivotY,
                        getX(mPivotX, mRadius, startA),
                        getY(mPivotY, mRadius, startA), mLinePaint);
            }
        }
        //画当前碎片前面的所有线
        for (int j = 0; j < animPosition; j++) {
            startA += mSweepAngles[j];
            if (Paint.Style.STROKE == mCirclePaint.getStyle()) {//画环形图的线
                canvas.drawLine(
                        getX(mPivotX, mRadius - mHalfStrokeWidth, startA),
                        getY(mPivotY, mRadius - mHalfStrokeWidth, startA),
                        getX(mPivotX, mRadius + mHalfStrokeWidth, startA),
                        getY(mPivotY, mRadius + mHalfStrokeWidth, startA), mLinePaint);
            } else {
                canvas.drawLine(//画饼状图的线
                        mPivotX,
                        mPivotY,
                        getX(mPivotX, mRadius, startA),
                        getY(mPivotY, mRadius, startA), mLinePaint);
            }
        }
    }

    private float getX(float mCircleX, float r, float angle) {
        return (float) (mCircleX + (r * Math.cos((360 - angle) * Math.PI / 180)));
    }

    private float getY(float mCircleY, float r, float angle) {
        return (float) (mCircleY - r * Math.sin((360 - angle) * Math.PI / 180));
    }
}
/**
 * 结果：
 *
 * 总结：
 * 一、步骤
 * 1、据程序功能确定Feild。
 * drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)：
 *      ==》mCirclePaint（持有PaintStyle、StrokeWidth、useCenter）、mRectF、mSweepAngles。
 * drawLine(float startX, float startY, float stopX, float stopY, Paint paint)：
 *      ==》mLinePaint、mPivotX、mPivotY、mRadius、mSweepAngles。
 * 具有动画效果：
 *      ==》mAnimHasGoneAngle、ANIM_ANGLE、ANIM_TIME、duration(可去掉)。
 *
 * 2、据需求确定所有程序入口及其相应参数（“类入参”），每个入口是一个流程图的“开始”：
 * 入口1：（xml）创建PieChartView：PieChartView(Context context, AttributeSet attrs)
 * 入口2：（代码）创建PieChartView：PieChartView(Context context, float strokeWidth, boolean isFillPaint, boolean isAnimation)
 *
 * 3、明确不同线程中的所有流程。
 * UI线程：
 * 流程1：
 * a、入口1
 * b、设置setPiecesParams(double[] piecesValue, String[] piecesColor)；
 * 流程2：
 * a、入口2
 * b、设置setPiecesParams(double[] piecesValue, String[] piecesColor)。
 *
 * View测量、布局、绘制所在的线程：
 * 流程1：
 * a、重写onMeasure（处理wrap_content、padding）
 * b、重写onSizeChanged（获取Circle大小、位置等数据：mRectF、mRadius、mPivotX、mPivotY）
 * c、重写onDraw（据mAnimHasGoneAngle 多次重复 drawCircle、drawLine）
 *
 * 二、关键
 * 1、据整个“类”的功能确定Feild（为使程序简单，Feild应尽可能少）。
 * 2、功能确定入参：据需求确定所有程序入口及其相应参数（“类入参”），每个入口是一个流程图的开始。
 *    流程图的每个步骤是一个方法，据该步骤功能确定输入（方法的入参）、输出。
 * 3、合法性判断：于入口处控制，避免后面出现太多判断，简化逻辑。
 * 4、将入参及其同步Field的修改封装为方法：入参值的变化会必定改变某个Field，入参和该Field是“同步”的，故封装起来。
 * 5、区分不同线程各自的流程。
 *
 * 总之，据整个“类”的功能确定全局变量Feild，其次根据相应功能确定各自的入参，然后据入参确定修改“同步”Feild方法！
 * 每个功能又是一个小流程，找出这些小流程的公共部分写成一个方法（如：initAccording2Input()、getCurrentAnimPosition()、getX()）。
 */