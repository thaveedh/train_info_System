from pymongo import MongoClient
import os

# MongoDB Seeding Script
# This script reads your connection URI from application.properties
# and populates your Atlas database with sample train data.

def get_mongo_uri():
    try:
        prop_path = os.path.join("backend", "src", "main", "resources", "application.properties")
        if not os.path.exists(prop_path):
             return "mongodb://localhost:27017/train_bot"
        with open(prop_path, "r") as f:
            for line in f:
                if line.startswith("spring.data.mongodb.uri="):
                    return line.split("=", 1)[1].strip()
    except Exception as e:
        print(f"Error reading application.properties: {e}")
    return "mongodb://localhost:27017/train_bot"

def seed_data():
    uri = get_mongo_uri()
    print(f"Connecting to MongoDB...")
    
    try:
        client = MongoClient(uri)
        # Extract DB name from URI (usually after the / and before ?)
        db_name = uri.split("/")[-1].split("?")[0]
        if not db_name:
            db_name = "train_bot"
            
        db = client[db_name]
        collection = db["train_details"]

        sample_trains = [
            {
                "trainNumber": "12627",
                "trainName": "Karnataka Express",
                "source": "Bangalore City (SBC)",
                "destination": "New Delhi (NDLS)",
                "scheduleTime": "19:20 (Departure)",
                "runningDays": "Daily"
            },
            {
                "trainNumber": "12675",
                "trainName": "Kovai Express",
                "source": "MGR Chennai Central (MAS)",
                "destination": "Coimbatore Junction (CBE)",
                "scheduleTime": "06:10 (Departure)",
                "runningDays": "Daily"
            },
            {
                "trainNumber": "12673",
                "trainName": "Cheran Express",
                "source": "MGR Chennai Central (MAS)",
                "destination": "Coimbatore Junction (CBE)",
                "scheduleTime": "22:00 (Departure)",
                "runningDays": "Daily"
            },
            {
                "trainNumber": "12001",
                "trainName": "New Delhi Shatabdi Express",
                "source": "Bhopal (BPL)",
                "destination": "New Delhi (NDLS)",
                "scheduleTime": "15:00 (Departure)",
                "runningDays": "Daily"
            }
        ]

        collection.delete_many({})
        collection.insert_many(sample_trains)
        print(f"✅ Successfully seeded {len(sample_trains)} trains into '{db_name}.train_details'!")

    except Exception as e:
        print(f"❌ Error seeding database: {e}")

if __name__ == "__main__":
    seed_data()