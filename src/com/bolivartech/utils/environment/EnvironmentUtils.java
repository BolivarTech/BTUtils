package com.bolivartech.utils.environment;

import com.bolivartech.utils.array.ArrayUtils;
import com.bolivartech.utils.converters.Base64;
import com.bolivartech.utils.converters.Converter;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.files.FileManager;
import com.bolivartech.utils.log.LoggerFormatter;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;

/**
 * <p>
 * Copyright 2014 BolivarTech INC.</p>
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>
 * This Class is the BolivarTech's util for System properties verification and
 * Environment Dependant Data.</p>
 *
 * <ul>
 * <li>Class ID: "35DGFH0"</li>
 * <li>Loc: 000-038</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2014, April 28, 2016.
 * @version 2.2.0
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v2.2.0 (2016-04-04) Se agrego el metodo getVolumeSerialNumber que recupera
 * el Volume Serial Nomber de una unidad.</li>
 * <li>v2.1.1 (2016-04-04) Se agrego el metodo recoverSalt que recupera la salt
 * de un archivo o genera una nueva y la almacena en el archivo</li>
 * <li>v2.1.0 (2016-03-28) Se agrego el metodo randomSleep para hacer dormir el
 * proceso de forma aleatoria</li>
 * <li>v2.0.1 (2016-03-25) Se implemento el uso del codigo de ubicacion
 * unico.</li>
 * <li>v2.0.0 (2015-08-14) Se agrego el soporte para el Ping y verificar que hay
 * conexion con internet.</li>
 * <li>v1.0.0 (2014-10-02) Version Inicial.</li>
 * </ul>
 */
public class EnvironmentUtils {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFH0";

    // Sitio para probar la existencia de una conexion con internet
    private static final String TESTHOST = "google.com";

    private static final String SALTFILENAME = "btenvrm.slt";

    // Errores del entorno
    public static final int ERROR_UNKNOWNOS = -1;
    public static final int ERROR_IO = -2;
    public static final int ERROR_INTERRUPTED = -3;
    public static final int ERROR_BADINPUT = -4;
    // No puede encontrar el serial del volumen
    public static final int ERROR_CANTFINDVOLUMESERIAL = -1;

