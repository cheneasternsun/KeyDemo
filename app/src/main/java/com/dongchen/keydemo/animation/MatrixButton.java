package com.dongchen.keydemo.animation;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by zhengmj on 16-7-12.
 */
public class MatrixButton extends Button {
    private boolean valid;

    public MatrixButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        // TODO Auto-generated constructor stub
    }

    public MatrixButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        // TODO Auto-generated constructor stub
    }

    public MatrixButton(Context context) {
        super(context);
        init();
        // TODO Auto-generated constructor stub
    }

    private void init() {
        this.valid = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (valid) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    Drawable downDrawable = getBackground();
                    ColorMatrix downMatrix = new ColorMatrix();
                    downMatrix.setSaturation(0.2f);
                    downDrawable.setColorFilter(new ColorMatrixColorFilter(downMatrix));
                    break;
                case MotionEvent.ACTION_UP:
                    Drawable upDrawable = getBackground();
                    upDrawable.clearColorFilter();  //清除颜色矩阵，恢复原来的显示效果
//                ColorMatrix upMatrix = new ColorMatrix();
//                upDrawable.setColorFilter(new ColorMatrixColorFilter(upMatrix));
                    break;
                default:
                    break;

            }
        }
        return super.onTouchEvent(event);
    }


    public void setInvalid() {
        valid = false;
        Drawable downDrawable = getBackground();
        ColorMatrix downMatrix = new ColorMatrix();
        downMatrix.setSaturation(0.2f);
        downDrawable.setColorFilter(new ColorMatrixColorFilter(downMatrix));
        postInvalidate();
    }

    public void setValid() {
        valid = true;
        Drawable upDrawable = getBackground();
        upDrawable.clearColorFilter();
        postInvalidate();
    }

    public boolean isValid() {
        return valid;
    }


}

