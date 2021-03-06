/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 * 
 */
package org.apache.directory.server.dhcp.io;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import org.apache.directory.server.dhcp.messages.DhcpMessage;
import org.apache.directory.server.dhcp.messages.HardwareAddress;
import org.apache.directory.server.dhcp.options.DhcpOption;
import org.apache.directory.server.dhcp.options.OptionsField;
import org.apache.directory.server.dhcp.options.dhcp.DhcpMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DhcpMessageEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(DhcpMessageEncoder.class);
    private static final byte EMPTY_HARDWARE_ADDRESS[] = {};
    private static final byte EMPTY_INET_ADDRESS[] = {0, 0, 0, 0};

    /**
     * Converts a DhcpMessage object into a byte buffer.
     *
     * @param byteBuffer ByteBuffer to put DhcpMessage into
     * @param message DhcpMessage to encode into ByteBuffer
     */
    public void encode(@Nonnull ByteBuffer byteBuffer, @Nonnull DhcpMessage message)
            throws IOException {
        try {
            byteBuffer.put(message.getOp());

            HardwareAddress hardwareAddress = message.getHardwareAddress();

            byteBuffer.put((byte) (null != hardwareAddress ? hardwareAddress.getType() : 0));
            byteBuffer.put((byte) (null != hardwareAddress ? hardwareAddress.getLength() : 0));
            byteBuffer.put((byte) message.getHopCount());
            byteBuffer.putInt(message.getTransactionId());
            byteBuffer.putShort((short) message.getSeconds());
            byteBuffer.putShort(message.getFlags());

            writeAddress(byteBuffer, message.getCurrentClientAddress());
            writeAddress(byteBuffer, message.getAssignedClientAddress());
            writeAddress(byteBuffer, message.getNextServerAddress());
            writeAddress(byteBuffer, message.getRelayAgentAddress());

            writeBytes(byteBuffer, (null != hardwareAddress ? hardwareAddress.getAddress() : EMPTY_HARDWARE_ADDRESS), 16);

            writeString(byteBuffer, message.getServerHostname(), 64);
            writeString(byteBuffer, message.getBootFileName(), 128);

            OptionsField options = message.getOptions();

            // update message type option (if set)
            if (message.getMessageType() != null) {
                options.add(new DhcpMessageType(message.getMessageType()));
            }

            encodeOptions(options, byteBuffer);
        } catch (BufferOverflowException e) {
            throw new IOException("Failed to encode " + message + " into " + byteBuffer, e);
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Failed to encode " + message + " into " + byteBuffer, e);
        }
    }

    /**
     * Write a string to a field of len bytes.
     *
     * @param byteBuffer
     * @param serverHostname
     * @param i
     */
    private void writeString(@Nonnull ByteBuffer byteBuffer, @CheckForNull String string, @Nonnegative int len)
            throws IOException {
        if (string == null)
            string = "";
        byte sbytes[] = string.getBytes(Charsets.ISO_8859_1);
        writeBytes(byteBuffer, sbytes, len);
    }


    /**
     * Write an InetAddress to the byte buffer.
     */
    private void writeAddress(@Nonnull ByteBuffer byteBuffer, @Nonnull InetAddress address) {
        if (null == address) {
            byteBuffer.put(EMPTY_INET_ADDRESS);
        } else {
            byte[] addressBytes = address.getAddress();
            byteBuffer.put(addressBytes);
        }
    }

    /**
     * Write an array of bytes to the buffer. Write exactly len bytes,
     * truncating if more than len, padding if less than len bytes are
     * available.
     */
    private void writeBytes(@Nonnull ByteBuffer byteBuffer, @CheckForNull byte bytes[], @Nonnegative int len) {
        if (bytes != null) {
            int blen = Math.min(len, bytes.length);
            byteBuffer.put(bytes, 0, blen);
            len -= blen;
        }

        while (len-- > 0)
            byteBuffer.put((byte) 0);
    }
    private static final byte[] VENDOR_MAGIC_COOKIE = {(byte) 99, (byte) 130, (byte) 83, (byte) 99};

    public void encodeOptions(@Nonnull OptionsField options, @Nonnull ByteBuffer message) {
        message.put(VENDOR_MAGIC_COOKIE);

        for (DhcpOption option : options) {
            // Option continuation per RFC3396
            byte tag = option.getTag();
            byte[] data = option.getData();
            for (int offset = 0; offset < data.length || offset == 0; offset += 0xFF) {
                int length = Math.min(data.length - offset, 0xFF);
                message.put(tag);
                message.put((byte) length);
                message.put(data, offset, length);
            }
        }

        // add end option
        message.put((byte) 0xff);
    }
}