    /**
     * Constructor Privado
     */
    private EnvironmentUtils() {
        super();
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
     * Varifica si la version del JRE es mayor o igual a la version especificada
     * en JREVersion.
     *
     * JREVersion debe tener el formato Ver.Rev.Comp_Update Por ejemplo para la
     * verison 1.7.0_40 debe ser: String JREVersion = "1.7.0_40";
     *
     * @param JREVersion Version minima de JRE a verificar.
     * @return TRUE si lo es y FALSE si no
     */
    public static boolean CheckJavaVersion(String JREVersion) {
        boolean Salida = false;
        StringBuffer Version;
        int Ver, Rev, Comp, Update;
        int rVer, rRev, rComp, rUpdate;

        // Recupera la version de la JRE
        Version = new StringBuffer(System.getProperty("java.version"));
        Rev = Version.indexOf(".");
        Ver = Integer.valueOf(Version.substring(0, Rev));
        Rev++;
        Comp = Version.indexOf(".", Rev);
        Rev = Integer.valueOf(Version.substring(Rev, Comp));
        Comp++;
        Update = Version.indexOf("_", Comp);
        if (Update != -1) {
            Comp = Integer.valueOf(Version.substring(Comp, Update));
            Update++;
            Update = Integer.valueOf(Version.substring(Update));
        } else {
            Comp = Integer.valueOf(Version.substring(Comp, Version.length()));
            Update = 0;
        }
        // Recupera la version minima requerida
        Version = new StringBuffer(JREVersion);
        rRev = Version.indexOf(".");
        rVer = Integer.valueOf(Version.substring(0, rRev));
        rRev++;
        rComp = Version.indexOf(".", rRev);
        rRev = Integer.valueOf(Version.substring(rRev, rComp));
        rComp++;
        rUpdate = Version.indexOf("_", rComp);
        if (rUpdate != -1) {
            rComp = Integer.valueOf(Version.substring(rComp, rUpdate));
            rUpdate++;
            rUpdate = Integer.valueOf(Version.substring(rUpdate));
        } else {
            rComp = Integer.valueOf(Version.substring(rComp, Version.length()));
            rUpdate = 0;
        }
        if (Ver > rVer) {
            Salida = true;
        } else if ((Ver == rVer) && (Rev > rRev)) {
            Salida = true;
        } else if ((Ver == rVer) && (Rev == rRev) && (Comp > rComp)) {
            Salida = true;
        } else if ((Ver == rVer) && (Rev == rRev) && (Comp == rComp) && (Update > rUpdate)) {
            Salida = true;
        } else if ((Ver == rVer) && (Rev == rRev) && (Comp == rComp) && (Update == rUpdate)) {
            Salida = true;
        }
        return Salida;
    }

    /**
     * Retorna el tipo de encoding usado por el OS, por ejemplo utf-8, utf-16,
     * cp1252, us_ascii, iso_8859_1, etc.
     *
     * @return Tipo de encoding del OS
     */
    public static String getEncoding() {
        return System.getProperty("file.encoding").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Retorna el Sistema Operativo (OS) en el cual esta corriendo la JRE.
     *
     * @return El nombre del OS donde esta ejecutandose la JRE
     */
    public static String getOS() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Retorna la arquitectura en el cual esta corriendo la JRE.
     *
     * @return El nombre de la arquitectura donde esta ejecutandose la JRE
     */
    public static String getArch() {
        return System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
    }

    /**
     * Retorna la ruta absoluta de donde se esta ejecutando una clase java
     *
     * @param classToUse Clase a verificar su ruta
     * @return Ruta absoluta a la clase
     */
    public static String getPathToJarfileDir(Class classToUse) {
        URL url;
        String extURL;      //  url.toExternalForm();
        File F;

        // get an url
        try {
            url = classToUse.getProtectionDomain().getCodeSource().getLocation();
            // url is in one of two forms
            //        ./build/classes/   NetBeans test
            //        jardir/JarName.jar  froma jar
        } catch (SecurityException ex) {
            url = classToUse.getResource(classToUse.getSimpleName() + ".class");
            // url is in one of two forms, both ending "/com/physpics/tools/ui/PropNode.class"
            //          file:/U:/Fred/java/Tools/UI/build/classes
            //          jar:file:/U:/Fred/java/Tools/UI/dist/UI.jar!
        }
        // convert to external form
        extURL = url.toExternalForm();
        // prune for various cases
        if (extURL.endsWith(".jar")) // from getCodeSource
        {
            extURL = extURL.substring(0, extURL.lastIndexOf("/"));
        } else {  // from getResource
            String suffix = "/" + (classToUse.getName()).replace(".", "/") + ".class";
            extURL = extURL.replace(suffix, "");
            if (extURL.startsWith("jar:") && extURL.endsWith(".jar!")) {
                extURL = extURL.substring(4, extURL.lastIndexOf("/"));
            }
        }
        // convert back to url
        try {
            url = new URL(extURL);
        } catch (MalformedURLException mux) {
            // leave url unchanged; probably does not happen
        }
        // convert url to File
        try {
            F = new File(url.toURI());
        } catch (URISyntaxException ex) {
            F = new File(url.getPath());
        }
        return F.getAbsolutePath();
    }

    /**
     * Retorna el Motherboard Serial Number o NULL si no logro recuperarlo.
     *
     * NOTA: Windows, need Admin rights. Linux(i386,amd64), need dmidecode
     * installed in the system. Linux(arm), need grep installed in the system.
     * Set {USER} ALL = (root) NOPASSWD: {PATH}/dmidecode -s
     * system-serial-number in sudo visudo -f /etc/sudoers.d/dmidecode and full
     * access to /tmp directory
     *
     * @param vLog Manejador de Bitacoras
     * @return Motherboard Serial Number or NULL if can't recover it.
     */
    public static String getMotherboardSN(LoggerManager vLog) {
        String result = null;
        FileManager file;
        Process proc;
        BufferedReader input;
        String line;
        String vbs;
        String OS;
        String Arch;
        MersenneTwisterPlus Random;
        byte[] BTemp;
        int i, j;
        LoggerFormatter BTLogF;

        BTLogF = LoggerFormatter.getInstance(vLog);
        Random = new MersenneTwisterPlus();
        OS = EnvironmentUtils.getOS();
        Arch = EnvironmentUtils.getArch();
        if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
            try {
                result = new String();
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "c:\\Windows\\Temp\\" + Converter.byte2StringHex(BTemp) + ".vbs";
                file = new FileManager(line);
                vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                        + "Set colItems = objWMIService.ExecQuery _ \n"
                        + "   (\"Select * from Win32_BaseBoard\") \n"
                        + "For Each objItem in colItems \n"
                        + "    Wscript.Echo objItem.SerialNumber \n"
                        + "    exit for  ' do the first cpu only! \n"
                        + "Next \n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("cscript //NoLogo " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                file.Wipe();
                result = result.trim();
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "000");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && ((Arch.toLowerCase(new Locale("en")).contains("i386")) || (Arch.toLowerCase(new Locale("en")).contains("amd64")))) {
            try {
                result = new String();
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "/tmp/" + Converter.byte2StringHex(BTemp) + ".sh";
                file = new FileManager(line);
                vbs = "#!/bin/sh\n"
                        + "sudo dmidecode -s system-serial-number\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                file.Wipe();
                result = result.trim();
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "001");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && (Arch.toLowerCase(new Locale("en")).contains("arm"))) {
            try {
                result = new String();
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "/tmp/" + Converter.byte2StringHex(BTemp) + ".sh";
                file = new FileManager(line);
                vbs = "#!/bin/sh\n"
                        + "grep Serial /proc/cpuinfo | cut -d \" \" -f 2\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                file.Wipe();
                result = result.trim();
                // Le hace trim al serial para solo dejar el valor significativo
                BTemp = Converter.StringHex2bytes(result);
                for (j = 0; ((j < BTemp.length) && (BTemp[j] == 0x00)); j++) {
                }
                if (j < BTemp.length) {
                    for (i = 0; j < BTemp.length; i++) {
                        BTemp[i] = BTemp[j];
                        j++;
                    }
                    BTemp = (byte[]) ArrayUtils.resizeArray(BTemp, i);
                    result = Converter.byte2StringHex(BTemp);
                } else {
                    result = null;
                }
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "002");
            }
        }
        return result;
    }

