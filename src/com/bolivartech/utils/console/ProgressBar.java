package com.bolivartech.utils.console;

import com.bolivartech.utils.environment.EnvironmentUtils;

/**
 * Copyright 2014 BolivarTech C.A.
 *
 * Implementa un rotor de Texto para indicar actividad en la consola, asi mismo
 * puede generar una barra de progreso en la consola
 *
 * @author Julian Bolivar
 * @since 2014 - March 23, 2014.
 * @version 1.0.0
 *
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v1.0.0 - New fancy console progress bar.</li>
 * </ul>
 */
public class ProgressBar {

    private int Progress;
    private int RotorPos;
    private int MaxChars;
    private boolean FirstTime;
    private char BarChar;
    private char[] Rotor;
    private long Plot_Start_Time, Plot_End_Time; // Valores utilizados para la animacion de la barra de progreso

    /**
     * Constructor por defecto con 79 Caracteres de longitud
     */
    public ProgressBar() {
//        String Encoding;

        RotorPos = 0;
        Progress = 0;
        MaxChars = 79;
        FirstTime = true;
        Plot_Start_Time = -1;
        Plot_End_Time = -1;
        /*        Encoding = EnvironmentUtils.getEncoding();
         if (Encoding.contains("cp1252")) {
         //BarChar = (char) 0xB2;
         //BarChar = '\u2593';
         BarChar = (char) '=';
         } else if ((Encoding.contains("iso_8859_1")) || (Encoding.contains("iso_8859_15"))) {
         //BarChar = (char) 0xBB;
         BarChar = (char) '=';
         } else if ((Encoding.contains("us_ascii")) || (Encoding.contains("ansi_x3.4-1968"))) {
         //BarChar = (char) 0x3E;
         BarChar = (char) '=';
         } else {
         //BarChar = '\u2593';
         BarChar = (char) '=';
         }  */
        BarChar = (char) '=';
        Rotor = new char[4];
        this.setStandarRotor();
    }

    /**
     * Establece el rotor estandar del sistema
     */
    public final void setStandarRotor() {
        Rotor[0] = '-';
        Rotor[1] = '\\';
        Rotor[2] = '|';
        Rotor[3] = '/';
    }

    /**
     * Establece un rotor Costumizado en NewRotor; NewRotor debe tener una
     * longitud de 4 caracteres, donde se implementa la animacion del rotor que
     * se va a realizar en la consola.
     *
     * @param NewRotor Arreglo de 4 caracteres con la animacion del rotor
     */
    public final void setCustomRotor(char[] NewRotor) {
        int i;

        if (NewRotor != null) {
            if (NewRotor.length == 4) {
                for (i = 0; i < 4; i++) {
                    this.Rotor[i] = NewRotor[i];
                }
            }
        }
    }

    /**
     * Reinicia la barra de progreso y la dibujada colocando el % de progreso en
     * 0.
     *
     * @return String para borrar barra de progreso en al consola.
     */
    public String ResetProgressBar() {
        int i;
        String Salida;

        FirstTime = true;
        Salida = "\r";
        Salida += DrawRotor() + " [";
        for (i = 0; i < this.MaxChars - 9; i++) {
            Salida += " ";
        }
        Salida += "] ";
        Salida += "0%  \r";
        return Salida;
    }

    /**
     * Establece la longitud maxima de caracteres en el terminal para la barra
     * de progreso de texto
     *
     * @param MaxChars 15&gt; MaxChars
     */
    public void setMaxChars(int MaxChars) {
        if (MaxChars > 15) {
            this.MaxChars = MaxChars;
        }
    }

    /**
     * Establece el caracter que se usa para imprimir la barra de progreso
     *
     * NOTA: El caracter es UTF-16
     *
     * @param BarChar
     */
    public void setBarChar(char BarChar) {
        this.BarChar = BarChar;
    }

    /**
     * Genera un simple rotor de texto almacenado en un String
     *
     * @return String con el codigo para generar el rotor
     */
    public String DrawRotor() {

        switch (RotorPos) {
            case 0:
                RotorPos++;
                return "\b" + this.Rotor[0];
            case 1:
                RotorPos++;
                return "\b" + this.Rotor[1];
            case 2:
                RotorPos++;
                return "\b" + this.Rotor[2];
            case 3:
                RotorPos = 0;
                return "\b" + this.Rotor[3];
            default:
                RotorPos = 0;
                return "\b" + this.Rotor[0];
        }
    }

