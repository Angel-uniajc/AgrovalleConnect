package com.agrovalleconnect.agrovalle_connect.dto;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Lectores JSON que no convierten tipos. Por defecto Jackson acepta 12.9 o "12"
 * como Long (y los convierte en 12) y 123 o true como String. Con estos lectores
 * esos valores se rechazan al deserializar y el endpoint responderá 400.
 * Un null no pasa por aquí: lo rechazan las validaciones del DTO (@NotNull, @NotBlank).
 */
public final class LecturaJsonEstricta {

    private LecturaJsonEstricta() {
    }

    /** Acepta solo un número entero JSON (12). Rechaza 12.9, "12", true, etc. */
    public static class EnteroEstricto extends StdDeserializer<Long> {

        public EnteroEstricto() {
            super(Long.class);
        }

        @Override
        public Long deserialize(JsonParser parser, DeserializationContext contexto) {
            if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
                return contexto.reportInputMismatch(this, "Se esperaba un número entero");
            }
            return parser.getLongValue();
        }
    }

    /** Acepta solo texto JSON ("Hola"). Rechaza 123, true, objetos y listas. */
    public static class TextoEstricto extends StdDeserializer<String> {

        public TextoEstricto() {
            super(String.class);
        }

        @Override
        public String deserialize(JsonParser parser, DeserializationContext contexto) {
            if (parser.currentToken() != JsonToken.VALUE_STRING) {
                return contexto.reportInputMismatch(this, "Se esperaba un texto");
            }
            return parser.getValueAsString();
        }
    }
}
