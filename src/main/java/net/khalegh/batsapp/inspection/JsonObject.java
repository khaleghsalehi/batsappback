
package net.khalegh.batsapp.inspection;

import javax.annotation.Generated;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;


@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class JsonObject {

    @SerializedName("className")
    @Getter
    @Setter
    private String mClassName;

    @SerializedName("probability")
    @Getter
    @Setter
    private Double mProbability;

    public JsonObject(String mClassName, Double mProbability) {
        this.mClassName = mClassName;
        this.mProbability = mProbability;
    }
}
