/*_##########################################################################
  _##
  _##  Copyright (C) 2012  Kaito Yamada
  _##
  _##########################################################################
*/

package org.pcap4j.packet;

import static org.pcap4j.util.ByteArrays.*;
import java.util.ArrayList;
import java.util.List;
import org.pcap4j.util.ByteArrays;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.11
 */
public final class IcmpV4DestinationUnreachablePacket
extends IcmpV4InvokingPacketPacket {

  /**
   *
   */
  private static final long serialVersionUID = 2435425895590241470L;

  private final IcmpV4DestinationUnreachableHeader header;

  /**
   *
   * @param rawData
   * @return a new IcmpV4DestinationUnreachablePacket object.
   */
  public static IcmpV4DestinationUnreachablePacket newPacket(byte[] rawData) {
    IcmpV4DestinationUnreachableHeader header
      = new IcmpV4DestinationUnreachableHeader(rawData);
    byte[] rawPayload
      = ByteArrays.getSubArray(
          rawData,
          header.length(),
          rawData.length - header.length()
        );
    return new IcmpV4DestinationUnreachablePacket(header, rawPayload);
  }

  private IcmpV4DestinationUnreachablePacket(
    IcmpV4DestinationUnreachableHeader header, byte[] rawData
  ) {
    super(rawData);
    this.header = header;
  }

  private IcmpV4DestinationUnreachablePacket(Builder builder) {
    super(builder);
    this.header = new IcmpV4DestinationUnreachableHeader(builder);
  }

  @Override
  public IcmpV4DestinationUnreachableHeader getHeader() {
    return header;
  }

  @Override
  public Builder getBuilder() {
    return new Builder(this);
  }

  /**
   * @author Kaito Yamada
   * @since pcap4j 0.9.11
   */
  public static
  final class Builder extends org.pcap4j.packet.IcmpV4InvokingPacketPacket.Builder {

    private int unused;

    /**
     *
     */
    public Builder() {}

    private Builder(IcmpV4DestinationUnreachablePacket packet) {
      super(packet);
      this.unused = packet.getHeader().unused;
    }

    /**
     *
     * @param unused
     * @return this Builder object for method chaining.
     */
    public Builder unused(int unused) {
      this.unused = unused;
      return this;
    }

    @Override
    public Builder payload(Packet payload) {
      super.payload(payload);
      return this;
    }

    @Override
    public IcmpV4DestinationUnreachablePacket build() {
      return new IcmpV4DestinationUnreachablePacket(this);
    }

  }

  /**
   * @author Kaito Yamada
   * @since pcap4j 0.9.11
   */
  public static final class IcmpV4DestinationUnreachableHeader extends AbstractHeader {

    /*
     *   0                            15                              31
     *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *  |                             unused                            |
     *  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     */

    /**
     *
     */
    private static final long serialVersionUID = 5245447443272345197L;

    private static final int UNUSED_OFFSET
      = 0;
    private static final int UNUSED_SIZE
      = INT_SIZE_IN_BYTES;
    private static final int ICMPV4_DESTINATION_UNREACHABLE_HEADER_SIZE
      = UNUSED_OFFSET + UNUSED_SIZE;

    private final int unused;

    private IcmpV4DestinationUnreachableHeader(byte[] rawData) {
      if (rawData.length < ICMPV4_DESTINATION_UNREACHABLE_HEADER_SIZE) {
        StringBuilder sb = new StringBuilder(80);
        sb.append("The data is too short to build an ICMPv4 Destination Unreachable Header(")
          .append(ICMPV4_DESTINATION_UNREACHABLE_HEADER_SIZE)
          .append(" bytes). data: ")
          .append(ByteArrays.toHexString(rawData, " "));
        throw new IllegalRawDataException(sb.toString());
      }

      this.unused
        = ByteArrays.getInt(rawData, UNUSED_OFFSET);
    }

    private IcmpV4DestinationUnreachableHeader(Builder builder) {
      this.unused = builder.unused;
    }

    /**
     *
     * @return unused
     */
    public int getUnused() { return unused; }

    @Override
    protected List<byte[]> getRawFields() {
      List<byte[]> rawFields = new ArrayList<byte[]>();
      rawFields.add(ByteArrays.toByteArray(unused));
      return rawFields;
    }

    @Override
    public int length() {
      return ICMPV4_DESTINATION_UNREACHABLE_HEADER_SIZE;
    }

    @Override
    protected String buildString() {
      StringBuilder sb = new StringBuilder();
      String ls = System.getProperty("line.separator");

      sb.append("[ICMPv4 Destination Unreachable Header (")
        .append(length())
        .append(" bytes)]")
        .append(ls);
      sb.append("  Unused: ")
        .append(unused)
        .append(ls);
      return sb.toString();
    }

  }

}
