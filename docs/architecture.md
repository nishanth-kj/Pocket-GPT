# Architecture Overview

Pocket GPT is designed to operate 100% offline. This means all heavy lifting, including Natural Language Processing (NLP) and Retrieval-Augmented Generation (RAG), happens directly on the device.

## Local RAG Pipeline

1. **Document Ingestion:** Users load PDF or text documents into the app.
2. **Chunking & Embedding:** The app extracts text, splits it into chunks, and uses an on-device embedding model (e.g., via ONNX Runtime or TFLite) to convert text into vector embeddings.
3. **Storage:** The embeddings and their corresponding text chunks are stored in a local SQLite database using Android's Room persistence library.
4. **Retrieval:** When a user asks a question, the query is embedded, and a Cosine Similarity search is executed against the SQLite database to find the most relevant chunks.
5. **Generation:** The retrieved context is fed into a local Small Language Model (SLM) to generate an accurate and private answer.

## Web Stack
The landing page is built using **Next.js 14 (App Router)** and **Tailwind CSS**. It is statically exported and hosted on GitHub Pages via automated GitHub Actions.
