package de.ewmksoft.xyplot.driver;

/**
 * XYPlotCanvas class is used to display the canvas where the XYPlot is placed on
 * 
 * @author Eberhard Kuemmel
 *
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.ewmksoft.xyplot.core.IXYGraphLib;
import de.ewmksoft.xyplot.core.IXYPlot;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.driver.XYGraphLibSWT;

/**
 * MyCanvas class is derived from Canvas class
 */
public class XYPlotCanvas extends Canvas {
	private static final int minDeltaX = 10;
	private static final int minDeltaY = 10;
	private double updateFreq;
	private long startT;
	private int updateCount = 0;
	private long updateDelay;
	private IXYPlot xyplot;
	private XYGraphLibSWT graphLib;
	private Listener listenerF1;
	private boolean isDown;
	private int oldX;
	private int oldY;

	public XYPlotCanvas(final Shell shell) {
		super(shell, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		Display display = shell.getDisplay();
		graphLib = new XYGraphLibSWT(display);
		xyplot = XYPlot.createXYPlot(graphLib);
		
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paint(event);
			}
		});
		addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				Rectangle rect = getClientArea();
				if (graphLib != null) {
					IXYGraphLib.Rect r = new IXYGraphLib.Rect(rect.x, rect.y,
							rect.width, rect.height);
					graphLib.setBounds(r);
				}
			}
		});
		addMouseListener(new MouseListener() {
			public void mouseUp(MouseEvent e) {
				onMouseUp(e.x, e.y);
				isDown = false;
			}

			public void mouseDown(MouseEvent e) {
				if (!isDown) {
					onMouseDown(e.x, e.y);
					isDown = true;
				}
			}

			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (isDown) {
					if (Math.abs(e.x - oldX) > minDeltaX
							|| Math.abs(e.y - oldY) > minDeltaY) {
						onMouseMove(e.x, e.y);
						oldX = e.x;
						oldY = e.y;
					}
				} else {
					/* Disabled this for the moment since it did not work
					Rectangle[] toolTipRects = graphLib.getToolTipRects();
					String[] toolTipStrings = graphLib.getToolTipStrings();
					for (int i = 0; i < toolTipRects.length; i++) {
						if (toolTipRects[i] != null && toolTipRects[i].contains(e.x, e.y)) {
							String s = toolTipStrings[i];
							if (!(s.equals(shell.getToolTipText()))) {
								shell.setToolTipText(s);
							}
							return;
						}
					}
					shell.setToolTipText(null);
					*/
				}

			}
		});
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.CR:
					onEnterKey();
					break;
				case SWT.BS:
					onBsKey();
					break;
				case SWT.F1:
					onF1Key();
					break;
				case SWT.F2:
					onF2Key();
					break;
				case SWT.ARROW_LEFT:
					onLeftKey();
					break;
				case SWT.ARROW_RIGHT:
					onRightKey();
					break;
				case SWT.ARROW_UP:
					onUpKey();
					break;
				case SWT.ARROW_DOWN:
					onDownKey();
					break;
				default:
					// ignore all other events
					break;
				}
			}

			public void keyReleased(KeyEvent e) {
				onKeyReleased();
			}
		});
		startT = System.currentTimeMillis();
	}

	public void addF1Listener(Listener listener) {
		listenerF1 = listener;
	}

	public void close() {
		if (graphLib != null) {
			graphLib.close();
		}
	}

	public IXYPlot getXYPlot() {
		return xyplot;
	}

	/**
	 * Update the XY plot by forcing a redraw or checking if redraw is requested
	 * by the plot itself
	 * 
	 * @result True if a redraw has been triggered.
	 */
	public synchronized boolean updateControl() {
		boolean result = false;
		XYGraphLibSWT.Rect r = xyplot.getRedrawArea();
		if (r != null) {
			updateCount++;
			redraw(r.x, r.y, r.width, r.height, false);
			result = true;
		}
		return result;
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onEnterKey() {
		if (xyplot.evalKey(XYPlot.CMD_SHOW_ALL)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onBsKey() {
		if (listenerF1 != null) {
			Event event = new Event();
			listenerF1.handleEvent(event);
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onF1Key() {
		if (xyplot.evalKey(XYPlot.CMD_ZOOM_IN)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onF2Key() {
		if (xyplot.evalKey(XYPlot.CMD_ZOOM_OUT)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onLeftKey() {
		if (xyplot.evalKey(XYPlot.CMD_MOVE_LEFT)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onRightKey() {
		if (xyplot.evalKey(XYPlot.CMD_MOVE_RIGHT)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onDownKey() {
		// TODO Auto-generated method stub
		if (xyplot.evalKey(XYPlot.CMD_LAST_DATA)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onUpKey() {
		// TODO Auto-generated method stub
		if (xyplot.evalKey(XYPlot.CMD_NEXT_DATA)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link IKeypad} interface to react on a key event
	 */
	public void onKeyReleased() {
	}

	/**
	 * Implementation of the {@link ITouschscreeen} interface to react on a
	 * touch event
	 */
	public void onMouseDown(int x, int y) {
		if (xyplot.evalMouseEvent(XYGraphLibSWT.MouseEvent.MOUSEDOWN, x, y)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link ITouschscreeen} interface to react on a
	 * touch event
	 */
	public void onMouseUp(int x, int y) {
		if (xyplot.evalMouseEvent(XYGraphLibSWT.MouseEvent.MOUSEUP, x, y)) {
			getDisplay().wake();
		}
	}

	/**
	 * Implementation of the {@link ITouschscreeen} interface to react on a
	 * touch event
	 */
	public void onMouseMove(int x, int y) {
		if (xyplot.evalMouseEvent(XYGraphLibSWT.MouseEvent.MOUSEMOVE, x, y)) {
		}
	}

	public long getUpdateDelay() {
		return updateDelay;
	}

	public double getUpdateFreq() {
		return updateFreq;
	}

	/**
	 * paint() method to update the window content
	 * 
	 * @param event
	 *            Paint event
	 */

	private void paint(PaintEvent event) {
		GC gc = event.gc;
		if (xyplot != null) {
			long t = System.currentTimeMillis();
			graphLib.paint(gc);
			long delay = System.currentTimeMillis() - t;
			updateDelay = (updateDelay + delay * 4) / 5;
			double dt = (t - startT) / 1000.0;
			if (dt > 0) {
				updateFreq = updateCount / dt;
			}
		}
	}

}
