/*****************************************************************************
 * 
 *  This file is part of the XYPlot library. The library allows to draw
 *  data in a x/y diagram using several output media.
 * 
 *  Copyright (C) 1994-2015 EWMK-Soft Eberhard Kuemmel
 *
 *  LICENSE AGREEMENT
 * 
 *  WHEREAS, Eberhard Kuemmel is the owner of valuable intellectual 
 *  property rights relating to the XYPlot and wish to license XYPlot
 *  subject to the terms and conditions set forth below;
 *  
 *  and 
 *
 *  WHEREAS, you ("Licensee") acknowledge that Eberhard Kuemmel has the
 *  right to grant licenses to the intellectual property rights relating
 *  to XYPlot, and that you desire to obtain a license to use XYPlot
 *  subject to the terms and conditions set forth below;
 *  
 *  Eberhard Kuemmel grants Licensee a non-exclusive, non-transferable,
 *  royalty-free license to use XYPlot and related materials without
 *  charge provided the Licensee adheres to all of the terms and conditions
 *  of this Agreement.
 * 
 *  By downloading, using, or copying XYPlot or any portion thereof,
 *  Licensee agrees to abide by the intellectual property laws and all
 *  other applicable laws of Germany, and to all of the terms and
 *  conditions of this Agreement, and agrees to take all necessary steps
 *  to ensure that the terms and conditions of this Agreement are not
 *  violated by any person or entity under the Licensee's control or in
 *  the Licensee's service.
 * 
 *  Licensee shall maintain the copyright and trademark notices on the
 *  materials within or otherwise related to XYPlot, and not alter,
 *  erase, deface or overprint any such notice.
 *  
 *  Licensee hereby grants a royalty-free license to any and all 
 *  derivatives based upon this software code base.
 *  
 *  Licensee may modify the sources of XYPlot for the Licensee's own
 *  purposes. Thus, Licensee may not distribute modified sources of
 *  XYPlot without prior written consent from the authors.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *  INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE. 
 *
 *****************************************************************************/

package de.ewmksoft.xyplot.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.ewmksoft.json.JSONArray;
import de.ewmksoft.json.JSONException;
import de.ewmksoft.json.JSONObject;
import de.ewmksoft.xyplot.core.XYPlot;
import de.ewmksoft.xyplot.core.XYPlotData;
import de.ewmksoft.xyplot.core.IXYGraphLib.RGB;
import de.ewmksoft.xyplot.core.XYPlotData.DataValue;

public class XYPlotPersistence {
	private static final String INPUT_CHARSET = "UTF8";
	private static final String DATEFORMAT = "yyyyMMdd.HHmmssZ";
	private static final String GZIPEXT = ".gzip";
	private static final String DHS = "dhs";
	private static final String NUMBER = "number";
	private static final String LEGEND = "legend";
	private static final String XUNIT = "xunit";
	private static final String YUNIT = "xunit";
	private static final String XTEXT = "xtext";
	private static final String COLOR = "color";
	private static final String XDATA = "xdata";
	private static final String YDATA = "ydata";
	private static final String PDATA = "pdata";
	private static final String DATE = "createdate";
	private static final String COMMENT = "comment";

	private SimpleDateFormat sdf;
	private String xtext;
	private String xunit;
	private String comment;
	private String createdate;
	private double xmin, xmax;

	public XYPlotPersistence() {
		sdf = new SimpleDateFormat(DATEFORMAT);
		xmin = Double.POSITIVE_INFINITY;
		xmax = Double.NEGATIVE_INFINITY;
		xtext = "";
		xunit = "";
		comment = "";
		createdate = "";
	}

