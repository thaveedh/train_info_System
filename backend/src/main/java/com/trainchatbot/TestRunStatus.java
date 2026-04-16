package com.trainchatbot;

import com.trainchatbot.service.TrainApiService;

public class TestRunStatus {
    public static void main(String[] args) {
        TrainApiService service = new TrainApiService();
        System.out.println("----- Testing Train 12627 -----");
        System.out.println(service.getLiveStatus("12627"));

        System.out.println("\n----- Testing Train 12301 -----");
        System.out.println(service.getLiveStatus("12301"));

        System.out.println("\n----- Testing Train 12951 -----");
        System.out.println(service.getLiveStatus("12951"));

        System.out.println("\n----- Testing Invalid Train -----");
        System.out.println(service.getLiveStatus("99999"));
    }
}