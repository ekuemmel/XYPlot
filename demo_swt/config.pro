-injars       ./build/libs/DemoSwt-1.3.24.jar
-outjars      app.jar
-libraryjars  <java.home>/lib/rt.jar(!**.jar;!module-info.class)
-printmapping myapplication.map

-keep public class de.ewmksoft.xyplot.demo.Main {
    public static void main(java.lang.String[]);
}


#-keep class org.eclipse.swt.**

-keep class org.eclipse.** {
    <fields>;
    <methods>;
}