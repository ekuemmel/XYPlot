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

package de.ewmksoft.xyplot.driver;

import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.ewmksoft.xyplot.core.IXYGraphLib;
import de.ewmksoft.xyplot.core.IXYGraphLibAdapter;
import de.ewmksoft.xyplot.core.IXYGraphLibInt;

/**
 * This is adaption for the generic XYPlot core to the SWT graphics library
 * 
 */
public class XYGraphLibIntSWT implements IXYGraphLibInt {
	private final Display display;
	private IXYGraphLibAdapter xyPlotAdapter;
	private GC currentGc;
	private Image bkImage;
	private Pt defCharSize;
	private Rectangle bounds;
	private ExecutorService updatePool;
	private Future<?> waitResult;
	private long wakeupDelay;
	private long lastWakeUpCall;

	private Color lineColor;
	private Color gridColor;
	private HashMap<Integer, Color> plotColor;
	private Color cursorColor;
	private Color buttonColor;

	private Color bgColor;
	private Color plotBgColor;
	private Color buttonBgColor;
	private Color buttonBgColorDisabled;
	private Color legendBgColor;
	private Color legendSelectBgColor;
	private Color cursorBgColor;

	private Font normalFont;
	private Font boldFont;

	private Image downImage;
	private Image upImage;
	private Image[] leftImage = { null, null };
	private Image[] rightImage = { null, null };
	private Image[] plusImage = { null, null };
	private Image[] minusImage = { null, null };
	private Image[] startImage = { null, null };
	private Image[] clearImage = { null, null };
	private Image[] pauseImage = { null, null };
	private Image[] pos1Image = { null, null };
	private Image[] endImage = { null, null };
	private Image[] zoomUpImage = { null, null };
	private Image[] zoomDownImage = { null, null };
	private Image[] saveCurveImage = { null, null };

