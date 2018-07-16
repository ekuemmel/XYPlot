/*
 ****************************************************************************
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

package de.ewmksoft.xyplot.driver;

import de.ewmksoft.xyplot.core.IXYGraphLib;
import de.ewmksoft.xyplot.core.IXYGraphLibInt;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

class XYGraphLibAndroid implements IXYGraphLib {
	private XYGraphLibIntAndroid xyGraphLibIntAndroid;
	
    XYGraphLibAndroid(View owner, Rect bounds) {
        xyGraphLibIntAndroid = new XYGraphLibIntAndroid(owner, bounds);
    }

    public void close() {

    }

    @Override
    public boolean hasZoomBox() {
        return false;
    }

    void setButtonSizeRatio(float r) {
        xyGraphLibIntAndroid.setButtonSizeRatio(r);
    }

    void paint(Canvas canvas) {
        xyGraphLibIntAndroid.paint(canvas);
    }

    public Rect getBounds() {
        return xyGraphLibIntAndroid.getBounds();
    }

    public void setBounds(IXYGraphLib.Rect rect) {
        xyGraphLibIntAndroid.setBounds(rect);
    }

    public IXYGraphLibInt getInt() {
        return xyGraphLibIntAndroid;
    }

    void setImageDrawable(IXYGraphLibInt.ButtonImages button, Drawable drawable) {
        xyGraphLibIntAndroid.setImageDrawable(button, drawable);
    }
  		
	void setAxisColor(int redColor,int green,int blue)
    {
        xyGraphLibIntAndroid.setAxisColor(redColor,green,blue);
    }

}
