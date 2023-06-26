package com.bolivartech.utils.random.sparkers;

import com.bolivartech.utils.bits.BitsUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.log.LoggerFormatter;
import com.bolivartech.utils.log.LoggerManager;
import com.bolivartech.utils.math.MathUtil;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Copyright 2014,2015,2016 BolivarTech INC.</p>
 *
 * <p>
 * Implementa el Sparker para generadores de Numeros aleatorios basado en una
 * clave de texto introducida</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHR"</li>
 * <li>Loc: 000-001</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2014 - March 25, 2016.
 * @version 1.2.1
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v1.2.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v1.2.0 (2015-07-22) Se realiza un redondeo de 11 decimales en los sparks
 * para compatibilidad con Android.</li>
 * <li>v1.1.0 (2015-07-21) Se utiliza UTF-8 para convertir el String en
 * bytes.</li>
 * </ul>
 */
public strictfp class PasswordSparker implements Sparker {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHR";

    private double[] Sparks;
    private int SparkPos;
    private int PasswordOK;  // Almacena la bandera si el password cumple o no
    private LoggerFormatter BTLogF;
    private int NumSparks = 40;  // Numero de Sparks a generar

    /**
     * Verifica si el password cumple con las condiciones de longitud y de
     * caracteres en MAYUSCULA, minuscula, numeros y caracteres de puntuacion.
     * La funcion almacena en PasswordOK: 0 el password cumple -1 longitud del
     * password es menor a 9 caracteres -2 el passord no contiene los tipos de
     * caracteres solicitados.
     *
     * PasswordOK: 0 el password cumple. -1 longitud del password es menor a 1
     * caracteres. -2 el passord no contiene los tipos de caracteres
     * solicitados.
     *
     * @param passw
     */
    private void PasswordCumple(String passw) {
        int longitud, minlong, salida;

        salida = 0;
        minlong = 1;
        longitud = passw.length();
        if (longitud < minlong) {
            // Busca minusculas
            salida = -1;
        }
        this.PasswordOK = salida;
    }

    /**
     * Recibe un byte y por combinatoria lo expande hasta formar un arreglo de
     * 56 bytes
     *
     * @param Input Byte a expandir
     * @return Arreglo de 56 bytes expandido.
     * @throws UtilsException
     */
    private byte[] ByteExpansor(byte Input) throws UtilsException {
        byte[] Salida;
        int i, j;
        long[] WalshCodes;

        WalshCodes = new long[128];
        WalshCodes[0] = 1085350949055100000L;
        WalshCodes[1] = -3074457345618260000L;
        WalshCodes[2] = -5534023222112870000L;
        WalshCodes[3] = 7378697629483820000L;
        WalshCodes[4] = -8138269444283630000L;
        WalshCodes[5] = 6510615555426900000L;
        WalshCodes[6] = 4340410370284600000L;
        WalshCodes[7] = -1627653888856720000L;
        WalshCodes[8] = -9151594822560190000L;
        WalshCodes[9] = 6172840429334710000L;
        WalshCodes[10] = 3732415143318660000L;
        WalshCodes[11] = -1830318964512040000L;
        WalshCodes[12] = 1148435428713440000L;
        WalshCodes[13] = -2691645536047110000L;
        WalshCodes[14] = -4844961964884800000L;
        WalshCodes[15] = 7608384715226510000L;
        WalshCodes[16] = -9223090566172970000L;
        WalshCodes[17] = 6149008514797120000L;
        WalshCodes[18] = 3689517697151000000L;
        WalshCodes[19] = -1844618113234590000L;
        WalshCodes[20] = 1085350949055100000L;
        WalshCodes[21] = -2712673695933230000L;
        WalshCodes[22] = -4882812652679810000L;
        WalshCodes[23] = 7595767819294840000L;
        WalshCodes[24] = 72056494543077100L;
        WalshCodes[25] = -3050438514103900000L;
        WalshCodes[26] = -5490789325387020000L;
        WalshCodes[27] = 7393108928392440000L;
        WalshCodes[28] = -8074690184392680000L;
        WalshCodes[29] = 6531808642057220000L;
        WalshCodes[30] = 4378557926219170000L;
        WalshCodes[31] = -1614938036878540000L;
        WalshCodes[32] = -9223372032559810000L;
        WalshCodes[33] = 6148914692668170000L;
        WalshCodes[34] = 3689348817318890000L;
        WalshCodes[35] = -1844674406511960000L;
        WalshCodes[36] = 1085102596360830000L;
        WalshCodes[37] = -2712756480164650000L;
        WalshCodes[38] = -4882961664296370000L;
        WalshCodes[39] = 7595718148755990000L;
        WalshCodes[40] = 71777218556133100L;
        WalshCodes[41] = -3050531606099550000L;
        WalshCodes[42] = -5490956890979190000L;
        WalshCodes[43] = 7393053073195050000L;
        WalshCodes[44] = -8074936604381160000L;
        WalshCodes[45] = 6531726502061060000L;
        WalshCodes[46] = 4378410074226080000L;
        WalshCodes[47] = -1614987320876230000L;
        WalshCodes[48] = 281474976645120L;
        WalshCodes[49] = -3074363520626040000L;
        WalshCodes[50] = -5533854337126880000L;
        WalshCodes[51] = 7378753924479150000L;
        WalshCodes[52] = -8138021084010120000L;
        WalshCodes[53] = 6510698342184740000L;
        WalshCodes[54] = 4340559386448710000L;
        WalshCodes[55] = -1627604216802020000L;
        WalshCodes[56] = -9151315538050290000L;
        WalshCodes[57] = 6172933524171350000L;
        WalshCodes[58] = 3732582714024600000L;
        WalshCodes[59] = -1830263107610060000L;
        WalshCodes[60] = 1148681856222170000L;
        WalshCodes[61] = -2691563393544200000L;
        WalshCodes[62] = -4844814108379560000L;
        WalshCodes[63] = 7608434000728250000L;
        WalshCodes[64] = -9223372036854775808L;
        WalshCodes[65] = 6148914691236520000L;
        WalshCodes[66] = 3689348814741910000L;
        WalshCodes[67] = -1844674407370960000L;
        WalshCodes[68] = 1085102592571150000L;
        WalshCodes[69] = -2712756481427880000L;
        WalshCodes[70] = -4882961666570180000L;
        WalshCodes[71] = 7595718147998050000L;
        WalshCodes[72] = 71777214294589700L;
        WalshCodes[73] = -3050531607520060000L;
        WalshCodes[74] = -5490956893536110000L;
        WalshCodes[75] = 7393053072342740000L;
        WalshCodes[76] = -8074936608141340000L;
        WalshCodes[77] = 6531726500807660000L;
        WalshCodes[78] = 4378410071969970000L;
        WalshCodes[79] = -1614987321628270000L;
        WalshCodes[80] = 281470681808895L;
        WalshCodes[81] = -3074363522057660000L;
        WalshCodes[82] = -5533854339703780000L;
        WalshCodes[83] = 7378753923620180000L;
        WalshCodes[84] = -8138021087799680000L;
        WalshCodes[85] = 6510698340921550000L;
        WalshCodes[86] = 4340559384174970000L;
        WalshCodes[87] = -1627604217559940000L;
        WalshCodes[88] = -9151315542311700000L;
        WalshCodes[89] = 6172933522750880000L;
        WalshCodes[90] = 3732582711467760000L;
        WalshCodes[91] = -1830263108462340000L;
        WalshCodes[92] = 1148681852462100000L;
        WalshCodes[93] = -2691563394797560000L;
        WalshCodes[94] = -4844814110635600000L;
        WalshCodes[95] = 7608433999976240000L;
        WalshCodes[96] = 4294967295L;
        WalshCodes[97] = -3074457344186600000L;
        WalshCodes[98] = -5534023219535890000L;
        WalshCodes[99] = 7378697630342810000L;
        WalshCodes[100] = -8138269440493950000L;
        WalshCodes[101] = 6510615556690130000L;
        WalshCodes[102] = 4340410372558410000L;
        WalshCodes[103] = -1627653888098790000L;
        WalshCodes[104] = -9151594818298640000L;
        WalshCodes[105] = 6172840430755230000L;
        WalshCodes[106] = 3732415145875590000L;
        WalshCodes[107] = -1830318963659730000L;
        WalshCodes[108] = 1148435432473620000L;
        WalshCodes[109] = -2691645534793720000L;
        WalshCodes[110] = -4844961962628690000L;
        WalshCodes[111] = 7608384715978550000L;
        WalshCodes[112] = -9223090561878130000L;
        WalshCodes[113] = 6149008516228730000L;
        WalshCodes[114] = 3689517699727900000L;
        WalshCodes[115] = -1844618112375630000L;
        WalshCodes[116] = 1085350952844660000L;
        WalshCodes[117] = -2712673694670040000L;
        WalshCodes[118] = -4882812650406070000L;
        WalshCodes[119] = 7595767820052750000L;
        WalshCodes[120] = 72056498804490500L;
        WalshCodes[121] = -3050438512683430000L;
        WalshCodes[122] = -5490789322830170000L;
        WalshCodes[123] = 7393108929244720000L;
        WalshCodes[124] = -8074690180632600000L;
        WalshCodes[125] = 6531808643310570000L;
        WalshCodes[126] = 4378557928475210000L;
        WalshCodes[127] = -1614938036126520000L;
        Salida = new byte[56];
        Salida[0] = Input;
        // Realiza la rotacion de la entrada para generar los primeros 8 bytes expandidos
        for (i = 1; i < 8; i++) {
            Salida[i] = (byte) ((byte) (BitsUtils.ByteLeftRotation((byte) WalshCodes[(Salida[i - 1] & 0x0F)], i)) ^ (byte) BitsUtils.ByteRightRotation(Salida[0], i));
        }
        // Realiza la combinatoria de los Xor
        for (i = 0; i < 7; i++) {
            for (j = i + 1; j < 8; j++) {
                Salida[(int) ((i + 1) * (7 - (0.5 * i)) + j)] = (byte) ((BitsUtils.ByteLeftRotation(Salida[i], j) ^ BitsUtils.ByteRightRotation(Salida[j], i)) ^ (BitsUtils.ByteLeftRotation((byte) WalshCodes[(Salida[(int) ((i + 1) * (7 - (0.5 * i)) + j - 1)] & 0x0F)], i)));
            }
        }
        for (i = 0; i < 5; i++) {
            for (j = i + 2; j < 8; j++) {
                Salida[(int) ((i + 1) * (6 - (0.5 * i)) + j + 28)] = (byte) ((((BitsUtils.ByteLeftRotation(Salida[(int) ((i + 1) * (6 - (0.5 * i)) + j + 7)], 2) & 0xAA) | (BitsUtils.ByteRightRotation(Salida[i], 2)) & 0x55)) ^ ((byte) WalshCodes[(Salida[(int) ((i + 1) * (6 - (0.5 * i)) + j + 11)] & 0x0F)]));
            }
        }
        return Salida;
    }

    /**
     * Inicializa los Sparks del sistema
     *
     * @param passw
     */
    private void InitPassword(String passw) {
        byte[] cpassw;
        int Plongitud;
        int i, f, c;
        byte[][] Expanded;

        PasswordCumple(passw);
        if (this.PasswordOK == 0) {
            this.SparkPos = 0;
            // Procesa el Password Introducido
            //Plongitud = passw.length();
            cpassw = passw.getBytes(StandardCharsets.UTF_8);
            Plongitud = cpassw.length;
            // Inicializa en entorno de las Sparks
            Expanded = new byte[Plongitud][56];
            // Realia la expansion del password
            for (i = 0; i < Plongitud; i++) {
                try {
                    Expanded[i] = this.ByteExpansor(cpassw[i]);
                } catch (UtilsException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, PasswordSparker.CLASSID, "000");
                }
            }
            // Genera el Espacio para los Sparks
            Sparks = new double[NumSparks];
            for (i = 0; i < NumSparks; i++) {
                Sparks[i] = 0;
            }
            i = 0;
            f = 0;
            c = 0;
            do {
                if ((c != 0) && (c % 2 == 0)) {
                    Sparks[i] += Math.pow(Math.PI, (1 / (double) c)) * (double) Expanded[f][c];
                    if (f % 3 != 0) {
                        Sparks[i] *= -0.1;
                    }
                } else if ((f != 0) && (c % 2 != 0)) {
                    Sparks[i] += Math.pow(Math.PI, (1 / (double) f)) * (double) Expanded[f][c];
                    if (f % 3 != 0) {
                        Sparks[i] *= -0.1;
                    }
                } else {
                    Sparks[i] += Math.pow(Math.PI, (double) (1 / 2)) * (double) Expanded[f][c];
                    if (f % 3 != 0) {
                        Sparks[i] *= -0.1;
                    }
                }
                i++;
                c++;
                f++;
                if (i >= this.NumSparks) {
                    i = 0;
                }
                if (f >= Expanded.length) {
                    f = 0;
                }
                if (c >= Expanded[0].length) {
                    c = 0;
                }
            } while ((c != 0) || (f != 0));
            // Redondea los decimales a 11 para compatibilidad con Android
            for (i = 0; i < this.NumSparks; i++) {
                //Sparks[i] = Double.longBitsToDouble(Double.doubleToRawLongBits(Sparks[i])&0xFFFFFFFFFFFFFFF0L);
                try {
                    Sparks[i] = MathUtil.roundToDecimals(Sparks[i], 11);
                } catch (UtilsException ex) {
                    this.BTLogF.LogMsg(LoggerManager.TYPE_ERROR, LoggerManager.LEVEL_ERROR, false, ex, null, PasswordSparker.CLASSID, "001");
                }
            }
        }
    }

    /**
     * Constructor por defecto
     */
    public PasswordSparker() {
        PasswordOK = -1;
        Sparks = null;
        this.BTLogF = LoggerFormatter.getInstance(null);
    }

    /**
     * Constructor con inicializacion por medio del password password.
     *
     * @param Password Password para inicializar
     * @param Log Apuntador al manerador de Logs, si es null se usa el general
     * del JAVA
     *
     */
    public PasswordSparker(String Password, LoggerManager Log) {
        PasswordOK = -1;
        this.BTLogF = LoggerFormatter.getInstance(Log);
        Sparks = null;
        this.InitPassword(Password);
    }

    /**
     * Retorna si el password cumple con las caracteristicas de cantidad y tipo
     * de caracteres asi como la longitud.
     *
     * @return 0 el password cumple. -1 longitud del password es menor a 1
     * caracteres. -2 el passord no contiene los tipos de caracteres
     * solicitados.
     */
    public int PasswordOK() {
        return this.PasswordOK;
    }

    @Override
    public double getDoubleSpark() {
        double Salida;

        Salida = this.Sparks[this.SparkPos];
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        return Salida;
    }

    @Override
    public float getFloatSpark() {
        float Salida;

        Salida = (float) this.Sparks[this.SparkPos];
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        return Salida;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getLongSpark() {
        long Salida;

        Salida = Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida ^= ~Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        return Salida;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIntegerSpark() {
        long Salida;

        Salida = Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida ^= ~Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida = (Salida & 0x7FFFFFFF000L) >>> 12;
        return (int) Salida;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public short getShortSpark() {
        long Salida;

        Salida = Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida ^= ~Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida = (Salida & 0x7FFF000L) >>> 12;
        return (short) Salida;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte getByteSparl() {
        long Salida;

        Salida = Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida ^= ~Double.doubleToRawLongBits(this.Sparks[this.SparkPos]);
        this.SparkPos++;
        if (this.SparkPos >= this.NumSparks) {
            this.SparkPos = 0;
        }
        Salida = (Salida & 0x7F000L) >>> 12;
        return (byte) Salida;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
