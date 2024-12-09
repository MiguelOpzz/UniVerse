from flask import Flask, request, jsonify
import pickle
from pickle_check_input_cloud import TextModerator  # Assuming your class is in text_moderator.py
import numpy as np
import os

app = Flask(__name__)

# Load the model once when the server starts
model_path = os.path.join(os.path.dirname(__file__), 'model_v3.pkl')
with open(model_path, 'rb') as file:
    loaded_model = pickle.load(file)

# Initialize the TextModerator instance
moderator = TextModerator()

@app.route('/api', methods=['POST'])
def check_text_route():
    data = request.json
    text = data.get('text', '')
    result = moderator.check_text(text, loaded_model)
    
    # Convert any numpy data types to Python native types
    clean_result = {key: bool(value) if isinstance(value, np.bool_) else value for key, value in result.items()}
    
    return jsonify(clean_result)

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))
