/* 
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2025 S. Cachat
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
package nextapp.echo.app;

import java.io.Serializable;

/**
 * A property which describes a single transition.
 */
public class SingleTransition implements Serializable {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 20070101L;
    private String property;
    private Integer time1Ms;
    private TransitionFunction function;
    private Integer time2Ms;
    private TransitionBehavior behavior;

    /**
     * Creates a new Transition object.
     *
     */
    public SingleTransition() {

    }

    /**
     * Creates a new Transition object with the given parameters.
     *
     * @param time1Ms the first timing value for the function
     */
    public SingleTransition(Integer time1Ms) {
        this(null, time1Ms, null, null, null);
    }

    /**
     * Creates a new Transition object with the given parameters.
     *
     * @param property the property the transision applies to
     * @param time1Ms the first timing value for the function
     */
    public SingleTransition(String property, Integer time1Ms) {
        this(property, time1Ms, null, null, null);
    }

    /**
     * Creates a new Transition object with the given parameters.
     *
     * @param time1Ms the first timing value for the function
     * @param function the easing function
     */
    public SingleTransition(Integer time1Ms, TransitionFunction function) {
        this(null, time1Ms, function, null, null);
    }

    /**
     * Creates a new Transition object with the given parameters.
     *
     * @param property the property the transision applies to
     * @param time1Ms the first timing value for the function
     * @param function the easing function
     */
    public SingleTransition(String property, Integer time1Ms, TransitionFunction function) {
        this(property, time1Ms, function, null, null);
    }

    /**
     * Creates a new Transition object with the given parameters.
     *
     * @param property the property the transision applies to
     * @param time1Ms the first timing value for the function
     * @param function the easing function
     * @param time2Ms the second timing value for the function
     */
    public SingleTransition(String property, Integer time1Ms, TransitionFunction function, Integer time2Ms) {
        this(property, time1Ms, function, time2Ms, null);
    }

    /**
     * Creates a new Transition object with the given parameters.
     *
     * @param property the property the transision applies to
     * @param time1Ms the first timing value for the function
     * @param function the easing function
     * @param time2Ms the second timing value for the function
     * @param behavior the behavior, normal ou discrete
     */
    public SingleTransition(String property, Integer time1Ms, TransitionFunction function, Integer time2Ms, TransitionBehavior behavior) {
        super();
        this.property = property;
        this.time1Ms = time1Ms;
        this.function = function;
        this.time2Ms = time2Ms;
        this.behavior = behavior;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Integer getTime1Ms() {
        return time1Ms;
    }

    public void setTime1Ms(Integer time1Ms) {
        this.time1Ms = time1Ms;
    }

    public TransitionFunction getFunction() {
        return function;
    }

    public void setFunction(TransitionFunction function) {
        this.function = function;
    }

    public Integer getTime2Ms() {
        return time2Ms;
    }

    public void setTime2Ms(Integer time2Ms) {
        this.time2Ms = time2Ms;
    }

    public TransitionBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(TransitionBehavior behavior) {
        this.behavior = behavior;
    }

    /**
     * generate the transition code
     *
     * @return the code
     */
    public String compile() {
        StringBuilder sb = new StringBuilder();
        if (property != null) {
            sb.append(property);
        }
        if (time1Ms != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(time1Ms).append("ms");
        }
        if (function != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(function.getValue());
        }
        if (time2Ms != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(time2Ms).append("ms");
        }
        if (behavior != null) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(behavior.getValue());
        }
        return sb.toString();
    }
}
