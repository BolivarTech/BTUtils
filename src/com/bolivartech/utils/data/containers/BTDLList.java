package com.bolivartech.utils.data.containers;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>
 * Copyright 2015 BolivarTech LLC
 * </p>
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * <p>
 * This Class is the BolivarTech's util that implement BolivarTech's Double
 * Linked List data structure.
 *
 * </p>
 * Implementa una clase que define la estructura de datos de una Lista
 * doblemente enlazada
 *
 * Clase Thread Safe
 *
 * @author Julian Bolivar
 * @version 2.0.0
 * @since 2015 - December 20, 2015
 *
 * Change Logs: 
 * v1.0.0 (10/12/2015): Version Inicial.
 * v2.0.0 (12/20/2015): Se agrego soporte para concurrencia
 *
 * @param <Item> El tipo de datos generico de los Items en la cola.
 */
public class BTDLList<Item> implements Iterable<Item> {

    // Lock para el manejo de concurrencia
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private long N;              // numero de elementos en la cola
    private Node<Item> first;    // principio de la cola
    private Node<Item> last;     // final de la cola
    private Node<Item> current;  // Elemento actual en la cola

    /**
     * Clase privada para implementar el Nodo de la cola
     */
    private static class Node<Item> {

        private Item item;
        private Node<Item> next;
        private Node<Item> previus;

        public Node() {

            item = null;
            next = null;
            previus = null;
        }

        public Node(Item item, Node<Item> next, Node<Item> previus) {

            this.item = item;
            this.next = next;
            this.previus = previus;
        }

    }

    /**
     * Constructor por defecto de la lista
     */
    public BTDLList() {
        this.first = null;
        this.last = null;
        this.current = null;
        this.N = 0;
    }

