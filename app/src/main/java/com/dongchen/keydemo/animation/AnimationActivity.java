package com.dongchen.keydemo.animation;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dongchen.keydemo.BaseActivity;
import com.dongchen.keydemo.R;

/**
 *
 *
 * @author dongchen
 * created at 2018/5/25 17:39
 *
 * 功能：
 * 目的：
 */
public class AnimationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button b = new Button(this);
        b.setGravity(Gravity.CENTER);
        b.setText("抽奖");
        b.setTextSize(30);
        setContentView(b);

        final CheckAwardBoxKeyDialog dialog = new CheckAwardBoxKeyDialog(this);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.showDialog();
            }
        });
    }
}
