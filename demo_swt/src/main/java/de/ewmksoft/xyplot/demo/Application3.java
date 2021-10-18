package de.ewmksoft.xyplot.demo;

/*
 * A demo application for the XYPlot component
 * 
 * @author Eberhard Kuemmel
 */

//import java.io.FileInputStream;

import java.io.IOException;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import de.ewmksoft.xyplot.core.IXYPlot;
import de.ewmksoft.xyplot.core.IXYPlotEvent;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphLibSWT;
import de.ewmksoft.xyplot.driver.XYPlotCanvas;
import de.ewmksoft.xyplot.utils.XYPlotPersistence;
import de.ewmksoft.xyplot.utils.XYPlotPersistence.ProgressCallback;

/**
 * Main class of the application
 * 
 * @author Eberhard Kuemmel
 * 
 */
public class Application3 implements ITimeTicker {
	public static final int WINDOW_WIDTH = 400;
	public static final int WINDOW_HEIGHT = 450;
	public static final int OFS_X = 0; // 50
	public static final int OFS_Y = 100; // 100

	public static final int UPDATE_DELAY = 50; // Update interval [ms]
	private static final int MAX_POINTS = 100000; // Total points in the plot

	private static boolean clearOnStart = false;

	public static final int MOVING_DELTAT = 10; // Visible seconds while
												// recording

	private Random random;
	private Shell shell;
	private Display display;
	private XYPlotCanvas xyPlotCanvas;
	private TimeTicker timeTicker;
	private String loadFileName;
	private String saveFileName;
	private XYPlotData[] dhs;
	private volatile long startTime;
	private boolean isPaused = false;
	private boolean newStart = false;
	private int step = 200;

	private String[] labels = { "Label 1", "Label 2 has a very long value and might be cut in some cases", "Label 3" };

	public Application3(Display display, String loadFileName, final String saveFileName) {
		this.display = display;
		this.loadFileName = loadFileName;
		this.saveFileName = saveFileName;
		random = new Random();

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

		dhs = new XYPlotData[2];
		dhs[0] = XYPlot.createDataHandler(MAX_POINTS, new XYGraphLibSWT.RGB(255, 50, 200, 50));
		dhs[0].setLegendText("Voltage");
		dhs[0].setUnit("V");
		dhs[0].setManualScaleMin(0);
		dhs[0].setManualScale(-10, 10);

		dhs[1] = XYPlot.createDataHandler(MAX_POINTS, new XYGraphLibSWT.RGB(55, 250, 200, 50));
		dhs[1].setLegendText("A Switch value with a long title which might be cut");
		dhs[1].setUnit("");

		IXYPlot xyplot = xyPlotCanvas.getXYPlot();
		xyplot.setXAxisText("Time");
		xyplot.setXUnitText("s");
		xyplot.setXRange(0, 0);
		xyplot.setUpdateDelay(200);
		xyplot.setFontSize("Courier", 12, 20);
		xyplot.setBgColor(0, 0, 100);
		xyplot.setDrawAreaBgColor(0, 10, 10);
		xyplot.setAxisColor(255, 255, 255);
		xyplot.setCursorColor(0, 255, 0);
		xyplot.setCursorBgColor(50, 50, 50);
		xyplot.setSmoothScroll(true);

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
					if (saveFileName != null) {
						try {
							IXYPlot xyPlot = xyPlotCanvas.getXYPlot();
							XYPlotPersistence xyPlotPersistence = new XYPlotPersistence();
							xyPlotPersistence.setComment("Test Save");
							xyPlotPersistence.setXDescription(xyPlot.getXAxisText(), xyPlot.getXUnitText());
							xyPlotPersistence.writeData(saveFileName, dhs, new ProgressCallback() {

								@Override
								public void onProgress(int progress) {
									System.out.println(String.format("Write progress: %d", progress));
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
				default:
					break;
				}
			}
		});

		shell.open();
		shell.pack();
	}

	/**
	 * Main loop of the application called in the main method. The application
	 * loops here until it is terminated.
	 */
	void run() {
		Thread timerThread = null;
		IXYPlot xyPlot = xyPlotCanvas.getXYPlot();

		startTime = System.currentTimeMillis();
		if (loadFileName != null) {
			xyPlot.setPaused(true);
			xyPlot.setStartButtonVisible(false);
			xyPlot.setSaveButtonVisible(false);

			XYPlotPersistence xyPlotPersistence = new XYPlotPersistence();
			try {
				dhs = xyPlotPersistence.readData(loadFileName, new ProgressCallback() {

					@Override
					public void onProgress(int progress) {
						System.out.println(String.format("Read progress: %d", progress));
					}
				});
				for (XYPlotData dh : dhs) {
					xyPlot.addDataHandler(dh);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			xyPlot.initXRange(xyPlotPersistence.getXMin(), xyPlotPersistence.getXMax());
		} else {
			xyPlot.setSaveButtonVisible(saveFileName != null);
			xyPlot.setXRange(0, 40);

			for (XYPlotData dh : dhs) {
				xyPlot.addDataHandler(dh);
			}

			timeTicker = new TimeTicker(this, UPDATE_DELAY);
			timerThread = new Thread(timeTicker);
			timerThread.start();
		}
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				xyPlotCanvas.updateControl();
				display.sleep();
			}
		}
		if (timeTicker != null) {
			timeTicker.shutdown();
		}

		if (timerThread != null) {
			try {
				timerThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
				for (XYPlotData dh : dhs) {
					dh.clear();
				}
				startTime = System.currentTimeMillis();
			}
		}
		long atime = System.currentTimeMillis();
		long totaldelta = atime - startTime;
		double x = 0.001 * totaldelta;

		if (isPaused) {
			for (XYPlotData dh : dhs) {
				if (dh != null) {
					dh.setPause();
				}
			}
		} else {
			if (dhs[0] != null) {
				double v = (-14 + random.nextDouble()) * 100000.0;
				dhs[0].addValue(x, v);
			}
			if (dhs[1] != null) {
				if (step == labels.length * 100) {
					step = 0;
				}
				dhs[1].addValue(x, labels[step / 100]);
				step++;
			}
			double min_x = Math.max(x - MOVING_DELTAT, 0);
			IXYPlot xyplot = xyPlotCanvas.getXYPlot();
			xyplot.setXRange(min_x, x);
		}
	}

}
