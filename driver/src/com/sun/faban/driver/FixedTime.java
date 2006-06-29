/* The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html or
 * install_dir/legal/LICENSE
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at install_dir/legal/LICENSE.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id: FixedTime.java,v 1.2 2006/06/29 19:38:36 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * The FixedTime annotation defines a fixed cycle or think time.
 * The attributes of this annotation defines the type, time, and
 * allowed deviation from the given time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface FixedTime {
    /** The type of cycle to be used */
    CycleType cycleType() default CycleType.CYCLETIME;

    /** The cycle or think time in milliseconds */
    int cycleTime() default 1000;

    /**
     * Causes the starting cycle or initial cycle time to be a random
     * selected time between 0 and cycleTime. This is used to distribute
     * the initial request randomly and prevent request thundering.
     * Subsequent requests still strictly adhere to the fixed cycle time.
     */
    boolean randomStart() default false;

    /** The allowed deviation from the targeted time, in % */
    double cycleDeviation();
}
