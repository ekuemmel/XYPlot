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

import java.util.ArrayList;

import de.ewmksoft.xyplot.core.IXYGraphLib.MouseEvent;
import de.ewmksoft.xyplot.core.IXYGraphLib.Rect;

public interface IXYPlot {

	/**
	 * Register a handler to receive clicks on the start/stop, pause keys.
	 *
	 * @param h
	 *            Receiver object for the events
	 */
	abstract void registerEventHandler(IXYPlotEvent h);

	/**
	 * Get list of data handlers
	 *
	 * @return List of data sets
	 */

	abstract ArrayList<XYPlotData> getDataHandler();

	/**
	 * Add an already existing data set
	 *
	 * @return true if data handler has been added
	 */
	abstract boolean addDataHandler(XYPlotData dh);

	/**
	 * Remove data set
	 *
	 * @return True if data set has been removed
	 */
	abstract boolean removeDataHandler(XYPlotData dh);

	/**
	 * Remove all data sets
	 */
	abstract void removeDataHandlers();

	/**
	 * Set the delay of the repaint operation. The delay controls how much time
	 * may expire until the class tells its owner via getRedrawArea() that a
	 * repaint is necessary. A repaint is only requested when required (new data
	 * or user activity in the graph). This delay here defines how much
	 * additional time is waited before such an request is actually triggered.
	 * (Note there is a minimum set internally which can not be exceeded)
	 *
	 * @return Value in [ms]
	 */
	abstract void setUpdateDelay(long delay);

	/**
	 * Set size of font for labels and title
	 *
	 * @param labelFontSize
	 * @param titleFontSize
	 */
	abstract void setFontSize(int labelFontSize, int titleFontSize);

	/**
	 * Set name and size of font for labels and title
	 *
	 * @param labelFontSize
	 * @param titleFontSize
	 */
	abstract void setFontSize(String fontName, int labelFontSize, int titleFontSize);

	/**
	 * Set color of global background
	 *
	 * @param r
	 *            R value of RGB
	 * @param g
	 *            G value of RGB
	 * @param b
	 *            B value of RGB
	 */
	abstract void setBgColor(int r, int g, int b);

	/**
	 * Set color of curve draw area background
	 *
	 * @param r
	 *            R value of RGB
	 * @param g
	 *            G value of RGB
	 * @param b
	 *            B value of RGB
	 */
	abstract void setDrawAreaBgColor(int r, int g, int b);

	/**
	 * Set color of axis
	 *
	 * @param r
	 *            R value of RGB
	 * @param g
	 *            G value of RGB
	 * @param b
	 *            B value of RGB
	 */
	abstract void setAxisColor(int r, int g, int b);

	/**
	 * Set color of cursor
	 *
	 * @param r
	 *            R value of RGB
	 * @param g
	 *            G value of RGB
	 * @param b
	 *            B value of RGB
	 */
	abstract void setCursorColor(int r, int g, int b);

	/**
	 * Set color of cursor box background
	 *
	 * @param r
	 *            R value of RGB
	 * @param g
	 *            G value of RGB
	 * @param b
	 *            B value of RGB
	 */
	abstract void setCursorBgColor(int r, int g, int b);

	/**
	 * Get the text for the X axis unit
	 *
	 * @return text for x axis unit
	 */
	abstract String getXUnitText();

	/**
	 * Set the text for the X axis unit
	 *
	 * @param s
	 *            Unit text i.e. "s" for seconds. Do not add brackets [] here
	 */
	abstract void setXUnitText(String s);

	/**
	 * Get the text for the X axis
	 *
	 * @return text for x axis.
	 */
	abstract String getXAxisText();

	/**
	 * Set the text for the X axis unit
	 *
	 * @param s
	 *            Unit text i.e. "s" for seconds. Do not use brackets [] here,
	 *            they are added automatically
	 */
	abstract void setXAxisText(String s);

	/**
	 * Set the range for the X axis. The class takes this values to calculate a
	 * proper scaling for the axis. Note: If the graph is in zoomed mode, the
	 * new values will be active after the zoomed mode has been left
	 *
	 * @param xmin
	 *            Minimum x value to be displayed
	 * @param xmax
	 *            Maximum x value to be displayed
	 */
	abstract void setXRange(double xmin, double xmax);

	/**
	 * Same as setXRange but is always executed independent of the graphs
	 * current paused state.
	 *
	 * @param xmin
	 *            Minimum x value to be displayed
	 * @param xmax
	 *            Maximum x value to be displayed
	 */
	abstract void initXRange(double xmin, double xmax);

	/**
	 * Get the minimum X value
	 */
	abstract double getXMin();

	/**
	 * Get the maximum X value
	 */
	abstract double getXMax();

	/**
	 * Method to be called by the owner of the plot to inform about a key
	 * stroke.
	 *
	 * @return true The key changed the plot display false The key did not
	 *         change anything
	 */
	abstract boolean evalKey(int key);

