import numpy as np
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
%matplotlib inline
import warnings
warnings.filterwarnings("ignore")

#Pre processing tag data
df["Tag"].apply(lambda x: [tag.replace(" ", "").lower() for tag in x.split(", ")])

#Menghitung similarity
!pip install sklearn==0.0
from sklearn.feature_extraction.text import CountVectorizer
cv = CountVectorizer(max_features=5000,stop_words='english')
vector = cv.fit_transform(df['Tag']).toarray()
cv.get_feature_names_out()
from sklearn.metrics.pairwise import cosine_similarity
similarity = cosine_similarity(vector)

#Memanggil rekomendasi
def recommend(forum):
    index = df[df['Judul'] == forum].index[0]
    distances = sorted(list(enumerate(similarity[index])),reverse=True,key = lambda x: x[1])
    for i in distances[1:6]:
        print(df.iloc[i[0]].Judul)
        print("Cosine Similarity: ",i[1])

recommend('How to start learning machine learning effectively?')