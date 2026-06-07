package com.eciwise.study.quiz;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utilidades de comparacion de respuestas de texto. La calificacion de preguntas
 * abiertas es por coincidencia normalizada (sin tildes, minusculas, sin signos);
 * es una limitacion conocida y puede ampliarse a una lista de respuestas aceptadas.
 */
public final class Answers {

    private Answers() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.trim().toLowerCase(Locale.ROOT);
        String noAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return noAccents.replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ").trim();
    }

    /** Interpreta verdadero/falso en es/en; null si no es reconocible. */
    public static Boolean parseBoolean(String value) {
        String n = normalize(value);
        return switch (n) {
            case "true", "verdadero", "v", "si", "sí", "1" -> Boolean.TRUE;
            case "false", "falso", "f", "no", "0" -> Boolean.FALSE;
            default -> null;
        };
    }
}
