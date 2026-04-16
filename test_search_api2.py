import requests
import json

headers = {
    "X-RapidAPI-Key": "df6b242d91msh03a79fd5bef11bap1c3ad1jsn7d62fe944f77",
    "X-RapidAPI-Host": "indian-railway-irctc.p.rapidapi.com"
}
endpoints = [
    ("https://indian-railway-irctc.p.rapidapi.com/api/trains-search/v1/train/kovai", {}),
    ("https://indian-railway-irctc.p.rapidapi.com/api/v1/searchTrain", {"query": "kovai"}),    
    ("https://indian-railway-irctc.p.rapidapi.com/api/v1/getTrainByName", {"name": "kovai"}),
    ("https://indian-railway-irctc.p.rapidapi.com/api/v1/getTrainSchedule", {"trainNo": "kovai"})
]

with open('api_results_utf8.txt', 'w', encoding='utf-8') as f:
    for endpoint, params in endpoints:
        f.write(f"Testing {endpoint} with {params}\n")
        try:
            response = requests.get(endpoint, headers=headers, params=params, timeout=10)
            f.write(f"Status: {response.status_code}\n")
            f.write(f"Body: {response.text[:200]}\n")
        except Exception as e:
            f.write(f"Error: {e}\n")
        f.write("-" * 40 + "\n")
