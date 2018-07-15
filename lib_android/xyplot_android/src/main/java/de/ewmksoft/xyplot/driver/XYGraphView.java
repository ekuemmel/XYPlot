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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ewmksoft.xyplot.R;
import de.ewmksoft.xyplot.core.IXYGraphLib;
import de.ewmksoft.xyplot.core.IXYGraphLib.MouseEvent;
import de.ewmksoft.xyplot.core.IXYGraphLibInt;
import de.ewmksoft.xyplot.core.IXYPlot;
import de.ewmksoft.xyplot.core.IXYPlotEvent;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.core.XYPlotData;

public class XYGraphView extends View implements Handler.Callback {
    private Logger logger = LoggerFactory.getLogger(XYGraphView.class);

    private Handler myHandler;
    private IXYGraphLib.Rect rect;
    private IXYPlotEvent xyPlotEvent;
    private IXYGraphView listener;
    private XYPlotData[] dh;
    private XYGraphLibAndroid graphLib;
    private IXYPlot xyPlot;
    private boolean autoFontSize;
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleDetector;
    private boolean shiftPending = false;
    private boolean zoomPending = false;
    private int lastXPos;
    private int zoomCenterPos;
    private long blockMoveUntil;

    private static final int MSG_MOVE = 1;
    private static final int MSG_ZOOM = 2;
    private static final int MSG_FLING = 3;

    private static final String PARAM_XPOS = "xpos";
    private static final String PARAM_ZOOM = "zoom";
    private static final String PARAM_SHIFT = "shift";
    private static final String PARAM_LAST = "last";

    public XYGraphView(Context context) {
        super(context);
        init("", "", new XYPlotData[0], null);
    }

