package de.interoberlin.lymbo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import de.interoberlin.lymbo.controller.LymbosController;
import de.interoberlin.lymbo.controller.SplashController;
import de.interoberlin.lymbo.controller.accelerometer.Simulation;
import de.interoberlin.lymbo.view.activities.LymbosActivity;
import de.interoberlin.sauvignon.lib.controller.loader.SvgLoader;
import de.interoberlin.sauvignon.lib.model.svg.SVG;
import de.interoberlin.sauvignon.lib.model.svg.elements.AGeometric;
import de.interoberlin.sauvignon.lib.model.svg.elements.rect.SVGRect;
import de.interoberlin.sauvignon.lib.model.svg.transform.transform.SVGTransformTranslate;
import de.interoberlin.sauvignon.lib.model.util.SVGPaint;
import de.interoberlin.sauvignon.lib.view.SVGPanel;

public class SplashActivity extends Activity {
    // Controllers
    SplashController splashController = SplashController.getInstance();
    LymbosController lymbosController = LymbosController.getInstance();

    private static Context context;
    private static Activity activity;

    private static LinearLayout llBackground;
    private static TextView tvMessage;

    private static SensorManager sensorManager;
    private WindowManager windowManager;
    private static Display display;

    private static SVG svg;
    private static SVGPanel panel;
    private static ImageView ivLogo;

    private static boolean ready = false;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get activity and context
        activity = this;
        context = getApplicationContext();

        // Load layout
        llBackground = (LinearLayout) findViewById(R.id.llBackground);
        tvMessage = (TextView) findViewById(R.id.tvMessage);

        // Get instances of managers
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();

        svg = SvgLoader.getSVGFromAsset(context, "lymbo.svg");

        panel = new SVGPanel(activity);
        panel.setSVG(svg);
        panel.setBackgroundColor(new SVGPaint(255, 208, 227, 153));
        panel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ready) {
                    Intent openStartingPoint = new Intent(SplashActivity.this, LymbosActivity.class);
                    startActivity(openStartingPoint);
                }
            }
        });

        ivLogo = new ImageView(activity);
        ivLogo.setImageDrawable(loadFromAssets("lymbo.png"));

        // Add surface view
        llBackground.addView(panel, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        llBackground.addView(ivLogo, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Initialize
        uiInit();

        Thread timer = new Thread() {
            public void run() {
                tvMessage.setText(R.string.search_lymbo_files);

                splashController.loadMessages();

                Collections.shuffle(splashController.getMessages());

                uiMessage(splashController.getMessages().get(0));

                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int i = 0;

                while (lymbosController.getLymboFiles().isEmpty()) {
                    uiMessage(splashController.getMessages().get(i++));

                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                uiMessage("Found " + lymbosController.getLymboFiles().size() + " lymbo files");

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                uiMessage(R.string.click_to_continue);

                ready = true;
            }
        };
        timer.start();
    }

    public void onResume() {
        super.onResume();
        panel.resume();

        draw();

        Simulation.getInstance(activity).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        panel.pause();

        Simulation.getInstance(activity).stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    // --------------------
    // Methods
    // --------------------

    public static void draw() {
    }

    private Drawable loadFromAssets(String image) {
        try {
            InputStream is = getAssets().open(image);
            return Drawable.createFromStream(is, null);
        } catch (IOException ex) {
            return null;
        }
    }

    public static void uiInit() {
    }

    public static void uiMessage(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMessage.setText(message);
            }
        });
    }

    public static void uiMessage(final int message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMessage.setText(message);
            }
        });
    }

    public static void uiUpdate() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (svg) {
                    for (AGeometric e : svg.getAllSubElements()) {
                        if (e instanceof SVGRect) {
                            float x = Simulation.getRawX() * (e.getzIndex() - svg.getMaxZindex() / 2) * -5;
                            float y = Simulation.getRawY() * (e.getzIndex() - svg.getMaxZindex() / 2) * -5;

                            System.out.println("DEBUG " + svg.getMaxZindex());

                            e.getAnimationSets().clear();
                            e.setAnimationTransform(new SVGTransformTranslate(x, y));
                        }
                    }
                }
            }
        });

        t.start();
    }

    public static void uiDraw() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                draw();
            }
        });
    }

    public static void uiToast(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --------------------
    // Getters / Setters
    // --------------------

    public Display getDisplay() {
        return display;
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }
}