<h1>XYPlot Library</h1>
<p>
XYPlot is a library written in Java which draws XY plots from 
any kind of data having X/Y values. The X values 
must not be equidistant but must continuously increase (e.g. time).<br>
It separates data and graphical representation and is usually used by 
a business logic thread creating the data and a visualization running 
in an GUI thread. The library is available for SWT (c) Oracle based 
Java applications and for Android (c) Google. Special focus had been put on axis scaling and performance.<br>
The graph can be inspected at runtime using things like zoom in and out, move left/right or change scaling.<br>
</p>
<p>
The library has quite some history, it was first developed in 1990 for my 
lab work in experimental physics at the University in 
Tübingen/Germany (in Borland Pascal) and much later been ported to Java.<br>
</p>
<p>
In this repository you find the library and example applications for SWT and Android.<br>
</p>
<p>
Maybe the library is useful for someone<br>
<br>
Eberhard Kümmel<br>
<br>
2015-05-30	Created repository<br>
2015-09-06	Created tag for version 1.0.4<br>
2016-09-02	New version 1.0.5 available (Features: Display mode without axis scale)<br>
2016-09-17	New version 1.0.6 available (Fixed class version problem with persistence support)<br>
2017-03-26	New version 1.0.7 available (Axis and scale can be hidden)<br>
2018-04-01  New version 1.3.0 available (New repository)<br>
2018-05-14  New version 1.3.3 published on JCenter<br>
2018-06-24  Dropped repository and re-created it again due to change of complete file structure and switch to Gradle scripts<br>
2018-07-12  Improved gesture zoom on Android<br>
2018-07-30  New version 1.3.4 published on JCenter<br>
2018-09-04  New version 1.3.5 published on JCenter<br>
2018-10-10  New version 1.3.8 published on JCenter<br>
2018-11-08  New version 1.3.10 published on JCenter<br>
</p>
<h2>How to use on Android</h2>
<p>
The Android archive is available on JCenter. Include the library into your gradle script via:<br>
implementation 'xyplot_android:xyplot_android:1.3.4@aar'<br>
<br>
Use the following grade dependency to fetch the library from JCenter<br>
implementation 'de.ewmksoft.xyplot:xyplot_android:1.3.5@aar'<br>
<br>
</p>
<h2>How to build</h2>
<p>
The build for xyplot is actually not a real gradle multi build. The different parts must be compiled separately on the command line using gradle in the respective folders.
First the core must be build, then the libraries, then the demo projects.
</p>

