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
	public static final int CMD_MOVE_LEFT = 1;
	public static final int CMD_MOVE_RIGHT = 2;
	public static final int CMD_LAST_DATA = 3;
	public static final int CMD_NEXT_DATA = 4;
	public static final int CMD_ZOOM_IN = 5;
	public static final int CMD_ZOOM_OUT = 6;
	public static final int CMD_SHOW_ALL = 7;

	private static final int ZOOMBOX_LAZY_UPDATE_DELAY = 10;
	private static final int DATA_UPDATE_DELAY = 500;
	private static final int MIN_DATA_UPDATE_DELAY = 10;
	private static final int BUTTON_NUM = 10;
	private static final int BUTTON_SPACING = 10;
	private static final int MIN_BUTTON_WIDTH = 44;
	private static final int PADDING_TOP = 3;
	private static final int PADDING_BUTTON = 5;
	private static final int PADDING_LEGEND = 5;
	private static final int PADDING_XR = 15;
	private static final int MOVES_PER_SCREEN = 20;
	private static final int MIN_ZOOM_RECT_SIZE = 10;
	private static final int MIN_MOVE_DETECT_SIZE = 5;
	private static final int POINT_DISTANCE_FOR_CIRCLES = 30;
	private static final int ZOOM_IN_FACTOR = 2;
	private static final String EXPO_STUB = " x 1E";
	private static final boolean GROUPING_USED = false;
	// The next two values define from which exponent the
	// display switches to a scaled format
	private static final int NON_EXPO_MAX = 4;
	private static final int NON_EXPO_MIN = -3;
	// The next two values define from which exponent the
	// scaled format is shown as e.g. 1E4 instead of x 10000
	private static final int EXPO_E_MIN = 6;
	private static final int EXPO_E_MAX = -4;

	// Smallest difference shown on the axis
	private static final double DEF_Y_DIFF = 1E-20;

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
	private int legendBoxBorder;
	private int tickLen;
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
	private boolean optimizedLineDrawing = true;
	private int zoomBoxLacyUpdateDelay = ZOOMBOX_LAZY_UPDATE_DELAY;
	private HashMap<Integer, XYPlotData.MinMax> dataMinMax;
	private HashMap<String, XYPlotData.MinMax> unitMinMax;

	protected XYPlot(IXYGraphLib graphLib) {
		dataMinMax = new HashMap<Integer, XYPlotData.MinMax>();
		unitMinMax = new HashMap<String, XYPlotData.MinMax>();
		graphLibInt = graphLib.getInt();
		graphLibInt.registerXYPlot(this);
		tickLen = graphLibInt.getAverageCharacterSize().x;
		legendBoxBorder = graphLibInt.getAverageCharacterSize().y;
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
	 * @param graphLib Interface to the graphic library
	 * @return Interface to the XYPlot graph
	 */
	static public IXYPlot createXYPlot(IXYGraphLib graphLib) {
		return new XYPlot(graphLib);
	}

	/**
	 * Register a handler to receive clicks on the start/stop, pause keys.
	 *
	 * @param h Receiver object for the events
	 */
	public void registerEventHandler(IXYPlotEvent h) {
		plotEvent = h;
	}

	/**
	 * Create a new data set to be displayed in the XY plot.
	 *
	 * @param max   Maximum number of XY values to be stored in the set
	 * @param color Color of the data to be displayed in the plot
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
	 * @see de.ewmksoft.xyplot.IXYPlot#getDataHandlers()
	 */
	public ArrayList<XYPlotData> getDataHandlers() {
		return new ArrayList<XYPlotData>(dataList);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.ewmksoft.xyplot.IXYPlot#setDataHandles(de.ewmksoft.xyplot.XYPlotData[])
	 */
	public void setDataHandlers(XYPlotData[] dhs) {
		removeDataHandlers();
		for (XYPlotData xyPlotData : dhs) {
			addDataHandler(xyPlotData);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#addDataHandler(de.ewmksoft.xyplot.XYPlotData)
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
		if (currentPlotNo >= dataList.size()) {
			currentPlotNo = 0;
		}
		XYPlotData.unlock();
		return result;
	}

	public void removeDataHandlers() {
		XYPlotData.lock();
		dataList.clear();
		currentPlotNo = 0;
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
	 */
	void setOutdated() {
		graphLibInt.startDelayedWakeTrigger(updateDelay);
	}

	/**
	 * Set redraw flag
	 */
	protected void setNeedsRedraw() {
		needsRedraw = true;
	}

	/**
	 * Returns true if the graph needs to be repainted. This call should be used
	 * before drawing to avoid unnecessary updates.
	 *
	 * @return true Graph needs to be repainted false No need to repaint the graph
	 */
	public boolean isOutdated() {
		return isOutdated(true);
	}

	/**
	 * Returns true if the graph needs to be repainted. This can be used to avoid
	 * unnecessary updates by only drawing if something has changed.
	 *
	 * @param resetState Reset the internal flag by calling this method (true) or
	 *                   check only the state (false)
	 * @return true Graph needs to be repainted false No need to repaint the graph
	 */
	private boolean isOutdated(boolean resetState) {
		boolean result = this.needsRedraw;
		if (resetState) {
			this.needsRedraw = false;
		}
		long msTime = System.currentTimeMillis();
		if (!isPaused() && !result && msTime - lastNewValueCheck > updateDelay) {
			for (XYPlotData data : dataList) {
				if (data.hasNewValues()) {
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
	 * @see de.ewmksoft.xyplot.IXYPlot#setLegendSelectBgColor(int, int, int)
	 */
	public void setLegendBgColor(int r, int g, int b) {
		graphLibInt.setLegendBgColor(r, g, b);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setLegendSelectBgColor(int, int, int)
	 */
	public void setLegendSelectBgColor(int r, int g, int b) {
		graphLibInt.setLegendSelectBgColor(r, g, b);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see de.ewmksoft.xyplot.IXYPlot#setOptimizedLineDrawing(boolean)
	 */
	public void setOptimizedLineDrawing(boolean optimizedDraw) {
		this.optimizedLineDrawing = optimizedDraw;
	}

	/**
	 * Method to be called by the owner of the plot to inform about a key stroke.
	 *
	 * @return true The key changed the plot display false The key did not change
	 *         anything
	 */
	public boolean evalKey(int key) {
		if (dataList.isEmpty() || startPointX == null) {
			return false;
		}
		boolean result = false;
		if (dataList.get(currentPlotNo) != null) {
			if (key == CMD_SHOW_ALL) {
				zoomScreenOut();
				plotEvent.onEvent(KeyEvent.KEY_START);
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
	 * @param x x coordinate of the mouse down click
	 * @param y y coordinate of the mouse down click
	 * @return true: The click was in a click sensitive area. false: The click did
	 *         not change anything because it did not hit a sensitive area
	 */
	public boolean evalMouseEvent(MouseEvent event, int x, int y) {
		if (dataList.isEmpty() || startPointX == null) {
			return false;
		}
		if (!bounds.contains(x, y)) {
			return false;
		}
		boolean result = false;
		double xvalue;
		int xLoc = x - bounds.x;
		int yLoc = y - bounds.y;
		switch (event) {
		case MOUSEDOWN:
			xvalue = screenToScaleX(xLoc);
			int nextNo = currentPlotNo;
			int no = -1;
			for (XYPlotData data : dataList) {
				no++;
				Rect legendRect = data.getLegendRect();
				if (legendRect != null && legendRect.contains(xLoc, yLoc)) {
					nextNo = no;
					needsRedraw = true;
					result = true;
					break;
				}
				if (legendFrameRect != null && legendFrameRect.contains(xLoc, yLoc)) {
					if (legendButtonRect != null && legendButtonRect.contains(xLoc, yLoc)) {
						needsRedraw = true;
						scaleChanged = true;
						expandLegend = expandLegend ^ true;
						blockNextTapEvent = true;
						result = true;
						break;
					}
					continue;
				}
				if (zoomYRectangle != null && zoomYRectangle.contains(xLoc, yLoc)) {
					globalYZoomSwitch = globalYZoomSwitch ^ true;
					needsRedraw = true;
					scaleChanged = true;
					result = true;
					break;
				}
				if (saveRectangle != null && showSaveButton && saveRectangle.contains(xLoc, yLoc)) {
					plotEvent.onEvent(KeyEvent.KEY_SAVE);
					result = true;
					break;
				}
				if (no == currentPlotNo && startRectangle != null && showStartButton
						&& startRectangle.contains(xLoc, yLoc)) {
					if (!isPaused()) {
						setPaused(true);
						yLoc = stopPointY.y - 1;
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
					if (clearRectangle != null && clearRectangle.contains(xLoc, yLoc)) {
						plotEvent.onEvent(KeyEvent.KEY_CLEAR);
						yLoc = stopPointY.y - 1;
						needsRedraw = true;
						result = true;
						break;
					}
					if (pos1Rectangle != null && pos1Rectangle.contains(xLoc, yLoc)) {
						moveScreenPos1();
						needsRedraw = true;
						result = true;
						break;
					}
					if (leftRectangle != null && leftRectangle.contains(xLoc, yLoc)) {
						moveScreenLeft();
						needsRedraw = true;
						result = true;
						break;
					}
					if (rightRectangle != null && rightRectangle.contains(xLoc, yLoc)) {
						moveScreenRight();
						needsRedraw = true;
						result = true;
						break;
					}
					if (endRectangle != null && endRectangle.contains(xLoc, yLoc)) {
						moveScreenEnd();
						needsRedraw = true;
						result = true;
						break;
					}
					if (zoomOutRectangle != null && zoomOutRectangle.contains(xLoc, yLoc)) {
						zoomScreenOut(data);
						needsRedraw = true;
						result = true;
						break;
					}
					if (zoomInRectangle != null && zoomInRectangle.contains(xLoc, yLoc)) {
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
			if (nextNo != currentPlotNo) {
				setCurrentPlot(nextNo);
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
		if (!isPaused() || currentPlotNo < 0 || currentPlotNo >= dataList.size()) {
			return result;
		}
		XYPlotData data = dataList.get(currentPlotNo);
		if (data != null) {
			if (xData.vfactor != 0) {
				double shift = (1.0 * pixelNum) / xData.vfactor;
				double xmin = data.getXMin();
				double xmax = data.getXMax();
				double min = xData.lmin + shift;
				double max = xData.lmax + shift;
				double diff = max - min;
				result = true;

				if (max < xmin) {
					max = xmin;
					min = max - diff;
					result = false;
				}
				if (min > xmax) {
					min = xmax;
					max = min + diff;
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
		if (isPaused() && dataList.size() > currentPlotNo) {
			XYPlotData xyPlotData = dataList.get(currentPlotNo);
			if (xyPlotData != null) {
				XYPlotData.lock();
				double xPosL = screenToScaleX(position);
				double f = Math.max(0.01d, factor);
				f = Math.min(1.99d, f);
				double xMin = xPosL - (xPosL - xData.lmin) / f;
				double xMax = xPosL + (xData.lmax - xPosL) / f;
				if (position != lastZoomAtPosition) {
					lastZoomAtPosition = position;
					int pos = xyPlotData.locateIndexFromXValue(xPosL);
					xyPlotData.setCursorPos(pos);
				}
				zoomStack.clear();
				result = zoomScreen(xyPlotData, xMin, xMax);
				needsRedraw = true;
				XYPlotData.unlock();
			}
		}
		return result;
	}

	/**
	 * The plot can host multiple XY data sets. However there is only one Y-Axis
	 * always showing the data of one selected set at a time. With this call one
	 * data set can be selected. A data set must have been created before it can be
	 * selected.
	 *
	 * @param no Number of the data set
	 */
	private void setCurrentPlot(int no) {
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
	 * @param bounds Outer rectangle defining the plotting are
	 */
	public void setBounds(Rect bounds) {
		boolean boundsChanged = this.bounds.width != bounds.width || this.bounds.height != bounds.height;
		this.bounds = bounds;
		if (boundsChanged) {
			calculateScale(xData, AxisType.XAXIS, userxmin, userxmax);
		}
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
	 * @param ymin Minimum y value to be displayed
	 * @param ymax Maximum y value to be displayed
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
	 * @see de.ewmksoft.xyplot.IXYPlot#initXRange(double, double)
	 */
	public void initXRange(double xmin, double xmax) {
		XYPlotData.lock();
		userxmin = xmin;
		userxmax = xmax;
		boolean save = smoothScroll;
		smoothScroll = true;
		calculateScale(xData, AxisType.XAXIS, xmin, xmax);
		smoothScroll = save;
		scaleChanged = true;
		setOutdated();
		needsRedraw = true;
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
		if (dataList.isEmpty()) {
			return;
		}
		XYPlotData.lock();
		graphLibInt.setNormalFont();
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
			} else {
				unitMinMax.put(unit, data.new MinMax(min, max));
			}
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
			if (minMax != null) {
				scaleChanged |= calculateScale(sd, AxisType.YAXIS, minMax.getMin(), minMax.getMax());
			}
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

			calculateLegendArea();
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
		if (dataList.isEmpty()) {
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
						String xs = formatValue(false, xData, xvalue);
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
	 * This value in [ms] is used to delay the update of the zoom box when changing
	 * the size. It is useful to set a higher value on devices with a lower graphic
	 * performance.
	 *
	 * @param zoomBoxLacyUpdateDelay New value for update delay
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
	 * @param allowPauseOnDataClick True to pause graph if clicked
	 */
	public void setAllowPauseOnDataClick(boolean allowPauseOnDataClick) {
		this.allowPauseOnDataClick = allowPauseOnDataClick;
	}

	/**
	 * Expand (true) ore collapse (false) the legend box
	 *
	 * @param value New value for legend expand state
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

	/**
	 * Status, expanded (true) ore collapsed (false), of the legend box
	 *
	 * @return true/false
	 */
	public boolean isLegendExpanded() {
		return expandLegend;
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
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setSaveButtonVisible(boolean)
	 */
	public void setSaveButtonVisible(boolean value) {
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
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setRunPauseButtonVisible(boolean)
	 */
	public void setStartButtonVisible(boolean value) {
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
	 * @see de.ewmksoft.xyplot.core.IXYPlot#setDeleteButtonVisible(boolean)
	 */
	public void setClearButtonVisible(boolean value) {
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
	 * Set the legend box to visible.
	 *
	 * @param value New value for visible state
	 */
	public void setLegendVisible(boolean value) {
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
	 * @param paused New value for paused state
	 */
	public void setPaused(boolean paused) {
		if (!this.paused && paused) {
			for (XYPlotData data : dataList) {
				data.setCursorPos(data.getLastDrawPointNum());
			}
		}
		this.paused = paused;
		buttonsChanged = true;
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
	public void setAxisLabels(boolean value) {
		showButtonsAndLegend = value;
	}

	/**
	 * Paint the curve
	 *
	 * @param no    Number of the curve
	 * @param data  Plot data
	 * @param start Start index
	 * @param stop  Stop index
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
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for (int i = start; i < stop; ++i) {
				XYPlotData.DataValue dv = data.getValue(i);
				if (i == start && dv.border())
					continue;
				boolean drawIt = (p1 == null);
				int visibleAdd = 0;
				Pt p2 = scaleToScreen(no, dv.x(), dv.y());
				if (p1 != null && clipDataLine(p1, p2)) {
					if (optimizedLineDrawing && p1.x == p2.x) {
						min = Math.min(min, p1.y);
						max = Math.max(max, p1.y);
						min = Math.min(min, p2.y);
						max = Math.max(max, p2.y);
					} else {
						if (max != Integer.MIN_VALUE) {
							if (pointNum + 3 < points.length) {
								points[pointNum++] = p1.x;
								points[pointNum++] = min;
								points[pointNum++] = p1.x;
								points[pointNum++] = max;
							}
							visibleAdd += 2;
							max = Integer.MIN_VALUE;
							min = Integer.MAX_VALUE;
						} else {
							if (pointNum >= 2) {
								pointNum -= 2;
							}
							if (pointNum + 3 < points.length) {
								points[pointNum++] = p1.x;
								points[pointNum++] = p1.y;
								points[pointNum++] = p2.x;
								points[pointNum++] = p2.y;
								visibleAdd += 1;
							}
						}
					}
					if (isPaused() && (p2.x - p1.x) > POINT_DISTANCE_FOR_CIRCLES) {
						graphLibInt.drawCircle(p1.x, p1.y, 3);
					}
					drawIt = true;
				}
				if (dv.border() || !drawIt) {
					graphLibInt.drawPolyline(points, pointNum);
					visible += visibleAdd;
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
		int xLoc = x - bounds.x;
		int yLoc = y - bounds.y;
		if ((legendFrameRect != null && legendFrameRect.contains(xLoc, yLoc)) || yLoc >= stopPointY.y) {
			result = false;
		} else {
			if (x >= startPointX.x && x <= stopPointX.x && y >= startPointY.y) {
				double xvalue = screenToScaleX(xLoc);
				int no = -1;
				int prevDiff = bounds.height;
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
							int ydiff = Math.abs(yLoc - p.y);
							if (ydiff < prevDiff) {
								prevDiff = ydiff;
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
		// **************** Draw boxes ***************************
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
	 * Calculate the surrounding box of the legend
	 */
	private void calculateLegendArea() {
		int y = PADDING_TOP;
		int w = buttonWidth;
		int h = buttonHeight;
		if (expandLegend) {
			w = legendWidth + 1;
			for (XYPlotData data : dataList) {
				Rect r = data.getLegendRect();
				if (r != null) {
					h += r.height;
				}
			}
		}
		h += legendBoxBorder;
		int x = bounds.width - 4 - w;
		legendFrameRect = null;
		if (showLegend) {
			legendFrameRect = new Rect(x, y, w, h);
			legendButtonRect = new Rect(x + 2, y + 2, w - 4, buttonHeight);
		}
	}

	/**
	 * Draw the surrounding box of the legend
	 */
	private void paintLegendArea() {
		graphLibInt.setFgColor(FgColor.AXIS);
		if (showLegend && legendFrameRect != null && legendButtonRect != null) {
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
	 * Calculate size and position of legend box.
	 *
	 * @param no Number of plot
	 */
	private void calculateLegendBox(int no, XYPlotData data) {
		if (expandLegend) {
			Pt fontSize = graphLibInt.getAverageCharacterSize();
			int x = bounds.width - 2 - legendWidth;
			int itemHeight = fontSize.y * 2 + legendBoxBorder;
			int y = PADDING_TOP + buttonBarHeight + 6 + no * itemHeight;
			int itemWidth = legendWidth - 4;
			data.setLegendRect(new Rect(x, y, itemWidth, itemHeight));
		} else {
			data.setLegendRect(null);
		}
	}

	/**
	 * Draw the legend box for one channel
	 */
	private void paintLegendBox(int no, XYPlotData data) {
		if (legendFrameRect == null || !expandLegend) {
			return;
		}
		Rect r = data.getLegendRect();
		if (r != null) {
			graphLibInt.setFgColor(FgColor.AXIS);
			graphLibInt.setBgColor(BgColor.LEGENDBG);
			graphLibInt.setSolidLines(1);
			if (no == currentPlotNo) {
				graphLibInt.setBgColor(BgColor.LEGENDSELECTBG);
			}
			graphLibInt.fillRectangle(r);
			Rect tp = new Rect(r.x + PADDING_LEGEND, r.y + legendBoxBorder / 2, r.width, r.height);
			graphLibInt.drawTextRect(no + 1, data.getLegendText(), tp);
			graphLibInt.setBgColor(BgColor.PLOTBG);
			graphLibInt.setFgPlotColor(no);
			graphLibInt.drawRectangle(
					new Rect(r.x, r.y + r.height - legendBoxBorder / 4 - 1, r.width, legendBoxBorder / 4));
			graphLibInt.setFgColor(FgColor.AXIS);
			if (data.length() > 0) {
				int pos = data.length() - 1;
				if (isPaused()) {
					pos = data.getCursorPos();
				}
				String label = formatValueUnit(false, data, data.getValue(pos).y());
				label = trimText(label, r.width - 2 * PADDING_LEGEND);
				graphLibInt.setBgColor(BgColor.LEGENDBG);
				if (no == currentPlotNo) {
					graphLibInt.setBgColor(BgColor.LEGENDSELECTBG);
				}
				graphLibInt.drawText(label, r.x + PADDING_LEGEND, r.y + r.height / 2);
			}
		}
	}

	/**
	 * Draw plot area background
	 *
	 * @param full true: paint from scratch false: paint only the additional area at
	 *             the right end
	 */
	private void paintPlotArea(boolean full) {
		int w = stopPointX.x - startPointX.x;
		int h = stopPointY.y - startPointY.y + 1;
		int x = startPointY.x;
		int y = startPointY.y - 1;
		graphLibInt.setBgColor(BgColor.PLOTBG);
		double xMax = XYPlotData.MIN_DOUBLE_VALUE;
		if (!full) {
			w = 0;
			for (XYPlotData data : dataList) {
				XYPlotData.ScaleData sd = data.getScaleData();
				if (sd != null && data.length() > 0) {
					XYPlotData.DataValue dv = data.getValue(0);
					xMax = Math.max(xMax, dv.x());
					Pt p = scaleToScreenY0(dv.x());
					if (p.x > stopPointX.x) {
						p.x = stopPointX.x;
					}
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
				for (int i = 0; i < data.length() && data.getValue(i).x() < xMax; ++i) {
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
		// **************** Draw Y axis ***************************
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

			graphLibInt.setBoldFont();
			Pt tw = graphLibInt.getStringExtends(ytext);
			// Draw current curve title in header
			int colorBoxSize = tw.y / 2;
			int titleXpos = startPointX.x + 2 * colorBoxSize;
			int titleYPos = PADDING_TOP;
			Rect titleRect = new Rect(titleXpos, titleYPos, stopPointX.x - startPointX.x - 50, tw.y);
			Rect titleColorBox = new Rect(startPointX.x + colorBoxSize / 2, titleYPos + colorBoxSize / 2, colorBoxSize,
					colorBoxSize);
			graphLibInt.drawTextRect(0, ytext, titleRect);
			graphLibInt.setBgPlotColor(currentPlotNo);
			graphLibInt.fillRectangle(titleColorBox);
			graphLibInt.setBgColor(BgColor.BG);
			graphLibInt.setNormalFont(); // Back from setBoldFont()
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
						p2 = new Pt(p.x - tickLen, p.y);
						graphLibInt.setBgColor(BgColor.LEGENDSELECTBG);
						String label = labels.get(y);
						Pt shift = graphLibInt.getStringExtends(label);
						int tx = Math.max(p2.x - shift.x - tickLen, 3);
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
					p2 = new Pt(p.x - tickLen / 4 * 3, p.y);
					Pt p3 = new Pt(p.x - tickLen, p.y);
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
					int x = Math.min(p3.x, shift.x);
					graphLibInt.drawText(label, p3.x - x, p3.y - shift.y / 2);
				}
			}
		}

		// **************** Draw X axis ***************************
		p1 = new Pt(startPointX.x, stopPointY.y);
		p2 = new Pt(stopPointX.x, stopPointY.y);
		graphLibInt.setFgColor(FgColor.AXIS);
		graphLibInt.setSolidLines(1);
		graphLibInt.drawLine(p1.x, p1.y, p2.x, p2.y);

		if (showButtonsAndLegend) {
			String s = xAxisText;
			if (xUnit.length() > 0) {
				s += " [" + xUnit + "] ";
			}
			int xexp = (int) xData.dexpo;
			if (xexp != 0) {
				s = s + EXPO_STUB + xexp;
			}
			s = " " + s;
			int unitPos = bounds.width - graphLibInt.getStringExtends(s + "_").x;
			graphLibInt.drawText(s, unitPos, p2.y + tickLen);
			q = Math.pow(10, xData.dexpo);
			nf = NumberFormat.getInstance();
			nf.setGroupingUsed(GROUPING_USED);
			nf.setMaximumFractionDigits(xData.nk);
			nf.setMinimumFractionDigits(xData.nk);
			for (int i = 1; i < xData.ticks; ++i) {
				double xvalue = xData.tmin + xData.tdelta * i;
				Pt p = scaleToScreenY0(xvalue * q);
				p1 = new Pt(p.x, p.y);
				p2 = new Pt(p.x, p.y + tickLen / 4 * 3);
				Pt p3 = new Pt(p.x, p.y + tickLen);
				String label = nf.format(xvalue);
				Pt shift = graphLibInt.getStringExtends(label);
				int labelPos = p2.x - shift.x / 2;
				if (p1.x > startPointX.x && p1.x < stopPointX.x) {
					graphLibInt.drawLine(p1.x, p1.y, p2.x, p2.y);
					if (labelPos + shift.x < unitPos) {
						graphLibInt.drawText(label, labelPos, p3.y);
					}
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
		int x = bounds.width - w - BUTTON_SPACING;
		if (showStartButton) {
			startRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x -= (w + BUTTON_SPACING);
		}
		if (showClearButton) {
			x -= (3 * BUTTON_SPACING);
			clearRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
			x -= (w + 2 * BUTTON_SPACING);
		}
		zoomYRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
		x -= (w + BUTTON_SPACING);
		if (showSaveButton) {
			saveRectangle = new Rect(x, bounds.height - buttonBarHeight + 2, w, h);
		}

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
	 * @param p1 Start point
	 * @param p2 Stop point
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
	 * @param r       Rectangle of button
	 * @param button  Button index
	 * @param enabled True if button is enabled
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
	 * The locations of the points (*) for drawing the axis a shown in this figure:
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
		int xrx = bounds.width - buttonBarHeight / 2;
		int xly = 0;
		legendWidth = 0;
		Pt fontSize = new Pt(0, 0);
		if (showButtonsAndLegend) {
			fontSize = graphLibInt.getAverageCharacterSize();
		}
		int no = 0;
		for (XYPlotData data : dataList) {
			XYPlotData.ScaleData sd = data.getScaleData();
			if (xData == null || sd == null)
				continue;
			int bw = 3 * legendBoxBorder;
			lw = bw + PADDING_LEGEND + graphLibInt.getStringExtends(data.getLegendText()).x;
			legendWidth = Math.max(legendWidth, lw);
			lw = bw + PADDING_LEGEND + graphLibInt.getStringExtends(formatValueUnit(true, data, sd.vmax)).x;
			legendWidth = Math.max(legendWidth, lw);
			lw = bw + PADDING_LEGEND + graphLibInt.getStringExtends(formatValueUnit(true, data, sd.vmin)).x;
			legendWidth = Math.max(legendWidth, lw);

			legendWidth = Math.min(2 * bounds.width / 3, legendWidth);

			if (showButtonsAndLegend) {
				yox = Math.max(yox, (sd.gk + 1) * fontSize.x + tickLen);
				int yexp = (int) sd.dexpo;
				if (yexp != 0) {
					int width = graphLibInt.getStringExtends(createExpoString(yexp)).x;
					yox = Math.max(yox, width);
				}
				yoy = Math.max(yoy, buttonBarHeight + fontSize.y / 2);
				yuy = Math.min(yuy, bounds.height - buttonBarHeight - PADDING_BUTTON * 2 - tickLen - fontSize.y);
			}
			double diff = sd.vmax - sd.vmin;
			if (diff != 0)
				sd.vfactor = (yuy - yoy) / diff;
			if (no == currentPlotNo) {
				xly = yuy + (int) Math.round(sd.vfactor * sd.vmin);
			}
			no++;
		}
		if (xData != null) {
			double diff = xData.vmax - xData.vmin;
			if (diff != 0)
				xData.vfactor = (xrx - yox) / diff;
			else
				xData.vfactor = 1;
		}
		startPointY = new Pt(yox, yoy);
		stopPointY = new Pt(yox, yuy);
		startPointX = new Pt(yox, xly);
		stopPointX = new Pt(xrx, xly);
	}

	/**
	 * If the Y values exceed a predefined size the will be shown with an additional
	 * exponent factor This display is called the scaled display
	 *
	 * @param yexp Exponential factor (e.g 3 means 1000)
	 * @return String for exponential value
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
	 * @param screenX Screen position in pixels
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
	 * @param no Number of the data set
	 * @param y  Y value
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
	 * @param x X value
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
	 * @param no Number of the data set
	 * @param x  X value
	 * @param y  Y value
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
	 * @param sd   Scale data
	 * @param axis Axis type (X or Y)
	 * @param min  Minimum value
	 * @param max  Maximum value
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
			double center = (min + max) / 2;
			double diff = 0.5f * Math.pow(10, NON_EXPO_MIN + 1);
			min = center - diff;
			max = center + diff;
			result = true;
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
		int vk = (int) Math.round(Math.log10(Math.abs(max)));
		if (vk <= 0) {
			vk = 1;
		}
		if (vk != sd.vk) {
			sd.vk = vk;
			result = true;
		}

		int nk = 0;
		if (expo <= 0) {
			nk = (int) Math.round(0.499 + Math.abs(Math.log10(delta)));
		}
		if (nk != sd.nk) {
			sd.nk = nk;
			result = true;
		}

		sd.gk = sd.vk + sd.nk + (max < 0 || min < 0 ? 3 : 2);

		double smin = Math.round(min / delta) * delta;
		if (min < smin) {
			smin -= delta;
		}
		if (Math.abs(smin - sd.smin) > delta / 10 || delta != sd.tdelta) {
			sd.tmin = smin;
			result = true;
		}
		double smax = Math.round(max / delta) * delta;
		if (max > smax) {
			smax += delta;
		}
		if (Math.abs(smax - sd.smax) > delta / 10 || delta != sd.tdelta) {
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
	 * Zoom into the data visible on the screen. The zoom is done symetrically to
	 * the left and right of the current cursor position, so the cursor moved to the
	 * middle.
	 *
	 * @param data  Currently selected plot data
	 * @param range New range for x axis
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
	 * Zoom into the data visible on the screen. The zoom is done to a given range
	 *
	 * @param data Currently selected plot data
	 * @param xmin New minimum x value
	 * @param xmax New maximum x value
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
	 * @param data Data area
	 */
	private void zoomScreenOut(XYPlotData data) {
		int cp = data.getCursorPos();
		if (cp >= 0) {
			if (allowZoomOut()) {
				double[] last = zoomStack.pop();
				double xmin;
				double xmax;
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
	 * @param data Data of which the cursor should be visible
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
		boolean push(double xmin, double xmax) {
			boolean result = false;
			if (top < MAX_STACK_SIZE) {
				min[top] = xmin;
				max[top] = xmax;
				top++;
				result = true;
			}
			return result;
		}

		double[] pop() {
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

		private static final int MAX_STACK_SIZE = 20;
		private int top = 0;
		private double[] min = new double[MAX_STACK_SIZE];
		private double[] max = new double[MAX_STACK_SIZE];
	}

	/**
	 * Trim a text to fit into a given width.
	 *
	 * @param text  Text to trim
	 * @param width Max allowed width in pixels.
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
				s = s.substring(0, s.length() - 1);
			}
			s += dots;
		}
		return s;
	}

	/**
	 * Convert an y value into a string
	 *
	 * @param scaled Display the value eventually reduced by an exponent as shown on
	 *               the y axis. (Example: y axis shows values 1, 2, 3 and the scale
	 *               factor x 1E3 at top of the axis. If scaled is true 1234 will be
	 *               converted to String 1,234 else 1234)
	 * @param data   Data context of the value
	 * @param value  The value to be displayed.
	 * @return String for y axis
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
	 * @param scaled Display the value eventually reduced by an exponent as shown on
	 *               the y axis. (Example: y axis shows values 1, 2, 3 and the scale
	 *               factor x 1E3 at top of the axis. If scaled is true 1234 will be
	 *               converted to String 1,234 else 1234)
	 * @param sd     Scale data for the value
	 * @param value  The value to be displayed
	 * @return String for y axis
	 */
	private String formatValue(boolean scaled, XYPlotData.ScaleData sd, double value) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(GROUPING_USED);
		int nk = (int) (xData.dexpo < 0 ? -xData.dexpo : sd.nk) + 1;
		nf.setMaximumFractionDigits(nk);
		nf.setMinimumFractionDigits(nk);
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

}
