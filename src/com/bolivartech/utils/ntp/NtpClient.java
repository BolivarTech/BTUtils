package com.bolivartech.utils.ntp;

import com.bolivartech.utils.log.LoggerFormatter;
import com.bolivartech.utils.log.LoggerManager;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * <p>
 * Copyright 2015,2016 BolivarTech INC</p>
 *
 * <p>
 * Implementa un cliente NTP para realizar la correccion del
 * System.currentTimeMillis()</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHQ"</li>
 * <li>Loc: 000-004</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2015 - March 25, 2016.
 * @version 1.0.2
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v1.0.2 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v1.0.1 (2015-08-22) - TimeOut manage when received the update.</li>
 * <li>v1.0.0 (2015-08-14) - Version Inicial.</li>
 * </ul>
 */
public class NtpClient {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHQ";

    // Resultados del update NTP
    public static final int OK = 0;  // Uptadte OK
    public static final int TIMEOUT = -1;  // Uptadte Time Out
    public static final int UNKNOWNHOST = -2;  // Uptadte Unknown Host
    public static final int NULLHOST = -3;  // Uptadte Host is NULL
    public static final int SOCKETEXCEPTION = -4;  // Uptadte Socket Exception
    public static final int IOEXCEPTION = -5;  // Uptadte IO Exception
    public static final int UNKNOWNERROR = -6;  // Uptadte Unknown Error

    // Pool de servidores NTP por defecto
    private static final String DEFNTPPOOLSERV = "pool.ntp.org";

    // Pool de servidores NTP por defecto
    private static final int SOCKETTIMEOUT = 2000;  // 2 sec

    // Servidor NTP a utlizar
    private String ServerName;
    // Parametros calculado por medio del NtpMessage
    private double destinationTimestamp;
    private double roundTripDelay;
    private double localClockOffset;

    // Manejador de Bitacoras
    private LoggerFormatter BTLogF;

    /**
     * Constructor por defecto; utiliza el pool de servidores 'pool.ntp.org'
     * 
     * @param vLog Manejador de Bitacoras
     */
    public NtpClient(LoggerManager vLog) {
        this(DEFNTPPOOLSERV, vLog);
    }

    /**
     * Constructor con inicializacion del servidor ntp
     *
     * @param Server Servidor NTP al cual consultar
     * @param vLog Manejador de Bitacoras
     */
    public NtpClient(String Server, LoggerManager vLog) {
        this.BTLogF = LoggerFormatter.getInstance(vLog);
        this.ServerName = Server;
        this.destinationTimestamp = 0;
        this.roundTripDelay = 0;
        this.localClockOffset = 0;
    }

    /**
     * Retorna el nombre del servidor NTP a utilizar
     *
     * @return servidor NTP a utilizar
     */
    public String getServerName() {
        return ServerName;
    }

    /**
     * Establece el nombre del servidor NTP a utlizar
     *
     * @param ServerName Nombre del servidor a utilizar
     */
    public void setServerName(String ServerName) {
        this.ServerName = ServerName;
    }

    /**
     * Realiza la actualizacion de los valores respecto al servidor NTP
     *
     * @return Return the update operation
     * result(OK,TIMEOUT,UNKNOWNHOST,NULLHOST,SOCKETEXCEPTION,IOEXCEPTION,UNKNOWNERROR).
     */
    public int Update() {
        int Result;
        DatagramSocket socket;
        InetAddress address;
        byte[] buf;
        DatagramPacket packet;
        NtpMessage msg;

        Result = UNKNOWNERROR;
        if (ServerName != null) {
            socket = null;
            try {
                // Send request
                socket = new DatagramSocket();
                socket.setSoTimeout(SOCKETTIMEOUT);
                address = InetAddress.getByName(ServerName);
                buf = new NtpMessage().toByteArray();
                packet = new DatagramPacket(buf, buf.length, address, 123);

                // Set the transmit timestamp *just* before sending the packet
                // ToDo: Does this actually improve performance or not?
                NtpMessage.encodeTimestamp(packet.getData(), 40, (System.currentTimeMillis() / 1000.0) + 2208988800.0);
                socket.send(packet);

                // Get response
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // Immediately record the incoming timestamp
                destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

                // Process response
                msg = new NtpMessage(packet.getData());

                // Corrected, according to RFC2030 errata
                roundTripDelay = (destinationTimestamp - msg.originateTimestamp) - (msg.transmitTimestamp - msg.receiveTimestamp);
                localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;
                Result = OK;
            } catch (SocketTimeoutException ex) {
                Result = TIMEOUT;
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_WARNING, false, ex, "NTP Connection TimeOut(" + ex.getMessage() + ")", NtpClient.CLASSID, "000");
            } catch (UnknownHostException ex) {
                Result = UNKNOWNHOST;
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "NTP Unknown Host (" + ex.getMessage() + ")", NtpClient.CLASSID, "001");
            } catch (SocketException ex) {
                Result = SOCKETEXCEPTION;
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "NTP Socket Exception (" + ex.getMessage() + ")", NtpClient.CLASSID, "002");
            } catch (IOException ex) {
                Result = IOEXCEPTION;
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, "NTP IO Exception (" + ex.getMessage() + ")", NtpClient.CLASSID, "003");
            } finally {
                // Realiza el cierre del socket
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                // Por seguridad elimina la referencia a estas valirable
                address = null;
                buf = null;
                packet = null;
                msg = null;
            }
        } else {
            Result = NULLHOST;
            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, null, "NTP server is NULL", NtpClient.CLASSID, "004");
        }
        return Result;
    }

    /**
     * Retorna el offset del reloj local respecto al del servidor NTP en
     * miliseguntos
     *
     * @return Offset en milisegundos del reloj local respecto al servidor
     */
    public long getLocalOffset() {
        return (long) (localClockOffset * 1000);
    }

    /**
     * Retorna el EPOCH timestamp actual corregido respecto al servidor NTP en
     * milisegundos
     *
     * @return Corrected current EPOCH timestamp
     */
    public long getCurrentCorrectedTimeMillis() {
        return (System.currentTimeMillis() + this.getLocalOffset());
    }

    /**
     * Retorna el retrazo de ida y vuelta con el servidor NTP
     *
     * @return Round Trip Delay en millisegundos
     */
    public long getRoundTripDelay() {
        return (long) (roundTripDelay * 1000);
    }

    /**
     * Retorna el timeStamp del servidor detino
     *
     * @return Destination TimeStamp
     */
    public long getDestinationTimeStamp() {
        return (long) destinationTimestamp;
    }
    
    /**
     * Retorna el identificador de la Clase
     * 
     * @return Identificador de la clase
     */
    public static String getCLASSID() {
        return CLASSID;
    }
}
