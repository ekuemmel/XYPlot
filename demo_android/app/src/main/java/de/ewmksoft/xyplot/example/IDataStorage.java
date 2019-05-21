package de.ewmksoft.xyplot.example;

import java.io.IOException;

import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphView;

public interface IDataStorage {
	public static final String EXT_FOLDER = "xyplot";

	public abstract void updateXAxis(XYGraphView xyGraphView);

	public abstract void clearData();

	public abstract XYPlotData[] getDataHandlers();
	
	public abstract String getXName();
	
	public abstract String getXUnit();

	public abstract void update();

	public abstract boolean isEnabled();

	public abstract double getXMin();

	public abstract double getXMax();

	public abstract void setEnabled(boolean enabled);

    public abstract void setXMin(double xMin);

    public abstract void setXMax(double xMax);

	public abstract String save() throws IOException;

	public abstract void restore() throws IOException;

	public void destroy();

}