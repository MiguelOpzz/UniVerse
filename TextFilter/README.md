# Text Content Moderator

## Project Overview

This Python-based text moderation tool uses machine learning to detect potentially unsafe or offensive text content. The system combines two key approaches:
1. Direct offensive word matching
2. Machine learning classification using Logistic Regression

## Key Features
- Preprocessing of input text
- Offensive word filtering
- Machine learning-based content classification
- Confidence scoring for text safety
- Customizable training data and offensive word lists

## Prerequisites

### Python Libraries
- pandas
- numpy
- scikit-learn

### Installation
```bash
pip install pandas numpy scikit-learn
```

## Project Structure
```
project_root/
│
├── Data/
│   ├── offensive_words.txt
│   └── training_data/
│       ├── safe_examples.txt
│       └── unsafe_examples.txt
└── text_moderator.py
```

## Configuration Files

### offensive_words.txt
- Contains a list of explicitly offensive words
- One word per line
- Used for direct word matching

### training_data
- `safe_examples.txt`: Contains examples of safe text
- `unsafe_examples.txt`: Contains examples of unsafe text
- Used to train the machine learning model

## Text Preprocessing
The `preprocess_text()` method performs:
- Conversion to lowercase
- Removal of special characters and numbers
- Whitespace normalization

## Model Details
- Vectorization: TF-IDF Vectorizer
  - Stops words removed
  - 1-2 word n-grams
  - Maximum 5000 features
- Classifier: Logistic Regression
  - Balanced class weights
  - Binary classification (safe/unsafe)

## Usage

### Initialization
```python
text_mod = TextModerator(
    offensive_words_path='path/to/offensive_words.txt',
    safe_examples_path='path/to/safe_examples.txt',
    unsafe_examples_path='path/to/unsafe_examples.txt'
)
```

### Training
```python
text_mod.train_model()
```
- Automatically splits data into training and test sets
- Prints classification report

### Text Safety Check
```python
result = text_mod.check_text("Your text here")
print(result['is_safe'])  # Boolean safety status
print(result['reason'])   # Reason for classification
print(result['confidence'])  # Confidence score
```

## Command-Line Interface
- Run the script directly
- Enter text to check safety
- Type 'quit' to exit

## Example Output
```
Enter text: Hello, how are you?
Result: Safe
Reason: ML model prediction
Confidence: 0.95

Enter text: Some offensive content
Result: Unsafe
Reason: Contains offensive word: [word]
Confidence: 1.00
```

## Customization
- Add/modify offensive words in `offensive_words.txt`
- Expand training data for improved accuracy
- Adjust TF-IDF or classification parameters

## Potential Improvements
- Support multiple languages
- Add more sophisticated preprocessing
- Implement more advanced ML models
- Create a web/API interface

## Limitations
- Relies on predefined word lists and training data
- May have false positives/negatives
- Performance depends on training data quality

## Troubleshooting
- Ensure all required files exist
- Check file encoding (UTF-8 recommended)
- Verify library versions

## License
Copyright (c) [2024] [David Patar Mandaoni Siringoringo]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


## Contributors
David Patar Mandaoni Siringoringo - CrazeDave-kill

## Acknowledgments
https://www.cs.cmu.edu/~biglou/resources/bad-words.txt [Database]
