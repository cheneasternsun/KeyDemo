package com.dongchen.keydemo.animation;

import android.support.v4.util.ArrayMap;
import android.view.View;

import java.util.Map;

/**
 * 防止多重点击 ,公用的类
 */
public abstract class AvoidMultiClick implements View.OnClickListener{
    private Map<Integer, Long> map = new ArrayMap<Integer, Long>();

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();

        if (!map.containsKey(v.getId()) || 1000 <= Math.abs(currentTime - map.get(v.getId()))) {
            map.put(v.getId(), currentTime);
            click(v);
        }
    }

    public abstract void click(View v);
}
