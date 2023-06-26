package com.bolivartech.utils.data.containers;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyright 2015 BolivarTech LLC
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * This Class is the BolivarTech's util that implement BolivarTech's SplayTree
 * data structure.
 *
 *
 * Implementa una clase que define la estructura de datos SplayTree
 *
 * Clase Thread Safe.
 *
 * @author Julian Bolivar
 * @version 2.0.1 - December 28, 2015
 * @since 2015
 *
 * Change Logs: 
 * v1.0.0 (10/07/2015): Version Inicial. 
 * v1.0.1 (10/13/2015): Los metodos getKeys y getValues retornan un BTDLList con 
 * los valores para faciliar su uso. 
 * v1.0.2 (10/14/2015): Se agrego el constructor de copiado y el metodo Clear 
 * para hacer una limpieza controlada del arbol. 
 * v1.0.3 (10/20/2015): Se agrego el metodo isEmpty() para verificar si el arbol
 * esta vacio.
 * v1.0.4 (10/21/2015): Se agregaron los metodos selectKeys() y selectValues() para
 * buscar rangos de datos en el arbol. 
 * v2.0.0 (12/20/2015): Se agrego soporte de concurrencia en el arbor asi como 
 * el apuntador al nodo padre en los nodos del arbol.
 * v2.0.1 (12/28/2015): Se solventaron bugs menores en la concurrencia.
 *
 * @param <Key> Llave de busqueda en el arbol
 * @param <Value> Valor asociado a la llave
 */
public final class BTSplayTree<Key extends Comparable<Key>, Value> {

    // Lock para el manejo de concurrencia
    private final ReentrantLock lock = new ReentrantLock();

    // Root del arbol
    private Node root;

    // Node data type
    private class Node {

        private Key key;            // Llave
        private Value value;        // Data asociada
        private Node left, right, parent;   // Subarboles derecho e izquierdo y el nodo padre

        /**
         * Constructor del nodo.
         *
         * @param key Llave del nodo
         * @param value Valor asociado del nodo
         */
        public Node(Key key, Value value) {

            this.key = key;
            this.value = value;
            this.right = null;
            this.left = null;
            this.parent = null;
        }
    }

    /**
     * Constructor por defecto
     */
    public BTSplayTree() {

        this.root = null;
    }