	public void setXDescription(String xtext, String xunit) {
		this.xtext = xtext;
		this.xunit = xunit;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public Date getDate() {
		Date date = null;
		try {
			date = sdf.parse(createdate);
		} catch (ParseException e) {
		}
		return date;
	}

	public String getXUnit() {
		return xunit;
	}

	public String getXText() {
		return xtext;
	}

	/**
	 * Save plot data into a file
	 * 
	 * @param fileName
	 *            Name of the file
	 * @throws IOException
	 */

	public void writeData(String fileName, XYPlotData[] dataList)
			throws IOException {
		try {
			Date date = new Date();
			createdate = sdf.format(date);
			int no = 0;
			int maxsize = 0;
			JSONArray dhs = new JSONArray();
			JSONObject root = new JSONObject();
			for (XYPlotData data : dataList) {
				if (data == null)
					continue;
				JSONArray parray = new JSONArray();
				JSONArray xarray = new JSONArray();
				JSONArray yarray = new JSONArray();
				for (int i = 0; i < data.length(); ++i) {
					DataValue dataValue = data.getValue(i);
					xarray.put(dataValue.x());
					yarray.put(dataValue.y());
					if (i > maxsize)
						maxsize = i;
					if (dataValue.border()) {
						parray.put(i);
					}
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(YUNIT, data.getUnit());
				jsonObject.put(COLOR, data.getColor().toString());
				jsonObject.put(XDATA, xarray);
				jsonObject.put(YDATA, yarray);
				jsonObject.put(PDATA, parray);
				jsonObject.put(LEGEND, data.getLegendText());
				jsonObject.put(NUMBER, no++);
				dhs.put(jsonObject);
			}
			root.put(DHS, dhs);
			root.put(XTEXT, xtext);
			root.put(XUNIT, xunit);
			root.put(DATE, createdate);
			root.put(COMMENT, comment);
			BufferedOutputStream out = new BufferedOutputStream(
					new GZIPOutputStream(new FileOutputStream(fileName
							+ GZIPEXT)));
			PrintWriter w = new PrintWriter(out);
			try {
				System.out.println("Writing file");
				root.write(w);
			} finally {
				if (w != null) {
					w.close();
				}
			}
		} catch (JSONException e) {
			throw new IOException(e.toString());
		}
	}

	/**
	 * Read data from a file
	 * 
	 * @param fileName
	 *            Name of the file
	 * @throws IOException
	 */
	public XYPlotData[] readData(String fileName) throws IOException {
		if (!fileName.endsWith(GZIPEXT)) {
			fileName += GZIPEXT;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(fileName))));
		BufferedReader reader = new BufferedReader(in);
		StringBuffer sb = new StringBuffer();
		XYPlotData[] dataList = null;
		do {
			int ch = reader.read();
			if (ch >= 0) {
				sb.append((char) ch);
			} else {
				break;
			}
		} while (true);
		try {
			JSONObject root = new JSONObject(sb.toString());
			xtext = new String(root.getString(XTEXT).getBytes(), INPUT_CHARSET);
			xunit = new String(root.getString(XUNIT).getBytes(), INPUT_CHARSET);
			comment = new String(root.getString(COMMENT).getBytes(),
					INPUT_CHARSET);
			createdate = root.getString(DATE);
			JSONArray ja = root.getJSONArray(DHS);
			dataList = new XYPlotData[ja.length()];
			for (int no = 0; no < ja.length(); ++no) {
				JSONObject jsonObject = ja.getJSONObject(no);
				String color = jsonObject.getString(COLOR);
				String yunit = new String(jsonObject.getString(YUNIT)
						.getBytes(), INPUT_CHARSET);
				String legend = new String(jsonObject.getString(LEGEND)
						.getBytes(), INPUT_CHARSET);
				JSONArray xarray = jsonObject.getJSONArray(XDATA);
				JSONArray yarray = jsonObject.getJSONArray(YDATA);
				JSONArray pdata = jsonObject.getJSONArray(PDATA);
				dataList[no] = XYPlot.createDataHandler(xarray.length(),
						new RGB(color));
				dataList[no].setLegendText(legend);
				dataList[no].setUnit(yunit);
				for (int i = 0; i < xarray.length(); ++i) {
					double x = xarray.getDouble(i);
					double y = yarray.getDouble(i);
					if (x < xmin)
						xmin = x;
					if (x > xmax)
						xmax = x;
					dataList[no].addValue(x, y);
					for (int k = 0; k < pdata.length(); ++k) {
						if (pdata.getInt(k) == i) {
							dataList[no].setPause();
							pdata.remove(k);
						}
					}
				}
			}
		} catch (JSONException e) {
			throw new IOException(e.toString());
		} finally {
			reader.close();
		}
		return dataList;
	}

	public double getXMin() {
		return xmin;
	}

	public double getXMax() {
		return xmax;
	}
}
