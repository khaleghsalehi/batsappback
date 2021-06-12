package net.khalegh.batsapp.inspection;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class FilterImage {
    static final Logger log = LoggerFactory.getLogger(FilterImage.class);
    List<String> NSFW_REST_URL = new ArrayList<>();

    public FilterImage() {
        //todo load from config file
        // nsfw server farms
        NSFW_REST_URL.add("http://localhost:8989/nsfw");
        NSFW_REST_URL.add("http://localhost:8989/nsfw");

    }

    public JsonObject checkMe(String fileName) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", fileName,
                        RequestBody.create(MediaType.parse("image/jpeg"),
                                new File(fileName)))
                .build();
        int index = (int) (Math.random() * NSFW_REST_URL.size());
        String url = NSFW_REST_URL.get(index);
        log.info("image nsfw candidate url " + url);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String json = Objects.requireNonNull(response.body()).string();
        JsonObject[] objectArray = gson.fromJson(json, JsonObject[].class);
        log.info("image filter result " + objectArray[0].getMClassName() + " " + objectArray[0].getMProbability());
        return objectArray[0];
    }


}
