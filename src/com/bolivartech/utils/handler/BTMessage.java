package com.bolivartech.utils.handler;

import com.bolivartech.utils.data.containers.BTRBTree;
import com.bolivartech.utils.kerneltasks.BTTask;
import com.bolivartech.utils.random.MersenneTwisterPlus;

/**
 * <p>
 * Copyright 2014 BolivarTech INC</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's util that implement BolivarTech's Messages to
 * communicate between thread.</p>
 *
 * <p>
 * When target is -1 is a broadcast message.</p>
 *
 * <p>
 * Implementa una clase que define el mensaje usando para la comunicacion entre
 * hebras.</p>
 *
 * @author Julian Bolivar
 * @since 2014 - October 14, 2015.
 * @version 1.7.2
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2014-12-12): Version Inicial.</li>
 * <li>v1.1.0 (2015-07-01): Se agrego el soporte de enviar apuntadores a objetos
 * en el mensaje.</li>
 * <li>v1.5.0 (2015-07-01): Se agrego el soporte de prioritizacion de los
 * mensajes en el handler.</li>
 * <li>v1.6.0 (2015-07-23): Se agrego la opcion de Reply para facilitar el
 * responder los mensajes al realizar el llenado de los datos de origen y
 * destino en una sola llamada.</li>
 * <li>v1.7.0 (2015-08-02): Se agrego la opcion de verificar si una llave esta
 * dentro del mensaje y retornar el tipo de dato que contiene.</li>
 * <li>v1.7.1 (2015-08-04): Se agregaron dos metodos protected para permitir
 * modificar el TimeStamp del mensaje y obtener un uso mas eficiete de la cola
 * en el handler.</li>
 * <li>v1.7.2 (2015-10-14): Se sustituyo el tipo HashMap de Java por el BTRBTree
 * como contenedor de los datos del mensaje.</li>
 *
 */
public final class BTMessage implements Comparable {

    /**
     * Valores de la prioridad
     */
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_NORMAL = 10;
    public static final int PRIORITY_HIGH = 20;

    /**
     * Resultados de la busqueda de una llave en el mensaje
     */
    public static final int KEY_NOT_FOUND = 0;
    public static final int KEY_IS_STRING = 1;
    public static final int KEY_IS_INTEGER = 2;
    public static final int KEY_IS_LONG = 3;
    public static final int KEY_IS_DOUBLE = 4;
    public static final int KEY_IS_FLOAT = 5;
    public static final int KEY_IS_BOOLEAN = 6;
    public static final int KEY_IS_BINARY = 7;
    public static final int KEY_IS_OBJECT = 8;

    /**
     * Sin Encryptamiento
     */
    public final static int NOENCRYPTION = 0;
    /**
     * Encryptado con Cuaima
     */
    public final static int CUAIMACRYPT = 1;
    /**
     * Encryptado con AES
     */
    public final static int AES = 2;
    /**
     * Mensaje de Broadcast
     */
    public final static int BROADCAST = -1;

    /**
     * Identificadores de prioridad del mensaje
     */
    private int Priority;
    private long TimeStamp;
    private long NanoTimeStamp;

    private int Encryption; // 0 none, 1 Cuaima, 2 AES
    private long MessageID; // ID del mensaje enviado
    private long Target, Origen;
    private BTHandler replyTo;
    private BTRBTree<String, String> StringData = null;
    private BTRBTree<String, Integer> IntegerData = null;
    private BTRBTree<String, Long> LongData = null;
    private BTRBTree<String, Double> DoubleData = null;
    private BTRBTree<String, Float> FloatData = null;
    private BTRBTree<String, Boolean> BooleanData = null;
    private BTRBTree<String, byte[]> BinaryData = null;
    private BTRBTree<String, Object> ObjectPointer = null;
    protected BTTask Ejecutable = null;

