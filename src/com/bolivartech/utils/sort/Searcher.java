package com.bolivartech.utils.sort;

import com.bolivartech.utils.exception.UtilsException;

/**
 * <p>
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015,2016 BolivarTech INC</p>
 *
 * <p>
 * Homepage:
 * <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage:
 * <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * <p>
 * This Class is the BolivarTech's Searcher.</p>
 *
 * <p>
 * Esta clase maneja la busqueda de un elemento Sortable en un arreglo ordenado
 * ascendentemente por la clase Sorter, dependiendo si el elemento se ordeno de
 * forma absoluta o relativa.</p>
 *
 * <p>
 * Esta clase es del tipo no instanseable</p>
 *
 * <ul>
 * <li>Class ID: "35DGFHK"</li>
 * <li>Loc: 000-002</li>
 * </ul>
 *
 * @author Julian Bolivar
 * @since 2007 - May 19, 2016.
 * @version 2.2.2
 *
 * <p>
 * Change Logs:</p>
 * <ul>
 * <li>v1.0.0 (2007-02-01) Version Inicial.</li>
 * <li>v2.0.0 (2011-12-11) Se corrigieron pequeños bugs.</li>
 * <li>v2.1.0 (2015-09-18) Se verifica si la lista pasada no es null y el buscar
 * el listas sin importar el orden.</li>
 * <li>v2.2.0 (2015-26-18) Se agrego la capacidad de especificar modos de
 * busquedas.</li>
 * <li>v2.2.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion
 * y bitacoras.</li>
 * <li>v2.2.2 (2016-05-19) Se corrigio error en la busqueda por celling y por
 * floor en los expremos del arreglo.</li>
 * </ul>
 */
public class Searcher {

    // Codigo de identificacion de la clase
    private static final String CLASSID = "35DGFHK";

    // Error la lista es NULL
    public static final int ERRORLISTISNULL = 0x01;

    /**
     * Indica que no se encontro el elemento buscado
     */
    public static final int ITEMNOTFOUND = -1;

    /**
     * Indica que se va a utilizar el busqueda Exacta de la Unimatrix
     */
    public final static int SEARCHEXACT = 0x00;
    /**
     * Indica que se va a utilizar el busqueda del valor mas proximo de la
     * Unimatrix, puede ser hacia arriba o hacia abajo
     */
    public final static int SEARCHNEAR = 0x02;
    /**
     * Indica que se va a utilizar el busqueda del valor mas proximo hacia abajo
     * de la Unimatrix
     */
    public final static int SEARCHFLOOR = 0x08;
    /**
     * Indica que se va a utilizar el busqueda del valor mas proximo hacia
     * arriba de la Unimatrix
     */
    public final static int SEARCHCEILING = 0x10;

    /**
     * Constructor privado
     */
    private Searcher() {
    }

