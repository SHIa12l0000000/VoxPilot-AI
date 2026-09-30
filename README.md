# VoxPilot AI

> **Talk. Interrupt. Learn. Act.**  
> *A real-time multilingual voice learning copilot built on Agora Conversational AI.*

---

## 🌟 Overview

**VoxPilot AI** is a real-time, interactive, multilingual voice copilot that transforms continuous speech into interactive learning and action. Unlike conventional chatbots that attach a microphone button to a standard text API, VoxPilot is built from the ground up for continuous conversational voice interaction:

$$\text{Speak} \longrightarrow \text{Understand} \longrightarrow \text{Respond} \longrightarrow \text{Interrupt} \longrightarrow \text{Continue} \longrightarrow \text{Act}$$

With VoxPilot, conversations flow naturally: you speak, Vox answers, you interrupt mid-sentence in Hindi or Hinglish, Vox immediately stops and pivots without missing a beat, quizzes you verbally, and saves structured study notes locally to your device.

---

## 🚀 Hackathon Problem & Solution

### The Problem
Traditional voice assistants and LLM wrappers feel robotic, rigid, and slow:
1. **No Real-Time Barge-In**: Users must sit through long, uninterrupted AI soliloquies or push "stop" buttons.
2. **Monolingual Constraints**: Most voice interfaces are hardcoded to English and falter when users switch to Hindi or code-switch into Hinglish.
3. **Turn-Taking Friction**: Push-to-talk models break conversational immersion.
4. **Ephemerality**: Insights, explanations, and concepts discussed verbally are lost once the call ends.

### The Solution: VoxPilot AI
- **Continuous Ultra-Low-Latency Audio**: Streamed via Agora RTC & RTM channels with near-instantaneous speech turnaround.
- **Natural Mid-Sentence Interruption**: Sub-second barge-in powered by real-time Voice Activity Detection (VAD) and Agora session interruption endpoints.
- **Fluid Multilingual Intelligence**: Native understanding and fluid code-switching across **English**, **Hindi**, and **Hinglish**.
- **Interactive Learn Mode**: Acts as a personal Socratic tutor that explains complex concepts using everyday analogies (e.g. cricket, tea stalls), provides bite-sized answers, and conducts spoken quizzes.
- **Local Smart Notes**: Automatically extracts titles, summaries, and key takeaways into a persistent local database whenever the user says *"Save this as notes"* or taps the bookmark action.
- **Polished Dark Navy Design**: High-contrast, minimalist Jetpack Compose Material 3 interface featuring a living, state-reactive animated Voice Orb.

---

## 📐 Architecture & Agora Foundation

VoxPilot AI preserves and extends Agora's real-time communication stack:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        VoxPilot Android Client                         │
│                                                                        │
│   ┌────────────────────┐   ┌───────────────────┐   ┌───────────────┐   │
│   │    Compose M3 UI   │   │  TurnManager &    │   │  Local SQLite │   │
│   │   Animated Orb     │◄──┤  BargeInDetector  │   │  Notes & Hist │   │
│   └─────────▲──────────┘   └─────────▲─────────┘   └───────▲───────┘   │
│             │                        │                     │           │
│   ┌─────────▼────────────────────────▼─────────────────────▼───────┐   │
│   │                    ConversationViewModel                       │   │
│   └──────────────────────────────────▲─────────────────────────────┘   │
│                                      │                                 │
│   ┌──────────────────────────────────▼─────────────────────────────┐   │
│   │                AgoraConversationSessionManager                 │   │
│   │             (RtcEngine & RtmClient Session Loop)               │   │
│   └──────────────▲──────────────────────────────────▲──────────────┘   │
└──────────────────┼──────────────────────────────────┼──────────────────┘
                   │ Real-Time Audio (RTC)            │ Signaling (RTM)
                   ▼                                  ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     Agora Real-Time Network (SD-RTN)                   │
