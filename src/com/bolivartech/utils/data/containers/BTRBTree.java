package com.bolivartech.utils.data.containers;

import com.bolivartech.utils.exception.UtilsException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Copyright 2015 BolivarTech LLC
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * This Class is the BolivarTech's util that implement BolivarTech's Red-Black
 * Tree data structure.
 *
 *
 * Implementa una clase que define la estructura de datos Red-Black Tree
 *
 * Clase Tread Safe.
 * 
 * Class ID: "35DGFH9"
 * Loc: 000-005
 *
 * @author Julian Bolivar
 * @version 2.0.1
 * @since 2015 - March 25, 2016
 *
 * Change Logs: 
 * v1.0.0 (10/10/2015): Version Inicial. 
 * v1.0.1 (10/13/2015): Los metodos getKeys y getValues retornan un BTDLList con 
 * los valores para faciliar su uso. 
 * v1.0.2 (10/14/2015): Se agrego el constructor de copiado y el metodo Clear 
 * para hacer una limpieza controlada del arbol. 
 * v1.0.3 (10/21/2015): Se agregaron los metodos selectKeys() y selectValues() 
 * para buscar rangos de datos en el arbol.
 * v2.0.0 (12/21/2015): Se agrego soporte de concurrencia en el arbor.
 * v2.0.1 (2016-03-25) Se implemento el uso del codigo de ubicacion unico 
 *
 * @param <Key> Llave de busqueda en el arbol
 * @param <Value> Valor asociado a la llave
 */
public final class BTRBTree<Key extends Comparable<Key>, Value> {
    
    // Codigo de identificacion de la clase
    private static final String CLASSID =  "35DGFH9";

    // Lock para el manejo de concurrencia
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // Tree check Errors
    public static final int NOTBALANCED = -1;
    public static final int NOTSYMMETRIC = -2;
    public static final int NOTCONSISTENTSUBTREE = -3;
    public static final int NOTCONSISTENTRANKS = -4;
    public static final int NOT2_3TREE = -5;
    public static final int SELECTOUTOFRANGE = -6;

    // Banderas de color de los nodos
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    // Nodo Raiz del arbol
    private Node root;

    // Clase privada definicion de los nodos del arbol
    private class Node {

        // Llave del Nodo
        private Key key;
        // Data asociada con la llave
        private Value val;
        // Padre, Subarbol derecho e izquierdo del nodo.
        private Node left, right, Parent;
        // Color del nodo
        private boolean color;
        // contador del subarbol
        private long N;

        /**
         * Constructor con inicalizacion del nodo
         *
         * @param key Llave del nodo
         * @param val Valor asociado con el nodo
         * @param color Color del nodo
         * @param N contador del subarbol
         */
        public Node(Key key, Value val, boolean color, long N) {

            this.key = key;
            this.val = val;
            this.color = color;
            this.N = N;
            this.left = null;
            this.right = null;
            this.Parent = null;
        }
    }

    /**
     * Constructor por defecto del arbol.
     */
    public BTRBTree() {

        this.root = null;
    }

