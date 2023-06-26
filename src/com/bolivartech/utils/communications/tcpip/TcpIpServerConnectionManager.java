package com.bolivartech.utils.communications.tcpip;

import com.bolivartech.utils.communications.Transporter;
import com.bolivartech.utils.communications.utils.SyncroBuffer;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.environment.EnvironmentUtils;
import com.bolivartech.utils.log.LoggerFormatter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Timestamp;

/**
 * Copyright 2011 BolivarTech C.A.
 *
 * <p>
 * Homepage:
 * <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage:
 * <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's Server Connection Manager Listener Over
 * TCP/IP Class .
 *
 * Esta clase implementa el Manager de Conexiones para el servidor TCP/IP.
 *
 * Class ID: "35DGFH7" Loc: 000-022
 *
 * @author Julian Bolivar
 * @since 2011 - March 25, 2016.
 * @version 1.0.1
 *
 * Change Logs: v1.0.0 (2011-01-10) Version Inicial. v1.0.1 (2016-03-25) Se
 * implemento el uso del codigo de ubicacion unico
 */
public class TcpIpServerConnectionManager extends Thread implements Transporter {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFH7";

    // Socket de conexion del servidor
    @GuardedBy("this")
    private DataInputStream is;
    @GuardedBy("this")
    private DataOutputStream os;
    private Socket socket = null;
    // ID Unico de conexion del servidor
    private String ConectionID;
    // Banderas de control
    private boolean OpenConnection = false;
    private boolean Runned = false;
    private boolean IsNew;
    private boolean StartCalled = false;
    private boolean StopCalled = false;
    // Tiempo en milisegndos cuando se abrio la conexion
    private long OpenTime;
    // Buffer de datos recibidos
    @GuardedBy("this")
    private SyncroBuffer Buffer;
    // Buffer de recepcion de datos del socket de conexion
    private int ReceiveBufferSize;
    // Buffer de envio de datos del socket de conexion
    private int SendBufferSize;
    // Keep Alive
    private boolean KeepAlive;
    // Establece el tipo de trafico TCP
    private int TrafficClass;
    // TCP No Delay
    private boolean TcpNoDelay;
    // Logger para el manejo de la bitacora
    private LoggerFormatter BTLogF;

    /**
     * ****** Traffic Class *******
     */
    /**
     * Define un trafico de bajo costo en la conexion
     */
    public final static int IPTOS_LOWCOST = 0x02;
    /**
     * Definie un trafico que requiere ser confiable
     */
    public final static int IPTOS_RELIABILITY = 0x04;
    /**
     * Define un trafico que requiere tener un alto THROUGHPUT
     */
    public final static int IPTOS_THROUGHPUT = 0x08;
    /**
     * Define un trafico que requiere tener una baja latencia en la conexion
     */
    public final static int IPTOS_LOWDELAY = 0x10;

    /**
     * **** Seleccion de Causas de Error en la conexion ******
     */
    /**
     * Error Host Desconocido
     */
    public final static int ERROR_UNKNOWHOST = 1;
    /**
     * Error de IO
     */
    public final static int ERROR_IO = 2;
    /**
     * Error de IO al enviar datos
     */
    public final static int ERROR_SENDDATA = 3;
    /**
     * Error open called by user and Start Not Called
     */
    public final static int ERROR_STARTNOTCALLED = 4;
    /**
     * Error connection manager runned
     */
    public final static int ERROR_CONNECTIONRUNNED = 5;
    /**
     * Error close called by user and Stop Not Called
     */
    public final static int ERROR_STOPNOTCALLED = 6;
    /**
     * Error de Interface
     */
    public final static int ERROR_INTERFACE = 7;

    /**
     * Constructor del manejador de conexion con el cliente
     *
     * @param socket de la conexion con el cliente
     */
    public TcpIpServerConnectionManager(Socket socket) {

        super("TCP/IP Connection Manager to " + socket.getInetAddress().getHostName() + " at " + new Timestamp(System.currentTimeMillis()).toString());
        this.OpenTime = System.currentTimeMillis();
        MersenneTwisterPlus Aleatorio = new MersenneTwisterPlus();
        this.socket = socket;
        long Hash = DJBHash(socket.getInetAddress().getHostName() + new Timestamp(this.OpenTime).toString() + convertToHex(long2byte(Aleatorio.nextLong())));
        ConectionID = convertToHex(long2byte(Hash));
        IsNew = true;
        this.ReceiveBufferSize = 8192;
        this.SendBufferSize = 8192;
        this.KeepAlive = false;
        this.TrafficClass = 0x02;
        this.TcpNoDelay = false;
        this.BTLogF = LoggerFormatter.getInstance(null);
    }

