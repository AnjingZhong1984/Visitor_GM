package fafa.com.visitor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 外部注册
 *
 * @author Silence
 */
public class StaffActivity extends AppCompatActivity {

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private TextView veryfyText;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    protected Handler.Callback callback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.obj.toString().equals("error")) {
                veryfyText.setText("GID验证错误，请输入正确GID");
            } else if (msg.obj.toString().equals("init")) {
                veryfyText.setVisibility(View.VISIBLE);
                veryfyText.setText("验证中...请稍候");
            } else {
                veryfyText.setVisibility(View.GONE);
                findViewById(R.id.verfyBtn).setVisibility(View.GONE);
                EditText hname = findViewById(R.id.visitName);
                hname.setText(msg.obj.toString());
                hname.setVisibility(View.VISIBLE);
                findViewById(R.id.visitNameDesc).setVisibility(View.VISIBLE);
                findViewById(R.id.remarkDesc).setVisibility(View.VISIBLE);
                findViewById(R.id.remark).setVisibility(View.VISIBLE);
                findViewById(R.id.departmentDesc).setVisibility(View.VISIBLE);
                findViewById(R.id.department).setVisibility(View.VISIBLE);
                findViewById(R.id.badageNoDesc).setVisibility(View.VISIBLE);
                findViewById(R.id.badageNo).setVisibility(View.VISIBLE);
            }
            return true;
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);
        mContentView = findViewById(R.id.fullscreen_content_s);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.submit).setOnClickListener(new DataHandler.DataSubmitListener().builder(this, 2));
        findViewById(R.id.reset).setOnClickListener(new DataHandler.DataRestListener().builder(this));

        veryfyText = findViewById(R.id.verfyResult);

        final EditText editText = findViewById(R.id.hCode);
        findViewById(R.id.verfyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (editText.getText().toString().trim().length() > 0) {
                            Message message1 = new Message();
                            message1.obj = "init";
                            new Handler(Looper.getMainLooper(), callback).sendMessage(message1);
                            //调用域信息，获取值
                            JSONObject jsonObject = new JSONObject();
                            JSONObject rep = null;
                            try {
                                jsonObject.put("gid", editText.getText());
                                rep = DataHandler.executePost(jsonObject, "http://153.13.200.56:28080/ldap/readLdap");
                                Message message = new Message();
                                if (rep != null && rep.getBoolean("success")) {
                                    message.obj = rep.getString("cn");
                                } else {
                                    message.obj = "error";
                                }
                                new Handler(Looper.getMainLooper(), callback).sendMessage(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }).start();
            }
        });

        TimerTask activeTask = new TimerTask() {
            @Override
            public void run() {
                hide();
            }
        };
        new Timer().schedule(activeTask, 0, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.out.println("============");
        delayedHide(100);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

}
