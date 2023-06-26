package com.bolivartech.utils.data.containers;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Copyright 2015 BolivarTech LLC
 *
 * <p>
 * BolivarTech Homepage: <a
 * href="http://www.bolivartech.com">http://www.bolivartech.com</a>.
 * </p>
 *
 * This Class is the BolivarTech's util that implement BolivarTech's queue data
 * structure.
 *
 *
 * Implementa una clase que define la estructura de datos de una cola
 *
 * Clase Thread Safe
 *
 * @author Julian Bolivar
 * @version 2.0.0
 * @since 2015 - December 20, 2015
 *
 * Change Logs: 
 * v1.0.0 (10/07/2015): Version Inicial. 
 * v2.0.0 (12/20/2015): Se agrego soporte para concurrencia
 *
 * @param <Item> El tipo de datos generico de los Items en la cola.
 */
public class BTQueue<Item> implements Iterable<Item> {

    // Lock para el manejo de concurrencia
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private long N;               // numero de elementos en la cola
    private Node<Item> first;    // principio de la cola
    private Node<Item> last;     // final de la cola

    /**
     * Clase privada para implementar el Nodo de la cola
     */
    private static class Node<Item> {

        private Item item;
        private Node<Item> next;
    }

    /**
     * Constructor por defecto de la cola
     */
    public BTQueue() {
        this.first = null;
        this.last = null;
        this.N = 0;
    }

    /**
     * Retorna TRUE si la cola esta vacia o FALSE si no
     *
     * @return TRUE si la cola esta vacia o FALSE si no
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
     * Retorna el tamaño de la cola.
     *
     * @return El numero de elementos contenidos en la cola
     */
    public long size() {
        long tamano;

        this.lock.readLock().lock();
        try {
            tamano = this.N;
        } finally {
            this.lock.readLock().unlock();
        }
        return tamano;
    }

    /**
     * Retorna el primer elemento de la cola y la cola queda apuntado al
     * siguiente elemento.
     *
     * @return El elemento que esta en la cabeza de la cola.
     */
    public Item get() {
        Item Result = null;

        this.lock.writeLock().lock();
        try {
            if (this.first != null) {
                Result = this.first.item;
                this.first = this.first.next;
                this.N--;
                if (this.first == null) {
                    this.last = null;
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return Result;
    }

    /**
     * Agrega el elemento 'item' a la cola
     *
     * @param item Elemento a ser agregado a la cola
     */
    public void add(Item item) {
        Node<Item> oldlast;

        this.lock.writeLock().lock();
        try {
            oldlast = this.last;
            this.last = new Node<Item>();
            this.last.item = item;
            this.last.next = null;
            if (this.first == null) {
                this.first = this.last;
            } else {
                oldlast.next = this.last;
            }
            this.N++;
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
     * Retorna un Iterator que opera sobre los elementos en la cola,
     * retornandolos en el mismo order en el que estan almacenados.
     *
     * @return Iterator que opera sobre los items de la cola.
     */
    @Override
    public Iterator<Item> iterator() {
        Iterator<Item> Result;

        this.lock.readLock().lock();
        try {
            Result = new ListIterator<Item>(first);
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
