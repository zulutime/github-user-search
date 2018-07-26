package io.arcblock.task.usersearcher.util;

import android.animation.Animator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class TextAnimator {

    public static void run(final TextView textView, final CharSequence text) {
        Animation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(100);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);

        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {
                textView.setText(text);
            }
        });
        textView.startAnimation(anim);
    }
}