    /**
     * Retorna el Volume Serial Number o UUID de la unidad o device especificados por 'Path'
     * 
     * @param Path unidad o dispositivo del cual recuperar el UUID 
     * @return Volume Serial Number o UUID de la unidad
     * @throws UtilsException 
     */
    public static String getVolumeSerialNumber(String Path) throws UtilsException {
        String vsn = null;
        String OS;
        String Arch;
        StringBuffer result = null;
        Process proc;
        BufferedReader input;
        String line;
        int bp, ep;
        FileManager file;

        OS = EnvironmentUtils.getOS();
        Arch = EnvironmentUtils.getArch();
        if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
            try {
                result = new StringBuffer();
                proc = Runtime.getRuntime().exec("cmd /c vol " + Path);
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result.append(line + "/n");
                }
                bp = result.indexOf("/n");
                if (bp < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "003");
                }
                bp++;
                ep = result.indexOf("/n", bp);
                if (ep < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "004");
                }
                line = result.substring(bp, ep);
                bp = line.lastIndexOf(" ");
                if (bp < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "005");
                }
                bp++;
                vsn = line.substring(bp);
            } catch (IOException ex) {
                throw new UtilsException(ex.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "006");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && ((Arch.toLowerCase(new Locale("en")).contains("i386")) || (Arch.toLowerCase(new Locale("en")).contains("amd64")))) {
            try {
                result = new StringBuffer();
                line = "/tmp/" + FileManager.genRandomFileName();
                file = new FileManager(line);
                line = "#!/bin/sh\n"
                        + "sudo blkid\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(line.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result.append(line + "/n");
                }
                file.Wipe();
                bp = result.indexOf(Path);
                if (bp < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "007");
                }
                bp++;
                bp = result.indexOf("UUID=", bp);
                if (bp < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "008");
                }
                bp += 6;
                ep = result.indexOf("\"", bp);
                if (ep < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "009");
                }
                vsn = result.substring(bp, ep);
            } catch (IOException ex) {
                throw new UtilsException(ex.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "010");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && (Arch.toLowerCase(new Locale("en")).contains("arm"))) {
            try {
                result = new StringBuffer();
                line = "/tmp/" + FileManager.genRandomFileName();
                file = new FileManager(line);
                line = "#!/bin/sh\n"
                        + "sudo blkid\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(line.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result.append(line + "/n");
                }
                file.Wipe();
                bp = result.indexOf(Path);
                if (bp < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "011");
                }
                bp++;
                bp = result.indexOf("UUID=", bp);
                if (bp < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "012");
                }
                bp += 6;
                ep = result.indexOf("\"", bp);
                if (ep < 0) {
                    throw new UtilsException("Can't find Volume Serial Number", EnvironmentUtils.ERROR_CANTFINDVOLUMESERIAL, EnvironmentUtils.CLASSID + "013");
                }
                vsn = result.substring(bp, ep);
            } catch (IOException ex) {
                throw new UtilsException(ex.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "014");
            }
        }
        return vsn;
    }

    /**
     * Retrona el Serial del Volument del HDD especificado en Hexadecimal o NULL
     * si no pudo recuperarlo.
     *
     * NOTA: Windows, need Admin rights. Linux(i386,amd64), need udevadm, grep
     * and awk installed in the system. Linux(arm), need cat installed in the
     * system. Linux full access to /tmp directory
     *
     * @param drive Drive, at Windows is only the letter, exp: "C" or "D"
     * @param vLog Manejador de Bitacoras
     * @return Volume Serial Number of the HDD specified or NULL.
     */
    public static String getHDDSerialNumber(String drive, LoggerManager vLog) {
        String result = null;
        FileManager file;
        int[] Serial;
        String OS;
        MersenneTwisterPlus Random;
        byte[] BTemp;
        String vbs;
        String line;
        Process proc;
        String Arch;
        BufferedReader input;
        int i, j;
        LoggerFormatter BTLogF;

        BTLogF = LoggerFormatter.getInstance(vLog);
        Random = new MersenneTwisterPlus();
        OS = EnvironmentUtils.getOS();
        Arch = EnvironmentUtils.getArch();
        if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
            try {
                result = new String();
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "c:\\Windows\\Temp\\" + Converter.byte2StringHex(BTemp) + ".vbs";
                file = new FileManager(line);
                if (drive.length() > 1) {
                    drive = drive.substring(0, 1);
                }
                vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                        + "Set colDrives = objFSO.Drives\n"
                        + "Set objDrive = colDrives.item(\"" + drive.toUpperCase(new Locale("en")) + "\")\n"
                        + "Wscript.Echo objDrive.SerialNumber";  // Other properties : objDrive.AvailableSpace/DriveType/FileSystem/IsReady
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("cscript //NoLogo " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                file.Wipe();
                Serial = new int[1];
                Serial[0] = Integer.parseInt(result, 10);
                BTemp = Converter.int2byte(Serial);
                result = Converter.byte2StringHex(BTemp);
                result = result.trim();
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "015");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && ((Arch.toLowerCase(new Locale("en")).contains("i386")) || (Arch.toLowerCase(new Locale("en")).contains("amd64")))) {
            try {
                result = new String();
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "/tmp/" + Converter.byte2StringHex(BTemp) + ".sh";
                file = new FileManager(line);
                vbs = "#!/bin/sh\n"
                        + "udevadm info --query=property --name=" + drive.toLowerCase(new Locale("en")) + " | grep ID_SERIAL= | awk -F '=' '{ print $2}'\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                file.Wipe();
                result = result.trim();
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "016");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && (Arch.toLowerCase(new Locale("en")).contains("arm"))) {
            try {
                result = new String();
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "/tmp/" + Converter.byte2StringHex(BTemp) + ".sh";
                file = new FileManager(line);
                vbs = "#!/bin/sh\n"
                        + "cat /sys/block/mmcblk0/device/cid\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                file.Wipe();
                result = result.trim();
                // Le hace trim al serial para solo dejar el valor significativo
                BTemp = Converter.StringHex2bytes(result);
                for (j = 0; ((j < BTemp.length) && (BTemp[j] == 0x00)); j++) {
                }
                if (j < BTemp.length) {
                    for (i = 0; j < BTemp.length; i++) {
                        BTemp[i] = BTemp[j];
                        j++;
                    }
                    BTemp = (byte[]) ArrayUtils.resizeArray(BTemp, i);
                    result = Converter.byte2StringHex(BTemp);
                } else {
                    result = null;
                }
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "017");
            }
        }
        return result;
    }

    /**
     * Retorna los HDDs presentes en el sistema
     *
     * NOTA: Windows, need Admin rights. Linux(i386,amd64), need grep, awk and
     * sort installed in the system Linux(arm), need grep, awk and sort
     * installed in the system Linux full access to /tmp directory
     *
     * @param vLog Manejador de Bitacoras
     * @return lista de HDDs presentes en el sistema
     */
    public static String[] getHDDs(LoggerManager vLog) {
        String[] Salida = null;
        String[] result = null;
        String OS;
        String Arch;
        MersenneTwisterPlus Random;
        byte[] BTemp;
        String vbs;
        String line;
        Process proc;
        BufferedReader input;
        int i;
        FileManager file;
        LoggerFormatter BTLogF;

        BTLogF = LoggerFormatter.getInstance(vLog);
        Random = new MersenneTwisterPlus();
        OS = EnvironmentUtils.getOS();
        Arch = EnvironmentUtils.getArch();
        if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
            try {
                result = new String[100];
                for (i = 0; i < result.length; i++) {
                    result[i] = null;
                }
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "c:\\Windows\\Temp\\" + Converter.byte2StringHex(BTemp) + ".vbs";
                file = new FileManager(line);
                vbs = "Dim goFS      : Set goFS      = CreateObject( \"Scripting.FileSystemObject\" )\n"
                        + "Dim dicDTypes : Set dicDTypes = buildDicMKV( vbTextCompare, Split( \"0 1 2 3 4 5\" ), Split( \"Unknown Removable Fixed Network CD-ROM RAM-Disk\" ))\n"
                        + "Dim dicDrives : Set dicDrives = CreateObject( \"Scripting.Dictionary\" )\n"
                        + "Dim oWSH      : Set oWSH      = CreateObject( \"WScript.Shell\" )\n"
                        + "Dim oDrive\n"
                        + "For Each oDrive In goFS.Drives\n"
                        + "If \"Fixed\" = dicDTypes( CStr( oDrive.DriveType ) ) And sSDLetter <> oDrive.DriveLetter Then\n"
                        + "Set dicDrives( oDrive.DriveLetter ) = oDrive\n"
                        + "End If\n"
                        + "Next\n"
                        + "Dim sDrive\n"
                        + "For Each sDrive In dicDrives.Keys\n"
                        + "Set oDrive = dicDrives( sDrive )\n"
                        + "WScript.Echo oDrive.DriveLetter\n"
                        + "Next\n"
                        + "\n"
                        + "Function buildDicMKV( vbCompMode, aKeys, aValues )\n"
                        + "Set buildDicMKV = CreateObject( \"Scripting.Dictionary\" )\n"
                        + "buildDicMKV.CompareMode = vbCompMode\n"
                        + "Dim nIdx\n"
                        + "For nIdx = 0 To UBound( aKeys )\n"
                        + "buildDicMKV.Add aKeys( nIdx ), aValues( nIdx )\n"
                        + "Next\n"
                        + "End Function\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("cscript //NoLogo " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                i = 0;
                while (((line = input.readLine()) != null) && (i < result.length)) {
                    result[i] = line;
                    i++;
                }
                file.Wipe();
                if (i > 0) {
                    Salida = (String[]) ArrayUtils.arrayTrim(result);
                }
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "018");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && ((Arch.toLowerCase(new Locale("en")).contains("i386")) || (Arch.toLowerCase(new Locale("en")).contains("amd64")))) {
            try {
                result = new String[100];
                for (i = 0; i < result.length; i++) {
                    result[i] = null;
                }
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "/tmp/" + Converter.byte2StringHex(BTemp) + ".sh";
                file = new FileManager(line);
                vbs = "#!/bin/sh\n"
                        + "df -h | grep /dev/ | awk -F '/' '{ print substr ($3, 0, 3)}' | sort -u\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                i = 0;
                while (((line = input.readLine()) != null) && (i < result.length)) {
                    result[i] = line;
                    i++;
                }
                file.Wipe();
                if (i > 0) {
                    Salida = (String[]) ArrayUtils.arrayTrim(result);
                }
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "019");
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux")) && (Arch.toLowerCase(new Locale("en")).contains("arm"))) {
            try {
                result = new String[100];
                for (i = 0; i < result.length; i++) {
                    result[i] = null;
                }
                BTemp = new byte[20];
                Random.nextBytes(BTemp);
                line = "/tmp/" + Converter.byte2StringHex(BTemp) + ".sh";
                file = new FileManager(line);
                vbs = "#!/bin/sh\n"
                        + "df -h | grep /dev/ | grep -v root | awk -F '/' '{ print substr ($3, 0, 8)}' | sort -u\n"
                        + "\n";
                file.Open(FileManager.WRITE, false);
                file.Write(vbs.getBytes("UTF-8"));
                file.Close();
                proc = Runtime.getRuntime().exec("sh " + file.getAbsoluteFilePath());
                input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                i = 0;
                while (((line = input.readLine()) != null) && (i < result.length)) {
                    result[i] = line;
                    i++;
                }
                file.Wipe();
                if (i > 0) {
                    Salida = (String[]) ArrayUtils.arrayTrim(result);
                }
            } catch (UtilsException | IOException e) {
                BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, e, null, EnvironmentUtils.CLASSID, "020");
            }
        }
        return Salida;
    }

    /**
     * Retorna las MACs del equipo o NULL si no logro recuperarlas
     *
     * @param vLog
     * @return MACs de las interfaces del equipo o NULL si no logro recuperarlo
     */
    public static String[] getMACs(LoggerManager vLog) {
        String[] MACs = null;
        byte[] MAC;
        StringBuilder sb;
        int NumMACs;
        LoggerFormatter BTLogF;

        BTLogF = LoggerFormatter.getInstance(vLog);
        try {
            //System.out.println("Current IP address : " + InetAddress.getLocalHost().getHostAddress());
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            if (networks.hasMoreElements()) {
                MACs = new String[100];
                for (NumMACs = 0; NumMACs < MACs.length; NumMACs++) {
                    MACs[NumMACs] = null;
                }
            }
            NumMACs = 0;
            while (networks.hasMoreElements() && (NumMACs < MACs.length)) {
                NetworkInterface network = networks.nextElement();
                if ((!network.isVirtual()) && (!network.isLoopback())) {
                    MAC = network.getHardwareAddress();
                    if ((MAC != null) && (MAC.length == 6)) {
                        sb = new StringBuilder();
                        for (int i = 0; i < MAC.length; i++) {
                            sb.append(String.format("%02X%s", MAC[i], (i < MAC.length - 1) ? "-" : ""));
                        }
                        MACs[NumMACs] = sb.toString();
                        NumMACs++;
                        //Bound InetAddress for interface
                        /*Enumeration<InetAddress> addresses = network.getInetAddresses();
                         while (addresses.hasMoreElements()) {
                         InetAddress address = addresses.nextElement();
                         System.out.println("\tBound to:" + address.getHostAddress());
                         }*/
                    }
                }
            }
            if (NumMACs < MACs.length) {
                MACs = (String[]) ArrayUtils.arrayTrim(MACs);
            }
        } catch (SocketException ex) {
            BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, EnvironmentUtils.CLASSID, "021");
        }
        return MACs;
    }

    /**
     * Retorna el BolivarTech Hardware ID, el cual es una mezcla del serial del
     * Motherboard, el serial del volumen del OS y de las MACs en el sistema.
     *
     * @param vLog Manejador de Bitacoras
     * @return BolivarTech Hardware ID
     */
    public static String getBTHwID(LoggerManager vLog) {
        String Salida = null;
        StringBuffer Mexclador = null;
        String[] MACs;
        String[] HDDs = null;
        int i, Pos, Group;
        String MBSerial;
        String HDDSerial = null;
        String OS;

        MACs = getMACs(vLog);
        MBSerial = getMotherboardSN(vLog);
        // Recupera los seriales de los discos duros
        OS = EnvironmentUtils.getOS();
        if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
            HDDs = getHDDs(vLog);
            if (HDDs != null) {
                HDDSerial = "";
                for (i = 0; i < HDDs.length; i++) {
                    HDDSerial += getHDDSerialNumber(HDDs[i], vLog);
                }
            } else {
                HDDSerial = getHDDSerialNumber("C", vLog);
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux"))) {
            HDDs = getHDDs(vLog);
            if (HDDs != null) {
                HDDSerial = "";
                for (i = 0; i < HDDs.length; i++) {
                    HDDSerial += getHDDSerialNumber(HDDs[i], vLog);
                }
            } else {
                HDDSerial = getHDDSerialNumber("sda", vLog);
            }
        }
        if (MBSerial != null) {
            Mexclador = new StringBuffer();
            for (Pos = 0; Pos < MBSerial.length(); Pos++) {
                Group = (((int) MBSerial.charAt(Pos)) + Pos + Mexclador.length()) % 3;
                switch (Group) {
                    case 0:
                        Mexclador.append((char) (((((int) MBSerial.charAt(Pos)) + Pos + Mexclador.length()) % 10) + 48));
                        break;
                    case 1:
                        Mexclador.append((char) (((((int) MBSerial.charAt(Pos)) + Pos + Mexclador.length()) % 25) + 97));
                        break;
                    case 2:
                        Mexclador.append((char) (((((int) MBSerial.charAt(Pos)) + Pos + Mexclador.length()) % 25) + 65));
                        break;
                    default:
                        Mexclador.append((char) (((((int) MBSerial.charAt(Pos)) + Pos + Mexclador.length()) % 25) + 65));
                }
            }
        }
        if (HDDSerial != null) {
            if (Mexclador == null) {
                Mexclador = new StringBuffer();
            }
            for (Pos = 0; Pos < HDDSerial.length(); Pos++) {
                Group = (((int) HDDSerial.charAt(Pos)) + Pos + Mexclador.length()) % 3;
                switch (Group) {
                    case 0:
                        Mexclador.append((char) (((((int) HDDSerial.charAt(Pos)) + Pos + Mexclador.length()) % 10) + 48));
                        break;
                    case 1:
                        Mexclador.append((char) (((((int) HDDSerial.charAt(Pos)) + Pos + Mexclador.length()) % 25) + 97));
                        break;
                    case 2:
                        Mexclador.append((char) (((((int) HDDSerial.charAt(Pos)) + Pos + Mexclador.length()) % 25) + 65));
                        break;
                    default:
                        Mexclador.append((char) (((((int) HDDSerial.charAt(Pos)) + Pos + Mexclador.length()) % 25) + 65));
                }
            }
        }
        if (MACs != null) {
            // Mexcla las MACs
            if (Mexclador == null) {
                Mexclador = new StringBuffer();
            }
            for (i = 0; i < MACs.length; i++) {
                for (Pos = 0; Pos < MACs[i].length(); Pos++) {
                    if (MACs[i].charAt(Pos) == '-') {
                        Mexclador.append((char) (((Pos + i + Mexclador.length()) % 25) + 65));
                    } else {
                        Group = (((int) MACs[i].charAt(Pos)) + i + Pos + Mexclador.length()) % 3;
                        switch (Group) {
                            case 0:
                                Mexclador.append((char) (((((int) MACs[i].charAt(Pos)) + i + Pos + Mexclador.length()) % 10) + 48));
                                break;
                            case 1:
                                Mexclador.append((char) (((((int) MACs[i].charAt(Pos)) + i + Pos + Mexclador.length()) % 25) + 97));
                                break;
                            case 2:
                                Mexclador.append((char) (((((int) MACs[i].charAt(Pos)) + i + Pos + Mexclador.length()) % 25) + 65));
                                break;
                            default:
                                Mexclador.append((char) (((((int) MACs[i].charAt(Pos)) + i + Pos + Mexclador.length()) % 25) + 65));
                        }
                    }
                }
            }
        }
        if (Mexclador != null) {
            Salida = Mexclador.toString();
        }
        return Salida;
    }

    /**
     * Retorna un ID unico aleatorio que puede ser usado como SALT en algoritmos
     * de codificacion.
     *
     * NOTA: Cada vez que se ejecuta este metodo se genera una SALT nueva
     *
     * @param vLog Manejador de Bitacoras
     * @return Valor SALT
     * @throws UtilsException
     */
    public static String genSALT(LoggerManager vLog) throws UtilsException {
        String Result = null;
        StringBuffer Salida = null;
        String VHwID = null;
        String randomID = null;
        byte[] binRandomID;
        MersenneTwisterPlus Random;
        int Salto, Pos, Pos2, Isrt;

        Random = new MersenneTwisterPlus();
        VHwID = getBTHwID(vLog);
        binRandomID = new byte[VHwID.length()];
        Random.nextBytes(binRandomID);
        try {
            randomID = Base64.encodeBytes(binRandomID, Base64.URL_SAFE);
            if ((randomID != null) && (VHwID != null)) {
                // Mexcla el RandomID con el VHwID y el Packet Name
                Salida = new StringBuffer();
                if (randomID.length() > VHwID.length()) {
                    Salto = randomID.length() / VHwID.length();
                    if (Salto == 0) {
                        Salto = 1;
                    }
                    Pos2 = 0;
                    for (Pos = 0; Pos < randomID.length(); Pos++) {
                        if (Pos2 < VHwID.length()) {
                            Isrt = Pos % Salto;
                            if (Isrt == 0) {
                                Salida.append(VHwID.charAt(Pos2));
                                Pos2++;
                            }
                        }
                        Salida.append(randomID.charAt(Pos));
                    }
                } else {
                    Salto = VHwID.length() / randomID.length();
                    if (Salto == 0) {
                        Salto = 1;
                    }
                    Pos2 = 0;
                    for (Pos = 0; Pos < VHwID.length(); Pos++) {
                        if (Pos2 < randomID.length()) {
                            Isrt = Pos % Salto;
                            if (Isrt == 0) {
                                Salida.append(randomID.charAt(Pos2));
                                Pos2++;
                            }
                        }
                        Salida.append(VHwID.charAt(Pos));
                    }
                }
            } else {
                Salida = new StringBuffer(randomID);
            }
        } catch (IOException ex) {
            throw new UtilsException(ex.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "022");
        }
        if (Salida != null) {
            Result = Salida.toString();
        }
        return Result;
    }

    /**
     * Recupera la salt del entorno o genera una nueva si no existe
     *
     * @param saltPath Ruta donde crear o recuperar el archivo de salt
     * @param vLog Manejador de Bitacoras
     * @return Salt generada o recuperada
     * @throws UtilsException
     */
    public static String recoverSalt(String saltPath, LoggerManager vLog) throws UtilsException {
        FileManager SALTFILE;
        byte[] Data, Buffer;
        //StringBuffer SBuf;
        int NumBytesRead, Pos;
        String lSALT;

        // Elimina el ultimo caracter si es el separador de archivo
        if (saltPath.endsWith(System.getProperty("file.separator", "/"))) {
            saltPath = saltPath.substring(0, saltPath.length() - 1);
        }
        SALTFILE = new FileManager(saltPath + System.getProperty("file.separator", "/") + EnvironmentUtils.SALTFILENAME);
        lSALT = null;
        try {
            if ((SALTFILE.Exists()) && (SALTFILE.isFile())) {
                // Recupera la SALT antes creada
                Data = new byte[(int) SALTFILE.getFileLength()];
                Buffer = new byte[16384];
                SALTFILE.Open(FileManager.READ, false);
                Pos = 0;
                while ((NumBytesRead = (int) SALTFILE.Read(Buffer)) > 0) {
                    ArrayUtils.arrayCopy(Buffer, 0, Data, Pos, NumBytesRead);
                    Pos += NumBytesRead;
                }
                if (Pos < Data.length) {
                    Data = (byte[]) ArrayUtils.resizeArray(Data, Pos);
                }
                lSALT = new String(Data, StandardCharsets.UTF_8);
            }
            if (lSALT == null) {
                // No existe archivo de salt o la salt vieja esta dañada
                lSALT = genSALT(vLog);
                Data = lSALT.getBytes(StandardCharsets.UTF_8);
                SALTFILE.Open(FileManager.WRITE, false);
                SALTFILE.Write(Data);
            }
        } finally {
            SALTFILE.Close();
        }
        return lSALT;
    }

    /**
     * Ping a host and return an int value of 0 or 1 or 2 0=success, 1=fail,
     * 2=error.
     *
     * Does not work in Android emulator and also delay by '1' second if host
     * not pingable In the Android emulator only ping to 127.0.0.1 works
     *
     * @param host in dotted IP address format
     * @return 0=success, 1=fail, 2=error
     * @throws UtilsException
     */
    public static int pingHost(String host) throws UtilsException {
        int exit;
        String OS;

        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OS = EnvironmentUtils.getOS();
        try {
            if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
                proc = runtime.exec("ping -n 4 " + host);
            } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux"))) {
                proc = runtime.exec("ping -c 4 " + host);
            } else {
                throw new UtilsException("Unknown OS", EnvironmentUtils.ERROR_UNKNOWNOS, EnvironmentUtils.CLASSID + "023");
            }
            proc.waitFor();
        } catch (IOException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "024");
        } catch (InterruptedException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_INTERRUPTED, EnvironmentUtils.CLASSID + "025");
        }
        exit = proc.exitValue();
        return exit;
    }

    /**
     * Ping a host and return an int value of 0 or 1 or 2 0=success, 1=fail,
     * 2=error
     *
     * Does not work in Android emulator and also delay by '1' second if host
     * not pingable In the Android emulator only ping to 127.0.0.1 works
     *
     * NOTE: In Windows the Interface MUST BE an IP Address
     *
     * @param host in dotted IP address format
     * @param Interface interface a ser usada para enviar el Ping
     * @return 0=success, 1=fail, 2=error
     * @throws UtilsException
     */
    public static int pingHost(String host, String Interface) throws UtilsException {
        int exit;
        String OS;

        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OS = EnvironmentUtils.getOS();
        try {
            if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
                proc = runtime.exec("ping -n 4 -S " + Interface + " " + host);
            } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux"))) {
                proc = runtime.exec("ping -c 4 -I " + Interface + " " + host);
            } else {
                throw new UtilsException("Unknown OS", EnvironmentUtils.ERROR_UNKNOWNOS, EnvironmentUtils.CLASSID + "026");
            }
            proc.waitFor();
        } catch (IOException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "027");
        } catch (InterruptedException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_INTERRUPTED, EnvironmentUtils.CLASSID + "028");
        }
        exit = proc.exitValue();
        return exit;
    }

    /**
     * Ejecuta un Ping y retorna un string con el resultado del ping
     *
     * @param host in dotted IP address format
     * @return El tiempo promedio de respuesta en msec
     * @throws UtilsException
     */
    public static String ping(String host) throws UtilsException {
        String line;
        StringBuffer echo;
        Runtime runtime;
        Process proc;
        int exit;
        String OS;

        exit = -1;
        line = "failed, exit = 1";
        echo = new StringBuffer();
        runtime = Runtime.getRuntime();
        OS = EnvironmentUtils.getOS();
        try {
            if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
                proc = runtime.exec("ping -n 4 " + host);
            } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux"))) {
                proc = runtime.exec("ping -c 4 " + host);
            } else {
                throw new UtilsException("Unknown OS", EnvironmentUtils.ERROR_UNKNOWNOS, EnvironmentUtils.CLASSID + "029");
            }
            proc.waitFor();
            exit = proc.exitValue();
            if (exit == 0) {
                InputStreamReader reader = new InputStreamReader(proc.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                line = "";
                while ((line = buffer.readLine()) != null) {
                    echo.append(line + "\n");
                }
                line = getPingStats(echo.toString());
            } else if (exit == 1) {
                line = "failed, exit = 1";
            } else {
                line = "error, exit = 2";
            }
        } catch (InterruptedException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_INTERRUPTED, EnvironmentUtils.CLASSID + "030");
        } catch (IOException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "031");
        }
        return line;
    }

    /**
     * Ejecuta un Ping y retorna un string con el resultado del ping
     *
     * @param host in dotted IP address format
     * @param Interface interface a ser usada para enviar el Ping
     * @return El tiempo promedio de respuesta en msec
     * @throws UtilsException
     */
    public static String ping(String host, String Interface) throws UtilsException {
        StringBuffer echo;
        Runtime runtime;
        Process proc;
        int exit;
        String line;
        String OS;

        exit = -1;
        line = "failed, exit = 1";
        echo = new StringBuffer();
        runtime = Runtime.getRuntime();
        OS = EnvironmentUtils.getOS();
        try {
            if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
                proc = runtime.exec("ping -n 4 -S " + Interface + " " + host);
            } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux"))) {
                proc = runtime.exec("ping -c 4 -I " + Interface + " " + host);
            } else {
                throw new UtilsException("Unknown OS", EnvironmentUtils.ERROR_UNKNOWNOS, EnvironmentUtils.CLASSID + "032");
            }
            proc.waitFor();
            exit = proc.exitValue();
            if (exit == 0) {
                InputStreamReader reader = new InputStreamReader(
                        proc.getInputStream());
                BufferedReader buffer = new BufferedReader(reader);
                line = "";
                while ((line = buffer.readLine()) != null) {
                    echo.append(line + "\n");
                }
                line = getPingStats(echo.toString());
            } else if (exit == 1) {
                line = "failed, exit = 1";
            } else {
                line = "error, exit = 2";
            }
        } catch (IOException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "033");
        } catch (InterruptedException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_INTERRUPTED, EnvironmentUtils.CLASSID + "034");
        }
        return line;
    }

    /**
     * getPingStats interprets the text result of a Windows or Linux ping
     * command
     *
     * Set pingError on error and return null
     *
     * http://en.wikipedia.org/wiki/Ping
     *
     * PING 127.0.0.1 (127.0.0.1) 56(84) bytes of data. 64 bytes from 127.0.0.1:
     * icmp_seq=1 ttl=64 time=0.251 ms 64 bytes from 127.0.0.1: icmp_seq=2
     * ttl=64 time=0.294 ms 64 bytes from 127.0.0.1: icmp_seq=3 ttl=64
     * time=0.295 ms 64 bytes from 127.0.0.1: icmp_seq=4 ttl=64 time=0.300 ms
     *
     * --- 127.0.0.1 ping statistics --- 4 packets transmitted, 4 received, 0%
     * packet loss, time 0ms rtt min/avg/max/mdev = 0.251/0.285/0.300/0.019 ms
     *
     * PING 192.168.0.2 (192.168.0.2) 56(84) bytes of data.
     *
     * --- 192.168.0.2 ping statistics --- 1 packets transmitted, 0 received,
     * 100% packet loss, time 0ms
     *
     * # ping 321321. ping: unknown host 321321.
     *
     * 1. Check if output contains 0% packet loss : Branch to success -> Get
     * stats 2. Check if output contains 100% packet loss : Branch to fail -> No
     * stats 3. Check if output contains 25% packet loss : Branch to partial
     * success -> Get stats 4. Check if output contains "unknown host"
     *
     * @param s Result from ping method
     * @throws UtilsException
     */
    private static String getPingStats(String s) throws UtilsException {
        int start;
        int end;
        String[] stats;
        String Salida;
        String OS;

        Salida = null;
        OS = EnvironmentUtils.getOS();
        if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("windows"))) {
            if (s.contains("100% loss")) {
                Salida = "100% packet loss";
            } else if (s.contains("0% loss")) {
                start = s.indexOf("Average =");
                end = s.indexOf("ms\n", start);
                Salida = s.substring(start + "Average = ".length(), end);
            } else if (s.contains("% loss")) {
                Salida = "partial packet loss";
            } else if (s.contains("could not find host")) {
                Salida = "unknown host";
            } else {
                Salida = "unknown error in getPingStats";
            }
        } else if ((OS != null) && (OS.toLowerCase(new Locale("en")).contains("linux"))) {
            if (s.contains("100% packet loss")) {
                Salida = "100% packet loss";
            } else if (s.contains("0% packet loss")) {
                start = s.indexOf("/mdev = ");
                end = s.indexOf(" ms\n", start);
                s = s.substring(start + 8, end);
                stats = s.split("/");
                Salida = stats[2];
            } else if (s.contains("% packet loss")) {
                Salida = "partial packet loss";
            } else if (s.contains("unknown host")) {
                Salida = "unknown host";
            } else {
                Salida = "unknown error in getPingStats";
            }
        } else {
            Salida = "Unknown OS";
            throw new UtilsException("Unknown OS", EnvironmentUtils.ERROR_UNKNOWNOS, EnvironmentUtils.CLASSID + "035");
        }
        return Salida;
    }

    /**
     * Verifica si hay una conexion de internet activa, retornando TRUE si
     * existe o FALSE si no
     *
     * @return true si exista una conexion activa o false si no
     * @throws UtilsException
     */
    public static boolean haveNetworkConnectionActive() throws UtilsException {
        boolean haveConnection = false;
        int i;

        try {
            i = pingHost(TESTHOST);
            if (i == 0) {
                haveConnection = true;
            }
        } catch (UtilsException e) {
            throw new UtilsException(e.getMessage(), EnvironmentUtils.ERROR_IO, EnvironmentUtils.CLASSID + "036");
        }
        return haveConnection;
    }

    /**
     * Hace dormir el proceso por un tiempo aleatorio comprendido entre 1 y
     * milisec
     *
     * @param milisec Maximo numero de milisegundos a dormir
     */
    public static void randomSleep(int milisec) throws UtilsException {
        MersenneTwisterPlus Random;

        if (milisec > 0) {
            Random = new MersenneTwisterPlus();
            try {
                Thread.sleep(1 + Random.nextInt(milisec - 1));
            } catch (InterruptedException ex) {
                throw new UtilsException(ex.getMessage(), EnvironmentUtils.ERROR_INTERRUPTED, EnvironmentUtils.CLASSID + "037");
            }
        } else {
            throw new UtilsException("Sleep time less than 0", EnvironmentUtils.ERROR_BADINPUT, EnvironmentUtils.CLASSID + "038");
        }
    }
}
