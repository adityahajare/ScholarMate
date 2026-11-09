from __future__ import annotations

import logging
import os
import pickle
from datetime import datetime
from pathlib import Path
from typing import Any

from river import compose, linear_model, metrics, optim, preprocessing

log = logging.getLogger(__name__)

# Directory for persisting the online learner
ONLINE_MODEL_DIR = Path(os.getenv("ONLINE_MODEL_DIR", "/tmp/scholarmate_ml"))
ONLINE_MODEL_PATH = ONLINE_MODEL_DIR / "online_ranker.pkl"

EVENT_WEIGHTS: dict[str, float] = {
    "CLICK": 0.25,
    "SAVE": 0.50,
    "APPLY_START": 0.70,
    "SUBMIT": 0.90,
    "AWARD_CONFIRMED": 1.0,
    "DISMISS": 0.50, # negative target
}

POSITIVE_EVENTS = {"CLICK", "SAVE", "APPLY_START", "SUBMIT", "AWARD_CONFIRMED"}
NEGATIVE_EVENTS = {"DISMISS"}


class OnlineScholarshipRanker:
    def __init__(self, model_path: Path = ONLINE_MODEL_PATH) -> None:
        self.model_path = model_path
        self.pipeline = compose.Pipeline(
            preprocessing.StandardScaler(),
            linear_model.LogisticRegression(optimizer=optim.Adam(lr=0.01)),
        )
        self.metric = metrics.ROCAUC()
        self.samples_seen = 0
        self.version = "river-online-v1"
        self._load()

    def _load(self) -> None:
        if self.model_path.exists():
            try:
                with open(self.model_path, "rb") as f:
                    data = pickle.load(f)
                    self.pipeline = data.get("pipeline", self.pipeline)
                    self.metric = data.get("metric", self.metric)
                    self.samples_seen = data.get("samples_seen", 0)
                    self.version = data.get("version", self.version)
                log.info("Loaded online learning model samples_seen=%d", self.samples_seen)
            except Exception:
                log.exception("Failed to load existing online model, initializing fresh pipeline")

    def save(self) -> None:
        try:
            self.model_path.parent.mkdir(parents=True, exist_ok=True)
            with open(self.model_path, "wb") as f:
                pickle.dump(
                    {
                        "pipeline": self.pipeline,
                        "metric": self.metric,
                        "samples_seen": self.samples_seen,
                        "version": self.version,
                        "saved_at": datetime.utcnow().isoformat(),
                    },
                    f,
                )
        except Exception:
            log.exception("Failed to persist online model to %s", self.model_path)

    def learn_one(self, event_type: str, features: dict[str, Any]) -> dict[str, Any]:
        normalized_event = event_type.strip().upper()
        if normalized_event not in EVENT_WEIGHTS:
            log.warning("Unknown feedback event: %s", event_type)
            return {"status": "ignored", "reason": f"Unknown event type: {event_type}"}

        is_positive = normalized_event in POSITIVE_EVENTS
        weight = EVENT_WEIGHTS[normalized_event]

        # Extract numeric features for River
        numeric_features = {
            k: float(v)
            for k, v in features.items()
            if isinstance(v, (int, float, bool)) and not isinstance(v, str)
        }

        if not numeric_features:
            numeric_features = {"generic_bias": 1.0}

        try:
            # Predict before update to track online ROC-AUC
            prediction = self.pipeline.predict_proba_one(numeric_features)
            pred_positive = prediction.get(True, 0.5)
            self.metric.update(is_positive, pred_positive)

            # Incremental update
            self.pipeline.learn_one(numeric_features, is_positive, sample_weight=weight)
            self.samples_seen += 1

            if self.samples_seen % 5 == 0:
                self.save()

            return {
                "status": "learned",
                "event_type": normalized_event,
                "label": is_positive,
                "weight": weight,
                "samples_seen": self.samples_seen,
                "online_auc": float(self.metric.get()) if self.metric.get() is not None else 0.5,
                "model_version": self.version,
            }
        except Exception as e:
            log.exception("Error in online learner update: %s", e)
            return {"status": "error", "message": str(e)}

    def score_candidate(self, features: dict[str, Any]) -> float:
        numeric_features = {
            k: float(v)
            for k, v in features.items()
            if isinstance(v, (int, float, bool)) and not isinstance(v, str)
        }
        if not numeric_features:
            numeric_features = {"generic_bias": 1.0}
        try:
            probabilities = self.pipeline.predict_proba_one(numeric_features)
            return float(probabilities.get(True, 0.5))
        except Exception:
            return 0.5


# Singleton instance
online_ranker = OnlineScholarshipRanker()
