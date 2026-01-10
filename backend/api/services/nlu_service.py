def process_nlu(message, lang="en"):
    message = message.lower()

    # Extract train number
    train_no = None
    for word in message.split():
        if word.isdigit() and len(word) == 5:
            train_no = word

    # Intent detection
    intent = "train_status" if train_no else "unknown"

    entities = {"train_no": train_no}
    return intent, entities
