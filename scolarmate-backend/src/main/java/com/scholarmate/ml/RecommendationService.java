package com.scholarmate.ml;

import org.springframework.stereotype.Service;
import com.scholarmate.feature.StudentFeatureVector;
import lombok.RequiredArgsConstructor;

/**
 * Service that communicates with the ML micro‑service to obtain latent vectors
 * and to request clustering of those vectors.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MlClientService mlClientService;

    /**
     * Sends a student feature vector to the ML service and receives a latent vector.
     * The ML service implements an autoencoder and returns a fixed‑size float array.
     */
    public float[] getStudentLatentVector(StudentFeatureVector student) {
        // The ML service expects a JSON payload with the structured features.
        // Here we simply forward the entire object; Jackson will serialize it.
        return mlClientService.encodeStudentFeatures(student);
    }

    /**
     * Triggers clustering on the ML side and returns the cluster ID for the given vector.
     */
    public int getClusterId(float[] latentVector) {
        return mlClientService.predictCluster(latentVector);
    }
}
