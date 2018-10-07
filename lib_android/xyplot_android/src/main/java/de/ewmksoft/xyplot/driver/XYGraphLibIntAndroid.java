/*
 ****************************************************************************
 *
 *  This file is part of the XYPlot library. The library allows to draw
 *  data in a x/y diagram using several output media.
 *
 *  Copyright (C) 1994-2015 EWMK-Soft Eberhard Kuemmel
 *
 *  LICENSE AGREEMENT
 *
 *  WHEREAS, Eberhard Kuemmel is the owner of valuable intellectual
 *  property rights relating to the XYPlot and wish to license XYPlot
 *  subject to the terms and conditions set forth below;
 *
 *  and
 *
 *  WHEREAS, you ("Licensee") acknowledge that Eberhard Kuemmel has the
 *  right to grant licenses to the intellectual property rights relating
 *  to XYPlot, and that you desire to obtain a license to use XYPlot
 *  subject to the terms and conditions set forth below;
 *
 *  Eberhard Kuemmel grants Licensee a non-exclusive, non-transferable,
 *  royalty-free license to use XYPlot and related materials without
 *  charge provided the Licensee adheres to all of the terms and conditions
 *  of this Agreement.
 *
 *  By downloading, using, or copying XYPlot or any portion thereof,
 *  Licensee agrees to abide by the intellectual property laws and all
 *  other applicable laws of Germany, and to all of the terms and
 *  conditions of this Agreement, and agrees to take all necessary steps
 *  to ensure that the terms and conditions of this Agreement are not
 *  violated by any person or entity under the Licensee's control or in
 *  the Licensee's service.
 *
 *  Licensee shall maintain the copyright and trademark notices on the
 *  materials within or otherwise related to XYPlot, and not alter,
 *  erase, deface or overprint any such notice.
 *
 *  Licensee hereby grants a royalty-free license to any and all
 *  derivatives based upon this software code base.
 *
 *  Licensee may modify the sources of XYPlot for the Licensee's own
 *  purposes. Thus, Licensee may not distribute modified sources of
 *  XYPlot without prior written consent from the authors.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *****************************************************************************/

package de.ewmksoft.xyplot.driver;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.View;

import de.ewmksoft.xyplot.core.IXYGraphLib;
import de.ewmksoft.xyplot.core.IXYGraphLibAdapter;
import de.ewmksoft.xyplot.core.IXYGraphLibInt;
import de.ewmksoft.xyplot.core.IXYGraphLib.*;

/**
 * Concrete XYPlot graphic library implementation for Android
 * <p>
 * (c) EWMK-Soft 1990-2010
 *
 * @author Eberhard Kuemmel
 */
class XYGraphLibIntAndroid implements IXYGraphLibInt {
    final private double CHAR_HEIGHT_SCALE_FACTOR = 1.5;

    private Paint paint;
    private Canvas canvas;
    private IXYGraphLibAdapter xyPlotAdapter;
    private Pt defCharSize;
    private IXYGraphLib.Rect bounds;

    private HashMap<Integer, Integer> plotColor;
    private int lineColor;
    private int gridColor;
    private int cursorColor;
    private int cursorBgColor;
    private int buttonColor;
    private int legendBgColor;
    private int legendSelectColor;
    private int currentBgColor;
    private int bgColor;
    private int plotBgColor;
    private int buttonBgColor;
    private int buttonBgColorDisabled;
    private int labelFontSize;
    private int titleFontSize;
    private Bitmap mBitmap;
    private Canvas bitmapCanvas;
    private Paint mBitmapPaint;
    private Bitmap downImage;
    private Bitmap upImage;
    private Bitmap leftImage;
    private Bitmap rightImage;
    private Bitmap plusImage;
    private Bitmap minusImage;
    private Bitmap startImage;
    private Bitmap stopImage;
    private Bitmap pauseImage;
    private Bitmap wasteImage;
    private Bitmap zoomUpImage;
    private Bitmap zoomDownImage;
    private Bitmap pos1Image;
    private Bitmap endImage;
    private Bitmap saveCurveImage;
    private Drawable[] imageDrawables;
    private float buttonRatio = 1.0f * 25 / 40;
    private View owner;

