package com.bolivartech.utils.converters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Copyright 2007,2009,2010,2011,2012 BolivarTech C.A.
 *
 * <p>Codifica y decodifica a una notacion en Base64.</p>
 * <p>Pagina WEB: <a href="http://www.bolivartech.com">BolivarTechNetworks</a>.</p>
 * 
 * <p>Ejemplo:</p>
 * 
 * <code>String codificado = Base64.encode( ArregloDeBytes );</code>
 * 
 * <code>byte[] ArregloDeBytes = Base64.decode( codificado );</code>
 *
 * <p>La libreria soporta los siguientes modificadores que permiten la modificacion
 * del comportamiento de la libreria</p>
 *
 * URL_SAFE: Realiza la codificacion en base al RFC3548 para permitir la compatibilidad
 *           con la codificacion de los URL
 *
 * ORDERED: Realiza la codificacion en base al alfabeto especificado en el RFCC1940
 *
 * BREAK_LINES: Realiza los saltos de linea segun el estandar de cada 76 caracteres
 *
 * <code>String codificado = Base64.encodeBytes( ArregloDeBytes, Base64.URL_SAFE | Base64.BREAK_LINES );</code>
 * <p>Esto permite realizar la codificacion en base al RFC3548 y limitar el numero de caracteres por linea
 * a 76.</p>
 *
 * @author Julian Bolivar
 * @since 2007 - January 15, 2012.
 * @version 2.0.0
 */
public class Base64
{
    
/* ********  Modificadores Publicos (se disponen de maximo 32 bits) ******** */
    
    /** No hay opciones especificadas. Valor es cero. */
    public final static int NO_OPTIONS = 0;
    
    /** En el primer bit especifica que se va realizar una codificacion. Valor es uno. */
    public final static int ENCODE = 1;
    
    
    /** En el primer bit especifica que se va a realizar una decodificacion. Valor es cero. */
    public final static int DECODE = 0;
        
    /** Coloca los saltos de linea al finalizar la codificacion. Value is 8. */
    public final static int BREAK_LINES = 2;
	
    /** 
     * Codifica utilizando un diccionario de base64 que es URL y nombre de archivo "segurp" segun lo
     * descrito en la seccion 4 del RFC3548
     *  
     * <a href="http://www.faqs.org/rfcs/rfc3548.html">http://www.faqs.org/rfcs/rfc3548.html</a>.
     */
     public final static int URL_SAFE = 4;

     /**
      * Codifica utilizando el diccionario de base64 "ordenado" que es descrito en el rfcc1940.
      *
      * <a href="http://www.faqs.org/qa/rfcc-1940.html">http://www.faqs.org/qa/rfcc-1940.html</a>.
      */
     public final static int ORDERED = 8;

    
/* ********  Parametros de configuracion privados  ******** */
    
    
    /** Longitud maxima de una linea (76) en la salida de base64. */
    private final static int MAX_LINE_LENGTH = 76;
    
    
    /** El signo de igual (=) almacenado como byte. */
    private final static byte EQUALS_SIGN = (byte)'=';
    
    
    /** El caracter de nueva linea (\n) almacenado como byte. */
    private final static byte NEW_LINE = (byte)'\n';
        
    // Indica espacios en blanco en la codificacion
    private final static byte WHITE_SPACE_ENC = -5;
    // Indica los signos de igual en la codificacion
    private final static byte EQUALS_SIGN_ENC = -1; 
	
	
/* ********  DICCIONARIO ESTANDAR PARA BASE64  ******** */
    
