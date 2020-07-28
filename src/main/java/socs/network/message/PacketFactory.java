package socs.network.message;

public class PacketFactory {
    public SOSPFPacket getConnRefused(String srcProcessIP, short srcProcessPort, String srcIP, String dstIP) {
        SOSPFPacket msg = new SOSPFPacket(srcProcessIP, srcProcessPort, srcIP, dstIP, (short) -1);
        return msg;
    }

    public SOSPFPacket getHello(String srcProcessIP, short srcProcessPort, String srcIP, String dstIP) {
        SOSPFPacket msg = new SOSPFPacket(srcProcessIP, srcProcessPort, srcIP, dstIP, (short) 0);
        return msg;
    }

    public SOSPFPacket getLSAUPDATE(String srcProcessIP, short srcProcessPort, String srcIP, String dstIP) {
        SOSPFPacket msg = new SOSPFPacket(srcProcessIP, srcProcessPort, srcIP, dstIP, (short) 1);
        return msg;
    }
}
