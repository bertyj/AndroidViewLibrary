package com.android.yj.viewlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by bertyj on 2017/6/2.
 */

public class SelectBar extends View {
    private static final String TAG = SelectBar.class.getSimpleName();
    private static final String ERROR_LABEL_ARRAY_NUM = "label array length should match specific item num";
    private static final String ERROR_SELECTED_ITEM_INDEX = "selected item index out of bounds";

    private Paint mUnClickedRectPaint;
    private Paint mClickedRectPaint;
    private Paint mUnClickedCirclePaint;
    private Paint mClickedCirclePaint;
    private Paint mTextPaint;

    private int mUnSelectedLineColor;
    private int mSelectedLineColor;
    private int mUnSelectedItemColor;
    private int mSelectedItemColor;
    private int mLabelColor;
    /* 圆圈总个数 */
    private int mItemNum;
    /* 被点击的圆圈下标，从0开始 */
    private int mSelectedItemIndex;
    /* 圆圈指示文字数组 */
    private String[] mLabelArray;

    /* 整个View控件的宽度 */
    private int mMeasuredViewWidth;
    /* 整个View控件的高度 */
    private int mMeasuredViewHeight;

    /* 矩形水平中心线Y轴坐标，作为绘制矩形的参考 */
    private int mMidRectHeightY;

    /* 矩形的高度 */
    private int mRectHeight;
    /* 矩形的宽度 */
    private int mRectWidth;
    /* 矩形的左右Padding，左右一致 */
    private int mRectLRPadding;

    /* 圆圈半径 */
    private int mCircleRadius;
    /* 相邻圆圈的圆心间距 */
    private int mCircleGap;

    private OnItemClickedListener mOicListener;

    public SelectBar(Context context) {
        this(context, null);
    }

