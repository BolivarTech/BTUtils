package com.bolivartech.utils.log;

import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.exception.UtilsException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Copyright 2016 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>This Class is the BolivarTech's util that implement the Singlenton
 BolivarTech's LogMsg Formatter.</p>
 * 
 * <p>NOTE: This class is Thread Safe.</p>
 *
 * <p>Implementa una clase Singlenton que define el formateador de eventos para la bitacoras.</p>
 * 
 * <p>Clase del tipo Singleton</p>
 *
 * <p>NOTA: Esta clase es segura para las concurrencias.</p>
 *
 * <ul>
 * <li>Class ID: "35DLFMN"</li>
 * <li>Loc: none</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2016 - March 27, 2016.
 * @version 1.0.0
 *
 * <p>Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2016-03-27): Version Inicial.</li>
 * </ul>
 */
public class LoggerFormatter {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DLFMN";
    
    // Los lock para el manejo de concurrencia
    private final static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
    // Manejador de Bitacora
    @GuardedBy("rwl")
    LoggerManager BTLogM;
    
    // Instancia de la clase
    @GuardedBy("rwl")
    private static LoggerFormatter instance = null;

    /**
     * Constructor privado con inicializacion del manejador de bitacoras
     *
     * @param BTLogM Apuntador al manejador de bitacoras
     */
    private LoggerFormatter(LoggerManager BTLogM) {
        this.BTLogM = BTLogM;
    }

    /**
     * Retorna le Instancia de la clase Singlenton.
     * 
     * NOTA: El manejador de bitacora usado sera especificado la
     * primera vez que se llama este metodo, en las demas llamadas
     * el parametro es ignorado retornando la instancia del formateador.
     *
     * @param BTLogM Apuntador al manejador de bitacoras
     * @return Instancia de la clase
     */
    public static LoggerFormatter getInstance(LoggerManager BTLogM) {

        rwl.writeLock().lock();
        try {
            if (instance == null) {
                instance = new LoggerFormatter(BTLogM);
            }
        } finally {
            rwl.writeLock().unlock();
        }
        return instance;
    }

