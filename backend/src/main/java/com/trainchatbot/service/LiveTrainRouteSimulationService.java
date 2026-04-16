package com.trainchatbot.service;

import com.trainchatbot.model.LiveTrainTrackingJsonResponse;
import com.trainchatbot.model.StationScheduleJson;
import com.trainchatbot.model.TrainStatusResponse;
import com.trainchatbot.util.DebugLogUtil;
import com.trainchatbot.util.TrainDisplayNameResolver;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simulates full-route live tracking: stations, increasing delays, forward-only progress,
 * catch-up delay adjustments on each query (IST).
 */
@Service
public class LiveTrainRouteSimulationService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] POOL = {
            "Mumbai Central", "Surat", "Vadodara Jn", "Ratlam Jn", "Kota Jn", "Mathura Jn",
            "New Delhi", "Kanpur Central", "Prayagraj Jn", "Varanasi Jn", "Patna Jn",
            "Howrah Jn", "Kharagpur Jn", "Bhubaneswar", "Visakhapatnam", "Vijayawada Jn",
            "Chennai Central", "Katpadi Jn", "Salem Jn", "Erode Jn", "Tiruppur", "Coimbatore Jn",
            "Palakkad", "Thrissur", "Ernakulam Jn", "Kottayam", "Kollam Jn", "Trivandrum Central",
            "Ksr Bengaluru", "Bengaluru Cant", "Jolarpettai", "Renigunta Jn",
            "Secunderabad Jn", "Balharshah", "Nagpur Jn", "Itarsi Jn", "Bhopal Jn", "Jhansi Jn"
    };

    private final Map<String, TrainRouteState> memory = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final DebugLogUtil debugLogUtil;

    public LiveTrainRouteSimulationService(DebugLogUtil debugLogUtil) {
        this.debugLogUtil = debugLogUtil;
    }

    /**
     * Build or advance simulation and return strict tracking JSON (localized strings).
     */
    public LiveTrainTrackingJsonResponse simulate(
            String trainNumber,
            TrainStatusResponse apiData,
            String language,
            String runId) {

        String key = trainNumber == null ? "" : trainNumber.trim();
        TrainRouteState state = memory.computeIfAbsent(key, k -> buildRouteOnly(k, apiData));

        synchronized (state) {
            advanceState(state, runId);
            return buildResponse(key, state, apiData, language, runId);
        }
    }

    private TrainRouteState buildRouteOnly(String trainNo, TrainStatusResponse apiData) {
        int seed = Math.abs(trainNo.hashCode());
        Random r = new Random(seed);

        int n = 8 + (seed % 6); // 8–13 stations
        List<String> names = new ArrayList<>();
        String from = blankOr(apiData.getFrom(), "Ksr Bengaluru");
        String to = blankOr(apiData.getTo(), "New Delhi");
        names.add(from);
        int denom = Math.max(1, POOL.length - n);
        int poolStart = seed % denom;
        for (int i = 1; i < n - 1; i++) {
            names.add(POOL[(poolStart + i * 7) % POOL.length]);
        }
        names.add(to);

        int[] arr = new int[n];
        int[] dep = new int[n];
        int[] delay = new int[n];

        int base = 3 + r.nextInt(8); // 3–10
        delay[0] = base;
        arr[0] = toMin(LocalTime.of(5, 30).plusMinutes(seed % 120));
        dep[0] = arr[0] + 10 + r.nextInt(20);

        for (int i = 1; i < n; i++) {
            delay[i] = delay[i - 1] + 1 + r.nextInt(3);
            int leg = 25 + r.nextInt(55);
            arr[i] = dep[i - 1] + leg;
            dep[i] = arr[i] + 5 + r.nextInt(12);
        }
        dep[n - 1] = arr[n - 1];

        int[] legKm = new int[n - 1];
        for (int i = 0; i < n - 1; i++) {
            legKm[i] = 40 + r.nextInt(85);
        }

        int progress = 8 + r.nextInt(25);
        TrainRouteState s = new TrainRouteState();
        s.trainKey = trainNo;
        s.stationNames = names;
        s.arrivalMin = arr;
        s.departureMin = dep;
        s.stationDelayMinutes = delay;
        s.legKm = legKm;
        s.progressPercent = Math.min(95, progress);
        int idx = indexFromProgress(progress, n);
        s.runningDelayMinutes = Math.max(3, delay[idx]);
        s.lastQueryMs = System.currentTimeMillis();
        s.queryCount = 0;
        return s;
    }

    private void advanceState(TrainRouteState state, String runId) {
        long now = System.currentTimeMillis();
        // First query: show initial route/delay only; do not step simulation yet
        if (state.queryCount == 0) {
            state.lastQueryMs = now;
            state.queryCount = 1;
            // #region agent log
            debugLogUtil.log(runId, "H20", "LiveTrainRouteSimulationService.advanceState", "First query snapshot", Map.of(
                    "progress", state.progressPercent,
                    "runningDelay", state.runningDelayMinutes
            ));
            // #endregion
            return;
        }

        state.queryCount++;
        long elapsedMin = Math.max(0, (now - state.lastQueryMs) / 60_000L);
        state.lastQueryMs = now;

        int d = state.runningDelayMinutes;
        for (long m = 0; m < elapsedMin; m++) {
            if (d > 0 && random.nextDouble() < 0.25) {
                d--;
            }
        }
        if (random.nextDouble() < 0.88) {
            d = Math.max(0, d - random.nextInt(3));
        } else {
            d = d + 1;
        }
        state.runningDelayMinutes = d;

        int delta = 1 + random.nextInt(6);
        state.progressPercent = Math.min(100, state.progressPercent + delta);

        // #region agent log
        debugLogUtil.log(runId, "H20", "LiveTrainRouteSimulationService.advanceState", "Route step", Map.of(
                "progress", state.progressPercent,
                "runningDelay", state.runningDelayMinutes,
                "queryCount", state.queryCount
        ));
        // #endregion
    }

    private LiveTrainTrackingJsonResponse buildResponse(
            String trainNumber,
            TrainRouteState state,
            TrainStatusResponse apiData,
            String language,
            String runId) {

        int n = state.stationNames.size();
        int p = Math.min(100, state.progressPercent);

        if (p >= 100) {
            String dest = state.stationNames.get(n - 1);
            LiveTrainTrackingJsonResponse out = new LiveTrainTrackingJsonResponse();
            out.setTrainNumber(blankOr(apiData.getTrainNo(), trainNumber));
            out.setTrainName(TrainDisplayNameResolver.resolveOrNeutral(trainNumber, apiData.getTrainName()));
            out.setCurrentStatus(localizeArrived(language, dest));
            out.setCurrentStation(dest);
            out.setNextStation("—");
            out.setProgressPercent(100);
            out.setDistanceRemainingKm(0);
            out.setExpectedArrivalNext(formatMin(state.arrivalMin[n - 1]));
            out.setOverallDelayMinutes(0);
            out.setStations(buildStationRows(state, n));
            return out;
        }

        float routePos = (p / 100f) * (n - 1);
        int seg = Math.min(n - 2, Math.max(0, (int) routePos));
        float frac = routePos - seg;

        String fromSt = state.stationNames.get(seg);
        String toSt = state.stationNames.get(seg + 1);
        boolean between = frac > 0.12 && frac < 0.88;

        String currentStation = between ? fromSt : (frac <= 0.12 ? fromSt : toSt);
        String nextSt = toSt;

        String statusLocalized = localizeStatus(language, between, fromSt, toSt, currentStation);

        int legKm = state.legKm[Math.min(seg, state.legKm.length - 1)];
        double distRemain = Math.round(legKm * (1.0 - frac) * 10.0) / 10.0;

        ZonedDateTime now = ZonedDateTime.now(IST);
        int minutesToNext = (int) Math.max(5, legKm * (1.0 - frac) / 2.5) + state.runningDelayMinutes;
        ZonedDateTime etaNext = now.plusMinutes(minutesToNext);

        int overall = Math.max(0, state.runningDelayMinutes);

        LiveTrainTrackingJsonResponse out = new LiveTrainTrackingJsonResponse();
        out.setTrainNumber(blankOr(apiData.getTrainNo(), trainNumber));
        out.setTrainName(TrainDisplayNameResolver.resolveOrNeutral(trainNumber, apiData.getTrainName()));
        out.setCurrentStatus(statusLocalized);
        out.setCurrentStation(currentStation);
        out.setNextStation(nextSt);
        out.setProgressPercent(p);
        out.setDistanceRemainingKm(distRemain);
        out.setExpectedArrivalNext(etaNext.toLocalTime().format(HM));
        out.setOverallDelayMinutes(overall);
        out.setStations(buildStationRows(state, n));
        return out;
    }

    private List<StationScheduleJson> buildStationRows(TrainRouteState state, int n) {
        int maxD = state.stationDelayMinutes[n - 1];
        int run = state.runningDelayMinutes;
        List<StationScheduleJson> stations = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            StationScheduleJson row = new StationScheduleJson();
            row.setStationName(state.stationNames.get(i));
            row.setArrival(formatMin(state.arrivalMin[i]));
            row.setDeparture(formatMin(state.departureMin[i]));
            int sd = state.stationDelayMinutes[i];
            int recovery = Math.min(sd - 3, Math.max(0, maxD - run) / 2);
            row.setDelayMinutes(Math.max(3, sd - recovery / (i + 1)));
            stations.add(row);
        }
        return stations;
    }

    private String localizeArrived(String lang, String dest) {
        return switch (lang) {
            case "ta" -> dest + " வந்தடைந்தது";
            case "hi" -> dest + " पहुँची";
            default -> "Arrived at " + dest;
        };
    }

    private String localizeStatus(String lang, boolean between, String fromSt, String toSt, String at) {
        if (between) {
            return switch (lang) {
                case "ta" -> fromSt + " மற்றும் " + toSt + " இடையே";
                case "hi" -> fromSt + " और " + toSt + " के बीच";
                default -> "Between " + fromSt + " and " + toSt;
            };
        }
        return switch (lang) {
            case "ta" -> at + " நிலையத்தில்";
            case "hi" -> at + " पर";
            default -> "At " + at;
        };
    }

    private static int indexFromProgress(int progress, int n) {
        float routePos = (progress / 100f) * (n - 1);
        return Math.min(n - 1, Math.max(0, (int) routePos));
    }

    private static int toMin(LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    private static LocalTime fromMin(int m) {
        int x = Math.floorMod(m, 24 * 60);
        return LocalTime.of(x / 60, x % 60);
    }

    private static String formatMin(int m) {
        return fromMin(m).format(HM);
    }

    private static String blankOr(String v, String d) {
        return (v == null || v.isBlank()) ? d : v.trim();
    }

    private static class TrainRouteState {
        String trainKey;
        List<String> stationNames;
        int[] arrivalMin;
        int[] departureMin;
        int[] stationDelayMinutes;
        int[] legKm;
        int progressPercent;
        int runningDelayMinutes;
        long lastQueryMs;
        int queryCount;
    }
}
