package com.bolivartech.utils.communications.tcpip;

import com.bolivartech.utils.array.ArrayUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.btthreads.annotations.GuardedBy;
import com.bolivartech.utils.log.LoggerFormatter;
import java.io.IOException;
import java.net.ServerSocket;
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
 * This Class is the BolivarTech's Server Manager Listener Over TCP/IP Class .
 *
 * Esta clase implementa el Manager de un servidor TCP/IP para la espera de las
 * solicitudes de conecciones hacia el cliente TCP/IP .
 *
 * Adicionalmente maneja todas las conexion establecidas al servidor
 *
 * Class ID: "35DGFH6" Loc: 000-003
 *
 * @author Julian Bolivar
 * @since 2011 - March 25, 2016.
 * @version 1.0.1
 *
 * Change Logs: v1.0.0 (2011-01-10) Version Inicial. v1.0.1 (2016-03-25) Se
 * implemento el uso del codigo de ubicacion unico
 */
public class TcpIpServer implements Runnable {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFH6";

    // Parametros de Conexion
    private final int Port;
    // Lista de conexiones activas en el servidor
    @GuardedBy("this")
    private TcpIpServerConnectionManager[] Conexiones;
    // Hebra de monitoreo de conexiones hacia el servidor
    private Thread ConectionsWatcher = null;
    // Hebra de recepcion de las  peticiones de conexion
    private TcpIPServerConnectionListener ConectionsListener = null;
    // Bandera para indicar si el servidor esta corriendo
    private boolean RunServer;
    private boolean ServerStopped;
    // Codigo de Error del listener
    private int ErrorCode;
    // Logger para el manejo de la bitacora
    private LoggerFormatter BTLogF;

    /**
     * **** Seleccion de Causas de Error en la conexion ******
     */
    /**
     * Error NO se pudo abrir el puerto de escucha para las solicitudes de
     * conexiones
     */
    public final static int ERROR_CANTOPENPORT = 1;
    /**
     * Error de IO
     */
    public final static int ERROR_IO = 2;
    /**
     * Error al tratar de cerrar el puerto
     */
    public final static int ERROR_CANTCLOSEPORT = 3;
    /**
     * Error al tratar de establecer la conexion con un cliente que realiza una
     * solicitud al servidor
     */
    public final static int ERROR_CANTCONNECTREQUEST = 4;
    /**
     * Error al tratar de poner al hebar a dormir
     */
    public final static int ERROR_CANTSLEEPTHREAD = 5;

