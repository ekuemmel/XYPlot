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

class DataStorage1 implements IDataStorage {
	private boolean enabled;
	private int x;
	private Random random;
	private XYPlotData[] dhs;
	private static final int MAX_POINTS = 1024; // Total points in the plot
	private static final String DATA_FILE_NAME = "data1_xyplot";

	DataStorage1() {
		dhs = new XYPlotData[3];
		dhs[0] = XYPlot
				.createDataHandler(MAX_POINTS, new RGB(255, 50, 150, 50));
		dhs[0].setLegendText("Battery Voltage");
		dhs[0].setUnit("Volt");
		dhs[0].setManualScaleMin(0);

		dhs[1] = XYPlot
				.createDataHandler(MAX_POINTS, new RGB(255, 250, 50, 50));
		dhs[1].setLegendText("Upper Range");
		dhs[1].setUnit("Volt");
		dhs[1].setManualScaleMin(0);

		dhs[2] = XYPlot
				.createDataHandler(MAX_POINTS, new RGB(255, 250, 50, 50));
		dhs[2].setLegendText("Lower Range");
		dhs[2].setUnit("Volt");
		dhs[2].setManualScaleMin(0);
		clearData();
		random = new Random();
		enabled = false;
	}

	public void clearData() {
		for (XYPlotData dh : dhs) {
			dh.clear();
		}
		for (int i = 0; i < MAX_POINTS; ++i) {
			dhs[0].addValue(i, 0);
		}
		for (int i = 0; i < MAX_POINTS; ++i) {
			dhs[1].addValue(i, 14);
		}
		for (int i = 0; i < MAX_POINTS; ++i) {
			dhs[2].addValue(i, 10);
		}
		x = 0;
	}

	public XYPlotData[] getDataHandlers() {
		return dhs;
	}

	public void update() {
		if (!enabled) {
			return;
		}
		double dy = random.nextInt(8);
		double[] y = new double[MAX_POINTS];
		for (int i = 0; i < MAX_POINTS; ++i) {
			double voltage = 13;
			if ((++x / 32) % 2 == 1) {
				voltage -= dy;
			}
			y[i] = voltage;
		}
		dhs[0].changeValues(y);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

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
		return result;	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateXAxis(XYGraphView xyGraphView) {
		xyGraphView.setVisibleXRange(0, MAX_POINTS);
	}

	@Override
	public void restore() {
		// TODO Auto-generated method stub

	}

}
