/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.dhcp.server.pcap;

import org.anarres.dhcp.test.AbstractDhcpServerTest;
import org.apache.directory.server.dhcp.service.store.FixedStoreLeaseManager;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author shevek
 */
public class DhcpServerTest extends AbstractDhcpServerTest {

    @Ignore
    @Test
    public void testServer() throws Exception {
        String INTERFACE_NAME = "eth0";
        FixedStoreLeaseManager manager = newLeaseManager(INTERFACE_NAME);
        DhcpServer server = new DhcpServer(manager);
        // server.addInterfaces(new DhcpInterfaceManager.NamedPredicate(INTERFACE_NAME));
        // assertKosher(server);

        // server.start();
        // Thread.sleep(200000);
        // server.stop();
        server.run();
    }

}
