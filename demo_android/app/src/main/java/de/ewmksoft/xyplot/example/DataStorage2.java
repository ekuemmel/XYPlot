package de.ewmksoft.xyplot.example;

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
	private static final double MOVING_DELTAT = 30;
    private boolean enabled;
    private double startTime;
    private Random random;
    private XYPlotData[] dhs;
    private double y = 0;
    private double startX = 0;
    private static final int MAX_POINTS = 200000; // Total points in the plot

    private final String[] labels = {"Under Limit", "In Limit", "Above Limit"};
    private double xMin;
    private double xMax;
	private double x;

    DataStorage2() {
        random = new Random();
        enabled = true;
        dhs = new XYPlotData[2];
        clearData();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.ewmksoft.graphview.IDataStorage#clearData()
     */
    @Override
    public void clearData() {
        y = 0;
        startX = 0;
        startTime = System.currentTimeMillis();
        dhs[0] = XYPlot
                .createDataHandler(MAX_POINTS, new RGB(255, 50, 150, 50));
        dhs[0].setLegendText("Battery Voltage");
        dhs[0].setUnit("Volt");
        dhs[0].clear();
        dhs[1] = XYPlot
                .createDataHandler(MAX_POINTS, new RGB(255, 150, 50, 50));
        dhs[1].setLegendText("Limit");
        dhs[1].getNumberOfDecimalPlaces();
        dhs[1].clear();
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

    @Override
    public String getXName() {
        return "time";
    }


    @Override
    public String getXUnit() {
        return "s";
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
        x = startX + 0.001 * (now - startTime);
        y = y + 10000.0 * (random.nextDouble() - 0.5);
        dhs[0].addValue(x, y);
        if (y > 3) {
            dhs[1].addValue(x, labels[2]);
        } else if (y < -3) {
            dhs[1].addValue(x, labels[0]);
        } else {
            dhs[1].addValue(x, labels[1]);
        }
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
        if (!enabled) {
            for (XYPlotData dh : dhs) {
                dh.setPause();
            }
        }
    }

    @Override
    public void setXMin(double xMin) {
        this.xMin = xMin;
    }

    @Override
    public void setXMax(double xMax) {
        this.xMax = xMax;
    }

    @Override
    public double getXMin() {
        return xMin;
    }

    @Override
    public double getXMax() {
        return xMax;
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
        xyPlotPersistence.writeData(result, dhs, new XYPlotPersistence.ProgressCallback() {
            @Override
            public void onProgress(int i) {

            }
        });
        return result;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateXAxis(XYGraphView xyGraphView) {
		double min_x = Math.max(x - MOVING_DELTAT, 0);
        xyGraphView.setVisibleXRange(min_x, x);
    }

    @Override
    public void restore() throws IOException {
        String homePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + EXT_FOLDER;
        XYPlotPersistence xyPlotPersistence = new XYPlotPersistence();
        dhs = xyPlotPersistence.readData(homePath + File.separator
                + DATA_FILE_NAME);
        setXMin(xyPlotPersistence.getXMin());
        startX = xyPlotPersistence.getXMax();
        setXMax(startX);
    }

}
