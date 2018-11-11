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
    private boolean enabled;
    private double startTime;
    private Random random;
    private XYPlotData[] dhs;
    private double y = 0;
    private static final int MAX_POINTS = 4096; // Total points in the plot

    private final String[] labels = {"Under Limit", "In Limit", "Above Limit"};
    private double xMin;
    private double xMax;

    DataStorage2() {
        dhs = new XYPlotData[2];
        int plotPoints = MAX_POINTS / dhs.length;
        dhs[0] = XYPlot
                .createDataHandler(plotPoints, new RGB(255, 50, 150, 50));
        dhs[0].setLegendText("Battery Voltage");
        dhs[0].setUnit("Volt");
        dhs[1] = XYPlot
                .createDataHandler(plotPoints, new RGB(255, 150, 50, 50));
        dhs[1].setLegendText("Limit");
        random = new Random();
        enabled = true;
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
        y = 0;
        startTime = System.currentTimeMillis();
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
        double delta = 0.001 * (now - startTime);
        y = y + (random.nextDouble() - 0.5);
        dhs[0].addValue(delta, y);
        if (y > 3) {
            dhs[1].addValue(delta, labels[2]);
        } else if (y < -3) {
            dhs[1].addValue(delta, labels[0]);
        } else {
            dhs[1].addValue(delta, labels[1]);
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
        xyPlotPersistence.writeData(result, dhs);
        return result;
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateXAxis(XYGraphView xyGraphView) {
        xyGraphView.setVisibleLastX(30);
    }

    @Override
    public void restore() throws IOException {
        String homePath = Environment.getExternalStorageDirectory().getPath()
                + File.separator + EXT_FOLDER;
        XYPlotPersistence xyPlotPersistence = new XYPlotPersistence();
        dhs = xyPlotPersistence.readData(homePath + File.separator
                + DATA_FILE_NAME);
    }

}
