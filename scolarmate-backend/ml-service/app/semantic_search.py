from __future__ import annotations

import logging
import numpy as np
from typing import Any
from app.recommendation import _sentence_model, _tokens

log = logging.getLogger(__name__)


def compute_semantic_similarities(query: str, documents: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Compute semantic cosine similarity between search query and scholarship documents."""
    if not documents or not query.strip():
        return []

    try:
        model = _sentence_model()
        doc_texts = [
            f"{doc.get('name') or ''} {doc.get('description') or ''} {doc.get('category') or ''} {doc.get('provider') or ''}"
            for doc in documents
        ]
        all_texts = [query] + doc_texts
        embeddings = model.encode(all_texts, normalize_embeddings=True, show_progress_bar=False)

        query_vec = embeddings[0]
        doc_vecs = embeddings[1:]
        similarities = np.clip(doc_vecs @ query_vec, 0.0, 1.0)

        results = []
        for doc, sim in zip(documents, similarities):
            results.append({
                "scholarship_id": doc["id"],
                "score": round(float(sim), 4),
            })

        results.sort(key=lambda x: x["score"], reverse=True)
        return results
    except Exception as e:
        log.warning("Sentence transformer unavailable for semantic search, falling back to lexical overlap: %s", e)
        # Lexical fallback
        query_tokens = set(_tokens(query.lower()))
        results = []
        for doc in documents:
            text = f"{doc.get('name', '')} {doc.get('description', '')} {doc.get('category', '')}".lower()
            doc_tokens = set(_tokens(text))
            if not doc_tokens:
                score = 0.0
            else:
                intersection = query_tokens.intersection(doc_tokens)
                score = len(intersection) / max(len(query_tokens), 1)
            results.append({
                "scholarship_id": doc["id"],
                "score": round(float(min(1.0, score)), 4),
            })
        results.sort(key=lambda x: x["score"], reverse=True)
        return results
