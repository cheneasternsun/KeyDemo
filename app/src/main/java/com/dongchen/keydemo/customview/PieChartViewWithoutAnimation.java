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
 * 有动画效果
 */

public class PieChartViewWithoutAnimation extends View {
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
    private String[] piecesColor;//各个碎片对应的颜色, 元素值如 "#55667788"
    private double sum;//piecesValue 的和
    //    private float[] piecesValue;//各个碎片对应的值
//    private double[] piecePercent;//各个碎片的百分比
    private float[] sweepAngles;

    //Paint
    private Paint mPaint;
    private Paint.Style paintStytle;
    private boolean useCenter;
    private final int STROKE_WIDTH_DIP = 10;//默认 Paint 的Stroke宽度
    private int mStrokeWidth;//Paint 的Stroke宽度
    private Paint mLinePaint;

    public PieChartViewWithoutAnimation(Context context) {
        super(context);

        init(context);
    }

    public PieChartViewWithoutAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);

        //创建初始化
        init(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PieChartView);
        mStrokeWidth = (int) (array.getInt(R.styleable.PieChartView_strokeWidthDip, STROKE_WIDTH_DIP)
                * context.getResources().getDisplayMetrics().density);
        this.useCenter = array.getBoolean(R.styleable.PieChartView_useCenter, false);
        //是 Fill 还是 Stroke 类型的 Paint。默认 Stroke
        boolean isFillPaint = array.getBoolean(R.styleable.PieChartView_isFillPaint, false);
        if (isFillPaint) {
            paintStytle = Paint.Style.FILL;
        }
        array.recycle();
    }

    private void init(Context context) {
        this.context = context;
        mPaint = new Paint();
        paintStytle = Paint.Style.STROKE;
        mStrokeWidth = (int) (STROKE_WIDTH_DIP * context.getResources().getDisplayMetrics().density);
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
//            mRadius = min;
//        } else if (Paint.Style.STROKE == paintStytle && min > mStrokeWidth){//弧
//            mRadius = min - mStrokeWidth/2;//注:要减去 Stroke 宽度
//        } else {
//            throw new ArithmeticException("mStrokeWidth 必须小于图宽度的一半, 即 mStrokeWidth 必须小于 Math.min(mWidth, mHeigth) / 2"
//                    + "\n当前 " + " mStrokeWidth = " + mStrokeWidth + " Math.min(mWidth, mHeigth) / 2 = " + Math.min(mWidth, mHeigth) / 2);
//        }
//        mArcRectF = new RectF(mCircleX - mRadius, mCircleY - mRadius, mCircleX + mRadius, mCircleY + mRadius);
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

        Log.i("PieChartView", "PieChartView--->mRadius = " + mCircleRadius);
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
                Log.i("PieChartView", "PieChartView--->sweepAngles[" + i + "] = " + sweepAngles[i]);
            }
        }

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

        if (piecesNum > 0 && sum > 0 && null != sweepAngles && sweepAngles.length == piecesNum) {
            //画弧或实心圆
            float startAngle = 270;
            for (int i = 0; i < piecesNum; i++) {
                mPaint.setColor(Color.parseColor(piecesColor[i]));
                canvas.drawArc(mArcRectF, startAngle, sweepAngles[i], useCenter, mPaint);
                startAngle += sweepAngles[i];
                if (startAngle >= 360) startAngle -= 360;
            }
            //画线(弧或实心圆的)
            float startA = 270;
            for (int i = 0; i < piecesNum; i++) {
                startA += sweepAngles[i];
                if (startA >= 360) startA -= 360;
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
        } else {//默认
            mPaint.setColor(Color.parseColor("#a6a6a6"));//默认
            canvas.drawCircle(mCircleX, mCircleY, mCircleRadius, mPaint);
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
