package com.mappfia.mobanic;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RatioImageView extends ImageView {

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = (int) (width * 0.5625);

        setMeasuredDimension(width, height);
    }
}
