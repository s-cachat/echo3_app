/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2009 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */
package nextapp.echo.app.serial.property;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import nextapp.echo.app.Extent;
import nextapp.echo.app.SingleTransition;
import nextapp.echo.app.Transition;
import nextapp.echo.app.TransitionBehavior;
import nextapp.echo.app.TransitionFunction;
import nextapp.echo.app.serial.SerialException;
import nextapp.echo.app.serial.SerialPropertyPeer;
import nextapp.echo.app.util.Context;
import nextapp.echo.app.util.DomUtil;

/**
 * <code>SerialPropertyPeer</code> for <code>Insets</code> properties.
 */
public class TransitionPeer
        implements SerialPropertyPeer {

    /**
     * Generates an <code>Insets</code> property from a string representation.
     *
     * @param value the string representation
     * @return the generated <code>Insets</code> value
     * @throws SerialException if the provided string representation is not
     * valid
     */
    public static Transition fromString(String value)
            throws SerialException {
        if (value == null) {
            return null;
        }
        Transition t = new Transition();
        String[] items = value.splitWithDelimiters("[ \n,]+",-1);
        int step = 0;
        SingleTransition st = null;
        for (String s : items) {
            if (s.trim().equals(",")) {
                t.getTransitions().add(st);
                st = null;
                step = 0;
            }else if (s.equals(" ")||s.equals("\n")){
                //nop
            }else {
                Integer time = testTime(s);
                TransitionFunction function = null;
                TransitionBehavior behavior = null;
                if (time == null) {
                    function = testFunction(s);
                    if (function == null) {
                        behavior = testBehavior(s);
                    }
                }
                switch (step) {
                    case 0 -> {
                        st = new SingleTransition();
                        if (time != null) {
                            st.setTime1Ms(time);
                            step = 2;
                        } else if (function != null) {
                            st.setFunction(function);
                            step = 3;
                        } else if (behavior != null) {
                            st.setBehavior(behavior);
                            step = 5;
                        } else {
                            st.setProperty(s);
                            step = 1;
                        }
                    }
                    case 1 -> {
                        if (time != null) {
                            st.setTime1Ms(time);
                            step = 2;
                        } else if (function != null) {
                            st.setFunction(function);
                            step = 3;
                        } else if (behavior != null) {
                            st.setBehavior(behavior);
                            step = 5;
                        } else {
                            throw new SerialException("Invalid transition (expect time, function, behavior, got " + s + ") : " + value, null);
                        }
                    }
                    case 2 -> {
                        if (time != null) {
                            st.setTime2Ms(time);
                            step = 4;
                        } else if (function != null) {
                            st.setFunction(function);
                            step = 3;
                        } else if (behavior != null) {
                            st.setBehavior(behavior);
                            step = 5;
                        } else {
                            throw new SerialException("Invalid transition (expect function, second time, behavior, got " + s + ") : " + value, null);
                        }
                    }
                    case 3 -> {
                        if (time != null) {
                            st.setTime2Ms(time);
                            step = 4;
                        } else if (behavior != null) {
                            st.setBehavior(behavior);
                            step = 5;
                        } else {
                            throw new SerialException("Invalid transition : (expect second time, behavior, got " + s + ") " + value, null);
                        }
                    }
                    case 4 -> {
                        if (behavior != null) {
                            st.setBehavior(behavior);
                            step = 5;
                        } else {
                            throw new SerialException("Invalid transition : (expect second behavior, got " + s + ") " + value, null);
                        }
                    }
                    case 5 ->
                        throw new SerialException("Invalid transition : (expect comma, end, got " + s + ") " + value, null);

                }
            }
        }
        if (st != null) {
            t.getTransitions().add(st);
        }
        return t;
    }

    public static TransitionFunction testFunction(String s) {
        try {
            for (TransitionFunction t : TransitionFunction.values()) {
                if (t.getValue().equals(s)) {
                    return t;
                }
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static TransitionBehavior testBehavior(String s) {
        try {
            for (TransitionBehavior t : TransitionBehavior.values()) {
                if (t.getValue().equals(s)) {
                    return t;
                }
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Integer testTime(String s) {
        Pattern timePattern = Pattern.compile("(\\d+(\\.\\d*)?)(m?s)");
        Matcher mat = timePattern.matcher(s);
        if (mat.matches()) {
            double value = Double.parseDouble(mat.group(1));
            switch (mat.group(3)) {
                case "s" ->
                    value *= 1000;
            }
            return (int) value;
        } else {
            return null;
        }
    }

    /**
     * @see nextapp.echo.app.serial.SerialPropertyPeer#toProperty(Context,
     * Class, org.w3c.dom.Element)
     */
    @Override
    public Object toProperty(Context context, Class objectClass, Element propertyElement)
            throws SerialException {
        return fromString(propertyElement.hasAttribute("v")
                ? propertyElement.getAttribute("v") : DomUtil.getElementText(propertyElement));
    }

    /**
     * @see
     * nextapp.echo.app.serial.SerialPropertyPeer#toXml(nextapp.echo.app.util.Context,
     * java.lang.Class, org.w3c.dom.Element, java.lang.Object)
     */
    @Override
    public void toXml(Context context, Class objectClass, Element propertyElement, Object propertyValue)
            throws SerialException {
        propertyElement.appendChild(propertyElement.getOwnerDocument().createTextNode(((Transition) propertyValue).compile()));
    }
}
