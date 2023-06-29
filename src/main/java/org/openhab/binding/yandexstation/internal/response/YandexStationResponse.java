package org.openhab.binding.yandexstation.internal.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class YandexStationResponse {
    @SerializedName("id")
    private String responseId;
    private String requestId;
    private String softwareVersion;
    private Long requestSentTime;
    private Long processingTime;
    private String status; // SUCCESS - возможно, это ответ на команду
    private YandexStationState state;

    private YandexStationResponseExtra extra;

    @SerializedName("supported_features")
    private List<String> supportedFeatures = new ArrayList<>();

    @SerializedName("unsupported_features")
    private List<String> unSupportedFeatures = new ArrayList<>();

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public Long getRequestSentTime() {
        return requestSentTime;
    }

    public void setRequestSentTime(Long requestSentTime) {
        this.requestSentTime = requestSentTime;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public YandexStationState getState() {
        return state;
    }

    public void setState(YandexStationState state) {
        this.state = state;
    }

    public YandexStationResponseExtra getExtra() {
        return extra;
    }

    public void setExtra(YandexStationResponseExtra extra) {
        this.extra = extra;
    }

    public List<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    public void setSupportedFeatures(List<String> supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    public List<String> getUnSupportedFeatures() {
        return unSupportedFeatures;
    }

    public void setUnSupportedFeatures(List<String> unSupportedFeatures) {
        this.unSupportedFeatures = unSupportedFeatures;
    }
}
