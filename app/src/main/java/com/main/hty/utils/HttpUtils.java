package com.main.hty.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class HttpUtils {

    final static String HTTP_URL = "http://mjk.pc28.ai/api/bettingsys/";
    public  static String requestByGet(String type,long time,Map<String,String> map) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(HTTP_URL)
                    .append(type)
                    .append("api_key=tz")
                    .append("&")
                    .append("time")
                            .append("=")
                    .append(time)
                    .append("&sign=")
                    .append(sha256(time+""));
            if(map != null) {
                for (Map.Entry<String, String> result : map.entrySet()) {
                    stringBuilder
                            .append("&")
                            .append(result.getKey())
                            .append("=")
                            .append(result.getValue());
                }
            }
            Log.i("","onAccessibilityEvent---:httpUrl:"+stringBuilder.toString());
            URL url = new URL(stringBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.connect();
            if (conn.getResponseCode() == 200) { // 如果状态码==200,说明请求成功
                StringBuilder sb = new StringBuilder();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static  String sha256(String time) {

        String apiSecret = "TzCvcH1BjXPz"; // 密钥
        // 创建SecretKeySpec对象
        SecretKeySpec secretKeySpec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] hmac = mac.doFinal(time.getBytes(StandardCharsets.UTF_8));

        // 将HMAC值转换为十六进制字符串
        StringBuilder sb = new StringBuilder();
        for (byte b : hmac) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static List<String> getMsg(String result){
        List<String> msgS = new ArrayList<>();
        if (TextUtils.isEmpty(result)) {
            return null;
        }
        try {
            JSONObject resultJson = new JSONObject(result);
            JSONArray jsonArray = resultJson.getJSONArray("data");
            for (int i = 0, length = jsonArray.length(); i < length; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String msg = jsonObject.getString("msg");
                msgS.add(msg);
            }
            return msgS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