    public SelectBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaints();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SelectBar);
        int attrCount = attrs.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);
            Log.i(TAG, attrName + " : " + attrValue);
        }
        mUnSelectedLineColor = typedArray.getColor(R.styleable.SelectBar_unselected_line_color, Color.LTGRAY);
        mSelectedLineColor = typedArray.getColor(R.styleable.SelectBar_selected_line_color, Color.DKGRAY);
        mUnSelectedItemColor = typedArray.getColor(R.styleable.SelectBar_unselected_item_color, Color.LTGRAY);
        mSelectedItemColor = typedArray.getColor(R.styleable.SelectBar_selected_item_color, Color.DKGRAY);
        mLabelColor = typedArray.getColor(R.styleable.SelectBar_label_color, Color.BLACK);
        mItemNum = typedArray.getInt(R.styleable.SelectBar_item_num, 1);
        mLabelArray = new String[mItemNum];
        CharSequence[] csArr = typedArray.getTextArray(R.styleable.SelectBar_label_array);
        if (csArr == null) {
            for (int i = 0; i < mItemNum; i++) {
                mLabelArray[i] = String.valueOf(i + 1);
            }
        } else {
            if (mItemNum == csArr.length) {
                for (int i = 0; i < mItemNum; i++) {
                    mLabelArray[i] = String.valueOf(csArr[i]);
                }
            } else {
                throw new RuntimeException(ERROR_LABEL_ARRAY_NUM);
            }
        }
        mSelectedItemIndex = typedArray.getInt(R.styleable.SelectBar_selected_item_index, 0);
        if (mSelectedItemIndex < 0 || mSelectedItemIndex > mItemNum - 1) {
            throw new RuntimeException(ERROR_SELECTED_ITEM_INDEX);
        }
        typedArray.recycle();
    }

    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mOicListener = listener;
    }

    public void setUnSelectLineColor(int color) {
        mUnSelectedLineColor = color;
    }

    public void setSelectLineColor(int color) {
        mSelectedLineColor = color;
    }

    public void setUnSelectItemColor(int color) {
        mUnSelectedItemColor = color;
    }

    public void setSelectItemColor(int color) {
        mSelectedItemColor = color;
    }

    public void setLabelColor(int color) {
        mLabelColor = color;
    }

    public void setItemNum(int num) {
        mItemNum = num;
        /* 设置Item总个数会改变整个控件的结构，为了防止出现crash，必须重新设置被选中item的下标为0 */
        mSelectedItemIndex = 0;
        invalidate();
    }

    public void setSelectedItemIndex(int index) {
        mSelectedItemIndex = index;
        invalidate();
    }

    public void setLabelArray(String[] array) {
        mLabelArray = array;
        invalidate();
    }

    private void initAttrs() {
        mItemNum = 5;
        mLabelArray = new String[mItemNum];
        for (int i = 0; i < mItemNum; i++) {
            mLabelArray[i] = String.valueOf(i + 1);
        }
    }

    private void initPaints() {
        mUnClickedRectPaint = new Paint();
        mUnClickedRectPaint.setStyle(Paint.Style.FILL);
        mClickedRectPaint = new Paint();
        mClickedRectPaint.setStyle(Paint.Style.FILL);

        mUnClickedCirclePaint = new Paint();
        mUnClickedCirclePaint.setStyle(Paint.Style.FILL);
        mClickedCirclePaint = new Paint();
        mClickedCirclePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setFakeBoldText(true);
    }

    private void updatePaintsColor() {
        if (mUnSelectedLineColor == 0) {
            mUnClickedRectPaint.setColor(Color.LTGRAY);
        } else {
            mUnClickedRectPaint.setColor(mUnSelectedLineColor);
        }

        if (mSelectedLineColor == 0) {
            mClickedRectPaint.setColor(Color.DKGRAY);
        } else {
            mClickedRectPaint.setColor(mSelectedLineColor);
        }

        if (mUnSelectedItemColor == 0) {
            mUnClickedCirclePaint.setColor(Color.BLUE);
        } else {
            mUnClickedCirclePaint.setColor(mUnSelectedItemColor);
        }

        if (mSelectedItemColor == 0) {
            mClickedCirclePaint.setColor(Color.MAGENTA);
        } else {
            mClickedCirclePaint.setColor(mSelectedItemColor);
        }

        if (mLabelColor == 0) {
            mTextPaint.setColor(Color.BLACK);
        } else {
            mTextPaint.setColor(mLabelColor);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasuredViewWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        mMeasuredViewHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(mMeasuredViewWidth, mMeasuredViewHeight);

        mMidRectHeightY = mMeasuredViewHeight / 3;
        mTextPaint.setTextSize(mMeasuredViewHeight / 3);

        mCircleRadius = mMeasuredViewHeight / 6;
        mCircleGap = mMeasuredViewWidth / (mItemNum + 1);

        mRectLRPadding = mCircleGap / 3;
        mRectHeight = mMeasuredViewHeight / 10;
        mRectWidth = mMeasuredViewWidth - mRectLRPadding * 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updatePaintsColor();
        if (mSelectedItemIndex < 0 || mSelectedItemIndex > mItemNum - 1) {
            throw new RuntimeException(ERROR_SELECTED_ITEM_INDEX);
        }
        canvas.drawRect(mRectLRPadding,
                mMidRectHeightY - mRectHeight / 2,
                mCircleGap * (mSelectedItemIndex + 1),
                mMidRectHeightY + mRectHeight / 2,
                mClickedRectPaint);
        canvas.drawRect(mCircleGap * (mSelectedItemIndex + 1),
                mMidRectHeightY - mRectHeight / 2,
                mRectLRPadding + mRectWidth,
                mMidRectHeightY + mRectHeight / 2,
                mUnClickedRectPaint);
        if (mSelectedItemIndex + 1 == mItemNum) {
            canvas.drawRect(mRectLRPadding,
                    mMidRectHeightY - mRectHeight / 2,
                    mRectLRPadding + mRectWidth,
                    mMidRectHeightY + mRectHeight / 2,
                    mClickedRectPaint);
        }

        for (int i = 0; i < mItemNum; i++) {
            if (i <= mSelectedItemIndex) {
                canvas.drawCircle((i + 1) * mCircleGap,
                        mMidRectHeightY,
                        mCircleRadius,
                        mClickedCirclePaint);
            } else {
                canvas.drawCircle((i + 1) * mCircleGap,
                        mMidRectHeightY,
                        mCircleRadius,
                        mUnClickedCirclePaint);
            }
            canvas.drawText(mLabelArray[i],
                    (i + 1) * mCircleGap,
                    mMidRectHeightY + mCircleRadius * 3,
                    mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                for (int i = 0; i < mItemNum; i++) {
                    if (y >= mMidRectHeightY - mCircleRadius &&
                            y <= mMidRectHeightY + mCircleRadius) {
                        if (x >= (i + 1) * mCircleGap - mCircleRadius &&
                                x <= (i + 1) * mCircleGap + mCircleRadius) {
                            mSelectedItemIndex = i;
                            invalidate();
                            if (mOicListener != null) {
                                mOicListener.onItemClick(mSelectedItemIndex,
                                        mLabelArray[mSelectedItemIndex],
                                        event);
                            }
                            return true;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface OnItemClickedListener {
        void onItemClick(int index, String label, MotionEvent event);
    }
}
