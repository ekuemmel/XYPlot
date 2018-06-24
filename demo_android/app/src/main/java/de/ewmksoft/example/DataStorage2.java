package de.ewmksoft.example;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import android.os.Environment;
import de.ewmksoft.xyplot.core.IXYGraphLib.RGB;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphView;
import de.ewmksoft.xyplot.utils.XYPlotPersistence;

class DataStorage2 implements IDataStorage {
	private static final String DATA_FILE_NAME = "data2_xyplot";
	private double t;
	private boolean enabled;
	private double lastTime;
	private Random random;
	private XYPlotData[] dhs;
	private static final int MAX_POINTS = 400; // Total points in the plot

	DataStorage2() {
		dhs = new XYPlotData[1];
		int plotPoints = MAX_POINTS / dhs.length;
		dhs[0] = XYPlot
				.createDataHandler(plotPoints, new RGB(255, 50, 150, 50));
		dhs[0].setLegendText("Battery Voltage");
		dhs[0].setUnit("Volt");
		dhs[0].setManualScaleMin(0);
		random = new Random();
		enabled = false;
		clearData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.graphview.IDataStorage#clearData()
	 */
	@Override
	public void clearData() {
		for (XYPlotData dh : dhs) {
			dh.clear();
		}
		t = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.graphview.IDataStorage#getDataHandler()
	 */
	@Override
	public XYPlotData[] getDataHandlers() {
		return dhs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.graphview.IDataStorage#update()
	 */
	@Override
	public void update() {
		if (!enabled) {
			return;
		}
		long now = System.currentTimeMillis();
		double delta = now - lastTime;
		lastTime = now;
		t += delta;
		double y = random.nextDouble() * 20;
		dhs[0].addValue(t, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.graphview.IDataStorage#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.graphview.IDataStorage#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			lastTime = System.currentTimeMillis();
		} else {
			for (XYPlotData dh : dhs) {
				dh.setPause();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ewmksoft.graphview.IDataStorage#save()
	 */
	public String save() throws IOException {
		String homePath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + EXT_FOLDER;
		File home = new File(homePath);
		if (!home.exists()) {
			if (!home.mkdirs()) {
				throw new IOException("Can not create folder: "
						+ home.toString());
			}
		}
		XYPlotPersistence xyPlotPersistence = new XYPlotPersistence();
		String result = home.getPath() + File.separator + DATA_FILE_NAME;
		xyPlotPersistence.writeData(result, dhs);
		return result;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateXAxis(XYGraphView xyGraphView) {
		xyGraphView.setVisibleLastX(1000);
	}

	@Override
	public void restore() throws IOException {
		String homePath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + EXT_FOLDER;
		XYPlotPersistence xyPlotPersistence = new XYPlotPersistence();
		dhs = xyPlotPersistence.readData(homePath + File.separator
				+ DATA_FILE_NAME);
		if (dhs != null && dhs.length > 0) {
			t = dhs[0].getXMax();
		}
	}

}
