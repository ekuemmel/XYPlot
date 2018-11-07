/*****************************************************************************
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

package de.ewmksoft.xyplot.core;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.ewmksoft.xyplot.core.IXYGraphLib.MouseEvent;
import de.ewmksoft.xyplot.core.IXYGraphLib.RGB;
import de.ewmksoft.xyplot.core.IXYGraphLib.Rect;
import de.ewmksoft.xyplot.core.IXYGraphLibInt.BgColor;
import de.ewmksoft.xyplot.core.IXYGraphLibInt.ButtonImages;
import de.ewmksoft.xyplot.core.IXYGraphLibInt.FgColor;
import de.ewmksoft.xyplot.core.IXYGraphLibInt.Pt;

/**
 * The XYPlot displays two dimensional data in a XY coordinate system. Multiple
 * plots can be displayed simultaneous. The plot contains mouse sensitive fields
 * to allow zooming and defining a cursor position
 */
public class XYPlot implements IXYGraphLibAdapter, IXYPlot, IXYPlotEvent {
	private static final int ZOOMBOX_LAZY_UPDATE_DELAY = 10;
	private static final int DATA_UPDATE_DELAY = 500;
	private static final int MIN_DATA_UPDATE_DELAY = 10;
	private static final double DEF_Y_DIFF = 0.1;
	private static final int BUTTON_NUM = 10;
	private static final int BUTTON_SPACING = 10;
	private static final int MIN_BUTTON_WIDTH = 44;
	private static final int LEGEND_BOX_BORDER = 4;
	private static final int TICK_LEN = 4;
	private static final int PADDING_TOP = 3;
	private static final int PADDING_BUTTON = 5;
	private static final int PADDING_XR = 5;
	private static final int MOVES_PER_SCREEN = 20;
	private static final int MIN_ZOOM_RECT_SIZE = 10;
	private static final int MIN_MOVE_DETECT_SIZE = 5;
	private static final int POINT_DISTANCE_FOR_CIRCLES = 30;
	private static final int ZOOM_IN_FACTOR = 2;
	private static final String EXPO_STUB = " x 1E";
	private static final boolean GROUPING_USED = false;
	// The next two values define from which exponent the
	// display switches to a scaled format
	private static final int NON_EXPO_MAX = 3;
	private static final int NON_EXPO_MIN = -2;
	// The next two values define from which exponent the
	// scaled format is shown as e.g. 1E4 instead of x 10000
	private static final int EXPO_E_MIN = 6;
	private static final int EXPO_E_MAX = -4;

	public static final int CMD_MOVE_LEFT = 1;
	public static final int CMD_MOVE_RIGHT = 2;
	public static final int CMD_LAST_DATA = 3;
	public static final int CMD_NEXT_DATA = 4;
	public static final int CMD_ZOOM_IN = 5;
	public static final int CMD_ZOOM_OUT = 6;
	public static final int CMD_SHOW_ALL = 7;

	public static final boolean SUPPORT_PARTIAL_DRAW = false;

	private IXYGraphLibInt graphLibInt;
	private ZoomStack zoomStack = new ZoomStack();
	private volatile boolean needsRedraw;
	private volatile boolean zoomCursorNeedsRedraw;
	private volatile long lastNewValueCheck = 0;
	private volatile long lastPaintCallTime;
	private Pt startPointX; // start point of x axis in graphics
	private Pt stopPointX; // stop point of x axis in graphics
	private Pt startPointY; // start point of y axis in graphics
	private Pt stopPointY; // stop point of y axis in graphics
	private Rect cursorBkRectangle;
	private XYPlotData.ScaleData xData;
	private String xUnit;
	private String xAxisText;
	private Rect bounds;
	private Rect pos1Rectangle;
	private Rect leftRectangle;
	private Rect rightRectangle;
	private Rect endRectangle;
	private Rect startRectangle;
	private Rect clearRectangle;
	private Rect zoomYRectangle;
	private Rect saveRectangle;
	private Rect zoomInRectangle;
	private Rect zoomOutRectangle;
	private Rect legendFrameRect;
	private Rect legendButtonRect;
	private boolean blockNextTapEvent;
	private boolean scaleChanged;
	private boolean buttonsChanged;
	private boolean showLegend;
	private boolean expandLegend;
	private double userxmin;
	private double userxmax;
	private double xShiftValue;
	private boolean paused;
	private int lastZoomAtPosition;
	private Pt mouseDownPosition;
	private Pt mouseCurrentPosition;
	private Pt mouseLastPosition;
	private ArrayList<XYPlotData> dataList;
	private int legendWidth;
	private int currentPlotNo;
	private int[] dividers = { 10, 5, 2 };
	private long updateDelay;
	private int buttonWidth;
	private int buttonHeight;
	private int buttonBarHeight;
	private IXYPlotEvent plotEvent;
	@SuppressWarnings("unused")
	private boolean zoomVisibleRange = false;
	private boolean globalYZoomSwitch = false;
	private boolean allowPauseOnDataClick = false;
	private boolean supportZoomBox = false;
	private boolean showStartButton = true;
	private boolean showClearButton = true;
	private boolean showSaveButton = false;
	private boolean showButtonsAndLegend = true;
	private boolean smoothScroll = true;
	private int zoomBoxLacyUpdateDelay = ZOOMBOX_LAZY_UPDATE_DELAY;
	private HashMap<Integer, XYPlotData.MinMax> dataMinMax;
	private HashMap<String, XYPlotData.MinMax> unitMinMax;

	protected XYPlot(IXYGraphLib graphLib) {
		dataMinMax = new HashMap<Integer, XYPlotData.MinMax>();
		unitMinMax = new HashMap<String, XYPlotData.MinMax>();
		this.graphLibInt = graphLib.getInt();
		this.graphLibInt.registerXYPlot(this);
		dataList = new ArrayList<XYPlotData>();
		xData = new XYPlotData.ScaleData();
		bounds = graphLibInt.getBounds();
		setBounds(bounds);
		currentPlotNo = 0;
		plotEvent = this;
		xUnit = new String();
		xAxisText = new String();
		scaleChanged = true;
		needsRedraw = true;
		showLegend = true;
		expandLegend = false;
		blockNextTapEvent = false;
		supportZoomBox = graphLib.hasZoomBox();
		updateDelay = DATA_UPDATE_DELAY;
		calculateScale(xData, AxisType.XAXIS, 0, 1);
	}

	/**
	 * Static method to create an XYPlot instance
	 *
	 * @param graphLib
	 * @return Interface to the XYPlot graph
	 */
	static public IXYPlot createXYPlot(IXYGraphLib graphLib) {
		return new XYPlot(graphLib);
	}

	/**
	 * Register a handler to receive clicks on the start/stop, pause keys.
	 *
	 * @param h
	 *            Receiver object for the events
	 */
	public void registerEventHandler(IXYPlotEvent h) {
		plotEvent = h;
	}

	/**
	 * Create a new data set to be displayed in the XY plot.
	 *
	 * @param max
	 *            Maximum number of XY values to be stored in the set
	 * @param color
	 *            Color of the data to be displayed in the plot
	 * @return Data Handler Object
	 */

