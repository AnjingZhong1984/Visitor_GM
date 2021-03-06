package fafa.com.visitor.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Zhou Bing
 * <p>
 * 2017/10/15.
 */
public class JSNO2Result {

    public static Result cast(String result) {
        if (result != null && !"".equals(result.trim())) {
            JSONObject rs = null;
            try {
                rs = new JSONObject(result);
                Boolean success = rs.getBoolean("success");
                //获得返回状态
                Map<String, Object> resultValues = new HashMap<>();

                JSONObject values = null;
                try {
                    //正常   构造响应的信息
                    if (rs.getString("message") != null) {
                        resultValues.put("message", rs.getString("message"));
                    }
                    values = rs.getJSONObject("data");
                    Iterator itor = values.keys();
                    while (itor.hasNext()) {
                        String key = (String) itor.next();
                        String value = values.getString(key);
                        resultValues.put(key, value);
                    }
                } catch (Exception e) {
                    //异常
                    if (rs.getString("message") != null) {
                        resultValues.put("message", rs.getString("message"));
                    } else {
                        resultValues.put("message", "解析json异常");
                    }
                }
                return new Result(success, resultValues);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
