package com.dongchen.keydemo.customview;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        pieChartView.setPiecesParams(3,
                new String[] {"#55ff0000", "#5500ff00", "#550000ff"},
                new double[] {3, 100, 257});
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