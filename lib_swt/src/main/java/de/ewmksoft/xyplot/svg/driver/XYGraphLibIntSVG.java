/*****************************************************************************
 * 
 *  This file is part of the XYPlot library. The library allows to draw
 *  data in a x/y diagram using several output media.
 * 
 *  Copyright (C) 1994-2012 EWMK-Soft Eberhard Kuemmel
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

package de.ewmksoft.xyplot.svg.driver;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import de.ewmksoft.xyplot.core.IXYGraphLib;
import de.ewmksoft.xyplot.core.IXYGraphLib.Rect;
import de.ewmksoft.xyplot.core.IXYGraphLibAdapter;
import de.ewmksoft.xyplot.core.IXYGraphLibInt;

/**
 * This is the adaption of the generic XYPlot core to a SVG representation of
 * the plot.
 *
 */
public class XYGraphLibIntSVG implements IXYGraphLibInt {
	private IXYGraphLibAdapter xyPlot;
	private Pt defCharSize;
	private int ofsX;
	private int ofsY;
	private Rect bounds;
	private Font font;
	private Font normalFont;
	private Font boldFont;
	private Writer out;

	public XYGraphLibIntSVG(OutputStream os, IXYGraphLib.Rect rect) {
		if (os != null) {
			try {
				out = new OutputStreamWriter(os, "UTF-8");
				out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
				out.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"  "
						+ "width=\""
						+ rect.width
						+ "\" height=\""
						+ rect.height
						+ "\" "
						+ "style=\"fill-opacity:1; color-rendering:auto; color-interpolation:auto; text-rendering:auto; stroke:black; stroke-linecap:square; stroke-miterlimit:10; shape-rendering:auto; stroke-opacity:1; fill:black; stroke-dasharray:none; font-weight:normal; stroke-width:1; font-family:&apos;Dialog&apos;; font-style:normal; stroke-linejoin:miter; font-size:12; stroke-dashoffset:0; image-rendering:auto;\""
						+ ">\n");
				out.append("  <rect x=\"0\" y=\"0\" width=\"" + rect.width
						+ "\" height=\"" + rect.height
						+ "\" style=\"fill:none;\" />\n");
			} catch (UnsupportedEncodingException e) {
				out = null;
			} catch (IOException e) {
			}
		}
		defCharSize = new Pt(10, 15);
		setFontSize(12, 12);
		setBounds(rect);
		ofsX = 0;
		ofsY = 0;
	}

	public void close() {
		if (out != null) {
			try {
				out.append("</svg>\n");
				out.flush();
				out.close();
			} catch (IOException e) {
			}
		}
	}

	public void registerXYPlot(IXYGraphLibAdapter xyPlot) {
		this.xyPlot = xyPlot;
	}

	public void paint() {
		if (xyPlot == null)
			return;
		xyPlot.paintGraph();
	}

	public void setOffset(int x, int y) {
		ofsX = x;
		ofsY = y;
	}

	/****************** Graphic library independent methods ******************/

	public void startDelayedWakeTrigger(long delay) {
	}

	public IXYGraphLib.Rect getBounds() {
		return new IXYGraphLib.Rect(bounds.x, bounds.y, bounds.width,
				bounds.height);
	}

	public void setBounds(IXYGraphLib.Rect rect) {
		this.bounds = new Rect(rect.x, rect.y, rect.width, rect.height);
		if (xyPlot != null)
			xyPlot.setBounds(rect);
	}

	public void setFontSize(int labelFontSize, int titleFontSize) {
		String fontName = "Arial";
		try {
			normalFont = new Font(fontName, Font.PLAIN, labelFontSize);
		} catch (Exception e) {
			normalFont = null;
		}
		try {
			boldFont = new Font(fontName, Font.BOLD, labelFontSize);
		} catch (Exception e) {
			boldFont = null;
		}
		font = normalFont;
		FontRenderContext frc = new FontRenderContext(null, false, false);
		Rectangle2D r = font.getStringBounds("g", frc);
		defCharSize.x = (int) Math.round(1.0 * r.getWidth());
		defCharSize.y = (int) r.getHeight();
	}

	public void setFgPlotColor(int no) {
	}

	public void setBgPlotColor(int no) {
	}

	public void createColor(int no, IXYGraphLib.RGB value) {
	}

	/**
	 * Get the width of a average character in the current drawing environment
	 * 
	 * @return Width of the character '0'
	 */
	public Pt getAverageCharacterSize() {
		return defCharSize;
	}

	/**
	 * Get width and height of a string in pixels
	 * 
	 * @return Y and Y information as Pt object
	 */
	public Pt getStringExtends(String s) {
		Pt result = new Pt();
		FontRenderContext frc = new FontRenderContext(null, false, false);
		Rectangle2D r = font.getStringBounds(s, frc);
		result.x = (int) Math.round(1.0f * r.getWidth());
		result.y = (int) Math.round(0.9f * r.getHeight());
		return result;
	}

	/**
	 * Set current foreground color
	 */

	public void setFgColor(FgColor c) {
		// switch (c) {
		// case AXIS: currentGc.setForeground(lineColor);break;
		// case GRID: currentGc.setForeground(gridColor);break;
		// case CURSOR: currentGc.setForeground(cursorColor);break;
		// case BUTTON: currentGc.setForeground(buttonColor);break;
		// }
	}

	/**
	 * Set current background color
	 */

	public void setBgColor(BgColor c) {
		// switch (c) {
		// case BG: currentGc.setBackground(bgColor);break;
		// case PLOTBG: currentGc.setBackground(plotBgColor);break;
		// case CURSORBG: currentGc.setBackground(cursorBgColor);break;
		// case LEGENDSELECTBG:
		// currentGc.setBackground(legendSelectBgColor);break;
		// case LEGENDBG: currentGc.setBackground(legendBgColor);break;
		// case BUTTONBG: currentGc.setBackground(buttonBgColor);break;
		// case BUTTONBGDISABLED:
		// currentGc.setBackground(buttonBgColorDisabled);break;
		// }
	}

	public void setSolidLines(int width) {
	}

	public void setDashedLines(int width) {
	}

	public void drawRectangle(IXYGraphLib.Rect rect) {
		drawRectangle(rect.x, rect.y, rect.width, rect.height);
	}

	public void drawRectangle(int x, int y, int width, int height) {
		if (out != null) {
			try {
				out.append("  <rect x=\"" + (x + ofsX) + "\" y=\"" + (y + ofsY)
						+ "\" width=\"" + (width + ofsX) + "\" height=\""
						+ (height + ofsY) + "\" style=\"fill:none;\" />\n");
			} catch (IOException e) {
			}
		}
	}

	public void drawImage(IXYGraphLib.Rect rect, ButtonImages button,
			boolean enabled) {
	}

	public void setNormalFont() {
		if (normalFont != null) {
			font = normalFont;
		}
	}

	public void setBoldFont() {
		if (boldFont != null) {
			font = boldFont;
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		if (out != null) {
			try {
				out.append("  <line x1=\"" + (ofsX + x1) + "\" y1=\""
						+ (y1 + ofsY) + "\" x2=\"" + (ofsX + x2) + "\" y2=\""
						+ (ofsY + y2) + "\" style=\"fill:none;\"/>\n");
			} catch (IOException e) {
			}
		}
	}

	public void drawPolyline(int[] points, int num) {
		if (out != null) {
			try {
				out.append("  <path d=\"");
				for (int i = 0; i < num / 2; ++i) {
					if (i == 0)
						out.append("M");
					else
						out.append("L");
					out.append((points[i * 2] + ofsX) + " "
							+ (points[i * 2 + 1] + ofsY) + " ");
				}
				out.append("\" ");
				out.append(" style=\"fill:none; stroke-dasharray:none;\"/>\n");

			} catch (IOException e) {
			}
		}
	}

	public void drawCircle(int x, int y, int radius) {
		if (out != null) {
			try {
				out.append("  <circle cx=\"" + (ofsX + x) + "\" cy=\""
						+ (y + ofsY) + "\"" + "\" r=\"" + radius + "\"/>");
			} catch (IOException e) {
			}
		}
	}

	public void fillRectangle(IXYGraphLib.Rect rect) {
	}

	public void drawRoundRectangle(IXYGraphLib.Rect rect, int radius) {
	}

	public void fillRoundRectangle(IXYGraphLib.Rect rect, int radius) {
	}

	public void drawText(String label, int x, int y) {
		if (out != null) {
			try {
				int xpos = x + ofsX;
				int ypos = y + ofsY + defCharSize.y;
				String fw = "normal";
				if (font == boldFont)
					fw = "bold";
				out.append("  <text x=\"" + xpos + "\" y=\"" + ypos
						+ "\" style=\"font-weight:" + fw + ";\" font-size=\""
						+ font.getSize() + "\">");
				out.append(label);
				out.append("</text>\n");
			} catch (IOException e) {
			}
		}
	}

	@Override
	public void drawBackground(Rect rect) {
	}

	@Override
	public void setBgColor(int r, int g, int b) {
	}

	@Override
	public void setDrawAreaBgColor(int r, int g, int b) {
	}

	@Override
	public void setAxisColor(int r, int g, int b) {
	}

	@Override
	public void setCursorColor(int r, int g, int b) {
	}

	@Override
	public void setFontSize(String fontName, int labelFontSize,
			int titleFontSize) {
	}

	@Override
	public void setCursorBgColor(int r, int g, int b) {
	}

	@Override
	public float getButtonRatio() {
		return 0;
	}

	@Override
	public boolean hasOwnButtonDrawing(ButtonImages button) {
		return false;
	}

}
