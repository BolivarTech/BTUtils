package com.bolivartech.utils.sort;

import com.bolivartech.utils.exception.UtilsException;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015,2016 BolivarTech C.A.
 *
 * <p>
 * Homepage:
 * <a href="http://www.cuaimacrypt.com">http://www.cuaimacrypt.com</a>.</p>
 * <p>
 * BolivarTech Homepage:
 * <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's Sorter.
 *
 * Esta clase maneja el ordenamiento ascendente o descendente de un arreglo de
 * objetos que desciendan de la clase Sorteable y que implementen el metodo
 * Compare.
 *
 * Esta clase es del tipo no instanseable
 * 
 * Class ID: "35DGFHJ"
 * Loc: 000-002
 *
 * @author Julian Bolivar
 * @since 2007 - March 25, 2016.
 * @version 2.1.1
 * 
 * Change Logs: 
 * v1.0.0 (2007-02-01) Version Inicial.
 * v2.0.0 (2011-12-11) Se corrigieron pequeños bugs.
 * v2.1.0 (2015-09-18) Se verifica si la lista pasada no es null.
 * v2.1.1 (2016-03-25) Se agrego el codigo de localizacion para la excepcion y bitacoras.
 */
public class Sorter {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFHJ";

    // Error la lista es NULL
    public static final int ERRORLISTISNULL = 0x01;

    /**
     * Indica si el orden de la lista es ascendente
     */
    public final static boolean ASCENDING = true;
    /**
     * Indica si el orden de la lista es descendente
     */
    public final static boolean DESCENDING = false;

    private Sorter() {
    }

