import vertexai
from vertexai.generative_models import GenerativeModel
import vertexai.preview.generative_models as generative_models


vertexai.init(project = "gemini-response", location = "us-central1")
model = GenerativeModel(
"gemini-1.5-flash-002"
)

GENERATION_CONFIG = {
    "max_output_tokens": 1000,
    "temperature": 0,
    "top_p": 0.95
}

SAFETY_SETTINGS = {
    generative_models.HarmCategory.HARM_CATEGORY_HATE_SPEECH: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
    generative_models.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
    generative_models.HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
    generative_models.HarmCategory.HARM_CATEGORY_HARASSMENT: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
}

def gemini_response(question):
    model = GenerativeModel(MODEL_NAME)
    
    response = model.generate_content(
        [question],
        generation_config=GENERATION_CONFIG,
        safety_settings=SAFETY_SETTINGS,
        stream=False,
    )
    
    return response.text
