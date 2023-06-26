package com.bolivartech.utils.exception;

/**
 * <p>Copyright 2012,2013,2014,2015,2016 BolivarTech INC.</p>
 *
 * <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>This Class is the BolivarTech Utils Errors Exception Manager.</p>
 *
 * @author Julian Bolivar
 * @since 2012 - March 25, 2016.
 * @version 1.1.0
 * 
 * <p>Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2012-01-15) Version Inicial.</li>
 * <li>v1.1.0 (2016-03-25) Se agrego el codigo de localizacion para la excepcion.</li>
 * </ul>
 */

public class UtilsException extends Exception {
    
    // Codigo de Error
    private final int ErrorCode;
    // Codigo de identificacion de la excepcion
    private final String LocalizationCode;
    
    /**
     * Constructor con mensaje de error
     *
     * @param message de error
     *
    public UtilsException(String message) {
        super(message);
        this.ErrorCode=0;
        this.Localization=0;
    } */

    /**
     * Constructor con mensaje de error, codigo de error e ID de identificacion
     * de la excepcion.
     *
     * NOTA: el Codigo de Identificacion esta formado por 10 caracteres,
     * donde los primeros 7 son el idenficador del clase y los ultimos 3
     * es el idenficiador de ubicacion de la execcion.
     * 
     * @param message de error
     * @param ErrorCode codigo de error
     * @param IDCode Codigo de Identificacion de 10 caracteres para la excepcion
     */
    public UtilsException(String message,int ErrorCode,String IDCode) {
        super(message);
        this.ErrorCode=ErrorCode;
        this.LocalizationCode = IDCode;
    }

    /**
     * Retorna el codigo de error de la excepcion
     *
     * @return ErrorCode
     */
    public int getErrorCode() {
        return ErrorCode;
    }
    
    /**
     * Retorna el codigo de identificacion de 10 caracteres para la excepccion
     * 
     * @return Codigo de identificacion de 10 caracteres
     */
    public String getIDCode(){
        
        return this.LocalizationCode;
    }

    /**
     * Retorna el codigo identificacion de la clase de 7 caracterers contenidos 
     * dentro de codigo de identificacion de la excepcion.
     * 
     * @return Codigo de identificacion de la clase de 7 caracteres.
     */
    public String getClassID(){
      String Result = null;
        
      if((this.LocalizationCode!=null)&&(this.LocalizationCode.length()==10)){
          Result = this.LocalizationCode.substring(0,7);
      }
      return Result;
    }
    
    
    /**
     * Retorna el codigo de Localizacion de 3 caracteres para la ubicacion de la excepcion.
     *
     * @return Codigo de localizacion de 3 caracteres.
     */
    public String getLocCode() {
        String Result = null;
        
      if((this.LocalizationCode!=null)&&(this.LocalizationCode.length()==10)){
          Result = this.LocalizationCode.substring(7,10);
      }
      return Result;
    }
}

