import json
import base64
import vertexai
from vertexai.generative_models import GenerativeModel, SafetySetting, Part

vertexai.init(project = "gemini-response", location = "us-central1")
model = GenerativeModel(
        "gemini-1.5-flash-002"
        )

with open('schema.json', 'r') as schema_file:
    SCHEMA = json.load(schema_file)

with open('example_convo.json', 'r') as convo_file:
    discussion = json.load(convo_file)
    
with open('prompt.txt', 'r') as prompt_file:
    PROMPT = prompt_file.read()

GENERATION_CONFIG = {
    "response_mime_type": "application/json",
    "max_output_tokens": 1000,
    "temperature": 0,
    "top_p": 0.95,
    "response_schema": SCHEMA,
}

SAFETY_SETTINGS = {
    generative_models.HarmCategory.HARM_CATEGORY_HATE_SPEECH: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
    generative_models.HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
    generative_models.HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
    generative_models.HarmCategory.HARM_CATEGORY_HARASSMENT: generative_models.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
}
    
def gemini_response(discussion):
    usr_resp = "forum_id "+discussion['forum_id']+" \n "
    for usr in discussion['conversations'] :
        usr_resp += usr['response'] + " \n "
        
    response = model.generate_content(
        [PROMPT, usr_resp],
        generation_config=GENERATION_CONFIG,
        safety_settings=SAFETY_SETTINGS,
        stream=False,
    )
    
    return response.text