    /**
     * Constructor de copiado
     *
     * @param Other Arbol a copiar
     */
    public BTSplayTree(BTSplayTree<Key, Value> Other) {
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
     * Limpia el arbol de forma controlada en todas las ramas.
     */
    public void Clear() {

        this.lock.lock();
        try {
            if (this.root != null) {
                this.root.left = ClearSurTree(this.root.left);
                this.root.right = ClearSurTree(this.root.right);
                this.root.value = null;
                this.root.key = null;
                this.root = null;
            }
        } finally {
            this.lock.unlock();
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
            SubRoot.value = null;
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

        this.lock.lock();
        try {
            if (this.root == null) {
                Result = true;
            }
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Retorna un BTDLList con las llaves contenidas en el arbol ordenadas de
 menor a mayor o un BTDLList vacio si el arbol esta vacio.
     *
     * @return Iterator con las llaves contenidas en el arbol
     */
    public BTDLList<Key> getKeys() {
        BTDLList<Key> Result;

        Result = new BTDLList<Key>();
        this.lock.lock();
        try {
            inorder_keys(Result, this.root);
        } finally {
            this.lock.unlock();
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
     * entre [MinKey,MaxKey].
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
     * Retorna un BTDLList con los valores contenidos en el arbol ordenadas de
 menor a mayor en base a la llave o un BTDLList vacio si el arbol esta
 vacio.
     *
     * @return Iterator con los valores contenidos en el arbol
     */
    public BTDLList<Value> getValues() {
        BTDLList<Value> Result;

        Result = new BTDLList<Value>();
        this.lock.lock();
        try {
            inorder_values(Result, this.root);
        } finally {
            this.lock.unlock();
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
            Arreglo.add(n.value);
            inorder_values(Arreglo, n.right);
        }
    }

    /**
     * Retorna la llave minima del arbol o NULL si el arbol esta vacio.
     *
     * @return Llave minima del arbol o NULL si el arbol esta vacio
     */
    public Key getMinKey() {
        Key Result = null;
        Node n;

        this.lock.lock();
        try {
            if (this.root != null) {
                n = this.root;
                while (n.left != null) {
                    n = n.left;
                }
                this.root = splay(this.root, n.key);
                Result = this.root.key;
            }
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Retorna el valor minimo del arbol o NULL si el arbol esta vacio.
     *
     * @return Valor minima del arbol o NULL si el arbol esta vacio
     */
    public Value getMinValue() {
        Value Result = null;
        Node n;

        this.lock.lock();
        try {
            if (this.root != null) {
                n = this.root;
                while (n.left != null) {
                    n = n.left;
                }
                this.root = splay(this.root, n.key);
                Result = this.root.value;
            }
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Retorna la llave maxima del arbol o NULL si el arbol esta vacio.
     *
     * @return Llave maxima del arbol o NULL si el arbol esta vacio
     */
    public Key getMaxKey() {
        Key Result = null;
        Node n;

        this.lock.lock();
        try {
            if (this.root != null) {
                n = this.root;
                while (n.right != null) {
                    n = n.right;
                }
                this.root = splay(this.root, n.key);
                Result = this.root.key;
            }
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Retorna el valor maxima del arbol o NULL si el arbol esta vacio.
     *
     * @return Valor maxima del arbol o NULL si el arbol esta vacio
     */
    public Value getMaxValue() {
        Value Result = null;
        Node n;

        this.lock.lock();
        try {
            if (this.root != null) {
                n = this.root;
                while (n.right != null) {
                    n = n.right;
                }
                this.root = splay(this.root, n.key);
                Result = this.root.value;
            }
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Retorna la llave siguiente a la llave especificada key.
     *
     * @param key Llave a la cual se le debe buscar su sucesor.
     * @return Llave sucesora a la llave especificada
     */
    public Key getSuccessorKey(Key key) {
        int cmp;
        Node n;
        Key Result = null;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp == 0) {
                        if (this.root.right != null) {
                            n = this.root.right;
                            while (n.left != null) {
                                n = n.left;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.key;
                        }
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor siguiente a la llave especificada key.
     *
     * @param key Llave a la cual se le debe buscar su sucesor.
     * @return Valor sucesor a la llave especificada
     */
    public Value getSuccessorValue(Key key) {
        int cmp;
        Node n;
        Value Result = null;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp == 0) {
                        if (this.root.right != null) {
                            n = this.root.right;
                            while (n.left != null) {
                                n = n.left;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.value;
                        }
                    }
                }
            } finally {
                this.lock.unlock();
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
        Node n;
        Key Result = null;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp == 0) {
                        if (this.root.left != null) {
                            n = this.root.left;
                            while (n.right != null) {
                                n = n.right;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.key;
                        }
                    }
                }
            } finally {
                this.lock.unlock();
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
        Node n;
        Value Result = null;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp == 0) {
                        if (this.root.left != null) {
                            n = this.root.left;
                            while (n.right != null) {
                                n = n.right;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.value;
                        }
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Verifica si la llave es contenida en el arbol.
     *
     * @param key Llave a verificar
     * @return TRUE si la llave esta presente y FALSE si no
     */
    public boolean containsKey(Key key) {
        boolean Result = false;

        if (key != null) {
            if (this.get(key) != null) {
                Result = true;
            }
        }
        return Result;
    }

    /**
     * Retorna la llave o la mayor llave que es menor que la llave buscada o
     * NULL si no existe.
     *
     * @param key Llave de busqueda
     * @return mayor llave que es menor que la llave buscada o NULL si no existe
     */
    public Key getFloorKey(Key key) {
        int cmp;
        Key Result = null;
        Node n;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp < 0) {
                        if (this.root.left != null) {
                            n = this.root.left;
                            while (n.right != null) {
                                n = n.right;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.key;
                        }
                    } else {
                        Result = this.root.key;
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna la llave o la menor llave que es mayor que la llave buscada o
     * NULL si no existe.
     *
     * @param key Llave de busqueda
     * @return menor llave que es mayor que la llave buscada o NULL si no existe
     */
    public Key getCeilingKey(Key key) {
        int cmp;
        Key Result = null;
        Node n;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp > 0) {
                        if (this.root.right != null) {
                            n = this.root.right;
                            while (n.left != null) {
                                n = n.left;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.key;
                        }
                    } else {
                        Result = this.root.key;
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor o el valor asociado a la mayor llave que es menor que la
     * llave buscada o NULL si no existe.
     *
     * @param key Llave de busqueda
     * @return Valor asociado a la mayor llave que es menor que la llave buscada
     * o NULL si no existe
     */
    public Value getFloorValue(Key key) {
        int cmp;
        Value Result = null;
        Node n;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp < 0) {
                        if (this.root.left != null) {
                            n = this.root.left;
                            while (n.right != null) {
                                n = n.right;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.value;
                        }
                    } else {
                        Result = this.root.value;
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Retorna el valor o el valor asociado a la menor llave que es mayor que la
     * llave buscada o NULL si no existe.
     *
     * @param key Llave de busqueda
     * @return Valor asociado a la menor llave que es mayor que la llave buscada
     * o NULL si no existe
     */
    public Value getCeilingValue(Key key) {
        int cmp;
        Value Result = null;
        Node n;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp > 0) {
                        if (this.root.right != null) {
                            n = this.root.right;
                            while (n.left != null) {
                                n = n.left;
                            }
                            this.root = splay(this.root, n.key);
                            Result = this.root.value;
                        }
                    } else {
                        Result = this.root.value;
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Retrona el valor asociado con la llave especificada o NULL si no existe
     * la llave en el arbol.
     *
     * @param key Llave de busqueda
     * @return Valor asociado con la llave o NULL si la llave no existe
     */
    public Value get(Key key) {
        int cmp;
        Value Result = null;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp == 0) {
                        Result = this.root.value;
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
        return Result;
    }

    /**
     * Inserta el valor en el arbor y realiza el ordenamiento en base a la llave
     * especificada.
     *
     * @param key Llave de busqueda del valor
     * @param value Valor asociado a la llave
     */
    public void put(Key key, Value value) {
        int cmp;
        Node n;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    // splay key to root
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    // Insert new node at root's left
                    if (cmp < 0) {
                        n = new Node(key, value);
                        n.left = this.root.left;
                        if (n.left != null) {
                            n.left.parent = n;
                        }
                        n.right = this.root;
                        this.root.left = null;
                        this.root.parent = n;
                        this.root = n;
                    } // Insert new node at root's right
                    else if (cmp > 0) {
                        n = new Node(key, value);
                        n.right = this.root.right;
                        if (n.right != null) {
                            n.right.parent = n;
                        }
                        n.left = this.root;
                        this.root.right = null;
                        this.root.parent = n;
                        this.root = n;
                    } // It was a duplicate key. Simply replace the value
                    else if (cmp == 0) {
                        this.root.value = value;
                    }
                } else {
                    // Add first node to a empty tree
                    this.root = new Node(key, value);
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    /**
     * Realiza el borrado de un nodo en base a la llave especificada.
     *
     * @param key Llave del nodo a eliminar en el arbol
     */
    public void delete(Key key) {
        int cmp;
        Node x;

        if (key != null) {
            this.lock.lock();
            try {
                if (this.root != null) {
                    this.root = splay(this.root, key);
                    cmp = key.compareTo(this.root.key);
                    if (cmp == 0) {
                        if (this.root.left == null) {
                            this.root = this.root.right;
                            if (this.root != null) {
                                this.root.parent = null;
                            }
                        } else {
                            x = this.root.right;
                            this.root = splay(this.root.left, key);
                            this.root.parent = null;
                            this.root.right = x;
                            if (x != null) {
                                x.parent = this.root;
                            }
                        }
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }
    }

    /**
     * Elimina la llave menor y el valor asociado en arbol, si el arbol esta
     * vacio no hace ninguna operacion.
     */
    public void deleteMin() {
        Key Result;
        Node n;

        this.lock.lock();
        try {
            if (this.root != null) {
                n = this.root;
                while (n.left != null) {
                    n = n.left;
                }
                Result = n.key;
                this.root = splay(this.root, Result);
                if (this.root.left == null) {
                    this.root = this.root.right;
                    if (this.root != null) {
                        this.root.parent = null;
                    }
                } else {
                    n = this.root.right;
                    this.root = splay(this.root.left, Result);
                    this.root.parent = null;
                    this.root.right = n;
                    if (n != null) {
                        n.parent = this.root;
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Elimina la llave mayor y el valor asociado en el arbol, si el arbol esta
     * vacio no hace ninguna operacion.
     */
    public void deleteMax() {
        Key Result;
        Node n;

        this.lock.lock();
        try {
            if (this.root != null) {
                n = this.root;
                while (n.right != null) {
                    n = n.right;
                }
                Result = n.key;
                this.root = splay(this.root, Result);
                if (this.root.left == null) {
                    this.root = this.root.right;
                    if (this.root != null) {
                        this.root.parent = null;
                    }
                } else {
                    n = this.root.right;
                    this.root = splay(this.root.left, Result);
                    this.root.parent = null;
                    this.root.right = n;
                    if (n != null) {
                        n.parent = this.root;
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Realiza el Splay de la llave key en el arbol con raiz en el nodo n, si un
     * nodo con la llave key existe es splayed hacia la raiz del arbol; si el
     * nodo no existe el ultimo nodo de la ruta de busqueda es splayed hacia la
     * raiz del arbol.
     *
     * @param h Nodo raiz del subarbol a ser splayed
     * @param key llave a ser splayed hacia la raiz
     * @return Arbol con el nuevo nodo raiz resultado de la operacion de splay
     */
    private Node splay(Node h, Key key) {
        int cmp1, cmp2;
        Node Result = null;

        if (h != null) {
            cmp1 = key.compareTo(h.key);
            if (cmp1 < 0) {
                if (h.left != null) {
                    cmp2 = key.compareTo(h.left.key);
                    if (cmp2 < 0) {
                        h.left.left = splay(h.left.left, key);
                        h = rotateRight(h);
                    } else if (cmp2 > 0) {
                        h.left.right = splay(h.left.right, key);
                        if (h.left.right != null) {
                            h.left = rotateLeft(h.left);
                        }
                    }
                    if (h.left != null) {
                        Result = rotateRight(h);
                    } else {
                        Result = h;
                    }
                } else {
                    // key not in tree, so we're done
                    Result = h;
                }
            } else if (cmp1 > 0) {
                if (h.right != null) {
                    cmp2 = key.compareTo(h.right.key);
                    if (cmp2 < 0) {
                        h.right.left = splay(h.right.left, key);
                        if (h.right.left != null) {
                            h.right = rotateRight(h.right);
                        }
                    } else if (cmp2 > 0) {
                        h.right.right = splay(h.right.right, key);
                        h = rotateLeft(h);
                    }
                    if (h.right != null) {
                        Result = rotateLeft(h);
                    } else {
                        Result = h;
                    }
                } else {
                    // key not in tree, so we're done
                    Result = h;
                }
            } else {
                Result = h;
            }
        }
        return Result;
    }

    /**
     * Retorna la altura del subarbol a partir del nodo x, un arbol de un solo
     * nodo tiene altura 0.
     *
     * @param x Nodo a partir del cual se calcula la profundidad del arbol
     * @return profundidad del arbol
     */
    private long height(Node x) {
        long Result = -1;

        if (x != null) {
            Result = 1 + Math.max(height(x.left), height(x.right));
        }
        return Result;
    }

    /**
     * Retorna el alto del arbor, un arbor de un solo nodo tiene altura 0.
     *
     * @return Altura del Arbol
     */
    public long height() {
        long Result = 0;

        this.lock.lock();
        try {
            Result = height(this.root);
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Calcula el tamano del subarbol a partir del nodo x.
     */
    private long size(Node x) {
        long Result = 0;

        if (x != null) {
            Result = 1 + size(x.left) + size(x.right);
        }
        return Result;
    }

    /**
     * Retorna el numero de nodos que tiene el arbol.
     *
     * @return Numero de nodos que tiene el arbol
     */
    public long size() {
        long Result = 0;

        this.lock.lock();
        try {
            Result = size(this.root);
        } finally {
            this.lock.unlock();
        }
        return Result;
    }

    /**
     * Rota el subarbol hacia la derecha en el nodo h.
     */
    private Node rotateRight(Node h) {
        Node x;

        x = h.left;
        h.left = x.right;
        if (h.left != null) {
            h.left.parent = h;
        }
        x.right = h;
        x.parent = h.parent;
        h.parent = x;
        return x;
    }

    /*
     * Rota el subarbol hacia la izquierda en el nodo h.
     */
    private Node rotateLeft(Node h) {
        Node x;

        x = h.right;
        h.right = x.left;
        if (h.right != null) {
            h.right.parent = h;
        }
        x.left = h;
        x.parent = h.parent;
        h.parent = x;
        return x;
    }
}
