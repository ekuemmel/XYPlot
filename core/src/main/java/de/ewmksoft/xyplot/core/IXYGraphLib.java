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

package de.ewmksoft.xyplot.core;

/**
 * Abstract graphic library interface for XYPlot. A implementation must
 * implement all this functions to provide drawing functionality for XYPlot
 * graph.
 * 
 */
public interface IXYGraphLib {
	/**
	 * 
	 * 
	 * 
	 */
	void close();

	/**
	 * Get rectangle containing the XY-Plot
	 * 
	 * @return Rectangle
	 */
	Rect getBounds();

	/**
	 * Set rectangle containing the XY-Plot
	 * 
	 * @param rect
	 *            New bounds of plot area
	 */
	void setBounds(IXYGraphLib.Rect rect);

	/**
	 * Get access to the internal functionality of the graphic library. (Only
	 * for internal purpose, don't use this call)
	 * 
	 * @return Interface to internal functionality
	 */
	IXYGraphLibInt getInt();

	/**
	 * A zoom box is a rectangle drawn by a mouse down, move and up event within
	 * the drawing area.
	 * 
	 * @return True if zoom box is supported
	 */
	boolean hasZoomBox();

	enum MouseEvent {
		MOUSEDOWN, MOUSEUP, MOUSEMOVE, MOUSEDOUBLETAP, MOUSESINGLETAP
	}

	public class RGB {
		public RGB(int alpha, int red, int green, int blue) {
			this.alpha = alpha;
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		public RGB(String color) {
			String[] s = color.split("/");
			if (s.length == 4) {
				try {
					this.alpha = Integer.parseInt(s[0]);
					this.red = Integer.parseInt(s[1]);
					this.green = Integer.parseInt(s[2]);
					this.blue = Integer.parseInt(s[3]);
				} catch (NumberFormatException e) {

				}
			}
		}

		public String toString() {
			return alpha + "/" + red + "/" + green + "/" + blue;
		}

		public int alpha;
		public int red;
		public int green;
		public int blue;
	}

	public class Rect {
		public Rect(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public int x;
		public int y;
		public int height;
		public int width;

		public boolean contains(int x, int y) {
			boolean result = false;
			result = (x >= this.x && y >= this.y && x <= this.x + width && y <= this.y + height);
			return result;
		}
	}

}
