package com.samples.flironecamera.camera;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;

/**box drawing, show temperature */

public class FaceGraphic extends GraphicOverlay.Graphic {
    public interface ITemp {
        double getTemp(int faceId);
    }

    private ITemp mITemp;

    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.MAGENTA,
            Color.CYAN,
            Color.GREEN,
            Color.YELLOW,
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    public volatile Face mFace;
    private int mFaceId;

    public FaceGraphic(GraphicOverlay overlay, ITemp iTemp) {
        super(overlay);
        mITemp = iTemp;

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    public void setId(int id) {
        mFaceId = id;
    }

    /* Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw. */
    public void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /* Draws the face annotations for position on the supplied canvas. */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;

        if (face == null) {
            return;
        }

        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawText("" + String.format("%.2f", mITemp.getTemp(face.getId())), x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }
}
