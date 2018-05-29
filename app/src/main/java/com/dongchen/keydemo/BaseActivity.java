package com.dongchen.keydemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 *
 *
 * @author dongchen
 * created at 2018/5/25 17:37
 *
 * 功能：
 * 目的：
 */
public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("class", "activity is " + this.getClass().getName());
    }
}
