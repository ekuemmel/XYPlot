XYPlot is a library written in Java which draws XY plots from 
any kind of data having X and Y value. The X values 
must continuously increase (e.g. time) but not necessarily be 
equidistant.
It separates data and graphical representation and is usually used by 
a business logic thread creating the data and a visualization running 
in an GUI thread. The library is available for SWT (c) Oracle based 
Java applications and for Android (c) Google. Special focus had 
been put on axis scaling and performance.
The graph can be inspected at runtime using things like zoom in and out, move left/right or change scaling.

The library has quite some history, it was first developed for my 
lab work in experimental physics at the University in 
Tübingen/Germany (in Borland Pascal) and much later been ported to Java.

In the repo you find the library and an example application for SWT and Android

Maybe the library is useful for someone

Eberhard Kümmel

2015-05-30	Created repo
2015-09-06	Created tag for version 1.0.4
2016-09-02	New version 1.0.5 available (Features: Display mode without axis scale)
2016-09-17	New version 1.0.6 available (Fixed class version problem with persistence support)
2017-03-26	New version 1.0.7 available (Axis and scale can be hidden)
2018-04-01  New version 1.3.0 available (New repository)
2018-06-24  Dropped repo and created it new due to change to gradle scripts
