import requests

url = "https://indian-railway-irctc.p.rapidapi.com/api/trains/v1/train/status"
querystring = {"train_number":"12627","isH5":"true", "departure_date": "2026-04-10"}

headers = {
    "X-RapidAPI-Key": "df6b242d91msh03a79fd5bef11bap1c3ad1jsn7d62fe944f77",
    "X-RapidAPI-Host": "indian-railway-irctc.p.rapidapi.com"
}

response = requests.get(url, headers=headers, params=querystring, timeout=30)
print(f"Status Code: {response.status_code}")
print(f"Response: {response.text}")