	/**
	 * Method to be called by the owner of the plot to inform about mouse click
	 * events.
	 *
	 * @param x
	 *            x coordinate of the mouse down click
	 * @param y
	 *            y coordinate of the mouse down click
	 * @return true The click was in a click sensitive area false. The click did
	 *         not change the plot display false The click did not change
	 *         anything because it did not hit a sensitive area
	 */
	abstract boolean evalMouseEvent(MouseEvent event, int x, int y);

	/**
	 * Get the area which needs to be redrawn. If nothing needs to be redrawn it
	 * return null.
	 *
	 * @return Rectangle or null
	 */
	abstract Rect getRedrawArea();

	/**
	 * Returns true if the graph needs to be repainted. This call should be used
	 * before drawing to avoid unnecessary updates.
	 *
	 * @return true Graph needs to be repainted false No need to repaint the
	 *         graph
	 */
	abstract boolean isOutdated();

	/**
	 * Set the boundaries of the component in absolute coordinates of the canvas
	 *
	 * @param bounds
	 *            Outer rectangle defining the plotting are
	 */
	abstract void setBounds(Rect bounds);

	/**
	 * Returns the current value of the zoom box delay (See
	 * {@link setZoomBoxLacyUpdateDelay})
	 *
	 * @return Current value [ms]
	 */
	abstract int getZoomBoxLacyUpdateDelay();

	/**
	 * This value in [ms] is used to delay the update of the zoom box when
	 * changing the size. It is useful to set a higher value on devices with a
	 * lower graphic performance.
	 *
	 * @param zoomBoxLacyUpdateDelay
	 */
	abstract void setZoomBoxLacyUpdateDelay(int zoomBoxLacyUpdateDelay);

	/**
	 * Returns the current state of the flag. See
	 * {@link setAllowPauseOnDataClick}.
	 *
	 * @return Current State
	 */
	abstract boolean isAllowPauseOnDataClick();

	/**
	 * Allow that a click into the graph switches the state to pause mode.
	 *
	 * @param New
	 *            state
	 */
	abstract void setAllowPauseOnDataClick(boolean allowPauseOnDataClick);

	/**
	 * Set graph in paused mode. In this mode the cursor appears Note: In paused
	 * mode, a setXRange() call is not executed immediately.
	 *
	 * @param zoomed
	 */
	abstract void setPaused(boolean paused);

	/**
	 * Expand (true) ore collapse (false) the legend box
	 *
	 * @param value
	 */
	abstract void setLegendExpanded(boolean value);

	/**
	 * Status, expanded (true) ore collapsed (false), of the legend box
	 *
	 * @return true/false
	 */
	abstract boolean isLegendExpanded();

	/**
	 * Set the legend box to visible or invisible The method is deprecated due
	 * to a typo. Use the correctly spelled name instead.
	 *
	 * @param value
	 */
	@Deprecated
	abstract void setLegendVisisble(boolean value);

	abstract void setLegendVisible(boolean value);

	/**
	 * Set the save button to visible or invisible. The method is deprecated due
	 * to a typo. Use the correctly spelled name instead.
	 *
	 * @param value
	 */
	@Deprecated
	abstract void setSaveButtonVisisble(boolean value);

	abstract void setSaveButtonVisible(boolean value);

	/**
	 * Set the run/pause button to visible or invisible The method is deprecated
	 * due to a typo. Use the correctly spelled name instead.
	 *
	 * @param value
	 */
	@Deprecated
	abstract void setStartButtonVisisble(boolean value);

	abstract void setStartButtonVisible(boolean value);

	/**
	 * Set the delete plot button to visible or invisible The method is
	 * deprecated due to a typo. Use the correctly spelled name instead.
	 *
	 * @param value
	 */
	@Deprecated
	abstract void setClearButtonVisisble(boolean value);

	abstract void setClearButtonVisible(boolean value);

	/**
	 * Turn on/off of axis labels and legend. This allows to create smaller
	 * diagrams.
	 *
	 * @param value
	 *            true (default) or false
	 */
	abstract void setAxisLabels(boolean value);

	/**
	 * Move visible data to the left
	 */
	abstract void moveLeft();

	/**
	 * Move visible data to the right
	 */
	abstract void moveRight();

	/**
	 * Move visible data to the right
	 *
	 * @return True if move was done, false if at left or right end of data
	 */
	abstract boolean moveByPixels(int pixelNum);

	/**
	 * Zoom x-Axis around cursor position
	 */
	abstract void zoomIn();

	/**
	 * Zoom x-Axis at given position by a factor
	 *
	 * @param position
	 *            X-position on the screen in pixels
	 * @param factor
	 *            Factor to scale
	 * 
	 * @return True in case a zoom was done
	 */
	abstract boolean zoomAt(int position, double factor);

	/**
	 * Set smooth scrolling. Smooth means, that the graph is not shifted in
	 * bigger chunks but only as much as required. This causes more drawing
	 * operations. Default is true.
	 * 
	 * @param smoothScroll
	 *            True to scroll smoothly
	 */
	abstract void setSmoothScroll(boolean smoothScroll);
}