package de.ewmksoft.xyplot.example;

import java.io.IOException;

import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphView;

public interface IDataStorage {
    String EXT_FOLDER = "xyplot";

    void updateXAxis(XYGraphView xyGraphView);

    void clearData();

    XYPlotData[] getDataHandlers();

    String getXName();

    String getXUnit();

    void update();

    boolean isEnabled();

    double getXMin();

    double getXMax();

    void setEnabled(boolean enabled);

    void setXMin(double xMin);

    void setXMax(double xMax);

    String save() throws IOException;

    void restore() throws IOException;

    void destroy();

}