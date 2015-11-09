package org.blockartistry.mod.Restructured.nbt;

public final class NBTFactory {

	public static MyNBTBase getTag(final byte type) {

		MyNBTBase result = null;
		
		switch (type) {
		case 0:
			result = new MyNBTTagEnd();
			break;
		case 1:
			result = new MyNBTTagByte();
			break;
		case 2:
			result = new MyNBTTagShort();
			break;
		case 3:
			result = new MyNBTTagInt();
			break;
		case 4:
			result = new MyNBTTagLong();
			break;
		case 5:
			result = new MyNBTTagFloat();
			break;
		case 6:
			result = new MyNBTTagDouble();
			break;
		case 7:
			result = new MyNBTTagByteArray();
			break;
		case 11:
			result = new MyNBTTagIntArray();
			break;
		case 8:
			result = new MyNBTTagString();
			break;
		case 9:
			result = new MyNBTTagList();
			break;
		case 10:
			result = new MyNBTTagCompound();
			break;
		}
		
		return result; //MyNBTBase.class.cast(result);
	}
}
