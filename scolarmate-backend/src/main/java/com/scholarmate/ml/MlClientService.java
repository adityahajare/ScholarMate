package com.scholarmate.ml;

import com.scholarmate.feature.StudentFeatureVector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Simple client that talks to the external ML micro‑service.
 * For now it returns dummy data so the backend can compile and run
 * without needing a real model.
 */
@Service
@Slf4j
public class MlClientService {

    private final String mlBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public MlClientService(@Value("${scholarmate.ml.base-url:http://localhost:8000}") String mlBaseUrl) {
        this.mlBaseUrl = mlBaseUrl.replaceAll("/$", "");
    }

    /**
     * Sends the student feature vector to the ML service for encoding.
     * @return a fixed‑size float array (latent vector). If the service is not reachable,
     *         a zero‑filled vector of length 32 is returned.
     */
    public float[] encodeStudentFeatures(StudentFeatureVector student) {
        try {
            String url = mlBaseUrl + "/encode";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<StudentFeatureVector> request = new HttpEntity<>(student, headers);
            ResponseEntity<float[]> response = restTemplate.postForEntity(url, request, float[].class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("ML encode call failed, returning dummy vector", e);
            return new float[32]; // dummy latent vector
        }
    }

    /**
     * Requests the cluster id for a given latent vector.
     */
    public int predictCluster(float[] latentVector) {
        try {
            String url = mlBaseUrl + "/cluster";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<float[]> request = new HttpEntity<>(latentVector, headers);
            ResponseEntity<Integer> response = restTemplate.postForEntity(url, request, Integer.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("ML cluster call failed, returning -1", e);
            return -1;
        }
    }

    /**
     * Predicts tags for a free‑form text (used by {@link com.scholarmate.service.NoteService}).
     * Returns a comma‑separated string; on failure returns an empty string.
     */
    public String predictTags(String content) {
        try {
            String url = mlBaseUrl + "/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(content, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("ML predict tags call failed", e);
            return "";
        }
    }
}
