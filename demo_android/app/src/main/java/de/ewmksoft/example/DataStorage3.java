package de.ewmksoft.example;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import de.ewmksoft.xyplot.core.IXYGraphLib.RGB;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphView;
import de.ewmksoft.xyplot.utils.XYPlotPersistence;

class DataStorage3 implements IDataStorage, SensorEventListener {
	private boolean enabled;
	private XYPlotData[] dhs;
	private double[] y;
	private double[] mp;
	private static final int MAX_POINTS = 1024; // Total points in the plot
	private static final int MP_POS = -5;
	private static final String DATA_FILE_NAME = "data3_xyplot";
	private int plotPoints;
	private SensorManager sensorManager;
	private int x;

	DataStorage3(Context context) {
		dhs = new XYPlotData[2];
		plotPoints = MAX_POINTS;
		dhs[0] = XYPlot
				.createDataHandler(plotPoints, new RGB(255, 50, 150, 50));
		dhs[0].setLegendText("Sensordata");
		dhs[0].setUnit("unit");
		dhs[0].setManualScale(-5, 25);
		dhs[1] = XYPlot.createDataHandler(plotPoints, new RGB(255, 0, 0, 0));
		dhs[1].setLegendText("MP");
		dhs[1].setUnit("unit");
		dhs[1].setManualScale(-5, 25);
		enabled = false;
		mp = new double[plotPoints];
		y = new double[plotPoints];
		clearData();
		sensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void destroy() {
		sensorManager.unregisterListener(this);
	}

	@Override
	public void clearData() {
		for (XYPlotData dh : dhs) {
			dh.clear();
		}
		for (int i = 0; i < plotPoints; ++i) {
			dhs[0].addValue(i, 0);
			dhs[1].addValue(i, 0);
			y[i] = 9.81;
			mp[i] = MP_POS;
		}
		x = 0;
	}

	@Override
	public XYPlotData[] getDataHandlers() {
		return dhs;
	}

	@Override
	public void update() {
		if (enabled) {
			dhs[0].changeValues(y);
			dhs[1].changeValues(mp);
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
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
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float sum = 0;
		for (int i = 0; i < 3; ++i) {
			sum += Math.pow(event.values[i], 2);
		}
		y[x] = Math.sqrt(sum);
		mp[x] = MP_POS + 1;
		if (x > 0) {
			mp[x - 1] = MP_POS;
		}
		if (++x == plotPoints) {
			x = 0;
			mp[plotPoints - 1] = MP_POS;
		}
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
