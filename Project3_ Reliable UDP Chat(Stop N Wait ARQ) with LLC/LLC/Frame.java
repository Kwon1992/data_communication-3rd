package LLC;

import java.util.BitSet;
import java.util.zip.CRC32;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import sun.misc.CRC16;

//@formatter:off
/**
 * The structure of a frame is
 *  -------------------------------------------------------------------------------------------
 * |  DestAddr  | SourceAddr | LengthPDU |  DSAP  |  SSAP  |  Control  | Information |   CRC   |
 * |   48 bits  |  48 bits   |  16 bits  | 8 bits | 8 bits |  16 bits  | Variable    | 32 bits |
 *  -------------------------------------------------------------------------------------------
 */
//@formatter:on
public abstract class Frame {

	//public static final LLCBitSet FLAG = LLCBitSet.fromBytes((byte) 0x7E); // 01111110 //

	//private static final CRC16 CRC_VALIDATOR = new CRC16(); //
	//private static final CRC32 CRC32_VALIDATOR = new CRC32();

	protected byte[] DestAddr = new byte[6]; //6
	protected byte[] SourceAddr = new byte[6]; //6
	protected byte[] LengthPDU = new byte[2]; //2
	protected byte DSAP = (byte)0x00; //1
	protected byte SSAP = (byte)0x00; //1
	protected byte[] control = new byte[2]; //2
	protected LLCBitSet information = new LLCBitSet(); //dynamic
	protected byte[] crc32; //4

	public byte[] getControl() {
		return control;
	}

	public byte[] getDestAddress() {
		return DestAddr;
	}
	public byte[] getSourceAddress() {
		return SourceAddr;
	}

	public byte[] getCRC() {
		return crc32;
	}

	public void setDestAddress(byte[] dest) {
		DestAddr = dest;
	}
	public void setSourceAddress(byte[] src) {
		SourceAddr = src;
	}
	public void setLengthPDU(byte[] length) {
		LengthPDU = length;
	}
	public void setInfo(LLCBitSet info) {
		information = info;
	}


	public abstract void setControl(byte nr, byte pf, byte type);

	public abstract byte getCommandType();

	public byte[] toByteArray() {
		LLCBitSet crcComputable = new LLCBitSet(); // crcComputable.numberOfBits == 0.
//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(DestAddr); // byte... byteData call
//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(SourceAddr);
//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(LengthPDU);
//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(DSAP);
//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(SSAP);
//  	System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(control);
//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());
		crcComputable.append(information);

//		System.out.println("toByteArray_NumofBits : "+crcComputable.getNumberOfBits());

		LLCBitSet result = new LLCBitSet();
		result.append(crcComputable);
		result.append(computeCRC(crcComputable));

		return result.toByteArray();
	}

	public String toString() {
		return LLCBitSet.fromBytes(toByteArray()).toString();
	}


	public static Frame fromByteArray(BitSet data) {
		Frame frame = null;
		byte[] header = LLCBitSet.fromBitSet(data, 0, 144).toByteArray();
		byte controlByte = LLCBitSet.fromBitSet(data, 128, 144).toByteArray()[1];


		if (SFrame.isFrameTypeValid(controlByte))
			frame = new SFrame((byte) 0);
		else if(UFrame.isFrameTypeValid(controlByte))
			frame = new UFrame((byte) 0);
		else if (IFrame.isFrameTypeValid(controlByte))
			frame = new IFrame(new LLCBitSet());
		else
			throw new InvalidFrameException("Frame type not recognize from the control byte: " + controlByte);


		frame.DestAddr = LLCBitSet.fromBitSet(data, 0, 48).toByteArray();
		frame.SourceAddr = LLCBitSet.fromBitSet(data, 48, 96).toByteArray();
		frame.LengthPDU = LLCBitSet.fromBitSet(data, 96, 112).toByteArray();
		frame.DSAP = header[14];
		frame.SSAP = header[15];
		frame.control = LLCBitSet.fromBitSet(data, 128, 144).toByteArray();
		frame.information = LLCBitSet.fromBitSet(data, 144, ((LLCBitSet)data).getNumberOfBits()-32);

		LLCBitSet crcComputable = new LLCBitSet();
		crcComputable.append(frame.DestAddr);
		crcComputable.append(frame.SourceAddr);
		crcComputable.append(frame.LengthPDU);
		crcComputable.append(frame.DSAP);
		crcComputable.append(frame.SSAP);
		crcComputable.append(frame.control);
		crcComputable.append(frame.information);

		byte[] computedCRC = computeCRC(crcComputable);
		byte[] dataCRC = LLCBitSet.fromBitSet(data, ((LLCBitSet)data).getNumberOfBits()-32,
																								((LLCBitSet)data).getNumberOfBits()).toByteArray(); // this is problem??

		//check CRC32
		if(ByteBuffer.wrap(computedCRC).order(ByteOrder.LITTLE_ENDIAN).getInt()
			 == ByteBuffer.wrap(dataCRC).order(ByteOrder.LITTLE_ENDIAN).getInt()) {
				 System.out.println("CRC32 CHECK :: VALID!");
		} else {
			System.out.println("CRC32 CHECK :: INVALID!");
			return null;
		}
		return frame;
	}


	private static byte[] computeCRC(LLCBitSet data) {
		CRC32 crc32 = new CRC32();
		crc32.reset();
		byte[] byteArray = data.toByteArray();
		crc32.update(byteArray);
		int tempVal =  (int) crc32.getValue();
		byte[] res = Frame.intToByteArray(tempVal);
		return res;
	}

	public static final byte[] intToByteArray(int value) { // only need 2 byte.
			return new byte[] {
							(byte)(value >>> 24),	(byte)(value >>> 16),
							(byte)(value >>> 8),	(byte)(value)	};
	}
}
