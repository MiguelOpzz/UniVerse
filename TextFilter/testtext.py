import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.metrics import classification_report
import re
import os

class TextModerator:
    def __init__(self, 
                 offensive_words_path='Users/david/OneDrive/Documents/MSIB/Bangkit/TextFilter/data/offensive_words.txt',
                 safe_examples_path='Users/david/OneDrive/Documents/MSIB/Bangkit/TextFilter/data/training_data/safe_examples.txt',
                 unsafe_examples_path='Users/david/OneDrive/Documents/MSIB/Bangkit/TextFilter/data/training_data/unsafe_examples.txt'):
        self.offensive_words_path = offensive_words_path
        self.safe_examples_path = safe_examples_path
        self.unsafe_examples_path = unsafe_examples_path
        
        self.offensive_words = set()
        self.pipeline = Pipeline([
            ('tfidf', TfidfVectorizer(
                stop_words='english',
                ngram_range=(1, 2),
                max_features=5000
            )),
            ('classifier', LogisticRegression(class_weight='balanced'))
        ])
        self.load_data()
    
    def preprocess_text(self, text):
        """Preprocess text for cleaning"""
        # Convert to lowercase
        text = text.lower()
        # Remove special characters and numbers
        text = re.sub(r'[^a-zA-Z\s]', '', text)
        # Remove extra whitespaces
        text = ' '.join(text.split())
        return text
    
    def load_data(self):
        """Load all necessary data from text files"""
        # Validate file paths
        for path in [self.offensive_words_path, self.safe_examples_path, self.unsafe_examples_path]:
            if not os.path.exists(path):
                raise FileNotFoundError(f"File not found: {path}")
        
        # Load offensive words
        with open(self.offensive_words_path, 'r', encoding='utf-8') as f:
            self.offensive_words = set(word.strip().lower() for word in f.readlines() if word.strip())
        
        # Load safe examples
        with open(self.safe_examples_path, 'r', encoding='utf-8') as f:
            safe_examples = [line.strip() for line in f.readlines() if line.strip()]
        
        # Load unsafe examples
        with open(self.unsafe_examples_path, 'r', encoding='utf-8') as f:
            unsafe_examples = [line.strip() for line in f.readlines() if line.strip()]
        
        # Create training dataset
        self.training_data = pd.DataFrame({
            'text': [self.preprocess_text(text) for text in safe_examples + unsafe_examples],
            'is_unsafe': [0] * len(safe_examples) + [1] * len(unsafe_examples)
        })
    
    def train_model(self):
        """Train the moderation model"""
        # Ensure we have training data
        if len(self.training_data) == 0:
            raise ValueError("No training data available")
        
        # Split the data
        X_train, X_test, y_train, y_test = train_test_split(
            self.training_data['text'], 
            self.training_data['is_unsafe'], 
            test_size=0.2, 
            random_state=42
        )
        
        # Train the pipeline
        self.pipeline.fit(X_train, y_train)
        
        # Print model performance
        y_pred = self.pipeline.predict(X_test)
        print("Model Performance:")
        print(classification_report(y_test, y_pred))
    
    def check_text(self, text):
        """Check if text is safe or unsafe"""
        # Preprocess the text
        processed_text = self.preprocess_text(text)
        
        # Direct word matching
        text_lower = processed_text.lower()
        for word in self.offensive_words:
            if word in text_lower:
                return {
                    'is_safe': False,
                    'reason': f'Contains offensive word: {word}',
                    'confidence': 1.0
                }
        
        # ML-based check
        prediction = self.pipeline.predict([processed_text])[0]
        probability = self.pipeline.predict_proba([processed_text])[0]
        
        return {
            'is_safe': prediction == 0,
            'reason': 'ML model prediction',
            'confidence': max(probability)
        }

def main():
    # Initialize text moderator with default or custom paths
    text_mod = TextModerator(
        offensive_words_path='Users/david/OneDrive/Documents/MSIB/Bangkit/TextFilter/data/offensive_words.txt',
        safe_examples_path='Users/david/OneDrive/Documents/MSIB/Bangkit/TextFilter/data/training_data/safe_examples.txt',
        unsafe_examples_path='Users/david/OneDrive/Documents/MSIB/Bangkit/TextFilter/data/training_data/unsafe_examples.txt'
    )
    
    # Train the model
    text_mod.train_model()
    
    # Test text moderation
    test_texts = [
        "Can someone help with homework?",
        "This class is good",
        "fuck man",
        "I love learning new things",
        "You are an idiot"
    ]
    
    print("\nTesting Text Moderation:")
    for text in test_texts:
        result = text_mod.check_text(text)
        print(f"\nText: {text}")
        print(f"Result: {'Safe' if result['is_safe'] else 'Unsafe'}")
        print(f"Reason: {result['reason']}")
        print(f"Confidence: {result['confidence']:.2f}")

if __name__ == "__main__":
    main()