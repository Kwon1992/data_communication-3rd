package LLC;

public class UFrame extends Frame {

	/**
	 * <b>Set mode; extended</b><br />
	 * Set asynchronous balanced extended mode SABME
	*/
	public static final short SABME= 0x6F;

	/**
	 * <b>Acknowledge acceptance of one of the set-mode commands.</b><br />
	 * Unnumbered Acknowledgment UA
	*/
	public static final short UA= 0x63; // 0x60?


	public UFrame(byte type){
		setControl((byte)0, (byte)0, type);
	}

	//@formatter:off
	/**
	 * Control has format
	 * -----------------------------------
	 * |  MMM   |  P/F  |  MM    | 1 | 1 |
	 * | 3 bits | 1 bit | 2 bits | 1 | 1 |
	 *  ----------------------------------
	 */
	//@formatter:on
	public void setControl(byte nr, byte pf, byte type) {
		control[1] = (byte) ((0xFC & type) | 0x03);
		control[0] = (byte) 0;
	}

	@Override
	public byte getCommandType() {
		return (byte) (control[1] & 0xFF);
	}

	public static boolean isFrameTypeValid(byte controlByte) {
		return (controlByte & 0x03) == 0x03;
	}

}