└──────────────────────────────────▲─────────────────────────────────────┘
                                   │ Token Auth / Session Orchestration
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      VoxPilot FastAPI Server                           │
│                                                                        │
│   • /v1/conversation/bootstrap  -> RTC/RTM session credentials         │
│   • /v1/conversation/join       -> Starts Agora Agent with mode/lang   │
│   • /v1/conversation/interrupt  -> Halts agent playback on barge-in    │
│   • /v1/conversation/leave      -> Clean session tear-down             │
│                                                                        │
│         ┌───────────────────┬───────────────────┬────────────────┐     │
│         │   Deepgram STT    │    OpenAI LLM     │  MiniMax TTS   │     │
│         │  (Multilingual)   │  (VoxPilot Prompt)│  (Low Latency) │     │
│         └───────────────────┴───────────────────┴────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘
```

### Agora Components Used:
- **Agora RTC (v4.6.4)**: Low-latency two-way audio streaming between Android and cloud agent.
- **Agora RTM (v2.3.0)**: Real-time signaling, turn detection events, transcript streaming, and agent state notifications.
- **Agora Conversational AI SDK (v2.8.1)**: Cloud agent lifecycle orchestration on the FastAPI backend.

---

## ⚡ Interruption & Barge-In Design

Interruption is natural and automatic:
1. While the AI is speaking (`AGENT_SPEAKING`), the user begins talking.
2. `BargeInDetector` and `AudioSessionManager` analyze acoustic energy and VAD confidence to filter out speaker leakage and confirm near-end user speech.
3. The client transitions UI state to `LISTENING` and sends an immediate interrupt signal via `/v1/conversation/interrupt`.
4. The server halts the agent's audio synthesis immediately.
5. Deepgram STT transcribes the user's speech and routes it to the LLM with conversational context preserved.

---

## 🌐 Multilingual Voice Pipeline

- **Speech Recognition (STT)**: Deepgram multilingual model configured with dynamic language selection (`multi`, `hi`, `en`). Supports automatic code-switching between Hindi and English (Hinglish).
- **Speech Synthesis (TTS)**: MiniMax ultra-low latency conversational voice capable of natural English, Hindi, and Indian English pronunciation.
- **Adaptive System Prompt**: Grounded on the backend to converse concisely, pivot language immediately upon user request, and use culturally relevant metaphors.

---

## 🎯 Modes of Operation

1. **LEARN (Hackathon Star Feature)**:
   - Interactive Socratic tutor.
   - Explains concepts step-by-step in 2–3 sentences.
   - Pushes analogies (cricket, food, everyday life) when prompted.
   - Verbal quizzing (*"Quiz me"* asks one question, listens to your verbal answer, evaluates, and waits for *"Next"*).
2. **ASK**:
   - General-purpose voice copilot for questions, brainstorming, and direct inquiries.
3. **TRANSLATE**:
   - Conversational English ↔ Hindi voice translation with dual-language live transcript display.

---

## 📱 Android App Features

- **Living Voice Orb**: Jetpack Compose Canvas orb reacting dynamically to `IDLE`, `CONNECTING`, `LISTENING`, `THINKING`, `SPEAKING`, and `ERROR` states.
- **Live Transcript Stream**: Compact, non-intrusive `YOU` and `VOX` speech cards.
- **Smart Notes**: Room/SQLite local database storing note ID, title, summary, key takeaways, and timestamps.
- **Session History**: Local archive of conversation logs with duration and quick "Convert to Note" capabilities.
- **Voice Action Triggers**: Say *"Save this as notes"* or *"Notes banao"* during conversation to trigger automatic note creation.
- **Settings**: Language selector (`AUTO`, `English`, `Hindi / Hinglish`), dark navy theme toggle, data wipe, and attribution.

---

## 🛠️ Getting Started

### Prerequisites
- **Android Studio Ladybug or newer** (with Android SDK 36 installed)
- **Java 17+**
- **Python 3.10+** (with virtualenv)
- **Agora Account**: App ID and App Certificate from [Agora Console](https://console.agora.io/)

---

### 1. Backend Setup

```bash
cd server
python -m venv venv
# Windows:
.\venv\Scripts\activate
# Linux/macOS:
source venv/bin/activate