    /**
     * Realiza el ordenamiento por el QuickSoft Ascendente Absoluto
     */
    private static void AbsoluteAscendQuickSort(Sortable Lista[], int IndexLo, int IndexHi) {
        long ValorMedio;
        int InLo;
        int InHi;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ValorMedio = Lista[(IndexHi + IndexLo) / 2].Metrica();

            // loop through the array until indices cross
            while (InLo <= InHi) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while ((InLo < IndexHi) && (Lista[InLo].Metrica() < ValorMedio)) {
                    ++InLo;
                }

                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while ((InHi > IndexLo) && (Lista[InHi].Metrica() > ValorMedio)) {
                    --InHi;
                }

                // if the indexes have not crossed, swap
                if (InLo <= InHi) {
                    Swap(Lista, InLo, InHi);
                    ++InLo;
                    --InHi;
                }
            }
            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if (IndexLo < InHi) {
                AbsoluteAscendQuickSort(Lista, IndexLo, InHi);
            }
            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if (InLo < IndexHi) {
                AbsoluteAscendQuickSort(Lista, InLo, IndexHi);
            }
        }
    }

    /**
     * Realiza el ordenamiento por el QuickSoft Descendente Absoluto
     */
    private static void AbsoluteDescendQuickSort(Sortable Lista[], int IndexLo, int IndexHi) {
        long ValorMedio;
        int InLo;
        int InHi;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ValorMedio = Lista[(IndexHi + IndexLo) / 2].Metrica();

            // loop through the array until indices cross
            while (InLo <= InHi) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while ((InLo < IndexHi) && (Lista[InLo].Metrica() > ValorMedio)) {
                    ++InLo;
                }

                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while ((InHi > IndexLo) && (Lista[InHi].Metrica() < ValorMedio)) {
                    --InHi;
                }

                // if the indexes have not crossed, swap
                if (InLo <= InHi) {
                    Swap(Lista, InLo, InHi);
                    ++InLo;
                    --InHi;
                }
            }
            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if (IndexLo < InHi) {
                AbsoluteDescendQuickSort(Lista, IndexLo, InHi);
            }
            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if (InLo < IndexHi) {
                AbsoluteDescendQuickSort(Lista, InLo, IndexHi);
            }
        }
    }

    /**
     * Realiza el ordenamiento por el QuickSoft Ascendente Relativo
     */
    private static void RelativeAscendQuickSort(Sortable Lista[], int IndexLo, int IndexHi) {
        int InLo;
        int InHi;
        Sortable Media;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            Media = Lista[(IndexLo + IndexHi) / 2];
            while (InLo <= InHi) {
                while ((Lista[InLo].Order(Media) < 0) && (InLo < IndexHi)) {
                    InLo++;
                }
                while ((Lista[InHi].Order(Media) > 0) && (InHi > IndexLo)) {
                    InHi--;
                }
                if (InLo <= InHi) {
                    Swap(Lista, InLo, InHi);
                    InLo++;
                    InHi--;
                }
            }
            if (IndexLo < InHi) {
                RelativeAscendQuickSort(Lista, IndexLo, InHi);
            }
            if (InLo < IndexHi) {
                RelativeAscendQuickSort(Lista, InLo, IndexHi);
            }
        }
    }

    /**
     * Realiza el ordenamiento por el QuickSoft Descendente Relativo
     */
    private static void RelativeDescendQuickSort(Sortable Lista[], int IndexLo, int IndexHi) {
        int InLo;
        int InHi;
        Sortable Media;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            Media = Lista[(IndexLo + IndexHi) / 2];
            while (InLo <= InHi) {
                while ((Lista[InLo].Order(Media) > 0) && (InLo < IndexHi)) {
                    InLo++;
                }
                while ((Lista[InHi].Order(Media) < 0) && (InHi > IndexLo)) {
                    InHi--;
                }
                if (InLo <= InHi) {
                    Swap(Lista, InLo, InHi);
                    InLo++;
                    InHi--;
                }
            }
            if (IndexLo < InHi) {
                RelativeDescendQuickSort(Lista, IndexLo, InHi);
            }
            if (InLo < IndexHi) {
                RelativeDescendQuickSort(Lista, InLo, IndexHi);
            }
        }
    }

    /**
     * Realiza el ordenamiento por el QuickSoft Ascendente Absoluto basado en
     * comparacion de doubles
     */
    private static void AbsoluteAscendQuickSortDouble(Sortable Lista[], int IndexLo, int IndexHi) {
        double ValorMedio;
        int InLo;
        int InHi;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ValorMedio = Lista[(IndexHi + IndexLo) / 2].MetricaDouble();

            // loop through the array until indices cross
            while (InLo <= InHi) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while ((InLo < IndexHi) && (Lista[InLo].MetricaDouble() < ValorMedio)) {
                    ++InLo;
                }
                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while ((InHi > IndexLo) && (Lista[InHi].MetricaDouble() > ValorMedio)) {
                    --InHi;
                }

                // if the indexes have not crossed, swap
                if (InLo <= InHi) {
                    Swap(Lista, InLo, InHi);
                    ++InLo;
                    --InHi;
                }
            }
            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if (IndexLo < InHi) {
                AbsoluteAscendQuickSortDouble(Lista, IndexLo, InHi);
            }
            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if (InLo < IndexHi) {
                AbsoluteAscendQuickSortDouble(Lista, InLo, IndexHi);
            }
        }
    }

    /**
     * Realiza el ordenamiento por el QuickSoft Descendente Absoluto basado en
     * comparacion de double.
     */
    private static void AbsoluteDescendQuickSortDouble(Sortable Lista[], int IndexLo, int IndexHi) {
        double ValorMedio;
        int InLo;
        int InHi;

        InLo = IndexLo;
        InHi = IndexHi;
        if (IndexHi > IndexLo) {
            /* Arbitrarily establishing partition element as the midpoint of
             * the array.
             */
            ValorMedio = Lista[(IndexHi + IndexLo) / 2].MetricaDouble();

            // loop through the array until indices cross
            while (InLo <= InHi) {
                /* find the first element that is greater than or equal to
                 * the partition element starting from the left Index.
                 */
                while ((InLo < IndexHi) && (Lista[InLo].MetricaDouble() > ValorMedio)) {
                    ++InLo;
                }
                /* find an element that is smaller than or equal to
                 * the partition element starting from the right Index.
                 */
                while ((InHi > IndexLo) && (Lista[InHi].MetricaDouble() < ValorMedio)) {
                    --InHi;
                }

                // if the indexes have not crossed, swap
                if (InLo <= InHi) {
                    Swap(Lista, InLo, InHi);
                    ++InLo;
                    --InHi;
                }
            }
            /* If the right index has not reached the left side of array
             * must now sort the left partition.
             */
            if (IndexLo < InHi) {
                AbsoluteDescendQuickSortDouble(Lista, IndexLo, InHi);
            }
            /* If the left index has not reached the right side of array
             * must now sort the right partition.
             */
            if (InLo < IndexHi) {
                AbsoluteDescendQuickSortDouble(Lista, InLo, IndexHi);
            }
        }
    }

    /**
     * Cambia de posibion los archivos en la posicion 'i' y 'j'
     */
    private static void Swap(Sortable a[], int i, int j) {
        Sortable T;

        T = a[i];
        a[i] = a[j];
        a[j] = T;
    }

    /**
     * Ordena la lista de Sortables basada en la metrica de valor absoluto
     * implementada en el metodo Metrica.
     *
     * @param Lista
     * @param Ascendente
     * @throws com.bolivartech.utils.exception.UtilsException
     *
     */
    public static void AbsoluteSort(Sortable Lista[], boolean Ascendente) throws UtilsException {
        if (Lista != null) {
            if (Ascendente) {
                AbsoluteAscendQuickSort(Lista, 0, Lista.length - 1);
            } else {
                AbsoluteDescendQuickSort(Lista, 0, Lista.length - 1);
            }
        } else {
            throw new UtilsException("Can't Sort elements because List is NULL", Sorter.ERRORLISTISNULL,Sorter.CLASSID+"000");
        }
    }

    /**
     * Ordena la lista de Sortables basada en la metrica de valor absoluto
     * double implementada en el metodo MetricaDouble.
     *
     * @param Lista
     * @param Ascendente
     * @throws com.bolivartech.utils.exception.UtilsException
     */
    public static void AbsoluteSortDouble(Sortable Lista[], boolean Ascendente) throws UtilsException {
        if (Lista != null) {
            if (Ascendente) {
                AbsoluteAscendQuickSortDouble(Lista, 0, Lista.length - 1);
            } else {
                AbsoluteDescendQuickSortDouble(Lista, 0, Lista.length - 1);
            }
        } else {
            throw new UtilsException("Can't Sort elements because List is NULL", Sorter.ERRORLISTISNULL,Sorter.CLASSID+"001");
        }
    }

    /**
     * Ordena la lista de Sortables basada en la metrica de valor relativo
     * implementada en el metodo Order.
     *
     * @param Lista
     * @param Ascendente
     * @throws com.bolivartech.utils.exception.UtilsException
     */
    public static void RelativeSort(Sortable Lista[], boolean Ascendente) throws UtilsException {
        if (Lista != null) {
            if (Ascendente) {
                RelativeAscendQuickSort(Lista, 0, Lista.length - 1);
            } else {
                RelativeDescendQuickSort(Lista, 0, Lista.length - 1);
            }
        } else {
            throw new UtilsException("Can't Sort elements because List is NULL", Sorter.ERRORLISTISNULL,Sorter.CLASSID+"002");
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
}
