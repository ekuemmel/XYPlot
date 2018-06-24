package de.ewmksoft.example;

import java.io.IOException;

import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.driver.XYGraphView;

public interface IDataStorage {
	public static final String EXT_FOLDER = "xyplot";

	public abstract void updateXAxis(XYGraphView xyGraphView);

	public abstract void clearData();

	public abstract XYPlotData[] getDataHandlers();

	public abstract void update();

	public abstract boolean isEnabled();

	public abstract void setEnabled(boolean enabled);

	public abstract String save() throws IOException;

	public abstract void restore() throws IOException;

	public void destroy();

}