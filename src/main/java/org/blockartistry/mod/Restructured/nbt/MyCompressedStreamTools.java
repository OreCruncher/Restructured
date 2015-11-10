package org.blockartistry.mod.Restructured.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants.NBT;

public class MyCompressedStreamTools {

	public static MyNBTTagCompound readCompressed(final InputStream stream) throws IOException {
		DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
		MyNBTTagCompound nbttagcompound;
		try {
			nbttagcompound = read(datainputstream, MyNBTSizeTracker.noopTracker);
		} finally {
			datainputstream.close();
		}

		return nbttagcompound;
	}

	public static void writeCompressed(final MyNBTTagCompound nbt, final OutputStream stream) throws IOException {
		DataOutputStream dataoutputstream = new DataOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(stream)));
		try {
			write(nbt, dataoutputstream);
		} finally {
			dataoutputstream.close();
		}
	}

	public static MyNBTTagCompound read(final byte[] buffer, final MyNBTSizeTracker tracker) throws IOException {
		DataInputStream datainputstream = new DataInputStream(
				new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(buffer))));
		MyNBTTagCompound nbttagcompound;
		try {
			nbttagcompound = read(datainputstream, tracker);
		} finally {
			datainputstream.close();
		}

		return nbttagcompound;
	}

	public static byte[] compress(final MyNBTTagCompound nbt) throws IOException {
		final ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		final DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));
		try {
			write(nbt, dataoutputstream);
		} finally {
			dataoutputstream.close();
		}

		return bytearrayoutputstream.toByteArray();
	}

	public static void safeWrite(final MyNBTTagCompound nbt, final File file) throws IOException {
		final File file2 = new File(file.getAbsolutePath() + "_tmp");

		if (file2.exists()) {
			file2.delete();
		}

		write(nbt, file2);

		if (file.exists()) {
			file.delete();
		}

		if (file.exists()) {
			throw new IOException("Failed to delete " + file);
		}

		file2.renameTo(file);
	}

	public static MyNBTTagCompound read(final DataInputStream stream) throws IOException {
		return read(stream, MyNBTSizeTracker.noopTracker);
	}

	public static MyNBTTagCompound read(final DataInput stream, final MyNBTSizeTracker tracker) throws IOException {
		final MyNBTBase nbtbase = read0(stream, 0, tracker);

		if (nbtbase instanceof MyNBTTagCompound) {
			return ((MyNBTTagCompound) nbtbase);
		}

		throw new IOException("Root tag must be a named compound tag");
	}

	public static void write(final MyNBTTagCompound nbt, final DataOutput stream) throws IOException {
		write0(nbt, stream);
	}

	private static void write0(final MyNBTBase tag, final DataOutput stream) throws IOException {
		stream.writeByte(tag.getId());

		if (tag.getId() == NBT.TAG_END)
			return;
		stream.writeUTF("");
		tag.writeStream(stream);
	}

	private static MyNBTBase read0(final DataInput stream, final int depth, final MyNBTSizeTracker tracker)
			throws IOException {
		final byte b0 = stream.readByte();
		tracker.func_152450_a(8L);

		if (b0 == NBT.TAG_END) {
			return MyNBTTagEnd.END;
		}

		MyNBTSizeTracker.readUTF(tracker, stream.readUTF());
		tracker.func_152450_a(32L);
		final MyNBTBase nbtbase = NBTFactory.getTag(b0);
		try {
			nbtbase.readStream(stream, depth, tracker);
			return nbtbase;
		} catch (final IOException ioexception) {
			CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
			crashreportcategory.addCrashSection("Tag name", "[UNNAMED TAG]");
			crashreportcategory.addCrashSection("Tag type", Byte.valueOf(b0));
			throw new ReportedException(crashreport);
		}
	}

	public static void write(final MyNBTTagCompound nbt, final File file) throws IOException {
		DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));
		try {
			write(nbt, dataoutputstream);
		} finally {
			dataoutputstream.close();
		}
	}

	public static MyNBTTagCompound read(final File file) throws IOException {
		return read(file, MyNBTSizeTracker.noopTracker);
	}

	public static MyNBTTagCompound read(final File file, final MyNBTSizeTracker tracker) throws IOException {
		if (!(file.exists())) {
			return null;
		}

		DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
		MyNBTTagCompound nbttagcompound;
		try {
			nbttagcompound = read(datainputstream, tracker);
		} finally {
			datainputstream.close();
		}

		return nbttagcompound;
	}
}
