package com.kdocke.fastjson;

import java.io.Serializable;
import java.util.*;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/19 - 15:43
 */
public class JSONArray extends JSON implements List<Object>, Cloneable, RandomAccess, Serializable {

    private static final long  serialVersionUID = 1L;
    private final List<Object> list;

    public JSONArray(){
        this.list = new ArrayList<>();
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Iterator<Object> iterator() {
        return list.iterator();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean add(Object e) {
        return list.add(e);
    }

    public void add(int index, Object element) {
        list.add(index, element);
    }

    public boolean addAll(Collection<? extends Object> c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends Object> c) {
        return list.addAll(index, c);
    }

    public Object set(int index, Object element) {
        if (index == -1) {
            list.add(element);
            return null;
        }

        if (list.size() <= index) {
            for (int i = list.size(); i < index; ++i) {
                list.add(null);
            }
            list.add(element);
            return null;
        }

        return list.set(index, element);
    }

    public Object get(int index) {
        return list.get(index);
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public List<Object> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public ListIterator<Object> listIterator() {
        return list.listIterator();
    }

    public ListIterator<Object> listIterator(int index) {
        return list.listIterator(index);
    }

    public void clear() {
        list.clear();
    }

}
