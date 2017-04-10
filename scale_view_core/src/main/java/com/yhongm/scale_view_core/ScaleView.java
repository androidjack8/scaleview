package com.yhongm.scale_view_core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ScaleView extends View {
    private Context mContext;
    private Paint scaleLinePaint;     //垂直刻度线画笔
    private Paint scaleTextPaint; //刻度值画笔
    private int scaleMaxLength = 100;//刻度尺的长度
    private int scaleMinNum = 150;//刻度尺的最小值
    private int eachScalePix = 15;//每个刻度值的像素
    private int defaultValue;
    private int mMoveX, lastMoveX = 0;
    private boolean isDrawScaleText;//是否画刻度
    private int mDownX;
    private int lastCurrentMoveX;
    private int mCenterY = 0;
    private int mCenterX;
    private Paint mCurrentSelectValuePaint;//当前值画笔
    private Paint mIndicatorPaint;//指示器刻度值画笔
    private int spaceUnit = 5;//单位间隔
    private String currentScaleUnit = "";

    public ScaleView(Context context) {
        this(context, null);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.ScaleView);
        int scalLineColor = typedArray.getColor(R.styleable.ScaleView_scaleLineColor, Color.parseColor("#666666"));//刻度线画笔颜色
        int scaleTextColor = typedArray.getColor(R.styleable.ScaleView_scaleTextColor, Color.parseColor("#666666"));//刻度值画笔颜色
        int indicatorColor = typedArray.getColor(R.styleable.ScaleView_indicatorColor, Color.parseColor("#ff9933"));//指示刻度线颜色
        int currentSelectColor = typedArray.getColor(R.styleable.ScaleView_currentSelectColor, Color.parseColor("#999999"));//选中结果文字颜色
        int scaleMaxLeng = typedArray.getInt(R.styleable.ScaleView_scaleMaxLength, 100);
        int scaleMinNumber = typedArray.getInt(R.styleable.ScaleView_scaleMinNumber, 150);
        String scaleUnit = typedArray.getString(R.styleable.ScaleView_scaleUnit);
        int spaceUnit = typedArray.getInt(R.styleable.ScaleView_spaceUnit, 5);
        scaleLinePaint = new Paint();
        scaleLinePaint.setAntiAlias(true);
        scaleLinePaint.setStyle(Paint.Style.STROKE);
        scaleLinePaint.setStrokeWidth(2);
        scaleLinePaint.setColor(scalLineColor);

        scaleTextPaint = new Paint();
        scaleTextPaint.setAntiAlias(true);
        scaleTextPaint.setStrokeWidth(2);
        scaleTextPaint.setTextSize(18);
        scaleTextPaint.setColor(scaleTextColor);
        scaleTextPaint.setTextAlign(Paint.Align.CENTER);

        mCurrentSelectValuePaint = new Paint();
        mCurrentSelectValuePaint.setAntiAlias(true);
        mCurrentSelectValuePaint.setTextSize(30);
        mCurrentSelectValuePaint.setColor(currentSelectColor);
        mCurrentSelectValuePaint.setTextAlign(Paint.Align.CENTER);

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setAntiAlias(true);
        mIndicatorPaint.setColor(indicatorColor);
        this.scaleMaxLength = scaleMaxLeng;
        this.scaleMinNum = scaleMinNumber;
        this.spaceUnit = spaceUnit;
        this.currentScaleUnit = scaleUnit;

    }

    /**
     * 设置刻度尺的长度
     *
     * @param scaleMaxLength 刻度尺的长度
     */
    public void setScaleMaxLength(int scaleMaxLength) {
        this.scaleMaxLength = scaleMaxLength;
    }

    /**
     * 设置最小值
     *
     * @param scaleMinNum 刻度尺的最小值
     */
    public void setScaleMinNum(int scaleMinNum) {
        this.scaleMinNum = scaleMinNum;
    }

    /**
     * 设置刻度尺的间隔
     *
     * @param spaceUnit
     */
    public void setSpaceUnit(int spaceUnit) {
        this.spaceUnit = spaceUnit;
    }

    /**
     * 设置当前刻度的单位
     *
     * @param currentScaleUnit
     */
    public void setCurrentScaleUnit(String currentScaleUnit) {
        this.currentScaleUnit = currentScaleUnit;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthValue = 0;
        int heightValue = 0;
        if (widthMode == MeasureSpec.AT_MOST) {
            defaultValue = Integer.MAX_VALUE;

            widthValue = Math.min(widthSize, defaultValue);
        } else if (widthMode == MeasureSpec.EXACTLY) {
            widthValue = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            defaultValue = 120;
            heightValue = Math.min(heightSize, defaultValue);
        } else if (widthMode == MeasureSpec.EXACTLY) {
            heightValue = heightSize;
        }

        setMeasuredDimension(widthValue, heightValue);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initStart();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int currentValue = (-lastMoveX + mCenterX) / eachScalePix + scaleMinNum;
        if (moveScaleListener != null) {
            moveScaleListener.currentVale(currentValue);
        }
        drawCurrentScale(canvas, currentValue);

        drawNum(canvas);
    }

    /**
     * 绘画数字
     *
     * @param canvas
     */
    private void drawNum(Canvas canvas) {
        for (int start = 0; start < mCenterX * 2; start++) {
            int top = mCenterY + 10;
            if ((-lastMoveX + start) % (eachScalePix * spaceUnit) == 0) {
                top = top + 20;
                isDrawScaleText = true;
            } else {
                isDrawScaleText = false;
            }
            //    （-lastMoveX + start)：向左滑动值增加，数值增加，所以为负数
            if ((-lastMoveX + start) % eachScalePix == 0) {
                if ((-lastMoveX + start) >= 0 && (-lastMoveX + start) <= scaleMaxLength * eachScalePix) {
                    canvas.drawLine(start, mCenterY, start, top, scaleLinePaint);
                }

            }

            if (isDrawScaleText) {
                if ((-lastMoveX + start) >= 0 && (-lastMoveX + start) <= scaleMaxLength * eachScalePix)
                    canvas.drawText((-lastMoveX + start) / eachScalePix + scaleMinNum + "", start, top + 20, scaleTextPaint);
            }
        }
    }

    /**
     * 绘画刻度
     *
     * @param canvas
     * @param currentValue
     */
    private void drawCurrentScale(Canvas canvas, int currentValue) {
        RectF roundRectF = new RectF();
        roundRectF.left = mCenterX - 3;
        roundRectF.right = mCenterX + 3;
        roundRectF.top = mCenterY;
        roundRectF.bottom = mCenterY + 50;
        canvas.drawRoundRect(roundRectF, 6, 6, mIndicatorPaint);
        String currentScaleText = currentValue + "";

        canvas.drawText(currentScaleText + currentScaleUnit, mCenterX, mCenterY - 10, mCurrentSelectValuePaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveX = (int) (event.getX() - mDownX);
//                if (lastCurrentMoveX == mMoveX) {
//                    return true;
//                }
                lastMoveX = lastMoveX + mMoveX;
                if (mMoveX < 0) {
                    //向左滑动,刻度值增大
                    if (-lastMoveX + mCenterX > scaleMaxLength * eachScalePix) {
                        //向左滑动如果刻度值大于最大值，则不能滑动了
                        lastMoveX = lastMoveX - mMoveX;
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                } else {
                    //向右滑动，刻度值减小
//                    向右滑动刻度值小于最小值则不能滑动了
                    if (lastMoveX - mCenterX > 0) {
                        lastMoveX = lastMoveX - mMoveX;
                        getParent().requestDisallowInterceptTouchEvent(true);
                        return true;
                    } else {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                lastCurrentMoveX = mMoveX;
                mDownX = (int) event.getX();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private void initStart() {
        mCenterY = getHeight() / 2;
        mCenterX = getWidth() / 2;
    }

    private MoveScaleListener moveScaleListener;

    public void setMoveScaleListener(MoveScaleListener moveScaleListener) {
        this.moveScaleListener = moveScaleListener;
    }

    /**
     * 速度追踪的回调
     */
    public interface MoveScaleListener {
        void currentVale(int value);
    }
}