    /**
     * Busca el elemento especificado en "searchItem" por su metodo "Metrica()"
     * retornando la posicion del elemento encontrado o ITEMNOTFOUND si no
     * encontro ninguno que satisfaga la condicion.
     *
     * El Modo de busqueda puede ser
     * SEARCHEXACT,SEARCHNEAR,SEARCHFLOOR,SEARCHCEILING
     *
     * @param Lista Arreglo de elementos donde realizar la busqueda
     * @param searchItem Elemento a buscar segun su metodo Metrica()
     * @param SearchMode Modo de busqueda.
     * @return Posicion del elemento encontrado o ITEMNOTFOUND si no consiguio
     * ninguna coincidencia
     * @throws UtilsException
     */
    public static int AbsoluteSearch(Sortable Lista[], Sortable searchItem, int SearchMode) throws UtilsException {
        int first;
        int last;
        int mid;
        int found = ITEMNOTFOUND;
        boolean Ascending;
        long diff, diffP, diffM, TempA, TempB;

        if (Lista != null) {
            first = 0;
            mid = 0;
            last = Lista.length - 1;
            if (last > first) {
                // Verifica si esta ordenado de forma ascendente o descendente
                if (Lista[last].Metrica() >= Lista[first].Metrica()) {
                    Ascending = true;
                } else {
                    Ascending = false;
                }
            } else {
                Ascending = true;
            }
            //Realiza un loop buscando el elemento
            while ((first <= last) && (found == ITEMNOTFOUND)) {
                //Busca el medio de la lista
                mid = (first + last) / 2;
                // Compara le medio de la lista con el elemento buscado
                if (Lista[mid].Metrica() == searchItem.Metrica()) {
                    found = mid;
                } else if (Ascending) {
                    if (Lista[mid].Metrica() > searchItem.Metrica()) {
                        // El elemento buscado esta por la parte baja del arreglo
                        last = mid - 1;
                    } else {
                        // El elemento buscado esta por la parte alta del arreglo
                        first = mid + 1;
                    }
                } else if (Lista[mid].Metrica() < searchItem.Metrica()) {
                    // El elemento buscado esta por la parte baja del arreglo
                    last = mid - 1;
                } else {
                    // El elemento buscado esta por la parte alta del arreglo
                    first = mid + 1;
                }
            }
            // Realiza el ajuste del modo de busqueda
            if (found == ITEMNOTFOUND) {
                if (SearchMode == SEARCHNEAR) {
                    diff = Math.abs(Lista[mid].Metrica() - searchItem.Metrica());
                    if (mid > 0) {
                        diffM = Math.abs(Lista[mid - 1].Metrica() - searchItem.Metrica());
                    } else {
                        diffM = -1;
                    }
                    if (mid < (Lista.length - 1)) {
                        diffP = Math.abs(Lista[mid + 1].Metrica() - searchItem.Metrica());
                    } else {
                        diffP = -1;
                    }
                    if (diffP >= 0) {
                        TempA = Math.min(diff, diffP);
                    } else {
                        TempA = diff;
                    }
                    if (diffM >= 0) {
                        TempB = Math.min(diff, diffM);
                    } else {
                        TempB = diff;
                    }
                    TempA = Math.min(TempA, TempB);
                    if (TempA == diff) {
                        found = mid;
                    } else if (TempA == diffM) {
                        found = mid - 1;
                    } else if (TempA == diffP) {
                        found = mid + 1;
                    }
                } else if (SearchMode == SEARCHFLOOR) {
                    if (Ascending) {
                        if (mid >= 0) {
                            while ((mid < Lista.length - 1) && (Lista[mid].Metrica() < searchItem.Metrica())) {
                                mid++;
                            }
                            while ((mid > 0) && (Lista[mid].Metrica() > searchItem.Metrica())) {
                                mid--;
                            }
                            if (Lista[mid].Metrica() < searchItem.Metrica()) {
                                found = mid;
                            } else {
                                found = ITEMNOTFOUND;
                            }
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else if (mid <= (Lista.length - 1)) {
                        while ((mid > 0) && (Lista[mid].Metrica() < searchItem.Metrica())) {
                            mid--;
                        }
                        while ((mid < Lista.length - 1) && (Lista[mid].Metrica() > searchItem.Metrica())) {
                            mid++;
                        }
                        if (Lista[mid].Metrica() < searchItem.Metrica()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (SearchMode == SEARCHCEILING) {
                    if (Ascending) {
                        if (mid <= (Lista.length - 1)) {
                            while ((mid > 0) && (Lista[mid].Metrica() > searchItem.Metrica())) {
                                mid--;
                            }
                            while ((mid < Lista.length - 1) && (Lista[mid].Metrica() < searchItem.Metrica())) {
                                mid++;
                            }
                            if (Lista[mid].Metrica() > searchItem.Metrica()) {
                                found = mid;
                            } else {
                                found = ITEMNOTFOUND;
                            }
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else if (mid >= 0) {
                        while ((mid < Lista.length - 1) && (Lista[mid].Metrica() > searchItem.Metrica())) {
                            mid++;
                        }
                        while ((mid > 0) && (Lista[mid].Metrica() < searchItem.Metrica())) {
                            mid--;
                        }
                        if (Lista[mid].Metrica() > searchItem.Metrica()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                }
            }
        } else {
            throw new UtilsException("Can't find element because List is NULL", Searcher.ERRORLISTISNULL, Searcher.CLASSID + "000");
        }
        return found;
    }

    /**
     * Busca el elemento especificado en "searchItem" por su metodo
     * "MetricaDouble()" retornando la posicion del elemento encontrado o
     * ITEMNOTFOUND si no encontro ninguno que satisfaga la condicion.
     *
     * El Modo de busqueda puede ser
     * SEARCHEXACT,SEARCHNEAR,SEARCHFLOOR,SEARCHCEILING
     *
     * @param Lista Arreglo de elementos donde realizar la busqueda
     * @param searchItem Elemento a buscar segun su metodo MetricaDouble()
     * @param SearchMode Modo de busqueda.
     * @return Posicion del elemento encontrado o ITEMNOTFOUND si no consiguio
     * ninguna coincidencia
     * @throws UtilsException
     */
    public static int AbsoluteSearchDouble(Sortable Lista[], Sortable searchItem, int SearchMode) throws UtilsException {
        int first;
        int last;
        int mid;
        int found = ITEMNOTFOUND;
        boolean Ascending;
        double diff, diffP, diffM, TempA, TempB;

        if (Lista != null) {
            first = 0;
            mid = 0;
            last = Lista.length - 1;
            if (last > first) {
                // Verifica si esta ordenado de forma ascendente o descendente
                if (Lista[last].MetricaDouble() >= Lista[first].MetricaDouble()) {
                    Ascending = true;
                } else {
                    Ascending = false;
                }
            } else {
                Ascending = true;
            }
            //Realiza un loop buscando el elemento
            while ((first <= last) && (found == ITEMNOTFOUND)) {
                //Busca el medio de la lista
                mid = (first + last) / 2;
                // Compara le medio de la lista con el elemento buscado
                if (Lista[mid].MetricaDouble() == searchItem.MetricaDouble()) {
                    found = mid;
                } else if (Ascending) {
                    if (Lista[mid].MetricaDouble() > searchItem.MetricaDouble()) {
                        // El elemento buscado esta por la parte baja del arreglo
                        last = mid - 1;
                    } else {
                        // El elemento buscado esta por la parte alta del arreglo
                        first = mid + 1;
                    }
                } else if (Lista[mid].MetricaDouble() < searchItem.MetricaDouble()) {
                    // El elemento buscado esta por la parte baja del arreglo
                    last = mid - 1;
                } else {
                    // El elemento buscado esta por la parte alta del arreglo
                    first = mid + 1;
                }
            }
            // Realiza el ajuste del modo de busqueda
            if (found == ITEMNOTFOUND) {
                if (SearchMode == SEARCHNEAR) {
                    diff = Math.abs(Lista[mid].MetricaDouble() - searchItem.MetricaDouble());
                    if (mid > 0) {
                        diffM = Math.abs(Lista[mid - 1].MetricaDouble() - searchItem.MetricaDouble());
                    } else {
                        diffM = -1;
                    }
                    if (mid < (Lista.length - 1)) {
                        diffP = Math.abs(Lista[mid + 1].MetricaDouble() - searchItem.MetricaDouble());
                    } else {
                        diffP = -1;
                    }
                    if (diffP >= 0) {
                        TempA = Math.min(diff, diffP);
                    } else {
                        TempA = diff;
                    }
                    if (diffM >= 0) {
                        TempB = Math.min(diff, diffM);
                    } else {
                        TempB = diff;
                    }
                    TempA = Math.min(TempA, TempB);
                    if (TempA == diff) {
                        found = mid;
                    } else if (TempA == diffM) {
                        found = mid - 1;
                    } else if (TempA == diffP) {
                        found = mid + 1;
                    }
                } else if (SearchMode == SEARCHFLOOR) {
                    if (Ascending) {
                        if (mid >= 0) {
                            while ((mid < Lista.length - 1) && (Lista[mid].MetricaDouble() < searchItem.MetricaDouble())) {
                                mid++;
                            }
                            while ((mid > 0) && (Lista[mid].MetricaDouble() > searchItem.MetricaDouble())) {
                                mid--;
                            }
                            if (Lista[mid].MetricaDouble() < searchItem.MetricaDouble()) {
                                found = mid;
                            } else {
                                found = ITEMNOTFOUND;
                            }
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else if (mid <= (Lista.length - 1)) {
                        while ((mid > 0) && (Lista[mid].MetricaDouble() < searchItem.MetricaDouble())) {
                            mid--;
                        }
                        while ((mid < Lista.length - 1) && (Lista[mid].MetricaDouble() > searchItem.MetricaDouble())) {
                            mid++;
                        }
                        if (Lista[mid].MetricaDouble() < searchItem.MetricaDouble()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (SearchMode == SEARCHCEILING) {
                    if (Ascending) {
                        if (mid <= (Lista.length - 1)) {
                            while ((mid > 0) && (Lista[mid].MetricaDouble() > searchItem.MetricaDouble())) {
                                mid--;
                            }
                            while ((mid < Lista.length - 1) && (Lista[mid].MetricaDouble() < searchItem.MetricaDouble())) {
                                mid++;
                            }
                            if (Lista[mid].MetricaDouble() > searchItem.MetricaDouble()) {
                                found = mid;
                            } else {
                                found = ITEMNOTFOUND;
                            }
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else if (mid >= 0) {
                        while ((mid < Lista.length - 1) && (Lista[mid].MetricaDouble() > searchItem.MetricaDouble())) {
                            mid++;
                        }
                        while ((mid > 0) && (Lista[mid].MetricaDouble() < searchItem.MetricaDouble())) {
                            mid--;
                        }
                        if (Lista[mid].MetricaDouble() > searchItem.MetricaDouble()) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                }
            }
        } else {
            throw new UtilsException("Can't find element because List is NULL", Searcher.ERRORLISTISNULL, Searcher.CLASSID + "001");
        }
        return found;
    }

    /**
     * Busca el elemento especificado en "searchItem" por su metodo "Order()"
     * retornando la posicion del elemento encontrado o ITEMNOTFOUND si no
     * encontro ninguno aque satisfaga la condicion.
     *
     * El Modo de busqueda puede ser SEARCHEXACT,SEARCHFLOOR,SEARCHCEILING
     *
     * @param Lista Arreglo de elementos donde realizar la busqueda
     * @param searchItem Elemento a buscar segun su metodo Order()
     * @param SearchMode Modo de Busqueda.
     * @return Posicion del elemento encontrado o ITEMNOTFOUND si no consiguio
     * ninguna coincidencia
     * @throws UtilsException
     */
    public static int RelativeSearch(Sortable Lista[], Sortable searchItem, int SearchMode) throws UtilsException {
        int first;
        int last;
        int mid;
        int Orden;
        int found = ITEMNOTFOUND;
        boolean Ascending;

        if (Lista != null) {
            first = 0;
            mid = 0;
            last = Lista.length - 1;
            if (last > first) {
                // Verifica si esta ordenado de forma ascendente o descendente
                Orden = Lista[last].Order(Lista[first]);
                if (Orden > 0) {
                    Ascending = true;
                } else {
                    Ascending = false;
                }
            } else {
                Ascending = true;
            }
            //Realiza un loop buscando el elemento
            while ((first <= last) && (found == ITEMNOTFOUND)) {
                //Busca el medio de la lista
                mid = (first + last) / 2;
                // Compara le medio de la lista con el elemento buscado
                Orden = Lista[mid].Order(searchItem);
                if (Orden == 0) {
                    found = mid;
                } else if (Ascending) {
                    if (Orden > 0) {
                        // El elemento buscado esta por la parte baja del arreglo
                        last = mid - 1;
                    } else {
                        // El elemento buscado esta por la parte alta del arreglo
                        first = mid + 1;
                    }
                } else if (Orden < 0) {
                    // El elemento buscado esta por la parte baja del arreglo
                    last = mid - 1;
                } else {
                    // El elemento buscado esta por la parte alta del arreglo
                    first = mid + 1;
                }
            }
            // Realiza el ajuste del modo de busqueda
            if (found == ITEMNOTFOUND) {
                if (SearchMode == SEARCHFLOOR) {
                    if (Ascending) {
                        if (mid >= 0) {
                            Orden = Lista[mid].Order(searchItem);
                            while ((mid < Lista.length - 1) && (Orden < 0)) {
                                mid++;
                                Orden = Lista[mid].Order(searchItem);
                            }
                            while ((mid > 0) && (Orden > 0)) {
                                mid--;
                                Orden = Lista[mid].Order(searchItem);
                            }
                            if (Orden < 0) {
                                found = mid;
                            } else {
                                found = ITEMNOTFOUND;
                            }
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else if (mid <= (Lista.length - 1)) {
                        Orden = Lista[mid].Order(searchItem);
                        while ((mid > 0) && (Orden < 0)) {
                            mid--;
                            Orden = Lista[mid].Order(searchItem);
                        }
                        while ((mid < Lista.length - 1) && (Orden > 0)) {
                            mid++;
                            Orden = Lista[mid].Order(searchItem);
                        }
                        if (Orden < 0) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                } else if (SearchMode == SEARCHCEILING) {
                    if (Ascending) {
                        if (mid <= (Lista.length - 1)) {
                            Orden = Lista[mid].Order(searchItem);
                            while ((mid > 0) && (Orden > 0)) {
                                mid--;
                                Orden = Lista[mid].Order(searchItem);
                            }
                            while ((mid < Lista.length - 1) && (Orden < 0)) {
                                mid++;
                                Orden = Lista[mid].Order(searchItem);
                            }
                            if (Orden > 0) {
                                found = mid;
                            } else {
                                found = ITEMNOTFOUND;
                            }
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else if (mid >= 0) {
                        Orden = Lista[mid].Order(searchItem);
                        while ((mid < Lista.length - 1) && (Orden > 0)) {
                            mid++;
                            Orden = Lista[mid].Order(searchItem);
                        }
                        while ((mid > 0) && (Orden < 0)) {
                            mid--;
                            Orden = Lista[mid].Order(searchItem);
                        }
                        if (Orden > 0) {
                            found = mid;
                        } else {
                            found = ITEMNOTFOUND;
                        }
                    } else {
                        found = ITEMNOTFOUND;
                    }
                }
            }
        } else {
            throw new UtilsException("Can't find element because List is NULL", Searcher.ERRORLISTISNULL, Searcher.CLASSID + "002");
        }
        return found;
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
