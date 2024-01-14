/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.yandexstation.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link YandexStationResponse} is describing api response with station state entity
 *
 * @author "Dmintry P (d51x)" - Initial contribution
 */
public class YandexStationResponse {
    @SerializedName("id")
    private String responseId;
    private String requestId;
    private String softwareVersion;
    private Long requestSentTime;
    private Long processingTime;
    private String status;
    private YandexStationState state;

    private YandexStationResponseExtra extra;

    @SerializedName("supported_features")
    private List<String> supportedFeatures = new ArrayList<>();

    @SerializedName("unsupported_features")
    private List<String> unSupportedFeatures = new ArrayList<>();

    /**
     * Gets response id.
     *
     * @return the response id
     */
    public String getResponseId() {
        return responseId;
    }

    /**
     * Sets response id.
     *
     * @param responseId the response id
     */
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets software version.
     *
     * @return the software version
     */
    public String getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Sets software version.
     *
     * @param softwareVersion the software version
     */
    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    /**
     * Gets request sent time.
     *
     * @return the request sent time
     */
    public Long getRequestSentTime() {
        return requestSentTime;
    }

    /**
     * Sets request sent time.
     *
     * @param requestSentTime the request sent time
     */
    public void setRequestSentTime(Long requestSentTime) {
        this.requestSentTime = requestSentTime;
    }

    /**
     * Gets processing time.
     *
     * @return the processing time
     */
    public Long getProcessingTime() {
        return processingTime;
    }

    /**
     * Sets processing time.
     *
     * @param processingTime the processing time
     */
    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets state.
     *
     * @return the state
     */
    public YandexStationState getState() {
        return state;
    }

    /**
     * Sets state.
     *
     * @param state the state
     */
    public void setState(YandexStationState state) {
        this.state = state;
    }

    /**
     * Gets extra.
     *
     * @return the extra
     */
    public YandexStationResponseExtra getExtra() {
        return extra;
    }

    /**
     * Sets extra.
     *
     * @param extra the extra
     */
    public void setExtra(YandexStationResponseExtra extra) {
        this.extra = extra;
    }

    /**
     * Gets supported features.
     *
     * @return the supported features
     */
    public List<String> getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * Sets supported features.
     *
     * @param supportedFeatures the supported features
     */
    public void setSupportedFeatures(List<String> supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    /**
     * Gets un supported features.
     *
     * @return the un supported features
     */
    public List<String> getUnSupportedFeatures() {
        return unSupportedFeatures;
    }

    /**
     * Sets un supported features.
     *
     * @param unSupportedFeatures the un supported features
     */
    public void setUnSupportedFeatures(List<String> unSupportedFeatures) {
        this.unSupportedFeatures = unSupportedFeatures;
    }

    /**
     * The type Yandex station response extra.
     */
    public class YandexStationResponseExtra {
        /**
         * The App state.
         */
        public String appState;
        /**
         * The Environment state.
         */
        public String environmentState;
        /**
         * The Watched video state.
         */
        public String watchedVideoState;
        /**
         * The Software version.
         */
        public String softwareVersion;
    }
}
