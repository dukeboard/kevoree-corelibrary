package org.kevoree.library.javase.voldemort.utils;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 11/07/13
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
public class StringInt {
    private final String s;
    private final int i;

    static StringInt valueOf( String string , int value ) {
        return new StringInt( string, value );
    }
    private StringInt( String string, int value ) {
        this.s = string;
        this.i = value;
    }
    public boolean equals( Object o ) {
        if( o != null && o instanceof StringInt ){
            StringInt other = ( StringInt ) o;
            return this.s == other.s && this.i == other.i;
        }

        return false;
    }
    public int hashCode() {
        return s != null ? s.hashCode() * 37 + i : i;
    }
}