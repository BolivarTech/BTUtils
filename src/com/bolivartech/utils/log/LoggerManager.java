package com.bolivartech.utils.log;

/**
 * <p>
 * Copyright 2011 BolivarTech INC.</p>
 *
 * <p>
 * Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>
 * This Class is the BolivarTech's LoggerManager Interface Class.</p>
 *
 * <p>
 * Esta interface establece el marco para el manejo y envio de eventos a un
 * manejador de bitacoras.</p>
 *
 * <p>
 * Esta es una interface</p>
 *
 * @author Julian Bolivar
 * @since 2011 - February 05, 2015.
 * @version 1.1.0
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (05/23/2011): Version Inicial.</li>
 * <li>v1.1.0 (02/05/2015): El tipo y los niveles de registro se cambiaron para
 * que fueran una mascara de bits.</li>
 * </ul>
 */
public interface LoggerManager {

    /**
     * Define la mascara del tipo de registro
     */
    public final static int TYPE_EVENT = 1;
    public final static int TYPE_ERROR = 2;
    /**
     * Define la mascara del nivel de registro
     */
    public final static int LEVEL_TRACE = 4;
    public final static int LEVEL_DEBUG = 8;
    public final static int LEVEL_INFO = 16;
    public final static int LEVEL_WARNING = 32;
    public final static int LEVEL_ERROR = 64;
    public final static int LEVEL_FATAL = 128;

    /**
     * Envia el mensaje de registro al manejador de bitacoras.
     *
     * NOTE: Cuando se implemente esta funcion debe ser "synchronized" para
     * asegurar que sea thread safe.
     *
     * @param Type Tipo de Evento
     * @param Level Nivel del Evento
     * @param Unit Unidad o clase donde se produce el evento
     * @param Messaje Mensaje referente al evento
     */
    public void LogMessage(int Type, int Level, String Unit, String Messaje);
}