    /**
     * Constructor de copiado
     *
     * @param Other Arbol a copiar
     */
    public BTRBTree(BTRBTree<Key, Value> Other) {
        BTDLList Keys;
        BTDLList Values;
        Key Llave;
        Value Valor;

        this.Clear();
        if (Other != null) {
            Keys = Other.getKeys();
            Values = Other.getValues();
            if ((!Keys.isEmpty()) && (!Values.isEmpty())) {
                Llave = (Key) Keys.getCurrent();
                Valor = (Value) Values.getCurrent();
                this.put(Llave, Valor);
                while (Keys.hasNext() && Values.hasNext()) {
                    Llave = (Key) Keys.getNext();
                    Valor = (Value) Values.getNext();
                    this.put(Llave, Valor);
                }
            }
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
     * Comprueba si un nodo es rojo, retornando TRUE si lo es o FALSE si no.
     */
    private boolean isRed(Node x) {
        boolean Result = false;

        if (x != null) {
            if (x.color == RED) {
                Result = true;
            }
        }
        return Result;
    }

    /**
     * Retorna el numero del nodo en el subarbol con raiz en el nodo 'x' o 0 si
     * 'x' es NULL
     */
    private long size(Node x) {
        long Result = 0;

        if (x != null) {
            Result = x.N;
        }
        return Result;
    }

    /**
     * Retorna el numero de nodos en el arbol
     *
     * @return Retorna el numero de nodos en el arbol
     */
    public long size() {
        long Result;

        this.lock.readLock().lock();
        try {
            Result = size(this.root);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Limpia el arbol de forma controlada en todas las ramas.
     */
    public void Clear() {

        this.lock.writeLock().lock();
        try {
            if (this.root != null) {
                this.root.left = ClearSurTree(this.root.left);
                this.root.right = ClearSurTree(this.root.right);
                this.root.Parent = null;
                this.root.val = null;
                this.root.key = null;
                this.root = null;
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Limpia las ramas del subarbol retornando NULL.
     *
     * @param SubRoot Raiz del subarbol
     * @return Retorna SIEMPRE NULL
     */
    private Node ClearSurTree(Node SubRoot) {

        if (SubRoot != null) {
            SubRoot.left = ClearSurTree(SubRoot.left);
            SubRoot.right = ClearSurTree(SubRoot.right);
            SubRoot.Parent = null;
            SubRoot.val = null;
            SubRoot.key = null;
        }
        return null;
    }

    /**
     * Retorna TRUE si el arbol esta vacio o FALSE si no.
     *
     * @return TRUE si el arbol esta vacio o FALSE en caso contrario
     */
    public boolean isEmpty() {
        boolean Result = false;

        this.lock.readLock().lock();
        try {
            Result = (this.root == null);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el valor asociado con la llave especificada en el arbol o NULL si
     * la llave no esta definida.
     *
     * @param key Llave de busqueda en el arbol
     * @return Valor asociado con la llave o NULL si no esta espedificado.
     */
    public Value get(Key key) {
        Value Result = null;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                Result = get(this.root, key);
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor asociado con la llave en el subarbol con raiz en 'x' o
     * NULL si no existe dicha llave en el arbol.
     */
    private Value get(Node x, Key key) {
        int cmp;
        Value Result = null;

        while (x != null) {
            cmp = key.compareTo(x.key);
            if (cmp < 0) {
                x = x.left;
            } else if (cmp > 0) {
                x = x.right;
            } else {
                Result = x.val;
                x = null;
            }
        }
        return Result;
    }

    /**
     * Verifica si la llave especificada por 'key' esta contenida en el arbol,
     * retornando TRUE si lo esta o FALSE si no.
     *
     * @param key Llave a verificar en el arbol
     * @return TRUE si la llave esta contenida en el arbol o FALSE si no
     */
    public boolean containsKey(Key key) {
        boolean Result = false;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                Result = (this.get(this.root, key) != null);
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Inserta la llave 'key' y su valor asociado 'val' en el arbol.
     *
     * Si la llave ya esta definida en el arbol se sustituye el valor anterior
     * por el valor de 'val'.
     *
     * @param key Llave de busqueda en el arbol
     * @param val Valor asociado con la llave
     */
    public void put(Key key, Value val) {

        if (key != null) {
            this.lock.writeLock().lock();
            try {
                this.root = put(this.root, key, val);
                this.root.Parent = null;
                this.root.color = BLACK;
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    /**
     * Inserta la llave 'key' y el valor 'val' asociado a la llave en el
     * subarbol con raiz en 'h'.
     *
     * Si la llave ya esta definida en el arbol se sustituye el valor anterior
     * por el valor de 'val'.
     *
     * @param h Raiz del subarbol
     * @param key Llave de busqueda en el arbol
     * @param val Valor asociado a la llave
     * @return Nodo modificado en la insersion
     */
    private Node put(Node h, Key key, Value val) {
        Node Result;
        int cmp;

        if (h != null) {
            cmp = key.compareTo(h.key);
            if (cmp < 0) {
                h.left = put(h.left, key, val);
                h.left.Parent = h;
            } else if (cmp > 0) {
                h.right = put(h.right, key, val);
                h.right.Parent = h;
            } else {
                h.val = val;
            }
            // Solventa cualquier tendencia hacia la derecha en la llamas del arbol
            if (isRed(h.right) && !isRed(h.left)) {
                h = rotateLeft(h);
            }
            if (isRed(h.left) && isRed(h.left.left)) {
                h = rotateRight(h);
            }
            if (isRed(h.left) && isRed(h.right)) {
                flipColors(h);
            }
            h.N = size(h.left) + size(h.right) + 1;
            Result = h;
        } else {
            Result = new Node(key, val, RED, 1);
        }
        return Result;
    }

    /**
     * Elimina la llave menor y el valor asociado en arbol, si el arbol esta
     * vacio no hace ninguna operacion.
     */
    public void deleteMin() {

        this.lock.writeLock().lock();
        try {
            if (this.root != null) {
                // if both children of root are black, set root to red
                if (!isRed(this.root.left) && !isRed(this.root.right)) {
                    this.root.color = RED;
                }
                this.root = deleteMin(root);
                if (this.root != null) {
                    this.root.color = BLACK;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Elimina la llave menor y el valor asociado en el subarbol con raiz en el
     * nodo h.
     *
     * @param h Nodo Raiz del subarbol
     * @return subarbol sin la menor llave y ya balanceado
     */
    private Node deleteMin(Node h) {
        Node Result = null;

        if (h.left != null) {
            if (!isRed(h.left) && !isRed(h.left.left)) {
                h = moveRedLeft(h);
            }
            h.left = deleteMin(h.left);
            Result = balance(h);
        }
        return Result;
    }

    /**
     * Elimina la llave mayor y el valor asociado en el arbol, si el arbol esta
     * vacio no hace ninguna operacion.
     */
    public void deleteMax() {

        this.lock.writeLock().lock();
        try {
            if (this.root != null) {
                // if both children of root are black, set root to red
                if (!isRed(this.root.left) && !isRed(this.root.right)) {
                    this.root.color = RED;
                }
                this.root = deleteMax(this.root);
                if (this.root != null) {
                    this.root.color = BLACK;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Elimina la llave mayor y el valor asociado en el subarbol con raiz en el
     * nodo h.
     *
     * @param h Nodo raiz del subarbol
     * @return subarbol sin la llave mayor y ya balanceado
     */
    private Node deleteMax(Node h) {
        Node Result = null;

        if (isRed(h.left)) {
            h = rotateRight(h);
        }
        if (h.right != null) {
            if (!isRed(h.right) && !isRed(h.right.left)) {
                h = moveRedRight(h);
            }
            h.right = deleteMax(h.right);
            Result = balance(h);
        }
        return Result;
    }

    /**
     * Elimina la llave 'key' y el valor asociado en el arbol, si el arbol esta
     * vacio o la llave no existe o es NULL no hace ninguna operacion.
     *
     * @param key the key
     */
    public void delete(Key key) {

        if (key != null) {
            this.lock.writeLock().lock();
            try {
                if (this.get(this.root, key) != null) {
                    // Si los dos hijos de la raiz don nregros, se establece la raiz como roja
                    if (!isRed(this.root.left) && !isRed(this.root.right)) {
                        this.root.color = RED;
                    }
                    this.root = delete(this.root, key);
                    if (this.root != null) {
                        this.root.color = BLACK;
                    }
                }
            } finally {
                this.lock.writeLock().unlock();
            }
        }
    }

    /**
     * Elimina la llave 'key' y el valor asociado en el subarbol 'h', si el
     * arbol esta vacio o la llave no existe no hace ninguna operacion.
     *
     * @param h Nodo raiz del subarbol
     * @param key llave a eliminar
     * @return subarbol con la llave eliminada y balanceado
     */
    private Node delete(Node h, Key key) {
        Node Result = null;
        Node x = null;

        if (key.compareTo(h.key) < 0) {
            if (!isRed(h.left) && !isRed(h.left.left)) {
                h = moveRedLeft(h);
            }
            h.left = delete(h.left, key);
        } else {
            if (isRed(h.left)) {
                h = rotateRight(h);
            }
            if (key.compareTo(h.key) == 0 && (h.right == null)) {
                h = null;
            } else {
                if (!isRed(h.right) && !isRed(h.right.left)) {
                    h = moveRedRight(h);
                }
                if (key.compareTo(h.key) == 0) {
                    x = minNode(h.right);
                    h.key = x.key;
                    h.val = x.val;
                    h.Parent = x.Parent;
                    h.right = deleteMin(h.right);
                } else {
                    h.right = delete(h.right, key);
                }
            }
        }
        if (h != null) {
            Result = balance(h);
        }
        return Result;
    }

    /**
     * Realiza la rotacion derecha de las ramas del nodo
     */
    private Node rotateRight(Node h) {
        Node x = null;

        if (h != null) {
            x = h.left;
            h.left = x.right;
            if (h.left != null) {
                h.left.Parent = h;
            }
            x.right = h;
            x.color = x.right.color;
            x.right.color = RED;
            x.N = h.N;
            x.Parent = h.Parent;
            h.Parent = x;
            h.N = size(h.left) + size(h.right) + 1;
        }
        return x;
    }

    /**
     * Realiza la rotacion izquierda de las ramas del nodo
     */
    private Node rotateLeft(Node h) {
        Node x = null;

        if (h != null) {
            x = h.right;
            h.right = x.left;
            if (h.right != null) {
                h.right.Parent = h;
            }
            x.left = h;
            x.color = x.left.color;
            x.left.color = RED;
            x.N = h.N;
            x.Parent = h.Parent;
            h.Parent = x;
            h.N = size(h.left) + size(h.right) + 1;
        }
        return x;
    }

    /**
     * Realiza la commutacion de los colores del nodo y de los dos nodos hijos
     */
    private void flipColors(Node h) {

        if (h != null) {
            h.color = !h.color;
            h.left.color = !h.left.color;
            h.right.color = !h.right.color;
        }
    }

    /**
     * Asumiendo que 'h' es rojo y la rama izquierda y la rama izquierda de esta
     * rama izquierda son negras, coloca la rama izquierda o a uno de sus hijos
     * como rojo.
     *
     * @param h Nodo raiz del subarbol
     * @return Apuntador al nodo raiz ajustado.
     */
    private Node moveRedLeft(Node h) {

        if (h != null) {
            flipColors(h);
            if (isRed(h.right.left)) {
                h.right = rotateRight(h.right);
                h = rotateLeft(h);
                flipColors(h);
            }
        }
        return h;
    }

    /**
     * Asumiendo que 'h' es rojo y la rama derecha y la rama izquiereda de esta
     * rama derecha son negras, hace que la rama derecha o uno de sus hijos sea
     * rojo.
     *
     * @param h Nodo raiz del subarbol
     * @return Apuntador al nodo raiz ajustado.
     */
    private Node moveRedRight(Node h) {

        if (h != null) {
            flipColors(h);
            if (isRed(h.left.left)) {
                h = rotateRight(h);
                flipColors(h);
            }
        }
        return h;
    }

    /**
     * Recostruye el balance rojo-negro del subarbol con raiz en 'h'
     *
     * @param h Nodo raiz del subarbol
     * @return Apuntador al nodo raiz del subarbol balanceado.
     */
    private Node balance(Node h) {

        if (h != null) {
            if (isRed(h.right)) {
                h = rotateLeft(h);
            }
            if (isRed(h.left) && isRed(h.left.left)) {
                h = rotateRight(h);
            }
            if (isRed(h.left) && isRed(h.right)) {
                flipColors(h);
            }

            h.N = size(h.left) + size(h.right) + 1;
        }
        return h;
    }

    /**
     * Retorna la altura del arbol.
     *
     * @return La altura del arbol, si el arbol tiene un solo nodo la altura es
     * 0.
     */
    public long height() {
        long Result;

        this.lock.readLock().lock();
        try {
            Result = height(this.root);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna la altura del subarbol con raiz en el nodo 'x'.
     *
     * @param x Nodo raiz del subarbol.
     * @return Altura del subarbol.
     */
    private long height(Node x) {
        long Altura = -1;

        if (x != null) {
            Altura = 1 + Math.max(height(x.left), height(x.right));
        }
        return Altura;
    }

    /**
     * Retorna la llave mas pequeña en el arbol o NULL si el arbol esta vacio.
     *
     * @return La llave mas pequeña en el arbol o NULL si esta vacio
     */
    public Key getMinKey() {
        Key Result = null;

        this.lock.readLock().lock();
        try {
            if (this.root != null) {
                Result = minNode(this.root).key;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el valor asociado con la llave mas pequeña en el arbol o NULL si
     * el arbol esta vacio.
     *
     * @return Valor asociado con la llave mas pequeña en el arbol o NULL si
     * esta vacio
     */
    public Value getMinValue() {
        Value Result = null;

        this.lock.readLock().lock();
        try {
            if (this.root != null) {
                Result = minNode(this.root).val;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el nodo con la llave mas pequeña en el subarbol con raiz en 'x'.
     *
     * @param x Raiz del subarbol
     * @return Nodo con la llave mas pequeña en el subarbol con raiz en 'x'
     */
    private Node minNode(Node x) {
        Node Result = x;

        if (x.left != null) {
            Result = minNode(x.left);
        }
        return Result;
    }

    /**
     * Retorna la llave mas grande en el arbol o NULL si el arbol esta vacio.
     *
     * @return La llave mas grande en el arbol o NULL si el arbol esta vacio.
     */
    public Key getMaxKey() {
        Key Result = null;

        this.lock.readLock().lock();
        try {
            if (this.root != null) {
                Result = maxNode(this.root).key;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el valor asociado con la llave mas grande en el arbol o NULL si
     * el arbol esta vacio.
     *
     * @return Valor asociado con la llave mas grande en el arbol o NULL si el
     * arbol esta vacio.
     */
    public Value getMaxValue() {
        Value Result = null;

        this.lock.readLock().lock();
        try {
            if (this.root != null) {
                Result = maxNode(this.root).val;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el nodo con la llave mas grande en el subarbol con raiz en 'x'.
     *
     * @param x Raiz del subarbol
     * @return Nodo con la llave mas grande en el subarbol con raiz en 'x'
     */
    private Node maxNode(Node x) {
        Node Result = x;

        if (x.right != null) {
            Result = maxNode(x.right);
        }
        return Result;
    }

    /**
     * Retorna la mayor llave en el arbol que es menor o igual a la llave 'key'.
     *
     * @param key Llave a buscar
     * @return La mayor llave que es menor o igual a 'key'.
     */
    public Key getFloorKey(Key key) {
        Key Result = null;
        Node x;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    x = floorNode(this.root, key);
                    if (x != null) {
                        Result = x.key;
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor asociado a la mayor llave en el arbol que es menor o
     * igual a la llave 'key'.
     *
     * @param key Llave a buscar
     * @return El valor asociado a la mayor llave que es menor o igual a 'key'.
     */
    public Value getFloorValue(Key key) {
        Value Result = null;
        Node x;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    x = floorNode(this.root, key);
                    if (x != null) {
                        Result = x.val;
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna la mayor llave en el subarbol con raiz en 'x' que es menor o
     * igual a la llave 'key'.
     *
     * @param x Raiz del subarbol.
     * @param key Llave a buscar.
     * @return La Nodo con la mayor llave que es menor o igual a 'key'.
     */
    // the largest key in the subtree rooted at x less than or equal to the given key
    private Node floorNode(Node x, Key key) {
        Node Result = null;
        int cmp;
        Node t;

        if (x != null) {
            cmp = key.compareTo(x.key);
            if (cmp < 0) {
                Result = floorNode(x.left, key);
            } else if (cmp > 0) {
                t = floorNode(x.right, key);
                if (t != null) {
                    Result = t;
                } else {
                    Result = x;
                }
            } else {
                Result = x;
            }
        }
        return Result;
    }

    /**
     * Retorna la menor llave en el arbol que es mayor o igual a la llave 'key'.
     *
     * @param key Llave a buscar
     * @return La menor llave en el arbol que es mayor o igual a la llave 'key'
     */
    public Key getCeilingKey(Key key) {
        Key Result = null;
        Node x;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    x = ceilingNode(this.root, key);
                    if (x != null) {
                        Result = x.key;
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor asociado a la menor llave en el arbol que es mayor o
     * igual a la llave 'key'.
     *
     * @param key Llave a buscar
     * @return El menor valor asociado a la menor llave en el arbol que es mayor
     * o igual a la llave 'key'
     */
    public Value getCeilingValue(Key key) {
        Value Result = null;
        Node x;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    x = ceilingNode(this.root, key);
                    if (x != null) {
                        Result = x.val;
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna la menor llave en el subarbol con raiz 'x' que es mayor o igual a
     * la llave 'key'.
     *
     * @param x Raiz del subarbol
     * @param key Llave a buscar
     * @return La menor llave en el subarbol que es mayor o igual a la llave
     * 'key'
     */
    // the smallest key in the subtree rooted at x greater than or equal to the given key
    private Node ceilingNode(Node x, Key key) {
        Node Result = null;
        int cmp;
        Node t;

        if (x != null) {
            cmp = key.compareTo(x.key);
            if (cmp > 0) {
                Result = ceilingNode(x.right, key);
            } else if (cmp < 0) {
                t = ceilingNode(x.left, key);
                if (t != null) {
                    Result = t;
                } else {
                    Result = x;
                }
            } else {
                Result = x;
            }
        }
        return Result;
    }

    /**
     * Retorna la llave siguiente a la llave especificada key.
     *
     * @param key Llave a la cual se le debe buscar su sucesor.
     * @return Llave sucesora a la llave espevificada
     */
    public Key getSuccessorKey(Key key) {
        int cmp;
        Node n, p;
        Key Result = null;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    n = this.root;
                    while (n != null) {
                        cmp = key.compareTo(n.key);
                        if (cmp < 0) {
                            n = n.left;
                        } else if (cmp > 0) {
                            n = n.right;
                        } else {
                            if (n.right != null) {
                                n = this.minNode(n.right);
                                Result = n.key;
                            } else {
                                p = n.Parent;
                                while ((p != null) && (p.right != null) && (n.key == p.right.key)) {
                                    n = p;
                                    p = n.Parent;
                                }
                                Result = (p != null ? p.key : null);
                            }
                            n = null;
                        }
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor siguiente a la llave especificada key.
     *
     * @param key Llave a la cual se le debe buscar su sucesor.
     * @return Valor sucesor a la llave espevificada
     */
    public Value getSuccessorValue(Key key) {
        int cmp;
        Node n, p;
        Value Result = null;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    n = this.root;
                    while (n != null) {
                        cmp = key.compareTo(n.key);
                        if (cmp < 0) {
                            n = n.left;
                        } else if (cmp > 0) {
                            n = n.right;
                        } else {
                            if (n.right != null) {
                                n = this.minNode(n.right);
                                Result = n.val;
                            } else {
                                p = n.Parent;
                                while ((p != null) && (p.right != null) && (n.key == p.right.key)) {
                                    n = p;
                                    p = n.Parent;
                                }
                                Result = (p != null ? p.val : null);
                            }
                            n = null;
                        }
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna la llave previa a la llave especificada key.
     *
     * @param key Llave a la cual se le debe buscar su predecesor.
     * @return Llave predecesora a la llave especificada
     */
    public Key getPredecessorKey(Key key) {
        int cmp;
        Node n, p;
        Key Result = null;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    n = this.root;
                    while (n != null) {
                        cmp = key.compareTo(n.key);
                        if (cmp < 0) {
                            n = n.left;
                        } else if (cmp > 0) {
                            n = n.right;
                        } else {
                            if (n.left != null) {
                                n = this.maxNode(n.left);
                                Result = n.key;
                            } else {
                                p = n.Parent;
                                while ((p != null) && (p.left != null) && (n.key == p.left.key)) {
                                    n = p;
                                    p = n.Parent;
                                }
                                Result = (p != null ? p.key : null);
                            }
                            n = null;
                        }
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor previo a la llave especificada key.
     *
     * @param key Llave a la cual se le debe buscar su predecesor.
     * @return Valor predecesor a la llave especificada
     */
    public Value getPredecessorValue(Key key) {
        int cmp;
        Node n, p;
        Value Result = null;

        if (key != null) {
            this.lock.readLock().lock();
            try {
                if (this.root != null) {
                    n = this.root;
                    while (n != null) {
                        cmp = key.compareTo(n.key);
                        if (cmp < 0) {
                            n = n.left;
                        } else if (cmp > 0) {
                            n = n.right;
                        } else {
                            if (n.left != null) {
                                n = this.maxNode(n.left);
                                Result = n.val;
                            } else {
                                p = n.Parent;
                                while ((p != null) && (p.left != null) && (n.key == p.left.key)) {
                                    n = p;
                                    p = n.Parent;
                                }
                                Result = (p != null ? p.val : null);
                            }
                            n = null;
                        }
                    }
                }
            } finally {
                this.lock.readLock().unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna un BTDLList con las llaves contenidas en el arbol ordenadas de
 menor a mayor o un BTDLList vacio si el arbol esta vacio.
     *
     * @return BTDLList con las llaves contenidas en el arbol
     */
    public BTDLList<Key> getKeys() {
        BTDLList<Key> Result;

        Result = new BTDLList<Key>();
        this.lock.readLock().lock();
        try {
            inorder_keys(Result, this.root);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Funncion de busqueda de llaves.
     *
     * @param Arreglo Arreglo donde almacenar los resultados
     * @param n Nodo a revisar
     */
    private void inorder_keys(BTDLList<Key> Arreglo, Node n) {

        if (n != null) {
            inorder_keys(Arreglo, n.left);
            Arreglo.add(n.key);
            inorder_keys(Arreglo, n.right);
        }
    }

    /**
     * Retorna la lista de llaves contenidas en el arbol que estan comprendidas
     * entre [MinKey,MaxKey].Ambas llaves deben de ser distintas de NULL.
     *
     * Si no se consigue el rango especificado se retorna una lista vacia.
     *
     * @param MinKey Liminte inferior del grupo de busqueda.
     * @param MaxKey Liminte superior del grupo de busqueda.
     * @return Lista de llaves contenidas entre los limites especificados o una
     * lista vacia
     */
    public BTDLList<Key> selectKeys(Key MinKey, Key MaxKey) {
        BTDLList<Key> Result = null;
        Key Cursor;

        if ((MinKey != null) && (MaxKey != null)) {
            Result = new BTDLList<Key>();
            Cursor = this.getCeilingKey(MinKey);
            if (Cursor != null) {
                while ((Cursor != null) && (Cursor.compareTo(MaxKey) < 1)) {
                    Result.add(Cursor);
                    Cursor = this.getSuccessorKey(Cursor);
                }
            }
        }
        return Result;
    }

    /**
     * Retorna la lista de Valores contenidas en el arbol que estan comprendidas
     * entre [MinKey,MaxKey].
     *
     * Si no se consigue el rango especificado se retorna una lista vacia.
     *
     * @param MinKey Liminte inferior del grupo de busqueda.
     * @param MaxKey Liminte superior del grupo de busqueda.
     * @return Lista de Valores contenidas entre los limites especificados o una
     * lista vacia
     */
    public BTDLList<Value> selectValues(Key MinKey, Key MaxKey) {
        BTDLList<Value> Result = null;
        Key Cursor;

        if ((MinKey != null) && (MaxKey != null)) {
            Result = new BTDLList<Value>();
            Cursor = this.getCeilingKey(MinKey);
            if (Cursor != null) {
                while ((Cursor != null) && (Cursor.compareTo(MaxKey) < 1)) {
                    Result.add(this.get(Cursor));
                    Cursor = this.getSuccessorKey(Cursor);
                }
            }
        }
        return Result;
    }

    /**
     * Retorna BTDLList con los valores contenidos en el arbol ordenadas de menor
 a mayor en base a la llave o un BTDLList vacio si el arbol esta vacio.
     *
     * @return BTDLList con los valores contenidos en el arbol
     */
    public BTDLList<Value> getValues() {
        BTDLList<Value> Result;

        Result = new BTDLList<Value>();
        this.lock.readLock().lock();
        try {
            inorder_values(Result, this.root);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Funcion de busqueda de Valores.
     *
     * @param Arreglo Arreglo donde almacenar los resultados
     * @param n Nodo a revisar
     */
    private void inorder_values(BTDLList<Value> Arreglo, Node n) {

        if (n != null) {
            inorder_values(Arreglo, n.left);
            Arreglo.add(n.val);
            inorder_values(Arreglo, n.right);
        }
    }

    /**
     * Verifica la integridad de la estructura del arbol Rojo-Nego, retornando
     * TRUE si la estructura es consistente o FALSE si no.
     *
     * @return TRUE si la estructura es consistente y FALSE si no.
     * @throws UtilsException En caso de inconsistencia genera una exception con
     * el codigo de la inconsistencia
     */
    public boolean check() throws UtilsException {
        boolean BST;
        boolean SizeConsistent;
        boolean RandConsistent;
        boolean es23;
        boolean BalancedTree;
        boolean Result = false;

        this.lock.writeLock().lock();
        try {
            BST = this.isBST();
            SizeConsistent = this.isSizeConsistent();
            RandConsistent = this.isRankConsistent();
            es23 = this.is23();
            BalancedTree = this.isBalanced();
            if (!BST) {
                throw new UtilsException("Tree NOT in symmetric order", BTRBTree.NOTSYMMETRIC,BTRBTree.CLASSID+"000");
            }
            if (!SizeConsistent) {
                throw new UtilsException("Subtree counts not consistent", BTRBTree.NOTCONSISTENTSUBTREE,BTRBTree.CLASSID+"001");
            }
            if (!RandConsistent) {
                throw new UtilsException("Ranks not consistent", BTRBTree.NOTCONSISTENTRANKS,BTRBTree.CLASSID+"002");
            }
            if (!es23) {
                throw new UtilsException("Not a 2-3 tree", BTRBTree.NOT2_3TREE,BTRBTree.CLASSID+"003");
            }
            if (!BalancedTree) {
                this.root = this.balance(this.root);
                BalancedTree = this.isBalanced();
                if (!BalancedTree) {
                    throw new UtilsException("Tree is NOT balanced", BTRBTree.NOTBALANCED,BTRBTree.CLASSID+"004");
                }
            }
            Result = BST && SizeConsistent && RandConsistent && es23 && BalancedTree;
        } finally {
            this.lock.writeLock().unlock();
        }
        return Result;
    }

    // does this binary tree satisfy symmetric order?
    // Note: this test also ensures that data structure is a binary tree since order is strict
    private boolean isBST() {
        return isBST(root, null, null);
    }

    // is the tree rooted at x a BST with all keys strictly between min and max
    // (if min or max is null, treat as empty constraint)
    // Credit: Bob Dondero's elegant solution
    private boolean isBST(Node x, Key min, Key max) {
        if (x == null) {
            return true;
        }
        if (min != null && x.key.compareTo(min) <= 0) {
            return false;
        }
        if (max != null && x.key.compareTo(max) >= 0) {
            return false;
        }
        return isBST(x.left, min, x.key) && isBST(x.right, x.key, max);
    }

    // are the size fields correct?
    private boolean isSizeConsistent() {
        return isSizeConsistent(root);
    }

    private boolean isSizeConsistent(Node x) {
        if (x == null) {
            return true;
        }
        if (x.N != size(x.left) + size(x.right) + 1) {
            return false;
        }
        return isSizeConsistent(x.left) && isSizeConsistent(x.right);
    }

    // check that ranks are consistent
    private boolean isRankConsistent() throws UtilsException {
        for (int i = 0; i < size(); i++) {
            if (i != rank(select(i))) {
                return false;
            }
        }
        for (Key key : keys()) {
            if (key.compareTo(select(rank(key))) != 0) {
                return false;
            }
        }
        return true;
    }

    // Does the tree have no red right links, and at most one (left)
    // red links in a row on any path?
    private boolean is23() {
        return is23(root);
    }

    private boolean is23(Node x) {
        if (x == null) {
            return true;
        }
        if (isRed(x.right)) {
            return false;
        }
        if (x != root && isRed(x) && isRed(x.left)) {
            return false;
        }
        return is23(x.left) && is23(x.right);
    }

    // do all paths from root to leaf have same number of black edges?
    private boolean isBalanced() {
        int black = 0;     // number of black links on path from root to min
        Node x = root;
        while (x != null) {
            if (!isRed(x)) {
                black++;
            }
            x = x.left;
        }
        return isBalanced(root, black);
    }

    // does every path from the root to a leaf have the given number of black links?
    private boolean isBalanced(Node x, int black) {
        if (x == null) {
            return black == 0;
        }
        if (!isRed(x)) {
            black--;
        }
        return isBalanced(x.left, black) && isBalanced(x.right, black);
    }

    /**
     * Return the kth smallest key in the symbol table.
     *
     * @param k the order statistic
     * @return the kth smallest key in the symbol table
     * @throws IllegalArgumentException unless <tt>k</tt> is between 0 and
     * <em>N</em> &minus; 1
     */
    private Key select(long k) throws UtilsException {
        if (k < 0 || k >= size()) {
            throw new UtilsException("Select out of range",BTRBTree.SELECTOUTOFRANGE,BTRBTree.CLASSID+"005");
        }
        Node x = select(root, k);
        return x.key;
    }

    // the key of rank k in the subtree rooted at x
    private Node select(Node x, long k) {
        long t;
        // assert x != null;
        // assert k >= 0 && k < size(x);
        t = size(x.left);
        if (t > k) {
            return select(x.left, k);
        } else if (t < k) {
            return select(x.right, k - t - 1);
        } else {
            return x;
        }
    }

    /**
     * Return the number of keys in the symbol table strictly less than
     * <tt>key</tt>.
     *
     * @param key the key
     * @return the number of keys in the symbol table strictly less than
     * <tt>key</tt>
     * @throws NullPointerException if <tt>key</tt> is <tt>null</tt>
     */
    private long rank(Key key) {
        return rank(key, root);
    }

    // number of keys less than key in the subtree rooted at x
    private long rank(Key key, Node x) {
        if (x == null) {
            return 0;
        }
        int cmp = key.compareTo(x.key);
        if (cmp < 0) {
            return rank(key, x.left);
        } else if (cmp > 0) {
            return 1 + size(x.left) + rank(key, x.right);
        } else {
            return size(x.left);
        }
    }

    /**
     * Returns all keys in the symbol table as an <tt>Iterable</tt>. To iterate
     * over all of the keys in the symbol table named <tt>st</tt>, use the
     * foreach notation: <tt>for (Key key : st.keys())</tt>.
     *
     * @return all keys in the sybol table as an <tt>Iterable</tt>
     */
    private Iterable<Key> keys() {
        return keys(getMinKey(), getMaxKey());
    }

    /**
     * Returns all keys in the symbol table in the given range, as an
     * <tt>Iterable</tt>.
     *
     * @return all keys in the sybol table between <tt>lo</tt>
     * (inclusive) and <tt>hi</tt> (exclusive) as an <tt>Iterable</tt>
     * @throws NullPointerException if either <tt>lo</tt> or <tt>hi</tt>
     * is <tt>null</tt>
     */
    private Iterable<Key> keys(Key lo, Key hi) {
        BTQueue<Key> queue = new BTQueue<Key>();
        // if (isEmpty() || lo.compareTo(hi) > 0) return queue;
        keys(root, queue, lo, hi);
        return queue;
    }

    // add the keys between lo and hi in the subtree rooted at x
    // to the queue
    private void keys(Node x, BTQueue<Key> queue, Key lo, Key hi) {
        if (x == null) {
            return;
        }
        int cmplo = lo.compareTo(x.key);
        int cmphi = hi.compareTo(x.key);
        if (cmplo < 0) {
            keys(x.left, queue, lo, hi);
        }
        if (cmplo <= 0 && cmphi >= 0) {
            queue.add(x.key);
        }
        if (cmphi > 0) {
            keys(x.right, queue, lo, hi);
        }
    }

}
