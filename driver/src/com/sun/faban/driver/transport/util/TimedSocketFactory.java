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
 * $Id: TimedSocketFactory.java,v 1.2 2006/06/29 19:38:39 akara Exp $
 *
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.faban.driver.transport.util;

import com.sun.faban.driver.transport.http.SocketFactory;

import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.Proxy;
import java.io.IOException;

/**
 * Socket factory to create new timed socket.
 *
 * @author Akara Sucharitakul
 */
public class TimedSocketFactory extends SocketFactory {

    public Socket createSocket(Proxy proxy) {
        return new TimedSocket(proxy);
    }

    public Socket createSocket(String s, int i)
            throws IOException, UnknownHostException {
        return new TimedSocket(s, i);
    }

    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1)
            throws IOException, UnknownHostException {
        return new TimedSocket(s, i, inetAddress, i1);
    }

    public Socket createSocket(InetAddress inetAddress, int i)
            throws IOException {
        return new TimedSocket(inetAddress, i);
    }

    public Socket createSocket(InetAddress inetAddress, int i,
                               InetAddress inetAddress1, int i1)
            throws IOException {
        return new TimedSocket(inetAddress, i, inetAddress1, i1);
    }
}
