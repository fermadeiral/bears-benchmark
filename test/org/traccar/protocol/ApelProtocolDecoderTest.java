package org.traccar.protocol;

import org.junit.Test;
import org.traccar.ProtocolTest;

public class ApelProtocolDecoderTest extends ProtocolTest {

    @Test
    public void testDecode() throws Exception {

        ApelProtocolDecoder decoder = new ApelProtocolDecoder(null);

        /*byte[] buf1 = {0x40,0x4E,0x54,0x43,0x01,0x00,0x00,0x00,0x7B,0x00,0x00,0x00,0x13,0x00,0x44,0x34,0x2A,0x3E,0x53,0x3A,0x38,0x36,0x31,0x37,0x38,0x35,0x30,0x30,0x35,0x32,0x30,0x35,0x30,0x37,0x39};
        verifyNull(decoder, text( ChannelBuffers.wrappedBuffer(ByteOrder.LITTLE_ENDIAN, buf1)));*/

        //0c002900f12a00000f003235303032363533343135313036340f0033353638393530333632373938313101002000000000
        //5c00380046e6a95136b693277f11b41a00172709f2ff03160002b9bc630007000000000000000000000000000000c31071090000880500000000000000000000
        //5c00380072e7a95136b693277f11b41a00172709f2ff03160002b9bc630007000000000000000000000000000000c31071090000880500000000000000000000

        //7900040069ea030000000000
        //8300c20003006aea03005c003800223aab5107a393276617b41a0030d506e3000414010250bf630007000000000000000000000000000000c3107209000089050000000000006bea03005c003800403aab5107a393276617b41a0030d506e3000414010250bf630007000000000000000000000000000000c3107209000089050000000000006cea03005c0038005e3aab5107a393276617b41a0030d506e3000414010250bf630007000000000000000000000000000000c31072090000890500000000000000000000

    }

}