    public XYGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init("", "", new XYPlotData[0], null);
        initAttributes(context, attrs);
    }

    public XYGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init("", "", new XYPlotData[0], null);
        initAttributes(context, attrs);
    }

    public void initAttributes(Context context, AttributeSet attrs) {
        if (context != null && attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.XYGraphView, 0, 0);
            autoFontSize = false;
            Drawable drawable;
            try {
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonDownImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.DOWN, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonUpImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.UP, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonPlusImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.PLUS, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonMinusImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.MINUS, drawable);

                drawable = a.getDrawable(R.styleable.XYGraphView_buttonLeftImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.LEFT, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonRightImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.RIGHT, drawable);

                drawable = a.getDrawable(R.styleable.XYGraphView_buttonStartImage);
                if (drawable != null) {
                    graphLib.setButtonSizeRatio(drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth());
                }
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.START, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonPauseImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.PAUSE, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonStopImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.STOP, drawable);

                drawable = a.getDrawable(R.styleable.XYGraphView_buttonClearImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.CLEAR, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonZoomUpImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.ZOOM_UP, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonZoomDownImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.ZOOM_DOWN, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonSaveImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.SAVE_CURVE, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonPos1Image);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.POS1, drawable);
                drawable = a.getDrawable(R.styleable.XYGraphView_buttonEndImage);
                graphLib.setImageDrawable(IXYGraphLibInt.ButtonImages.END, drawable);
				// set axis color.
				int axisColor = a.getColor(R.styleable.XYGraphView_axis_color, Color.GRAY);
                setAxisColor(axisColor);

            } finally {
                a.recycle();
            }
        }
    }

    public void init(String xTitle, String xUnit, XYPlotData[] xyPlotData, IXYGraphView alistener) {
        if (myHandler == null) {
            myHandler = new Handler(this);
        }
        if (rect == null) {
            rect = new IXYGraphLib.Rect(0, 0, 10, 10);
        }
        if (graphLib == null) {
            graphLib = new XYGraphLibAndroid(this, rect);
            xyPlot = XYPlot.createXYPlot(graphLib);
            gestureDetector = new GestureDetector(getContext(), new XYGestureListener());
            scaleDetector = new ScaleGestureDetector(getContext(), new ZoomListener());
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    performClick();
                    boolean result = false;
                    if (gestureDetector.onTouchEvent(event)) {
                        result = true;
                    } else if (scaleDetector.onTouchEvent(event)) {
                        result = true;
                    }
                    return result;
                }
            });
        }
        graphLib.setBounds(rect);
        this.dh = xyPlotData;
        this.listener = alistener;
        xyPlot.setXRange(0, 0);
        xyPlot.removeDataHandlers();
        for (XYPlotData data : dh) {
            xyPlot.addDataHandler(data);
        }
        xyPlot.setXAxisText(xTitle);
        xyPlot.setXUnitText(xUnit);
        xyPlot.setAllowPauseOnDataClick(true);

        xyPlotEvent = new IXYPlotEvent() {
            public void onEvent(KeyEvent event) {
                switch (event) {
                    case KEY_START:
                        if (listener != null) {
                            listener.setEnabled(true);
                        }
                        break;
                    case KEY_PAUSE:
                        if (listener != null) {
                            listener.setEnabled(false);
                        }
                        for (XYPlotData xyPlotData : dh) {
                            xyPlotData.setPause();
                        }
                        break;
                    case KEY_CLEAR:
                        if (listener != null) {
                            listener.clear();
                        }
                        break;
                    case KEY_SAVE:
                        if (listener != null) {
                            listener.save();
                        }
                        break;
                    case KEY_STOP:
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected boolean onSetAlpha(int alpha) {
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightMeasure, MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int yofs = 3;

        super.onLayout(changed, left, top, right, bottom);
        bottom = bottom - top;
        top = 0;
        rect.x = 0;
        rect.y = top + yofs;
        rect.width = right - left;
        rect.height = bottom - top - yofs;
        graphLib.setBounds(rect);
        if (autoFontSize) {
            int x = Math.min(rect.width, rect.height);
            int labelFontSize = x / 15;
            int titleFontSize = (int) (labelFontSize * 1.2);
            xyPlot.setFontSize(labelFontSize, titleFontSize);
        }
        xyPlot.registerEventHandler(xyPlotEvent);
        setVisibleXRange(0, 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        graphLib.paint(canvas);
    }

    /**
     * Define which value range is visible on the X-axis. The range is [xmax -
     * visibleRange, xmax]
     *
     * @param visibleRange range in x axis units
     */
    public void setVisibleLastX(double visibleRange) {
        if (xyPlot != null) {
            if (dh.length > 0) {
                double xmax = dh[0].getXMax();
                double xmin = Math.max(dh[0].getXMin(), xmax - visibleRange);
                xyPlot.setXRange(xmin, xmax);
                if (xyPlot.isOutdated()) {
                    invalidate();
                }
            }
        }
    }

    /**
     * Set a given range on the x axis to be visible
     *
     * @param xmin minimum x value
     * @param xmax maximum x value
     */
    public void setVisibleXRange(double xmin, double xmax) {
        if (xyPlot != null) {
            xyPlot.setXRange(xmin, xmax);
            if (xyPlot.isOutdated()) {
                invalidate();
            }
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public IXYPlot getXYPlot() {
        return xyPlot;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Bundle b = msg.getData();
        switch (msg.what) {
            case MSG_MOVE:
            case MSG_FLING:
                if (System.currentTimeMillis() > blockMoveUntil) {
                    int shift = b.getInt(PARAM_SHIFT);
                    int last = b.getInt(PARAM_LAST);
                    boolean ContinueMove = xyPlot.moveByPixels(shift);
                    logger.debug("MOVE: shift: {} shiftPending: {} last: {} ContinueMove: {}"
                            , shift, shiftPending, last, ContinueMove);
                    if (last == 0 && ContinueMove) {
                        shift = shift / 2;
                        if (Math.abs(shift) > 2) {
                            Message msg2 = myHandler.obtainMessage();
                            msg2.what = MSG_MOVE;
                            Bundle b2 = msg2.getData();
                            b2.putInt(PARAM_SHIFT, shift);
                            b2.putInt(PARAM_LAST, 0);
                            msg2.setData(b2);
                            myHandler.sendMessageDelayed(msg2, 100);
                        } else {
                            shiftPending = false;
                        }
                    }
                    invalidate();
                } else {
                    logger.debug("Move suppressed by zoom");
                }
                break;
            case MSG_ZOOM:
                int xPos = b.getInt(PARAM_XPOS);
                float factor = 0.000001f * b.getInt(PARAM_ZOOM);
                boolean valid = Math.abs(factor - 1) > 0.0;
                logger.debug("Zoom factor: {} at {} -> {}", factor, xPos, Boolean.toString(valid));
                blockMoveUntil = System.currentTimeMillis() + 500;
                if (valid) {
                    if (xyPlot.zoomAt(xPos, factor)) {
                        invalidate();
                    } else {
                        if (factor < 1) {
                            xyPlot.zoomAt(xPos, factor * 1.1f);
                        } else {
                            xyPlot.zoomAt(xPos, factor * 0.9f);
                        }
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    private class ZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            if (Math.abs(1.0f - scaleFactor) > 0.05) {
                Message msg = myHandler.obtainMessage();
                msg.what = MSG_ZOOM;
                Bundle b = new Bundle();
                int zoom = Math.round(1000000f * scaleFactor);
                int xPos = (int) detector.getFocusX();
                if (0 == zoomCenterPos) {
                    zoomCenterPos = xPos;
                }
                b.putInt(PARAM_XPOS, zoomCenterPos);
                b.putInt(PARAM_ZOOM, zoom);
                logger.debug("onScale zoom {} on xpos {}", zoom, xPos);
                msg.setData(b);
                myHandler.sendMessage(msg);
                zoomPending = true;
                return true;
            }
            return false;
        }
    }

    private class XYGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTapEvent(MotionEvent ev) {
            logger.debug("onDoubleTapEvent {}", ev.toString());
            if (ev.getAction() == 0) {
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                xyPlot.evalMouseEvent(MouseEvent.MOUSEDOUBLETAP, x, y);
                invalidate();
                return true;
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            logger.debug("onSingleTapUp {}", ev.toString());
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            xyPlot.evalMouseEvent(MouseEvent.MOUSESINGLETAP, x, y);
            xyPlot.evalMouseEvent(MouseEvent.MOUSEUP, x, y);
            invalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent ev) {
            shiftPending = false;
            zoomPending = false;
            lastXPos = (int) ev.getX();
            int x = lastXPos;
            int y = (int) ev.getY();
            logger.debug("onDown {}", ev.toString());
            zoomCenterPos = 0;
            xyPlot.evalMouseEvent(MouseEvent.MOUSEDOWN, x, y);
            xyPlot.evalMouseEvent(MouseEvent.MOUSEUP, x, y);
            invalidate();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent ev) {
            logger.debug("onShowPress {}", ev.toString());
        }

        @Override
        public void onLongPress(MotionEvent ev) {
            logger.debug("onLongPress {}", ev.toString());
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            boolean multiTouch = e2.getPointerCount() > 1;
            if (!zoomPending && multiTouch && Math.abs(distanceX) > 3) {
                shiftPending = true;
                logger.debug("onScroll {}:{} {}", distanceX, e1.getX(), e2.getX());
                int delta = (int) distanceX;
                if (Math.abs(delta) > 2) {
                    Message msg = myHandler.obtainMessage();
                    msg.what = MSG_MOVE;
                    Bundle b = new Bundle();
                    b.putInt(PARAM_SHIFT, delta);
                    b.putInt(PARAM_LAST, 1);
                    msg.setData(b);
                    myHandler.sendMessage(msg);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            logger.debug("onFling {}:{} {}", velocityX, e1.toString(), e2.toString());
            if (shiftPending) {
                int factor = Math.max(1, (int) (Math.abs(velocityX / 1000.0f)));
                int delta = factor * (int) (e1.getX() - e2.getX());
                boolean doFling = Math.abs(delta) > 2;
                logger.debug("onFling Factor {} and Delta {}  -> {}", Integer.toString(factor), delta, doFling);
                if (doFling) {
                    Message msg = myHandler.obtainMessage();
                    Bundle b = new Bundle();
                    msg.what = MSG_FLING;
                    b.putInt(PARAM_SHIFT, delta);
                    b.putInt(PARAM_LAST, 0);
                    msg.setData(b);
                    myHandler.sendMessage(msg);
                    return true;
                }
            }
            return false;
        }
    }
    public void setAxisColor(int axisColor) {
        int red = Color.red(axisColor);
        int green = Color.green(axisColor);
        int blue = Color.blue(axisColor);
        graphLib.setAxisColor(red, green, blue);
    }
}