    /**
     * Retorna TRUE si la lista esta vacia o FALSE si no
     *
     * @return TRUE si la lista esta vacia o FALSE si no
     */
    public boolean isEmpty() {
        boolean Result = false;

        this.lock.readLock().lock();
        try {
            if (this.first == null) {
                Result = true;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el tamaño de la lista.
     *
     * @return El numero de elementos contenidos en la lista
     */
    public long size() {
        long Tamano;

        this.lock.readLock().lock();
        try {
            Tamano = this.N;
        } finally {
            this.lock.readLock().unlock();
        }
        return Tamano;
    }

    /**
     * Coloca el cursor en el primer elemento de la lista
     */
    public void seekFisrt() {

        this.lock.writeLock().lock();
        try {
            if (this.first != null) {
                this.current = this.first;
            } else {
                this.last = null;
                this.current = null;
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Coloca el cursor en el ultimo elemento de la lista
     */
    public void seekLast() {

        this.lock.writeLock().lock();
        try {
            if (this.first != null) {
                this.current = this.last;
            } else {
                this.last = null;
                this.current = null;
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Coloca el cursor en el siguiente elemento de la lista, retornando TRUE si
     * fue satistactorio o FALSE si no hay mas elementos en la lista.
     *
     * @return TRUE si fue satisfactorio o FALSE si no hay mas elementos en la
     * lista
     */
    public boolean seekNext() {
        boolean Result = false;

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                if (this.current.next != null) {
                    this.current = this.current.next;
                    Result = true;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return Result;
    }

    /**
     * Coloca el cursor en el elemento anterior de la lista, retornando TRUE si
     * fue satistactorio o FALSE si no hay mas elementos en la lista
     *
     * @return TRUE si fue satisfactorio o FALSE si no hay mas elementos en la
     * lista
     */
    public boolean seekPrevius() {
        boolean Result = false;

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                if (this.current.previus != null) {
                    this.current = this.current.previus;
                    Result = true;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna TRUE si existe un siguiente elemento a partir de la posicion
     * actual del cursos en la lista.
     *
     * @return TRUE si existe un proximo elemento o FALSE si no
     */
    public boolean hasNext() {
        boolean Result = false;

        this.lock.readLock().lock();
        try {
            if (this.current != null) {
                Result = (this.current.next != null);
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna TRUE si existe un previo elemento a partir de la posicion actual
     * del cursos en la lista.
     *
     * @return TRUE si existe un previo elemento o FALSE si no
     */
    public boolean hasPrevius() {
        boolean Result = false;

        this.lock.readLock().lock();
        try {
            if (this.current != null) {
                Result = (this.current.previus != null);
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el elemento actual al que apunta el cursor de la lista.
     *
     * NOT: Esta funcion no realiza ningun desplazamiento del cursor
     *
     * @return El elemento al que apunta el cursor de la lista.
     */
    public Item getCurrent() {
        Item Result = null;

        this.lock.readLock().lock();
        try {
            if (this.current != null) {
                Result = this.current.item;
            }
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el elemento previo a la posicion actual del cursor en la lista y
     * el cursor queda apuntado a dicho elemento, si llega al principio de la
     * lista retorna NULL.
     *
     * @return El elemento anterior al que apunta el cursor de la lista o NULL
     * si llego al final de la lista.
     */
    public Item getPrevius() {
        Item Result = null;

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                if (this.current.previus != null) {
                    this.current = this.current.previus;
                    Result = this.current.item;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return Result;
    }

    /**
     * Retorna el elemento siguiente a la posicion actual del cursor en la lista
     * y el cursor queda apuntado al elemento, si llega al final de la lista
     * retorna NULL.
     *
     * @return El elemento siguiente al que apunta el cursor de la lista o NULL
     * si llego al final de la lista.
     */
    public Item getNext() {
        Item Result = null;

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                if (this.current.next != null) {
                    this.current = this.current.next;
                    Result = this.current.item;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return Result;
    }

    /**
     * Agrega el elemento 'item' al final de la lista
     *
     * @param item Elemento a ser agregado a la lista
     */
    public void add(Item item) {
        Node<Item> oldlast;

        this.lock.writeLock().lock();
        try {
            oldlast = this.last;
            this.last = new Node<Item>(item, null, oldlast);
            if (this.first == null) {
                this.first = this.last;
                this.current = this.first;
            } else {
                oldlast.next = this.last;
            }
            this.N++;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Inserta el elemento 'item' en la posicion anterior del cursor.
     *
     * NOTA: El cursor continua apuntado al elemento donde se realizo la
     * insercion, es decir no se desplaza el cursor
     *
     * @param item Elemento a ser agregado en la posicion actual de la lista
     */
    public void preinsert(Item item) {
        Node<Item> newItem;

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                newItem = new Node<Item>(item, this.current, this.current.previus);
                if (this.current.previus != null) {
                    this.current.previus.next = newItem;
                }
                this.current.previus = newItem;
            } else {
                newItem = this.last;
                this.last = new Node<Item>(item, null, newItem);
                if (this.first == null) {
                    this.first = this.last;
                    this.current = this.first;
                } else {
                    newItem.next = this.last;
                }
            }
            this.N++;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Inserta el elemento 'item' en la posicion posterior del cursor.
     *
     * NOTA: El cursor continua apuntado al elemento donde se realizo la
     * insercion, es decir no se desplaza el cursor
     *
     * @param item Elemento a ser agregado en la posicion actual de la lista
     */
    public void posinsert(Item item) {
        Node<Item> newItem;

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                newItem = new Node<Item>(item, this.current.next, this.current);
                if (this.current.next != null) {
                    this.current.next.previus = newItem;
                }
                this.current.next = newItem;
            } else {
                newItem = this.last;
                this.last = new Node<Item>(item, null, newItem);
                if (this.first == null) {
                    this.first = this.last;
                    this.current = this.first;
                } else {
                    newItem.next = this.last;
                }
            }
            this.N++;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Elimina el elemento al que apunta el cursor de la lista.
     */
    public void remove() {

        this.lock.writeLock().lock();
        try {
            if (this.current != null) {
                if (this.current.next != null) {
                    this.current.next.previus = this.current.previus;
                }
                if (this.current.previus != null) {
                    this.current.previus.next = this.current.next;
                    this.current = this.current.previus;
                } else {
                    this.current = this.current.next;
                }
                this.N--;
            } else {
                this.first = null;
                this.last = null;
                this.N = 0;
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Limpia la lista de todo su contenido
     */
    public void clean() {

        this.lock.writeLock().lock();
        try {
            this.current = null;
            this.first = null;
            this.last = null;
            this.N = 0;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Retorna un string que representa la cola.
     *
     * @return La secuencia de elementos en el orden de la cola y separado por
     * ','
     */
    @Override
    public String toString() {
        StringBuilder s;

        s = new StringBuilder();
        this.lock.readLock().lock();
        try {
            for (Item item : this) {
                s.append(item);
                s.append(",");
            }
        } finally {
            this.lock.readLock().unlock();
        }
        s.deleteCharAt(s.lastIndexOf(","));
        return s.toString();
    }

    /**
     * Retorna un Iterator que opera sobre los elementos en la lista,
     * retornandolos en el mismo order en el que estan almacenados.
     *
     * @return Iterator que opera sobre los items de la cola.
     */
    @Override
    public Iterator<Item> iterator() {
        ListIterator<Item> Result;

        this.lock.readLock().lock();
        try {
            Result = new ListIterator<Item>(this.first);
        } finally {
            this.lock.readLock().unlock();
        }
        return Result;
    }

    /**
     * Clase privada que implementa el Iterator.
     *
     * @param <Item> El tipo de datos generico de los Items en el Iterator
     */
    private class ListIterator<Item> implements Iterator<Item> {

        // Lock para el manejo de concurrencia
        private final ReentrantReadWriteLock ilock = new ReentrantReadWriteLock();

        // Apuntador al elemento actual de la cola.
        private Node<Item> current;

        /**
         * Constructor del Iterator
         *
         * @param first Primer elemento en la lista.
         */
        public ListIterator(Node<Item> first) {

            current = first;
        }

        /**
         * Retorna TRUE si tiene mas elementos en la cola o FALSE si no.
         *
         * @return TRUE si hay mas elementos o FALSE si no.
         */
        @Override
        public boolean hasNext() {
            boolean Result = false;

            this.ilock.readLock().lock();
            try {
                if (current != null) {
                    Result = true;
                }
            } finally {
                this.ilock.readLock().unlock();
            }
            return Result;
        }

        /**
         * Elimina el elemento actual del Iterator
         */
        @Override
        public void remove() {
            this.next();
        }

        /**
         * Retorna el proximo elemento en el Iterator o NULL si no hay mas
         * elemetos disponibles.
         *
         * @return Proximo elemento en el Iterator o NULL si no hay mas
         * elementos.
         */
        @Override
        public Item next() {
            Item Result = null;

            if (this.hasNext()) {
                this.ilock.writeLock().lock();
                try {
                    Result = current.item;
                    current = current.next;
                } finally {
                    this.ilock.writeLock().unlock();
                }
            }
            return Result;
        }
    }
}
