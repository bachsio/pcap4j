package org.pcap4j.packet;

import static org.junit.Assert.*;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcap4j.packet.IpV6ExtHopByHopOptionsPacket.IpV6ExtHopByHopOptionsHeader;
import org.pcap4j.packet.IpV6ExtOptionsPacket.IpV6Option;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.packet.namednumber.UdpPort;
import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("javadoc")
public class IpV6ExtHopByHopOptionsPacketTest extends AbstractPacketTest {

  private static final Logger logger
    = LoggerFactory.getLogger(IpV6ExtHopByHopOptionsPacketTest.class);

  private final IpNumber nextHeader;
  private final byte hdrExtLen;
  private final List<IpV6Option> options;
  private final IpV6ExtHopByHopOptionsPacket packet;

  private final Inet6Address srcAddr;
  private final Inet6Address dstAddr;

  public IpV6ExtHopByHopOptionsPacketTest() throws Exception {
    this.nextHeader = IpNumber.UDP;
    this.hdrExtLen = (byte)0;
    this.options = new ArrayList<IpV6Option>();
    options.add(IpV6Pad1Option.getInstance());
    options.add(
      new IpV6PadNOption.Builder()
        .data(new byte[] { 0, 0, 0 })
        .dataLen((byte)3)
        .build()
    );

    try {
      srcAddr
        = (Inet6Address)InetAddress.getByName("2001:db8::3:2:1");
      dstAddr
        = (Inet6Address)InetAddress.getByName("2001:db8::3:2:2");
    } catch (UnknownHostException e) {
      throw new AssertionError();
    }

    UnknownPacket.Builder anonb = new UnknownPacket.Builder();
    anonb.rawData(new byte[] { (byte)0, (byte)1, (byte)2, (byte)3 });

    UdpPacket.Builder udpb = new UdpPacket.Builder();
    udpb.dstPort(UdpPort.getInstance((short)0))
        .srcPort(UdpPort.SNMP_TRAP)
        .dstAddr(dstAddr)
        .srcAddr(srcAddr)
        .payloadBuilder(anonb)
        .correctChecksumAtBuild(true)
        .correctLengthAtBuild(true);

    IpV6ExtHopByHopOptionsPacket.Builder b
      = new IpV6ExtHopByHopOptionsPacket.Builder();
    b.nextHeader(nextHeader)
     .hdrExtLen(hdrExtLen)
     .options(options)
     .correctLengthAtBuild(false)
     .payloadBuilder(udpb);

    this.packet = b.build();
  }

  @Override
  protected Packet getPacket() {
    return packet;
  }

  @Override
  protected Packet getWholePacket() {
    IpV6Packet.Builder IpV6b = new IpV6Packet.Builder();
    IpV6b.version(IpVersion.IPV6)
         .trafficClass(IpV6SimpleTrafficClass.newInstance((byte)0x12))
         .flowLabel(IpV6SimpleFlowLabel.newInstance(0x12345))
         .nextHeader(IpNumber.IPV6_HOPOPT)
         .hopLimit((byte)100)
         .srcAddr(srcAddr)
         .dstAddr(dstAddr)
         .payloadBuilder(packet.getBuilder())
         .correctLengthAtBuild(true);

    EthernetPacket.Builder eb = new EthernetPacket.Builder();
    eb.dstAddr(MacAddress.getByName("fe:00:00:00:00:02"))
      .srcAddr(MacAddress.getByName("fe:00:00:00:00:01"))
      .type(EtherType.IPV6)
      .payloadBuilder(IpV6b)
      .paddingAtBuild(true);

    eb.get(UdpPacket.Builder.class)
      .dstAddr(dstAddr)
      .srcAddr(srcAddr);
    return eb.build();
  }

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    logger.info(
      "########## " + IpV6ExtHopByHopOptionsPacketTest.class.getSimpleName() + " START ##########"
    );
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Test
  public void testNewPacket() {
    IpV6ExtHopByHopOptionsPacket p
      = IpV6ExtHopByHopOptionsPacket.newPacket(packet.getRawData());
    assertEquals(packet, p);
  }

  @Test
  public void testGetHeader() {
    IpV6ExtHopByHopOptionsHeader h = packet.getHeader();
    assertEquals(nextHeader, h.getNextHeader());
    assertEquals(hdrExtLen, h.getHdrExtLen());
    assertEquals(options.size(), h.getOptions().size());
    Iterator<IpV6Option> iter = h.getOptions().iterator();
    for (IpV6Option opt: options) {
      assertEquals(opt, iter.next());
    }

    IpV6ExtHopByHopOptionsPacket.Builder b = packet.getBuilder();
    IpV6ExtHopByHopOptionsPacket p;

    b.hdrExtLen((byte)0);
    p = b.build();
    assertEquals((byte)0, (byte)p.getHeader().getHdrExtLenAsInt());

    b.hdrExtLen((byte)-1);
    p = b.build();
    assertEquals((byte)-1, (byte)p.getHeader().getHdrExtLenAsInt());

    b.hdrExtLen((byte)127);
    p = b.build();
    assertEquals((byte)127, (byte)p.getHeader().getHdrExtLenAsInt());

    b.hdrExtLen((byte)-128);
    p = b.build();
    assertEquals((byte)-128, (byte)p.getHeader().getHdrExtLenAsInt());
  }

}
