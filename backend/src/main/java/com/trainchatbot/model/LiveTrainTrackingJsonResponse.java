package com.trainchatbot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Strict JSON for advanced live tracking: full route + position + delays.
 */
@JsonPropertyOrder({
        "train_number", "train_name", "current_status", "current_station", "next_station",
        "progress_percent", "distance_remaining_km", "expected_arrival_next", "overall_delay_minutes",
        "stations"
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiveTrainTrackingJsonResponse {

    @JsonProperty("train_number")
    private String trainNumber;

    @JsonProperty("train_name")
    private String trainName;

    @JsonProperty("current_status")
    private String currentStatus;

    @JsonProperty("current_station")
    private String currentStation;

    @JsonProperty("next_station")
    private String nextStation;

    @JsonProperty("progress_percent")
    private int progressPercent;

    @JsonProperty("distance_remaining_km")
    private double distanceRemainingKm;

    @JsonProperty("expected_arrival_next")
    private String expectedArrivalNext;

    @JsonProperty("overall_delay_minutes")
    private int overallDelayMinutes;

    private List<StationScheduleJson> stations;

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getCurrentStation() {
        return currentStation;
    }

    public void setCurrentStation(String currentStation) {
        this.currentStation = currentStation;
    }

    public String getNextStation() {
        return nextStation;
    }

    public void setNextStation(String nextStation) {
        this.nextStation = nextStation;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public double getDistanceRemainingKm() {
        return distanceRemainingKm;
    }

    public void setDistanceRemainingKm(double distanceRemainingKm) {
        this.distanceRemainingKm = distanceRemainingKm;
    }

    public String getExpectedArrivalNext() {
        return expectedArrivalNext;
    }

    public void setExpectedArrivalNext(String expectedArrivalNext) {
        this.expectedArrivalNext = expectedArrivalNext;
    }

    public int getOverallDelayMinutes() {
        return overallDelayMinutes;
    }

    public void setOverallDelayMinutes(int overallDelayMinutes) {
        this.overallDelayMinutes = overallDelayMinutes;
    }

    public List<StationScheduleJson> getStations() {
        return stations;
    }

    public void setStations(List<StationScheduleJson> stations) {
        this.stations = stations;
    }
}
