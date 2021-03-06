package fafa.com.visitor;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import fafa.com.visitor.util.UsbPrintUtil;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Silence
 */
public class DataHandler {
    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected final static String SERVER_HOST = "http://153.13.200.56:6161";
//    protected final static String SERVER_HOST = "http://192.168.1.155:6161";

    private static String inputStreamToString(InputStream inputStream) throws IOException {
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "UTF-8");
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0) {
                break;
            }
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    public static JSONObject executePost(JSONObject object, String host) {
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        System.out.println(host);
        try {
            URL url = new URL(host);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            /* optional request header */
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            /* optional request header */
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("accept", "application/json");
            // read response
            /* for Get request */
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write(object.toString().getBytes());
            wr.flush();
            wr.close();
            // try to get response
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String rep = inputStreamToString(inputStream);
                Log.i("response:", rep);
                return new JSONObject(rep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    private final static int SHOW_MESSAGE = 0;//显示消息
    private final static int LOADING_SHOW = 1;//显示加载
    private final static int LOADING_HIDE = 2;//显示加载
    private final static int FORM_ClEAR = 3;//清空表单
    private final static int SHOW_MESSAGE_LONG = 4;//显示消息

    static class PageHandler extends Handler {
        Activity context;

        TextView loading;

        public Handler buildContext(Activity context) {
            this.context = context;
            this.loading = context.findViewById(R.id.loading);
            return this;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_MESSAGE:
                    loading.setVisibility(View.INVISIBLE);
                    Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_MESSAGE_LONG:
                    loading.setVisibility(View.INVISIBLE);
                    Toast.makeText(context, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case LOADING_SHOW:
                    loading.setVisibility(View.VISIBLE);
                    break;
                case LOADING_HIDE:
                    loading.setVisibility(View.INVISIBLE);
                    break;
                case FORM_ClEAR:
                    loading.setVisibility(View.INVISIBLE);
                    EditText name = context.findViewById(R.id.name);
                    EditText department = context.findViewById(R.id.department);
                    EditText email = context.findViewById(R.id.email);
                    EditText visitCompany = context.findViewById(R.id.visitCompany);
                    EditText visitName = context.findViewById(R.id.visitName);
                    EditText visitMobile = context.findViewById(R.id.visitMobile);
                    EditText reason = context.findViewById(R.id.reason);
                    EditText hcode = context.findViewById(R.id.hCode);
                    EditText badage = context.findViewById(R.id.badageNo);
                    EditText remark = context.findViewById(R.id.remark);
                    if (name != null) {
                        name.setText("");
                    }
                    if (department != null) {
                        department.setText("");
                    }
                    if (email != null) {
                        email.setText("");
                    }
                    if (visitCompany != null) {
                        visitCompany.setText("");
                    }
                    if (visitName != null) {
                        visitName.setText("");
                    }
                    if (visitMobile != null) {
                        visitMobile.setText("");
                    }
                    if (reason != null) {
                        reason.setText("");
                    }
                    if (hcode != null) {
                        hcode.setText("");
                    }
                    if (badage != null) {
                        badage.setText("");
                    }
                    if (remark != null) {
                        remark.setText("");
                    }
                    break;
            }
        }
    }

    public static class DataSubmitListener implements View.OnClickListener {

        Activity activity;

        Integer type;

        Handler handler;

        public View.OnClickListener builder(Activity activity, Integer type) {
            this.activity = activity;
            this.type = type;
            this.handler = new PageHandler().buildContext(activity);
            return this;
        }

        private boolean checkEmpty(EditText str) {
            return str != null && str.getText().toString().trim().length() > 0;
        }

        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(LOADING_SHOW);
                    EditText name = activity.findViewById(R.id.name);
                    EditText department = activity.findViewById(R.id.department);
                    EditText email = activity.findViewById(R.id.email);
                    EditText visitCompany = activity.findViewById(R.id.visitCompany);
                    EditText visitName = activity.findViewById(R.id.visitName);
                    EditText visitMobile = activity.findViewById(R.id.visitMobile);
                    EditText reason = activity.findViewById(R.id.reason);
                    EditText hcode = activity.findViewById(R.id.hCode);
                    EditText badage = activity.findViewById(R.id.badageNo);
                    EditText pcode = activity.findViewById(R.id.pinCode);
                    EditText remark = activity.findViewById(R.id.remark);
                    JSONObject jsonObject = new JSONObject();
                    try {
                        if (checkEmpty(name)) {
                            jsonObject.put("name", name.getText());
                        }
                        if (checkEmpty(department)) {
                            jsonObject.put("department", department.getText());
                        } else {
                            if (type == 0) {
                                Message message = new Message();
                                message.what = SHOW_MESSAGE_LONG;
                                message.obj = "请填写正确的员工信息";
                                handler.sendMessage(message);
                                return;
                            }
                        }
                        if (checkEmpty(email)) {
                            jsonObject.put("email", email.getText());
                        }
                        if (checkEmpty(visitCompany)) {
                            jsonObject.put("visitCompany", visitCompany.getText());
                        }
                        if (checkEmpty(visitName)) {
                            jsonObject.put("visitName", visitName.getText());
                        } else {
                            if (type != 9) {
                                Message message = new Message();
                                message.what = SHOW_MESSAGE_LONG;
                                message.obj = "请填写完整内容";
                                handler.sendMessage(message);
                                return;
                            }
                        }
                        if (checkEmpty(visitMobile)) {
                            jsonObject.put("visitMobile", visitMobile.getText());
                        }
                        if (checkEmpty(reason)) {
                            jsonObject.put("reason", reason.getText());
                        }
                        if (checkEmpty(hcode)) {
                            jsonObject.put("hCode", hcode.getText());
                        }
                        if (checkEmpty(badage)) {
                            jsonObject.put("badageNo", badage.getText());
                        }
                        if (checkEmpty(pcode)) {
                            jsonObject.put("pinCode", pcode.getText());
                        }
                        if (checkEmpty(remark)) {
                            jsonObject.put("remark", remark.getText());
                        }
                        jsonObject.put("visitType", type);
                        jsonObject.put("temporary", 1);
                        JSONObject rep = DataHandler.executePost(jsonObject, type == 9 ?
                                DataHandler.SERVER_HOST + "/fengqi/reserve/checkOut" :
                                DataHandler.SERVER_HOST + "/fengqi/reserve/save");
                        handler.sendEmptyMessage(LOADING_HIDE);
                        if (rep != null && rep.getBoolean("success")) {
                            if (type == 0) {
                                Object mo = rep.get("data");
                                UsbPrintUtil usbPrintUtil = new UsbPrintUtil(activity);
                                Map<String, Object> m = new HashMap<>();
                                if (mo instanceof Map) {
                                    m = (Map<String, Object>) mo;
                                } else if (mo instanceof JSONObject) {
                                    JSONObject mmo = (JSONObject) mo;
                                    m.put("visitName", mmo.get("visitName"));
                                    m.put("visitCompany", mmo.get("visitCompany"));
                                    m.put("name", mmo.get("name"));
                                    m.put("department", jsonObject.get("department"));
                                    m.put("pinCode", mmo.get("pinCode"));
                                    m.put("visitTime", sdf.format(new Date()));
                                }
                                if (m.size() == 0) {
                                    m.put("visitName", jsonObject.get("visitName"));
                                    m.put("visitCompany", jsonObject.get("visitCompany"));
                                    m.put("name", jsonObject.get("name"));
                                    m.put("department", jsonObject.get("department"));
                                    m.put("pinCode", "000000");
                                    m.put("visitTime", sdf.format(new Date()));
                                }
                                try {
                                    usbPrintUtil.print(m);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            activity.finish();
                        } else {
                            Message message = new Message();
                            message.what = SHOW_MESSAGE_LONG;
                            message.obj = "请求异常";
                            if (rep != null && rep.get("message") != null) {
                                message.obj = rep.get("message").toString();
                            }
                            handler.sendMessage(message);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Message message = new Message();
                        message.what = SHOW_MESSAGE_LONG;
                        message.obj = e.getMessage();
                        handler.sendMessage(message);
                    }
                }
            }).start();
            System.out.println(this.activity);
            System.out.println("===============");
        }
    }

    public static class DataRestListener implements View.OnClickListener {

        Activity activity;

        Handler handler;

        public View.OnClickListener builder(Activity activity) {
            this.activity = activity;
            this.handler = new PageHandler().buildContext(activity);
            return this;
        }

        @Override
        public void onClick(View v) {
            this.handler.sendEmptyMessage(FORM_ClEAR);
        }
    }

}
