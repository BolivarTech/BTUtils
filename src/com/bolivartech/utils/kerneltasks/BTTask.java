package com.bolivartech.utils.kerneltasks;

import com.bolivartech.utils.log.LoggerFormatter;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;

/**
 * <p>
 * Copyright 2015,2016 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's util that implement BolivarTech's Task for
 * the KernelTask.</p>
 *
 * <p>
 * This Class implement Task Priority Control.</p>
 *
 * <p>
 * Implementa una clase que define una tarea para ser ejecutada en el
 * KernelTask.</p>
 *
 * <p>
 * Esta clase implementa el control de prioridades.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHO"</li>
 * <li>Loc: 000-001</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2015 - April 01, 2016.
 * @version 1.0.5
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2015-07-04): Version Inicial.</li>
 * <li>v1.0.1 (2015-09-26): Se agrego la capacidad de retornar el apuntador al
 * manejador de bitacoras de la tarea.</li>
 * <li>v1.0.2 (2015-10-03): El manejador de bitacora se le quito la propiedad de
 * estatico.</li>
 * <li>v1.0.3 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v1.0.4 (2016-03-28) Se implemento el uso del LoggerFormatter para la
 * bitacora.</li>
 * <li>v1.0.5 (2016-04-01) Se implemento el uso del metodo abstracto Execute()
 * como lugar donde se implementa el codigo de tarea a ejecutar.</li>
 * </ul>
 */
public abstract class BTTask implements Comparable, Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHO";

    /**
     * Valores de la prioridad
     */
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_NORMAL = 10;
    public static final int PRIORITY_HIGH = 20;

    /**
     * Identificadores de prioridad del mensaje
     */
    private int Priority;
    private long TimeStamp;
    private long NanoTimeStamp;

    /**
     * Identificador aleatorio de la tarea
     */
    private long TaskID;

    /**
     * BolivarTech Log Manager
     */
    protected LoggerFormatter BTLogF;

    /**
     * Constructor con incializacion del manejador de bitarocas.
     *
     * La bitacora vLogM esta disponible para ser usada en el metodo run().
     *
     * @param vLogM Apuntador al manejador de bitacora.
     */
    public BTTask(LoggerManager vLogM) {

        MersenneTwisterPlus Random = new MersenneTwisterPlus();
        this.BTLogF = LoggerFormatter.getInstance(vLogM);
        this.TaskID = Random.nextLong63();
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Retorna la priodidad de la tarea, la cual esta comprendida entre 1 y 20
     *
     * @return Prioridad comprendida entre 1 y 20
     */
    public int getPriority() {

        return Priority;
    }

    /**
     * Establece la prioridad de la tarea, el cual debe de estar comprendido
     * entre 1 y 20
     *
     * @param priority del mensaje entre 1 y 20
     */
    public void setPriority(int priority) {

        if (priority > PRIORITY_HIGH) {
            Priority = PRIORITY_HIGH;
        } else if (priority < PRIORITY_LOW) {
            Priority = PRIORITY_LOW;
        } else {
            Priority = priority;
        }
    }

    /**
     * Retorna el ID aleatorio asignado a la tarea
     *
     * @return Task ID
     */
    public long getTaskID() {

        return TaskID;
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
     * Retorna apuntador al manejador de bitacoras.
     *
     * @return Apuntador al manejador de bitacoras.
     */
    public LoggerManager getLogger() {

        return BTLogF.getBTLoggerManager();
    }

    /**
     * Retorna -1 si el objeto es menor que another, 0 si son iguales y 1 si
     * objeto es mayor que another
     *
     * @param another
     * @return -1 si es menor, 0 iguales y 1 si es mayor
     */
    @Override
    public int compareTo(Object another) {
        BTTask Other;
        double P1, P2, P3;
        double ATS, BTS, AP, BP, ANT, BNT, Max;
        int Result = 0;

        if (BTTask.class.isInstance(another)) {
            // Pesos comparativos de los valores
            P1 = 0.14;
            P2 = 0.85;
            P3 = 0.01;
            Other = (BTTask) another;
            // Normaliza los parametros de la ecuacion
            Max = (double) Math.max(this.TimeStamp, Other.TimeStamp);
            ATS = (((double) this.TimeStamp) / Max);
            BTS = (((double) Other.TimeStamp) / Max);
            Max = (double) Math.max(this.NanoTimeStamp, Other.NanoTimeStamp);
            ANT = (((double) this.NanoTimeStamp) / Max);
            BNT = (((double) Other.NanoTimeStamp) / Max);
            Max = (double) Math.max(this.Priority, Other.Priority);
            AP = (((double) this.Priority) / Max);
            BP = (((double) Other.Priority) / Max);
            // Ecuacion de comparacion
            Max = ((ATS - BTS) * P1) + ((BP - AP) * P2) + ((ANT - BNT) * P3);
            Result = (int) Math.signum(Max);
        } else {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "Task IS NOT BTTask Class", BTTask.CLASSID, "000");
        }
        return Result;
    }

    /**
     * Las Subclasses deben implementar este metodo con el codigo para ejecutar
     * la tarea deseada.
     *
     * @param BTLogM apuntador al manejador de bitacoras
     */
    public abstract void Execute(LoggerManager BTLogM);

    /**
     * Ejecutor de la tarea
     */
    @Override
    public void run() {
        this.Execute(BTLogF.getBTLoggerManager());
    }
}
