package com.hochan.bubbleanimation;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final ImageView ivShake = findViewById(R.id.iv_shake);
		ivShake.setImageDrawable(new BubbleLayout.BubbleDrawable(ContextCompat.getColor(this, R.color.colorOrange)));
		ivShake.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_shake);//加载动画资源文件
				ivShake.startAnimation(shake); //给组件播放动画效果
			}
		});
	}
}
