from flask import Flask, request, jsonify
from flask_cors import CORS
from pdfminer.high_level import extract_text
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import io
import re

app = Flask(__name__)
CORS(app)

def clean_text(text):
    text = text.lower()
    text = re.sub(r'\s+', ' ', text)
    text = re.sub(r'[^a-z0-9 ]', '', text)
    return text.strip()

def extract_keywords(text, top_n=20):
    vectorizer = TfidfVectorizer(stop_words='english', max_features=top_n)
    vectorizer.fit([text])
    return list(vectorizer.vocabulary_.keys())

def get_verdict(score):
    if score >= 75:
        return {"verdict": "Excellent Match", "emoji": "🌟", "color": "green",
                "message": "Your resume strongly matches the job. You are a great candidate!"}
    elif score >= 55:
        return {"verdict": "Good Match", "emoji": "✅", "color": "blue",
                "message": "Your resume matches reasonably well. A few improvements can make it stronger."}
    elif score >= 35:
        return {"verdict": "Partial Match", "emoji": "⚠️", "color": "orange",
                "message": "Your resume partially matches. Add more relevant skills and keywords."}
    else:
        return {"verdict": "Poor Match", "emoji": "❌", "color": "red",
                "message": "Your resume does not match well. Significant improvements needed before applying."}

def find_keywords(resume_text, job_text):
    job_keywords = extract_keywords(job_text, top_n=20)
    matched = [kw for kw in job_keywords if kw in resume_text]
    missing = [kw for kw in job_keywords if kw not in resume_text]
    return matched, missing

@app.route('/', methods=['GET'])
def home():
    return jsonify({"status": "Resume Analyzer API is running!"})

@app.route('/analyze', methods=['POST'])
def analyze():
    if 'resume' not in request.files or 'job_description' not in request.form:
        return jsonify({'error': 'Missing resume or job description'}), 400

    pdf_file = request.files['resume']
    job_desc = request.form['job_description']

    if not pdf_file.filename.endswith('.pdf'):
        return jsonify({'error': 'Only PDF files are supported'}), 400

    if len(job_desc.strip()) < 20:
        return jsonify({'error': 'Job description is too short'}), 400

    raw_resume = extract_text(io.BytesIO(pdf_file.read()))
    resume_text = clean_text(raw_resume)
    job_text = clean_text(job_desc)

    if len(resume_text) < 50:
        return jsonify({'error': 'Could not extract enough text from PDF'}), 400

    vectorizer = TfidfVectorizer()
    vectors = vectorizer.fit_transform([resume_text, job_text])
    score = cosine_similarity(vectors[0], vectors[1])[0][0]
    percent = round(score * 100, 2)

    verdict_info = get_verdict(percent)
    matched_kw, missing_kw = find_keywords(resume_text, job_text)

    return jsonify({
        "score": percent,
        "verdict": verdict_info["verdict"],
        "emoji": verdict_info["emoji"],
        "color": verdict_info["color"],
        "message": verdict_info["message"],
        "matched_keywords": matched_kw[:10],
        "missing_keywords": missing_kw[:10],
        "resume_word_count": len(raw_resume.split()),
        "job_word_count": len(job_desc.split())
    })

if __name__ == '__main__':
    app.run(debug=True)
