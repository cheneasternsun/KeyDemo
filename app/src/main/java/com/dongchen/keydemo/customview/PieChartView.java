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
    private Context context;

    //PieCharView
    private int mWidth;
    private int mHeigth;
    //弧、圆
    public float halfStrokeWidth;//StrokeWidth 的一半
    public float mCircleRadius;//圆半径
    private float mCircleX;    //圆心横坐标值
    private float mCircleY;    //圆心纵坐标值
    private RectF mArcRectF;    //注：存放矩形四条边的四个参数
    //碎片
    private int piecesNum;//饼状图碎片总数
    private int notEmptyPiecesNum;//饼状图碎片值大于0的碎片数量
    private String[] piecesColor;//各个碎片对应的颜色, 元素值如 "#55667788"
    private double sum;//piecesValue 的和
//    private float[] piecesValue;//各个碎片对应的值
//    private double[] piecePercent;//各个碎片的百分比
    private float[] sweepAngles;

    //Paint
    private Paint mPaint;
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
    private final int durationFract = 10;
    private final float animationA = 360 / ((float) duration / durationFract);//每次增加的角度
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
        mPaint = new Paint();
//        mPaint.setStyle(paintStytle);
        mPaint.setAntiAlias(true);
//        mPaint.setColor(Color.parseColor("#a6a6a6"));
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
//        mCircleX = mWidth / 2;
//        mCircleY = mHeigth / 2;
//        int min = Math.min(mWidth, mHeigth) / 2;
//
//        if (Paint.Style.FILL == paintStytle) {//实心圆
//            mCircleRadius = min;
//        } else if (Paint.Style.STROKE == paintStytle && min > mStrokeWidth){//弧
//            mCircleRadius = min - mStrokeWidth/2;//注:要减去 Stroke 宽度
//        } else {
//            throw new ArithmeticException("mStrokeWidth 必须小于图宽度的一半, 即 mStrokeWidth 必须小于 Math.min(mWidth, mHeigth) / 2"
//                    + "\n当前 " + " mStrokeWidth = " + mStrokeWidth + " Math.min(mWidth, mHeigth) / 2 = " + Math.min(mWidth, mHeigth) / 2);
//        }
//        mArcRectF = new RectF(mCircleX - mCircleRadius, mCircleY - mCircleRadius, mCircleX + mCircleRadius, mCircleY + mCircleRadius);
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
        mCircleX = mWidth / 2;
        mCircleY = mHeigth / 2;
        resetCircleParams();
    }

    /**
     * 因为 mStrokeWidth 的改变需要重置 circle 的相关参数,如 半径
     */
    private void resetCircleParams() {
        halfStrokeWidth = mStrokeWidth / 2.0f;//重置
        int min = Math.min(mWidth, mHeigth) / 2;
        if (Paint.Style.FILL == paintStytle) {//实心圆
            mCircleRadius = min;
        } else if (Paint.Style.STROKE == paintStytle && min > halfStrokeWidth) {//弧
            mCircleRadius = min - halfStrokeWidth;//注:要减去 Stroke 宽度的一半
        } else {
            throw new ArithmeticException("mStrokeWidth/2 必须小于图宽度的一半, 即 mStrokeWidth/2 必须小于 Math.min(mWidth, mHeigth) / 2"
                    + "\n当前 " + " mStrokeWidth/2 = " + halfStrokeWidth + " Math.min(mWidth, mHeigth) / 2 = " + Math.min(mWidth, mHeigth) / 2);
        }
        mArcRectF = new RectF(mCircleX - mCircleRadius, mCircleY - mCircleRadius, mCircleX + mCircleRadius, mCircleY + mCircleRadius);

//        Log.i("PieChartView", "PieChartView--->mCircleRadius = " + mCircleRadius);
    }

    /**
     * 设置 Paint 属性
     *
     * @param paintStytle
     * @param strokeWidthDip 单位是 dip
     * @param useCenter
     */
    public void setPaintParams(Paint.Style paintStytle, int strokeWidthDip, boolean useCenter) {
        this.paintStytle = paintStytle;
        this.mStrokeWidth = (int) (strokeWidthDip * context.getResources().getDisplayMetrics().density);
        this.useCenter = useCenter;

//        requestLayout();//不能是 invalidate() 因为要重新测量长度并减去 mStrokeWidth,得出半径长度。 注意:只有宽高发生变化才会触发该方法
        resetCircleParams();
        allHasGoneA = 0;//重置
    }

    /**
     * 初始化碎片的各个数据
     *
     * @param piecesNum   碎片总数 > 0
     * @param piecesColor 各个碎片对应的颜色, 元素值如 "#55667788"
     * @param piecesValue 各个碎片对应的值
     */
    public void setPiecesParams(int piecesNum, String[] piecesColor, double[] piecesValue) {
        if (piecesNum <= 0)
            throw new ArithmeticException("碎片必须大于 0");

        this.piecesNum = piecesNum;
        updatePiecesColor(piecesColor);
        updatePiecesValue(piecesValue);//这个方法要最后调用,因为有 invalidate()
    }

    /**
     * 更新碎片对应的颜色。注意:单独实用时 piecesColor 的长度必须与原有碎片数量一致
     *
     * @param piecesColor 碎片对应的颜色
     */
    public void updatePiecesColor(String[] piecesColor) {
        //piecesNum != piecesColor.length 抛出异常
        if (piecesNum != piecesColor.length)
            throw new ArrayIndexOutOfBoundsException("碎片颜色数量必须与碎片数量相等, piecesNum 必须等于 piecesColor.length。"
                    + "\n当前 " + " piecesColor.length = " + piecesColor.length + " piecesNum = " + piecesNum);

        this.piecesColor = piecesColor;
        allHasGoneA = isAnimation ? 0 : 360;;//重置, allHasGoneA = 0 添加动画, allHasGoneA = 360 无动画
    }

    /**
     * 更新碎片对应的值。注意:单独实用时 piecesValue 的长度必须与原有碎片数量一致
     *
     * @param piecesValue 碎片对应的值
     */
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
            if (null != sweepAngles) sweepAngles = null;
            sweepAngles = new float[piecesValue.length];
            float sumAngle = 0;
            for (int i = 0, j = piecesValue.length; i < j; i++) {
                if (i == j - 1) {
                    sweepAngles[i] = 360 - sumAngle;//剩余
                } else {
                    sweepAngles[i] = (float) (360 * piecesValue[i] / sum);
                    sumAngle += sweepAngles[i];
                }
//                Log.i("PieChartView", "PieChartView--->sweepAngles[" + i + "] = " + sweepAngles[i]);
            }

            //设置最小角度
            notEmptyPiecesNum = 0;//重置
            for (int i = 0; i < sweepAngles.length; i++) {
                if (sweepAngles[i] < MIN_A && 0 != sweepAngles[i]) {
                    //最大角度
                    int index = 0;
                    float maxA = sweepAngles[index];
                    for (int n = 1; n < sweepAngles.length; n++) {
                        if (maxA < sweepAngles[n]) {
                            maxA = sweepAngles[n];
                            index = n;
                        }
                    }

                    float temp = sweepAngles[i];
                    sweepAngles[i] = MIN_A;//默认最小角度
                    sweepAngles[index] -= (MIN_A - temp);
                }

                //统计值大于0 的碎片的总数量
                if (0 != sweepAngles[i]) {
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
        mPaint.setStyle(paintStytle);
        if (Paint.Style.STROKE == paintStytle) {
            if (mStrokeWidth <= 0)
                throw new ArithmeticException("Stroke 宽度不能小于 0");
            mPaint.setStrokeWidth(mStrokeWidth);
        }

        //一次动画
        if (piecesNum > 0 && sum > 0 && null != sweepAngles && sweepAngles.length == piecesNum) {
            //画弧或实心圆
            float startAngle = 270;//注：从最右边中点开始计算多少角度后开始画弧，方向顺时钟。
            float sumA = 0;//第一个碎片到当前已绘制到的碎片,这些碎片所需绘制的角度的和
            int currentFract = 0;//当前绘制到哪个碎片
            allHasGoneA += animationA;

            for (int i = 0; i < piecesNum; i++) {
                mPaint.setColor(Color.parseColor(piecesColor[i]));
                //绘制第一个碎片时 startAngle 必须为零
                if (0 != i) {
                    startAngle += sweepAngles[i - 1];
                }
                if (startAngle >= 360) {
                    startAngle -= 360;
                }

                sumA += sweepAngles[i];
                currentFract = i;
                if (allHasGoneA > sumA) {
                    //画完整碎片
                    canvas.drawArc(mArcRectF, startAngle, sweepAngles[i], useCenter, mPaint);
//                    currentFract = i;//避免每次增加的角度大于某个碎片的角度导致没画这个碎片前面的线。但是有可能导致碎片太小被完全覆盖。
                } else {
                    //画部分碎片
//                    currentFract = i;
                    float currentFractionHasGoneA = sweepAngles[i] - (sumA - allHasGoneA);//当前碎片已绘制完成的角度
                    canvas.drawArc(mArcRectF, startAngle, currentFractionHasGoneA, useCenter, mPaint);
                    break;
                }
            }

            //画线(弧或实心圆的)
            float startA = 270;
            if (notEmptyPiecesNum > 1) {//值大于0 的碎片的总数量大于1 才能画线
                //画最后一个碎片后面的线
                if (allHasGoneA >= 360 && 0 != sweepAngles[piecesNum - 1]) {//最后一个碎片 && 线的碎片大于0
//            if (allHasGoneA >= 360 && sweepAngles[piecesNum - 1] > MIN_A && sweepAngles[0] > MIN_A) {//线的左右碎片必须大于 MIN_A 才能画
                    float a = startA + 360;
                    if (Paint.Style.STROKE == paintStytle) {
                        canvas.drawLine(getX(mCircleX, mCircleRadius - halfStrokeWidth, a),//绘制碎片后面的"线"
                                getY(mCircleY, mCircleRadius - halfStrokeWidth, a),
                                getX(mCircleX, mCircleRadius + halfStrokeWidth, a),
                                getY(mCircleY, mCircleRadius + halfStrokeWidth, a), mLinePaint);
                    } else {
                        canvas.drawLine(mCircleX,//绘制碎片后面的"线"
                                mCircleY,
                                getX(mCircleX, mCircleRadius, a),
                                getY(mCircleY, mCircleRadius, a), mLinePaint);
                    }
                }
                //画当前碎片前面的所有线
                for (int j = 0; j < currentFract; j++) {
                    startA += sweepAngles[j];
                    if (0 != sweepAngles[j]) {//线的碎片大于0才能画
//                if (sweepAngles[j] > MIN_A && sweepAngles[j + 1] > MIN_A) {//线的左右碎片必须大于 MIN_A 才能画
                        if (Paint.Style.STROKE == paintStytle) {
                            canvas.drawLine(getX(mCircleX, mCircleRadius - halfStrokeWidth, startA),//绘制碎片后面的"线"
                                    getY(mCircleY, mCircleRadius - halfStrokeWidth, startA),
                                    getX(mCircleX, mCircleRadius + halfStrokeWidth, startA),
                                    getY(mCircleY, mCircleRadius + halfStrokeWidth, startA), mLinePaint);
                        } else {
                            canvas.drawLine(mCircleX,//绘制碎片后面的"线"
                                    mCircleY,
                                    getX(mCircleX, mCircleRadius, startA),
                                    getY(mCircleY, mCircleRadius, startA), mLinePaint);
                        }
                    }
                }
            }

            //重绘:进入下一个动画
            if (allHasGoneA < 360) {
                postInvalidateDelayed(durationFract);
            }

        } else {//默认
            mPaint.setColor(Color.parseColor("#a6a6a6"));//默认
//            canvas.drawCircle(mCircleX, mCircleY, mCircleRadius, mPaint);
            canvas.drawArc(mArcRectF, 0, 360, false, mPaint);
        }
    }

//    private float getX(float mCircleX, float r, float a) {
//        return (float) (mCircleX + (r * Math.cos((360 - a) * Math.PI / 180)));
//    }
//
//    private float getY(float mCircleY, float r, float a) {
//        return (float) (mCircleY - r * Math.sin((360 - a) * Math.PI / 180));
//    }

    private float getX(float x0, float r, float a) {
        return x0 + r * (float) Math.cos(a * Math.PI / 180);
    }

    private float getY(float y0, float r, float a) {
        return y0 + r * (float) Math.sin(a * Math.PI / 180);
    }
}
