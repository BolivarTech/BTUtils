package com.bolivartech.utils.data.containers;

import com.bolivartech.utils.array.Unimatrix;
import com.bolivartech.utils.bits.BitsUtils;
import com.bolivartech.utils.exception.UtilsException;
import com.bolivartech.utils.random.KAOSrand;
import com.bolivartech.utils.random.MersenneTwisterPlus;
import com.bolivartech.utils.random.sparkers.PasswordSparker;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright 2015 BolivarTech LLC
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * This Class is the BolivarTech's util that implement BolivarTech's HashMap
 * data structure.
 *
 *
 * Implementa una clase que define la estructura de datos de un HashMap.
 *
 *
 * NOTA: Key debe implementar los metodos hashCode() y toString() para el
 * calculo del hash de la llave.
 *
 * Clase Tread Safe.
 *
 * @author Julian Bolivar
 * @version 2.0.0
 * @since 2015 - December 21, 2015
 *
 * Change Logs: 
 * v1.0.0 (10/14/2015): Version Inicial.
 * v2.0.0 (12/21/2015): Se agrego el soporte para concurrencia.
 *
 * @param <Key> Llave de busqueda en el HashMap
 * @param <Value> Valor asociado a la llave
 */
public final class BTHashMap<Key, Value> {

    // Lock para el manejo de concurrencia
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Capacidad Inicial del Hash
    private static final long INIT_CAPACITY = 16;

    // Mascara de capacidad maxima de un Long
    private static final long LONGLENGMASK = 0x7FFFFFFFFFFFFFFFL;

    // totally anti-symmetric quasigroup matrix
    private static final int[][] antisymmetric
            = {{0, 3, 1, 7, 5, 9, 8, 6, 4, 2},
            {7, 0, 9, 2, 1, 5, 4, 8, 6, 3},
            {4, 2, 0, 6, 8, 7, 1, 3, 5, 9},
            {1, 7, 5, 0, 9, 8, 3, 4, 2, 6},
            {6, 1, 2, 3, 0, 4, 5, 9, 7, 8},
            {3, 6, 7, 4, 2, 0, 9, 5, 8, 1},
            {5, 8, 6, 9, 7, 2, 0, 1, 3, 4},
            {8, 9, 4, 5, 3, 6, 2, 0, 1, 7},
            {9, 4, 3, 8, 6, 1, 7, 2, 0, 5},
            {2, 5, 8, 1, 4, 3, 6, 7, 9, 0}};

    private long N;           // Numero de pares Llave-Valor en la tabla de simbolos
    private long M;           // Tamaño de la tabla de simbolos
    private Unimatrix keys;      // Las llaves
    private Unimatrix vals;    // los Valores
    private long Salt;        // Sal usada para calcular los Hash

    /**
     * Constructor por defecto
     */
    public BTHashMap() {
        this(INIT_CAPACITY);
    }

    /**
     * Constructor con inicializacion de la capacidad del HashMap
     *
     * @param capacity La capacidad inicial del HashMap
     */
    public BTHashMap(long capacity) {
        MersenneTwisterPlus random;

        random = new MersenneTwisterPlus();
        this.N = 0;
        this.M = capacity;
        this.Salt = random.nextLong63();
        this.keys = new Unimatrix(M);
        this.vals = new Unimatrix(M);
    }

