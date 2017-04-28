package com.yhongm.scale_view_core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private int mSlidingMoveX = 0;//滑动的差值
    private int totalX = 0;//滑动总距离
    private boolean isDrawScaleText;//是否画刻度
    private int mDownX;
    private int mCenterY = 0;
    private int mCenterX;
    private Paint mCurrentSelectValuePaint;//当前值画笔
    private Paint mIndicatorPaint;//指示器刻度值画笔
    private int spaceUnit = 5;//单位间隔
    private String currentScaleUnit = "";
    private int mHeight;
    private int mWidth;

    public ScaleView(Context context) {
        this(context, null);
    }

    public ScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setClickable(true);
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

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mCenterY = getMeasuredHeight() / 2;
        mCenterX = getMeasuredWidth() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int currentValue = (-totalX + mCenterX) / eachScalePix + scaleMinNum;
        if (moveScaleListener != null) {
            moveScaleListener.currentVale(currentValue);
        }
        drawCurrentScale(canvas, currentValue);

        drawNum(canvas);

        drawMask(canvas);
    }

    /**
     * 画遮罩层
     *
     * @param canvas
     */
    private void drawMask(Canvas canvas) {
        Paint mMaskPaint = new Paint();
        mMaskPaint.setStrokeWidth(mHeight);
        LinearGradient mLeftLinearGradient = new LinearGradient(0, mCenterY, 100, mCenterY, Color.parseColor("#01000000"), Color.parseColor("#11000000"), Shader.TileMode.CLAMP);
        mMaskPaint.setShader(mLeftLinearGradient);
        canvas.drawLine(0, mCenterY, mCenterX, mCenterY, mMaskPaint);
        LinearGradient mRightLinearGradient = new LinearGradient(mWidth - 100, mCenterY, mWidth, mCenterY, Color.parseColor("#11000000"), Color.parseColor("#01000000"), Shader.TileMode.CLAMP);
        mMaskPaint.setShader(mRightLinearGradient);
        canvas.drawLine(mCenterX, mCenterY, mWidth, mCenterY, mMaskPaint);
    }

    /**
     * 绘画数字
     *
     * @param canvas
     */
    private void drawNum(Canvas canvas) {
        for (int i = 0; i < mWidth; i++) {
            int top = mCenterY + 10;
            if ((-totalX + i) % (eachScalePix * spaceUnit) == 0) {
                top = top + 30;
                isDrawScaleText = true;
            } else {
                isDrawScaleText = false;
            }
            if ((-totalX + i) % eachScalePix == 0) {
                if ((-totalX + i) >= 0 && (-totalX + i) <= scaleMaxLength * eachScalePix) {
                    canvas.drawLine(i, mCenterY, i, top, scaleLinePaint);
                }

            }

            if (isDrawScaleText) {
                if ((-totalX + i) >= 0 && (-totalX + i) <= scaleMaxLength * eachScalePix)
                    canvas.drawText((-totalX + i) / eachScalePix + scaleMinNum + "", i, top + 20, scaleTextPaint);
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
                mSlidingMoveX = (int) (event.getX() - mDownX);//滑动距离
                totalX = totalX + mSlidingMoveX;
                if (mSlidingMoveX < 0) {
                    //向左滑动,刻度值增大
                    if (-totalX + mCenterX > scaleMaxLength * eachScalePix) {
                        //向左滑动如果刻度值大于最大值，则不能滑动了
                        totalX = totalX - mSlidingMoveX;
                        return true;
                    } else {
                        invalidate();
                    }
                } else {
                    //向右滑动，刻度值减小
//                    向右滑动刻度值小于最小值则不能滑动了
                    if (totalX - mCenterX > 0) {
                        totalX = totalX - mSlidingMoveX;
                        return true;
                    } else {
                        invalidate();

                    }
                }
                mDownX = (int) event.getX();

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
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

