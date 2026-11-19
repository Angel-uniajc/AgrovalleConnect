package com.agrovalleconnect.agrovalle_connect.dto;

/**
 * Reglas de validación compartidas por los registros de HU-01 (agricultor) y HU-17 (comprador).
 * Contrato en docs/contrato-registro-hu17.md.
 */
public final class ReglasRegistro {

    public static final int NOMBRE_MAX = 50;

    public static final int CORREO_MAX = 50;

    public static final String REGEX_CORREO =
            "^[A-Za-z0-9_%+-]+(\\.[A-Za-z0-9_%+-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$";

    public static final String REGEX_PASSWORD =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public static final String REGEX_DOCUMENTO = "\\d{6,10}";

    public static final String MSG_NOMBRE_OBLIGATORIO = "El nombre es obligatorio";

    public static final String MSG_NOMBRE_LARGO = "El nombre no puede superar 50 caracteres";

    public static final String MSG_CORREO_OBLIGATORIO = "El correo electrónico es obligatorio";

    public static final String MSG_CORREO_LARGO = "El correo no puede superar 50 caracteres";

    public static final String MSG_CORREO_FORMATO = "El correo electrónico debe tener un formato válido";

    public static final String MSG_PASSWORD_OBLIGATORIA = "La contraseña es obligatoria";

    public static final String MSG_PASSWORD_DEBIL = "La contraseña debe tener al menos 8 caracteres, "
            + "incluyendo una letra mayúscula, una letra minúscula, un número y un carácter especial";

    public static final String MSG_ROL_INVALIDO = "El rol debe ser AGRICULTOR o COMPRADOR";

    private ReglasRegistro() {
        // Solo guarda constantes; no se crean objetos de esta clase
    }
}
