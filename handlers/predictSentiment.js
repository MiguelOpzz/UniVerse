// sentimentModel.js
const tf = require('@tensorflow/tfjs-node');
let sentimentModel;

// Load the sentiment model
const loadSentimentModel = async () => {
  try {
    sentimentModel = await tf.loadLayersModel('https://storage.googleapis.com/universe-storage-bucket/ml/text-sentiment/sentiment_model.h5');
    console.log('Sentiment model loaded successfully!');
  } catch (error) {
    console.error('Error loading sentiment model:', error.message);
  }
};

// Preprocess the text (simple example, you might need more sophisticated preprocessing)
const preprocessText = (text) => {
  // Convert text to lowercase and remove non-alphabetical characters
  return text.toLowerCase().replace(/[^a-z\s]/g, '');
};

// Convert text to tokenized form (example of character-based encoding)
const textToTensor = (text) => {
  const cleanedText = preprocessText(text);
  // Simple tokenization: split by spaces and map words to ASCII values (you could use a more complex tokenizer here)
  const wordTokens = cleanedText.split(' ').map((word) => word.charCodeAt(0));
  
  // Pad the sequence to a fixed length (e.g., 100) if necessary
  const paddedTokens = wordTokens.length < 100 ? [...wordTokens, ...new Array(100 - wordTokens.length).fill(0)] : wordTokens.slice(0, 100);
  
  return tf.tensor2d([paddedTokens], [1, 100]);
};

// Predict sentiment of the input text
const predictSentiment = async (text) => {
  const inputTensor = textToTensor(text);
  const prediction = sentimentModel.predict(inputTensor);
  const result = prediction.dataSync();
  
  return result[0] > 0.5 ? 'positive' : 'negative'; // Simplified binary classification
};

module.exports = {
  loadSentimentModel,
  predictSentiment,
};
