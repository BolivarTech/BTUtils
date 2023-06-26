package com.bolivartech.utils.files;

import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.sort.Sortable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 * <p>
 * Homepage: <a
 * href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the CuaimaCrypt's util for files IO management.
 *
 * Realiza el manejo de archivos
 * 
 * Class ID: "35DGFHA"
 * Loc: 000-044
 *
 * @author Julian Bolivar
 * @since 2007 - March 25, 2016.
 * @version 3.1.1
 *
 * Change Logs: 
 * v1.0.0 (2007-04-25): Version Inicial. 
 * v2.2.0 (2015-02-05): Se agrego la recuperacion de los tiempos de creacion, ultimo accesso y
 * modificacion del archivo. 
 * v2.3.0 (2015-05-19): En Wipe se enmascara el nombre original del archivo con un nombre aleatorio. Bug fixed on Rename method.
 * v2.4.0 (2015-11-27): En Wipe Bug solucionado en el Path. 
 * v2.5.0 (2015-11-28): Se agrego el metodo estatico genRandomFileName para generar nombres de
 * archivos aleatorios. 
 * v2.5.1 (2016-01-16): Se corrigio un Bug en los metodos ListDirectory. 
 * v3.0.0 (2016-03-06): Se agrego los metodos mkdir y mkdirs para crear directorios, asi como el metodo truncate para recortar un archivo. Se
 * mejoro el manejo de directorios y se realio una mejora sustancial en del
 * desempño en general de la clase y la consitencia de los datos de estado sobre
 * todo en los metodos skip y seek. Se mejoro notablemente los metodos delete,
 * wipe para eliminar directorios de forma recursiva, y copy para mejorar la
 * velocidad 
 * v3.1.0 (2016-03-07): Se agrego el metodo CreateNewFile() para crear archivo vacios nuevos. 
 * v3.1.1 (2016-03-12): Se solvento un Bug en el metodo mkDirs para manejar el crear los directorios padres de un archivo
 * v3.1.2 (2016-03-25) Se agrego el codigo de localizacion para la excepcion.
 */
public class FileManager implements Sortable {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHA";

    private static final int CopyBufferSize = 10485760; // 10MB

    private File Archivo;
    private long FileLength;
    private String FileName;
    private String FilePath;
    private String Separador;
    private FileInputStream Entrada = null;
    private FileOutputStream Salida = null;
    private long FilePosition;
    /**
     * **** Especifica como se abre el archivo ******
     */
    /**
     * Se abre para lectura
     */
    public final static int READ = 0;
    /**
     * Se abre para escritura
     */
    public final static int WRITE = 1;
    /**
     * **** Seleccion de Causas de Error para las Exepciones del manejo de IO
     * de archivo ******
     */
    /**
     * No se especifico un archivo en la ruta
     */
    public final static int ERROR_ISNOFILE = -1;
    /**
     * Archivo NO encontrado
     */
    public final static int ERROR_FILENOTFOUND = -2;
    /**
     * Error general de IO
     */
    public final static int ERROR_IO = -3;
    /**
     * El archivo no esta abierto para escritura
     */
    public final static int ERROR_NOWRITOPEN = -4;
    /**
     * El archivo no esta abierto para lectura
     */
    public final static int ERROR_NOREADOPEN = -5;
    /**
     * El archivo de salida se pudo crear
     */
    public final static int ERROR_NOCREATEOUTPUTFILE = -6;
    /**
     * La dimensiopn del archivo en invalida
     */
    public final static int ERROR_INVALIDSIZE = -7;

    /**
     * Contructor de Manejador de Entrada y Salida de Archivos, recibiendo como
     * parametro la ruta y el archivo a manejar,
     *
     * @param ArchivoPath Ruta del archivo a manejar
     */
    public FileManager(String ArchivoPath) {
        this(new File(ArchivoPath));
    }

