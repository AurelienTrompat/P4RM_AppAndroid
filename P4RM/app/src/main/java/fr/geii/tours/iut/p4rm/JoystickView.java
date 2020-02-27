package fr.geii.tours.iut.p4rm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by corentin on 13/11/16.
 */

public class JoystickView extends View{

    private OnJoystickMoveListener mOnJoystickMoveListener;

    private float mViewCenterX;
    private float mViewCenterY;

    private Bitmap mCircleOutBitmap;
    private Paint mCircleOutPaint;

    private Bitmap mCircleInBitmap;
    private Paint mCircleInPaint;

    private float mCircleInXOffset;
    private float mCircleInYOffset;

    private float mCircleInXCenter;
    private float mCircleInYCenter;

    private float mCircleInX;
    private float mCircleInY;

    private float mJoystickRadius;

    private int mPrevAngle, mPrevPower;




    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCircleOutBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cercle_externe_joystick);
        mCircleInBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cercle_interne_joystick);
        init();

    }

    private void init() {
        mCircleOutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleInPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCircleInXCenter = mCircleInBitmap.getWidth()/2.f;
        mCircleInYCenter = mCircleInBitmap.getHeight()/2.f;

        mCircleInXOffset = 0;
        mCircleInYOffset = 0;

        mPrevAngle = 0;
        mPrevPower = 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewCenterX = getWidth()/2.f;
        mViewCenterY = getHeight()/2.f;
        mJoystickRadius = ((mViewCenterX+mViewCenterY)/2.f - (float) Math.sqrt(mCircleInXCenter*mCircleInXCenter+mCircleInYCenter+mCircleInYCenter))*0.95f;

        mCircleInX = mViewCenterX - mCircleInXCenter + mCircleInXOffset;
        mCircleInY = mViewCenterY - mCircleInYCenter + mCircleInYOffset;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + mCircleOutBitmap.getWidth();
        int w = resolveSize(minw, widthMeasureSpec);

        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumWidth() + mCircleOutBitmap.getHeight();
        int h = resolveSize(minh, heightMeasureSpec);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mCircleOutBitmap, 0, 0, mCircleOutPaint);
        canvas.drawBitmap(mCircleInBitmap, mCircleInX, mCircleInY, mCircleInPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;

        int angle, power;

        float X = event.getX()-mViewCenterX;
        float Y = event.getY()-mViewCenterY;

        float abs = (float) Math.sqrt((double)(X*X+Y*Y));

        if(abs > mJoystickRadius) {
            X = X * mJoystickRadius / abs;
            Y = Y * mJoystickRadius / abs;
        }

        if(event.getAction() == MotionEvent.ACTION_UP) {
            mCircleInXOffset = 0;
            mCircleInYOffset = 0;
            if (mOnJoystickMoveListener != null)
                mOnJoystickMoveListener.onValueChanged((short)128, (short)128, (short)128, (short)128);
        }
        else if(mOnJoystickMoveListener != null && (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)) {
            mCircleInXOffset = X;
            mCircleInYOffset = Y;
            angle = getAngle();
            power = getPower();
            if(angle != mPrevAngle || power != mPrevPower)
                mOnJoystickMoveListener.onValueChanged((short)(X*128/(mJoystickRadius)+127), (short)(-Y*128/(mJoystickRadius)+127), getAngle(), getPower());
            mPrevAngle = angle;
            mPrevPower = power;
            result = true;
        }
        mCircleInX = mViewCenterX - mCircleInXCenter + mCircleInXOffset;
        mCircleInY = mViewCenterY - mCircleInYCenter + mCircleInYOffset;
        invalidate();
        requestLayout();
        return result;
    }

    private short getPower() {
        return (short) (255 * Math.sqrt(mCircleInXOffset*mCircleInXOffset + mCircleInYOffset*mCircleInYOffset)/mJoystickRadius);
    }
    private short getAngle() {
        short angle = 0;

        if(mCircleInXOffset > 0)
            if(mCircleInYOffset > 0)
                angle = (short) (Math.toDegrees(Math.atan(mCircleInYOffset/mCircleInXOffset)) - 360);
            else
                angle = (short)  Math.toDegrees(Math.atan(mCircleInYOffset/mCircleInXOffset));
        else if(mCircleInXOffset < 0) {
            angle = (short) (Math.toDegrees(Math.atan(mCircleInYOffset/mCircleInXOffset)) - 180);

        }
        return (short) ((-angle) * 255 / 360);
    }

    public interface OnJoystickMoveListener {
        void onValueChanged(short xAxis, short yAxis, short angle, short power);
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener listener) {
        this.mOnJoystickMoveListener = listener;
    }

}
