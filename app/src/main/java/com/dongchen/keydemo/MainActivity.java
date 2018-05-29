package com.dongchen.keydemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 首页
 * 
 * @author dongchen
 * created at 2018/5/26 16:34
 * 
 * 功能：
 * 目的：
 */
public class MainActivity extends Activity {
    private final int EXPLICIT_JUMP = 1;    //显式启动
    private final int IMPLICIT_JUMP = 0;    //隐式启动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = (ListView) findViewById(R.id.listView);
        ListViewAdapter adapter = new ListViewAdapter(MainActivity.this, getList());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                JumpBean jumpBean = (JumpBean) parent.getItemAtPosition(position);

                if (null == jumpBean) return;
                if (EXPLICIT_JUMP == jumpBean.getJumpType()) { //显式启动
                    try {
                        Intent intent = new Intent(MainActivity.this, Class.forName(jumpBean.getAbsClsUrlOrAction()));
                        startActivity(intent);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
                    Intent intent = new Intent();
                    intent.setAction(jumpBean.getAbsClsUrlOrAction());
                    startActivity(intent);
                }
            }
        });
    }

    private List getList() {
        List list = new ArrayList();

        /*添加数据*/

        /*//Test 显式启动 隐式启动
        JumpBean j1 = new JumpBean();
        j1.setJumpType(EXPLICIT_JUMP);
        j1.setPage(0);
        j1.setContent("Explicit Jump Test 显示启动");
        j1.setAbsClsUrlOrAction("com.dongcheng.qunyingzhuandemo.test.ExplicitJumpTestActivity");
        list.add(j1);

        JumpBean j2 = new JumpBean();
        j2.setJumpType(IMPLICIT_JUMP);
        j1.setPage(0);
        j2.setContent("Implicit Jump Test 隐式启动");
        j2.setAbsClsUrlOrAction("com.dongcheng.qunyingzhuandemo.test.ImplicitJumpTestAcitvity");
        list.add(j2);

        //165页：属性动画的简单使用（具备交互性，响应事件的位置总不在原地）
        JumpBean j16 = new JumpBean();
        j16.setJumpType(EXPLICIT_JUMP);
        j16.setPage(165);
        j16.setContent(j16.getPage() + "页：属性动画的简单使用（具备交互性，响应事件的位置总不在原地）");
        j16.setAbsClsUrlOrAction("com.dongcheng.qunyingzhuandemo.attributeanimatorwhat.AttributeAnimatorWhatActivity");
        list.add(j16);*/

        //Animation
        JumpBean j1 = new JumpBean();
        j1.setJumpType(EXPLICIT_JUMP);
        j1.setPage(0);
        j1.setContent("Animation");
        j1.setAbsClsUrlOrAction("com.dongchen.keydemo.animation.AnimationActivity");
        list.add(j1);


        //排序
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((JumpBean)rhs).getPage() - ((JumpBean)lhs).getPage();   //递减
            }
        });

        return list;
    }
}

/*
结果：

总结：
一、步骤

二、关键

 */