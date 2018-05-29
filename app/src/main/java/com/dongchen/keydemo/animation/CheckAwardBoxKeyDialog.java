package com.dongchen.keydemo.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.dongchen.keydemo.R;

/**
 * 弹出宝箱的对话框
 *
 * @author dongchen
 * created at 2018/5/26 16:09
 *
 * 功能：
 * 目的：
 */
public class CheckAwardBoxKeyDialog extends AvoidMultiClick {
    //宝箱布局
    private ImageView ivLight;//发光的背景图
    private Button btnOpenBox;
    private ImageView ivClosedBox;
    private TextView tvTip;//提示

    private Activity activity;
    private Dialog dialog;
    private boolean isFirstTime = true;

    public CheckAwardBoxKeyDialog(Context context) {
        this.activity = (Activity) context;
        initDialog();
    }

    private void initDialog() {
        //宝箱布局
        View view = LayoutInflater.from(activity).inflate(R.layout.tjr_social_dialog_checkawardbox_box, null);
        tvTip = (TextView) view.findViewById(R.id.tvTip);
        ivLight = (ImageView) view.findViewById(R.id.ivLight);
        btnOpenBox = (MatrixButton) view.findViewById(R.id.btnOpenBox);
        //贝塞尔曲线对应的宝箱 ImageView
        ivClosedBox = (ImageView) view.findViewById(R.id.ivClosedBox);

        dialog = new Dialog(activity, R.style.checkawardboxdialog) {
            @Override
            public void onWindowFocusChanged(boolean hasFocus) {
                super.onWindowFocusChanged(hasFocus);

                if (isFirstTime) {
                    getBezierAnimator().start();
                    isFirstTime = false;
                }
            }

            @Override
            public void dismiss() {//cancel()会调用dismiss()，但dismiss()不会调用cancel()
                isFirstTime = true;
                resetDialog();
                super.dismiss();
            }

            @Override
            protected void onStop() {
                super.onStop();
            }
        };
        dialog.setContentView(view);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.CENTER);

        //设置
        resetDialog();
        btnOpenBox.setOnClickListener(this);
    }

    private void resetDialog() {
        /*重置Dialog */
        ivClosedBox.clearAnimation();
        ivLight.clearAnimation();
        ivLight.setVisibility(View.INVISIBLE);
        tvTip.setVisibility(View.INVISIBLE);
        btnOpenBox.setVisibility(View.INVISIBLE);
        ivClosedBox.setVisibility(View.INVISIBLE);
        ivClosedBox.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_box_closed_box));
    }

    private ValueAnimator getBezierAnimator() {
        ivClosedBox.setVisibility(View.VISIBLE);

        /*初始化 3 个点*/
        int[] location = new int[2];
        //起点
        tvTip.getLocationInWindow(location);
        location[0] += tvTip.getWidth() / 2 - ivClosedBox.getWidth() / 2;
        location[1] += tvTip.getHeight() / 2 - ivClosedBox.getHeight() / 2;
        PointF pointStart = new PointF(location[0], location[1]);//起点坐标（是绝对坐标），是tvTip的中心。
        //终点和辅助点
        ivLight.getLocationInWindow(location);
        location[0] += ivLight.getWidth() / 2 - ivClosedBox.getWidth() / 2;
        location[1] += ivLight.getHeight() / 2 - ivClosedBox.getHeight() / 2;
        PointF pointEnd = new PointF(location[0], location[1]);//终点坐标，是ivLight的中心。
        PointF pointMid = new PointF(location[0], location[1] - 200);//中间的辅助点

        /*获取 ValueAnimator */
        QuadraticBezier bezierEvaluator = new QuadraticBezier(pointMid);
        ValueAnimator valueAnimator = ValueAnimator.ofObject(bezierEvaluator, pointStart, pointEnd);
        valueAnimator.setDuration(1000);
        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        valueAnimator.setInterpolator(bounceInterpolator);//动画结束时弹起

        // 给动画添加一个动画的进度监听;在动画执行的过程中动态的改变view的位置;
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();//getAnimatedValue()获取的是已设置的TypeEvaluator的值。
                ViewCompat.setX(ivClosedBox, pointF.x);
                ViewCompat.setY(ivClosedBox, pointF.y);

                ViewCompat.setScaleX(ivClosedBox, animation.getAnimatedFraction());//getAnimatedFraction()获取的是时间流逝百分比。
                ViewCompat.setScaleY(ivClosedBox, animation.getAnimatedFraction());
                // 设置view的透明度,达到动画执行过程view逐渐透明效果;
//                view.setAlpha(1 - animation.getAnimatedFraction());
            }
        });

        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ivClosedBox.clearAnimation();
                tvTip.setVisibility(View.VISIBLE);
                btnOpenBox.setVisibility(View.VISIBLE);
                ivLight.setVisibility(View.VISIBLE);
                AnimationSet as = (AnimationSet) AnimationUtils.loadAnimation(activity,
                        R.anim.box_rotate_alpha_infinite);
                ivLight.setAnimation(as);
                as.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        return valueAnimator;
    }


    public void showDialog() {
        if (!activity.isFinishing() && null != dialog && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dissmissDialog() {
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isDialogShowing() {
        return !(activity.isFinishing() || null == dialog || !dialog.isShowing());
    }

    @Override
    public void click(View v) {
        int i = v.getId();
        if (i == R.id.btnOpenBox) {//开启宝箱
            RotateAnimation as = (RotateAnimation) AnimationUtils.loadAnimation(activity, R.anim.box_rotate);
            as.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ivClosedBox.clearAnimation();
                    tvTip.setVisibility(View.INVISIBLE);
                    btnOpenBox.setVisibility(View.INVISIBLE);
                    ivClosedBox.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_box_empty_open_box));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            ivClosedBox.startAnimation(as);
        }
    }

}
/**
 * 结果：
 *
 * 总结：
 * 一、步骤
 *
 * 二、关键
 *
 */