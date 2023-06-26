package com.bolivartech.utils.array;

/**
 * Copyright 2007,2009,2010,2011,2012,2013,2014,2015 BolivarTech C.A.
 *
 * <p>
 * BolivarTech Homepage:
 * <a href="http://www.bolivartech.com">http://www.bolivartech.com</a>.</p>
 *
 * This Class is the BolivarTech's util for Array Data Managment.
 *
 * Realiza operaciones sobre arreglos canonicos de Java 
 *
 * @author Julian Bolivar
 * @since 2007 - March 01, 2015.
 * @version 1.2.0
 *
 * Change Logs: v1.0.0 Version Inicial. v1.1.0 (2011-04-10) Se agrego la funcion
 * de Trim v1.2.0 (2015-03-01) Se agrego la funcion de Defrag
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Realiza la creacion de un nuevo arreglo de un nuevo tamaño y copia el
     * contenido del arreglo viejo al nuevo.
     *
     * @param oldArray el arreglo originar a ser redimensionado.
     * @param newSize el nuevo tamaño del arreglo.
     * @return el arreglo con el nuevo tamaño y el contenido original.
     */
    public static Object resizeArray(Object oldArray, int newSize) {
        int oldSize;
        int preserveLength;
        Class elementType;
        Object newArray;

        oldSize = java.lang.reflect.Array.getLength(oldArray);
        elementType = oldArray.getClass().getComponentType();
        newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
        preserveLength = Math.min(oldSize, newSize);
        if (preserveLength > 0) {
            System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
        }
        return newArray;
    }

    /**
     * Realiza la creacion de un nuevo arreglo conteniendo un segmento del del
     * arreglo viejo.
     *
     * @param oldArray el arreglo originar.
     * @param org el punto de origen de donde se inicia el substring
     * @param length el numero de elementos a extraer
     * @return el arreglo con el nuevo substring del original.
     */
    public static Object subArray(Object oldArray, int org, int length) {
        int oldSize;
        Class elementType;
        Object newArray = null;

        oldSize = java.lang.reflect.Array.getLength(oldArray);
        elementType = oldArray.getClass().getComponentType();
        if (org < oldSize) {
            if ((oldSize - org) < length) {
                length = oldSize - org;
            }
            newArray = java.lang.reflect.Array.newInstance(elementType, length);
            System.arraycopy(oldArray, org, newArray, 0, length);
        }
        return newArray;
    }

    /**
     * Realiza la copia segura del arreglo src al arreglo dest, se preservan los
     * tamaños pero se protege el hacer una copia fuera de rango
     *
     * @param src Arreglo de Origen
     * @param srcPos Posicion desde donde se hace la copia en el arreglo de
     * Origen
     * @param dest Arreglo de Destino
     * @param destPos Posicion hacia donde se hace la copia en el arreglo de
     * Destino
     * @param length Numero de elementos a copias desde el Origen al Destino
     */
    @SuppressWarnings("unchecked")
    public static void arrayCopy(Object src, int srcPos, Object dest, int destPos, int length) {
        int srcSize;
        int destSize;
        int preserveLength;
        Class srcElementType;
        Class destElementType;

        if ((src != null) && (dest != null)) {
            srcElementType = src.getClass().getComponentType();
            destElementType = dest.getClass().getComponentType();
            if (destElementType.isAssignableFrom(srcElementType)) {
                srcSize = java.lang.reflect.Array.getLength(src) - srcPos;
                destSize = java.lang.reflect.Array.getLength(dest) - destPos;
                preserveLength = Math.min(srcSize, destSize);
                preserveLength = Math.min(preserveLength, length);
                if (preserveLength > 0) {
                    System.arraycopy(src, srcPos, dest, destPos, preserveLength);
                }
            }
        }
    }

    /**
     * Realiza la eliminacion de todos los elementos null dentro del arreglo y
     * lo redimensiona para la nueva cantidad de elementos.
     *
     * El orden que tenian los elementos originales se presenvan.
     *
     * @param oldArray Arreglo a ser ajustado
     * @return Arreglo ajustado sin los elementos null
     */
    public static Object arrayTrim(Object[] oldArray) {
        int posA, posB;
        Class elementType;
        Object newArray;

        elementType = oldArray.getClass().getComponentType();
        posA = oldArray.length;
        posB = 0;
        while ((posB < oldArray.length) && (oldArray[posB] != null)) {
            posB++;
        }
        // Verifica si encontro un null en la lista
        if (posB < oldArray.length) {
            posA = posB;
            while (posB < oldArray.length) {
                // Se salta los nulls que encontro seguidos
                while ((posB < oldArray.length) && (oldArray[posB] == null)) {
                    posB++;
                }
                // Copia los elementos que estan despues del null
                if (posB < oldArray.length) {
                    while ((posB < oldArray.length) && (oldArray[posB] != null)) {
                        oldArray[posA] = oldArray[posB];
                        oldArray[posB] = null;
                        posA++;
                        posB++;
                    }
                }
            }
        }
        // Realiza la copia del array y su redimensionamiento
        newArray = java.lang.reflect.Array.newInstance(elementType, posA);
        System.arraycopy(oldArray, 0, newArray, 0, posA);
        return newArray;
    }

    /**
     * Realiza la defragmentacion de todos los elementos dentro del arreglo
     * moviendolos al principio del arreglo y los null hacial el
     * final.
     *
     * El orden que tenian los elementos originales se presenvan.
     *
     * @param OrgArray Arreglo a ser defragmentado
     * @return La posicion de la primera posicion NULL en el array defragmentado 
     *         o -1 si no hay nulls en el arreglo.
     */
    public static int arrayDefrag(Object[] OrgArray) {
        int posA, posB;

        posA = -1;
        posB = 0;
        while ((posB < OrgArray.length) && (OrgArray[posB] != null)) {
            posB++;
        }
        // Verifica si encontro un null en la lista
        if (posB < OrgArray.length) {
            posA = posB;
            while (posB < OrgArray.length) {
                // Se salta los nulls que encontro seguidos
                while ((posB < OrgArray.length) && (OrgArray[posB] == null)) {
                    posB++;
                }
                // Copia los elementos que estan despues del null
                if (posB < OrgArray.length) {
                    while ((posB < OrgArray.length) && (OrgArray[posB] != null)) {
                        OrgArray[posA] = OrgArray[posB];
                        OrgArray[posB] = null;
                        posA++;
                        posB++;
                    }
                }
            }
        }
        return posA;
    }
}