    /**
     * Constructor de Copiado
     *
     * @param Other Otra BTHashMap a copiar dentro del nuevo BTHashMap
     */
    public BTHashMap(BTHashMap Other) {
        this(INIT_CAPACITY);

        int i;

        if (Other != null) {
            try {
                this.resize(Other.M);
                for (i = 0; i < Other.M; i++) {
                    if (Other.keys.getElement(i) != null) {
                        this.put((Key) Other.keys.getElement(i), (Value) Other.vals.getElement(i));
                    }
                }
            } catch (UtilsException ex) {
                Logger.getLogger(BTHashMap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Retorna el numero de pares Llave-Valor contenidos en el BTHashMap
     *
     * @return Numero de pares Llave-Valor contenidos en el BTHashMap
     */
    public long size() {
        long Result = 0;

        this.lock.readLock().lock();
        try {
            Result = this.N;
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Verifica si la BTHashMap esta vacio, retornando TRUE si lo esta o FALSE si
 no.
     *
     * @return TRUE si esta vacio o FALSE si no.
     */
    public boolean isEmpty() {
        boolean Result = false;

        this.lock.readLock().lock();
        try {
            Result = (this.N == 0);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Verifica si la tabla contiene la llave 'key', retornando TRUE si esta
     * contenida o FALSE si no
     *
     * @param key Llave a verificar
     * @return TRUE si la llave esta contenida o FALSE si no
     * @throws UtilsException Excepcion de busqueda de la llave
     */
    public boolean contains(Key key) throws UtilsException {
        boolean Result = false;

        if (key != null) {
            Result = (this.get(key) != null);
        }
        return Result;
    }

    /**
     * Calcula el numero Damm del number.
     *
     * @param number Numero a calcularle el Damm
     * @return Valor Damm de number.
     */
    private Long calculateDamm(Long number) {
        Long Result;
        String strNumber;
        int[] Digits;
        int interim, i;

        Result = null;
        number = Math.abs(number);
        strNumber = number.toString();
        Digits = new int[strNumber.length()];
        for (i = 0; i < Digits.length; i++) {
            Digits[i] = Integer.parseInt(strNumber.substring(i, i + 1));
        }
        interim = 0;
        for (i = 0; i < Digits.length; i++) {
            interim = antisymmetric[interim][Digits[i]];
        }
        Result = new Long(interim);
        return Result;
    }

    /**
     * Funcion de Hash que retorna un valor entre 0 y M-1
     *
     * @param key Llave a buscar
     * @param i indice del Hash
     * @return Valor entre 0 y M-1
     */
    private long hash(Key key, long i) throws UtilsException {
        long k, Result, Damm, kHash;
        KAOSrand Scrambler;

        kHash = key.hashCode();
        Scrambler = new KAOSrand(new PasswordSparker(key.toString(), null));
        k = kHash;
        Result = kHash;
        Damm = 7;
        while (Damm > 0) {
            k = BitsUtils.LongRightRotation(k, 5) ^ Scrambler.nextLong63();
            Result = BitsUtils.LongLeftRotation(Result, 9) ^ Scrambler.nextLong63();
            kHash = kHash ^ (k ^ Result);
            Damm--;
        }
        k = (((kHash ^ Scrambler.nextLong63()) ^ this.Salt) & LONGLENGMASK);
        Damm = calculateDamm(k);
        Damm = (Damm == 0 ? 1 : Damm);
        Result = ((k % M) + i * (i + (k % (M - Damm)))) % M;
        return Result;
    }

    /**
     * Realiza el redimensionamiento de la tabla a la capacidad especificada por
 'capacity' y ademas realiza el re-hashing de todas las llaves que estaban
 contenidas en el BTHashMap.
     *
     * @param capacity Nueva capacidad de la tabla de BTHashMap
     */
    private void resize(long capacity) throws UtilsException {
        long i;
        BTHashMap<Key, Value> temp;

        temp = new BTHashMap<Key, Value>(capacity);
        i = 0;
        while (i < this.M) {
            if (this.keys.getElement(i) != null) {
                temp.put((Key) this.keys.getElement(i), (Value) this.vals.getElement(i));
            }
            i++;
        }
        this.Salt = temp.Salt;
        this.keys = temp.keys;
        this.vals = temp.vals;
        this.M = temp.M;
    }

    /**
     * Inserta un par Llave-Valor en el BTHashMap, si la llave esta definida se
 sobreescribe el valor viejo en la tabla. Si el Valor es NULL se borra la
     * llave de la tabla.
     *
     * @param key La llave a insertar en la tabla
     * @param val El valor asociado con la llave
     * @throws UtilsException Excepcion de agregado de la llave-valor
     */
    public void put(Key key, Value val) throws UtilsException {

        if (key != null) {
            if (val != null) {
                this.lock.writeLock().lock();
                try {
                    this.private_put(key, val);
                } finally {
                    this.lock.writeLock().unlock();
                }
            } else {
                this.delete(key);
            }
        }
    }

    /**
     * Inserta un par Llave-Valor en el BTHashMap, si la llave esta definida se
 sobreescribe el valor viejo en la tabla. Si el Valor es NULL se borra la
     * llave de la tabla.
     *
     * @param key La llave a insertar en la tabla
     * @param val El valor asociado con la llave
     * @throws UtilsException Excepcion de agregado de la llave-valor
     */
    private void private_put(Key key, Value val) throws UtilsException {
        long i, h;
        boolean NotFound;

        if (key != null) {
            if (val != null) {
                // double table size if 98% full
                if (this.N >= ((29 * this.M) / 32)) {
                    this.resize((5 * this.M) / 4);
                }
                i = 0;
                h = this.hash(key, i);
                if (this.keys.getElement(h) != null) {
                    NotFound = true;
                    while ((NotFound) && (this.keys.getElement(h) != null)) {
                        if (this.keys.getElement(h).equals(key)) {
                            this.vals.setElement(h, val);
                            NotFound = false;
                        } else {
                            if (i >= (2 * this.M)) {
                                this.resize((5 * this.M) / 4);
                                i = -1;
                            }
                            i++;
                            h = this.hash(key, i);
                        }
                    }
                    if (NotFound) {
                        this.keys.setElement(h, key);
                        this.vals.setElement(h, val);
                        this.N++;
                    }
                } else {
                    this.keys.setElement(h, key);
                    this.vals.setElement(h, val);
                    this.N++;
                }
            } else {
                this.private_delete(key);
            }
        }
    }

    /**
     * Retorna el valor asociado con la llave 'key'.
     *
     * @param key Llave para buscar el valor asociado
     * @return El valor asociado con la llave o NULL si la llave no esta
     * definida en la tabla
     * @throws UtilsException Excepcion de la recuperacion del valor
     */
    public Value get(Key key) throws UtilsException {
        long i, h;
        boolean NotFound;
        Value Result = null;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                i = 0;
                h = this.hash(key, i);
                NotFound = true;
                while ((NotFound) && (this.keys.getElement(h) != null) && (i < (2 * this.M))) {
                    if (this.keys.getElement(h).equals(key)) {
                        Result = (Value) this.vals.getElement(h);
                        NotFound = false;
                    } else {
                        i++;
                        h = this.hash(key, i);
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Elimina la llave y el valor asociado en el BTHashMap.
     *
     * @param key Llave a eliminar del BTHashMap
     * @throws UtilsException Excepcion de borrado de la llave
     */
    public void delete(Key key) throws UtilsException {

        if (key != null) {
            this.lock.writeLock().lock();
            try {
                this.private_delete(key);
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    /**
     * Elimina la llave y el valor asociado en el BTHashMap.
     *
     * @param key Llave a eliminar del BTHashMap
     * @throws UtilsException Excepcion de borrado de la llave
     */
    private void private_delete(Key key) throws UtilsException {
        long i, h;
        Key keyToRehash;
        Value valToRehash;

        if (key != null) {
            // find position i of key
            i = 0;
            h = this.hash(key, i);
            while ((this.keys.getElement(h) != null) && (!key.equals(this.keys.getElement(h))) && (i < (2 * this.M))) {
                i++;
                h = this.hash(key, i);
            }
            if (key.equals(this.keys.getElement(h))) {
                // delete key and associated value
                this.keys.setElement(h, null);
                this.vals.setElement(h, null);
                // Realiza el Rehash de las siguientes llaves en el cluster
                i++;
                h = this.hash(key, i);
                while (this.keys.getElement(h) != null) {
                    keyToRehash = (Key) this.keys.getElement(h);
                    valToRehash = (Value) this.vals.getElement(h);
                    this.keys.setElement(h, null);
                    this.vals.setElement(h, null);
                    this.N--;
                    this.private_put(keyToRehash, valToRehash);
                    i++;
                    h = this.hash(key, i);
                }
                this.N--;
                // halves size of array if it's 12.5% full or less
                if ((this.N > 0) && (this.N <= (this.M / 4))) {
                    resize((this.M / 2) > INIT_CAPACITY ? (this.M / 2) : INIT_CAPACITY);
                }
            }
            // Verifica y trata de reparar cualquier inconsistencia en la tabla
            if (!this.check()) {
                this.resize((5 * this.M) / 4);
            }
        }
    }

    /**
     * Retorna un BTDLList con todas la llaves contenidas en el BTHashMap.
     *
     * @return Todas las llaves contenidas en el BTHashMap
     * @throws UtilsException Excepcion en la recuperacion de la lista de llaves
     */
    public BTDLList<Key> getKeys() throws UtilsException {
        long i;
        BTDLList<Key> List;
        Key Element;

        List = new BTDLList<Key>();
        i = 0;
        this.lock.readLock().lock();
        try {
            while (i < this.M) {
                Element = (Key) this.keys.getElement(i);
                if (Element != null) {
                    List.add(Element);
                }
                i++;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return List;
    }

    /**
     * Verifica la integridad de la tabla, retornando TRUE si esta bien o FALSE
     * si hay inconsistencias.
     *
     * NOTA: No se realiza el chequedo durante el put() porque durante el
     * delete() la consistencia no es mantenida mientras se borra la llave.
     *
     * @return TRUE si la tabla esta consistente o FALSE si no.
     */
    private boolean check() throws UtilsException {
        long i;
        boolean todoOK = true;

        // check that hash table is at most 98% full
        if (this.N < ((29 * this.M) / 32)) {
            // check that each key in table can be found by get()
            i = 0;
            while (todoOK && (i < this.M)) {
                if (this.keys.getElement(i) != null) {
                    if (this.get((Key) this.keys.getElement(i)) != this.vals.getElement(i)) {
                        todoOK = false;
                    }
                }
                i++;
            }
        } else {
            todoOK = false;
        }
        return todoOK;
    }
}