    private final static byte[] _DICCIONARIO_STANDARD = {
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
        (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
        (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
        (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
        (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
        (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
        (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
        (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
    };
	
    
    /**
     * Realiza la traduccion de un caracter codificado en Base64 a su valor de
     * 6 bits en binario.
     **/
    private final static byte[] _STANDARD_DECODABET = {
        -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
        -5,-5,                                      // Whitespace: Tab and Linefeed
        -9,-9,                                      // Decimal 11 - 12
        -5,                                         // Whitespace: Carriage Return
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
        -9,-9,-9,-9,-9,                             // Decimal 27 - 31
        -5,                                         // Whitespace: Space
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
        62,                                         // Plus sign at decimal 43
        -9,-9,-9,                                   // Decimal 44 - 46
        63,                                         // Slash at decimal 47
        52,53,54,55,56,57,58,59,60,61,              // Numbers zero through nine
        -9,-9,-9,                                   // Decimal 58 - 60
        -1,                                         // Equals sign at decimal 61
        -9,-9,-9,                                      // Decimal 62 - 64
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,            // Letters 'A' through 'N'
        14,15,16,17,18,19,20,21,22,23,24,25,        // Letters 'O' through 'Z'
        -9,-9,-9,-9,-9,-9,                          // Decimal 91 - 96
        26,27,28,29,30,31,32,33,34,35,36,37,38,     // Letters 'a' through 'm'
        39,40,41,42,43,44,45,46,47,48,49,50,51,     // Letters 'n' through 'z'
        -9,-9,-9,-9                                 // Decimal 123 - 126
    };
	
	
/* ********  DICCIONARIO BASE64 PARA URL SAFE  ******** */
	
    /**
     * Los caracteres "slash" y el signo de "mas" fueron cambiado por "guion" y "underscore"
     */
    private final static byte[] _DICCIONARIO_URL_SAFE = {
      (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
      (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
      (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
      (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
      (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
      (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
      (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
      (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
      (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
      (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'-', (byte)'_'
    };
	
    /**
     * Realiza la traduccion de un caracter codificado en Base64 a su valor de
     * 6 bits en binario para la codificacion URL_SAFE
     **/
    private final static byte[] _URL_SAFE_DECODABET = {
      -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
      -5,-5,                                      // Whitespace: Tab and Linefeed
      -9,-9,                                      // Decimal 11 - 12
      -5,                                         // Whitespace: Carriage Return
      -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
      -9,-9,-9,-9,-9,                             // Decimal 27 - 31
      -5,                                         // Whitespace: Space
      -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
      -9,                                         // Plus sign at decimal 43
      -9,                                         // Decimal 44
      62,                                         // Minus sign at decimal 45
      -9,                                         // Decimal 46
      -9,                                         // Slash at decimal 47
      52,53,54,55,56,57,58,59,60,61,              // Numbers zero through nine
      -9,-9,-9,                                   // Decimal 58 - 60
      -1,                                         // Equals sign at decimal 61
      -9,-9,-9,                                   // Decimal 62 - 64
      0,1,2,3,4,5,6,7,8,9,10,11,12,13,            // Letters 'A' through 'N'
      14,15,16,17,18,19,20,21,22,23,24,25,        // Letters 'O' through 'Z'
      -9,-9,-9,-9,                                // Decimal 91 - 94
      63,                                         // Underscore at decimal 95
      -9,                                         // Decimal 96
      26,27,28,29,30,31,32,33,34,35,36,37,38,     // Letters 'a' through 'm'
      39,40,41,42,43,44,45,46,47,48,49,50,51,     // Letters 'n' through 'z'
      -9,-9,-9,-9                                 // Decimal 123 - 126
    };



/* ******** DICCIONARIO ORDERED PARA BASE64 ******** */

    private final static byte[] _DICCIONARIO_ORDERED = {
      (byte)'-',
      (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4',
      (byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9',
      (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
      (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
      (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
      (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
      (byte)'_',
      (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
      (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
      (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
      (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z'
    };
	
    /**
     * Realiza la traduccion de un caracter codificado en Base64 a su valor de
     * 6 bits en binario para el diccionario ordenado
     **/
    private final static byte[] _ORDERED_DECODABET = {
      -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
      -5,-5,                                      // Whitespace: Tab and Linefeed
      -9,-9,                                      // Decimal 11 - 12
      -5,                                         // Whitespace: Carriage Return
      -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
      -9,-9,-9,-9,-9,                             // Decimal 27 - 31
      -5,                                         // Whitespace: Space
      -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
      -9,                                         // Plus sign at decimal 43
      -9,                                         // Decimal 44
      0,                                          // Minus sign at decimal 45
      -9,                                         // Decimal 46
      -9,                                         // Slash at decimal 47
      1,2,3,4,5,6,7,8,9,10,                       // Numbers zero through nine
      -9,-9,-9,                                   // Decimal 58 - 60
      -1,                                         // Equals sign at decimal 61
      -9,-9,-9,                                   // Decimal 62 - 64
      11,12,13,14,15,16,17,18,19,20,21,22,23,     // Letters 'A' through 'M'
      24,25,26,27,28,29,30,31,32,33,34,35,36,     // Letters 'N' through 'Z'
      -9,-9,-9,-9,                                // Decimal 91 - 94
      37,                                         // Underscore at decimal 95
      -9,                                         // Decimal 96
      38,39,40,41,42,43,44,45,46,47,48,49,50,     // Letters 'a' through 'm'
      51,52,53,54,55,56,57,58,59,60,61,62,63,     // Letters 'n' through 'z'
      -9,-9,-9,-9                                 // Decimal 123 - 126
    };
	
/* ********  DETERMINA CUAL DICCIONARIO UTILIZAR  ******** */

    /**
     * Calcula cual diccionario se va a utilizar en la codificacion
     */
    private final static byte[] getAlphabet( int options ) {
        if ((options & URL_SAFE) == URL_SAFE) {
            return _DICCIONARIO_URL_SAFE;
        } else if ((options & ORDERED) == ORDERED) {
            return _DICCIONARIO_ORDERED;
        } else {
            return _DICCIONARIO_STANDARD;
        }
    }

    /**
     * Retorna cual es el diccionario de traduccion a binario a utilizar
     */
    private final static byte[] getDecodabet( int options ) {
        if( (options & URL_SAFE) == URL_SAFE) {
            return _URL_SAFE_DECODABET;
        } else if ((options & ORDERED) == ORDERED) {
            return _ORDERED_DECODABET;
        } else {
            return _STANDARD_DECODABET;
        }
    }	
    
    /** Evita que la clase se pueda instaciar. */
    private Base64(){}
    
/* ********  METODOS DE CODIFICACION  ******** */
          
    /**
     * <p>Encodes up to three bytes of the array <var>source</var>
     * and writes the resulting four Base64 bytes to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying 
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 3 for
     * the <var>source</var> array or <var>destOffset</var> + 4 for
     * the <var>destination</var> array.
     * The actual number of significant bytes in your array is
     * given by <var>numSigBytes</var>.</p>
     * <p>This is the lowest level of the encoding methods with
     * all possible parameters.</p>
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param numSigBytes the number of significant bytes in your array
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
     * @return the <var>destination</var> array
     */
    private static byte[] encode3to4( 
    byte[] source, int srcOffset, int numSigBytes,
    byte[] destination, int destOffset, int options ) {
        
	byte[] ALPHABET = getAlphabet( options ); 
	
        //           1         2         3  
        // 01234567890123456789012345678901 Bit position
        // --------000000001111111122222222 Array position from threeBytes
        // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
        //          >>18  >>12  >> 6  >> 0  Right shift necessary
        //                0x3f  0x3f  0x3f  Additional AND
        
        // Create buffer with zero-padding if there are only one or two
        // significant bytes passed in the array.
        // We have to shift left 24 in order to flush out the 1's that appear
        // when Java treats a value as negative that is cast from a byte to an int.
        int inBuff =   ( numSigBytes > 0 ? ((source[ srcOffset     ] << 24) >>>  8) : 0 )
                     | ( numSigBytes > 1 ? ((source[ srcOffset + 1 ] << 24) >>> 16) : 0 )
                     | ( numSigBytes > 2 ? ((source[ srcOffset + 2 ] << 24) >>> 24) : 0 );

        switch( numSigBytes )
        {
            case 3:
                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
                destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
                destination[ destOffset + 3 ] = ALPHABET[ (inBuff       ) & 0x3f ];
                return destination;
                
            case 2:
                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
                destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
                destination[ destOffset + 3 ] = EQUALS_SIGN;
                return destination;
                
            case 1:
                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
                destination[ destOffset + 2 ] = EQUALS_SIGN;
                destination[ destOffset + 3 ] = EQUALS_SIGN;
                return destination;
                
            default:
                return destination;
        }   // end switch
    }   // end encode3to4
    
    
    /**
     * Encodes a byte array into Base64 notation.
     *  
     * @param source The data to convert
     * @return The data in Base64-encoded form
     * @throws NullPointerException if source array is null
     */
    public static String encodeBytes( byte[] source ) {
        String encoded = null;

        try {
            encoded = encodeBytes(source, 0, source.length, NO_OPTIONS);
        } catch (IOException ex) {
            ex.getMessage();
        } 
        return encoded;
    }   
    
    /**
     * Encodes a byte array into Base64 notation.
     * <p>
     * Example options:<pre>
     *   BREAK_LINES: break lines at 76 characters
     *     <i>Note: Technically, this makes your encoding non-compliant.</i>
     * </pre>
     * <p>
     * Example: <code>encodeBytes( myData, Base64.NO_OPTIONS )</code> or
     * <p>
     * Example: <code>encodeBytes( myData, Base64.URL_SAFE | Base64.BREAK_LINES )</code>
     *
     * @param source The data to convert
     * @param options Specified options
     * @return The Base64-encoded data as a String
     * @see Base64#BREAK_LINES
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if source array is null
     */
    public static String encodeBytes( byte[] source, int options ) throws IOException {
        return encodeBytes( source, 0, source.length, options );
    }   // end encodeBytes
    
    
    /**
     * Encodes a byte array into Base64 notation. 
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @return The Base64-encoded data as a String
     * @throws NullPointerException if source array is null
     * @throws IllegalArgumentException if source array, offset, or length are invalid
     */
    public static String encodeBytes( byte[] source, int off, int len ) {
        
        String encoded = null;
        try {
            encoded = encodeBytes( source, off, len, NO_OPTIONS );
        } catch (IOException ex) {
            ex.getMessage();
        }   // end catch
        return encoded;
    }   // end encodeBytes

    /**
     * Encodes a byte array into Base64 notation.
     * <p>
     * Example options:<pre>
     *   BREAK_LINES: break lines at 76 characters
     *     <i>Note: Technically, this makes your encoding non-compliant.</i>
     * </pre>
     * <p>
     * Example: <code>encodeBytes( myData, Base64.NO_OPTIONS )</code> or
     * <p>
     * Example: <code>encodeBytes( myData, Base64.URL_SAFE | Base64.BREAK_LINES )</code>
     * 
     * 
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @param options Specified options
     * @return The Base64-encoded data as a String
     * @see Base64#BREAK_LINES
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if source array is null
     * @throws IllegalArgumentException if source array, offset, or length are invalid
     */
    public static String encodeBytes( byte[] source, int off, int len, int options ) throws IOException {
        byte[] encoded = encodeBytesToBytes( source, off, len, options );

        // Return value according to relevant encoding.
        return new String( encoded, StandardCharsets.US_ASCII );
    }   // end encodeBytes

    /**
     * Similar to {@link #encodeBytes(byte[])} but returns
     * a byte array instead of instantiating a String. This is more efficient
     * if you're working with I/O streams and have large data sets to encode.
     *
     *
     * @param source The data to convert
     * @return The Base64-encoded data as a byte[] (of ASCII characters)
     * @throws NullPointerException if source array is null
     */
    public static byte[] encodeBytesToBytes( byte[] source ) {
        byte[] encoded = null;
        try {
            encoded = encodeBytesToBytes( source, 0, source.length, Base64.NO_OPTIONS );
        } catch( IOException ex ) {
            ex.getMessage();
        }
        return encoded;
    }

    /**
     * Similar to {@link #encodeBytes(byte[], int, int, int)} but returns
     * a byte array instead of instantiating a String. This is more efficient
     * if you're working with I/O streams and have large data sets to encode.
     *
     *
     * @param source The data to convert
     * @param off Offset in array where conversion should begin
     * @param len Length of data to convert
     * @param options Specified options
     * @return The Base64-encoded data as a String
     * @see Base64#BREAK_LINES
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if source array is null
     * @throws IllegalArgumentException if source array, offset, or length are invalid
     */
    public static byte[] encodeBytesToBytes( byte[] source, int off, int len, int options ) throws IOException {

        if( source == null ){
            throw new NullPointerException( "Cannot serialize a null array." );
        }   // end if: null

        if( off < 0 ){
            throw new IllegalArgumentException( "Cannot have negative offset: " + off );
        }   // end if: off < 0

        if( len < 0 ){
            throw new IllegalArgumentException( "Cannot have length offset: " + len );
        }   // end if: len < 0

        if( off + len > source.length  ){
            throw new IllegalArgumentException(
            new String( "Cannot have offset of "+off+" and length of "+len+" with array of length "+source.length));
        }   // end if: off < 0

        // Else, don't compress. Better not to use streams at all then.
        
            boolean breakLines = (options & BREAK_LINES) > 0;

            //int    len43   = len * 4 / 3;
            //byte[] outBuff = new byte[   ( len43 )                      // Main 4:3
            //                           + ( (len % 3) > 0 ? 4 : 0 )      // Account for padding
            //                           + (breakLines ? ( len43 / MAX_LINE_LENGTH ) : 0) ]; // New lines
            // Try to determine more precisely how big the array needs to be.
            // If we get it right, we don't have to do an array copy, and
            // we save a bunch of memory.
            int encLen = ( len / 3 ) * 4 + ( len % 3 > 0 ? 4 : 0 ); // Bytes needed for actual encoding
            if( breakLines ){
                encLen += encLen / MAX_LINE_LENGTH; // Plus extra newline characters
            }
            byte[] outBuff = new byte[ encLen ];


            int d = 0;
            int e = 0;
            int len2 = len - 2;
            int lineLength = 0;
            for( ; d < len2; d+=3, e+=4 ) {
                encode3to4( source, d+off, 3, outBuff, e, options );

                lineLength += 4;
                if( breakLines && lineLength >= MAX_LINE_LENGTH )
                {
                    outBuff[e+4] = NEW_LINE;
                    e++;
                    lineLength = 0;
                }   // end if: end of line
            }   // en dfor: each piece of array

            if( d < len ) {
                encode3to4( source, d+off, len - d, outBuff, e, options );
                e += 4;
            }   // end if: some padding needed


            // Only resize array if we didn't guess it right.
            if( e < outBuff.length - 1 ){
                byte[] finalOut = new byte[e];
                System.arraycopy(outBuff,0, finalOut,0,e);
                //System.err.println("Having to resize array from " + outBuff.length + " to " + e );
                return finalOut;
            } else {
                //System.err.println("No need to resize array.");
                return outBuff;
            }
        
           // end else: don't compress

    }   // end encodeBytesToBytes
    
/* ********  D E C O D I N G   M E T H O D S  ******** */
    
    /**
     * Decodes four bytes from array <var>source</var>
     * and writes the resulting bytes (up to three of them)
     * to <var>destination</var>.
     * The source and destination arrays can be manipulated
     * anywhere along their length by specifying 
     * <var>srcOffset</var> and <var>destOffset</var>.
     * This method does not check to make sure your arrays
     * are large enough to accomodate <var>srcOffset</var> + 4 for
     * the <var>source</var> array or <var>destOffset</var> + 3 for
     * the <var>destination</var> array.
     * This method returns the actual number of bytes that 
     * were converted from the Base64 encoding.
	 * <p>This is the lowest level of the decoding methods with
	 * all possible parameters.</p>
     * 
     *
     * @param source the array to convert
     * @param srcOffset the index where conversion begins
     * @param destination the array to hold the conversion
     * @param destOffset the index where output will be put
	 * @param options alphabet type is pulled from this (standard, url-safe, ordered)
     * @return the number of decoded bytes converted
     * @throws NullPointerException if source or destination arrays are null
     * @throws IllegalArgumentException if srcOffset or destOffset are invalid
     *         or there is not enough room in the array.
     */
    private static int decode4to3( 
    byte[] source, int srcOffset, 
    byte[] destination, int destOffset, int options ) {
        
        // Lots of error checking and exception throwing
        if( source == null ){
            throw new NullPointerException( "Source array was null." );
        }   // end if
        if( destination == null ){
            throw new NullPointerException( "Destination array was null." );
        }   // end if
        if( srcOffset < 0 || srcOffset + 3 >= source.length ){
            throw new IllegalArgumentException( new String("Source array with length "+source.length+" cannot have offset of "+srcOffset+" and still process four bytes.") );
        }   // end if
        if( destOffset < 0 || destOffset +2 >= destination.length ){
            throw new IllegalArgumentException( new String("Destination array with length "+destination.length+" cannot have offset of "+destOffset+" and still store three bytes.") );
        }   // end if
        
        byte[] DECODABET = getDecodabet( options ); 
	
        if( source[ srcOffset + 2] == EQUALS_SIGN ) {
            int outBuff =   ( ( DECODABET[ source[ srcOffset    ] ] & 0xFF ) << 18 )
                          | ( ( DECODABET[ source[ srcOffset + 1] ] & 0xFF ) << 12 );
            destination[ destOffset ] = (byte)( outBuff >>> 16 );
            return 1;
        }
        
        else if( source[ srcOffset + 3 ] == EQUALS_SIGN ) {
            int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] & 0xFF ) << 18 )
                          | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
                          | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6 );    
            destination[ destOffset     ] = (byte)( outBuff >>> 16 );
            destination[ destOffset + 1 ] = (byte)( outBuff >>>  8 );
            return 2;
        }
        
        else {
            int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] & 0xFF ) << 18 )
                          | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
                          | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6)
                          | ( ( DECODABET[ source[ srcOffset + 3 ] ] & 0xFF )      );

            
            destination[ destOffset     ] = (byte)( outBuff >> 16 );
            destination[ destOffset + 1 ] = (byte)( outBuff >>  8 );
            destination[ destOffset + 2 ] = (byte)( outBuff       );

            return 3;
        }
    }  
    
    /**
     * Low-level access to decoding ASCII characters in
     * the form of a byte array. This is not generally a recommended method,
     * although it is used internally as part of the decoding process.
     * Special case: if len = 0, an empty array is returned. Still,
     * if you need more speed and reduced memory footprint consider this method.
     *
     * @param source The Base64 encoded data
     * @return decoded data
     */
    public static byte[] decode( byte[] source ){
        byte[] decoded = null;
        try {
            decoded = decode( source, 0, source.length, Base64.NO_OPTIONS );
        } catch( java.io.IOException ex ) {
            ex.getMessage();
        }
        return decoded;
    }
    
    /**
     * Low-level access to decoding ASCII characters in
     * the form of a byte array. This is not generally a recommended method,
     * although it is used internally as part of the decoding process.
     * Special case: if len = 0, an empty array is returned. Still,
     * if you need more speed and reduced memory footprint consider this method.
     *
     * @param source The Base64 encoded data
     * @param off    The offset of where to begin decoding
     * @param len    The length of characters to decode
     * @param options Can specify options such as alphabet type to use
     * @return decoded data
     * @throws java.io.IOException If bogus characters exist in source data
     */
    public static byte[] decode( byte[] source, int off, int len, int options )
    throws IOException {
        
        // Lots of error checking and exception throwing
        if( source == null ){
            throw new NullPointerException( "Cannot decode null source array." );
        }   // end if
        if( off < 0 || off + len > source.length ){
            throw new IllegalArgumentException( new String("Source array with length "+source.length+" cannot have offset of "+off+" and process "+len+" bytes." ) );
        }   // end if
        
        if( len == 0 ){
            return new byte[0];
        }else if( len < 4 ){
            throw new IllegalArgumentException( 
            "Base64-encoded string must have at least four characters, but length specified was " + len );
        }   // end if
        
        byte[] DECODABET = getDecodabet( options );
	
        int    len34   = len * 3 / 4;       // Estimate on array size
        byte[] outBuff = new byte[ len34 ]; // Upper limit on size of output
        int    outBuffPosn = 0;             // Keep track of where we're writing
        
        byte[] b4        = new byte[4];     // Four byte buffer from source, eliminating white space
        int    b4Posn    = 0;               // Keep track of four byte input buffer
        int    i         = 0;               // Source array counter
        byte   sbiCrop   = 0;               // Low seven bits (ASCII) of input
        byte   sbiDecode = 0;               // Special value from DECODABET
        
        for( i = off; i < off+len; i++ ) {  // Loop through source
            
            sbiCrop = (byte)(source[i] & 0x7f); // Only the low seven bits
            sbiDecode = DECODABET[ sbiCrop ];   // Special value
            
            // White space, Equals sign, or legit Base64 character
            // Note the values such as -5 and -9 in the
            // DECODABETs at the top of the file.
            if( sbiDecode >= WHITE_SPACE_ENC )  {
                if( sbiDecode >= EQUALS_SIGN_ENC ) {
                    b4[ b4Posn++ ] = sbiCrop;           // Save non-whitespace
                    if( b4Posn > 3 ) {                  // Time to decode?
                        outBuffPosn += decode4to3( b4, 0, outBuff, outBuffPosn, options );
                        b4Posn = 0;
                        
                        // If that was the equals sign, break out of 'for' loop
                        if( sbiCrop == EQUALS_SIGN ) {
                            break;
                        }   // end if: equals sign
                    }   // end if: quartet built
                }   // end if: equals sign or better
            }   // end if: white space, equals sign or better
            else {
                // There's a bad input character in the Base64 stream.
                throw new IOException( new String("Bad Base64 input character '"+source[i]+"' in array position "+ i ) );            }   // end else:
        }   // each input character
                                   
        byte[] out = new byte[ outBuffPosn ];
        System.arraycopy( outBuff, 0, out, 0, outBuffPosn ); 
        return out;
    }  

    /**
     * Decodes data from Base64 notation.
     *
     * @param s the string to decode
     * @return the decoded data
     * @throws java.io.IOException If there is a problem
     */
    public static byte[] decode( String s ) throws IOException {
        return decode( s, NO_OPTIONS );
    }

    
    
    /**
     * Decodes data from Base64 notation.
     *
     * @param s the string to decode
     * @param options encode options such as URL_SAFE
     * @return the decoded data
     * @throws java.io.IOException if there is an error
     * @throws NullPointerException if <tt>s</tt> is null
     */
    public static byte[] decode( String s, int options ) throws IOException {
        
        if( s == null ){
            throw new NullPointerException( "Input string was null." );
        }   // end if
        
        byte[] bytes;
        
        bytes = s.getBytes(StandardCharsets.US_ASCII);
        // Decode
        bytes = decode( bytes, 0, bytes.length, options );             
        return bytes;
    }   // end decode
    
    
    
} 
