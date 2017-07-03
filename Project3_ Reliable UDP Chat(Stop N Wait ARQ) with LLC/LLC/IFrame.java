package LLC;

public class IFrame extends Frame {

	public IFrame() {
	}

	public IFrame(LLCBitSet information){
		this.information = information;
	}

	//@formatter:off
	/**
	 * Control has format
	 * -------------------------------
	 * |  N(R)  |  P/F  |  N(S)  | 0 |
	 * | 7 bits | 1 bit | 7 bits | 0 |
	 *  ------------------------------
	 */
	//@formatter:on
	public void setControl(byte nr, byte pf, byte ns) {
		control[1] = (byte) (0xFE & (ns << 1));
		control[0] = (byte) ((0xFE & (nr << 1)) | (0x1 & pf));
	}

	public static boolean isFrameTypeValid(byte controlByte){
		return (controlByte & 0x01) == 0x0;
	}

	@Override
	public byte getCommandType() {
		return (byte) (control[1] & 0x0C);
	}

	public byte getSendSequence() {
		return (byte) ((control[1] & 0xFE) >> 1);
	}

	public byte getRecvSequence() {
		return (byte) ((control[0] & 0xFE) >> 1);
	}

	public LLCBitSet getInformation() {
		return this.information;
	}
}
