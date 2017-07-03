package LLC;

public class SFrame extends Frame {

	/**
	 * <b>Receive Ready</b><br />
	 * Indicate that the sender is ready to receive more data (cancels the
	 * effect of a previous RNR).
	 */
	public static final byte RR = 0x00;

	/**
	 * <b>Receive Not Ready</b><br />
	 * Acknowledge some packets and request no more be sent until further
	 * notice.
	 */
	public static final byte RNR = 0x08;

	/**
	 * <b>Reject</b><br />
	 * Requests immediate retransmission starting with N(R).
	 */
	public static final byte REJ = 0x04;

	/**
	 * <b>Selective Reject</b><br />
	 * Requests retransmission of only the frame N(R).
	 */
	public static final byte SREJ = 0x0C;

	public SFrame(byte type){
		setControl((byte)0, (byte)0, type);
	}

	//@formatter:off
	/**
	 * Control has format
	 * --------------------------------------------
	 * | N(R)   |  P/F  |   X    |  SS    | 0 | 1 |
	 * | 7 bits | 1 bit | 4 bits | 2 bits | 0 | 1 |
	 *  -------------------------------------------
	 */
	//@formatter:on
	public void setControl(byte nr, byte pf, byte type) {
		control[1] = (byte) ((0x0C & type) | 0x01);
		control[0] = (byte) ((0xFE & (nr << 1)) | (0x01 & pf));
	}

	public static boolean isFrameTypeValid(byte controlByte){
		return (controlByte & 0x03) == 0x01;
	}

	@Override
	public byte getCommandType() {
		return (byte) (control[1] & 0x0C);
	}

	public byte getRecvSequence() {
		return (byte) ((control[0] & 0xFE) >> 1);
	}
}
