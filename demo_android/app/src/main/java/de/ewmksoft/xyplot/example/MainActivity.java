package de.ewmksoft.xyplot.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

import de.ewmksoft.xyplot.core.IXYPlot;
import de.ewmksoft.xyplot.driver.IXYGraphView;
import de.ewmksoft.xyplot.driver.XYGraphView;

public class MainActivity extends Activity implements Handler.Callback {
    private Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final int MSG_TAG_UPDATE = 1;
    private static final int MSG_TAG_SAVE = 2;
    private static final int MSG_TAG_LEGEND = 3;
    private IDataStorage dataStorage;
    private Thread dataThread;
    private XYGraphView xyGraphView;
    private Handler myHandler;
    private int legendCount = 0;

    public MainActivity() {
        super();
        myHandler = new Handler(this);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        xyGraphView = (XYGraphView) findViewById(R.id.xyPlot1);

        MyApplication application = (MyApplication) getApplication();
        int dataStorageNum = application.getCurrentDataStorageNum();
        if (dataStorageNum > 0) {
            initGraph(dataStorageNum, false);
        }

        // uncomment below line to set axis color.
        // xyGraphView.setAxisColor(getResources().getColor(R.color.magenta_light));
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        Button button4 = (Button) findViewById(R.id.button4);
        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initGraph(1, false);
            }
        });
        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                legendCount = 1;
                initGraph(2, true);
            }
        });
        button3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                initGraph(3, false);
            }
        });
        button4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                ViewGroup.LayoutParams layoutParams = xyGraphView.getLayoutParams();
                int height = random.nextInt(1500);
                if (height < 100) {
                    height = 0;
                }
                layoutParams.height = height;
                xyGraphView.setLayoutParams(layoutParams);
            }
        });

        dataThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        if (dataStorage != null) {
                            dataStorage.update();
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        dataThread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        if (dataStorage != null) {
            IXYPlot xyPlot = xyGraphView.getXYPlot();
            dataStorage.setXMin(xyPlot.getXMin());
            dataStorage.setXMax(xyPlot.getXMax());
        }
    }

    @Override
    public void onDestroy() {
        dataThread.interrupt();
        try {
            dataThread.join(2000);
        } catch (InterruptedException e) {
            // Ignore
        }
        super.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_TAG_UPDATE:
                if (dataStorage != null && dataStorage.isEnabled()) {
                    dataStorage.updateXAxis(xyGraphView);
                }
                myHandler.removeMessages(MSG_TAG_UPDATE);
                myHandler.sendEmptyMessageDelayed(MSG_TAG_UPDATE, 100);
                break;
            case MSG_TAG_LEGEND:
                if (2 == legendCount) {
                    IXYPlot xyPlot = xyGraphView.getXYPlot();
                    xyPlot.setLegendExpanded(true);
                    xyGraphView.invalidate();
                    myHandler.sendEmptyMessageDelayed(MSG_TAG_LEGEND, 4000);
                    legendCount = 3;
                } else if (3 == legendCount) {
                    IXYPlot xyPlot = xyGraphView.getXYPlot();
                    xyPlot.setLegendExpanded(false);
                    xyGraphView.invalidate();
                    legendCount = 4;
                }
                break;
            case MSG_TAG_SAVE:
                if (dataStorage != null) {
                    dataStorage.setEnabled(false);
                    try {
                        String result = dataStorage.save();
                        Toast.makeText(this, "Saved: " + result, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Error on save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void initGraph(final int dataStorageNum, boolean loadPrevData) {
        MyApplication application = (MyApplication) getApplication();

        final IXYPlot xyPlot = xyGraphView.getXYPlot();

        if (dataStorage != null) {
            dataStorage.setXMin(xyPlot.getXMin());
            dataStorage.setXMax(xyPlot.getXMax());
        }

        dataStorage = application.getDataStorage(dataStorageNum, this);

        if (loadPrevData) {
            try {
                xyPlot.setPaused(true);
                dataStorage.restore();
                dataStorage.setEnabled(false);
            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        xyGraphView.setKeyListener(new IXYGraphView() {
            public void setEnabled(boolean enabled) {
                dataStorage.setEnabled(enabled);
                if (!enabled && 1 == legendCount) {
                    myHandler.sendEmptyMessageDelayed(MSG_TAG_LEGEND, 2000);
                    legendCount = 2;
                }
            }

            public boolean isEnabled() {
                return dataStorage.isEnabled();
            }

            public void clear() {
                dataStorage.clearData();
                initGraph(dataStorageNum, false);
            }

            public void save() {
                myHandler.sendEmptyMessageDelayed(MSG_TAG_SAVE, 100);
            }
        });
        xyPlot.setFontSize(15, 20);
        xyPlot.setAxisLabels(true);
        xyPlot.setLegendExpanded(true);
        xyPlot.setSaveButtonVisible(true);
        xyPlot.setAllowPauseOnDataClick(false);
        xyPlot.setSmoothScroll(true);
        xyPlot.setXAxisText(dataStorage.getXName());
        xyPlot.setXUnitText(dataStorage.getXUnit());
        xyPlot.setDataHandlers(dataStorage.getDataHandlers());

        xyGraphView.initXRange(dataStorage.getXMin(), dataStorage.getXMax());

        myHandler.sendEmptyMessage(MSG_TAG_UPDATE);
    }
}