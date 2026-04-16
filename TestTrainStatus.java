import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class TestTrainStatus {

    public static String calculateDynamicTrainStatus(List<String> stations, List<String> scheduledTimes) {
        if (stations == null || scheduledTimes == null || stations.size() != scheduledTimes.size() || stations.isEmpty()) {
            return "Invalid schedule data provided.";
        }

        int delayMinutes = new java.util.Random().nextInt(31); 
        LocalTime now = LocalTime.now();
        System.out.println("Current Time: " + now.withNano(0) + " (Simulated delay applied: " + delayMinutes + " mins)");

        for (int i = 0; i < stations.size(); i++) {
            LocalTime actualTime = LocalTime.parse(scheduledTimes.get(i)).plusMinutes(delayMinutes);
            System.out.println("  -> " + stations.get(i) + " expects train at: " + actualTime);
            
            if (now.isBefore(actualTime) && i == 0) {
                return String.format("Not started (Delay: %d mins)", delayMinutes);
            }
            
            if (now.isBefore(actualTime)) {
                return String.format("Running between %s and %s (Delay: %d mins)", 
                                      stations.get(i - 1), stations.get(i), delayMinutes);
            }
        }

        return String.format("Reached destination: %s (Delay: %d mins)", 
                             stations.get(stations.size() - 1), delayMinutes);
    }

    public static void main(String[] args) {
        System.out.println("--- Test 1: Future Schedule (Not Started) ---");
        LocalTime future = LocalTime.now().plusHours(2);
        List<String> s1 = Arrays.asList("Station A", "Station B");
        List<String> t1 = Arrays.asList(future.toString().substring(0, 5), future.plusHours(1).toString().substring(0, 5));
        System.out.println("Result: " + calculateDynamicTrainStatus(s1, t1) + "\n");

        System.out.println("--- Test 2: In Transit (Running between) ---");
        LocalTime past = LocalTime.now().minusHours(1);
        LocalTime upcoming = LocalTime.now().plusHours(1);
        List<String> s2 = Arrays.asList("Station X", "Station Y", "Station Z");
        List<String> t2 = Arrays.asList(past.toString().substring(0, 5), upcoming.toString().substring(0, 5), upcoming.plusHours(1).toString().substring(0, 5));
        System.out.println("Result: " + calculateDynamicTrainStatus(s2, t2) + "\n");
        
        System.out.println("--- Test 3: Past Schedule (Reached Destination) ---");
        LocalTime longPast1 = LocalTime.now().minusHours(3);
        LocalTime longPast2 = LocalTime.now().minusHours(2);
        List<String> s3 = Arrays.asList("Start City", "End City");
        List<String> t3 = Arrays.asList(longPast1.toString().substring(0, 5), longPast2.toString().substring(0, 5));
        System.out.println("Result: " + calculateDynamicTrainStatus(s3, t3) + "\n");
    }
}
