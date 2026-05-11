# 📄 AI-Based Resume Analyzer

![Java](https://img.shields.io/badge/Java-Swing-orange?style=flat-square&logo=java)
![Python](https://img.shields.io/badge/Python-Flask-blue?style=flat-square&logo=python)
![scikit-learn](https://img.shields.io/badge/scikit--learn-TF--IDF-green?style=flat-square&logo=scikit-learn)
![Deployment](https://img.shields.io/badge/Deployed-Render-purple?style=flat-square)

A desktop + cloud based AI Resume Analyzer that compares your resume with a job description and tells you how well you match the role — with keyword analysis and improvement suggestions.

---

## 📸 Features

- ✅ Upload PDF resume
- ✅ Paste any job description
- ✅ Get a match score (0% to 100%)
- ✅ See matched keywords (what's good)
- ✅ See missing keywords (what to add)
- ✅ Get personalized improvement tips
- ✅ Verdict — Excellent / Good / Partial / Poor Match

---

## 🏗️ Architecture

```
┌─────────────────────┐         HTTP REST API        ┌──────────────────────┐
│                     │ ─────────────────────────── ▶ │                      │
│   Java Swing App    │                               │   Python Flask API   │
│   (Desktop Client)  │ ◀ ─────────────────────────── │   (Cloud - Render)   │
│                     │         JSON Response         │                      │
└─────────────────────┘                               └──────────────────────┘
```

---

## ⚙️ How It Works

```
1. User uploads PDF resume        →   Java reads the file
2. User enters job description    →   Java sends both to Flask API
3. Flask extracts text            →   pdfminer reads PDF content
4. TF-IDF vectorization           →   converts text into numbers
5. Cosine similarity              →   calculates match percentage
6. Result returned as JSON        →   score + keywords + verdict
7. Java displays result           →   beautiful black themed UI
```

---

## 📁 Project Structure

```
resume-analyzer/
│
├── backend/
│   ├── app.py                  # Flask API - main backend logic
│   └── requirements.txt        # Python dependencies
│
├── frontend/
│   └── ResumeAnalyzer.java     # Java Swing desktop application
│
├── .gitignore                  # Files to ignore in Git
└── README.md                   # Project documentation
```

---

## 🛠️ Technologies Used

| Layer | Technology | Purpose |
|---|---|---|
| Frontend | Java Swing | Desktop GUI application |
| Backend | Python Flask | REST API server |
| NLP | scikit-learn TF-IDF | Text vectorization |
| Similarity | Cosine Similarity | Match score calculation |
| PDF | pdfminer.six | Extract text from PDF |
| Server | Gunicorn | Production WSGI server |
| Cloud | Render / Railway | Backend deployment |

---

## 🚀 Run Locally

### Prerequisites
- Java JDK 11 or higher → [Download](https://adoptium.net)
- Python 3.8 or higher → [Download](https://python.org)
- Git → [Download](https://git-scm.com)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/resume-analyzer.git
cd resume-analyzer
```

### Step 2 — Start the Backend

```bash
cd backend
pip install -r requirements.txt
python app.py
```

Backend runs at → `http://localhost:5000`

### Step 3 — Run the Frontend

```bash
cd frontend
javac ResumeAnalyzer.java
java ResumeAnalyzer
```

Java app opens → enter resume + job description → click Analyze

---

## 🌐 Deploy Backend on Render

1. Push code to GitHub
2. Go to [render.com](https://render.com) → New Web Service
3. Connect your GitHub repo
4. Set the following:

| Setting | Value |
|---|---|
| Runtime | Python 3 |
| Build Command | `pip install -r requirements.txt` |
| Start Command | `gunicorn app:app` |

5. Click **Deploy**
6. Copy your live URL → e.g. `https://resume-analyzer-xxxx.onrender.com`
7. Update this line in `ResumeAnalyzer.java`:

```java
static final String API_URL = "https://resume-analyzer-xxxx.onrender.com/analyze";
```

8. Recompile and run:

```bash
javac ResumeAnalyzer.java
java ResumeAnalyzer
```

---

## 📊 Match Score Breakdown

| Score | Verdict | Meaning |
|---|---|---|
| 75% — 100% | 🌟 Excellent Match | Strong candidate, apply confidently |
| 55% — 74% | ✅ Good Match | Good fit, minor improvements needed |
| 35% — 54% | ⚠️ Partial Match | Add more relevant keywords |
| 0% — 34% | ❌ Poor Match | Significant changes needed |

---

## ⚠️ Limitations

- Works with PDF files only
- Basic keyword-based NLP (no deep AI model)
- Match score depends on keyword overlap
- Free Render tier sleeps after 15 min of inactivity

---

## 🔮 Future Scope

- [ ] Support DOCX files
- [ ] Add machine learning models (BERT, GPT)
- [ ] Skill gap suggestions
- [ ] Web-based UI
- [ ] Save and compare multiple resumes
- [ ] ATS (Applicant Tracking System) simulation

---

## 👨‍💻 Author

**Your Name**
- GitHub → [github.com/YOUR_USERNAME](https://github.com/YOUR_USERNAME)
- Email → your@email.com

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
