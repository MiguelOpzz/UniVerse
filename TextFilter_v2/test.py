import pickle;
from sklearn.pipeline import Pipeline

with open('model_v5.pkl', 'rb') as file:
    model = pickle.load(file)
print(model)  # Verify loaded object