    /**
     * Retorna el manejador de bitacora contenido en el formateador
     * 
     * @return manejador de bitacora
     */
    public LoggerManager getBTLoggerManager() {
        LoggerManager Result;
        
        rwl.readLock().lock();
        try{
            Result = BTLogM;
        }finally{
            rwl.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el identificador de la Clase
     * 
     * @return Identificador de la clase
     */
    public static String getCLASSID() {
        return CLASSID;
    }
    
    /**
     * Metodo que se encarga de manejar y formatear el envio de mensajes hacia el
     * manejador de bitacoras.
     *
     * Las Banderas usadas son las especificadas por la interface LoggerManager
     * de BolivarTech.
     *
     * Si el mensaje no es especificado, se recupera el mensaje de le exception
     *
     * Si el ClassID o el LogCode no son especificados, se trata de recuperar la
     * informacion a partir de la excepcion y siempre se imprime el stack
     *
     * @param Tipo Tipo de Evento
     * @param Nivel Nivel del Evento
     * @param printStack TRUE para imprimer el stack de la excepcion ex o FALSE
     * para no.
     * @param ex Excepcion de la cual imprimir el stack
     * @param Mensaje Mensaje a enviar al manejador de bitacoras
     * @param ClassID Identificador de la clase
     * @param LocCode Identificador de ubicacion del evento
     */
    public void LogMsg(int Tipo, int Nivel, boolean printStack, Exception ex, String Mensaje, String ClassID, String LocCode) {
        this.Logger(this.BTLogM,Tipo,Nivel,printStack,ex,Mensaje,ClassID,LocCode);
    }
    
    /**
     * Metodo que se encarga de manejar y formatear el envio de mensajes hacia el
     * manejador de bitacoras.
     *
     * Las Banderas usadas son las especificadas por la interface LoggerManager
     * de BolivarTech.
     *
     * Si el mensaje no es especificado, se recupera el mensaje de le exception
     *
     * Si el ClassID o el LogCode no son especificados, se trata de recuperar la
     * informacion a partir de la excepcion y siempre se imprime el stack
     *
     * @param BTLogM Manerador de bitacoras de BolivarTech
     * @param Tipo Tipo de Evento
     * @param Nivel Nivel del Evento
     * @param printStack TRUE para imprimer el stack de la excepcion ex o FALSE
     * para no.
     * @param ex Excepcion de la cual imprimir el stack
     * @param Mensaje Mensaje a enviar al manejador de bitacoras
     * @param ClassID Identificador de la clase
     * @param LocCode Identificador de ubicacion del evento
     */
    private void Logger(LoggerManager BTLogM, int Tipo, int Nivel, boolean printStack, Exception ex, String Mensaje, String ClassID, String LocCode) {
        // Impresion del Stack
        StringWriter StackTrace;
        // Identificador de ubicacion
        String LocationID;

        // Forma el identificador de posicion
        if ((ClassID != null) && (LocCode != null)) {
            LocationID = ClassID + LocCode;
        } else if (LocCode != null) {
            if ((ex != null) && (UtilsException.class.isInstance(ex))) {
                LocationID = "???????" + LocCode + " --> " + ((UtilsException) ex).getLocCode();
                printStack = true;
            } else {
                LocationID = "???????" + LocCode;
            }
        } else if (ClassID != null) {
            if ((ex != null) && (UtilsException.class.isInstance(ex))) {
                LocationID = ClassID + "??? --> " + ((UtilsException) ex).getLocCode();
                printStack = true;
            } else {
                LocationID = ClassID + "???";
            }
        } else if ((ex != null) && (UtilsException.class.isInstance(ex))) {
            LocationID = "?????????? --> " + ((UtilsException) ex).getLocCode();
            printStack = true;
        } else {
            LocationID = "??????????";
            printStack = true;
        }
        // Si el mensaje no esta definido se trata de recuperar de la excepcion
        if ((Mensaje == null) && (ex != null)) {
            Mensaje = ex.getMessage();
        } else if ((Mensaje == null) && (ex == null)) {
            Mensaje = "UNKNOWN";
        }
        // Agrega el identificador de ubicacion del mensaje
        Mensaje = "(" + LocationID + ") " + Mensaje;
        // Agrega el stack al mensaje
        if (printStack) {
            if (ex != null) {
                StackTrace = new StringWriter();
                ex.printStackTrace(new PrintWriter(StackTrace));
                Mensaje += "\nSTACK TRACE(" + LocationID + "):\n" + StackTrace.toString();
            } else {
                Mensaje += "Can't Print Stack(" + LocationID + ") Due Exception is NULL";
            }
        }
        // Imprime el mensaje en el manejador de bitacora
        if (BTLogM != null) {
            BTLogM.LogMessage(Tipo, Nivel, LocationID, Mensaje);
        } else {
            switch (Nivel) {
                case LoggerManager.LEVEL_TRACE:
                    Logger.getLogger(LocationID).log(Level.INFO, Mensaje);
                    break;
                case LoggerManager.LEVEL_DEBUG:
                    Logger.getLogger(LocationID).log(Level.INFO, Mensaje);
                    break;
                case LoggerManager.LEVEL_INFO:
                    Logger.getLogger(LocationID).log(Level.INFO, Mensaje);
                    break;
                case LoggerManager.LEVEL_WARNING:
                    Logger.getLogger(LocationID).log(Level.WARNING, Mensaje);
                    break;
                case LoggerManager.LEVEL_ERROR:
                    Logger.getLogger(LocationID).log(Level.SEVERE, Mensaje);
                    break;
                case LoggerManager.LEVEL_FATAL:
                    Logger.getLogger(LocationID).log(Level.SEVERE, Mensaje);
                    break;
                default:
                    Logger.getLogger(LocationID).log(Level.SEVERE, Mensaje);
            }
        }
    }
}
