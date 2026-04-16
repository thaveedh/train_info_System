package com.trainchatbot.service;

import com.trainchatbot.model.TrainStatusResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.trainchatbot.util.JsonParserUtil;
import com.trainchatbot.util.DebugLogUtil;
import com.trainchatbot.util.TrainDisplayNameResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TrainService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final JsonParserUtil jsonParserUtil;
    private final DebugLogUtil debugLogUtil;
    private final Map<String, CacheEntry> liveCache = new ConcurrentHashMap<>();
    private final Map<String, Object> trainLocks = new ConcurrentHashMap<>();
    private final Map<String, SimTrainState> simulatedTrains = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 90_000;
    private static final int DATASET_SIZE = 500;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${apihub.base.url:https://api.example.com/train}")
    private String apiHubBaseUrl;

    @Value("${apihub.api.key:}")
    private String apiHubApiKey;

    @Value("${apihub.host:}")
    private String apiHubHost;

    public TrainService(JsonParserUtil jsonParserUtil, DebugLogUtil debugLogUtil) {
        this.jsonParserUtil = jsonParserUtil;
        this.debugLogUtil = debugLogUtil;
        initSimulatedDataset();
    }

    /**
     * API Hub integration + fallback simulation.
     */
    public TrainStatusResponse fetchTrainData(String trainNumber) {
        String runId = "run-" + System.currentTimeMillis();
        CacheEntry fastCache = liveCache.get(trainNumber);
        if (fastCache != null && !isExpired(fastCache.timestampMs)) {
            // #region agent log
            debugLogUtil.log(runId, "H9", "TrainService.java:43", "Cache hit before lock", Map.of(
                    "trainNumber", trainNumber
            ));
            // #endregion
            return copyResponse(fastCache.response, false);
        }

        Object lock = trainLocks.computeIfAbsent(trainNumber, key -> new Object());
        synchronized (lock) {
            CacheEntry lockedCache = liveCache.get(trainNumber);
            if (lockedCache != null && !isExpired(lockedCache.timestampMs)) {
                // #region agent log
                debugLogUtil.log(runId, "H9", "TrainService.java:55", "Cache hit inside lock", Map.of(
                        "trainNumber", trainNumber
                ));
                // #endregion
                return copyResponse(lockedCache.response, false);
            }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiHubApiKey);
            if (apiHubHost != null && !apiHubHost.isBlank()) {
                headers.set("x-rapidapi-host", apiHubHost);
            }
            headers.set("x-rapidapi-ua", "RapidAPI-Playground");
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            String url = apiHubBaseUrl + "/" + trainNumber;
            // #region agent log
            debugLogUtil.log(runId, "H6", "TrainService.java:52", "Calling API Hub", Map.of(
                    "trainNumber", trainNumber,
                    "url", url
            ));
            // #endregion
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );
            // #region agent log
            debugLogUtil.log(runId, "H7", "TrainService.java:60", "API Hub responded", Map.of(
                    "statusCode", response.getStatusCode().value(),
                    "bodyPresent", response.getBody() != null
            ));
            // #endregion
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().isBlank()) {
                return simulatedData(trainNumber, runId);
            }
            TrainStatusResponse parsed = parseApiHubSearchResponse(response.getBody(), trainNumber);
            if (parsed.getTrainNo() == null || parsed.getTrainNo().isBlank()) {
                return simulatedData(trainNumber, runId);
            }
            if (parsed.isFallback()) {
                return parsed;
            }
            parsed.setFallback(false);
            TrainStatusResponse snapshot = copyResponse(parsed, false);
            liveCache.put(trainNumber, new CacheEntry(snapshot, System.currentTimeMillis()));
            return snapshot;
        } catch (Exception ex) {
            CacheEntry stale = liveCache.get(trainNumber);
            if (stale != null) {
                // #region agent log
                debugLogUtil.log(runId, "H10", "TrainService.java:86", "API failed, serving stale cache", Map.of(
                        "trainNumber", trainNumber
                ));
                // #endregion
                return copyResponse(stale.response, false);
            }
            // #region agent log
            debugLogUtil.log(runId, "H8", "TrainService.java:75", "API Hub exception fallback", Map.of(
                    "exceptionType", ex.getClass().getSimpleName(),
                    "message", ex.getMessage() == null ? "" : ex.getMessage()
            ));
            // #endregion
            return simulatedData(trainNumber, runId);
        }
        }
    }

    private TrainStatusResponse parseApiHubSearchResponse(String json, String requestedTrainNumber) {
        JsonNode root = jsonParserUtil.fromJson(json, JsonNode.class);
        JsonNode body = root.path("body");
        if (!body.isArray() || body.isEmpty()) {
            return simulatedData(requestedTrainNumber, "run-" + System.currentTimeMillis());
        }
        JsonNode trains = body.get(0).path("trains");
        if (!trains.isArray() || trains.isEmpty()) {
            return simulatedData(requestedTrainNumber, "run-" + System.currentTimeMillis());
        }
        JsonNode train = trains.get(0);
        JsonNode schedule = train.path("schedule");

        String from = safeText(train, "origin");
        String to = safeText(train, "destination");
        String departure = "--";
        String nextStation = "--";
        String eta = "--";
        String currentStatus = "Route data fetched from API Hub.";
        String currentLocation = "Schedule available";

        if (schedule.isArray() && !schedule.isEmpty()) {
            JsonNode first = schedule.get(0);
            departure = safeText(first, "departureTime");
            nextStation = schedule.size() > 1 ? safeText(schedule.get(1), "stationName") : safeText(first, "stationName");
            eta = schedule.size() > 1 ? safeText(schedule.get(1), "arrivalTime") : "--";
            String generatedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            currentStatus = "Train schedule fetched at " + generatedAt;
            currentLocation = "Live position unavailable from this endpoint";
        }

        TrainStatusResponse out = new TrainStatusResponse();
        String no = safeText(train, "trainNumber");
        out.setTrainNo(no);
        out.setTrainName(TrainDisplayNameResolver.resolveOrNeutral(no, safeText(train, "trainName")));
        out.setFrom(from);
        out.setTo(to);
        out.setDepartureTime(departure);
        out.setCurrentStatus(currentStatus);
        out.setCurrentLocation(currentLocation);
        out.setNextStation(nextStation);
        out.setEta(eta);
        out.setDelay("N/A");
        out.setStatusType("SCHEDULED");
        return out;
    }

    private String safeText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("");
    }

    private boolean isExpired(long timestampMs) {
        return (System.currentTimeMillis() - timestampMs) > CACHE_TTL_MS;
    }

    /** Ensures display names are never generic placeholders, including for stale cache entries. */
    private void resolveTrainDisplayName(TrainStatusResponse r) {
        if (r == null) {
            return;
        }
        String no = r.getTrainNo() == null ? "" : r.getTrainNo().trim();
        r.setTrainName(TrainDisplayNameResolver.resolveOrNeutral(no, r.getTrainName()));
    }

    private TrainStatusResponse copyResponse(TrainStatusResponse src, boolean fallback) {
        TrainStatusResponse out = new TrainStatusResponse();
        out.setTrainNo(src.getTrainNo());
        out.setTrainName(src.getTrainName());
        out.setFrom(src.getFrom());
        out.setTo(src.getTo());
        out.setDepartureTime(src.getDepartureTime());
        out.setCurrentStatus(src.getCurrentStatus());
        out.setCurrentLocation(src.getCurrentLocation());
        out.setNextStation(src.getNextStation());
        out.setEta(src.getEta());
        out.setDelay(src.getDelay());
        out.setStatusType(src.getStatusType());
        out.setFallback(fallback);
        resolveTrainDisplayName(out);
        return out;
    }

    private static class CacheEntry {
        private final TrainStatusResponse response;
        private final long timestampMs;

        private CacheEntry(TrainStatusResponse response, long timestampMs) {
            this.response = response;
            this.timestampMs = timestampMs;
        }
    }

    private TrainStatusResponse simulatedData(String trainNumber, String runId) {
        SimTrainState state = simulatedTrains.computeIfAbsent(trainNumber, this::createStateFromTrainNo);
        int queryStep = state.queryCount.incrementAndGet();
        long elapsedMinutes = Math.max(0, Duration.between(state.anchorTime, LocalDateTime.now()).toMinutes()) + (queryStep - 1);
        int progressMinutes = (int) Math.min(state.totalJourneyMinutes, elapsedMinutes);
        int dynamicDelay = state.baseDelayMinutes + (int) (elapsedMinutes / 20) + (queryStep / 2);
        int percent = state.totalJourneyMinutes == 0 ? 0 : (progressMinutes * 100) / state.totalJourneyMinutes;

        int segmentIndex = Math.min(state.stations.size() - 2, Math.max(0, percent / 20));
        String currentStation = state.stations.get(segmentIndex);
        String nextStation = state.stations.get(Math.min(state.stations.size() - 1, segmentIndex + 1));

        int remainingMinutes = Math.max(0, state.totalJourneyMinutes - progressMinutes);
        LocalDateTime eta = LocalDateTime.now().plusMinutes(remainingMinutes + Math.max(1, dynamicDelay / 3));
        String statusType = dynamicDelay <= 5 ? "ON_TIME" : (dynamicDelay <= 20 ? "DELAYED" : "LATE");
        String currentStatus = progressMinutes >= state.totalJourneyMinutes
                ? "Reached destination"
                : "Running between " + currentStation + " and " + nextStation;
        String currentLocation = progressMinutes >= state.totalJourneyMinutes
                ? "100% completed"
                : percent + "% completed";

        // #region agent log
        debugLogUtil.log(runId, "H11", "TrainService.java:222", "Serving simulated running train", Map.of(
                "trainNumber", trainNumber,
                "datasetSize", DATASET_SIZE,
                "percent", percent,
                "dynamicDelay", dynamicDelay,
                "queryStep", queryStep
        ));
        // #endregion

        TrainStatusResponse out = new TrainStatusResponse();
        out.setTrainNo(state.trainNo);
        out.setTrainName(state.trainName);
        out.setFrom(state.from);
        out.setTo(state.to);
        out.setDepartureTime(state.departure.format(TIME_FMT));
        out.setCurrentStatus(currentStatus);
        out.setCurrentLocation(currentLocation);
        out.setNextStation(nextStation);
        out.setEta(eta.toLocalTime().format(TIME_FMT));
        out.setDelay(dynamicDelay + " mins");
        out.setStatusType(statusType);
        out.setFallback(true);
        resolveTrainDisplayName(out);
        return out;
    }

    private void initSimulatedDataset() {
        for (int i = 0; i < DATASET_SIZE; i++) {
            String trainNo = String.valueOf(12001 + i);
            simulatedTrains.put(trainNo, createStateFromTrainNo(trainNo));
        }
    }

    private SimTrainState createStateFromTrainNo(String trainNo) {
        List<String> hubs = Arrays.asList(
                "Chennai", "Bangalore", "Mysore", "Coimbatore", "Madurai",
                "Hyderabad", "Vijayawada", "Nagpur", "Bhopal", "Delhi",
                "Kolkata", "Patna", "Lucknow", "Pune", "Mumbai"
        );
        int seed = Math.abs(trainNo.hashCode());
        String from = hubs.get(seed % hubs.size());
        String to = hubs.get((seed / 7 + 3) % hubs.size());
        if (from.equals(to)) {
            to = hubs.get((seed / 11 + 5) % hubs.size());
        }

        List<String> route = new ArrayList<>();
        route.add(from);
        route.add(hubs.get((seed / 13 + 1) % hubs.size()));
        route.add(hubs.get((seed / 17 + 4) % hubs.size()));
        route.add(to);

        int totalMinutes = 360 + (seed % 720); // 6h to 18h
        int baseDelay = seed % 26; // original fake delay 0-25 mins
        LocalDateTime anchor = LocalDateTime.of(LocalDate.now(), LocalTime.of(5, 0))
                .plusMinutes(seed % 300)
                .minusMinutes(seed % 240);

        return new SimTrainState(
                trainNo,
                TrainDisplayNameResolver.resolveOrNeutral(trainNo, null),
                from,
                to,
                anchor.toLocalTime(),
                anchor,
                totalMinutes,
                baseDelay,
                route
        );
    }

    private static class SimTrainState {
        private final String trainNo;
        private final String trainName;
        private final String from;
        private final String to;
        private final LocalTime departure;
        private final LocalDateTime anchorTime;
        private final int totalJourneyMinutes;
        private final int baseDelayMinutes;
        private final List<String> stations;
        private final AtomicInteger queryCount = new AtomicInteger(0);

        private SimTrainState(String trainNo,
                              String trainName,
                              String from,
                              String to,
                              LocalTime departure,
                              LocalDateTime anchorTime,
                              int totalJourneyMinutes,
                              int baseDelayMinutes,
                              List<String> stations) {
            this.trainNo = trainNo;
            this.trainName = trainName;
            this.from = from;
            this.to = to;
            this.departure = departure;
            this.anchorTime = anchorTime;
            this.totalJourneyMinutes = totalJourneyMinutes;
            this.baseDelayMinutes = baseDelayMinutes;
            this.stations = stations;
        }
    }
}