	public static XYPlotData createDataHandler(int max, RGB color) {
		XYPlotData result = new XYPlotData(null, max);
		result.setColor(color);
		result.setAutoScale(true);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.xyplot.IXYPlot#getDataHandler()
	 */

	public ArrayList<XYPlotData> getDataHandler() {
		return dataList;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.ewmksoft.xyplot.IXYPlot#addDataHandler(de.ewmksoft.xyplot.XYPlotData)
	 */
	public boolean addDataHandler(XYPlotData dh) {
		XYPlotData.lock();
		boolean result = false;
		dh.setOwner(this);
		result = dataList.add(dh);
		int no = dataList.size() - 1;
		graphLibInt.createColor(no, dh.getColor());
		scaleChanged = true;
		needsRedraw = true;
		if (dh.getCursorPos() >= 0) {
			//paused = true;
		}
		XYPlotData.unlock();
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#removeDataHandler(de.ewmksoft.xyplot.
	 * IXYPlotData )
	 */
	public boolean removeDataHandler(XYPlotData dh) {
		XYPlotData.lock();
		boolean result = dataList.remove(dh);
		XYPlotData.unlock();
		return result;
	}

	public void removeDataHandlers() {
		XYPlotData.lock();
		dataList.clear();
		XYPlotData.unlock();
	}

	/**
	 * This is the default handler for button events send from the plot to its
	 * owner.
	 */
	public void onEvent(KeyEvent event) {
		// Default Handler, does nothing
	}

	/**
	 * Check status of cursor
	 *
	 * @return True if cursor is visible
	 */
	public boolean showsCursor() {
		return (cursorBkRectangle != null);
	}

	/**
	 * Trigger a redraw of the graph
	 *
	 * @return
	 */
	protected void setOutdated() {
		graphLibInt.startDelayedWakeTrigger(updateDelay);
	}

	/**
	 * Set redraw flag
	 *
	 * @return
	 */
	protected void setNeedsRedraw() {
		needsRedraw = true;
	}

	/**
	 * Returns true if the graph needs to be repainted. This call should be used
	 * before drawing to avoid unnecessary updates.
	 *
	 * @return true Graph needs to be repainted false No need to repaint the
	 *         graph
	 */
	public boolean isOutdated() {
		return isOutdated(true);
	}

	/**
	 * Returns true if the graph needs to be repainted. This can be used to
	 * avoid unnecessary updates by only drawing if something has changed.
	 *
	 * @param resetState
	 *            Reset the internal flag by calling this method (true) or check
	 *            only the state (false)
	 * @return true Graph needs to be repainted false No need to repaint the
	 *         graph
	 */
	public boolean isOutdated(boolean resetState) {
		boolean result = this.needsRedraw;
		if (resetState) {
			this.needsRedraw = false;
		}
		long msTime = System.currentTimeMillis();
		if (!isPaused() && !result && msTime - lastNewValueCheck > updateDelay) {
			for (XYPlotData data : dataList) {
				if (data.hasNewValues() /* && data.getCursorPos() < 0 */) {
					result = true;
					break;
				}
			}
			lastNewValueCheck = msTime;
		}
		for (XYPlotData data : dataList) {
			if (data.hasOtherChanges()) {
				scaleChanged = true;
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Get the delay of the repaint operation (see {@link #setUpdateDelay(long)}
	 *
	 * @return Value in [ms]
	 */
	public long getDelayRepaint() {
		return updateDelay;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setUpdateDelay(long)
	 */
	public void setUpdateDelay(long delay) {
		this.updateDelay = Math.max(delay, MIN_DATA_UPDATE_DELAY);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setBkColor(int, int, int)
	 */
	public void setBgColor(int r, int g, int b) {
		graphLibInt.setBgColor(r, g, b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setDrawAreaBkColor(int, int, int)
	 */
	public void setDrawAreaBgColor(int r, int g, int b) {
		graphLibInt.setDrawAreaBgColor(r, g, b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setAxisColor(int, int, int)
	 */
	public void setAxisColor(int r, int g, int b) {
		graphLibInt.setAxisColor(r, g, b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setCursorColor(int, int, int)
	 */
	public void setCursorColor(int r, int g, int b) {
		graphLibInt.setCursorColor(r, g, b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setCursorBgColor(int, int, int)
	 */
	public void setCursorBgColor(int r, int g, int b) {
		graphLibInt.setCursorBgColor(r, g, b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setFontSize(int, int)
	 */
	public void setFontSize(int labelFontSize, int titleFontSize) {
		graphLibInt.setFontSize(labelFontSize, titleFontSize);
	}

	public void setFontSize(String fontName, int labelFontSize, int titleFontSize) {
		graphLibInt.setFontSize(fontName, labelFontSize, titleFontSize);
	}

	/**
	 * Method to be called by the owner of the plot to inform about a key
	 * stroke.
	 *
	 * @return true The key changed the plot display false The key did not
	 *         change anything
	 */
	public boolean evalKey(int key) {
		if (dataList.size() == 0 || startPointX == null) {
			return false;
		}
		boolean result = false;
		if (dataList.get(currentPlotNo) != null) {
			if (key == CMD_SHOW_ALL) {
				zoomScreenOut();
				needsRedraw = true;
				result = true;
			} else if (key == CMD_ZOOM_IN) {
				zoomScreen(dataList.get(currentPlotNo), (xData.lmax - xData.lmin) / ZOOM_IN_FACTOR);
				needsRedraw = true;
				result = true;
			} else if (key == CMD_ZOOM_OUT) {
				zoomScreenOut(dataList.get(currentPlotNo));
				needsRedraw = true;
				result = true;
			} else if (key == CMD_MOVE_LEFT || key == CMD_MOVE_RIGHT) {
				for (XYPlotData data : dataList) {
					if (data == null)
						continue;
					int cursorPos = data.getCursorPos();
					if (cursorPos < 0) {
						cursorPos = data.getVisiblePointNum() - 1;
					}
					if (key == CMD_MOVE_LEFT) {
						int shift = Math.max(data.getVisiblePointNum() / MOVES_PER_SCREEN, 1);
						cursorPos -= shift;
						data.setCursorPos(cursorPos);
						int count = 0;
						while (!isCursorVisible(data) && count++ < 4) {
							moveScreenLeft();
						}
						if (!isCursorVisible(data)) {
							setCursorVisible(data);
						}
					} else {
						int shift = Math.max(data.getVisiblePointNum() / MOVES_PER_SCREEN, 1);
						cursorPos += shift;
						data.setCursorPos(cursorPos);
						int count = 0;
						while (!isCursorVisible(data) && count++ < 4) {
							moveScreenRight();
						}
						if (!isCursorVisible(data)) {
							setCursorVisible(data);
						}
					}
				}
				needsRedraw = true;
				result = true;
			}
		}
		if (key == CMD_NEXT_DATA || key == CMD_LAST_DATA) {
			int pos = currentPlotNo;
			for (XYPlotData data : dataList) {
				if (key == CMD_NEXT_DATA) {
					pos += 1;
				} else {
					pos -= 1;
				}
				if (pos >= dataList.size()) {
					pos = 0;
				}
				if (pos < 0) {
					pos = dataList.size() - 1;
				}
				if (data.getScaleData() != null) {
					setCurrentPlot(pos);
					needsRedraw = true;
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Method to be called by the owner of the plot to inform about mouse click
	 * events.
	 *
	 * @param x
	 *            x coordinate of the mouse down click
	 * @param y
	 *            y coordinate of the mouse down click
	 * @return true: The click was in a click sensitive area. false: The click
	 *         did not change anything because it did not hit a sensitive area
	 */
	public boolean evalMouseEvent(MouseEvent event, int x, int y) {
		if (dataList.size() == 0 || startPointX == null) {
			return false;
		}
		if (!bounds.contains(x, y)) {
			return false;
		}
		boolean result = false;
		double xvalue;
		int x_loc = x - bounds.x;
		int y_loc = y - bounds.y;
		switch (event) {
		case MOUSEDOWN:
			xvalue = screenToScaleX(x_loc);
			int next_no = currentPlotNo;
			int no = -1;
			for (XYPlotData data : dataList) {
				no++;
				Rect legendRect = data.getLegendRect();
				if (legendRect != null && legendRect.contains(x_loc, y_loc)) {
					next_no = no;
					needsRedraw = true;
					result = true;
					break;
				}
				if (legendFrameRect != null && legendFrameRect.contains(x_loc, y_loc)) {
					if (legendButtonRect != null && legendButtonRect.contains(x_loc, y_loc)) {
						needsRedraw = true;
						scaleChanged = true;
						expandLegend = expandLegend ^ true;
						blockNextTapEvent = true;
						result = true;
						break;
					}
					continue;
				}
				if (zoomYRectangle != null && zoomYRectangle.contains(x_loc, y_loc)) {
					globalYZoomSwitch = globalYZoomSwitch ^ true;
					needsRedraw = true;
					scaleChanged = true;
					result = true;
					break;
				}
				if (saveRectangle != null && showSaveButton && saveRectangle.contains(x_loc, y_loc)) {
					plotEvent.onEvent(KeyEvent.KEY_SAVE);
					result = true;
					break;
				}
				if (no == currentPlotNo && startRectangle != null && showStartButton
						&& startRectangle.contains(x_loc, y_loc)) {
					if (!isPaused()) {
						setPaused(true);
						y_loc = stopPointY.y - 1;
						plotEvent.onEvent(KeyEvent.KEY_PAUSE);
					} else {
						zoomScreenOut();
						plotEvent.onEvent(KeyEvent.KEY_START);
					}
					needsRedraw = true;
					result = true;
					break;
				}

				if (no == currentPlotNo) {
					if (clearRectangle != null && clearRectangle.contains(x_loc, y_loc)) {
						plotEvent.onEvent(KeyEvent.KEY_CLEAR);
						// setPaused(true);
						y_loc = stopPointY.y - 1;
						needsRedraw = true;
						result = true;
						break;
					}
					if (pos1Rectangle != null && pos1Rectangle.contains(x_loc, y_loc)) {
						moveScreenPos1();
						needsRedraw = true;
						result = true;
						break;
					}
					if (leftRectangle != null && leftRectangle.contains(x_loc, y_loc)) {
						moveScreenLeft();
						needsRedraw = true;
						result = true;
						break;
					}
					if (rightRectangle != null && rightRectangle.contains(x_loc, y_loc)) {
						moveScreenRight();
						needsRedraw = true;
						result = true;
						break;
					}
					if (endRectangle != null && endRectangle.contains(x_loc, y_loc)) {
						moveScreenEnd();
						needsRedraw = true;
						result = true;
						break;
					}
					if (zoomOutRectangle != null && zoomOutRectangle.contains(x_loc, y_loc)) {
						zoomScreenOut(data);
						needsRedraw = true;
						result = true;
						break;
					}
					if (zoomInRectangle != null && zoomInRectangle.contains(x_loc, y_loc)) {
						zoomScreen(data, (xData.lmax - xData.lmin) / ZOOM_IN_FACTOR);
						needsRedraw = true;
						result = true;
						break;
					}
				}
			}
			if (!result && supportZoomBox) {
				result = checkDrawAreaHit(x, y);
			}
			if (next_no != currentPlotNo) {
				setCurrentPlot(next_no);
			}
			break;
		case MOUSESINGLETAP:
			if (!blockNextTapEvent) {
				result = checkDrawAreaHit(x, y);
			}
			blockNextTapEvent = false;
			break;
		case MOUSEDOUBLETAP:
			result = checkDrawAreaHit(x, y);
			if (result) {
				zoomIn();
			}
			break;
		case MOUSEUP:
			mouseDownPosition = null;
			mouseCurrentPosition = null;
			if (cursorBkRectangle != null) {
				XYPlotData data = dataList.get(currentPlotNo);
				if (data != null) {
					int xl = cursorBkRectangle.x - bounds.x;
					int xr = cursorBkRectangle.x + cursorBkRectangle.width - bounds.x;
					xvalue = screenToScaleX(xl);
					int pos1 = data.locateIndexFromXValue(xvalue);
					if (pos1 >= 0) {
						double xvalue2 = screenToScaleX(xr);
						int pos2 = data.locateIndexFromXValue(xvalue2);
						if (cursorBkRectangle.width > MIN_ZOOM_RECT_SIZE && Math.abs(pos1 - pos2) > 2) {
							data.setCursorPos((pos1 + pos2) / 2);
							zoomScreen(data, Math.abs(xvalue - xvalue2));
						}
					}
				}
				graphLibInt.startDelayedWakeTrigger(zoomBoxLacyUpdateDelay);
				cursorBkRectangle = null;
			}
			needsRedraw = true;
			result = true;
			break;
		case MOUSEMOVE:
			long delay = System.currentTimeMillis() - lastPaintCallTime;
			if (mouseDownPosition != null) {
				if (mouseCurrentPosition == null) {
					cursorBkRectangle = new Rect(mouseDownPosition.x, mouseDownPosition.y, 1, 1);
				}
				mouseCurrentPosition = new Pt(x, y);
				if (delay > zoomBoxLacyUpdateDelay) {
					if (Math.abs(mouseLastPosition.x - x) >= MIN_MOVE_DETECT_SIZE
							|| Math.abs(mouseLastPosition.y - y) >= MIN_MOVE_DETECT_SIZE) {
						mouseLastPosition = mouseCurrentPosition;
						result = true;
					}
					zoomCursorNeedsRedraw = true;
				}
				graphLibInt.startDelayedWakeTrigger(zoomBoxLacyUpdateDelay);
			}
			break;
		}
		return result;
	}

	public void moveLeft() {
		evalKey(CMD_MOVE_LEFT);
	}

	public void moveRight() {
		evalKey(CMD_MOVE_RIGHT);
	}

	public boolean moveByPixels(int pixelNum) {
		boolean result = false;
		if (currentPlotNo < 0 || currentPlotNo >= dataList.size()) {
			return result;
		}
		XYPlotData data = dataList.get(currentPlotNo);
		if (data != null) {
			if (xData.vfactor != 0) {
				double shift = (1.0 * pixelNum) / xData.vfactor;
				double space = 50 / xData.vfactor;
				double xmin = data.getXMin() - space;
				double xmax = data.getXMax() + space;
				double min = xData.lmin + shift;
				double max = xData.lmax + shift;
				double diff = max - min;
				result = true;
				if (min < xmin) {
					min = xmin;
					max = min + diff;
					result = false;
				}
				if (max > xmax) {
					max = xmax;
					min = max - diff;
					result = false;
				}
				XYPlotData.lock();
				scaleChanged |= calculateScale(xData, AxisType.XAXIS, min, max);
				if (scaleChanged) {
					needsRedraw = true;
				}
				XYPlotData.unlock();
			}
		}
		return result;
	}

	/**
	 *
	 */
	public void zoomIn() {
		XYPlotData xyPlotData = dataList.get(currentPlotNo);
		if (xyPlotData != null) {
			zoomScreen(xyPlotData, (xData.lmax - xData.lmin) / ZOOM_IN_FACTOR);
			needsRedraw = true;
		}
	}

	/**
	 * 
	 */
	public boolean zoomAt(int position, double factor) {
		boolean result = false;
		if (dataList.size() > currentPlotNo) {
			XYPlotData xyPlotData = dataList.get(currentPlotNo);
			if (xyPlotData != null) {
				XYPlotData.lock();
				double xposL = screenToScaleX(position);
				double f = Math.max(0.01d, factor);
				f = Math.min(1.99d, f);
				double xmin = xposL - (xposL - xData.lmin) / f;
				double xmax = xposL + (xData.lmax - xposL) / f;
				if (position != lastZoomAtPosition) {
					lastZoomAtPosition = position;
					int pos = xyPlotData.locateIndexFromXValue(xposL);
					xyPlotData.setCursorPos(pos);
				}
				zoomStack.clear();
				result = zoomScreen(xyPlotData, xmin, xmax);
				needsRedraw = true;
				XYPlotData.unlock();
			}
		}
		return result;
	}

	/**
	 * The plot can host multiple XY data sets. However there is only one Y-Axis
	 * always showing the data of one selected set at a time. With this call one
	 * data set can be selected. A data set must have been created before it can
	 * be selected.
	 *
	 * @param no
	 *            Number of the data set
	 */
	public void setCurrentPlot(int no) {
		if (no < dataList.size() && dataList.get(no).getScaleData() != null) {
			currentPlotNo = no;
			scaleChanged = true;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#getXUnitText()
	 */
	public String getXUnitText() {
		return xUnit;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setXUnitText(java.lang.String)
	 */
	public void setXUnitText(String s) {
		xUnit = s;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#getXAxisText()
	 */
	public String getXAxisText() {
		return xAxisText;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setXAxisText(java.lang.String)
	 */
	public void setXAxisText(String s) {
		xAxisText = s;
	}

	/**
	 * Set the boundaries of the component in absolute coordinates of the canvas
	 *
	 * @param bounds
	 *            Outer rectangle defining the plotting are
	 */
	public void setBounds(Rect bounds) {
		this.bounds = bounds;
		calculateButtonSize();
		scaleChanged = true;
	}

	/**
	 * Get the area which needs to be redrawn. If nothing needs to be redrawn it
	 * return null.
	 *
	 * @return Rectangle or null
	 */
	public Rect getRedrawArea() {
		Rect result = null;
		if (cursorBkRectangle != null) {
			long delay = System.currentTimeMillis() - lastPaintCallTime;
			if (delay >= zoomBoxLacyUpdateDelay) {
				zoomCursorNeedsRedraw = true;
			}
			if (zoomCursorNeedsRedraw) {
				result = cursorBkRectangle;
				zoomCursorNeedsRedraw = false;
			}
		} else if (isOutdated()) {
			result = bounds;
		}
		return result;
	}

	/**
	 * Get width of plot are
	 *
	 * @return Width of plot are
	 */
	public int width() {
		return bounds.width;
	}

	/**
	 * Get height of plot are
	 *
	 * @return Height of plot area
	 */
	public int height() {
		return bounds.height;
	}

	/**
	 * Set the range for the Y axis. The class takes this values to calculate a
	 * proper scaling for the Y axis
	 *
	 * @param ymin
	 *            Minimum y value to be displayed
	 * @param ymax
	 *            Maximum y value to be displayed
	 */
	public void setYRange(XYPlotData data, double ymin, double ymax) {
		XYPlotData.lock();
		scaleChanged |= calculateScale(data.getScaleData(), AxisType.YAXIS, ymin, ymax);
		XYPlotData.unlock();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setXRange(double, double)
	 */
	public void setXRange(double xmin, double xmax) {
		XYPlotData.lock();
		userxmin = xmin;
		userxmax = xmax;
		if (!isPaused()) {
			scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
			if (scaleChanged) {
				setOutdated();
				needsRedraw = true;
			}
		}
		XYPlotData.unlock();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#getXMin()
	 */
	public double getXMin() {
		return xData.vmin;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#getXMax()
	 */
	public double getXMax() {
		return xData.vmax;
	}

	/**
	 * Paint the plot to the internal image
	 */
	public void paintGraph() {
		if (dataList.size() == 0) {
			return;
		}
		XYPlotData.lock();
		int no = -1;
		dataMinMax.clear();
		unitMinMax.clear();
		String globalZoomedUnit = "";
		for (XYPlotData data : dataList) {
			no++;
			String unit = data.getUnit();
			XYPlotData.MinMax temporaryMinMax = null;
			if (globalYZoomSwitch && no == currentPlotNo) {
				int minIndex = data.locateIndexFromXValue(xData.vmin);
				int maxIndex = data.locateIndexFromXValue(xData.vmax);
				temporaryMinMax = data.getYRange(minIndex, maxIndex);
				globalZoomedUnit = unit;
			} else {
				temporaryMinMax = data.getYRange();
			}
			dataMinMax.put(no, temporaryMinMax);
			XYPlotData.MinMax totalMinMax = unitMinMax.get(unit);
			double min = temporaryMinMax.getMin();
			double max = temporaryMinMax.getMax();
			if (totalMinMax != null) {
				double totalMin = Math.min(min, totalMinMax.getMin());
				double totalMax = Math.max(max, totalMinMax.getMax());
				unitMinMax.put(unit, data.new MinMax(totalMin, totalMax));
			} else
				unitMinMax.put(unit, data.new MinMax(min, max));
		}
		for (XYPlotData data : dataList) {
			XYPlotData.ScaleData sd = data.getScaleData();
			XYPlotData.MinMax minMax;
			String unit = data.getUnit();
			if (globalYZoomSwitch && dataMinMax.containsKey(currentPlotNo) && unit.equals(globalZoomedUnit)) {
				minMax = dataMinMax.get(currentPlotNo);
			} else {
				minMax = unitMinMax.get(unit);
			}
			scaleChanged |= calculateScale(sd, AxisType.YAXIS, minMax.getMin(), minMax.getMax());
		}
		if (scaleChanged) {
			calculateAxisPosition();
			paintBackground();
			paintPlotArea(true);
			XYPlotData currentData = dataList.get(currentPlotNo);
			paintAxis(currentData);
			for (XYPlotData data : dataList) {
				data.setLastDrawPointNum(0);
			}
		} else {
			paintPlotArea(false);
		}
		// ************** Draw data and legend ********************
		// Paint all curves but draw the selected one as latest
		no = currentPlotNo;
		for (int i = 0; i < dataList.size(); ++i) {
			no = (no + 1) % dataList.size();
			XYPlotData data = dataList.get(no);
			// Draw data, but only the missing points
			int start = data.getLastDrawPointNum();
			if (start > 0)
				start--;
			int stop = data.length();
			drawXYData(no, data, start, stop);
		}
		if (showButtonsAndLegend) {
			if (scaleChanged || buttonsChanged) {
				paintClickAreas(currentPlotNo);
			}

			no = 0;
			for (XYPlotData data : dataList) {
				calculateLegendBox(no, data);
				no++;
			}
			paintLegendArea();
			no = 0;
			for (XYPlotData data : dataList) {
				paintLegendBox(no, data);
				no++;
			}
		}
		scaleChanged = false;
		buttonsChanged = false;
		XYPlotData.unlock();
	}

	/**
	 * Paint the cursor into the plot
	 */
	public void paintCursor() {
		if (dataList.size() == 0) {
			return;
		}
		XYPlotData data = dataList.get(currentPlotNo);
		if (data == null)
			return;
		XYPlotData.ScaleData sd = data.getScaleData();
		if (xData == null || sd == null)
			return;
		int cursorPos = data.getCursorPos();
		if (cursorPos >= 0 && cursorPos < data.length()) {
			graphLibInt.setBgColor(BgColor.CURSORBG);
			graphLibInt.setFgColor(FgColor.CURSOR);
			graphLibInt.setSolidLines(1);
			XYPlotData.DataValue dv = data.getValue(cursorPos);
			Pt p1 = scaleToScreen(currentPlotNo, dv.x(), dv.y());
			if (mouseDownPosition != null && mouseCurrentPosition != null) {
				int xPos = Math.min(mouseDownPosition.x, mouseCurrentPosition.x) - 1;
				int yPos = mouseDownPosition.y;
				int width = Math.abs(mouseDownPosition.x - mouseCurrentPosition.x) + 2;
				int height = 4;
				cursorBkRectangle = new Rect(xPos, yPos, width, height);
				graphLibInt.drawRectangle(cursorBkRectangle.x + 1, cursorBkRectangle.y + 1, cursorBkRectangle.width - 3,
						cursorBkRectangle.height - 3);
			} else {
				if (p1.x >= startPointX.x && p1.x <= stopPointX.x) {
					int x = p1.x + bounds.x;
					int y = p1.y + bounds.y;
					int y1 = startPointY.y + bounds.y;
					int y2 = stopPointY.y + bounds.y;
					graphLibInt.setSolidLines(2);
					graphLibInt.drawLine(x - 1, y1, x - 1, y2);
					graphLibInt.setFgPlotColor(currentPlotNo);
					graphLibInt.drawCircle(x, y, 5);
					if (showButtonsAndLegend) {
						double xvalue = dv.x();
						double yvalue = dv.y();
						NumberFormat nf = NumberFormat.getInstance();
						nf.setGroupingUsed(GROUPING_USED);
						nf.setMaximumFractionDigits(xData.nk + 1);
						nf.setMinimumFractionDigits(xData.nk + 1);
						String xs = nf.format(xvalue);
						String ys = formatValue(false, sd, yvalue);
						String label = xs.trim() + " / " + ys.trim();
						Pt pt = graphLibInt.getStringExtends(label);
						int w = pt.x + 10;
						int h = pt.y + 5;
						if (p1.x + w > stopPointX.x) {
							x -= w;
						}
						Rect r = new Rect(x + 1, y1 - h - 2, w, h);
						graphLibInt.setSolidLines(1);
						graphLibInt.drawRoundRectangle(r, 8);
						graphLibInt.fillRoundRectangle(r, 8);
						graphLibInt.setFgPlotColor(currentPlotNo);
						graphLibInt.setFgColor(FgColor.AXIS);
						graphLibInt.drawText(label, r.x + 3, r.y + 4);
					}
				}
			}
		}
		lastPaintCallTime = System.currentTimeMillis();
	}

	/**
	 * Returns the current value of the zoom box delay.
	 *
	 * @return Current value [ms]
	 */
	public int getZoomBoxLacyUpdateDelay() {
		return zoomBoxLacyUpdateDelay;
	}

	/**
	 * This value in [ms] is used to delay the update of the zoom box when
	 * changing the size. It is useful to set a higher value on devices with a
	 * lower graphic performance.
	 *
	 * @param zoomBoxLacyUpdateDelay
	 */
	public void setZoomBoxLacyUpdateDelay(int zoomBoxLacyUpdateDelay) {
		this.zoomBoxLacyUpdateDelay = zoomBoxLacyUpdateDelay;
	}

	/**
	 * Returns the current state of the flag.
	 *
	 * @return Current State
	 */
	public boolean isAllowPauseOnDataClick() {
		return allowPauseOnDataClick;
	}

	/**
	 * Allow that a click into the graph switches the state to pause mode.
	 *
	 * @param allowPauseOnDataClick
	 *            True to pause graph if clicked
	 */
	public void setAllowPauseOnDataClick(boolean allowPauseOnDataClick) {
		this.allowPauseOnDataClick = allowPauseOnDataClick;
	}

	/**
	 * Expand (true) ore collapse (false) the legend box
	 *
	 * @param value
	 */
	public void setLegendExpanded(boolean value) {
		XYPlotData.lock();
		boolean changed = (value != expandLegend);
		if (changed) {
			scaleChanged = true;
			expandLegend = value;
			needsRedraw = true;
		}
		XYPlotData.unlock();
		if (changed && showLegend) {
			setOutdated();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setSmoothScroll(boolean)
	 */
	public void setSmoothScroll(boolean smoothScroll) {
		this.smoothScroll = smoothScroll;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setSaveButtonVisisble(boolean)
	 */
	public void setSaveButtonVisisble(boolean value) {
		XYPlotData.lock();
		boolean changed = (value != showSaveButton);
		if (changed) {
			scaleChanged = true;
			showSaveButton = value;
			needsRedraw = true;
		}
		XYPlotData.unlock();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setRunPauseButtonVisisble(boolean)
	 */
	public void setStartButtonVisisble(boolean value) {
		XYPlotData.lock();
		boolean changed = (value != showStartButton);
		if (changed) {
			scaleChanged = true;
			showStartButton = value;
			needsRedraw = true;
		}
		XYPlotData.unlock();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setDeleteButtonVisisble(boolean)
	 */
	public void setClearButtonVisisble(boolean value) {
		XYPlotData.lock();
		boolean changed = (value != showClearButton);
		if (changed) {
			scaleChanged = true;
			showClearButton = value;
			needsRedraw = true;
		}
		XYPlotData.unlock();
	}

	/**
	 * Set the legend box to visible
	 *
	 * @param value
	 */
	public void setLegendVisisble(boolean value) {
		XYPlotData.lock();
		boolean changed = (value != showLegend);
		if (changed) {
			scaleChanged = true;
			showLegend = value;
			expandLegend = false;
			needsRedraw = true;
		}
		XYPlotData.unlock();
		if (changed) {
			setOutdated();
		}
	}

	/**
	 * Set graph in paused mode. In this mode the cursor appears Note: In paused
	 * mode, a setXRange() call is not executed immediately.
	 *
	 * @param zoomed
	 */
	public void setPaused(boolean paused) {
		if (!this.paused) {
			for (XYPlotData data : dataList) {
				data.setCursorPos(data.getLastDrawPointNum());
			}
		}
		this.paused = paused;
		buttonsChanged = true;
	}

	@Override
	public void setAxisLabels(boolean value) {
		showButtonsAndLegend = value;
	}

	/**
	 * Paint the curve
	 *
	 * @param no
	 *            Number of the curve
	 * @param data
	 *            Plot data
	 * @param start
	 *            Start index
	 * @param stop
	 *            Stop index
	 * @return Number of visible points
	 */
	private int drawXYData(int no, XYPlotData data, int start, int stop) {
		XYPlotData.ScaleData sd = data.getScaleData();
		if (xData == null || sd == null || start > stop)
			return 0;

		// Draw data
		int lineWidth = 2;
		if (no == currentPlotNo)
			lineWidth = 3;
		graphLibInt.setSolidLines(lineWidth);
		graphLibInt.setFgPlotColor(no);
		int pointNum = 0;
		if (data.length() > 1) {
			Pt p1 = null;
			int visible = data.getVisiblePointNum();
			int[] points = new int[(stop - start + 1) * 2];
			for (int i = start; i < stop; ++i) {
				XYPlotData.DataValue dv = data.getValue(i);
				if (i == start && dv.border())
					continue;
				boolean drawIt = (p1 == null);
				Pt p2 = scaleToScreen(no, dv.x(), dv.y());
				if (p1 != null && clipDataLine(p1, p2)) {
					if (pointNum >= 2)
						pointNum -= 2;
					points[pointNum++] = p1.x;
					points[pointNum++] = p1.y;
					points[pointNum++] = p2.x;
					points[pointNum++] = p2.y;
					if (isPaused()
							// && data.getVisiblePointNum() <
							// MAX_POINT_NUM_FOR_CIRCLES)
							// {
							&& (p2.x - p1.x) > POINT_DISTANCE_FOR_CIRCLES) {
						graphLibInt.drawCircle(p1.x, p1.y, 3);
					}
					drawIt = true;
				}
				if (dv.border() || !drawIt) {
					graphLibInt.drawPolyline(points, pointNum);
					visible += pointNum / 2;
					pointNum = 0;
					if (dv.border()) {
						p2 = null;
					}
				}
				p1 = p2;
			}
			graphLibInt.drawPolyline(points, pointNum);
			visible += pointNum / 2;
			data.setVisiblePointNum(visible);
		}
		return pointNum / 2;
	}

	private boolean checkDrawAreaHit(int x, int y) {
		boolean result = false;
		int x_loc = x - bounds.x;
		int y_loc = y - bounds.y;
		if ((legendFrameRect != null && legendFrameRect.contains(x_loc, y_loc)) || y_loc >= stopPointY.y) {
			result = false;
		} else {
			if (x >= startPointX.x && x <= stopPointX.x && y >= startPointY.y) {
				double xvalue = screenToScaleX(x_loc);
				int no = -1;
				int prev_diff = bounds.height;
				for (XYPlotData data : dataList) {
					no++;
					if (xvalue > xData.vmax)
						xvalue = xData.vmax;
					if (xvalue < xData.vmin)
						xvalue = xData.vmin;
					if (data.length() >= 2) {
						int pos = data.locateIndexFromXValue(xvalue);
						if ((allowPauseOnDataClick || isPaused()) && pos >= 0) {
							Pt p = scaleToScreenX0(no, data.getValue(pos).y());
							int ydiff = Math.abs(y_loc - p.y);
							if (ydiff < prev_diff) {
								prev_diff = ydiff;
							}
							if (mouseDownPosition == null) {
								mouseDownPosition = new Pt(x, y);
								setPaused(true);
								plotEvent.onEvent(KeyEvent.KEY_PAUSE);
								mouseLastPosition = mouseDownPosition;
							}
							data.setCursorPos(pos);
						}
						result = true;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Paint the background of the graph
	 */
	private void paintBackground() {
		/***************** Draw boxes ****************************/
		Rect r = new Rect(0, 0, bounds.width, bounds.height);
		graphLibInt.setBgColor(BgColor.BG);
		graphLibInt.drawBackground(r);
		if (showButtonsAndLegend) {
			// Draw box for buttons
			graphLibInt.setFgColor(FgColor.AXIS);
			graphLibInt.setSolidLines(1);
			r.width -= 8;
			r.x += 4;
			r.y = r.height - buttonBarHeight;
			r.height = buttonHeight + 4;
			graphLibInt.setBgColor(BgColor.BG);
			graphLibInt.drawRoundRectangle(r, 4);
			graphLibInt.fillRoundRectangle(r, 4);
		}
	}

	/**
	 * Draw the surrounding box of the legend
	 */
	private void paintLegendArea() {
		graphLibInt.setFgColor(FgColor.AXIS);
		int y = PADDING_TOP;
		int w = buttonWidth;
		int h = buttonHeight;
		if (expandLegend) {
			w = legendWidth;
			for (XYPlotData data : dataList) {
				Rect r = data.getLegendRect();
				if (r != null) {
					h += r.height + 1;
				}
			}
		}
		int x = bounds.width - 4 - w;
		legendFrameRect = null;
		if (showLegend) {
			int padding = 1;
			legendFrameRect = new Rect(x, y, w, h);
			legendButtonRect = new Rect(x + 1 + padding, y + 1 + padding, w - 4 - padding, buttonHeight - 4 - padding);
			graphLibInt.setBgColor(BgColor.LEGENDBG);
			graphLibInt.setDashedLines(1);
			if (expandLegend) {
				graphLibInt.fillRoundRectangle(legendFrameRect, 8);
				graphLibInt.drawRoundRectangle(legendFrameRect, 8);
				drawImageButton(legendButtonRect, ButtonImages.UP, true);
			} else {
				drawImageButton(legendButtonRect, ButtonImages.DOWN, true);
			}
		}
	}

	/**
	 * Calculate size and position of legend box
	 *
	 * @param no
	 */
	private void calculateLegendBox(int no, XYPlotData data) {
		if (legendFrameRect == null)
			return;
		if (expandLegend) {
			graphLibInt.setNormalFont();
			Pt fontSize = graphLibInt.getAverageCharacterSize();
			int x = bounds.width - legendWidth;
			int itemHeight = fontSize.y * 2 + LEGEND_BOX_BORDER;
			int y = legendFrameRect.y + no * itemHeight + 4 + legendButtonRect.height;
			data.setLegendRect(new Rect(x, y, legendWidth - 2 * LEGEND_BOX_BORDER, itemHeight));
		} else {
			data.setLegendRect(null);
		}
	}

	/**
	 * Draw the legend box for one channel
	 */
	private void paintLegendBox(int no, XYPlotData data) {
		if (legendFrameRect == null)
			return;
		Rect r = data.getLegendRect();
		if (r != null) {
			graphLibInt.setNormalFont();
			graphLibInt.setFgColor(FgColor.AXIS);
			graphLibInt.setBgColor(BgColor.LEGENDBG);
			graphLibInt.setSolidLines(1);
			if (no == currentPlotNo) {
				graphLibInt.setBgColor(BgColor.LEGENDSELECTBG);
			}
			graphLibInt.fillRectangle(r);
			String s = trimText(data.getLegendText(), r.width);
			graphLibInt.drawText(s, r.x, r.y);
			graphLibInt.setBgColor(BgColor.PLOTBG);
			graphLibInt.setFgPlotColor(no);
			graphLibInt.drawRectangle(new Rect(r.x, r.y + r.height - 3, r.width, 2));
			graphLibInt.setFgColor(FgColor.AXIS);
			if (data.length() > 0) {
				int pos = data.length() - 1;
				if (isPaused()) {
					pos = data.getCursorPos();
				}
				String label = formatValueUnit(false, data, data.getValue(pos).y());
				label = trimText(label, r.width);
				graphLibInt.setBgColor(BgColor.LEGENDBG);
				if (no == currentPlotNo) {
					graphLibInt.setBgColor(BgColor.LEGENDSELECTBG);
				}
				graphLibInt.setNormalFont();
				graphLibInt.drawText(label, r.x, r.y + r.height / 2 - 2);
			}
		}
	}

	/**
	 * Draw plot area background
	 *
	 * @param full
	 *            true: paint from scratch false: paint only the additional area
	 *            at the right end
	 */
	private void paintPlotArea(boolean full) {
		int w = stopPointX.x - startPointX.x;
		int h = stopPointY.y - startPointY.y + 1;
		int x = startPointY.x;
		int y = startPointY.y - 1;
		graphLibInt.setBgColor(BgColor.PLOTBG);
		double x_max = XYPlotData.MIN_DOUBLE_VALUE;
		if (!full) {
			w = 0;
			for (XYPlotData data : dataList) {
				XYPlotData.ScaleData sd = data.getScaleData();
				if (sd != null && data.length() > 0) {
					XYPlotData.DataValue dv = data.getValue(0);
					x_max = Math.max(x_max, dv.x());
					Pt p = scaleToScreenY0(dv.x());
					if (p.x > stopPointX.x)
						p.x = stopPointX.x;
					w = Math.max(p.x - startPointY.x, w);
				}
			}
		}
		if (w > 0) {
			Rect r = new Rect(x + 1, y, w - 1, h);
			graphLibInt.fillRectangle(r);
			// Draw horizontal grid lines
			graphLibInt.setSolidLines(1);
			graphLibInt.setFgColor(FgColor.GRID);
			graphLibInt.setDashedLines(1);
			XYPlotData currentData = dataList.get(currentPlotNo);
			if (currentData != null) {
				XYPlotData.ScaleData sd = currentData.getScaleData();
				if (sd != null) {
					double q = Math.pow(10, sd.dexpo);
					for (int i = 0; i < sd.ticks; ++i) {
						double yv = sd.tmin + i * sd.tdelta;
						Pt p = scaleToScreenX0(currentPlotNo, yv * q);
						if (p.y != stopPointY.y) {
							graphLibInt.drawLine(x + 1, p.y, x + w, p.y);
						}
					}
				}
			}
			// Draw the left most part of the curve if necessary
			int no = 0;
			for (XYPlotData data : dataList) {
				int num = 0;
				for (int i = 0; i < data.length() && data.getValue(i).x() < x_max; ++i) {
					num++;
				}
				if (num != 0)
					drawXYData(no, data, 0, num + 1);
				no++;
			}
		}
	}

	/**
	 * Draw the XY axis
	 */
	private void paintAxis(XYPlotData data) {
		Pt p1;
		Pt p2;
		XYPlotData.ScaleData sd = data.getScaleData();
		if (xData == null || sd == null) {
			return;
		}
		/**************** Draw Y axis ***************************/
		graphLibInt.setBoldFont();
		p1 = new Pt(startPointY.x, startPointY.y - 2);
		p2 = new Pt(stopPointY.x, stopPointY.y);
		graphLibInt.setSolidLines(1);
		graphLibInt.setBgColor(BgColor.BG);
		graphLibInt.setFgColor(FgColor.AXIS);
		graphLibInt.drawLine(p1.x, p1.y, p2.x, p2.y);
		if (showButtonsAndLegend) {
			String ytext = data.getLegendText();
			String unit = data.getUnit();
			if (unit.length() > 0) {
				unit = " [" + unit + "]";
			}
			ytext += unit;
			Pt tw = graphLibInt.getStringExtends(ytext);
			// Draw current curve title in header
			// int titleXpos = (startPointX.x + stopPointX.x - tw.x) / 2;
			int titleXpos = startPointX.x + 40;
			int titleYPos = PADDING_TOP;
			Rect titleColorBox = new Rect(titleXpos - 30, titleYPos, 20, 20);
			graphLibInt.drawText(ytext, titleXpos, titleYPos);
			graphLibInt.setBgPlotColor(currentPlotNo);
			graphLibInt.fillRectangle(titleColorBox);
			graphLibInt.setBgColor(BgColor.BG);
			graphLibInt.setNormalFont();
			int yexp = (int) sd.dexpo;
			if (yexp != 0) {
				ytext = createExpoString(yexp);
				tw = graphLibInt.getStringExtends(ytext);
				graphLibInt.drawText(ytext, startPointY.x - tw.x - 2, 4);
			}
		}
		double q = Math.pow(10, sd.dexpo);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(GROUPING_USED);
		nf.setMaximumFractionDigits(sd.nk);
		nf.setMinimumFractionDigits(sd.nk);
		if (showButtonsAndLegend) {
			List<String> labels = data.getSwitchLabels();
			if (labels.size() > 0) {
				for (int i = 0; i < sd.ticks; ++i) {
					int y = (int) Math.round(sd.tmin + sd.tdelta * i);
					if (y >= 0 && y < labels.size()) {
						Pt p = scaleToScreenX0(currentPlotNo, y * q);
						p1 = new Pt(p.x, p.y);
						p2 = new Pt(p.x - TICK_LEN, p.y);
						graphLibInt.setBgColor(BgColor.LEGENDSELECTBG);
						String label = labels.get(y);
						Pt shift = graphLibInt.getStringExtends(label);
						int tx = Math.max(p2.x - shift.x - TICK_LEN, 3);
						int ty = p2.y - shift.y;
						graphLibInt.drawRectangle(tx - 2, ty - 6, shift.x + 8, shift.y + 6);
						graphLibInt.setFgColor(FgColor.AXIS);
						graphLibInt.drawText(label, tx, ty);
					}
				}

			} else {
				for (int i = 0; i < sd.ticks; ++i) {
					double y = sd.tmin + sd.tdelta * i;
					Pt p = scaleToScreenX0(currentPlotNo, y * q);
					p1 = new Pt(p.x, p.y);
					p2 = new Pt(p.x - TICK_LEN, p.y);
					graphLibInt.setFgColor(FgColor.AXIS);
					graphLibInt.setSolidLines(1);
					graphLibInt.drawLine(p1.x, p1.y, p2.x, p2.y);
					nf.setMaximumFractionDigits(sd.nk);
					nf.setMinimumFractionDigits(sd.nk);
					String label = nf.format(y);
					if (label.endsWith(".")) {
						label = label.replace('.', ' ').trim();
					}
					if (label.equals("-0")) {
						label = "0";
					}
					Pt shift = graphLibInt.getStringExtends(label);
					graphLibInt.drawText(label, p2.x - shift.x - TICK_LEN, p2.y - shift.y / 2);
				}
			}
		}

		/**************** Draw X axis ***************************/
		p1 = new Pt(startPointX.x, stopPointY.y);
		p2 = new Pt(stopPointX.x, stopPointY.y);
		graphLibInt.setFgColor(FgColor.AXIS);
		String s = xAxisText + " [" + xUnit + "]";
		int xexp = (int) xData.dexpo;
		if (xexp != 0) {
			s = s + EXPO_STUB + xexp;
		}
		int unitPos = bounds.width - graphLibInt.getStringExtends(s).x - 10;
		graphLibInt.drawText(s, unitPos, p2.y + TICK_LEN);
		graphLibInt.setSolidLines(1);
		graphLibInt.drawLine(p1.x, p1.y, p2.x, p2.y);
		q = Math.pow(10, xData.dexpo);
		nf = NumberFormat.getInstance();
		nf.setGroupingUsed(GROUPING_USED);
		nf.setMaximumFractionDigits(xData.nk);
		nf.setMinimumFractionDigits(xData.nk);
		if (showButtonsAndLegend) {
			for (int i = 1; i < xData.ticks; ++i) {
				double xvalue = xData.tmin + xData.tdelta * i;
				Pt p = scaleToScreenY0(xvalue * q);
				p1 = new Pt(p.x, p.y);
				p2 = new Pt(p.x, p.y + TICK_LEN);
				String label = nf.format(xvalue);
				Pt shift = graphLibInt.getStringExtends(label);
				int labelPos = p2.x - shift.x / 2;
				if (labelPos > startPointX.x && labelPos + shift.x < unitPos) {
					graphLibInt.drawLine(p1.x, p1.y, p2.x, p2.y);
					graphLibInt.drawText(label, labelPos, p2.y + TICK_LEN);
				}
			}
		}
	}

	/**
	 * Draw the mouse sensitive areas
	 */
	private void paintClickAreas(int no) {
		XYPlotData data = dataList.get(no);
		int h = buttonHeight;
		int w = buttonWidth;
		int x = bounds.width - w - 8;
		if (showStartButton) {
			startRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x -= (w + 8);
		}
		if (showClearButton) {
			clearRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x -= (w + 8);
		}
		zoomYRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
		x -= (w + 8);
		saveRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
		boolean showAllButtons = (x > (buttonWidth + BUTTON_SPACING) * 6);
		x = BUTTON_SPACING;
		if (isPaused() && showAllButtons) {
			pos1Rectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x = x + w + BUTTON_SPACING;
			leftRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x = x + w + BUTTON_SPACING;
			zoomInRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x = x + w + BUTTON_SPACING;
		} else {
			pos1Rectangle = null;
			leftRectangle = null;
			zoomInRectangle = null;
		}

		if (isPaused()) {
			zoomOutRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x = x + w + BUTTON_SPACING;
		} else {
			zoomOutRectangle = null;
		}

		if (isPaused() && showAllButtons) {
			rightRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x = x + w + BUTTON_SPACING;
			endRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
		} else {
			rightRectangle = null;
			endRectangle = null;
		}

		if (zoomInRectangle != null) {
			drawImageButton(zoomInRectangle, ButtonImages.PLUS,
					allowZoomIn(data) && dataList.get(currentPlotNo).getCursorPos() >= 0);
		}
		if (zoomOutRectangle != null) {
			drawImageButton(zoomOutRectangle, ButtonImages.MINUS,
					allowZoomOut() && dataList.get(currentPlotNo).getCursorPos() >= 0);
		}
		if (pos1Rectangle != null) {
			drawImageButton(pos1Rectangle, ButtonImages.POS1, true);
		}
		if (leftRectangle != null) {
			drawImageButton(leftRectangle, ButtonImages.LEFT, true);
		}
		if (rightRectangle != null) {
			drawImageButton(rightRectangle, ButtonImages.RIGHT, true);
		}
		if (endRectangle != null) {
			drawImageButton(endRectangle, ButtonImages.END, true);
		}
		if (startRectangle != null && showStartButton) {
			if (isPaused()) {
				drawImageButton(startRectangle, ButtonImages.START, true);
			} else {
				drawImageButton(startRectangle, ButtonImages.PAUSE, true);
			}
		}
		if (clearRectangle != null && showClearButton) {
			drawImageButton(clearRectangle, ButtonImages.CLEAR, true);
		}
		if (zoomYRectangle != null) {
			if (globalYZoomSwitch) {
				drawImageButton(zoomYRectangle, ButtonImages.ZOOM_DOWN, true);
			} else {
				drawImageButton(zoomYRectangle, ButtonImages.ZOOM_UP, true);
			}
		}
		if (saveRectangle != null && showSaveButton) {
			drawImageButton(saveRectangle, ButtonImages.SAVE_CURVE, true);
		}
	}

	/**
	 * Draw a line with clipping at start/end of X or Y axis. The argument point
	 * values x/y are returned modified if clipping is required.
	 *
	 * @param p1
	 *            Start point
	 * @param p2
	 *            Stop point
	 * @return True if line is (at least partially) visible
	 */
	private boolean clipDataLine(Pt p1, Pt p2) {
		if (p2.x > stopPointX.x && p1.x >= stopPointX.x) {
			return false;
		}
		if (p2.x < startPointX.x) {
			return false;
		}
		boolean result = true;
		if (p1.x < startPointX.x) {
			double dx = (p2.x - p1.x);
			if (dx != 0) {
				double m = (p2.y - p1.y) / dx;
				p1.y = p2.y - (int) (m * (p2.x - startPointX.x));
				p1.x = startPointX.x;
			}
		}
		if (p2.x > stopPointX.x) {
			double dx = (p2.x - p1.x);
			if (dx != 0) {
				double m = (p2.y - p1.y) / dx;
				p2.y = p1.y + (int) (m * (stopPointX.x - p1.x));
				p2.x = stopPointX.x;
			}
		}
		if (p1.y <= startPointY.y && p2.y <= startPointY.y) {
			result = (p1.y == p2.y && p1.y == startPointY.y);
		} else if (p1.y >= stopPointY.y && p2.y >= stopPointY.y) {
			result = (p1.y == p2.y && p1.y == stopPointY.y);
		}
		if (p1.y > startPointY.y && p2.y < startPointY.y) {
			double dx = (p2.x - p1.x);
			if (dx != 0) {
				double m = (p1.y - p2.y) / dx;
				if (m != 0) {
					p2.y = startPointY.y;
					p2.x = p1.x + (int) (1.0 * (p1.y - p2.y) / m);
				}
			} else
				p2.y = startPointY.y;
		}
		if (p1.y < startPointY.y && p2.y > startPointY.y) {
			double dx = (p2.x - p1.x);
			if (dx != 0) {
				double m = (p2.y - p1.y) / dx;
				if (m != 0) {
					p1.y = startPointY.y;
					p1.x = p2.x - (int) (1.0 * (p2.y - p1.y) / m);
				}
			} else
				p1.y = startPointY.y;
		}
		if (p1.y < stopPointY.y && p2.y > stopPointY.y) {
			double dx = (p2.x - p1.x);
			if (dx != 0) {
				double m = (p2.y - p1.y) / dx;
				if (m != 0) {
					p2.y = stopPointY.y;
					p2.x = p1.x + (int) (1.0 * (p2.y - p1.y) / m);
				}
			} else
				p2.y = stopPointY.y;
		}
		if (p1.y > stopPointY.y && p2.y < stopPointY.y) {
			double dx = (p2.x - p1.x);
			if (dx != 0) {
				double m = (p1.y - p2.y) / dx;
				if (m != 0) {
					p1.y = stopPointY.y;
					p1.x = p2.x - (int) (1.0 * (p1.y - p2.y) / m);
				}
			} else
				p1.y = stopPointY.y;
		}
		return result;
	}

	/**
	 * Draw a mouse sensitive area
	 *
	 * @param r
	 *            Rectangle of button
	 * @param button
	 *            Button index
	 * @param enabled
	 *            True if button is enabled
	 */
	private void drawImageButton(Rect r, ButtonImages button, boolean enabled) {
		graphLibInt.setFgColor(FgColor.BUTTON);
		if (!graphLibInt.hasOwnButtonDrawing(button)) {
			if (enabled) {
				graphLibInt.setBgColor(BgColor.BUTTONBG);
			} else {
				graphLibInt.setBgColor(BgColor.BUTTONBGDISABLED);
			}
			graphLibInt.setSolidLines(1);
			graphLibInt.drawRoundRectangle(r, 8);
		} else {
			graphLibInt.setBgColor(BgColor.BG);
		}
		graphLibInt.fillRoundRectangle(r, 8);
		graphLibInt.drawImage(r, button, enabled);
	}

	/**
	 * Calculate the start/stop positions of the axis in screen positions
	 * <p>
	 * The locations of the points (*) for drawing the axis a shown in this
	 * figure:
	 * <p>
	 * 
	 * <pre>
	 * startPointY
	 *             |
	 *             |
	 *             |
	 *             |
	 * stopPointY/ | startPointX
	 *             |----------------------------* stopPointX
	 * </pre>
	 */
	private void calculateAxisPosition() {
		int lw = 0;
		int yox = 0;
		int yoy = 0;
		int yuy = bounds.height - 1;
		int xrx = bounds.width;
		int xly = 0;
		legendWidth = 0;
		Pt fontSize = new Pt(0, 0);
		if (showButtonsAndLegend) {
			graphLibInt.setNormalFont();
			fontSize = graphLibInt.getAverageCharacterSize();
		}
		int no = 0;
		for (XYPlotData data : dataList) {
			XYPlotData.ScaleData sd = data.getScaleData();
			if (xData == null || sd == null)
				continue;
			int bw = 3 * LEGEND_BOX_BORDER;
			graphLibInt.setNormalFont();
			lw = bw + graphLibInt.getStringExtends(data.getLegendText()).x;
			legendWidth = Math.max(legendWidth, lw);
			lw = bw + graphLibInt.getStringExtends(formatValueUnit(true, data, sd.vmax)).x;
			legendWidth = Math.max(legendWidth, lw);
			lw = bw + graphLibInt.getStringExtends(formatValueUnit(true, data, sd.vmin)).x;
			legendWidth = Math.max(legendWidth, lw);
			if (showButtonsAndLegend) {
				graphLibInt.setNormalFont();
				yox = Math.max(yox, (sd.gk + 4) * fontSize.x + TICK_LEN);
				int yexp = (int) sd.dexpo;
				if (yexp != 0) {
					int width = graphLibInt.getStringExtends(createExpoString(yexp)).x;
					yox = Math.max(yox, width);
				}
				yoy = Math.max(yoy, buttonBarHeight + fontSize.y / 2);
				yuy = Math.min(yuy, bounds.height - buttonBarHeight - PADDING_BUTTON * 2 - TICK_LEN - fontSize.y);
				xrx = bounds.width - graphLibInt.getStringExtends(formatValue(true, xData, xData.vmax)).x;
				xrx = Math.min(xrx, bounds.width - (PADDING_XR + 10));
			}
			double diff = sd.vmax - sd.vmin;
			if (diff != 0)
				sd.vfactor = (yuy - yoy) / diff;
			if (no == currentPlotNo) {
				xly = yuy + (int) Math.round(sd.vfactor * sd.vmin);
			}
			no++;
		}
		double diff = xData.vmax - xData.vmin;
		if (diff != 0)
			xData.vfactor = (xrx - yox) / diff;
		else
			xData.vfactor = 1;
		no++;
		startPointY = new Pt(yox, yoy);
		stopPointY = new Pt(yox, yuy);
		startPointX = new Pt(yox, xly);
		stopPointX = new Pt(xrx, xly);
	}

	/**
	 * If the Y values exceed a predefined size the will be shown with an
	 * additional exponent factor This display is called the scaled display
	 *
	 * @param yexp
	 *            Exponential factor (e.g 3 means 1000)
	 * @return
	 */
	private String createExpoString(int yexp) {
		StringBuffer ytext = new StringBuffer();
		ytext.append(" ");
		if (yexp > 0 && yexp < EXPO_E_MIN) {
			ytext.append("x 1");
			for (int i = 0; i < yexp; ++i) {
				ytext.append("0");
			}
		} else if (yexp < 0 && yexp > EXPO_E_MAX) {
			ytext.append("x 0.");
			for (int i = 1; i < -yexp; ++i) {
				ytext.append("0");
			}
			ytext.append("1");
		} else {
			ytext.append(EXPO_STUB + yexp);
		}
		return ytext.toString();
	}

	/**
	 * Calculate a x value for a given screen coordinate
	 *
	 * @param screenX
	 *            Screen position in pixels
	 * @return X value of the plot on the screenX position
	 */
	private double screenToScaleX(int screenX) {
		double x = 0;
		if (xData.vfactor != 0) {
			x = (screenX - startPointX.x) / xData.vfactor + xData.vmin;
		}
		return x;
	}

	/**
	 * Calculate a screen position on the y axis (x==0) for a given y value
	 *
	 * @param no
	 *            Number of the data set
	 * @param y
	 *            Y value
	 * @return Point object
	 */
	private Pt scaleToScreenX0(int no, double y) {
		XYPlotData.ScaleData sd = dataList.get(no).getScaleData();
		int xp = (int) (startPointX.x);
		int yp = (int) (stopPointY.y - ((y - sd.vmin) * sd.vfactor));
		return new Pt(xp, yp);
	}

	/**
	 * Calculate a screen position on the x axis (y==0) for a given x value
	 *
	 * @param x
	 *            X value
	 * @return Point object
	 */
	private Pt scaleToScreenY0(double x) {
		int xp = (int) (((x - xData.vmin) * xData.vfactor) + startPointX.x);
		int yp = (int) (stopPointY.y);
		return new Pt(xp, yp);
	}

	/**
	 * Calculate a screen position for a given X/Y value
	 *
	 * @param no
	 *            Number of the data set
	 * @param x
	 *            X value
	 * @param y
	 *            Y value
	 * @return Point object
	 */
	private Pt scaleToScreen(int no, double x, double y) {
		XYPlotData.ScaleData sd = dataList.get(no).getScaleData();
		int xp = (int) (((x - xData.vmin) * xData.vfactor) + startPointX.x);
		int yp = (int) (stopPointY.y - ((y - sd.vmin) * sd.vfactor));
		return new Pt(xp, yp);
	}

	/**
	 * Calculate the internal data required for drawing the scale
	 *
	 * @param sd
	 *            Scale data
	 * @param axis
	 *            Axis type (X or Y)
	 * @param min
	 *            Minimum value
	 * @param max
	 *            Maximum value
	 * @return True if scale has changed
	 */
	private boolean calculateScale(XYPlotData.ScaleData sd, AxisType axis, double min, double max) {
		int maxticks = 0;
		boolean result = false;
		boolean calcPointNum = false;
		Pt fontSize = graphLibInt.getAverageCharacterSize();
		switch (axis) {
		case XAXIS: {
			maxticks = bounds.width / (fontSize.x * 25) + 1;
			calcPointNum = true;
			break;
		}
		case YAXIS:
			maxticks = bounds.height / (fontSize.y * 4) + 1;
			break;
		}
		sd.lmin = min;
		sd.lmax = max;
		if (Math.abs(max - min) < DEF_Y_DIFF) {
			double q = Math.max(Math.abs(min), Math.abs(max));
			double diff = q * DEF_Y_DIFF;
			if (diff == 0) {
				diff = DEF_Y_DIFF;
			}
			min = min - diff / 2;
			max = max + diff / 2;
		}
		int expo = (int) Math.floor(Math.log10(max - min));
		int dexpo = 0;
		if (expo > NON_EXPO_MAX || expo < NON_EXPO_MIN) {
			dexpo = expo;
			double q = Math.pow(10, dexpo);
			min = min / q;
			max = max / q;
			expo = 0;
		}
		double delta = Math.pow(10, expo) / 10;
		long ticks = (long) Math.floor((max - min) / delta) + 1;
		int ticktype = 0;
		int loopcount = 0;
		boolean anyChange = true;
		while (Math.abs(ticks) > maxticks && anyChange) {
			anyChange = false;
			loopcount++;
			int index = 0;
			while (index < dividers.length) {
				if (ticks >= dividers[index] * maxticks) {
					delta = delta * dividers[index];
					ticktype = index;
					anyChange = true;
					break;
				}
				index++;
			}
			if (index >= dividers.length) {
				index = 0;
				while (index < dividers.length) {
					if (ticks * dividers[index] <= maxticks) {
						delta = delta / dividers[index];
						ticktype = index;
						anyChange = true;
						break;
					}
					index++;
				}
			}
			ticks = Math.round((max - min) / delta) + 1;
			if (loopcount > 10) {
				break;
			}
		}
		sd.vk = (int) Math.round(Math.log10(max));
		if (sd.vk <= 0)
			sd.vk = 1;
		sd.nk = 0;
		if (expo <= 0) {
			sd.nk = (int) (Math.round(0.499 + Math.abs(Math.log10(delta))));
		}
		sd.gk = sd.vk + Math.abs(sd.nk) + 2;
		double smin = Math.round(min / delta) * delta;
		if (min < smin) {
			smin -= delta;
		}
		if (Math.abs(smin - sd.smin) > delta / 10) {
			sd.tmin = smin;
			result = true;
		}
		double smax = Math.round(max / delta) * delta;
		if (max > smax) {
			smax += delta;
		}
		if (Math.abs(smax - sd.smax) > delta / 10) {
			sd.tmax = smax;
			result = true;
		}
		if ((isPaused() || smoothScroll) && AxisType.XAXIS.equals(axis)) {
			if (sd.smin != min || sd.smax != max) {
				result = true;
			}
			sd.smin = min;
			sd.smax = max;
		} else {
			sd.smin = sd.tmin;
			sd.smax = sd.tmax;
		}
		if (sd.isSwitch && delta < 1) {
			delta = 1;
		}
		ticks = Math.round((sd.tmax - sd.tmin) / delta) + 1;
		double q = Math.pow(10, dexpo);
		sd.vmin = sd.smin * q;
		sd.vmax = sd.smax * q;
		sd.dexpo = dexpo;
		sd.tdelta = delta;
		sd.ticks = ticks;
		sd.maxticks = maxticks;
		sd.ticktype = ticktype;
		int nk = sd.nk + 1 - dexpo;
		if (nk < 0)
			nk = 0;
		if (calcPointNum) {
			for (XYPlotData data : dataList) {
				int visible = 0;
				int nums = data.length();
				for (int i = 0; i < nums; ++i) {
					XYPlotData.DataValue dv = data.getValue(i);
					double x = dv.x();
					if (x >= sd.vmin && x <= sd.vmax) {
						visible++;
					}
				}
				data.setVisiblePointNum(visible);
			}
		}
		if (axis.equals(AxisType.XAXIS)) {
			xShiftValue = (sd.lmax - sd.lmin);
		}
		return result;
	}

	/**
	 * Move visible data to the maximum left position
	 */
	private void moveScreenPos1() {
		XYPlotData data = dataList.get(currentPlotNo);
		if (data != null) {
			double save = xShiftValue;
			double xmin = data.getXMin();
			double xmax = xmin + xShiftValue;
			scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
			xShiftValue = save;
		}
	}

	/**
	 * Move visible data to the left
	 */
	private void moveScreenLeft() {
		double save = xShiftValue;
		double xmin = xData.lmin - xShiftValue / 2;
		double xmax = xmin + xShiftValue;
		scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
		xShiftValue = save;
	}

	/**
	 * Move visible data to the left
	 */
	private void moveScreenRight() {
		double save = xShiftValue;
		double xmax = xData.lmax + xShiftValue / 2;
		double xmin = xmax - xShiftValue;
		scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
		xShiftValue = save;
	}

	/**
	 * Move visible data to maximum right position
	 */
	private void moveScreenEnd() {
		XYPlotData data = dataList.get(currentPlotNo);
		if (data != null) {
			double save = xShiftValue;
			double xmax = data.getXMax();
			double xmin = xmax - xShiftValue;
			scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
			xShiftValue = save;
		}
	}

	private boolean allowZoomOut() {
		double min = XYPlotData.MAX_DOUBLE_VALUE;
		double max = XYPlotData.MIN_DOUBLE_VALUE;
		XYPlotData data = dataList.get(currentPlotNo);
		if (data != null) {
			double tmp = data.getXMin();
			if (tmp < min)
				min = tmp;
			tmp = data.getXMax();
			if (tmp > max)
				max = tmp;
		}
		return (min < xData.vmin || max > xData.vmax);
	}

	private boolean allowZoomIn(XYPlotData data) {
		return data.getVisiblePointNum() > MOVES_PER_SCREEN;
	}

	/**
	 * Zoom into the data visible on the screen. The zoom is done symetrically
	 * to the left and right of the current cursor position, so the cursor moved
	 * to the middle.
	 *
	 * @param data
	 *            Currently selected plot data
	 * @param range
	 *            New range for x axis
	 * @return True in case a zoom was done
	 */
	private boolean zoomScreen(XYPlotData data, double range) {
		boolean result = false;
		int cp = data.getCursorPos();
		if (cp >= 0) {
			double cx = data.getValue(cp).x();
			double diff = range / 2;
			double xmin = xData.lmin;
			double xmax = xData.lmax;
			double dataXMin = data.getXMin();
			double dataXMax = data.getXMax();
			double dataXRange = dataXMax - dataXMin;
			if (((xmax - xmin) < range) || allowZoomIn(data)) {
				double saveXmin = xmin;
				double saveXmax = xmax;
				xmin = cx - diff;
				xmax = cx + diff;

				if (xmin < dataXMin - dataXRange) {
					xmin = dataXMin - dataXRange;
				}

				if (xmax > dataXMax + dataXRange) {
					xmax = dataXMax + dataXRange;
				}

				setPaused(true);

				scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
				if (scaleChanged) {
					zoomStack.push(saveXmin, saveXmax);
					result = true;
				}
				zoomVisibleRange = true;
			}
		}
		return result;
	}

	/**
	 * Zoom into the data visible on the screen. The zoom is done to a given
	 * range
	 *
	 * @param data
	 *            Currently selected plot data
	 * @param xmin
	 *            New minimum x value
	 * @param xmax
	 *            New maximum x value
	 * @return True in case a zoom was done
	 */
	private boolean zoomScreen(XYPlotData data, final double xmin, final double xmax) {
		boolean result = false;
		double dataXMin = data.getXMin();
		double dataXMax = data.getXMax();
		double x_min = xmin;
		double x_max = xmax;
		double dataXRange = dataXMax - dataXMin;
		if (allowZoomIn(data) || (xData.lmax - xData.lmin) < (xmax - xmin)) {
			if (x_min < dataXMin - dataXRange) {
				x_min = dataXMin - dataXRange;
			}

			if (x_max > dataXMax + dataXRange) {
				x_max = dataXMax + dataXRange;
			}

			setPaused(true);

			scaleChanged |= calculateScale(xData, AxisType.XAXIS, x_min, x_max);
			if (scaleChanged) {
				result = true;
			}
			zoomVisibleRange = true;
		}
		return result;
	}

	/**
	 * Zoom out the data visible on the screen
	 *
	 * @param data
	 *            Data area
	 */
	private void zoomScreenOut(XYPlotData data) {
		int cp = data.getCursorPos();
		if (cp >= 0) {
			if (allowZoomOut()) {
				double[] last = zoomStack.pop();
				double xmin;
				double xmax;
				// double cx = data.getValue(cp).x();
				if (last != null) {
					xmin = last[0];
					xmax = last[1];
				} else {
					xmin = data.getXMin();
					xmax = data.getXMax();
				}
				scaleChanged |= calculateScale(xData, AxisType.XAXIS, xmin, xmax);
				setPaused(true);
			}
		}
	}

	/**
	 * Zoom to the full data view
	 */
	private void zoomScreenOut() {
		calculateScale(xData, AxisType.XAXIS, userxmin, userxmax);
		scaleChanged = true;
		setPaused(false);
		zoomStack.clear();
		zoomVisibleRange = false;
		for (XYPlotData data : dataList) {
			if (data != null) {
				data.hideCursor();
			}
		}
	}

	/**
	 * Check if cursor is visible
	 *
	 * @return Yes (true), No (false)
	 */
	private boolean isCursorVisible(XYPlotData data) {
		boolean result = false;
		if (data != null) {
			int cursorPos = data.getCursorPos();
			Pt p1 = scaleToScreenY0(data.getValue(cursorPos).x());
			if (p1.x >= startPointX.x && p1.x <= stopPointX.x) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Set the cursor to be visible
	 *
	 * @param data
	 *            Data of which the cursor should be visible
	 * @return True if operation was successful
	 */
	private boolean setCursorVisible(XYPlotData data) {
		boolean result = false;
		if (data != null) {
			double xposL = screenToScaleX(startPointX.x);
			double xposR = screenToScaleX(stopPointX.x);
			double diff = xposR - xposL;
			int pos = data.locateIndexFromXValue(xposR);
			data.setCursorPos(pos);
			double xmax = data.getValue(pos).x();
			double xmin = xmax - diff;
			calculateScale(xData, AxisType.XAXIS, xmin, xmax);
			result = true;
		}
		return result;
	}

	/**
	 * Internal types
	 */
	private enum AxisType {
		XAXIS, YAXIS
	}

	/**
	 * Internal stack class to store min/max values for repeated in/out zooming
	 *
	 * @author kue2pl
	 */
	class ZoomStack {
		public boolean push(double xmin, double xmax) {
			boolean result = false;
			if (top < MAX_STACK_SIZE) {
				min[top] = xmin;
				max[top] = xmax;
				top++;
				result = true;
			}
			return result;
		}

		public double[] pop() {
			double[] result = null;
			if (top > 0) {
				result = new double[2];
				top--;
				result[0] = min[top];
				result[1] = max[top];
			}
			return result;
		}

		public void clear() {
			top = 0;
		}

		private final static int MAX_STACK_SIZE = 20;
		private int top = 0;
		private double min[] = new double[MAX_STACK_SIZE];
		private double max[] = new double[MAX_STACK_SIZE];;

	}

	/**
	 * Trim a text to fit into a given width.
	 * 
	 * @param text
	 *            Text to trim
	 * @param width
	 *            Max allowed width in pixels.
	 * @return Text fitting into the width
	 */
	private String trimText(final String text, final int width) {
		String dots = "...";
		int w = width;
		String s = text;
		int dotWidth = graphLibInt.getStringExtends(dots).x;
		if (graphLibInt.getStringExtends(s).x > w) {
			w -= dotWidth;
			while (s.length() > 0 && graphLibInt.getStringExtends(s).x > w) {
				s = s.substring(0, s.length()-1);
			}
			s += dots;
		}
		return s;
	}

	/**
	 * Convert an y value into a string
	 *
	 * @param scaled
	 *            Display the value eventually reduced by an exponent as shown
	 *            on the y axis. (Example: y axis shows values 1, 2, 3 and the
	 *            scale factor x 1E3 at top of the axis. If scaled is true 1234
	 *            will be converted to String 1,234 else 1234)
	 * @param data
	 *            Data context of the value
	 * @param value
	 *            The value to be displayed.
	 * @return
	 */
	private String formatValueUnit(boolean scaled, XYPlotData data, double value) {
		XYPlotData.ScaleData sd = data.getScaleData();
		if (sd.isSwitch) {
			List<String> labels = data.getSwitchLabels();
			int y = (int) Math.round(value);
			if (y >= 0 && y < labels.size()) {
				return labels.get(y);
			}
		} else {
			String unit = data.getUnit();
			if (unit.length() > 0) {
				unit = " [" + unit + "]";
			}
			return formatValue(scaled, sd, value) + unit;
		}
		return "";
	}

	/**
	 * Convert an y value into a string
	 *
	 * @param scaled
	 * @param sd
	 *            Scale data for the value
	 * @param value
	 *            The value to be displayed
	 * @return
	 */
	private String formatValue(boolean scaled, XYPlotData.ScaleData sd, double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(GROUPING_USED);
		int nk = sd.nk - (int) sd.dexpo;
		if (nk < 0)
			nk = 0;
		nf.setMaximumFractionDigits(nk + 1);
		nf.setMinimumFractionDigits(nk + 1);
		return nf.format(value);
	}

	/**
	 * Calculate the button size depending on screen dimensions and format of
	 * external button images (if available)
	 */
	private void calculateButtonSize() {
		int base = Math.min(bounds.width, bounds.height);
		buttonWidth = Math.max(MIN_BUTTON_WIDTH, base / BUTTON_NUM - BUTTON_SPACING);
		buttonHeight = Math.round(graphLibInt.getButtonRatio() * buttonWidth);
		if (buttonHeight == 0) {
			buttonHeight = buttonWidth;
		}
		buttonBarHeight = buttonHeight + 8;
	}

	/**
	 * Check if the graph is in recording or paused mode.
	 *
	 * @return true: Graph is in paused mode false: Graph is in recording mode
	 */
	private boolean isPaused() {
		return paused;
	}

	@Override
	public void setAxisVisible(boolean value) {
		showButtonsAndLegend = value;
	}

}
