package net.khalegh.batsapp.smspanel;

import com.google.gson.Gson;
import net.khalegh.batsapp.config.MemoryCache;
import net.khalegh.batsapp.inspection.FilterImage;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SmsDotIR {

    static final Logger log = LoggerFactory.getLogger(SmsDotIR.class);

    private static String SecretKey = "TomCat2013@new@";
    private static String UserApiKey = "bb185c01b90e18e3fbac6880";
    private static String TOKEN_URL = "https://RestfulSms.com/api/Token";


    private static String UltraFastSend = "https://RestfulSms.com/api/UltraFastSend";
    private static String templateId = "49933";

    /**
     * @return TokenKey
     * @throws IOException
     */
    public static String getTokenKey() throws IOException, ExecutionException {
        if (!MemoryCache.SMS_DOT_IR_TokenKey.get("tokenKey").isEmpty()) {
            log.info("fetch tokenKey from cache!");
            return MemoryCache.SMS_DOT_IR_TokenKey.get("tokenKey");
        }
        OkHttpClient client = new OkHttpClient();
        String json = "{\"SecretKey\": \"" + SecretKey + "\",\"UserApiKey\":\"" + UserApiKey + "\"}";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String resp = Objects.requireNonNull(response.body()).string();
        System.out.println(resp);
        Gson gson = new Gson();
        TokenKeyObject object = gson.fromJson(resp, TokenKeyObject.class);
        if (object.IsSuccessful) {
            log.info("got token key.");
            String tokenKey = object.getTokenKey();
            MemoryCache.SMS_DOT_IR_TokenKey.put("tokenKey", tokenKey);
            log.info("put tokenKey in cache!");
            return tokenKey;
            //todo store token in cache with ttl ( 15 min)
        } else {
            log.info("got token key failed!");
            return "";
            //todo WTF, call it again
        }
    }

    /**
     * send OTP to userPhone
     *
     * @param phoneNumber
     * @param otpCode
     * @return boolean
     */
    public static boolean sendVerificationCode(String phoneNumber,
                                               String otpCode) throws IOException, ExecutionException {
        OkHttpClient client = new OkHttpClient();
        String json = "{\n" +
                " \"ParameterArray\":[\n" +
                "{ \"Parameter\": \"VerificationCode\",\"ParameterValue\":\"" + otpCode + "\"}\n" +
                "],\n" +
                "\"Mobile\":\"" + phoneNumber + "\",\n" +
                "\n" +
                "\"TemplateId\":\"" + templateId + "\"\n" +
                "}";

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .addHeader("x-sms-ir-secure-token", getTokenKey())
                .url(UltraFastSend)
                .post(body)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String resp = Objects.requireNonNull(response.body()).string();
        System.out.println(resp);
        Gson gson = new Gson();
        UltraFastSendObject object = gson.fromJson(resp, UltraFastSendObject.class);
        if (object.IsSuccessful) {
            log.info("OTP send to " + phoneNumber);
            return object.IsSuccessful;
            //todo store token in cache with ttl ( 15 min)
        } else {
            log.info("OTP cant not send " + phoneNumber);
            return false;
            //todo WTF, call it again
        }
    }

}
