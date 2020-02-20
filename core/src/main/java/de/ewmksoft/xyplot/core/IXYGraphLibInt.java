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

/**
 * Abstract graphic library interface for XYPlot. A implementation must
 * implement all this functions to provide drawing functionality for XYPlot
 * graph.
 */
public interface IXYGraphLibInt {

	abstract public void startDelayedWakeTrigger(long delay);

	abstract public Pt getAverageCharacterSize();

	abstract public Pt getStringExtends(String s);

	abstract public IXYGraphLib.Rect getBounds();

	abstract public void registerXYPlot(IXYGraphLibAdapter xyPlot);

	public abstract void setBgColor(int r, int g, int b);

	public abstract void setDrawAreaBgColor(int r, int g, int b);

	public abstract void setAxisColor(int r, int g, int b);

	public abstract void setCursorColor(int r, int g, int b);

	public abstract void setCursorBgColor(int r, int g, int b);

	abstract public void setFontSize(int labelFontSize, int titleFontSize);

	abstract public void setFontSize(String fontName, int labelFontSize, int titleFontSize);

	abstract public void setNormalFont();

	abstract public void setBoldFont();

	abstract public void createColor(int no, IXYGraphLib.RGB value);

	abstract public void setFgColor(FgColor c);

	abstract public void setFgPlotColor(int no);

	abstract public void setBgPlotColor(int no);

	abstract public void setBgColor(BgColor c);

	abstract public void setSolidLines(int width);

	abstract public void setDashedLines(int width);

	abstract public void drawRectangle(IXYGraphLib.Rect rect);

	abstract public void drawRectangle(int x, int y, int width, int height);

	abstract public void drawImage(IXYGraphLib.Rect rect, ButtonImages button, boolean enabled);

	abstract public void drawLine(int x1, int y1, int x2, int y2);

	abstract public void drawPolyline(int[] points, int num);

	abstract public void drawCircle(int x, int y, int radius);

	abstract public void fillRectangle(IXYGraphLib.Rect rect);

	abstract public void drawRoundRectangle(IXYGraphLib.Rect rect, int radius);

	abstract public void fillRoundRectangle(IXYGraphLib.Rect rect, int radius);

	abstract public void drawBackground(IXYGraphLib.Rect rect);

	abstract public void drawText(String label, int x, int y);

	abstract public void drawTextRect(int number, String label, IXYGraphLib.Rect rect);

	/**
	 * If true, the core will not paint the buttons. The painting is done by the
	 * platform specific implementation
	 *
	 * @return True if platform implementation does button drawing
	 */
	public abstract boolean hasOwnButtonDrawing(ButtonImages button);

	/**
	 * Get the ration of button width to height.
	 * 
	 * @return Factor of height/width
	 */
	public abstract float getButtonRatio();

	public class Pt {
		public Pt() {
		};

		public Pt(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int x;
		public int y;
	}

	enum FgColor {
		AXIS, GRID, CURSOR, BUTTON
	}

	enum BgColor {
		BG, PLOTBG, LEGENDBG, LEGENDSELECTBG, CURSORBG, BUTTONBG, BUTTONBGDISABLED
	}

	enum ButtonImages {
		DOWN, UP, ZOOM_UP, ZOOM_DOWN, LEFT, RIGHT, PLUS, MINUS, START, STOP, CLEAR, PAUSE, POS1, END, SAVE_CURVE
	}

}
