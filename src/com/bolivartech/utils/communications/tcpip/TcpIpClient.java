package com.bolivartech.utils.communications.tcpip;

import com.bolivartech.utils.communications.Transporter;
import com.bolivartech.utils.communications.utils.SyncroBuffer;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.btthreads.annotations.ThreadSafe;
import com.bolivartech.utils.log.LoggerFormatter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Copyright 2011 BolivarTech C.A.
 *
 * <p>
 * Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's Transporter Over TCP/IP Class implementing
 * the Transporter Interface .
 *
 * Esta clase implementa la interface trasnporte sobre TCP/IP para el envio y
 * recepcion de datos desde y hacia un servidor TCP/IP .
 *
 * Class ID: "35DGFH5" Loc: 000-017
 *
 * @author Julian Bolivar
 * @since 2011 @date January 10, 2012.
 * @version 1.0.1
 *
 * Change Logs: v1.0.0 (2011-01-10) Version Inicial. v1.0.1 (2016-03-25) Se
 * implemento el uso del codigo de ubicacion unico
 */
@ThreadSafe
public class TcpIpClient implements Transporter, Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFH5";

    // Parametros de Conexion
    private final String Host;
    private final int Port;
    // Socket de conexion hacia el servidor
    @GuardedBy("this")
    private DataInputStream is;
    @GuardedBy("this")
    private DataOutputStream os; //  PrintStream os;
    @GuardedBy("this")
    private Socket clientSocket = null;
    // Hebra de recepcion de datos
    private Thread Receptor = null;
    // Control de la Hebra de Recepcion
    private boolean RunReceptor;
    // Buffer de datos recibidos;
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
     * Error de Interface
     */
    public final static int ERROR_INTERFACE = 4;

    /**
     * Constructor con inicializacon de la configuracion
     *
     * @param Host Donde se realiza la conexion, puede ser el un nombre de
     * dominio que puede ser resuelto por DNS o IP en el formato XXX.XXX.XXX.XXX
     * @param Port Puerto TCP donde conectarse
     */
    public TcpIpClient(String Host, int Port) {
        this.Host = Host;
        this.Port = Port;
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
     * Retorna el Traffic Class del socket.
     *
     * @return 0 &le; Traffic Class &le; 255
     * @throws UtilsException
     */
    public int getTrafficClass() throws UtilsException {

        if (clientSocket != null) {
            try {
                this.TrafficClass = clientSocket.getTrafficClass();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Traffic Class the socket", ERROR_INTERFACE, TcpIpClient.CLASSID + "000");
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
            if (clientSocket != null) {
                try {
                    this.TrafficClass = TrafficClass;
                    clientSocket.setTrafficClass(this.TrafficClass);
                } catch (SocketException ex) {
                    throw new UtilsException("ERROR: Can't set Traffic Class to " + Integer.toString(TrafficClass), ERROR_INTERFACE, TcpIpClient.CLASSID + "001");
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
            if (clientSocket != null) {
                try {
                    this.ReceiveBufferSize = ReceiveBufferSize;
                    clientSocket.setReceiveBufferSize(this.ReceiveBufferSize);
                } catch (SocketException ex) {
                    throw new UtilsException("ERROR: Can't set Recive Buffer Size to " + Integer.toString(ReceiveBufferSize) + " kbps ", ERROR_INTERFACE, TcpIpClient.CLASSID + "002");
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
            if (clientSocket != null) {
                try {
                    this.SendBufferSize = SendBufferSize;
                    clientSocket.setReceiveBufferSize(this.SendBufferSize);
                } catch (SocketException ex) {
                    throw new UtilsException("ERROR: Can't set Send Buffer Size to " + Integer.toString(SendBufferSize) + " kbps ", ERROR_INTERFACE, TcpIpClient.CLASSID + "003");
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

        if (clientSocket != null) {
            try {
                this.KeepAlive = KeepAlive;
                clientSocket.setKeepAlive(this.KeepAlive);
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't set Socket KeepAlive", ERROR_INTERFACE, TcpIpClient.CLASSID + "004");
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

        if (clientSocket != null) {
            try {
                this.ReceiveBufferSize = clientSocket.getReceiveBufferSize();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Revice Buffer Size at the socket", ERROR_INTERFACE, TcpIpClient.CLASSID + "005");
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

        if (clientSocket != null) {
            try {
                this.SendBufferSize = clientSocket.getSendBufferSize();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Send Buffer Size at the socket", ERROR_INTERFACE, TcpIpClient.CLASSID + "006");
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

        if (clientSocket != null) {
            try {
                this.KeepAlive = clientSocket.getKeepAlive();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the Keep Alive status at the socket", ERROR_INTERFACE, TcpIpClient.CLASSID + "007");
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

        if (clientSocket != null) {
            try {
                this.TcpNoDelay = TcpNoDelay;
                clientSocket.setTcpNoDelay(this.TcpNoDelay);
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't set Socket TcpNoDelay", ERROR_INTERFACE, TcpIpClient.CLASSID + "008");
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

        if (clientSocket != null) {
            try {
                this.TcpNoDelay = clientSocket.getTcpNoDelay();
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Can't get the TcpNoDelay status at the socket", ERROR_INTERFACE, TcpIpClient.CLASSID + "009");
            }
        }
        return this.TcpNoDelay;
    }

    /**
     * Retorna el Host hacia donde se realiza la coneccion
     *
     * @return Host donde se realiza la conexion
     */
    public String getHost() {
        return Host;
    }

    /*
     * Establece el donde se realiza la conexion, puede ser el un nombre de
     * dominio que puede ser resuelto por DNS o IP en el formato XXX.XXX.XXX.XXX
     *
     * @param Host Donde se realiza la conexion, puede ser el un nombre de
     * dominio que puede ser resuelto por DNS o IP en el formato XXX.XXX.XXX.XXX
     *
     * public void setHost(String Host) { this.Host = Host; }
     */
    /**
     * Retorna el puerto hacia donde se realizan las conexiones en el servidor
     *
     * @return Puerto
     */
    public int getPort() {
        return Port;
    }

    /**
     * Retorna el identificador de la Clase
     * 
     * @return Identificador de la clase
     */
    public static String getCLASSID() {
        return CLASSID;
    }

    /*
     * Establece el puerto hacia donde se realiza la conexion
     *
     * @param Port
     *
     * public void setPort(int Port) { this.Port = Port; }
     */
    /**
     * Metodo para enviar un bloque de datos hacia el sensor.
     *
     * @param Datos Datos enviados al puerto
     * @throws UtilsException
     *
     */
    @Override
    public synchronized void Send(byte[] Datos) throws UtilsException {

        try {
            os.write(Datos, 0, Datos.length);
        } catch (IOException ex) {
            throw new UtilsException("ERROR: Error al enviar datos al Host " + this.Host + " " + ex.getLocalizedMessage(), ERROR_SENDDATA, TcpIpClient.CLASSID + "010");
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
     * Metodo que retorna el tipo de transporter que se esta usando
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
     * Abre la conexion del transportador
     *
     * @throws UtilsException
     */
    @Override
    public synchronized void Open() throws UtilsException {
        boolean ChangeThead;
        int Priority;

        Buffer = new SyncroBuffer();
        try {
            clientSocket = new Socket(this.Host, this.Port);
            clientSocket.setKeepAlive(this.KeepAlive);
            clientSocket.setTcpNoDelay(this.TcpNoDelay);
            clientSocket.setReceiveBufferSize(this.ReceiveBufferSize);
            clientSocket.setSendBufferSize(this.SendBufferSize);
            clientSocket.setTrafficClass(this.TrafficClass);
            //clientSocket.setPerformancePreferences(8,2,3);
            //System.out.println("Class: "+Integer.toString(clientSocket.getTrafficClass()));
            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());
        } catch (UnknownHostException ex) {
            throw new UtilsException("ERROR: Host " + this.Host + " Desconocido ", ERROR_UNKNOWHOST, TcpIpClient.CLASSID + "011");
            //Logger.getLogger(EtherTrasnporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            throw new UtilsException("ERROR: Conexion con el Host " + this.Host + " " + ex.getLocalizedMessage(), ERROR_IO, TcpIpClient.CLASSID + "012");
            //Logger.getLogger(EtherTrasnporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        RunReceptor = true;
        Receptor = new Thread(this);
        ChangeThead = true;
        try {
            Receptor.checkAccess();
        } catch (SecurityException ex) {
            ChangeThead = false;
        }
        if (ChangeThead) {
            Priority = Receptor.getPriority() - 2;
            if (Priority < Thread.MIN_PRIORITY) {
                Priority = Thread.MIN_PRIORITY;
            }
            Receptor.setPriority(Priority);
        }
        Receptor.start();
    }

    /**
     * Cierra la conexion del transportador
     *
     * @throws UtilsException
     */
    @Override
    public synchronized void Close() throws UtilsException {

        if (Receptor != null) {
            this.RunReceptor = false;
        }
        try {
            is.close();
            os.close();
            clientSocket.close();
            clientSocket = null;
            is = null;
            os = null;
        } catch (IOException ex) {
            throw new UtilsException("ERROR: Al cerrar conexion con el Host " + this.Host + " " + ex.getLocalizedMessage(), ERROR_IO, TcpIpClient.CLASSID + "013");
        }
    }

    @Override
    public void run() {
        byte[] BufferByte;
        int NumBytes;
        MersenneTwisterPlus Aleatorio;

        Aleatorio = new MersenneTwisterPlus();
        while (this.RunReceptor) {
            try {
                NumBytes = 0;
                do {
                    synchronized (this) {
                        if (is != null) {
                            NumBytes = is.available();
                        }
                    }
                    if ((NumBytes < 1) && (this.RunReceptor)) {
                        try {
                            Thread.sleep(100 + (Math.abs(Aleatorio.nextInt()) % 400));
                        } catch (InterruptedException ex) {
                            this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread Sleeper Fail", TcpIpClient.CLASSID, "016");
                        }
                    }
                } while ((NumBytes < 1) && (this.RunReceptor));
                if (this.RunReceptor) {
                    BufferByte = new byte[NumBytes];
                    synchronized (this) {  // Realiza el bloque del buffer para realizar la escritura en el buffer
                        if (is != null) {
                            is.read(BufferByte, 0, NumBytes);
                            Buffer.put(BufferByte);
                        }
                    }
                }
            } catch (IOException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread RUN Fail", TcpIpClient.CLASSID, "017");
            }
        }
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
        if (clientSocket.isBound()) {
            // Retorna la interfaz por donde se conecta el socket
            try {
                Interfaz = NetworkInterface.getByInetAddress(clientSocket.getLocalAddress());
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Al obtener la interface:" + ex.getLocalizedMessage(), ERROR_INTERFACE, TcpIpClient.CLASSID + "014");
            }
            try {
                MTU = Interfaz.getMTU();
                // Resto los 20 bytes del encabezado IP y los 20 del encabezado TCP 
                MTU -= 40;
            } catch (SocketException ex) {
                throw new UtilsException("ERROR: Al obtener el MTU de la interface:" + ex.getLocalizedMessage(), ERROR_INTERFACE, TcpIpClient.CLASSID + "015");
            }
        }
        return MTU;
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
