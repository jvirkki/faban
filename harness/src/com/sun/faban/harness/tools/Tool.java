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
 * $Id: Tool.java,v 1.2 2006/06/29 19:38:43 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.harness.tools;
import com.sun.faban.harness.agent.CmdAgent;

import java.util.List;

/**
 * The methods in this interface are the public face of 
 * all Tools configured. New tools that are dynamically added
 * should implement this interface and belong to the tools package.
 * Note that the GenericTool abstract class implements this interface
 * and provides much of the functionality required by most tools.
 * Tools can simply extend GenericTool.
 *
 * @author Ramesh Ramachandran
 */
public interface Tool extends Runnable {

    /**
     * This method is called to configure the tool
     * @param toolName name of the tool (Executable)
     * @param args list containing arguments to tool
     * @param path
     * @param host name of machine the tool is running on
     * @param masterhost name of master machine
     */
    public void configure(String toolName, List args, String path, String outDir,
                          String host, String masterhost, CmdAgent cmdAgent);

    /**
     * Abort any running tools and exit
     */
    public void kill();

    /**
     * This method is responsible for starting the tool after delay
     * and stopping it after duration. No need to call the stop at
     * the end of the benchmark run if using this method.
     * @param 	delay - time (sec) to delay before starting
     * @param    duration for which the tool should be run
     * @return 	true if tool started successfully
     */
    public boolean start(int delay, int duration);

    /**
     * This method is responsible for starting the tool 
     * @param 	delay - time (sec) to delay before starting
     * @return 	true if tool started successfully
     */
    public boolean start(int delay);

    /**
     * This method is responsible for stopping the tool
     */
    public void stop();
}