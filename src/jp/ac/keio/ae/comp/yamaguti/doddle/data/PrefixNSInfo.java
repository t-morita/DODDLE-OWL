/*
 * @(#) PrefixNSInfo.java
 *
 *
 * Copyright (C) 2003-2006 The MMM Project
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package jp.ac.keio.ae.comp.yamaguti.doddle.data;

/**
 * PrefixNSSet
 * 
 * @author takeshi morita
 */
public class PrefixNSInfo {

    private String prefix;
    private String nameSpace;
    private boolean isAvailable;

    public PrefixNSInfo(String p, String ns, boolean t) {
        prefix = p;
        nameSpace = ns;
        isAvailable = t;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String toString() {
        return "prefix: " + prefix + " | NameSpace: " + nameSpace + " | available: " + isAvailable;
    }
}
