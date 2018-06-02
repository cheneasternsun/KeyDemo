package com.dongchen.keydemo.customview;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dongchen.keydemo.BaseActivity;
import com.dongchen.keydemo.R;

/**
 * 自定义View
 *
 * @author dongchen
 * created at 2018/5/31 15:33
 *
 * 功能：具有动画效果的环形饼状图、条形图、线性图
 * 目的：自定义View的基本实现
 */
public class CustomViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customview);
        PieChartView pieChartView = (PieChartView) findViewById(R.id.pieChartView);
        pieChartView.setPiecesParams(
                new double[] {1.333, 150.22, 207.77, 1.2},
                new String[] {"#55ff0000", "#5500ff00", "#550000ff", "#220f0f0f"});

//        pieChartView.setPiecesParams(
//                new double[] {1.333, 150.22, 1.2, 207.77},
//                new String[] {"#55ff0000", "#5500ff00", "#220f0f0f", "#550000ff"});

        //代码创建
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
        PieChartView p = new PieChartView(this, 0, true, true);
        p.setPiecesParams(
                new double[] {1.333, 150.22, 207.77, 1.2, 33.77},
                new String[] {"#55ff0000", "#5500ff00", "#550000ff", "#220f0f0f", "#88000000"});
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setLayoutParams(lp);
        ll.addView(p);
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