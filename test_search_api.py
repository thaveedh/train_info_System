import requests

url = "https://indian-railway-irctc.p.rapidapi.com/api/v1/searchTrain"
querystring = {"query": "kovai"}

headers = {
    "X-RapidAPI-Key": "df6b242d91msh03a79fd5bef11bap1c3ad1jsn7d62fe944f77",
    "X-RapidAPI-Host": "indian-railway-irctc.p.rapidapi.com"
}

# Wait, let's try a few different endpoints that are common:
endpoints = [
    ("https://indian-railway-irctc.p.rapidapi.com/api/trains-search/v1/train/kovai", {}),
    ("https://indian-railway-irctc.p.rapidapi.com/api/v1/searchTrain", {"query": "kovai"}),
    ("https://indian-railway-irctc.p.rapidapi.com/api/v1/trains", {"search": "kovai"})
]

for endpoint, params in endpoints:
    print(f"Testing {endpoint} with {params}")
    try:
        response = requests.get(endpoint, headers=headers, params=params, timeout=10)
        print(f"Status: {response.status_code}")
        print(f"Body: {response.text[:200]}")
    except Exception as e:
        print(f"Error: {e}")
    print("-" * 40)
