package com.trainchatbot.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TrainApiService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
    private final Map<String, Integer> delayCache = new ConcurrentHashMap<>();

    private static class Station {
        final String name;
        final LocalTime scheduledTime;

        Station(String name, String time) {
            this.name = name;
            this.scheduledTime = LocalTime.parse(time, FMT);
        }
    }

    private static class TrainInfo {
        final String trainNo;
        final String trainName;
        final List<Station> stations;

        TrainInfo(String trainNo, String trainName, List<Station> stations) {
            this.trainNo = trainNo;
            this.trainName = trainName;
            this.stations = stations;
        }
    }

    private static final Map<String, TrainInfo> TRAIN_DATASET = new HashMap<>();

    static {
        TRAIN_DATASET.put("12627", new TrainInfo("12627", "Karnataka Express", Arrays.asList(
                new Station("New Delhi", "06:00"),
                new Station("Mathura Junction", "07:55"),
                new Station("Agra Cantt", "08:55"),
                new Station("Gwalior", "10:30"),
                new Station("Jhansi", "12:00"),
                new Station("Bhopal Junction", "14:30"),
                new Station("Nagpur", "20:00"),
                new Station("Secunderabad", "07:30"),
                new Station("Bengaluru City", "11:00")
        )));

        TRAIN_DATASET.put("12301", new TrainInfo("12301", "Howrah Rajdhani", Arrays.asList(
                new Station("New Delhi", "16:55"),
                new Station("Kanpur Central", "21:45"),
                new Station("Allahabad Junction", "23:59"),
                new Station("Mughal Sarai", "01:05"),
                new Station("Dhanbad", "05:40"),
                new Station("Asansol", "07:05"),
                new Station("Howrah Junction", "09:55")
        )));

        TRAIN_DATASET.put("12951", new TrainInfo("12951", "Mumbai Rajdhani", Arrays.asList(
                new Station("New Delhi", "16:30"),
                new Station("Kota Junction", "22:05"),
                new Station("Vadodara Junction", "04:30"),
                new Station("Surat", "06:25"),
                new Station("Mumbai Central", "08:35")
        )));

        TRAIN_DATASET.put("12009", new TrainInfo("12009", "Shatabdi Express", Arrays.asList(
                new Station("Mumbai Central", "06:00"),
                new Station("Borivali", "06:25"),
                new Station("Vapi", "07:45"),
                new Station("Surat", "08:35"),
                new Station("Vadodara Junction", "09:45"),
                new Station("Ahmedabad Junction", "11:05")
        )));

        TRAIN_DATASET.put("11301", new TrainInfo("11301", "Udyan Express", Arrays.asList(
                new Station("Lokmanya Tilak T", "08:05"),
                new Station("Pune Junction", "11:50"),
                new Station("Solapur", "15:30"),
                new Station("Wadi", "18:35"),
                new Station("Gulbarga", "19:50"),
                new Station("Bengaluru City", "01:15")
        )));
    }

    public String getLiveStatus(String trainNumber) {
        TrainInfo train = TRAIN_DATASET.get(trainNumber.trim());

        if (train == null) {
            System.out.println("[TrainApiService] Train " + trainNumber + " not in dataset. Triggering fallback.");
            return null;
        }

        int delay = delayCache.computeIfAbsent(trainNumber, key -> new Random().nextInt(31));
        LocalTime now = LocalTime.now();

        List<Station> stops = train.stations;
        Station source = stops.get(0);
        Station destination = stops.get(stops.size() - 1);
        LocalTime effectiveDeparture = source.scheduledTime.plusMinutes(delay);
        String statusType = delay == 0 ? "ON_TIME" : (delay <= 10 ? "DELAYED" : "LATE");

        if (now.isBefore(effectiveDeparture)) {
            return buildResponse(
                    train,
                    source,
                    destination,
                    delay,
                    "Not started yet - Train departs at " + effectiveDeparture.format(FMT),
                    source.name,
                    stops.get(1).name,
                    stops.get(1).scheduledTime.plusMinutes(delay),
                    "ON_TIME"
            );
        }

        LocalTime lastEffective = destination.scheduledTime.plusMinutes(delay);
        if (!now.isBefore(lastEffective)) {
            return buildResponse(
                    train,
                    source,
                    destination,
                    delay,
                    "Reached destination - " + destination.name,
                    destination.name,
                    "None",
                    null,
                    statusType
            );
        }

        for (int i = 0; i < stops.size() - 1; i++) {
            Station current = stops.get(i);
            Station next = stops.get(i + 1);

            LocalTime effectiveCurrent = current.scheduledTime.plusMinutes(delay);
            LocalTime effectiveNext = next.scheduledTime.plusMinutes(delay);

            if (!now.isBefore(effectiveCurrent) && now.isBefore(effectiveCurrent.plusMinutes(5))) {
                return buildResponse(
                        train,
                        source,
                        destination,
                        delay,
                        "Arrived at " + current.name,
                        current.name,
                        next.name,
                        effectiveNext,
                        statusType
                );
            }

            if (!now.isBefore(effectiveCurrent) && now.isBefore(effectiveNext)) {
                long segmentTotal = java.time.Duration.between(current.scheduledTime, next.scheduledTime).toMinutes();
                long segmentElapsed = java.time.Duration.between(effectiveCurrent, now).toMinutes();
                int pct = segmentTotal > 0 ? (int) ((segmentElapsed * 100) / segmentTotal) : 0;

                String locationLabel = pct >= 75
                        ? "Near " + next.name
                        : pct + "% between " + current.name + " and " + next.name;

                return buildResponse(
                        train,
                        source,
                        destination,
                        delay,
                        "Running between " + current.name + " and " + next.name,
                        locationLabel,
                        next.name,
                        effectiveNext,
                        statusType
                );
            }
        }

        return buildResponse(train, source, destination, delay, "Status unknown", "Unknown", "Unknown", null, statusType);
    }

    private String buildResponse(TrainInfo train,
                                 Station source,
                                 Station destination,
                                 int delayMins,
                                 String currentStatus,
                                 String currentLocation,
                                 String nextStation,
                                 LocalTime eta,
                                 String statusType) {

        String etaStr = eta != null ? eta.format(FMT) : "--";

        return String.format(
                "{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"," +
                        "\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"," +
                        "\"%s\":\"%s\",\"%s\":\"%s mins\",\"%s\":\"%s\"}",
                "trainNo", train.trainNo,
                "trainName", train.trainName,
                "from", source.name,
                "to", destination.name,
                "departureTime", source.scheduledTime.format(FMT),
                "currentStatus", currentStatus,
                "currentLocation", currentLocation,
                "nextStation", nextStation,
                "eta", etaStr,
                "delay", delayMins,
                "statusType", statusType
        );
    }
}
