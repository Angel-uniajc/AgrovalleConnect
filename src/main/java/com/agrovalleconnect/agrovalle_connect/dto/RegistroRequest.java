package com.agrovalleconnect.agrovalle_connect.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Contrato compartido de POST /api/v1/auth/register (HU-01 y HU-17).
 * El campo "rol" decide qué DTO se construye; sin rol se asume AGRICULTOR.
 * Cuidado: Jackson también usa defaultImpl cuando el rol es desconocido (p. ej. "ADMIN"),
 * por eso RegistroAgricultorRequest valida que rol sea nulo o AGRICULTOR.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "rol",
        visible = true,
        defaultImpl = RegistroAgricultorRequest.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegistroAgricultorRequest.class, name = "AGRICULTOR"),
        @JsonSubTypes.Type(value = RegistroCompradorRequest.class, name = "COMPRADOR")
})
public sealed interface RegistroRequest permits RegistroAgricultorRequest, RegistroCompradorRequest {

    String nombre();

    String correo();

    String password();

    String rol();
}