    /**
     * Constructor del servidor que maneja conexiones TCP/IP, se debe
     * especificar el puerto donde se escuchan las peticiones de conexiones
     *
     * @param Port Puerto de escucha de las peticiones de conexiones
     */
    public TcpIpServer(int Port) {
        this.Port = Port;
        this.Conexiones = null;
        this.RunServer = false;
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
     * Retorna el puerto donde se realizan las peticiones de conexion
     *
     * @return puerto de escucha de las peticiones de conexion
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
    
    /**
     * Inicial el servidor de conexiones TCP/IP.
     *
     * @throws UtilsException
     */
    public void start() throws UtilsException {
        boolean ChangeThead;
        int Priority;

        // Inicializa la hebra de monitoreo de conexiones activas
        ConectionsWatcher = new Thread(this);
        ConectionsWatcher.setName(this.CLASSID + "[" + new Timestamp(System.currentTimeMillis()).toString() + "]");
        ChangeThead = true;
        try {
            ConectionsWatcher.checkAccess();
        } catch (SecurityException ex) {
            ChangeThead = false;
        }
        if (ChangeThead) {
            Priority = ConectionsWatcher.getPriority() - 3;
            if (Priority < Thread.MIN_PRIORITY) {
                Priority = Thread.MIN_PRIORITY;
            }
            ConectionsWatcher.setPriority(Priority);
        }
        // Inicializa la hebla de escucha de nuevas peticiones de conexiones
        ConectionsListener = new TcpIPServerConnectionListener(Port, Conexiones, this);
        ChangeThead = true;
        try {
            ConectionsListener.checkAccess();
        } catch (SecurityException ex) {
            ChangeThead = false;
        }
        if (ChangeThead) {
            Priority = ConectionsListener.getPriority() - 1;
            if (Priority < Thread.MIN_PRIORITY) {
                Priority = Thread.MIN_PRIORITY;
            }
            ConectionsListener.setPriority(Priority);
        }
        // Inicial la ejecucion del servidor
        RunServer = true;
        ConectionsWatcher.start();
        ConectionsListener.start();
        ErrorCode = ConectionsListener.GetErrorCode();
        if (ErrorCode != 0) {
            throw new UtilsException("ERROR: Could not open listener on port: " + this.Port, ErrorCode, TcpIpServer.CLASSID + "000");
        }
    }

    /**
     * Detiene el servidor de conexiones TCP/IP
     *
     * @throws UtilsException
     */
    public void Stop() throws UtilsException {
        boolean Error;

        Error = false;
        RunServer = false;
        while (!ServerStopped) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new UtilsException("ERROR: Could not sleep the TCP/IP Server Thread", ERROR_CANTSLEEPTHREAD, TcpIpServer.CLASSID + "001");
            }
        }
        ConectionsListener.Stop();
        ErrorCode = ConectionsListener.GetErrorCode();
        if (ErrorCode != 0) {
            Error = true;
        }
        ConectionsListener = null;
        this.Clean();
        ConectionsWatcher = null;
        if (Error) {
            throw new UtilsException("ERROR: Could not close listen on port: " + this.Port, ErrorCode, TcpIpServer.CLASSID + "002");
        }
    }

    /**
     * Retorna el codigo de error producido dentro del manejador de conexiones
     * del servidor
     *
     * @return Codigo de error
     */
    public synchronized int getErrorCode() {
        int Salida;

        Salida = ErrorCode;
        ErrorCode = 0;
        return Salida;
    }

    /**
     * Retorna las nuevas conexiones realizadas al servidor, si no hay nuevas
     * conexiones retorna null.
     *
     * NOTA: las conexiones retornadas por esta clase no pueden ser recuperadas
     * despues.
     *
     * @return Conexiones nuevas al servidor
     */
    public synchronized TcpIpServerConnectionManager[] getNewConnections() {
        TcpIpServerConnectionManager[] NewConexiones = null;
        int i, longitud;

        // Elimina las conexiones nulas
        if (Conexiones != null) {
            Conexiones = (TcpIpServerConnectionManager[]) ArrayUtils.arrayTrim(Conexiones);
            // Busca las nuevas conexiones
            longitud = Conexiones.length;
            for (i = 0; i < longitud; i++) {
                if (Conexiones[i].isNew()) {
                    if (NewConexiones == null) {
                        NewConexiones = new TcpIpServerConnectionManager[1];
                        NewConexiones[0] = Conexiones[i];
                    } else {
                        NewConexiones = (TcpIpServerConnectionManager[]) ArrayUtils.resizeArray(NewConexiones, NewConexiones.length + 1);
                        NewConexiones[NewConexiones.length - 1] = Conexiones[i];
                    }
                    Conexiones[i].ClearNewFlag();
                }
            }
        }
        return NewConexiones;
    }

    /**
     * Realiza la limpieza de todas las conexiones abiertas en el servidor
     */
    private synchronized void Clean() {
        int i, longitud;

        if (Conexiones != null) {
            longitud = Conexiones.length;
            for (i = 0; i < longitud; i++) {
                Conexiones[i].Stop();
                //Conexiones[i].interrupt();
                Conexiones[i] = null;
            }
            Conexiones = null;
        }
    }

    /**
     * Realiza el monitoreo de las conexiones abiertas hacia el servidor
     */
    @Override
    public void run() {
        int i, longitud;
        MersenneTwisterPlus Aleatorio;

        ServerStopped = false;
        Aleatorio = new MersenneTwisterPlus();
        while (RunServer) {
            // Verifica si se agrego una nueva conexion 
            if (ConectionsListener.ConnetionsChanged()) {
                synchronized (this) {
                    Conexiones = ConectionsListener.GetConnetions();
                    ErrorCode = ConectionsListener.GetErrorCode();
                }
            }
            // Verifica si se cerro una conexion
            synchronized (this) {
                if (Conexiones != null) {
                    longitud = Conexiones.length;
                    for (i = 0; i < longitud; i++) {
                        if (!Conexiones[i].isOpen()) {
                            Conexiones[i] = null;
                        }
                    }
                    Conexiones = (TcpIpServerConnectionManager[]) ArrayUtils.arrayTrim(Conexiones);
                    ConectionsListener.SetConnections(Conexiones);
                }
            }
            try {
                Thread.sleep(100 + (Math.abs(Aleatorio.nextInt()) % 900));
            } catch (InterruptedException ex) {
                this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "ERROR: Thread Sleeper Fail", TcpIpServer.CLASSID, "003");
            }
        }
        ServerStopped = true;
    }

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
     * This private Class the BolivarTech's Server Connection Listener Over
     * TCP/IP Class .
     *
     * Esta clase privada implementa el Listener de un servidor TCP/IP para la
     * espera de las solicitudes de conecciones hacia el cliente TCP/IP .
     *
     * <b>Esta clase es utilizada internamente por la clase TcpIpServer y no
     * debe ser utilizada fuera de este</b>
     *
     *
     *
     * @author Julian Bolivar
     * @version 1.0.0
     */
    private class TcpIPServerConnectionListener extends Thread {

        /* Usado para proger el acceso a la lista de conexiones para la sincronizacion
         * con la clase server
         */
        private final Object ConectionLock;
        private boolean Salir;
        @GuardedBy("ConectionLock")
        private TcpIpServerConnectionManager[] Conexiones;
        @GuardedBy("ConectionLock")
        private boolean ChangedConexiones;
        // Socket de espera de las conexiones hacia el servidor
        @GuardedBy("this")
        private ServerSocket ServerSocket = null;
        private int Port;
        @GuardedBy("this")
        private int ErrorCode;
        /**
         * **** Seleccion de Causas de Error en la apertura del listener ******
         */
        /**
         * Error NO se pudo abrir el puerto de escucha para las solicitudes de
         * conexiones
         */
        public final static int ERROR_CANTOPENPORT = 1;
        /**
         * Error de IO
         */
        public final static int ERROR_IO = 2;
        /**
         * Error al tratar de cerrar el puerto
         */
        public final static int ERROR_CANTCLOSEPORT = 3;
        /**
         * Error al tratar de establecer la conexion con un cliente que realiza
         * una solicitud al servidor
         */
        public final static int ERROR_CANTCONNECTREQUEST = 4;

        /**
         * Constructor con inicializacion de los parametros del listener
         *
         * @param Port
         * @param Conexiones
         * @param Lock
         */
        public TcpIPServerConnectionListener(int Port, TcpIpServerConnectionManager[] Conexiones, Object Lock) {
            super("TCP/IP Connection Listener started " + new Timestamp(System.currentTimeMillis()).toString());
            this.ChangedConexiones = false;
            this.Conexiones = Conexiones;
            this.ConectionLock = Lock;
            this.Port = Port;
            synchronized (this) {
                this.ErrorCode = 0;
            }
        }

        /**
         * Retorna el codigo de error existente en el listener, 0 indica ningun
         * error
         *
         * @return Codigo de Error
         */
        @GuardedBy("this")
        public synchronized int GetErrorCode() {
            int Salida;

            Salida = ErrorCode;
            ErrorCode = 0;
            return Salida;
        }

        /**
         * Retorna true si la lista de conexiones ha cambiado o false si no
         *
         * NOTA: Realiza el bloqueo de la variable usando el locker pasado por
         * referencia en el constructor
         *
         * @return true si cambio y false si no
         */
        protected boolean ConnetionsChanged() {
            boolean Salida;

            Salida = false;
            synchronized (ConectionLock) {
                Salida = ChangedConexiones;
            }
            return Salida;
        }

        /**
         * Retorna el apuntador de las nuevas conexiones disponibles en el
         * sistema
         *
         * NOTA: EL BLOQUEO SE DEBE REALIZAR DESDE EL LOCKER EXTERIOR que se
         * paso por referencia en el constructor
         *
         * @return apuntador al contenedor de conexiones
         */
        protected TcpIpServerConnectionManager[] GetConnetions() {
            TcpIpServerConnectionManager[] Salida;

            //synchronized (ConectionLock) {
            Salida = this.Conexiones;
            ChangedConexiones = false;
            return Salida;
            //}
        }

        /**
         * Almacena las conexiones modificadas por la clase TcpIpServer
         *
         * NOTA: EL BLOQUEO SE DEBE REALIZAR DESDE EL LOCKER EXTERIOR que se
         * paso por referencia en el constructor
         *
         * @param Conexiones Modificadas por TcpIpServer
         */
        protected void SetConnections(TcpIpServerConnectionManager[] Conexiones) {

            //synchronized (ConectionLock) {
            this.Conexiones = Conexiones;
            ChangedConexiones = false;
            //}
        }

        /**
         * Inicializa el listener de peticion de conexiones
         */
        @Override
        public void start() {

            synchronized (this) {
                Salir = false;
                try {
                    ServerSocket = new ServerSocket(Port);
                } catch (IOException e) {
                    Salir = true;
                    this.ErrorCode = ERROR_CANTOPENPORT;
                } finally {
                    super.start();
                }
            }
        }

        /**
         * Detiene el listener de peticion de conexiones
         */
        public synchronized void Stop() {

            Salir = true;
            try {
                ServerSocket.close();
            } catch (IOException ex) {
                this.ErrorCode = ERROR_CANTCLOSEPORT;
            }
            ServerSocket = null;
        }

        @Override
        public void run() {
            TcpIpServerConnectionManager ConectionManager;
            int longitud;

            while (!Salir) {
                try {
                    ConectionManager = new TcpIpServerConnectionManager(ServerSocket.accept());
                    if (ConectionManager != null) {
                        ConectionManager.setBitacora(BTLogF.getBTLoggerManager());
                        ConectionManager.start();
                        synchronized (ConectionLock) {
                            if (Conexiones != null) {
                                longitud = Conexiones.length;
                                Conexiones = (TcpIpServerConnectionManager[]) ArrayUtils.resizeArray(Conexiones, longitud + 1);
                            } else {
                                Conexiones = new TcpIpServerConnectionManager[1];
                                longitud = 0;
                            }
                            Conexiones[longitud] = ConectionManager;
                            ChangedConexiones = true;
                        }
                        ConectionManager = null;
                    }
                } catch (IOException ex) {
                    synchronized (this) {
                        this.ErrorCode = ERROR_CANTCONNECTREQUEST;
                    }
                }
            }
        }
    }
}
