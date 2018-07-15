package de.ewmksoft.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import de.ewmksoft.xyplot.core.IXYPlot;
import de.ewmksoft.xyplot.driver.IXYGraphView;
import de.ewmksoft.xyplot.driver.XYGraphView;

public class MainActivity extends Activity implements Handler.Callback {
    private Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private static final int MSG_TAG_UPDATE = 1;
    private static final int MSG_TAG_SAVE = 2;
    private IDataStorage dataStorage;
    private Thread dataThread;
    private XYGraphView xyGraphView;
    private Handler myHandler;

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

        xyGraphView = (XYGraphView) findViewById(R.id.xyPlot1);

        IXYPlot xyPlot = xyGraphView.getXYPlot();
        xyPlot.setFontSize(20, 30);
        xyPlot.setAxisLabels(true);
        xyPlot.setSaveButtonVisisble(true);
        // uncomment below line to set axis color.
        //xyGraphView.setAxisColor(getResources().getColor(R.color.magenta_light));
        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);
        Button button3 = (Button) findViewById(R.id.button3);
        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                initGraph(1, false);
            }
        });
        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                initGraph(2, false);
            }
        });
        button3.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                initGraph(3, false);
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
                if (dataStorage != null) {
                    dataStorage.updateXAxis(xyGraphView);
                    if (dataStorage.isEnabled()) {
                        myHandler.sendEmptyMessageDelayed(MSG_TAG_UPDATE, 100);
                    }
                }
                break;
            case MSG_TAG_SAVE:
                if (dataStorage != null) {
                    dataStorage.setEnabled(false);
                    try {
                        String result = dataStorage.save();
                        Toast.makeText(this, "Saved: " + result, Toast.LENGTH_SHORT)
                                .show();
                    } catch (IOException e) {
                        Toast.makeText(this, "Error on save: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void initGraph(int dataStorageNum, boolean loadPrevData) {
        MyApplication application = (MyApplication) getApplication();
        dataStorage = application.getDataStorage(dataStorageNum, this);
        dataStorage.setEnabled(true);
        if (loadPrevData) {
            try {
                dataStorage.restore();
            } catch (IOException e) {
                Toast.makeText(this, "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        xyGraphView.init("Time", "s", dataStorage.getDataHandlers(),
                new IXYGraphView() {
                    public void setEnabled(boolean enabled) {
                        dataStorage.setEnabled(enabled);
                        myHandler.sendEmptyMessageDelayed(1, 100);
                    }

                    public boolean isEnabled() {
                        return dataStorage.isEnabled();
                    }

                    public void clear() {
                        dataStorage.clearData();
                    }

                    public void save() {
                        myHandler.sendEmptyMessageDelayed(MSG_TAG_SAVE, 100);
                    }
                });
        myHandler.sendEmptyMessage(MSG_TAG_UPDATE);
    }
}