    @SuppressLint("UseSparseArrays")
    XYGraphLibIntAndroid(View owner, IXYGraphLib.Rect bounds) {
        this.owner = owner;
        setBounds(bounds);
        imageDrawables = new Drawable[ButtonImages.values().length];
        plotColor = new HashMap<Integer, Integer>();
        plotBgColor = Color.argb(255, 252, 252, 252);
        bgColor = Color.argb(255, 250, 250, 250);
        lineColor = Color.argb(255, 50, 50, 50);
        legendBgColor = Color.argb(255, 255, 255, 255);
        buttonBgColor = Color.argb(255, 190, 190, 190);
        buttonBgColorDisabled = Color.argb(255, 220, 220, 220);
        buttonColor = Color.argb(255, 255, 255, 255);
        gridColor = Color.argb(205, 235, 235, 235);
        legendSelectColor = Color.argb(255, 230, 230, 230);
        cursorColor = Color.argb(155, 55, 55, 55);
        cursorBgColor = Color.argb(255, 230, 230, 230);

        paint = new Paint();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        if (!owner.isInEditMode()) {
            try {
                loadImages();
            } catch (IOException e) {
                // Ignore
            }
        }
        int x = Math.min(bounds.height, bounds.width);
        int fontSize = Math.max(18, x / 20);
        setFontSize(fontSize, fontSize);
    }

    void setBounds(IXYGraphLib.Rect rect) {
        bounds = rect;
        int w = Math.max(bounds.width, 10);
        int h = Math.max(bounds.height, 10);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(mBitmap);
        if (xyPlotAdapter != null) {
            xyPlotAdapter.setBounds(rect);
        }
    }

    public IXYGraphLibInt getInt() {
        return null;
    }

    public void registerXYPlot(IXYGraphLibAdapter xyPlot) {
        this.xyPlotAdapter = xyPlot;
    }

    void paint(Canvas canvas) {
        if (xyPlotAdapter == null)
            return;
        if (xyPlotAdapter.showsCursor()) {
            canvas.drawBitmap(mBitmap, bounds.x, bounds.y, mBitmapPaint);
            this.canvas = canvas;
            xyPlotAdapter.paintCursor();
        } else {
            this.canvas = bitmapCanvas;
            xyPlotAdapter.paintGraph();
            canvas.drawBitmap(mBitmap, bounds.x, bounds.y, mBitmapPaint);
            this.canvas = canvas;
            xyPlotAdapter.paintCursor();
        }
    }

    /****************** Graphic library independent methods ******************/

    @Override
    public void startDelayedWakeTrigger(long delay) {

    }

    @Override
    public IXYGraphLib.Rect getBounds() {
        return bounds;
    }

    @Override
    public void setFgPlotColor(int no) {
        Integer c = plotColor.get(no);
        if (c != null) {
            paint.setColor(c);
        }
    }

    @Override
    public void setBgPlotColor(int no) {
        Integer c = plotColor.get(no);
        if (c != null) {
            currentBgColor = c;
        }
    }

    @Override
    public void createColor(int no, RGB value) {
        Integer c = plotColor.get(no);
        if (c == null) {
            plotColor.put(no,
                    Color.argb(255, value.red, value.green, value.blue));
        }
    }

    /**
     * Get the width of a average character in the current drawing environment
     *
     * @return Width of the character '0'
     */
    @Override
    public Pt getAverageCharacterSize() {
        return defCharSize;
    }

    /**
     * Get width and height of a string in pixels
     *
     * @return Y and Y information as Pt object
     */
    @Override
    public Pt getStringExtends(String s) {
        Pt result = new Pt();
        android.graphics.Rect r = new android.graphics.Rect();
        paint.getTextBounds(s, 0, s.length(), r);
        result.x = r.width();
        result.y = (int) Math.round(CHAR_HEIGHT_SCALE_FACTOR * r.height());
        return result;
    }

    /**
     * Set current foreground color
     */
    @Override
    public void setFgColor(FgColor c) {
        switch (c) {
            case AXIS:
                paint.setColor(lineColor);
                break;
            case GRID:
                paint.setColor(gridColor);
                break;
            case CURSOR:
                paint.setColor(cursorColor);
                break;
            case BUTTON:
                paint.setColor(buttonColor);
                break;
        }
    }

