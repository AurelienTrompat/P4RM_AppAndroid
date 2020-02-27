package fr.geii.tours.iut.p4rm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by corentin on 17/03/17.
 */

public class PaintView extends View{

    private Path mRobotPath;
    private Paint mPathPaint, mCanvasPaint;
    private int mPathColor = 0xFFFF0000;
    private Canvas mDrawCanvas;
    private Bitmap mCanvasBitmap;

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        mRobotPath = new Path();
        mPathPaint = new Paint();
        mPathPaint.setColor(mPathColor);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStrokeWidth(2);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mCanvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mDrawCanvas = new Canvas(mCanvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mCanvasBitmap, 0, 0, mCanvasPaint);
        canvas.drawPath(mRobotPath, mPathPaint);
    }

    public void drawTo(int x, int y) {
        System.out.println(mDrawCanvas.getWidth());
        mRobotPath.lineTo(x+mDrawCanvas.getWidth()/2, -y+mDrawCanvas.getHeight()/2);

        mDrawCanvas.drawPath(mRobotPath, mPathPaint);
        invalidate();
    }
    public void moveTo(int x, int y) {
        mRobotPath.moveTo(x+mDrawCanvas.getWidth()/2, -y+mDrawCanvas.getHeight()/2);
    }
    public void clear() {
        mRobotPath.moveTo(mDrawCanvas.getWidth()/2, mDrawCanvas.getHeight()/2);
        mDrawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);
        mRobotPath.reset();
        invalidate();
    }
}