    /**
     * Establece la Bitacora a utilizar por la clase
     *
     * @param Bitacora
     */
    public void setBitacora(LoggerManager Bitacora) {
        this.BTLogF = LoggerFormatter.getInstance(Bitacora);
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
     * Retorna el Traffic Class del socket.
     *
     * @return 0 &le; Traffic Class &le; 255
     */
    public int getTrafficClass() throws UtilsException {

        if (this.socket != null) {
            try {
                this.TrafficClass = this.socket.getTrafficClass();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Traffic Class the socket", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "000");
                //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.TrafficClass;

    }

    /**
     * Establece el Traffic Class del socket.
     *
     * El valor debe ser 0 &le; TrafficClass &le; 255
     *
     * @param TrafficClass
     * @throws UtilsException
     */
    public void setTrafficClass(int TrafficClass) throws UtilsException {

        if ((TrafficClass >= 0) && (TrafficClass <= 255)) {
            if (this.socket != null) {
                try {
                    this.TrafficClass = TrafficClass;
                    this.socket.setTrafficClass(this.TrafficClass);
                } catch (SocketException ex) {
                    throw new UtilsException("ERROR: Can't set Traffic Class to " + Integer.toString(TrafficClass), ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "001");
                    //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                this.TrafficClass = TrafficClass;
            }
        }
    }

    /**
     * Establece el valor del buffer de recepcion del socket de conexion.
     *
     * Esta valor debe de ser mayor a 0.
     *
     * @param ReceiveBufferSize
     * @throws UtilsException
     */
    public void setReceiveBufferSize(int ReceiveBufferSize) throws UtilsException {

        if (ReceiveBufferSize > 0) {
            if (this.socket != null) {
                try {
                    this.ReceiveBufferSize = ReceiveBufferSize;
                    this.socket.setReceiveBufferSize(this.ReceiveBufferSize);
                } catch (SocketException ex) {
                    throw new UtilsException("ERROR: Can't set Recive Buffer Size to " + Integer.toString(ReceiveBufferSize) + " kbps ", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "002");
                    //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                this.ReceiveBufferSize = ReceiveBufferSize;
            }
        }
    }

    /**
     * Establece el valor del buffer de transmision del socket de conexion.
     *
     * Esta valor debe de ser mayor a 0.
     *
     * @param SendBufferSize
     * @throws UtilsException
     */
    public void setSendBufferSize(int SendBufferSize) throws UtilsException {

        if (SendBufferSize > 0) {
            if (this.socket != null) {
                try {
                    this.SendBufferSize = SendBufferSize;
                    this.socket.setReceiveBufferSize(this.SendBufferSize);
                } catch (SocketException ex) {
                    throw new UtilsException("ERROR: Can't set Send Buffer Size to " + Integer.toString(SendBufferSize) + " kbps ", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "003");
                    //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                this.SendBufferSize = SendBufferSize;
            }
        }
    }

    /**
     * Habilita el Keep Alive en el socket de conexion.
     *
     * @param KeepAlive
     * @throws UtilsException
     */
    public void setKeepAlive(boolean KeepAlive) throws UtilsException {

        if (this.socket != null) {
            try {
                this.KeepAlive = KeepAlive;
                this.socket.setKeepAlive(this.KeepAlive);
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't set Socket KeepAlive", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "004");
                //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.KeepAlive = KeepAlive;
        }
    }

    /**
     * Retorna el tamaño del buffer de recepcion del socket.
     *
     * @return tamaño del buffer de recepcion en bytes.
     * @throws UtilsException
     */
    public int getReceiveBufferSize() throws UtilsException {

        if (this.socket != null) {
            try {
                this.ReceiveBufferSize = this.socket.getReceiveBufferSize();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Revice Buffer Size at the socket", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "005");
                //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.ReceiveBufferSize;
    }

    /**
     * Retorna el tamaño del buffer de transmision del socket.
     *
     * @return tamaño del buffer de transmision en bytes.
     * @throws UtilsException
     */
    public int getSendBufferSize() throws UtilsException {

        if (this.socket != null) {
            try {
                this.SendBufferSize = this.socket.getSendBufferSize();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Send Buffer Size at the socket", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "006");
                //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.SendBufferSize;
    }

    /**
     * Retorna el estado del Keep Alive en el socket.
     *
     * @return Estado de la funcion de Keep Alive del socket, TRUE or FALSE.
     * @throws UtilsException
     */
    public boolean isKeepAlive() throws UtilsException {

        if (this.socket != null) {
            try {
                this.KeepAlive = this.socket.getKeepAlive();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Keep Alive status at the socket", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "007");
                //Logger.getLogger(TcpIpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return this.KeepAlive;
    }

    /**
     * Habilita el "TCP NO Delay" en el socket de conexion.
     *
     * @param TcpNoDelay
     * @throws UtilsException
     */
    public void setTCPNoDelay(boolean TcpNoDelay) throws UtilsException {

        if (this.socket != null) {
            try {
                this.TcpNoDelay = TcpNoDelay;
                this.socket.setTcpNoDelay(this.TcpNoDelay);
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't set Socket TcpNoDelay", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "008");
            }
        } else {
            this.TcpNoDelay = TcpNoDelay;
        }
    }

    /**
     * Retorna el estado del "TCP NO Delay" en el socket.
     *
     * @return Estado de la funcion "TCP NO Delay" del socket, TRUE or FALSE.
     * @throws UtilsException
     */
    public boolean isTCPNoDelay() throws UtilsException {

        if (this.socket != null) {
            try {
                this.TcpNoDelay = this.socket.getTcpNoDelay();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the TcpNoDelay status at the socket", ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "009");
            }
        }
        return this.TcpNoDelay;
    }

    /**
     * Retorna un valor hexadecimal que representa el ID de la conexion, el cual
     * es unico para cada conexion hacia el servidor
     *
     * @return ID de la conexion
     */
    public String getConnectionID() {

        return ConectionID;
    }

    /**
     * Retorna el hombre del cliente si se puede hacer una resolucion por DNS o
     * su IP si no.
     *
     * @return Nombre del Cliente o IP
     */
    public String getClient() {

        return socket.getInetAddress().getHostName();
    }

    /**
     * Retorna la cantidad de milisegundos que tiene la conexion establecida
     *
     * @return milisegundos que tiene la conexion establecida
     */
    public long getConnetionTime() {
        long Timer;

        Timer = System.currentTimeMillis() - this.OpenTime;
        return Timer;

    }

    /**
     * Retorna el puerto donde se realizo la conexion
     *
     * @return Puerto de conexion
     */
    public int getPort() {
        return this.socket.getPort();
    }

    /**
     * Realiza la apertura de la conexion y la inicializacion de todas las
     * variables.
     *
     * NOTA: ESTE METODO NO DEBE LLAMARSE POR SI SOLO, SE DEBE LLAMAR ES EL
     * METODO START PARA REALIZAR LA CONEXION Y AL INICIALIZACION DE LA HEBRA
     *
     * @throws UtilsException
     */
    @Override
    public synchronized void Open() throws UtilsException {
        boolean ChangeThead;
        int Priority;

        if ((!Runned) && (StartCalled)) {
            Buffer = new SyncroBuffer();
            try {
                this.socket.setKeepAlive(this.KeepAlive);
                this.socket.setTcpNoDelay(this.TcpNoDelay);
                this.socket.setReceiveBufferSize(this.ReceiveBufferSize);
                this.socket.setSendBufferSize(this.SendBufferSize);
                this.socket.setTrafficClass(this.TrafficClass);
                is = new DataInputStream(this.socket.getInputStream());
                os = new DataOutputStream(this.socket.getOutputStream());
            } catch (UnknownHostException ex) {
                throw new UtilsException("ERROR: Host " + socket.getInetAddress().getHostName() + " Desconocido ", ERROR_UNKNOWHOST, TcpIpServerConnectionManager.CLASSID + "010");
                //Logger.getLogger(EtherTrasnporter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                throw new UtilsException("ERROR: Conexion con el Host " + socket.getInetAddress().getHostName() + " " + ex.getLocalizedMessage(), ERROR_IO, TcpIpServerConnectionManager.CLASSID + "011");
                //Logger.getLogger(EtherTrasnporter.class.getName()).log(Level.SEVERE, null, ex);
            }
            ChangeThead = true;
            try {
                this.checkAccess();
            } catch (SecurityException ex) {
                ChangeThead = false;
            }
            if (ChangeThead) {
                Priority = this.getPriority() - 2;
                if (Priority < Thread.MIN_PRIORITY) {
                    Priority = Thread.MIN_PRIORITY;
                }
                this.setPriority(Priority);
            }

        } else if (!StartCalled) {
            throw new UtilsException("ERROR: Method Open called by user and not by Start ", ERROR_STARTNOTCALLED, TcpIpServerConnectionManager.CLASSID + "012");
        } else {
            throw new UtilsException("ERROR: Connection manager runned ", ERROR_CONNECTIONRUNNED, TcpIpServerConnectionManager.CLASSID + "013");
        }
    }

    /**
     * Realiza el cierre de la conexion y libera todas las variables
     *
     * NOTA: ESTE METODO NO DEBE LLAMARSE POR SI SOLO, SE DEBE LLAMAR ES EL
     * METODO STOP PARA REALIZAR LA DESCONEXION Y AL FINALIZACION DE LA HEBRA
     *
     * @throws UtilsException
     */
    @Override
    public synchronized void Close() throws UtilsException {

        if (StopCalled) {
            try {
                is.close();
                os.close();
                socket.close();
            } catch (IOException ex) {
                throw new UtilsException("ERROR: Al cerrar conexion con el Host " + socket.getInetAddress().getHostName() + " " + ex.getLocalizedMessage(), ERROR_IO, TcpIpServerConnectionManager.CLASSID + "014");
            }
        } else {
            throw new UtilsException("ERROR: Method Close called by user and not by Stop ", ERROR_STOPNOTCALLED, TcpIpServerConnectionManager.CLASSID + "015");
        }
    }

    /**
     * Metodo para enviar un bloque de datos hacia el cliente
     *
     * @param Datos Datos enviados al cliente
     * @throws UtilsException
     *
     */
    @Override
    public synchronized void Send(byte[] Datos) throws UtilsException {

        try {
            os.write(Datos, 0, Datos.length);
        } catch (IOException ex) {
            throw new UtilsException("ERROR: Error al enviar datos al Host " + socket.getInetAddress().getHostName() + " " + ex.getLocalizedMessage(), ERROR_SENDDATA, TcpIpServerConnectionManager.CLASSID + "016");
            //Logger.getLogger(EtherTrasnporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Metodo que retorna los datos recibidos por el puerto de comunicacion Este
     * metodo no produce bloqueo si no hay datos disponibles, este metodo
     * retorna los datos recibidos por el puerto o null si no hay ninguno.
     *
     * @return Datos leidos del puerto o null si no existe ninguno.
     */
    @Override
    public synchronized byte[] Recive() {
        byte[] Datos;

        Datos = this.Buffer.getall();
        return Datos;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Metodo que retorna TRUE si hay datos disponibles en el puerto para leer,
     * FALSE en caso contrario.
     *
     * NOTA: Aunque se garantiza la atomicidad de esta funcion y esta bloqueado
     * durante su ejecucion, al finalizar el bloqueo es liberado permitiendo que
     * cualquier otra hebra modifique el contenido de los datos recibidos con lo
     * cual este valor seria invalido, por esto se recomienda utilizar las
     * tecnicas necesarias para asegurar confiabilidad de los datos.
     *
     * @return TRUE si hay datos disponibles y FALSE si no
     */
    @Override
    public synchronized boolean DataAvailable() {
        boolean Salida;

        Salida = false;
        if (Buffer.length() > 0) {
            Salida = true;
        }
        return Salida;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Metodo que retorna el tipo de transporter que se esta usando.
     *
     * ID del Transporter Tipo 1 Ethernet
     *
     * @return ID del tipo de transponder
     */
    @Override
    public int TransporterTypeID() {
        return 1;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retorna TRUE si la conexion esta abierta y FALSE si no esta abierta.
     *
     * @return TRUE si esta abierta la conexion y FALSE si se cerro
     */
    public synchronized boolean isOpen() {

        return OpenConnection;
    }

    /**
     * Inicia la hebra de receccion de datos del servidor, abriendo el puerto y
     * realizando todas las inicializaciones para hacerlo.
     *
     * NOTA: ESTE METODO DEBE SER LLAMADO EN LUGAR DEL METODO OPEN
     *
     */
    @Override
    public void start() {

        if (!Runned) {
            try {
                StartCalled = true;
                this.Open();
                Runned = true;
                synchronized (this) {
                    OpenConnection = true;
                }
                super.start();
            } catch (UtilsException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread Start Fail", TcpIpServerConnectionManager.CLASSID, "019");
            }
        }
    }

    /**
     * Finaliza la hebra de recepcion de datos del servdor y cierra el puerto,
     * realizando la liberacion de todos los recursos utilizados para la
     * conexion.
     *
     * NOTA: ESTE METODO DEBE SER LLAMADO EN LUGAR DEL METODO CLOSE
     *
     */
    public void Stop() {

        synchronized (this) {
            OpenConnection = false;
        }
        try {
            StopCalled = true;
            this.Close();
        } catch (UtilsException ex) {
            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread Stop Fail", TcpIpServerConnectionManager.CLASSID, "020");
        }
    }

    @Override
    public void run() {
        byte[] BufferByte;
        int NumBytes;

        while (this.OpenConnection) {
            try {
                NumBytes = 0;
                do {
                    synchronized (this) {
                        if (is != null) {
                            NumBytes = is.available();
                        }
                    }
                    if ((NumBytes < 1) && (this.OpenConnection)) {
                        try {
                            EnvironmentUtils.randomSleep(500);
                        } catch (UtilsException ex) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread Sleeper Fail", TcpIpServerConnectionManager.CLASSID, "021");
                        }
                    }
                } while ((NumBytes < 1) && (this.OpenConnection));
                if (this.OpenConnection) {
                    BufferByte = new byte[NumBytes];
                    synchronized (this) {  // Realiza el bloque del buffer para realizar la escritura en el buffer
                        is.read(BufferByte, 0, NumBytes);
                        Buffer.put(BufferByte);
                    }
                }
            } catch (IOException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread Run Fail", TcpIpServerConnectionManager.CLASSID, "022");
            }
        }
    }

    /**
     * An algorithm produced by Professor Daniel J. Bernstein and shown first to
     * the world on the usenet newsgroup comp.lang.c. It is one of the most
     * efficient hash functions ever published.
     */
    private long DJBHash(String str) {
        long hash = 5381;

        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) + hash) + str.charAt(i);
        }
        return hash;
    }

    /**
     * Realiza la conversion de un arreglo de bytes en un string conteniendo la
     * representacion exadecimal.
     *
     * @param arreglo de bytes
     * @return string representando el valor hexadecimal
     */
    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Realiza la conversion de un longs en un arreglo de bytes
     *
     * @param Entrada
     * @return Arreglo de bytes
     */
    private byte[] long2byte(long Entrada) {
        byte[] Salida;
        int j;
        int multi;
        int base = 8;
        long temp;

        multi = 64 / base;
        Salida = new byte[multi];
        for (j = 0; j < multi; j++) {
            temp = Entrada << (base * j);
            Salida[j] = (byte) (temp >>> (64 - base));
        }
        return Salida;
    }

    /**
     * Verifica si es una conexion nueva retornando TRUE si lo es y FALSE si no.
     *
     * @return TRUE o FALSE
     */
    protected boolean isNew() {

        return IsNew;
    }

    /**
     * Limpia la bandera de nueva conexion
     */
    protected void ClearNewFlag() {

        IsNew = false;
    }

    /**
     * Metodo que retorna el Maximum Transmission Unit (MTU) de la interface,
     * retornando la CANTIDAD NETA de bytes que se pueden enviar por la
     * interface tomando en cuenta los encabezados usados por el protocolo.
     *
     * Este es el tamaño maximo del paquete de datos (en bytes) que se puede
     * enviar por la interface sin necesidad de fragmentar el paquete.
     *
     * NOTA: Retorna -1 si no logra recuperar el valor del MTU de la interface
     *
     * @return Maximum Transmission Unit (MTU)
     * @throws UtilsException
     */
    @Override
    public int getMTU() throws UtilsException {
        NetworkInterface Interfaz;
        int MTU;

        MTU = -1;
        // Verifica si la conexion esta asignada a una interface
        if (socket.isBound()) {
            // Retorna la interfaz por donde se conecta el socket
            try {
                Interfaz = NetworkInterface.getByInetAddress(socket.getLocalAddress());
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Al obtener la interface:" + ex.getLocalizedMessage(), ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "017");
            }
            try {
                MTU = Interfaz.getMTU();
                // Resto los 20 bytes del encabezado IP y los 20 del encabezado TCP 
                MTU -= 40;
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Al obtener el MTU de la interface:" + ex.getLocalizedMessage(), ERROR_INTERFACE, TcpIpServerConnectionManager.CLASSID + "018");
            }
        }
        return MTU;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

}
