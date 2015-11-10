package org.blockartistry.mod.Restructured.nbt;

import net.minecraftforge.common.util.Constants.NBT;

public final class NBTFactory {

	public static MyNBTBase getTag(final byte type) {

		switch (type) {
		case NBT.TAG_END:
			return new MyNBTTagEnd();
		case NBT.TAG_BYTE:
			return new MyNBTTagByte();
		case NBT.TAG_SHORT:
			return new MyNBTTagShort();
		case NBT.TAG_INT:
			return new MyNBTTagInt();
		case NBT.TAG_LONG:
			return new MyNBTTagLong();
		case NBT.TAG_FLOAT:
			return new MyNBTTagFloat();
		case NBT.TAG_DOUBLE:
			return new MyNBTTagDouble();
		case NBT.TAG_BYTE_ARRAY:
			return new MyNBTTagByteArray();
		case NBT.TAG_INT_ARRAY:
			return new MyNBTTagIntArray();
		case NBT.TAG_STRING:
			return new MyNBTTagString();
		case NBT.TAG_LIST:
			return new MyNBTTagList();
		case NBT.TAG_COMPOUND:
			return new MyNBTTagCompound();
		}

		return null;
	}

	//
	// Indicates whether the NBT object represents an immutable
	// type. This is to optimize copies and reduce on overhead.
	//
	public static boolean isImmutable(final byte type) {
		return !(type == NBT.TAG_BYTE_ARRAY || type == NBT.TAG_INT_ARRAY || type == NBT.TAG_COMPOUND
				|| type == NBT.TAG_LIST);
	}
}
