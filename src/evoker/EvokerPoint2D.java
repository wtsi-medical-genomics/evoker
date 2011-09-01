/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package evoker;

import java.io.Serializable;

/**
 * The <code>EvokerPoint2D</code> class defines a point representing a location
 * in {@code (x,y)} coordinate space.
 * <p>
 * This class is only the abstract superclass for all objects that
 * store a 2D coordinate.
 * The actual storage representation of the coordinates is left to
 * the subclass.
 *
 * @author      Jim Graham
 * @since 1.2
 */
/**
 * Modified for use in evoker
 */
public class EvokerPoint2D implements Cloneable {

    public static long IDAt = 0;
    public long ID;
    /**
     * The X coordinate of this <code>Point2D</code>.
     * @since 1.2
     * @serial
     */
    public double x;
    /**
     * The Y coordinate of this <code>Point2D</code>.
     * @since 1.2
     * @serial
     */
    public double y;

    /**
     * Constructs and initializes a <code>Point2D</code> with
     * coordinates (0,&nbsp;0).
     * @since 1.2
     */
    public EvokerPoint2D() {
    }

    /**
     * Constructs and initializes a <code>Point2D</code> with the
     * specified coordinates.
     *
     * @param x the X coordinate of the newly
     *          constructed <code>Point2D</code>
     * @param y the Y coordinate of the newly
     *          constructed <code>Point2D</code>
     * @since 1.2
     */
    public EvokerPoint2D(double x, double y) {
        this.x = x;
        this.y = y;
        this.ID = IDAt++;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getY() {
        return y;
    }
    
    public double getID(){
        return ID;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns a <code>String</code> that represents the value
     * of this <code>Point2D</code>.
     * @return a string representation of this <code>Point2D</code>.
     * @since 1.2
     */
    public String toString() {
        return "Point2D.Double[" + x + ", " + y + "]";
    }

    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }
    
    /**
     * Determines whether or not two points are equal. Two instances of
     * <code>Point2D</code> are equal if the values of their
     * <code>x</code> and <code>y</code> member fields, representing
     * their position in the coordinate space, are the same.
     * @param obj an object to be compared with this <code>Point2D</code>
     * @return <code>true</code> if the object to be compared is
     *         an instance of <code>Point2D</code> and has
     *         the same values; <code>false</code> otherwise.
     * @since 1.2 */
    public boolean equals(Object obj) {
        if (obj instanceof EvokerPoint2D) {
            EvokerPoint2D p2d = (EvokerPoint2D) obj;
            return (getX() == p2d.getX()) && (getY() == p2d.getY()) && (getID() == p2d.getID());
        }
        return false;
    }
}
