package com.trainchatbot.model;

/**
 * TrainStatusResponse
 *
 * Unified train data model used internally, whether from API Hub or fallback simulation.
 */
public class TrainStatusResponse {
    private String trainNo;
    private String trainName;
    private String from;
    private String to;
    private String departureTime;
    private String currentStatus;
    private String currentLocation;
    private String nextStation;
    private String eta;
    private String delay;
    private String statusType;
    private boolean fallback;

    public String getTrainNo() { return trainNo; }
    public void setTrainNo(String trainNo) { this.trainNo = trainNo; }
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }
    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }
    public String getNextStation() { return nextStation; }
    public void setNextStation(String nextStation) { this.nextStation = nextStation; }
    public String getEta() { return eta; }
    public void setEta(String eta) { this.eta = eta; }
    public String getDelay() { return delay; }
    public void setDelay(String delay) { this.delay = delay; }
    public String getStatusType() { return statusType; }
    public void setStatusType(String statusType) { this.statusType = statusType; }
    public boolean isFallback() { return fallback; }
    public void setFallback(boolean fallback) { this.fallback = fallback; }
}
