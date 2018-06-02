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

                float temp = mSweepAngles[i];
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
     * @param measuredWidth  测量得到的宽
     * @param measuredHeight 测量得到的高
     */
    private void measureCircleParams(int measuredWidth, int measuredHeight) {
        //处理padding
        int contentWidth;
        int contentHeight;
        int l = getPaddingLeft();
        int t = getPaddingTop();
        int r = getPaddingRight();
        int b = getPaddingBottom();
        contentWidth = measuredWidth - l - r;//View内容的宽度
        contentHeight = measuredHeight - t - b;

        Log.d("PieChartView", "measuredWidth = " + measuredWidth + " measuredHeight = " + measuredHeight + " PaddingTop = " + t
        + " PaddingBottom = " + b);

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

//    /**
//     * 设置 Paint 属性
//     *
//     * @param paintStytle
//     * @param strokeWidthDip 单位是 dip
//     * @param useCenter
//     */
//    public void setPaintParams(Paint.Style paintStytle, int strokeWidthDip, boolean useCenter) {
//        this.paintStytle = paintStytle;
//        this.mStrokeWidth = (int) (strokeWidthDip * context.getResources().getDisplayMetrics().density);
//        this.useCenter = useCenter;
//
////        requestLayout();//不能是 invalidate() 因为要重新测量长度并减去 mStrokeWidth,得出半径长度。 注意:只有宽高发生变化才会触发该方法
//        measureCircleParams();
//        allHasGoneA = 0;//重置
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //一次动画
        if (null != mSweepAngles && mSweepAngles.length != 0) {
            int animPosition = getCurrentAnimPosition();
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
     * @param canvas
     * @param animPosition 本次动画终点所在碎片的位置（即下标）
     */
    private void drawCircle(Canvas canvas, int animPosition) {
        mAnimHasGoneAngle += ANIM_ANGLE;
        float sumA = 0;//累计：已画过的完整碎片的角度和
        float startAngle = 270;//注：从最右边中点开始计算多少角度后开始画弧，方向顺时钟。
        boolean useCenter = (Paint.Style.FILL == mCirclePaint.getStyle());
        for (int i = 0; i < animPosition; i++) {
            //画完整碎片
            mCirclePaint.setColor(Color.parseColor(mPiecesColor[i]));
            canvas.drawArc(mArcRectF, startAngle, mSweepAngles[i], useCenter, mCirclePaint);
            startAngle += mSweepAngles[i];//画完后才加
            sumA += mSweepAngles[i];
        }
        //画部分碎片
        if (mAnimHasGoneAngle > 360) mAnimHasGoneAngle = 360;
        float lastA = mAnimHasGoneAngle - sumA;//当前碎片已绘制完成的角度
        mCirclePaint.setColor(Color.parseColor(mPiecesColor[animPosition]));
        canvas.drawArc(mArcRectF, startAngle, lastA, useCenter, mCirclePaint);
    }

    /**
     * 画线(可以是弧或实心圆的)
     * @param canvas
     * @param animPosition 本次动画终点所在碎片的位置（即下标）
     */
    private void drawLine(Canvas canvas, int animPosition) {
        float startA = 270;
        if (mSweepAngles.length > 1) {//碎片的总数量大于1 才画线
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
                if (0 != mSweepAngles[j]) {//线的碎片大于0才能画
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
 * 1、据程序功能确定入口参数。
 * drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)、
 * drawLine(float startX, float startY, float stopX, float stopY, Paint paint)、
 * 具有动画效果。
 * 确定程序入口参数：PaintStyle、StrokeWith、isAnimation。
 * 2、明确不同线程中的所有流程。
 * UI线程：
 * 流程1：a、创建PieChartView：代码（含入参） b、设置setPiecesParams(double[] piecesValue, String[] piecesColor)；
 * 流程2：a、创建PieChartView：xml（含入参）  b、设置setPiecesParams(double[] piecesValue, String[] piecesColor)。
 *
 * View测量、布局、绘制的线程：
 * 流程1：
 * a、重写onMeasure（处理wrap_content、padding）
 * b、重写onSizeChanged（获取Circle的mRectF、mRadius、mPivotX、mPivotY）
 * c、重写onDraw（据mAnimHasGoneAngle 多次重复 drawCircle、drawLine）
 *
 * 二、关键
 * 1、确定程序的所有入口，每个入口是一个流程图的开始。
 * 2、据程序功能确定程序入口参数（对象的入参）。
 * 3、合法性判断：于入口处控制，避免后面出现太多判断，简化逻辑。
 * 4、流程图的每个步骤是一个方法，据该步骤功能确定输入（方法的入参）、输出。
 * 5、区分不同线程各自的流程。
 *
 * 总之，根据对应的功能确定各自的入参，然后据所有入参确定全局变量即Feild（为使程序简单，Feild应尽可能少）。
 */


/*public class PieChartView extends View {
    private Context context;

    //PieCharView
    private int mWidth;
    private int mHeigth;
    //弧、圆
    public float mHalfStrokeWidth;//StrokeWidth 的一半
    public float mRadius;//圆半径
    private float mPivotX;    //圆心横坐标值
    private float mPivotY;    //圆心纵坐标值
    private RectF mArcRectF;    //注：存放矩形四条边的四个参数
    //碎片
    private int piecesNum;//饼状图碎片总数
    private int notEmptyPiecesNum;//饼状图碎片值大于0的碎片数量
    private String[] mPiecesColor;//各个碎片对应的颜色, 元素值如 "#55667788"
    private double sum;//piecesValue 的和
//    private float[] piecesValue;//各个碎片对应的值
//    private double[] piecePercent;//各个碎片的百分比
    private float[] mSweepAngles;

    //Paint
    private Paint mCirclePaint;
    private Paint.Style paintStytle;
    private boolean useCenter;//默认 false
    private boolean isAnimation;//是否有动画效果
    private final int STROKE_WIDTH_DIP = 10;//默认 Paint 的Stroke宽度
    private int mStrokeWidth;//Paint 的Stroke宽度
    private Paint mLinePaint;

    //动画
    private float allHasGoneA;//总共已绘制角度
//    private float currentFractionHasGoneA;//当前碎片已绘制到的角度
//    private int currentFract;//当前绘制到哪个碎片
    private final int duration = 1000;
    private final int ANIM_TIME = 10;
    private final float ANIM_ANGLE = 360 / ((float) duration / ANIM_TIME);//每次增加的角度
    private final int MIN_A = 3;//允许画线的碎片的最小角度

    public PieChartView(Context context) {
        super(context);

        init(context);
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //创建初始化
        init(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PieChartView);
        mStrokeWidth = (int) (array.getInt(R.styleable.PieChartView_strokeWidthDip, STROKE_WIDTH_DIP)
                * context.getResources().getDisplayMetrics().density);
        this.useCenter = array.getBoolean(R.styleable.PieChartView_useCenter, false);
        //是 Fill 还是 Stroke 类型的 Paint。默认 Stroke
        boolean isFillPaint = array.getBoolean(R.styleable.PieChartView_isFillPaint, false);
        isAnimation = array.getBoolean(R.styleable.PieChartView_isAnimation, false);
        if (isFillPaint) {
            paintStytle = Paint.Style.FILL;
        }
        array.recycle();
    }

    private void init(Context context) {
        this.context = context;
        paintStytle = Paint.Style.STROKE;
        mStrokeWidth = (int) (STROKE_WIDTH_DIP * context.getResources().getDisplayMetrics().density);
        mCirclePaint = new Paint();
//        mCirclePaint.setStyle(paintStytle);
        mCirclePaint.setAntiAlias(true);
//        mCirclePaint.setColor(Color.parseColor("#a6a6a6"));
        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
//        mLinePaint.setColor(Color.parseColor("#a6a6a6"));
        mLinePaint.setColor(Color.WHITE);// 线的颜色
        mLinePaint.setStrokeWidth(3f);
    }

    //    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        mWidth = measure(widthMeasureSpec);
//        mHeigth = measure(heightMeasureSpec);
//        mPivotX = mWidth / 2;
//        mPivotY = mHeigth / 2;
//        int min = Math.min(mWidth, mHeigth) / 2;
//
//        if (Paint.Style.FILL == paintStytle) {//实心圆
//            mRadius = min;
//        } else if (Paint.Style.STROKE == paintStytle && min > mStrokeWidth){//弧
//            mRadius = min - mStrokeWidth/2;//注:要减去 Stroke 宽度
//        } else {
//            throw new ArithmeticException("mStrokeWidth 必须小于图宽度的一半, 即 mStrokeWidth 必须小于 Math.min(mWidth, mHeigth) / 2"
//                    + "\n当前 " + " mStrokeWidth = " + mStrokeWidth + " Math.min(mWidth, mHeigth) / 2 = " + Math.min(mWidth, mHeigth) / 2);
//        }
//        mArcRectF = new RectF(mPivotX - mRadius, mPivotY - mRadius, mPivotX + mRadius, mPivotY + mRadius);
//
//        setMeasuredDimension(mWidth, mHeigth);
//    }
//
//    private int measure(int measureSpec) {
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//        int result = 200;
//        if (specMode == MeasureSpec.EXACTLY) {// 精确尺寸
//            result = specSize;
//        } else if (specMode == MeasureSpec.AT_MOST) {// 最大可获得的空间
//            result = specSize;
//        }
//        return result;
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeigth = h;
        mPivotX = mWidth / 2;
        mPivotY = mHeigth / 2;
        resetCircleParams();
    }

    *//**
 * 因为 mStrokeWidth 的改变需要重置 circle 的相关参数,如 半径
 * <p/>
 * 设置 Paint 属性
 *
 * @param paintStytle
 * @param strokeWidthDip 单位是 dip
 * @param useCenter
 * <p/>
 * 初始化碎片的各个数据
 * @param piecesNum   碎片总数 > 0
 * @param mPiecesColor 各个碎片对应的颜色, 元素值如 "#55667788"
 * @param piecesValue 各个碎片对应的值
 * <p/>
 * 更新碎片对应的颜色。注意:单独实用时 mPiecesColor 的长度必须与原有碎片数量一致
 * @param mPiecesColor 碎片对应的颜色
 * <p/>
 * 更新碎片对应的值。注意:单独实用时 piecesValue 的长度必须与原有碎片数量一致
 * @param piecesValue 碎片对应的值
 * <p/>
 * 设置 Paint 属性
 * @param paintStytle
 * @param strokeWidthDip 单位是 dip
 * @param useCenter
 * <p/>
 * 初始化碎片的各个数据
 * @param piecesNum   碎片总数 > 0
 * @param mPiecesColor 各个碎片对应的颜色, 元素值如 "#55667788"
 * @param piecesValue 各个碎片对应的值
 * <p/>
 * 更新碎片对应的颜色。注意:单独实用时 mPiecesColor 的长度必须与原有碎片数量一致
 * @param mPiecesColor 碎片对应的颜色
 * <p/>
 * 更新碎片对应的值。注意:单独实用时 piecesValue 的长度必须与原有碎片数量一致
 * @param piecesValue 碎片对应的值
 *//*
    private void resetCircleParams() {
        mHalfStrokeWidth = mStrokeWidth / 2.0f;//重置
        int min = Math.min(mWidth, mHeigth) / 2;
        if (Paint.Style.FILL == paintStytle) {//实心圆
            mRadius = min;
        } else if (Paint.Style.STROKE == paintStytle && min > mHalfStrokeWidth) {//弧
            mRadius = min - mHalfStrokeWidth;//注:要减去 Stroke 宽度的一半
        } else {
            throw new ArithmeticException("mStrokeWidth/2 必须小于图宽度的一半, 即 mStrokeWidth/2 必须小于 Math.min(mWidth, mHeigth) / 2"
                    + "\n当前 " + " mStrokeWidth/2 = " + mHalfStrokeWidth + " Math.min(mWidth, mHeigth) / 2 = " + Math.min(mWidth, mHeigth) / 2);
        }
        mArcRectF = new RectF(mPivotX - mRadius, mPivotY - mRadius, mPivotX + mRadius, mPivotY + mRadius);

//        Log.i("PieChartView", "PieChartView--->mRadius = " + mRadius);
    }

    *//**
 * 设置 Paint 属性
 *
 * @param paintStytle
 * @param strokeWidthDip 单位是 dip
 * @param useCenter
 *//*
    public void setPaintParams(Paint.Style paintStytle, int strokeWidthDip, boolean useCenter) {
        this.paintStytle = paintStytle;
        this.mStrokeWidth = (int) (strokeWidthDip * context.getResources().getDisplayMetrics().density);
        this.useCenter = useCenter;

//        requestLayout();//不能是 invalidate() 因为要重新测量长度并减去 mStrokeWidth,得出半径长度。 注意:只有宽高发生变化才会触发该方法
        resetCircleParams();
        allHasGoneA = 0;//重置
    }

    *//**
 * 初始化碎片的各个数据
 *
 * @param piecesNum   碎片总数 > 0
 * @param mPiecesColor 各个碎片对应的颜色, 元素值如 "#55667788"
 * @param piecesValue 各个碎片对应的值
 *//*
    public void setPiecesParams(int piecesNum, String[] mPiecesColor, double[] piecesValue) {
        if (piecesNum <= 0)
            throw new ArithmeticException("碎片必须大于 0");

        this.piecesNum = piecesNum;
        updatePiecesColor(mPiecesColor);
        updatePiecesValue(piecesValue);//这个方法要最后调用,因为有 invalidate()
    }

    *//**
 * 更新碎片对应的颜色。注意:单独实用时 mPiecesColor 的长度必须与原有碎片数量一致
 *
 * @param mPiecesColor 碎片对应的颜色
 *//*
    public void updatePiecesColor(String[] mPiecesColor) {
        //piecesNum != mPiecesColor.length 抛出异常
        if (piecesNum != mPiecesColor.length)
            throw new ArrayIndexOutOfBoundsException("碎片颜色数量必须与碎片数量相等, piecesNum 必须等于 mPiecesColor.length。"
                    + "\n当前 " + " mPiecesColor.length = " + mPiecesColor.length + " piecesNum = " + piecesNum);

        this.mPiecesColor = mPiecesColor;
        allHasGoneA = isAnimation ? 0 : 360;;//重置, allHasGoneA = 0 添加动画, allHasGoneA = 360 无动画
    }

    *//**
 * 更新碎片对应的值。注意:单独实用时 piecesValue 的长度必须与原有碎片数量一致
 *
 * @param piecesValue 碎片对应的值
 *//*
    public void updatePiecesValue(double[] piecesValue) {
        //piecesNum != piecesValue.length 抛出异常
        if (piecesNum != piecesValue.length)
            throw new ArrayIndexOutOfBoundsException("碎片值的数量必须与碎片数量相等,即 piecesNum 必须等于 piecesValue.length。"
                    + "\n当前 " + " piecesValue.length = " + piecesValue.length + " piecesNum = " + piecesNum);

        sum = 0;//重置
        for (double v : piecesValue) {
            //v < 0 时抛出异常
            if (v < 0) throw new ArithmeticException("碎片值不能小于 0, 即 piecesValue 的元素都必须大于 0。");

            sum += v;
        }

        if (sum > 0) {
            //按比例设置角度
            if (null != mSweepAngles) mSweepAngles = null;
            mSweepAngles = new float[piecesValue.length];
            float sumAngle = 0;
            for (int i = 0, j = piecesValue.length; i < j; i++) {
                if (i == j - 1) {
                    mSweepAngles[i] = 360 - sumAngle;//剩余
                } else {
                    mSweepAngles[i] = (float) (360 * piecesValue[i] / sum);
                    sumAngle += mSweepAngles[i];
                }
//                Log.i("PieChartView", "PieChartView--->mSweepAngles[" + i + "] = " + mSweepAngles[i]);
            }

            //设置最小角度
            notEmptyPiecesNum = 0;//重置
            for (int i = 0; i < mSweepAngles.length; i++) {
                if (mSweepAngles[i] < MIN_A && 0 != mSweepAngles[i]) {
                    //最大角度
                    int index = 0;
                    float maxA = mSweepAngles[index];
                    for (int n = 1; n < mSweepAngles.length; n++) {
                        if (maxA < mSweepAngles[n]) {
                            maxA = mSweepAngles[n];
                            index = n;
                        }
                    }

                    float temp = mSweepAngles[i];
                    mSweepAngles[i] = MIN_A;//默认最小角度
                    mSweepAngles[index] -= (MIN_A - temp);
                }

                //统计值大于0 的碎片的总数量
                if (0 != mSweepAngles[i]) {
                    notEmptyPiecesNum++;
                }
            }

        }

        allHasGoneA = isAnimation ? 0 : 360;;//重置, allHasGoneA = 0 添加动画, allHasGoneA = 360 无动画
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //设置 Paint
        mCirclePaint.setStyle(paintStytle);
        if (Paint.Style.STROKE == paintStytle) {
            if (mStrokeWidth <= 0)
                throw new ArithmeticException("Stroke 宽度不能小于 0");
            mCirclePaint.setStrokeWidth(mStrokeWidth);
        }

        //一次动画
        if (piecesNum > 0 && sum > 0 && null != mSweepAngles && mSweepAngles.length == piecesNum) {
            //画弧或实心圆
            float startAngle = 270;//注：从最右边中点开始计算多少角度后开始画弧，方向顺时钟。
            float sumA = 0;//第一个碎片到当前已绘制到的碎片,这些碎片所需绘制的角度的和
            int currentFract = 0;//当前绘制到哪个碎片
            allHasGoneA += ANIM_ANGLE;

            for (int i = 0; i < piecesNum; i++) {
                mCirclePaint.setColor(Color.parseColor(mPiecesColor[i]));
                //绘制第一个碎片时 startAngle 必须为零
                if (0 != i) {
                    startAngle += mSweepAngles[i - 1];
                }
                if (startAngle >= 360) {
                    startAngle -= 360;
                }

                sumA += mSweepAngles[i];
                currentFract = i;
                if (allHasGoneA > sumA) {
                    //画完整碎片
                    canvas.drawArc(mArcRectF, startAngle, mSweepAngles[i], useCenter, mCirclePaint);
//                    currentFract = i;//避免每次增加的角度大于某个碎片的角度导致没画这个碎片前面的线。但是有可能导致碎片太小被完全覆盖。
                } else {
                    //画部分碎片
//                    currentFract = i;
                    float currentFractionHasGoneA = mSweepAngles[i] - (sumA - allHasGoneA);//当前碎片已绘制完成的角度
                    canvas.drawArc(mArcRectF, startAngle, currentFractionHasGoneA, useCenter, mCirclePaint);
                    break;
                }
            }

            //画线(弧或实心圆的)
            float startA = 270;
            if (notEmptyPiecesNum > 1) {//值大于0 的碎片的总数量大于1 才能画线
                //画最后一个碎片后面的线
                if (allHasGoneA >= 360 && 0 != mSweepAngles[piecesNum - 1]) {//最后一个碎片 && 线的碎片大于0
//            if (allHasGoneA >= 360 && mSweepAngles[piecesNum - 1] > MIN_A && mSweepAngles[0] > MIN_A) {//线的左右碎片必须大于 MIN_A 才能画
                    float a = startA + 360;
                    if (Paint.Style.STROKE == paintStytle) {
                        canvas.drawLine(getX(mPivotX, mRadius - mHalfStrokeWidth, a),//绘制碎片后面的"线"
                                getY(mPivotY, mRadius - mHalfStrokeWidth, a),
                                getX(mPivotX, mRadius + mHalfStrokeWidth, a),
                                getY(mPivotY, mRadius + mHalfStrokeWidth, a), mLinePaint);
                    } else {
                        canvas.drawLine(mPivotX,//绘制碎片后面的"线"
                                mPivotY,
                                getX(mPivotX, mRadius, a),
                                getY(mPivotY, mRadius, a), mLinePaint);
                    }
                }
                //画当前碎片前面的所有线
                for (int j = 0; j < currentFract; j++) {
                    startA += mSweepAngles[j];
                    if (0 != mSweepAngles[j]) {//线的碎片大于0才能画
//                if (mSweepAngles[j] > MIN_A && mSweepAngles[j + 1] > MIN_A) {//线的左右碎片必须大于 MIN_A 才能画
                        if (Paint.Style.STROKE == paintStytle) {
                            canvas.drawLine(getX(mPivotX, mRadius - mHalfStrokeWidth, startA),//绘制碎片后面的"线"
                                    getY(mPivotY, mRadius - mHalfStrokeWidth, startA),
                                    getX(mPivotX, mRadius + mHalfStrokeWidth, startA),
                                    getY(mPivotY, mRadius + mHalfStrokeWidth, startA), mLinePaint);
                        } else {
                            canvas.drawLine(mPivotX,//绘制碎片后面的"线"
                                    mPivotY,
                                    getX(mPivotX, mRadius, startA),
                                    getY(mPivotY, mRadius, startA), mLinePaint);
                        }
                    }
                }
            }

            //重绘:进入下一个动画
            if (allHasGoneA < 360) {
                postInvalidateDelayed(ANIM_TIME);
            }

        } else {//默认
            mCirclePaint.setColor(Color.parseColor("#a6a6a6"));//默认
//            canvas.drawCircle(mPivotX, mPivotY, mRadius, mCirclePaint);
            canvas.drawArc(mArcRectF, 0, 360, false, mCirclePaint);
        }
    }

//    private float getX(float mPivotX, float r, float a) {
//        return (float) (mPivotX + (r * Math.cos((360 - a) * Math.PI / 180)));
//    }
//
//    private float getY(float mPivotY, float r, float a) {
//        return (float) (mPivotY - r * Math.sin((360 - a) * Math.PI / 180));
//    }

    private float getX(float x0, float r, float a) {
        return x0 + r * (float) Math.cos(a * Math.PI / 180);
    }

    private float getY(float y0, float r, float a) {
        return y0 + r * (float) Math.sin(a * Math.PI / 180);
    }
}*/


