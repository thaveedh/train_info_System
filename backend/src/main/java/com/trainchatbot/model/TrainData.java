package com.trainchatbot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * TrainData
 * 
 * This file defines the structure of train data stored in MongoDB.
 * It acts as a fallback dataset for Indian Railways when the real API fails.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "train_details")
public class TrainData {
    @Id
    private String id;
    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private String scheduleTime; // Scheduled departure/arrival
    private String runningDays;  // Days of operation

    // Explicit Getters and Setters for compatibility
    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(String scheduleTime) { this.scheduleTime = scheduleTime; }

    public String getRunningDays() { return runningDays; }
    public void setRunningDays(String runningDays) { this.runningDays = runningDays; }
}
