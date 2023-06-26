package com.bolivartech.utils.sort;

/**
 * Copyright 2007,2009,2010,2011 BolivarTech C.A.
 *
 *  <p>Homepage: <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 *  <p>BolivarTech Homepage: <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 *   This Class is the BolivarTech's Sortable interface.
 *
 *   Esta interface establece el marco para que un objeto sea ordenable en
 *   base a un criterio en el cual debe retorna una metrica en relacion a los otros
 *   la cual debe ser capaz de permitir el establecer el orden de cada elemento dentro
 *   del arreglo.
 *
 *   
 *
 * @author Julian Bolivar
 * @since 2007 - December 11, 2011.
 * @version 2.0.0
 */
public interface Sortable {

    /**
     * Metodo Abstracto que establece el valor de ordenamiento absoluto del elemento
     *
     * @return indice de ordenamiento absotulo
     */
    public abstract long Metrica();
    
    /**
     * Metodo Abstracto que establece el valor de ordenamiento absoluto del elemento basado
     * en una variable del tipo double
     *
     * @return indice de ordenamiento absotulo basado en doubles
     */
    public abstract double MetricaDouble();
    
    /**
     * Metodo Abstracto que compara dos elementos y retorna -1 si es menor al "Other", 0
     * si es igual y 1 si es mayo que "Other"
     *
     * @param Other
     * @return -1 si menor, 0 si igual y 1 si mayor
     */
    public abstract int Order(Sortable Other);


}
