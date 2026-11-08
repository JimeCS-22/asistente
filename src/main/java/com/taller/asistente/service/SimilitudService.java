package com.taller.asistente.service;

import org.springframework.stereotype.Service;

@Service
public class SimilitudService {

    /** cos(A,B) = (A·B) / (||A|| * ||B||). Entre -1 y 1; más cerca de 1 = más parecido. */
    public double coseno(double[] a, double[] b) {
        // ====== TODO 1 (Paso 1): implementar la similitud coseno ======
        double punto = 0, normaA = 0, normaB = 0;
        for (int i = 0; i < a.length; i++) {
            punto += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }
        return punto / (Math.sqrt(normaA) * Math.sqrt(normaB));
        // ====== FIN TODO 1 ======
    }
}