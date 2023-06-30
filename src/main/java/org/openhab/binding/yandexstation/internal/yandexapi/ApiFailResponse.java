package org.openhab.binding.yandexstation.internal.yandexapi;

import com.google.gson.annotations.SerializedName;

public class ApiFailResponse extends ApiResponse {
    /**
     * The message text.
     */
    @SerializedName("message")
    public String message;  // AUTH_TOKEN_INVALID or
                            // Unknown device LP000000000000120345000067089123
    /**
     * The Status.
     */
    public String status; //error
}