	public XYGraphLibIntSWT(Display dp) {
		this.display = dp;
		defCharSize = new Pt(10, 15);
		this.bounds = new Rectangle(0,0,10,10);
		bkImage = new Image(display, bounds);

		updatePool = Executors.newCachedThreadPool();

		plotColor = new HashMap<Integer, Color>();
		plotBgColor = new Color(display, 220, 220, 220);
		bgColor = new Color(display, 250, 250, 250);
		lineColor = new Color(display, 50, 50, 50);
		buttonBgColor = new Color(display, 0xC3, 0xC3, 0xC3);
		buttonBgColorDisabled = new Color(display, 220, 220, 220);
		buttonColor = new Color(display, 255, 255, 255);
		gridColor = new Color(display, 255, 255, 255);
		legendSelectBgColor = new Color(display, 220, 220, 220);
		legendBgColor = new Color(display, 255, 255, 255);
		cursorColor = new Color(display, 55, 55, 55);
		cursorBgColor = new Color(display, 230, 230, 230);
		setFontSize(8, 12);

		InputStream is;
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/down.png");
		downImage = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/up.png");
		upImage = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/left.png");
		leftImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/left_g.png");
		leftImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/right.png");
		rightImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/right_g.png");
		rightImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/plus.png");
		plusImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/plus_g.png");
		plusImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/minus.png");
		minusImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/minus_g.png");
		minusImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/start.png");
		startImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/start_g.png");
		startImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/waste.png");
		clearImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/waste_g.png");
		clearImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/pause.png");
		pauseImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/pause_g.png");
		pauseImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/leftleft.png");
		pos1Image[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/leftleft_g.png");
		pos1Image[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/rightright.png");
		endImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/rightright_g.png");
		endImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/toggleup.png");
		zoomUpImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/toggleup_g.png");
		zoomUpImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/toggledown.png");
		zoomDownImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/toggledown_g.png");
		zoomDownImage[1] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/savecurve.png");
		saveCurveImage[0] = new Image(display, is);
		is = this.getClass().getClassLoader()
				.getResourceAsStream("images/savecurve_g.png");
		saveCurveImage[1] = new Image(display, is);
	}

	public void close() {
		updatePool.shutdown();
		if (normalFont != null) {
			normalFont.dispose();
		}
		if (boldFont != null) {
			boldFont.dispose();
		}
		for (Integer i : plotColor.keySet()) {
			plotColor.get(i).dispose();
		}
		plotBgColor.dispose();
		bgColor.dispose();
		lineColor.dispose();
		buttonBgColor.dispose();
		buttonBgColorDisabled.dispose();
		buttonColor.dispose();
		gridColor.dispose();
		legendSelectBgColor.dispose();
		legendBgColor.dispose();
		cursorColor.dispose();
		cursorBgColor.dispose();
		bkImage.dispose();
	}

	public void registerXYPlot(IXYGraphLibAdapter xyPlot) {
		this.xyPlotAdapter = xyPlot;
	}

	public void paint(GC gc) {
		if (xyPlotAdapter == null)
			return;

		if (xyPlotAdapter.showsCursor()) {
			currentGc = gc;
			gc.drawImage(bkImage, bounds.x, bounds.y);
			xyPlotAdapter.paintCursor();
		} else {
			currentGc = new GC(bkImage);
			setNormalFont();
			Point tw = currentGc.stringExtent("g");
			defCharSize.x = tw.x;
			defCharSize.y = tw.y;
			xyPlotAdapter.paintGraph();
			currentGc.dispose();
			gc.drawImage(bkImage, bounds.x, bounds.y);
			currentGc = gc;
			xyPlotAdapter.paintCursor();
		}
		currentGc = null;
	}

	public synchronized void startDelayedWakeTrigger(long delay) {
		wakeupDelay = delay;
		boolean launchTimer = false;
		if (waitResult != null) {
			if (waitResult.isDone()) {
				launchTimer = true;
			}
		} else
			launchTimer = true;
		if (launchTimer) {
			try {
				waitResult = updatePool.submit(new Runnable() {
					public void run() {
						try {
							Thread.sleep(wakeupDelay);
							display.wake();
						} catch (InterruptedException e) {
						} catch (Exception e) {
						}
					}
				});
			} catch (Exception e) {
				// TODO handle exception
				waitResult = null;
			}

		}
		long msTime = System.currentTimeMillis();
		if (msTime - lastWakeUpCall > delay) {
			display.wake();
			lastWakeUpCall = msTime;
		}
	}

	public IXYGraphLib.Rect getBounds() {
		return new IXYGraphLib.Rect(bounds.x, bounds.y, bounds.width,
				bounds.height);
	}

	public void setBounds(IXYGraphLib.Rect rect) {
		this.bounds = new Rectangle(rect.x, rect.y, rect.width, rect.height);
		bkImage.dispose();
		bkImage = new Image(display, this.bounds);
		xyPlotAdapter.setBounds(rect);
	}

	public void setFontSize(int labelFontSize, int titleFontSize) {
		String fontName = "Arial";
		setFontSize(fontName, labelFontSize, titleFontSize);
	}

	public void setFontSize(String fontName, int labelFontSize,
			int titleFontSize) {
		try {
			if (normalFont != null) {
				normalFont.dispose();
			}
			normalFont = new Font(display, fontName, labelFontSize, SWT.NORMAL);
		} catch (Exception e) {
			normalFont = null;
		}
		try {
			if (boldFont != null) {
				boldFont.dispose();
			}
			boldFont = new Font(display, fontName, titleFontSize, SWT.BOLD);
		} catch (Exception e) {
			boldFont = null;
		}
	}

	public void setAxisColor(int r, int g, int b) {
		if (lineColor != null) {
			lineColor.dispose();
		}
		lineColor = new Color(display, r, g, b);
	}

	public void setCursorColor(int r, int g, int b) {
		if (cursorColor != null) {
			cursorColor.dispose();
		}
		cursorColor = new Color(display, r, g, b);
	}

	public void setCursorBgColor(int r, int g, int b) {
		if (cursorBgColor != null) {
			cursorBgColor.dispose();
		}
		cursorBgColor = new Color(display, r, g, b);
	}

	public void setBgColor(int r, int g, int b) {
		if (bgColor != null) {
			bgColor.dispose();
		}
		bgColor = new Color(display, r, g, b);
		if (legendBgColor != null) {
			legendBgColor.dispose();
		}
		legendBgColor = new Color(display, r, g, b);
	}

	public void setDrawAreaBgColor(int r, int g, int b) {
		if (plotBgColor != null) {
			plotBgColor.dispose();
		}
		plotBgColor = new Color(display, r, g, b);
		if (legendSelectBgColor != null) {
			legendSelectBgColor.dispose();
		}
		legendSelectBgColor = new Color(display, r, g, b);
	}

	public void setFgPlotColor(int no) {
		Color c = plotColor.get(no);
		if (c != null) {
			currentGc.setForeground(c);
		}
	}

	public void setBgPlotColor(int no) {
		Color c = plotColor.get(no);
		if (c != null) {
			currentGc.setBackground(c);
		}
	}

	public void createColor(int no, IXYGraphLib.RGB value) {
		Color c = plotColor.get(no);
		if (c == null) {
			plotColor.put(no, new Color(display, value.red, value.green,
					value.blue));
		}
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
		Point tw = currentGc.stringExtent(s);
		result.x = tw.x;
		result.y = tw.y;
		return result;
	}

	/**
	 * Set current foreground color
	 */

	public void setFgColor(FgColor c) {
		switch (c) {
		case AXIS:
			currentGc.setForeground(lineColor);
			break;
		case GRID:
			currentGc.setForeground(gridColor);
			break;
		case CURSOR:
			currentGc.setForeground(cursorColor);
			break;
		case BUTTON:
			currentGc.setForeground(buttonColor);
			break;
		}
	}

	/**
	 * Set current background color
	 */

	public void setBgColor(BgColor c) {
		switch (c) {
		case BG:
			currentGc.setBackground(bgColor);
			break;
		case PLOTBG:
			currentGc.setBackground(plotBgColor);
			break;
		case CURSORBG:
			currentGc.setBackground(cursorBgColor);
			break;
		case LEGENDSELECTBG:
			currentGc.setBackground(legendSelectBgColor);
			break;
		case LEGENDBG:
			currentGc.setBackground(legendBgColor);
			break;
		case BUTTONBG:
			currentGc.setBackground(buttonBgColor);
			break;
		case BUTTONBGDISABLED:
			currentGc.setBackground(buttonBgColorDisabled);
			break;
		}
	}

	public void setSolidLines(int width) {
		currentGc.setLineStyle(SWT.LINE_SOLID);
		currentGc.setLineWidth(width);
	}

	public void setDashedLines(int width) {
		currentGc.setLineStyle(SWT.LINE_DASH);
		currentGc.setLineWidth(width);
	}

	public void drawRectangle(IXYGraphLib.Rect rect) {
		currentGc.drawRectangle(rect.x, rect.y, rect.width, rect.height);
	}

	public void drawRectangle(int x, int y, int width, int height) {
		currentGc.drawRectangle(x, y, width, height);
	}

	public void drawImage(IXYGraphLib.Rect rect, ButtonImages button,
			boolean enabled) {
		int picNum = (enabled ? 0 : 1);
		Image image = null;
		switch (button) {
		case DOWN:
			image = downImage;
			break;
		case UP:
			image = upImage;
			break;
		case LEFT:
			image = leftImage[picNum];
			break;
		case RIGHT:
			image = rightImage[picNum];
			break;
		case PLUS:
			image = plusImage[picNum];
			break;
		case MINUS:
			image = minusImage[picNum];
			break;
		case START:
			image = startImage[picNum];
			break;
		case CLEAR:
			image = clearImage[picNum];
			break;
		case PAUSE:
			image = pauseImage[picNum];
			break;
		case POS1:
			image = pos1Image[picNum];
			break;
		case END:
			image = endImage[picNum];
			break;
		case ZOOM_UP:
			image = zoomUpImage[picNum];
			break;
		case ZOOM_DOWN:
			image = zoomDownImage[picNum];
			break;
		case SAVE_CURVE:
			image = saveCurveImage[picNum];
			break;
		default:
			break;
		}
		if (image != null) {
			int xp = rect.x + rect.width / 2 - image.getBounds().width / 2;
			int yp = rect.y + rect.height / 2 - image.getBounds().height / 2;
			currentGc.drawImage(image, xp, yp);
		}
	}

	public void setNormalFont() {
		if (normalFont != null) {
			currentGc.setFont(normalFont);
		}
	}

	public void setBoldFont() {
		if (boldFont != null) {
			currentGc.setFont(boldFont);
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		currentGc.drawLine(x1, y1, x2, y2);
	}

	public void drawPolyline(int[] points, int num) {
		int[] pointArray = new int[num];
		for (int i = 0; i < num; ++i) {
			pointArray[i] = points[i];
		}
		currentGc.drawPolyline(pointArray);
	}

	public void drawCircle(int x, int y, int radius) {
		currentGc.drawOval(x - radius, y - radius, 2 * radius, 2 * radius);
	}

	public void fillRectangle(IXYGraphLib.Rect rect) {
		currentGc.fillRectangle(rect.x, rect.y, rect.width, rect.height);
	}

	public void drawRoundRectangle(IXYGraphLib.Rect rect, int radius) {
		currentGc.drawRoundRectangle(rect.x, rect.y, rect.width, rect.height,
				radius, radius);
	}

	public void fillRoundRectangle(IXYGraphLib.Rect rect, int radius) {
		currentGc.fillRoundRectangle(rect.x + 1, rect.y + 1, rect.width - 1,
				rect.height - 1, radius, radius);
	}

	public void drawText(String label, int x, int y) {
		currentGc.drawText(label, x, y);
	}

	public void drawBackground(IXYGraphLib.Rect rect) {
		setBgColor(BgColor.BG);
		fillRectangle(rect);
	}

	public float getButtonRatio() {
		return 1.0f*25/40;
	}

	public boolean hasOwnButtonDrawing(ButtonImages button) {
		return false;
	}

}
