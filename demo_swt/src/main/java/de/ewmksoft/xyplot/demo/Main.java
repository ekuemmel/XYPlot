package de.ewmksoft.xyplot.demo;

import org.eclipse.swt.widgets.Display;

/**
 * Hello World demo application for XYPlot library
 * 
 * @author Eberhard Kuemmel
 */

/**
 * Main class having main() method as and starting point of the application.
 * 
 */
public class Main {

	private static int APPLICATION_TYPE = 3;

	/**
	 * @param args
	 *            Not used
	 */
	public static void main(String[] args) {
		Display display = new Display();
		if (APPLICATION_TYPE == 1) {
			Application1 appl = new Application1(display);
			appl.run(); // does not return until application is closed
		} else if (APPLICATION_TYPE == 2) {
			Application2 appl = new Application2(display);
			appl.run(); // does not return until application is closed
		} else {
			Application3 appl = new Application3(display);
			appl.run(); // does not return until application is closed
		}

		System.out.println("All done");
	}
}
