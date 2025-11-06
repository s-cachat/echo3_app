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
import java.util.ArrayList;
import java.util.List;

/**
 * A property which describes a transition. A transition is made of one or more
 * single transition, each targetting a css property
 */
public class Transition implements Serializable {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 20070101L;

    /**
     * property name to targets all properties
     */
    public String PROPERTY_ALL = "all";

    private List<SingleTransition> transitions;

    public Transition() {
    }

    /**
     * Creates a new Transition, with one SingleTransition
     *
     * @param property the property the transision applies to
     * @param time1Ms the first timing value for the function
     * @param function the easing function
     * @param time2Ms the second timing value for the function
     * @param behavior the behavior, normal ou discrete
     */
    public Transition(String property, int time1Ms, TransitionFunction function, int time2Ms, TransitionBehavior behavior) {
        super();
        getTransitions().add(new SingleTransition(property, time1Ms, function, time2Ms, behavior));
    }

    /**
     * Creates a new Transition, with one SingleTransition
     *
     * @param time1Ms the first timing value for the function
     * @param function the easing function
     */
    public Transition(int time1Ms, TransitionFunction function) {
        super();
        getTransitions().add(new SingleTransition(time1Ms, function));
    }

    /**
     * Creates a new Transition, with one SingleTransition
     *
     * @param time1Ms the first timing value for the function
     */
    public Transition(int time1Ms) {
        super();
        getTransitions().add(new SingleTransition(time1Ms));
    }

    public List<SingleTransition> getTransitions() {
        if (transitions == null) {
            transitions = new ArrayList<>();
        }
        return transitions;
    }

    public void setTransitions(List<SingleTransition> transitions) {
        this.transitions = transitions;
    }

    /**
     * generate the transition code
     *
     * @return the code
     */
    public String compile() {
        StringBuilder sb = new StringBuilder();
        for (SingleTransition t : getTransitions()) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(t.compile());
        }
        return sb.toString();
    }
}