    /**
     * Set current background color
     */
    @Override
    public void setBgColor(BgColor c) {
        switch (c) {
            case BG:
                currentBgColor = bgColor;
                break;
            case PLOTBG:
                currentBgColor = plotBgColor;
                break;
            case LEGENDBG:
                currentBgColor = legendBgColor;
                break;
            case LEGENDSELECTBG:
                currentBgColor = legendSelectColor;
                break;
            case BUTTONBG:
                currentBgColor = buttonBgColor;
                break;
            case BUTTONBGDISABLED:
                currentBgColor = buttonBgColorDisabled;
                break;
            case CURSORBG:
                currentBgColor = cursorBgColor;
                break;
        }
    }

    @Override
    public void setSolidLines(int width) {
        paint.setStrokeWidth(width);
    }

    @Override
    public void setDashedLines(int width) {
        paint.setStrokeWidth(width);
    }

    @Override
    public void drawRectangle(IXYGraphLib.Rect rect) {
        canvas.drawRect(rect.x, rect.y, rect.x + rect.width, rect.y
                + rect.height, paint);
    }

    @Override
    public void drawRectangle(int x, int y, int width, int height) {
        canvas.drawRect(x, y, x + width, x + height, paint);
    }

    @Override
    public void setNormalFont() {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(labelFontSize);
        mBitmapPaint.setTextSize(labelFontSize);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void setBoldFont() {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(titleFontSize);
        mBitmapPaint.setTextSize(titleFontSize);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    @Override
    public void drawPolyline(int[] points, int num) {
        if (num >= 4) {
            float[] fPoints = new float[num + (num - 4)];
            int count = 0;
            for (int i = 0; i < num; ++i) {
                fPoints[count++] = points[i];
                if ((i % 2) == 1 && i > 1 && i != num - 1) {
                    fPoints[count++] = points[i - 1];
                    fPoints[count++] = points[i];
                }
            }
            canvas.drawLines(fPoints, paint);
        }
    }

    @Override
    public void drawCircle(int x, int y, int radius) {
        canvas.drawCircle(x, y, radius, paint);
    }

    @Override
    public void drawBackground(IXYGraphLib.Rect rect) {
        Drawable drawable = owner.getBackground();
        if (drawable != null) {
            drawable.draw(canvas);
        } else {
            canvas.drawColor(bgColor);
        }
    }

    @Override
    public void drawImage(IXYGraphLib.Rect rect, ButtonImages button,
                          boolean enabled) {
        Bitmap image = null;
        Drawable drawable = imageDrawables[button.ordinal()];
        if (drawable == null) {
            switch (button) {
                case DOWN:
                    image = downImage;
                    break;
                case UP:
                    image = upImage;
                    break;
                case PLUS:
                    image = plusImage;
                    break;
                case MINUS:
                    image = minusImage;
                    break;
                case LEFT:
                    image = leftImage;
                    break;
                case RIGHT:
                    image = rightImage;
                    break;
                case START:
                    image = startImage;
                    break;
                case PAUSE:
                    image = pauseImage;
                    break;
                case STOP:
                    image = stopImage;
                    break;
                case CLEAR:
                    image = wasteImage;
                    break;
                case ZOOM_UP:
                    image = zoomUpImage;
                    break;
                case ZOOM_DOWN:
                    image = zoomDownImage;
                    break;
                case SAVE_CURVE:
                    image = saveCurveImage;
                    break;
                case END:
                    image = endImage;
                    break;
                case POS1:
                    image = pos1Image;
                    break;
                default:
                    break;
            }
            if (image != null) {
                int w = image.getWidth();
                int h = image.getHeight();
                android.graphics.Rect src = new android.graphics.Rect(0, 0, w, h);
                w = Math.min(w, rect.width - 4);
                h = Math.min(h, rect.height - 4);
                android.graphics.Rect dst = new android.graphics.Rect(rect.x + 2,
                        rect.y + 2, rect.x + w, rect.y + h);
                canvas.drawBitmap(image, src, dst, paint);
            }
        } else {
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            float f = Math.max(1.0f * w / rect.width, 1.0f * h / rect.height);
            w = Math.round(w / f);
            h = Math.round(h / f);
            int x = rect.x + rect.width - w;
            drawable.setBounds(x, rect.y, x + w, rect.y + h);
            drawable.setAlpha(enabled ? 255 : 64);
            drawable.draw(canvas);
        }

    }

    @Override
    public void fillRectangle(IXYGraphLib.Rect rect) {
        int save = paint.getColor();
        paint.setColor(currentBgColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect.x, rect.y, rect.x + rect.width, rect.y
                + rect.height, paint);
        paint.setColor(save);
    }

    @Override
    public void drawRoundRectangle(IXYGraphLib.Rect rect, int radius) {
        paint.setStyle(Paint.Style.STROKE);
        RectF r = new RectF(rect.x, rect.y, rect.x + rect.width, rect.y
                + rect.height);
        canvas.drawRoundRect(r, radius, radius, paint);
    }

    @Override
    public void fillRoundRectangle(IXYGraphLib.Rect rect, int radius) {
        int save = paint.getColor();
        paint.setColor(currentBgColor);
        paint.setStyle(Paint.Style.FILL);
        RectF r = new RectF(rect.x + 1, rect.y + 1, rect.x + rect.width - 1,
                rect.y + rect.height - 1);
        canvas.drawRoundRect(r, radius, radius, paint);
        paint.setColor(save);
    }

    @Override
    public void drawText(String label, int x, int y) {
        android.graphics.Rect r = new android.graphics.Rect();
        paint.getTextBounds(label, 0, label.length(), r);
        canvas.drawText(label, x, y + r.height(), paint);
    }

    @Override
    public float getButtonRatio() {
        return buttonRatio;
    }

    public void setFontSize(int labelFontSize, int titleFontSize) {
        this.labelFontSize = labelFontSize;
        this.titleFontSize = titleFontSize;
        defCharSize = new Pt();
        android.graphics.Rect r = new android.graphics.Rect();
        mBitmapPaint.setTextSize(labelFontSize);
        mBitmapPaint.getTextBounds("g", 0, 1, r);
        defCharSize.x = r.width();
        defCharSize.y = (int) Math.round(CHAR_HEIGHT_SCALE_FACTOR * r.height());
        setNormalFont();
    }

    @Override
    public void setBgColor(int r, int g, int b) {
        // Not supported on Android
    }

    @Override
    public void setDrawAreaBgColor(int r, int g, int b) {
        // Not supported on Android
    }

    @Override
    public void setAxisColor(int r, int g, int b) {
        lineColor = Color.argb(250, r, g, b);
    }

    @Override
    public void setCursorColor(int r, int g, int b) {
        // Not supported on Android
    }

    @Override
    public void setCursorBgColor(int r, int g, int b) {
        // Not supported on Android
    }

    @Override
    public void setFontSize(String fontName, int labelFontSize,
                            int titleFontSize) {
        this.setFontSize(labelFontSize, titleFontSize);
    }

    @Override
    public boolean hasOwnButtonDrawing(ButtonImages button) {
        return (imageDrawables[button.ordinal()] != null);
    }


    void setButtonSizeRatio(float r) {
        this.buttonRatio = r;
    }

    void setImageDrawable(ButtonImages button, Drawable drawable) {
        if (drawable != null) {
            imageDrawables[button.ordinal()] = drawable;
        }
    }

    private void loadImages() throws IOException {
        InputStream is;
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/down.png");
        if (is != null) {
            downImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/up.png");
        if (is != null) {
            upImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/left.png");
        if (is != null) {
            leftImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/right.png");
        if (is != null) {
            rightImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/plus.png");
        if (is != null) {
            plusImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/minus.png");
        if (is != null) {
            minusImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/start.png");
        if (is != null) {
            startImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/stop.png");
        if (is != null) {
            stopImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/pause.png");
        if (is != null) {
            pauseImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/waste.png");
        if (is != null) {
            wasteImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/toggleup.png");
        if (is != null) {
            zoomUpImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/toggledown.png");
        if (is != null) {
            zoomDownImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/savecurve.png");
        if (is != null) {
            saveCurveImage = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/leftleft.png");
        if (is != null) {
            pos1Image = BitmapFactory.decodeStream(is);
            is.close();
        }
        is = this.getClass().getClassLoader()
                .getResourceAsStream("images/rightright.png");
        if (is != null) {
            endImage = BitmapFactory.decodeStream(is);
            is.close();
        }
    }
}
