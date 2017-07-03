package LLC;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class LLCBitSet extends BitSet {

	private int numberOfBits = 0;

	public LLCBitSet() {
		this(0);
	}

	public LLCBitSet(int numberOfBits) {
		this.numberOfBits = numberOfBits;
	}

	public int getNumberOfBits() {
		return numberOfBits;
	}

	public void setNumberOfBits(int numberOfBits) {
		this.numberOfBits = numberOfBits;
	}

	public void append(LLCBitSet bitset) {
		for (int i = 0; i < bitset.numberOfBits; i++)
			set(numberOfBits++, bitset.get(i));
	}



	public void append(int intData) {
		append(ByteBuffer.allocate(4).putInt(intData).array());
	}

	public void append(int... intData) {
		for (int i = 0; i < intData.length; i++)
			append(intData[i]);
	}

	public void append(short shortData) {
		append(ByteBuffer.allocate(2).putShort(shortData).array());
	}


	public void append(short... shortData) {
		for (int i = 0; i < shortData.length; i++)
			append(shortData[i]);
	}

	public void append(byte byteData) {
		for (int i = 0; i < 8; i++)
			set(numberOfBits++, (byteData >> i & 0x01) > 0);
	}

	public void append(byte... byteData) {
		for (int i = 0; i < byteData.length; i++){
			append(byteData[i]);
		}
	}

	@Override
	public byte[] toByteArray() {
		byte[] byteArray = super.toByteArray();
		byte[] result = new byte[(numberOfBits + 7) / 8];
		for(int i = 0; i < result.length; i++)
			if(i < byteArray.length)
				result[i] = byteArray[i];
			else
				result[i] = 0;
		return result;
	}

	public short[] toShortArray() {
		byte[] bytes = toByteArray();
		short[] shorts = new short[(bytes.length + 1) / 2];
		ByteBuffer.wrap(bytes).asShortBuffer().get(shorts);
		return shorts;
	}

	public int[] toIntArray() {
		byte[] bytes = toByteArray();
		int[] ints = new int[(bytes.length + 3) / 4];
		ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
		return ints;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		byte[] byteArray = super.toByteArray();
		for (int i = 0; i < byteArray.length; i++)
			sb.append(String.format("%02X ", byteArray[i]));
		return sb.toString();
	}

	public static LLCBitSet fromBytes(byte... byteData) {
		LLCBitSet bitSet = new LLCBitSet();
		bitSet.append(byteData);
		return bitSet;
	}

	public static LLCBitSet fromBitSet(BitSet bitSet, int startIndex,
			int endIndex) {
		LLCBitSet result = new LLCBitSet();
		for (int i = startIndex; i < endIndex; i++)
			result.set(result.numberOfBits++, bitSet.get(i));
		return result;
	}

}
