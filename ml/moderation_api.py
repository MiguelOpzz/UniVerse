from flask import Flask, request, jsonify
from testtext import TextModerator  # Import your class

app = Flask(__name__)
moderator = TextModerator(
    offensive_words_path='C:/Users/MiguelW/OneDrive/Desktop/api-UniVerse-comment-topic/ml/Data/offensive_words.txt',
    safe_examples_path='C:/Users/MiguelW/OneDrive/Desktop/api-UniVerse-comment-topic/ml/Data/training_data/safe_examples.txt',
    unsafe_examples_path='C:/Users/MiguelW/OneDrive/Desktop/api-UniVerse-comment-topic/ml/Data/training_data/unsafe_examples.txt'
)

# Train the model (optional to preload)
moderator.train_model()

@app.route('/api', methods=['POST'])
def moderate_text():
    data = request.get_json()
    text = data.get('text', '')
    
    if not text:
        return jsonify({"error": "No text provided"}), 400
    
    result = moderator.check_text(text)
    result['is_safe'] = bool(result['is_safe'])
    return jsonify(result)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
