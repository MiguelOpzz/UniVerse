import pandas as pd
import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras import layers
from tensorflow.keras.models import Sequential

# Load train dataset
train_file = r'C:\Users\david\OneDrive\Documents\MSIB\Bangkit\TextFilter\train.csv'  # Update with your file path
train_df = pd.read_csv(train_file, encoding='ISO-8859-1')  # Ensure the correct encoding

# Preprocess text data to ensure all values are strings and handle NaN values
X_train = train_df['text'].fillna('').astype(str).values
y_train = train_df['sentiment'].values

# Map sentiments to numeric values
sentiment_mapping = {'positive': 2, 'neutral': 1, 'negative': 0}
y_train = np.array([sentiment_mapping[sent] for sent in y_train])

# Tokenize and pad the text
max_words = 10000
max_sequence_length = 50

tokenizer = Tokenizer(num_words=max_words, lower=True, oov_token="<OOV>")
tokenizer.fit_on_texts(X_train)

X_train_seq = tokenizer.texts_to_sequences(X_train)
X_train_padded = pad_sequences(X_train_seq, maxlen=max_sequence_length, padding='post')

# Train-test split for validation
X_train_split, X_val, y_train_split, y_val = train_test_split(X_train_padded, y_train, test_size=0.2, random_state=42)

# Build the model using TensorFlow
embedding_dim = 64

model = Sequential([
    layers.Embedding(input_dim=max_words, output_dim=embedding_dim, input_length=max_sequence_length),
    layers.LSTM(64, return_sequences=True),
    layers.Dropout(0.5),
    layers.LSTM(32),
    layers.Dense(3, activation='softmax')  # 3 classes: positive, neutral, negative
])

model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

# Train the model
print("Training the model...")
history = model.fit(
    X_train_split, y_train_split,
    epochs=10,
    validation_data=(X_val, y_val),
    batch_size=32,
    verbose=1
)

# Function to predict sentiment of a comment
def predict_sentiment(comment, model, tokenizer):
    comment_seq = tokenizer.texts_to_sequences([comment])
    comment_padded = pad_sequences(comment_seq, maxlen=max_sequence_length, padding='post')
    prediction = model.predict(comment_padded)
    sentiment_index = np.argmax(prediction, axis=1)[0]

    inverse_sentiment_mapping = {v: k for k, v in sentiment_mapping.items()}
    return inverse_sentiment_mapping[sentiment_index]

# Input comments to test sentiment
while True:
    comment = input("Enter a comment to predict its sentiment (type 'exit' to stop): ")
    if comment.lower() == 'exit':
        print("Exiting...")
        break
    sentiment = predict_sentiment(comment, model, tokenizer)
    print(f"The sentiment of the comment is: {sentiment}")