    /**
     * Constructor port defecto
     */
    public BTMessage() {
        super();
        MersenneTwisterPlus Random = new MersenneTwisterPlus();
        this.MessageID = Random.nextLong63();
        this.Encryption = NOENCRYPTION;
        this.Target = BROADCAST;
        this.Origen = BROADCAST;
        this.replyTo = null;
        this.StringData = new BTRBTree<String, String>();
        this.IntegerData = new BTRBTree<String, Integer>();
        this.LongData = new BTRBTree<String, Long>();
        this.DoubleData = new BTRBTree<String, Double>();
        this.FloatData = new BTRBTree<String, Float>();
        this.BooleanData = new BTRBTree<String, Boolean>();
        this.BinaryData = new BTRBTree<String, byte[]>();
        this.ObjectPointer = new BTRBTree<String, Object>();
        this.Ejecutable = null;
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Constructor de copiado
     *
     * @param Other
     */
    public BTMessage(BTMessage Other) {
        super();
        this.MessageID = Other.MessageID;
        this.Encryption = Other.Encryption;
        this.Target = Other.Target;
        this.Origen = Other.Origen;
        this.replyTo = Other.replyTo;
        this.StringData = new BTRBTree<String, String>(Other.StringData);
        this.IntegerData = new BTRBTree<String, Integer>(Other.IntegerData);
        this.LongData = new BTRBTree<String, Long>(Other.LongData);
        this.DoubleData = new BTRBTree<String, Double>(Other.DoubleData);
        this.FloatData = new BTRBTree<String, Float>(Other.FloatData);
        this.BooleanData = new BTRBTree<String, Boolean>(Other.BooleanData);
        this.BinaryData = new BTRBTree<String, byte[]>(Other.BinaryData);
        this.ObjectPointer = new BTRBTree<String, Object>(Other.ObjectPointer);
        this.Ejecutable = Other.Ejecutable;
        this.TimeStamp = Other.TimeStamp;
        this.NanoTimeStamp = Other.NanoTimeStamp;
        this.Priority = Other.Priority;
    }

    /**
     * Constructor con asignacion del BTHandler al cual se va a responder el
     * mensaje.
     *
     * @param replyTo BTHandler al cual se va a responder el mensaje.
     */
    public BTMessage(BTHandler replyTo) {
        super();
        MersenneTwisterPlus Random = new MersenneTwisterPlus();
        this.MessageID = Random.nextLong63();
        this.Encryption = NOENCRYPTION;
        this.Target = BROADCAST;
        this.Origen = replyTo.getHandlerID();
        this.replyTo = replyTo;
        this.StringData = new BTRBTree<String, String>();
        this.IntegerData = new BTRBTree<String, Integer>();
        this.LongData = new BTRBTree<String, Long>();
        this.DoubleData = new BTRBTree<String, Double>();
        this.FloatData = new BTRBTree<String, Float>();
        this.BooleanData = new BTRBTree<String, Boolean>();
        this.BinaryData = new BTRBTree<String, byte[]>();
        this.ObjectPointer = new BTRBTree<String, Object>();
        this.Ejecutable = null;
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Constructor con Inicializacion del ID del origen y el destino
     *
     * @param target ID del Destino
     * @param origen ID del Origen
     *
     */
    public BTMessage(long target, long origen) {
        super();
        MersenneTwisterPlus Random = new MersenneTwisterPlus();
        this.MessageID = Random.nextLong63();
        this.Encryption = NOENCRYPTION;
        this.Target = target;
        this.Origen = origen;
        this.replyTo = null;
        this.StringData = new BTRBTree<String, String>();
        this.IntegerData = new BTRBTree<String, Integer>();
        this.LongData = new BTRBTree<String, Long>();
        this.DoubleData = new BTRBTree<String, Double>();
        this.FloatData = new BTRBTree<String, Float>();
        this.BooleanData = new BTRBTree<String, Boolean>();
        this.BinaryData = new BTRBTree<String, byte[]>();
        this.ObjectPointer = new BTRBTree<String, Object>();
        this.Ejecutable = null;
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Constructor con Inicializacion del ID del origen y el destino, asi como
     * del BTHandler al cual responder el mensaje.
     *
     * @param target ID del Destino.
     * @param origen ID del Origen.
     * @param replyTo BTHandler al cual se va a responder el mensaje.
     */
    public BTMessage(long target, long origen, BTHandler replyTo) {
        super();
        MersenneTwisterPlus Random = new MersenneTwisterPlus();
        this.MessageID = Random.nextLong63();
        this.Encryption = NOENCRYPTION;
        this.Target = target;
        this.Origen = origen;
        this.replyTo = replyTo;
        this.StringData = new BTRBTree<String, String>();
        this.IntegerData = new BTRBTree<String, Integer>();
        this.LongData = new BTRBTree<String, Long>();
        this.DoubleData = new BTRBTree<String, Double>();
        this.FloatData = new BTRBTree<String, Float>();
        this.BooleanData = new BTRBTree<String, Boolean>();
        this.BinaryData = new BTRBTree<String, byte[]>();
        this.ObjectPointer = new BTRBTree<String, Object>();
        this.Ejecutable = null;
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Clear all the data saved in the message and set a new random MessageID
     */
    public void Clear() {
        MersenneTwisterPlus Random;

        Random = new MersenneTwisterPlus();
        this.MessageID = Random.nextLong63();
        this.Encryption = NOENCRYPTION;
        this.Target = BROADCAST;
        this.Origen = BROADCAST;
        this.StringData.Clear();
        this.IntegerData.Clear();
        this.LongData.Clear();
        this.DoubleData.Clear();
        this.FloatData.Clear();
        this.BooleanData.Clear();
        this.BinaryData.Clear();
        this.ObjectPointer.Clear();
        this.Ejecutable = null;
        this.replyTo = null;
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Clean all the data saved in the message but keep the current MessageID
     */
    public void Clean() {

        this.Encryption = NOENCRYPTION;
        this.Target = BROADCAST;
        this.Origen = BROADCAST;
        this.StringData.Clear();
        this.IntegerData.Clear();
        this.LongData.Clear();
        this.DoubleData.Clear();
        this.FloatData.Clear();
        this.BooleanData.Clear();
        this.BinaryData.Clear();
        this.ObjectPointer.Clear();
        this.Ejecutable = null;
        this.replyTo = null;
        this.TimeStamp = System.currentTimeMillis();
        this.NanoTimeStamp = System.nanoTime();
        this.Priority = PRIORITY_NORMAL;
    }

    /**
     * Generate a clean reply message but fill Target information using replyTo
     * field, fill new Origen data using the BTHandler provided in Replier and
     * clean all the data saved in the message but keep the current MessageID,
     * Priority and Encryption flags. This method return the BTHandler to reply
     * the message or NULL if this not exits
     *
     * NOTE: If no replyTo is defined at the original message this message is
     * market as BROADCAST and if Replier is NULL the origen is market as
     * BROADCAST and replyTo is filled with NULL
     *
     * @param Replier BTHandler from where is replied the message
     * @return BTHandler to respond the message
     */
    public BTHandler Reply(BTHandler Replier) {
        BTHandler newTarger;
        int keepPriority, keepEncryption;

        newTarger = this.replyTo;
        keepPriority = this.Priority;
        keepEncryption = this.Encryption;
        this.Clean();
        this.Encryption = keepEncryption;
        this.Priority = keepPriority;
        if (newTarger != null) {
            this.Target = newTarger.getHandlerID();
        }
        if (Replier != null) {
            this.replyTo = Replier;
            this.Origen = this.replyTo.getHandlerID();
        }
        return newTarger;
    }

    /**
     * Retorna la priodidad del mensaje, la cual esta comprendida entre 1 y 20
     *
     * @return Prioridad comprendida entre 1 y 20
     */
    public int getPriority() {
        return Priority;
    }

    /**
     * Establece la prioridad del mensaje, el cual debe de estar comprendido
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
     * Metodo protected para obtener el timestamp del mensaje
     *
     * @return TimeStamp del mensaje
     */
    protected long getTimeStamp() {
        return TimeStamp;
    }

    /**
     * Metodo protected para establecel el TimeStamp del mensaje
     *
     * @param timeStamp Nuevo TimeStamp del mensaje
     */
    protected void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    /**
     * @return the encryption used in the message
     */
    public int getEncryption() {

        return Encryption;
    }

    /**
     * @param encryption the encryption to set
     */
    public void setEncryption(int encryption) {

        if ((encryption >= 0) && (encryption < 3)) {
            Encryption = encryption;
        }
    }

    /**
     * Retorna el ID aleatorio asignado al mensaje
     *
     * @return Message ID
     */
    public long getMessageID() {

        return MessageID;
    }

    /**
     * @return the target
     */
    public long getTarget() {

        return Target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(long target) {

        Target = target;
    }

    /**
     * @return the origen
     */
    public long getOrigen() {

        return Origen;
    }

    /**
     * @param origen the origen to set
     */
    public void setOrigen(long origen) {

        Origen = origen;
    }

    /**
     * Retorna el apuntador al BTHandler al cual se debe responder el mensaje
     *
     * @return BTHandler al cual responder el mensaje
     */
    public BTHandler getReplyTo() {

        return replyTo;
    }

    /**
     * Establece el apuntador al BTHandler al cual se debe responder el mensaje
     *
     * @param replyTo BTHandler al cual responder el mensaje
     */
    public void setReplyTo(BTHandler replyTo) {

        this.replyTo = replyTo;
    }

    /**
     * Verifica si la llave esta contenida en el mensaje, si es asi retorna el
     * tipo de dato que contiene la llave o KEY_NOT_FOUND si no la consiguio.
     *
     * @param key Llave a buscar
     * @return Tipo de dato que contiene la llave
     */
    public int containsKey(String key) {
        int Result;

        if (this.StringData.containsKey(key)) {
            Result = KEY_IS_STRING;
        } else if (this.IntegerData.containsKey(key)) {
            Result = KEY_IS_INTEGER;
        } else if (this.LongData.containsKey(key)) {
            Result = KEY_IS_LONG;
        } else if (this.DoubleData.containsKey(key)) {
            Result = KEY_IS_DOUBLE;
        } else if (this.FloatData.containsKey(key)) {
            Result = KEY_IS_FLOAT;
        } else if (this.BooleanData.containsKey(key)) {
            Result = KEY_IS_BOOLEAN;
        } else if (this.BinaryData.containsKey(key)) {
            Result = KEY_IS_BINARY;
        } else if (this.ObjectPointer.containsKey(key)) {
            Result = KEY_IS_OBJECT;
        } else {
            Result = KEY_NOT_FOUND;
        }
        return Result;
    }

    /**
     * Return the String associated with the Key
     *
     * @return String
     */
    public String getString(String Key) {

        return StringData.get(Key);
    }

    /**
     * Set the String "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The String to set
     */
    public void setString(String Key, String Mensaje) {

        StringData.put(Key, Mensaje);
    }

    /**
     * Return the int associated with the Key
     *
     * @return int
     */
    public int getInteger(String Key) {

        return IntegerData.get(Key).intValue();
    }

    /**
     * Set the int "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The int to set
     */
    public void setInteger(String Key, int Mensaje) {

        IntegerData.put(Key, Integer.valueOf(Mensaje));
    }

    /**
     * Return the long associated with the Key
     *
     * @return long
     */
    public long getLong(String Key) {

        return LongData.get(Key).longValue();
    }

    /**
     * Set the long "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The long to set
     */
    public void setLong(String Key, long Mensaje) {

        LongData.put(Key, Long.valueOf(Mensaje));
    }

    /**
     * Return the double associated with the Key
     *
     * @return double
     */
    public double getDouble(String Key) {

        return DoubleData.get(Key).doubleValue();
    }

    /**
     * Set the double "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The double to set
     */
    public void setDouble(String Key, double Mensaje) {

        DoubleData.put(Key, Double.valueOf(Mensaje));
    }

    /**
     * Return the float associated with the Key
     *
     * @return float
     */
    public float getFloat(String Key) {

        return FloatData.get(Key).floatValue();
    }

    /**
     * Set the float "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The float to set
     */
    public void setFloat(String Key, float Mensaje) {

        FloatData.put(Key, Float.valueOf(Mensaje));
    }

    /**
     * Return the boolena associated with the Key
     *
     * @return boolean
     */
    public boolean getBoolean(String Key) {

        return BooleanData.get(Key).booleanValue();
    }

    /**
     * Set the boolean "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The boolean to set
     */
    public void setBoolean(String Key, boolean Mensaje) {

        BooleanData.put(Key, Boolean.valueOf(Mensaje));
    }

    /**
     * Return the byte array associated with the Key
     *
     * @return byte[]
     */
    public byte[] getBinary(String Key) {

        return BinaryData.get(Key);
    }

    /**
     * Set the byte array "Mensaje" value to the messaje
     *
     * @param Key La llave del Mensaje
     * @param Mensaje The byte[] to set
     */
    public void setBinary(String Key, byte[] Mensaje) {

        BinaryData.put(Key, Mensaje);
    }

    /**
     * Return the Object pointer associated with the Key
     *
     * @param Key La llave del Objeto
     * @return Object
     */
    public Object getObject(String Key) {

        return ObjectPointer.get(Key);
    }

    /**
     * Set the Object pointer "Obj" to the messaje
     *
     * @param Key La llave del Objeto
     * @param Obj The byte[] to set
     */
    public void setObject(String Key, Object Obj) {

        ObjectPointer.put(Key, Obj);
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
        BTMessage Other;
        double P1, P2, P3;
        double ATS, BTS, AP, BP, ANT, BNT, Max;
        int Result = 0;

        if (BTMessage.class.isInstance(another)) {
            // Pesos comparativos de los valores
            P1 = 0.14;
            P2 = 0.85;
            P3 = 0.01;
            Other = (BTMessage) another;
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
        }
        return Result;
    }
}