pip install -r requirements.txt
```

Create `server/.env.local`:
```env
AGORA_APP_ID=your_32_character_agora_app_id
AGORA_APP_CERTIFICATE=your_32_character_certificate
AGORA_AGENT_UID=123456
AGORA_AREA=NORTH_AMERICA

ASR_MODEL=nova-3
ASR_LANGUAGE=multi
LLM_MODEL=gpt-4o-mini
TTS_MODEL=speech_2_6_turbo
TTS_VOICE_ID=English_captivating_female1
```

Run the server:
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Expose the server via HTTPS tunnel (e.g. ngrok):
```bash
ngrok http 8000
```

---

### 2. Android Setup

In project root `local.properties`:
```properties
sdk.dir=C\:\\Users\\Shivam\\AppData\\Local\\Android\\Sdk
QUICKSTART_SERVER_URL=https://your-ngrok-tunnel-url.ngrok-free.app
```

Build the debug APK:
```bash
gradlew assembleDebug
```

Output APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🏆 Hackathon Demo Script

Follow this exact flow for the highest impact hackathon presentation:

1. **Launch App**: Open VoxPilot AI on Android. Notice the deep navy background and breathing Voice Orb. Select **LEARN** mode.
2. **Start Conversation**: Tap **Start Conversation**. The orb transitions to `LISTENING`.
3. **Ask Concept**: 
   > *User*: "Hey Vox, teach me binary search."  
   > *Vox*: "Binary search is an efficient algorithm to find an item in a sorted list by repeatedly dividing the search interval in half..."
4. **Natural Interruption (Barge-In)**:
   > *User (interrupts mid-sentence)*: "Wait, Hindi mein samjhao."  
   > *Vox (immediately stops, switches)*: "Haan bilkul! Maan lo tumhare paas ek sorted list hai, jaise dictionary. Har baar hum list ko theek beech se aadha kar dete hain..."
5. **Contextual Analogy**:
   > *User*: "Cricket ka example use karo."  
   > *Vox*: "Socho batsman run chase kar raha hai aur scoreboard par required run rate dhoondhna hai. Agar target zyada hai toh uppar dekho, kam hai toh neeche..."
6. **Verbal Quiz**:
   > *User*: "Quiz me."  
   > *Vox*: "Sawaal: Binary search chalane ke liye list ka sorted hona zaroori hai ya nahi?"  
   > *User*: "Haan, sorted hona zaroori hai."  
   > *Vox*: "Shabaash! Bilkul sahi jawaab. Binary search sorted data par hi kaam karta hai. Next question ke liye batao."
7. **Smart Note Generation**:
   > *User*: "Save this as notes."  
   > *Vox / App*: Toast indicates *"Note saved: Binary search in Hindi"*.
8. **Inspect Notes**: Tap the **Notes** bottom navigation tab. Show the generated title, summary, and bulleted takeaways stored in the local database!

---

## 🔒 Security & Privacy

- **Zero Credentials on Client**: `AGORA_APP_CERTIFICATE` and AI model keys reside strictly on the server.
- **Short-Lived Tokens**: The Android app requests temporary, channel-scoped RTC/RTM session tokens on demand.
- **Local-Only Storage**: Session history and notes remain private on the user's device and are never sent to external tracking services.

---

## 📜 Legal & Attribution

VoxPilot AI is developed as a hackathon submission built on the **Agora Conversational AI Android Quickstart** and **Agora SD-RTN™** (Real-Time Communication & Real-Time Messaging engines). All Agora libraries and SDKs are subject to the Agora Terms of Service and respective open source licenses.
