package com.trainchatbot.repository;

import com.trainchatbot.model.TrainData;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * TrainRepository
 * 
 * This interface provides methods to interact with MongoDB.
 * It allows us to find train details by their 5-digit train number.
 */
public interface TrainRepository extends MongoRepository<TrainData, String> {
    Optional<TrainData> findByTrainNumber(String trainNumber);
    Optional<TrainData> findByTrainNameContainingIgnoreCase(String trainName);
}
