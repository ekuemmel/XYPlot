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
Tübingen/Germany (in Borland Pascal) and many years later been ported to Java.<br>
</p>
<p>
In this repository you find the library and example applications for SWT and Android.<br>
</p>
<p>
May be the library useful for whomever<br>
<br>
Eberhard Kümmel<br>
<br>
2015-05-30   Created repository<br>
2015-09-06   Created tag for version 1.0.4<br>
2016-09-02   New version 1.0.5 available (Features: Display mode without axis scale)<br>
2016-09-17   New version 1.0.6 available (Fixed class version problem with persistence support)<br>
2017-03-26   New version 1.0.7 available (Axis and scale can be hidden)<br>
2018-04-01   New version 1.3.0 available (New repository)<br>
2018-05-14   New version 1.3.3 published on JCenter<br>
2018-06-24   Dropped repository and re-created it again due to change of complete file structure and switch to Gradle scripts<br>
2018-07-12   Improved gesture zoom on Android<br>
2018-07-30   New version 1.3.4 published on JCenter<br>
2018-09-04   New version 1.3.5 published on JCenter<br>
2018-10-10   New version 1.3.8 published on JCenter<br>
2018-11-08   New version 1.3.10 published on JCenter<br>
2018-12-27   New version 1.3.14 published on JCenter<br>
2019-01-13   New version 1.3.15 published on JCenter<br>
2019-01-22   New version 1.3.16 published on JCenter<br>
2019-05-21   New version 1.3.17 published on JCenter<br>
2019-10-05   New version 1.3.18 published on JCenter<br>
2020-02-21   New version 1.3.22 published on JCenter<br>
2020-03-22   New version 1.3.23 published on JCenter<br>
2020-05-22   New version 1.3.24 published on JCenter<br>

</p>
<h2>How to use on Android</h2>
<p>
Use the following grade dependency to fetch the library from JCenter<br>
implementation 'de.ewmksoft.xyplot:xyplot_android:1.3.24@aar'<br>
<br>
</p>
<h2>How to build</h2>
<p>
The following products can be build by executing the commands from top folder.

The Android demo application:
 .\gradlew.bat :demo_android:app:build

 The Android library (.aar):
 .\gradlew.bat :lib_android:xyplot_android:build 

The SWT demo application:
 .\gradlew.bat :demo_swt:build

 The SWT library:
 .\gradlew.bat :lib_swt:xyplot_swt:build 
 
</p>

