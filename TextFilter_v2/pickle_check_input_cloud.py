import requests
import pickle
import re
import os

# Ensure the path is relative to the current script


class TextModerator:
    def __init__(self, 
                 offensive_words_url='https://storage.googleapis.com/universe-storage-bucket/ml/textfilter/offensive_words.txt'):
        self.offensive_words_url = offensive_words_url
        self.offensive_words = set()
        self.load_offensive_words()
    
    def load_offensive_words(self):
        """Load offensive words from the URL"""
        response = requests.get(self.offensive_words_url)
        
        print(f"Response status code: {response.status_code}")  
        
        if response.status_code != 200:
            raise Exception(f"Failed to load offensive words from {self.offensive_words_url}")
        
        self.offensive_words = set(word.strip().lower() for word in response.text.splitlines() if word.strip())
    
    def preprocess_text(self, text):
        """Preprocess text for cleaning"""
        text = text.lower()
        text = re.sub(r'[^a-zA-Z\s]', '', text)
        text = ' '.join(text.split())
        return text
    
    def check_text(self, text, model):
        """Check if text is safe or unsafe"""
        processed_text = self.preprocess_text(text)
        words = processed_text.split()
        for word in self.offensive_words:
            if word in words:
                return {
                    'is_safe': False,
                    'reason': f'Contains offensive word: {word}',
                    'confidence': 1.0
                }
        
        prediction = model.predict([processed_text])[0]
        probability = model.predict_proba([processed_text])[0]
        
        return {
            'is_safe': prediction == 0,
            'reason': 'ML model prediction',
            'confidence': max(probability)
        }

def main():
    # Load the pickle file
    model_path = os.path.join(os.path.dirname(__file__), 'model_v3.pkl')
    with open(model_path, 'rb') as file:
        loaded_model = pickle.load(file)
    
    # Initialize TextModerator instance
    text_mod = TextModerator()

    print("\nEnter a sentence to check for safety (type 'quit' to exit):")
    while True:
        user_input = input("\nEnter text: ")
        if user_input.lower() == 'quit':
            break
        result = text_mod.check_text(user_input, loaded_model)
        print(f"\nText: {user_input}")
        print(f"Result: {'Safe' if result['is_safe'] else 'Unsafe'}")
        print(f"Reason: {result['reason']}")
        print(f"Confidence: {result['confidence']:.2f}")

if __name__ == "__main__":
    main()
