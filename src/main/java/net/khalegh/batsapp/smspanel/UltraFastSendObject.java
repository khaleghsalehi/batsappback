package net.khalegh.batsapp.smspanel;

import lombok.Getter;
import lombok.Setter;

public class UltraFastSendObject {

    @Getter
    @Setter
    String VerificationCodeId;

    @Getter
    @Setter
    boolean IsSuccessful;

    @Getter
    @Setter
    String Message;
}