    /**
     * Gerea un String que anima en la consola una barra de progreso de texto
     * basado en el porcentaje Porcent.
     *
     * @param Porcent Porcentaje de progreso 0&lt;= Percent &lt;=100
     * @return String con el codigo para animar la barra de progreso en la
     * consola
     */
    public String DrawProgressBar(int Porcent) {
        String Salida;
        int NumNewChars;
        int i;

        Salida = "";
        if ((Porcent >= 0) && (Porcent <= 100)) {
            if (this.FirstTime) {
                this.FirstTime = false;
                Salida = "\r";
                Salida += DrawRotor() + " [";
                for (i = 0; i < this.MaxChars - 9; i++) {
                    Salida += " ";
                }
                Salida += "] ";
                Salida += "0%  \r";
            } else if (Porcent != this.Progress) {
                // Actualiza la Barra
                Salida += this.DrawRotor();
                Salida += " [";
                // Calcula la cantidad de nuevos caracteres de la barra a agregar
                NumNewChars = ((int) ((Porcent * (this.MaxChars - 9)) / 100));
                // Imprime la Barra
                for (i = 0; i < NumNewChars; i++) {
                    Salida += this.BarChar;
                }
                if (NumNewChars < (this.MaxChars - 9)) {
                    Salida += ">";
                    NumNewChars++;
                    for (i = NumNewChars; i < (this.MaxChars - 9); i++) {
                        Salida += " ";
                    }
                }
                Salida += "] ";
                // Imprime en porcentaje de progreso
                this.Progress = Porcent;
                Salida += String.valueOf(this.Progress) + "%\r";
            } else {
                // Imprime Solo el Rotor
                Salida += this.DrawRotor();
            }
        }
        return Salida;
    }

    /**
     * Gerea un String que anima en la consola una barra de progreso de texto
     * basado en el porcentaje Porcent.
     *
     * El codigo de animacion se genera cuando han transcurrido mas de 0.03
     * segundos desde la ultima vez que se actualizo la barra de
     * progreso.
     *
     * &nbsp;&nbsp;String S = TR.PlotProgressBar((int) ((i * 100) / (j -
     * 1)));
     * &nbsp;&nbsp;if (!S.equals("")) {
     * &nbsp;&nbsp;&nbsp;&nbsp;System.out.print(S);
     * &nbsp;&nbsp;}
     *
     * @param Porcent Porcentaje de progreso 0&lt;= Percent &lt;=100
     * @return String con el codigo para animar la barra de progreso en la
     * consola o un string vacio si no se ha alcanzado el tiempo necesario.
     */
    public String PlotProgressBar(int Porcent) {
        String Salida;
        double difference;

        Salida = "";
        if (this.Plot_Start_Time == -1) {
            difference = 1;
        } else {
            this.Plot_End_Time = System.nanoTime();
            difference = (this.Plot_End_Time - this.Plot_Start_Time) / 1e9; // segundos transcurridos
        }
        if ((difference >= 0.03) || (Porcent == 100)) {
            Salida = DrawProgressBar(Porcent);
            this.Plot_Start_Time = System.nanoTime();
        }
        return Salida;
    }

    /**
     * Realiza el Reset del Timer que controla el plot de la Barra de Progreso
     * generada por la funcion PlotProgressBar, ocasionando la impresion de la
     * misma la proxima vez que se realize su llamado.
     */
    public void ResetTimerPlotProgressBar() {

        this.Plot_Start_Time = -1;
    }

    /**
     * Gerea un String que anima en la consola el rotor de texto para indicar
     * actividad.
     *
     * El codigo de animacion se genera cuando han transcurrido mas de 0.03
     * segundos desde la ultima vez que se actualizo el rotor de
     * actividad.
     *
     * &nbsp;&nbsp;String S = TR.PlotRotor();
     * &nbsp;&nbsp;if (!S.equals("")) {
     * &nbsp;&nbsp;&nbsp;&nbsp;System.out.print(S);
     * &nbsp;&nbsp;}
     *
     * @return String con el codigo para animar el rotor de actividad  en la
     * consola o un string vacio si no se ha alcanzado el tiempo necesario.
     */
    public String PlotRotor() {
        String Salida;
        double difference;

        Salida = "";
        if (this.Plot_Start_Time == -1) {
            difference = 1;
        } else {
            this.Plot_End_Time = System.nanoTime();
            difference = (this.Plot_End_Time - this.Plot_Start_Time) / 1e9; // segundos transcurridos
        }
        if (difference >= 0.03) {
            Salida = DrawRotor();
            this.Plot_Start_Time = System.nanoTime();
        }
        return Salida;
    }

    /**
     * Realiza el Reset del Timer que controla el plot del rotor de actividad
     * generado por la funcion PlotRotor, ocasionando la impresion del  mismo
     * la proxima vez que se realize su llamado.
     */
    public void ResetTimerPlotRotor() {

        this.Plot_Start_Time = -1;
    }

}