    /**
     * Contructor de Manejador de Entrada y Salida de Archivos, recibiendo como
     * parametro el descriptor de archivos
     *
     * @param ArchivoDesc Descriptor del archivo
     */
    public FileManager(File ArchivoDesc) {

        // Genera el archivo
        this.Archivo = ArchivoDesc;
        if (this.Archivo.exists()) {
            if (this.Archivo.isFile()) {
                // Se recupera los parametros del archivo original
                this.FileLength = this.Archivo.length();
                this.FileName = this.Archivo.getName();
                this.FilePath = this.Archivo.getParent();
                this.Separador = File.separator;
            } else if (this.Archivo.isDirectory()) {
                this.FileLength = -1;
                this.FileName = null;
                this.Separador = File.separator;
                this.FilePath = this.Archivo.getPath();
            }
        } else {
            this.FileLength = -1;
            this.FileName = this.Archivo.getName();
            this.FilePath = this.Archivo.getParent();
            this.Separador = File.separator;
        }
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
     * Si es un archivo retorna el nombre, en caso contrario retorna null
     *
     * @return File Name
     */
    public String getFileName() {
        String lSalida = null;

        // Verifica si es un archivo
        if (Archivo.isFile()) {
            lSalida = FileName;
            if (this.Archivo.exists()) {
                this.FileLength = this.Archivo.length();
            } else {
                this.FileLength = -1;
            }
        }
        return lSalida;
    }

    /**
     * Retorna el nombre base del archivo, sin la extension
     *
     * @return File BaseName
     */
    public String getFileBaseName() {
        String BaseName = null;

        // Verifica si es un archivo
        if (Archivo.isFile()) {
            // Verifica que el archivo no sea oculto y que comience por punto
            if (FileName.lastIndexOf(".") > 0) {
                BaseName = FileName.substring(0, FileName.lastIndexOf("."));
            } else {
                BaseName = new String(FileName);
            }
            if (this.Archivo.exists()) {
                this.FileLength = this.Archivo.length();
            } else {
                this.FileLength = -1;
            }
        }
        return BaseName;
    }

    /**
     * Retorna la extension del archivo
     *
     * @return File Extension
     */
    public String getFileExtension() {
        String Extension = null;

        // Verifica si es un archivo
        if (Archivo.isFile()) {
            // Verifica que el archivo no sea oculto y que comience por punto
            if (FileName.lastIndexOf(".") > 0) {
                Extension = FileName.substring(FileName.lastIndexOf("."), FileName.length());
            } else {
                Extension = new String("");
            }
            if (this.Archivo.exists()) {
                this.FileLength = this.Archivo.length();
            } else {
                this.FileLength = -1;
            }
        }
        return Extension;
    }

    /**
     * Retorna la ruta del archivo o directorio
     *
     * @return File Path
     */
    public String getFilePath() {

        if (this.Archivo.exists()) {
            this.FileLength = this.Archivo.length();
        } else {
            this.FileLength = -1;
        }
        return this.FilePath;
    }

    /**
     * Retorna la longitud del archivo, -1 si no es un archivo o no existe
     *
     * @return File Length
     */
    public long getFileLength() {
        long lSalida = -1;

        if (this.Archivo.exists()) {
            // Verifica si es un archivo
            if (this.Archivo.isFile()) {
                this.FileLength = this.Archivo.length();
            } else {
                this.FileLength = -1;
            }
        } else {
            this.FileLength = -1;
        }
        lSalida = this.FileLength;
        return lSalida;
    }

    /**
     * Retorna el separador de ruta del sistema operativo
     *
     * @return OS path separator
     */
    public String getSeparador() {

        if (this.Archivo.exists()) {
            this.FileLength = this.Archivo.length();
        } else {
            this.FileLength = -1;
        }
        return this.Separador;
    }

    /**
     * Retorna la ruta completa al archivo, incluyendo el nombre del mismo, si
     * es un directorio retorna la ruta del mismo.
     *
     * Si no es ninguno de los dos retorna null.
     *
     * @return Ruta del archivo
     */
    public String getAbsoluteFilePath() {
        String AbsoluteFilePath;

        if (FileName != null) {
            AbsoluteFilePath = new String(FilePath + Separador + FileName);
        } else {
            AbsoluteFilePath = new String(FilePath);
        }
        // Verifica si es un archivo
        if (this.Archivo.isFile()) {
            this.FileLength = this.Archivo.length();
        } else if (this.Archivo.isDirectory()) {
            this.FileLength = -1;
        }
        return AbsoluteFilePath;
    }

    /**
     * Crea un archivo nuevo vacio, retornando TRUE si lo logro crear y FALSE si
     * no porque ya existe.
     *
     * @return TRUE si lo creo o FALSE si no.
     * @throws UtilsException
     */
    public boolean CreateNewFile() throws UtilsException {
        boolean Result = false;

        try {
            Result = this.Archivo.createNewFile();
        } catch (IOException ex) {
            throw new UtilsException("ERROR " + FilePath + Separador + FileName + " CAN'T be Created: " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"000");
        }
        return Result;
    }

    /**
     * Realiza el truncado del archivo a la nueva dimension de 'newSize'. Si
     * 'newSize' es mayor al tamaño actual no hace ningun cambio.
     *
     * @param newSize Nuevo tamaño del archvio
     * @throws UtilsException
     */
    public void Truncate(long newSize) throws UtilsException {
        FileChannel outChan;

        if (newSize >= 0) {
            if (Archivo.exists() && Archivo.isFile()) {
                try {
                    if (this.Entrada != null) {
                        outChan = this.Entrada.getChannel();
                    } else if (this.Salida != null) {
                        outChan = this.Salida.getChannel();
                    } else {
                        outChan = new FileOutputStream(Archivo, true).getChannel();
                    }
                    if (newSize < this.FileLength) {
                        outChan.truncate(newSize);
                        this.FileLength = newSize;
                    }
                    if (this.FilePosition > this.FileLength) {
                        this.FilePosition = this.FileLength;
                        outChan.position(this.FilePosition);
                    }
                    outChan.force(true);
                    outChan.close();
                } catch (FileNotFoundException ex) {
                    throw new UtilsException("ERROR " + FilePath + Separador + FileName + " NOT found: " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"001");
                } catch (IOException ex) {
                    throw new UtilsException("IO ERROR at " + FilePath + Separador + FileName + ": " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"002");
                }
            } else {
                this.FileLength = -1;
                throw new UtilsException("ERROR: " + FilePath + Separador + FileName + " is not a file to Truncate", ERROR_ISNOFILE,FileManager.CLASSID+"003");
            }
        } else {
            throw new UtilsException("ERROR: New Size to " + FilePath + Separador + FileName + " is below to ZERO", ERROR_INVALIDSIZE,FileManager.CLASSID+"004");
        }
    }

    /**
     * Retorna el tiempo de creacion del archivo en milisegundos desde epoch
     * (1970-01-01T00:00:00Z)
     *
     * @return the value in milliseconds, since the epoch (1970-01-01T00:00:00Z)
     * @throws UtilsException
     */
    public long getCreationTimeMillis() throws UtilsException {
        BasicFileAttributes attr;
        Path filep;
        long Millis;

        Millis = -1;
        // Verifica si es un archivo
        if (Archivo.isFile()) {
            try {
                filep = Paths.get(this.getAbsoluteFilePath());
                attr = Files.readAttributes(filep, BasicFileAttributes.class);
                Millis = attr.creationTime().toMillis();
            } catch (IOException ex) {
                throw new UtilsException("ERROR getting file creation time: " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"005");
            }
        } else {
            throw new UtilsException("ERROR is not file getting creation time of " + this.FileName, ERROR_ISNOFILE,FileManager.CLASSID+"006");
        }
        return Millis;
    }

    /**
     * Retorna el tiempo del ultimo acceso al archivo en milisegundos desde
     * epoch (1970-01-01T00:00:00Z)
     *
     * @return the value in milliseconds, since the epoch (1970-01-01T00:00:00Z)
     * @throws UtilsException
     */
    public long getLastAccessTimeMillis() throws UtilsException {
        BasicFileAttributes attr;
        Path filep;
        long Millis;

        Millis = -1;
        // Verifica si es un archivo
        if (Archivo.isFile()) {
            try {
                filep = Paths.get(this.getAbsoluteFilePath());
                attr = Files.readAttributes(filep, BasicFileAttributes.class);
                Millis = attr.lastAccessTime().toMillis();
            } catch (IOException ex) {
                throw new UtilsException("ERROR getting file last access time: " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"007");
            }
        } else {
            throw new UtilsException("ERROR is not file getting last access time of " + this.FileName, ERROR_ISNOFILE,FileManager.CLASSID+"008");
        }
        return Millis;
    }

    /**
     * Retorna el tiempo de la ultima modificacion al archivo en milisegundos
     * desde epoch (1970-01-01T00:00:00Z)
     *
     * @return the value in milliseconds, since the epoch (1970-01-01T00:00:00Z)
     * @throws UtilsException
     */
    public long getLastModifiedTimeMillis() throws UtilsException {
        BasicFileAttributes attr;
        Path filep;
        long Millis;

        Millis = -1;
        // Verifica si es un archivo
        if (Archivo.isFile()) {
            try {
                filep = Paths.get(this.getAbsoluteFilePath());
                attr = Files.readAttributes(filep, BasicFileAttributes.class);
                Millis = attr.lastModifiedTime().toMillis();
            } catch (IOException ex) {
                throw new UtilsException("ERROR getting file last modified time: " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"009");
            }
        } else {
            throw new UtilsException("ERROR is not file getting last modified time of " + this.FileName, ERROR_ISNOFILE,FileManager.CLASSID+"010");
        }
        return Millis;
    }

    /**
     * Verifica si el archivo denotado por la ruta abstracta se puede escribir
     *
     * @return TRUE si se puede escribir y FALSE si no.
     */
    public boolean canWrite() {
        return Archivo.canWrite();
    }

    /**
     * Verifica si el archivo denotado por la ruta abstracta se puede leer
     *
     * @return TRUE si se puede leer y FALSE si no.
     */
    public boolean canRead() {
        return Archivo.canRead();
    }

    /**
     * Verifica si el archivo denotado por la ruta abstracta se puede ejecutar
     *
     * @return TRUE si es ejecutable y FALSE si no.
     */
    public boolean canExecute() {
        boolean lSalida = false;

        // Verifica si es un archivo
        if (Archivo.isFile()) {
            lSalida = Archivo.canExecute();
        }
        return lSalida;
    }

    /**
     * Borra el archivo o el directorio especificado por lArchivo, si el
     * directorio no esta vacio continua borrando todo su contenido de forma
     * recursiva
     *
     * @param lArchivo Archivo o directorio a borrar
     * @return TRUE si lo logro o FALSE si no
     */
    private boolean Delete(File lArchivo) {
        boolean Result = true;
        File[] Contenido = null;
        int i;

        if (lArchivo.isDirectory()) {
            Contenido = lArchivo.listFiles();
            if (Contenido != null) {
                for (i = 0; i < Contenido.length; i++) {
                    Result = Result && this.Delete(Contenido[i]);
                }
            }
        }
        Result = Result && lArchivo.delete();
        return Result;
    }

    /**
     * Realiza el borrado del archivo o directorio denotado por la ruta
     * abstracta. Si el directorio no esta vacio continua eliminando todo su
     * contenido de forma recursiva.
     *
     * @return TRUE si se borro y FALSE si no.
     */
    public boolean Delete() {
        boolean lSalida;

        lSalida = this.Delete(Archivo);
        if (Archivo.exists()) {
            if (Archivo.isFile()) {
                FileLength = Archivo.length();
            } else {
                this.FileLength = -1;
            }
        } else {
            this.FileLength = -1;
        }
        return lSalida;
    }

    /**
     * Genera un nombre de archivo aleatorio con el formato nombre.ext en donde
     * el nombre es de longitud aleatoria y la extension es de 3 caracteres
     *
     * @return Nombre de archivo aleatorio
     */
    public static String genRandomFileName() {
        MersenneTwisterPlus random;
        StringBuffer RandName;
        int i;

        random = new MersenneTwisterPlus();
        RandName = new StringBuffer();
        for (i = 0; i < 30; i++) {
            if (random.nextDouble() < 0.5) {
                RandName.append((char) ((97 + random.nextInt(26)) % 123));
            } else {
                RandName.append((char) ((65 + random.nextInt(26)) % 91));
            }
            if (random.nextDouble() < 0.1) {
                i = 30;
            }
        }
        RandName.append('.');
        for (i = 0; i < 3; i++) {
            if (random.nextDouble() < 0.5) {
                RandName.append((char) ((97 + random.nextInt(26)) % 123));
            } else {
                RandName.append((char) ((65 + random.nextInt(26)) % 91));
            }
        }
        return RandName.toString();
    }

    /**
     * Borra un archivo o directorio de forma segura, Si el directorio no esta
     * vacio borra su contenio de froma segura. Retorna TRUE si lo logor borrar
     * y FALSE si NO
     *
     * @return TRUE si se borro y FALSE si no.
     * @throws UtilsException
     */
    public boolean Wipe() throws UtilsException {
        boolean Result = false;

        Result = this.Wipe(this.Archivo);
        if (Archivo.exists()) {
            if (Archivo.isFile()) {
                FileLength = Archivo.length();
            } else {
                this.FileLength = -1;
            }
        } else {
            this.FileLength = -1;
        }
        return Result;
    }

    /**
     * Realiza el borrado seguro del archivo o directorio denotado por la ruta
     * abstracta. Si el directorio no esta vacio realiza el borrado seguro de
     * forma recursiva
     *
     * @param lArchivo Archivo o directorio a borrar
     * @return TRUE si se borro y FALSE si no.
     * @throws UtilsException
     */
    private boolean Wipe(File lArchivo) throws UtilsException {
        boolean rSalida = true;
        MersenneTwisterPlus random;
        byte[] Buffer;
        long NumByteWrite, lFileSize;
        String NameMask;
        File[] Contenido = null;
        File NewFile = null;
        int i;
        FileOutputStream lSalida = null;

        random = new MersenneTwisterPlus();
        // Renombra el archivo para enmascarar el nombre
        NameMask = "." + FileManager.genRandomFileName();
        NewFile = new File(lArchivo.getParent() + Separador + NameMask);
        if (lArchivo.renameTo(NewFile)) {
            if (this.Archivo.equals(lArchivo)) {
                this.Archivo = NewFile;
            }
            lArchivo = NewFile;
        } else {
            throw new UtilsException("ERROR: File " + lArchivo.getParent()
                    + Separador + lArchivo.getName() + " NO Wiped, Rename Failed",
                    ERROR_NOCREATEOUTPUTFILE,FileManager.CLASSID+"011");
        }
        if (lArchivo.isDirectory()) {
            Contenido = lArchivo.listFiles();
            if (Contenido != null) {
                for (i = 0; i < Contenido.length; i++) {
                    rSalida = rSalida && this.Wipe(Contenido[i]);
                }
            }
        } else if ((lArchivo.isFile()) && (lArchivo.length() > 0)) {
            try {
                lFileSize = lArchivo.length();
                lSalida = new FileOutputStream(lArchivo, false);
                if (lFileSize > CopyBufferSize) {
                    Buffer = new byte[CopyBufferSize];
                } else {
                    Buffer = new byte[(int) lFileSize];
                }
                NumByteWrite = 0;
                while (NumByteWrite < lFileSize) {
                    random.nextBytes(Buffer);
                    lSalida.write(Buffer);
                    NumByteWrite += Buffer.length;
                }
            } catch (IOException e) {
                throw new UtilsException("ERROR: File " + lArchivo.getParent()
                        + Separador + lArchivo.getName() + " NO Created (" + e.getMessage() + ")",
                        ERROR_NOCREATEOUTPUTFILE,FileManager.CLASSID+"012");
            } finally {
                try {
                    if (lSalida != null) {
                        lSalida.flush();
                        lSalida.close();
                        lSalida = null;
                    }
                } catch (IOException e) {
                    throw new UtilsException("ERROR: File " + lArchivo.getParent()
                            + Separador + lArchivo.getName() + " NO Wiped (" + e.getMessage() + ")",
                            ERROR_NOCREATEOUTPUTFILE,FileManager.CLASSID+"013");
                }
            }
        }
        rSalida = rSalida && this.Delete(lArchivo);
        return rSalida;
    }

    /**
     * Verifica si el archivo denotado por la ruta abstracta existe
     *
     * @return TRUE si existe y FALSE si no.
     */
    public boolean Exists() {
        boolean lSalida;

        lSalida = this.Archivo.exists();
        if ((lSalida) && (this.Archivo.isFile())) {
            this.FileLength = this.Archivo.length();
        } else {
            this.FileLength = -1;
        }
        return lSalida;
    }

    /**
     * Realiza el cambio de nombre del archivo al especificado por la ruta
     * abstracta NewFileName
     *
     * @param NewFileName
     * @return TRUE si logro realizar el cambio y FALSE si no
     */
    public boolean Rename(String NewFileName) {
        File Nuevo;
        boolean Salida = false;

        Nuevo = new File(NewFileName);
        Salida = this.Archivo.renameTo(Nuevo);
        if (Salida) {
            this.Archivo = Nuevo;
        }
        return Salida;
    }

    /**
     * Abre el archivo en modo escritura si se especifica FileManager.WRITE o en
     * modo lectura si se especifica FileManager.READ. Si se abre en modo WRITE
     * se especifica si se abre en modo append o no.
     *
     * @param mode puede ser FileManager.WRITE o FileManager.READ
     * @param append TRUE para indicar que se abre en modo Append o FALSE para
     * no
     * @throws UtilsException
     */
    public void Open(int mode, boolean append) throws UtilsException {

        try {
            // Verifica si el archivo no existe y estamos en modo de escritura para
            // crearlo
            if ((!this.Archivo.exists()) && (mode == WRITE)) {
                try {
                    if (this.Entrada != null) {
                        this.Entrada.close();
                    }
                    this.Entrada = null;
                    this.Salida = new FileOutputStream(this.Archivo, append);
                    this.FilePosition = 0;
                    this.FileLength = 0;
                } catch (FileNotFoundException ex) {
                    throw new UtilsException("ERROR: File " + this.FilePath + this.Separador + this.FileName + " NOT Created (" + ex.getMessage() + ")", ERROR_NOCREATEOUTPUTFILE,FileManager.CLASSID+"014");
                }
            } else if ((this.Archivo.exists()) && (this.Archivo.isFile())) {  // Verifica si es un archivo
                if (mode == READ) {
                    try {
                        if (this.Salida != null) {
                            this.Salida.close();
                        }
                        this.Salida = null;
                        this.Entrada = new FileInputStream(this.Archivo);
                        this.FilePosition = 0;
                    } catch (FileNotFoundException ex) {
                        throw new UtilsException("ERROR: File " + this.FilePath + this.Separador + this.FileName + " NOT found (" + ex.getMessage() + ")", ERROR_FILENOTFOUND,FileManager.CLASSID+"015");
                    }
                } else if (mode == WRITE) {
                    try {
                        if (this.Entrada != null) {
                            this.Entrada.close();
                        }
                        this.Entrada = null;
                        this.Salida = new FileOutputStream(this.Archivo, append);
                        this.FilePosition = this.Salida.getChannel().size();
                    } catch (FileNotFoundException ex) {
                        throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " NOT Created (" + ex.getMessage() + ")", ERROR_NOCREATEOUTPUTFILE,FileManager.CLASSID+"016");
                    }
                }
                this.FileLength = this.Archivo.length();
            } else if ((!Archivo.exists()) && (mode == READ)) {
                this.FileLength = -1;
                throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " NO exists and CAN'T be open in read mode", ERROR_FILENOTFOUND,FileManager.CLASSID+"017");
            } else {
                this.FileLength = -1;
                throw new UtilsException("ERROR: " + FilePath + Separador + FileName + " is NOT a file", ERROR_ISNOFILE,FileManager.CLASSID+"018");
            }
        } catch (IOException ex) {
            throw new UtilsException("IO ERROR at " + FilePath + Separador + FileName + ": " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"019");
        }
    }

    /**
     * Cierra el archivo abierto con open
     *
     * @throws UtilsException
     */
    public void Close() throws UtilsException {

        try {
            // Verifica si es un archivo
            if (this.Archivo.isFile()) {
                if (this.Entrada != null) {
                    this.Entrada.close();
                }
                if (this.Salida != null) {
                    this.Salida.flush();
                    this.Salida.close();
                }
                this.FileLength = this.Archivo.length();
                this.FilePosition = 0;
            } else {
                throw new UtilsException("ERROR: " + FilePath + Separador + FileName + " is NOT a file", ERROR_ISNOFILE,FileManager.CLASSID+"020");
            }
        } catch (IOException ex) {
            throw new UtilsException("IO ERROR at " + FilePath + Separador + FileName + ": " + ex.getMessage(), ERROR_IO,FileManager.CLASSID+"021");
        } finally {
            this.Entrada = null;
            this.Salida = null;
        }
    }

    /**
     * Escribe el contenido de "Data" en el archivo, retornando la cantidad de
     * bytes escritos en la operacion
     *
     * Si el archivo no esta abierto para escritura retorna -1
     *
     * @param Data arreglo de bytes a escribir
     * @return numero de bytes escritos, -1 si el archivo no esta abierto para
     * escritura
     * @throws UtilsException
     */
    public long Write(byte[] Data) throws UtilsException {
        long NumBytesWrite;

        NumBytesWrite = this.Write(Data, 0, Data.length);
        return NumBytesWrite;
    }

    /**
     * Escribe la cantidad de bytes de "Data", especificados por "len" a partir
     * de "off", retornando la cantidad de bytes escritos en la operacion
     *
     * Si el archivo no esta abierto para escritura o algun error de escritura
     * retorna -1
     *
     * @param Data arreglo de bytes conteniendo los datods
     * @param off offset de Data desde donde iniciar la escritura
     * @param len Longitud de los datos a copiar
     * @return Numero de bytes escritos en la operacion, -1 si el archivo no
     * esta abierto para escritura
     * @throws UtilsException
     */
    public long Write(byte[] Data, int off, int len) throws UtilsException {
        long NumBytesWrite;

        NumBytesWrite = -1;
        try {
            // Verifica si es un archivo
            if (this.Archivo.isFile()) {
                if (this.Salida != null) {
                    if (len > 0) {
                        if ((off >= 0) && (off < Data.length)) {
                            if (off + len > Data.length) {
                                len = Data.length - off;
                            }
                            this.Salida.write(Data, off, len);
                            NumBytesWrite = len;
                            this.FilePosition += NumBytesWrite;
                            this.FileLength = Archivo.length();
                        } else {
                            throw new UtilsException("ERROR: At Write File " + FilePath + Separador + FileName + " offset out of range", ERROR_IO,FileManager.CLASSID+"022");
                        }
                    } else {
                        throw new UtilsException("ERROR: At Write File " + FilePath + Separador + FileName + " length out of range", ERROR_IO,FileManager.CLASSID+"023");
                    }
                } else {
                    throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " NOT Open to Write", ERROR_NOWRITOPEN,FileManager.CLASSID+"024");
                }
            } else {
                throw new UtilsException("ERROR: At Write " + FilePath + Separador + FileName + " is NOT a File", ERROR_ISNOFILE,FileManager.CLASSID+"025");
            }
        } catch (IOException ex) {
            throw new UtilsException("ERROR: At Write File " + FilePath + Separador + FileName + " IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"026");
        }
        return NumBytesWrite;
    }

    /**
     * Realiza la lectura del archivo leyendo Data.length bytes, y cargando el
     * resultado en Data.
     *
     * Retorna el numero de bytes leidos y cargados en el buffer, -1 si no logro
     * leer ninguno porque llego al final del archivo o -2 en caso de Error
     *
     * @param Data buffer donde almacenar los bytes leidos
     * @return Numero de bytes leidos, -1 si llego al final del archivo o -2 en
     * caso de Error
     * @throws UtilsException
     */
    public long Read(byte[] Data) throws UtilsException {
        long NumBytesRead;

        NumBytesRead = this.Read(Data, 0, Data.length);
        return NumBytesRead;
    }

    /**
     * Realiza la lectura del archivo tratando de leer "len" bytes y colocarlos
     * en el buffer "Data" a partir de la posicion "off", si no logra leer los
     * "len" bytes coloca en el buffer lo que logro leer.
     *
     * Si "len" es mayor que 0, el metodo bloquea hasta que se lean "len" bytes
     *
     * Retorna el numero de bytes leidos y cargados en el buffer y -1 si no
     * logro leer ninguno porque llego al final del archivo o -2 en caso de
     * error de lectura
     *
     * @param Data buffer donde almacenar los bytes leidos
     * @param off Posicion a partir de donde colocar los bytes leidos en el
     * buffer Data
     * @param len Numero MAXIMO de bytes a leer y cargar en el buffer, si 0 no
     * bloquea
     * @return Numero de bytes leidos, -1 si llego al final del archivo o -2 en
     * caso de error de lectura
     * @throws UtilsException
     */
    public long Read(byte[] Data, int off, int len) throws UtilsException {
        long NumBytesRead;

        NumBytesRead = -2;
        try {
            // Verifica si es un archivo
            if (this.Archivo.isFile()) {
                if (this.Entrada != null) {
                    if (len >= 0) {
                        if ((off >= 0) && (off < Data.length)) {
                            if ((off + len > Data.length) && (len > 0)) {
                                len = Data.length - off;
                            }
                            NumBytesRead = this.Entrada.read(Data, off, len);
                            if (NumBytesRead > 0) {
                                this.FilePosition += NumBytesRead;
                            }
                            this.FileLength = Archivo.length();
                        } else {
                            throw new UtilsException("ERROR: At read File " + FilePath + Separador + FileName + " offset out of range", ERROR_IO,FileManager.CLASSID+"027");
                        }
                    } else {
                        throw new UtilsException("ERROR: At read File " + FilePath + Separador + FileName + " length out of range", ERROR_IO,FileManager.CLASSID+"028");
                    }
                } else {
                    throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " NO Read Open", ERROR_NOREADOPEN,FileManager.CLASSID+"029");
                }
            } else {
                throw new UtilsException("ERROR: " + FilePath + Separador + FileName + " is NOT a File", ERROR_ISNOFILE,FileManager.CLASSID+"030");
            }
        } catch (IOException ex) {
            throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"031");
        }
        return NumBytesRead;
    }

    /**
     * Realiza el copiado desde el archivo origen al destino usando el metodo de
     * NIO con buffer. Retorna TRUE si logro realizar la copia o FALSE si no
     *
     * @param source Archivo de Origen
     * @param target Archivo de Destino
     * @return TRUE si logro realizar la copia o FALSE si no
     */
    private boolean nioBufferCopy(File Source, File Target) throws UtilsException {
        FileChannel inChan = null;
        FileChannel outChan = null;
        ByteBuffer buffer = null;
        boolean Result = false;

        try {
            inChan = new FileInputStream(Source).getChannel();
            outChan = new FileOutputStream(Target, false).getChannel();
            if (outChan.size() > 0) {
                outChan.truncate(0);
            }
            buffer = ByteBuffer.allocateDirect(CopyBufferSize);
            while (inChan.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    outChan.write(buffer);
                }
                buffer.clear();
            }
            Result = true;
        } catch (IOException ex) {
            throw new UtilsException("ERROR: File Copy IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"032");
        } finally {
            try {
                if (inChan != null) {
                    inChan.close();
                }
            } catch (IOException ex) {
                throw new UtilsException("ERROR: File " + Source.getAbsolutePath() + " IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"033");
            }
            try {
                if (outChan != null) {
                    outChan.force(true);
                    outChan.close();
                }
            } catch (IOException ex) {
                throw new UtilsException("ERROR: File " + Target.getAbsolutePath() + " IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"034");
            }
        }
        return Result;
    }

    /**
     * Realiza el copiado del archivo especificado por FM reemplazando el actual
     * archivo o si es un directorio creando una copia dentro del directorio; el
     * objeto directorio sigue apuntando al directorio raiz.
     *
     * Retorna TRUE si lo logro copiar o FALSE si n o
     *
     * @param FM Archivo de entrada desde donde se va a realizar la copia
     * @return TRUE si logro copiar, FALSE si no.
     * @throws UtilsException
     */
    public boolean Copy(FileManager FM) throws UtilsException {
        boolean lSalida = false;
        File In, Out;

        if (FM.isFile()) {
            In = new File(FM.Archivo.getAbsolutePath());
            if (this.isDirectory()) {
                Out = new File(this.FilePath + this.Separador + FM.getFileName());
            } else {
                Out = this.Archivo;
            }
            lSalida = this.nioBufferCopy(In, Out);
            if (this.Archivo.isFile()) {
                this.FileLength = this.Archivo.length();
            } else {
                this.FileLength = -1;
            }
        }
        return lSalida;
    }

    /**
     * Coloca el aputando del archivo en la posicion inical.
     *
     * @throws UtilsException
     */
    public void Reset() throws UtilsException {

        this.Seek(0);
    }

    /**
     * Salta los siguientes "n" bytes del archivo, retornando los bytes que se
     * ignoraron a parti de la posicion actual en el archivo.
     *
     * @param n Numero de bytes a ignorar, si es menor que 0 se saltan hacia
     * atras esa cantida de bytes
     * @return numero de bytes saltaron en la operacion
     * @throws UtilsException
     */
    public long Skip(long n) throws UtilsException {
        FileChannel fChan = null;
        long NewPos, CurrentPos, NBS;

        NBS = 0;
        try {
            // Verifica si es un archivo
            if (this.Archivo.isFile()) {
                if (this.Entrada != null) {
                    fChan = this.Entrada.getChannel();
                } else if (this.Salida != null) {
                    fChan = this.Salida.getChannel();
                } else {
                    throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " NOT OPEN", ERROR_IO,FileManager.CLASSID+"035");
                }
                if (fChan != null) {
                    this.FileLength = fChan.size();
                    CurrentPos = fChan.position();
                    NewPos = CurrentPos + n;
                    if (NewPos < 0) {
                        NewPos = 0;
                    } else if (NewPos > this.FileLength) {
                        NewPos = this.FileLength;
                    }
                    NBS = NewPos - CurrentPos;
                    fChan.position(NewPos);
                    fChan.force(true);
                    this.FilePosition = NewPos;
                }
            } else {
                throw new UtilsException("ERROR: is NOT a File", ERROR_ISNOFILE,FileManager.CLASSID+"036");
            }
        } catch (IOException ex) {
            throw new UtilsException("ERROR: File Skip " + FilePath + Separador + FileName + " IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"037");
        }
        return NBS;
    }

    /**
     * Posiciona el apuntador de posicion del archivo al valor especificado en
     * "pos".
     *
     * Si el valor de pos esta fuera del rango del tamaño del archivo genera una
     * excepcion, asi mismo si existe algun error de acceso al archivo.
     *
     * @param pos
     * @throws UtilsException
     */
    public void Seek(long pos) throws UtilsException {
        FileChannel fChan = null;

        try {
            // Verifica si es un archivo
            if (this.Archivo.isFile()) {
                if ((pos >= 0) && (pos <= this.Archivo.length())) {
                    if (this.Entrada != null) {
                        fChan = this.Entrada.getChannel();
                    } else if (this.Salida != null) {
                        fChan = this.Salida.getChannel();
                    } else {
                        throw new UtilsException("ERROR: File " + FilePath + Separador + FileName + " NOT OPEN", ERROR_IO,FileManager.CLASSID+"038");
                    }
                    if (fChan != null) {
                        this.FileLength = fChan.size();
                        fChan.position(pos);
                        fChan.force(true);
                        this.FilePosition = pos;
                    }
                } else {
                    throw new UtilsException("ERROR: Seek position out of range at file " + FilePath + Separador + FileName, ERROR_IO,FileManager.CLASSID+"039");
                }
            } else {
                this.FileLength = -1;
                throw new UtilsException("ERROR: Is NOT a File", ERROR_ISNOFILE,FileManager.CLASSID+"040");
            }
        } catch (IOException ex) {
            throw new UtilsException("ERROR: File Seek " + FilePath + Separador + FileName + " IO Error (" + ex.getMessage() + ")", ERROR_IO,FileManager.CLASSID+"041");
        }
    }

    /**
     * Retorna TRUE si el archivo esta abierto para lectura, caso contrario
     * retorna FALSE.
     *
     * @return TRUE si esta abierto para lectura FALSE si no.
     */
    public boolean isReadOpen() {
        boolean lSalida;

        if (Archivo.isFile()) {
            FileLength = Archivo.length();
        } else {
            this.FileLength = -1;
        }
        if ((Entrada != null) && (Salida == null)) {
            lSalida = true;
        } else {
            lSalida = false;
        }
        return lSalida;
    }

    /**
     * Retorna TRUE si el archivo esta abierto para escritura, caso contrario
     * retorna FALSE.
     *
     * @return TRUE si esta abierto para escritura FALSE si no.
     */
    public boolean isWriteOpen() {
        boolean lSalida;

        if (Archivo.isFile()) {
            FileLength = Archivo.length();
        } else {
            this.FileLength = -1;
        }
        if ((Entrada == null) && (Salida != null)) {
            lSalida = true;
        } else {
            lSalida = false;
        }
        return lSalida;
    }

    /**
     * Al crear la clase se verifica que es un archivo el parametro especificado
     * en la ruta abstracta, pero esta funcion realiza esa verificacion.
     *
     * @return TRUE si es un archivo y FALSE si no
     */
    public boolean isFile() {
        boolean lSalida = false;

        if (Archivo.exists()) {
            // Verifica si es un archivo
            if (Archivo.isFile()) {
                FileLength = Archivo.length();
                lSalida = true;
            } else {
                this.FileLength = -1;
            }
        } else {
            this.FileLength = -1;
        }
        return lSalida;
    }

    /**
     * Al crear la clase se verifica que es un directorio el parametro
     * especificado en la ruta abstracta, pero esta funcion realiza esa
     * verificacion.
     *
     * @return TRUE si es un directorio y FALSE si no
     */
    public boolean isDirectory() {
        boolean Result = false;

        if (Archivo.exists()) {
            if (Archivo.isDirectory()) {
                Result = true;
                this.FileLength = -1;
            } else if (Archivo.isFile()) {
                FileLength = Archivo.length();
            }
        } else {
            this.FileLength = -1;
        }
        return Result;
    }

    /**
     * Crea el directorio denotador por la ruta abstracta COMPLETA (todos los que
     * no existen), retornando TRUE si lo logro y FALSE si no.
     *
     * @return TRUE lo logro FALSE si no
     */
    public boolean mkDirs() {
        boolean Result = false;

        if (!Archivo.exists()) {
            Result = Archivo.mkdirs();
        } else {
            Result = true;
        }
        return Result;
    }

    /**
     * Crea el directorio denotador por la ruta abstracta, asi como los
     * directorios faltantes para completar la ruta.
     * Si el Nombre del archivo NO ESTA definido crea la ruta completa como
     * directorios, SI ESTA definido, crea la ruta de los directorios padres al
     * nombre del archivo, retornando TRUE si lo logro o FALSE si no.
     *
     * @return TRUE lo logro FALSE si no
     */
    public boolean mkDir() {
        boolean Result = false;
        File Parent;

        if (!Archivo.exists()) {
            if (this.FileName == null) {
                Result = Archivo.mkdirs();
            } else {
                Parent = Archivo.getParentFile();
                if (Parent != null) {
                    if (!Parent.exists()) {
                        Result = Parent.mkdirs();
                    } else {
                        Result = true;
                    }
                }
            }
        }
        return Result;
    }

    /**
     * Si la clase FileManager contiene un directorio retorna una arreglo de
     * FileManagers con el contenido del directorio, ya sean archivos o
     * directorios, si no es un directorio retorna NULL
     *
     * El contenido no se retorna en ningun orden especifico
     *
     * @return Lista de archivos y directorios contenidos en el directorio o
     * NULL
     * @throws UtilsException
     */
    public FileManager[] ListDirectory() throws UtilsException {
        FileManager[] Archivos;
        File[] Lista;
        int i;

        Archivos = null;
        if (this.Archivo.isDirectory()) {
            Lista = this.Archivo.listFiles();
            if (Lista != null) {
                Archivos = new FileManager[Lista.length];
                if (Lista.length > 0) {
                    for (i = 0; i < Lista.length; i++) {
                        Archivos[i] = new FileManager(Lista[i]);
                    }
                }
            } else {
                throw new UtilsException("ERROR: Listing directory " + FilePath + Separador + FileName + " IO Error", ERROR_IO,FileManager.CLASSID+"042");
            }
        }
        return Archivos;
    }

    /**
     * Si la clase FileManager contiene un directorio retorna una arreglo de
     * FileManagers con el contenido del directorio, ya sean archivos o
     * directorios filtrados segun la regla especificada por el parametro Filter
     *
     * El contenido no se retorna en ningun orden especifico
     *
     * @param Filter Filtro de busqueda en el directorio
     * @return Lista de archivos y directorios contenidos en el directorio
     * @throws UtilsException
     */
    public FileManager[] ListDirectory(FilenameFilter Filter) throws UtilsException {
        FileManager[] Archivos;
        File[] Lista;
        int i;

        Archivos = null;
        if (this.Archivo.isDirectory()) {
            Lista = this.Archivo.listFiles(Filter);
            if (Lista != null) {
                Archivos = new FileManager[Lista.length];
                if (Lista.length > 0) {
                    for (i = 0; i < Lista.length; i++) {
                        Archivos[i] = new FileManager(Lista[i]);
                    }
                }
            } else {
                throw new UtilsException("ERROR: Listing filtered directory " + FilePath + Separador + FileName + " IO Error", ERROR_IO,FileManager.CLASSID+"043");
            }
        }
        return Archivos;
    }

    /**
     * Si la clase FileManager contiene un directorio retorna una arreglo de
     * FileManagers con el contenido del directorio, ya sean archivos o
     * directorios filtrados segun la regla especificada por el parametro Filter
     *
     * El contenido no se retorna en ningun orden especifico
     *
     * @param Filter Filtro de busqueda en el directorio
     * @return Lista de archivos y directorios contenidos en el directorio
     * @throws UtilsException
     */
    public FileManager[] ListDirectory(FileFilter Filter) throws UtilsException {
        FileManager[] Archivos;
        File[] Lista;
        int i;

        Archivos = null;
        if (this.Archivo.isDirectory()) {
            Lista = this.Archivo.listFiles(Filter);
            if (Lista != null) {
                Archivos = new FileManager[Lista.length];
                if (Lista.length > 0) {
                    for (i = 0; i < Lista.length; i++) {
                        Archivos[i] = new FileManager(Lista[i]);
                    }
                }
            } else {
                throw new UtilsException("ERROR: Listing filtered directory " + FilePath + Separador + FileName + " IO Error", ERROR_IO,FileManager.CLASSID+"044");
            }
        }
        return Archivos;
    }

    /**
     * Metodo que establece la longitud del archivo como el valor de
     * ordenamiento absoluto del elemento
     *
     * @return indice de ordenamiento absotulo
     */
    @Override
    public long Metrica() {
        long lSalida = -1;

        // Verifica si es un archivo
        if (this.Archivo.isFile()) {
            lSalida = this.FileLength;
        }
        return lSalida;
    }

    /**
     * Metodo que compara el nombre de dos Archivos y retorna -1 si es menor al
     * "Other", 0 si es igual y 1 si es mayo que "Other"
     *
     * @param Other
     * @return -1 si menor, 0 si igual y 1 si mayor
     */
    @Override
    public int Order(Sortable Other) {
        FileManager OFM;
        String Local, Otro;
        int lSalida;

        OFM = (FileManager) Other;
        Local = this.getAbsoluteFilePath();
        Otro = OFM.getAbsoluteFilePath();
        if (Local == null) {
            lSalida = -1;
        } else if (Otro == null) {
            lSalida = 1;
        } else {
            lSalida = Local.compareTo(Otro);
        }
        return lSalida;
    }

    @Override
    public double MetricaDouble() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
