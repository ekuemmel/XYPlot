package de.ewmksoft.xyplot.demo;

/*
 * A demo application for the XYPlot component
 * 
 * @author Eberhard Kuemmel
 */

//import java.io.FileInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.ewmksoft.xyplot.core.IXYPlot;
import de.ewmksoft.xyplot.core.IXYPlotEvent;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphLibSWT;
import de.ewmksoft.xyplot.driver.XYPlotCanvas;


/**
 * Main class of the application
 * 
 * @author Eberhard Kuemmel
 * 
 */
public class Application1 implements ITimeTicker {
	private static final int TMAX = 300;
	public static final int WINDOW_WIDTH = 600;
	public static final int WINDOW_HEIGHT = 500;
	public static final int OFS_X = 0; // 50
	public static final int OFS_Y = 100; // 100

	public static final int UPDATE_DELAY = 1000; // Update interval [ms]
	private static final int MAX_POINTS = 8000; // Total points in the plot

	private static boolean clearOnStart = false;

	public static final int MOVING_DELTAT = 20; // Visible seconds while
												// recording

	
	
	private Shell shell;
	private Display display;
	private XYPlotCanvas xyPlotCanvas;
	private TimeTicker timeTicker;
	// private double last_rx;
	private volatile double last_xmax;
	private XYPlotData[] dh;
	private volatile long startTime;
	private boolean isPaused = false;
	private boolean newStart = false;

	public Application1(Display display) {
		this.display = display;
		shell = new Shell(display);
		// shell = new Shell(display, SWT.NONE); // No window border
		FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		shell.setText("XYPlot Demo Application");
		shell.setMinimumSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		shell.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		shell.setLayout(fillLayout);

		xyPlotCanvas = new XYPlotCanvas(this.shell);
		xyPlotCanvas.addF1Listener(new Listener() {
			public void handleEvent(Event event) {
			}
		});

		dh = new XYPlotData[3];
		int plotPoints = MAX_POINTS / dh.length;
		dh[0] = XYPlot.createDataHandler(plotPoints, new XYGraphLibSWT.RGB(255,
				50, 200, 50));
		dh[0].setLegendText("Pressure");
		dh[0].setUnit("hPa");
		dh[0].setAutoScale(true);
		dh[0].setManualScale(0, 25);

		dh[1] = XYPlot.createDataHandler(plotPoints, new XYGraphLibSWT.RGB(255,
				200, 20, 50));
		dh[1].setLegendText("Pressure");
		dh[1].setUnit("hPa");
		dh[1].setManualScale(0, 25);
		dh[1].setAutoScale(true);
		for (int i = 0; i < TMAX; ++i) {
			double x = i;
			dh[1].addValue(x, f(x) + 3);
		}

		dh[2] = XYPlot.createDataHandler(plotPoints, new XYGraphLibSWT.RGB(255,
				200, 20, 50));
		dh[2].setLegendText("Pressure");
		dh[2].setUnit("hPa");
		dh[2].setManualScale(0, 25);
		dh[2].setAutoScale(true);
		for (int i = 0; i < TMAX; ++i) {
			double x = i;
			dh[2].addValue(x, f(x) - 3);
		}
		IXYPlot xyplot = xyPlotCanvas.getXYPlot();
		xyplot.setXAxisText("Time");
		xyplot.setXUnitText("s");
		xyplot.setXRange(0, 0);
		xyplot.setFontSize(12, 14);
		xyplot.setSaveButtonVisisble(false);

		xyplot.registerEventHandler(new IXYPlotEvent() {
			public void onEvent(KeyEvent event) {
				switch (event) {
				case KEY_START:
					isPaused = false;
					newStart = true;
					break;
				case KEY_PAUSE:
					isPaused = true;
					break;
				case KEY_CLEAR:
					newStart = true;
					clearOnStart = true;
					break;
				case KEY_SAVE:
					MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION
							| SWT.OK | SWT.CANCEL);
					dialog.setText("Save graph");
					dialog.setMessage("Not implemented yet!");
					dialog.open();
					break;
				default:
					break;
				}
			}
		});

		for (int i = 0; i < dh.length; ++i) {
			xyplot.addDataHandler(dh[i]);
		}
		shell.open();
		shell.pack();
	}

	/**
	 * Main loop of the application called in the main method. The application
	 * loops here until it is terminated.
	 */
	void run() {
		last_xmax = 0;
		startTime = System.currentTimeMillis();
		IXYPlot xyplot = xyPlotCanvas.getXYPlot();
		xyplot.setXRange(0, TMAX);

		timeTicker = new TimeTicker(this, UPDATE_DELAY);
		Thread timerThread = new Thread(timeTicker);
		timerThread.start();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				xyPlotCanvas.updateControl();
				display.sleep();
			}
		}
		timeTicker.shutdown();
		try {
			timerThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		xyPlotCanvas.close();
		shell.dispose();
	}

	/**
	 * React on a timer event. Create new data to be shown in the XY plot by
	 * calling {@link shiftGraph()}
	 */
	public void onTimer() {
		if (newStart) {
			newStart = false;
			if (clearOnStart) {
				clearOnStart = false;
				for (XYPlotData xyPlotData : dh) {
					xyPlotData.clear();
				}
				startTime = System.currentTimeMillis();
			}
			last_xmax = 0;
		}
		long atime = System.currentTimeMillis();
		long totaldelta = atime - startTime;
		double x = 1.0 * (totaldelta) / 1000;

		if (isPaused) {
			for (XYPlotData dhx : dh) {
				if (dhx != null) {
					dhx.setPause();
				}
			}
		} else {
			if (x < TMAX) {
				if (dh[0] != null) {
					dh[0].addValue(x, f(x));
				}
				if (x > last_xmax) {
					x += MOVING_DELTAT / 2;
					// double min_x = Math.max(x - MOVING_DELTAT, 0);
					// IXYPlot xyplot = xyPlotCanvas.getXYPlot();
					// xyplot.setXRange(min_x, x);
					last_xmax = x;
				}
			}
		}
	}

	private double f(double x) {
		return 4.1311E-13 * x * x * x * x * x * x - 4.4282E-10 * x * x * x * x
				* x + 1.8959E-07 * x * x * x * x - 4.1569E-05 * x * x * x
				+ 5.0523E-03 * x * x - 3.6047E-01 * x + 1.9668E+01;
	}

}
