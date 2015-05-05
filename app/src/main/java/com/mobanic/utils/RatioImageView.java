package com.mobanic.utils;

import android.content.Context;
import android.util.AttributeSet;

import com.parse.ParseImageView;

public class RatioImageView extends ParseImageView {

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = width / 16 * 9;

        setMeasuredDimension(width, height);
    }
}
