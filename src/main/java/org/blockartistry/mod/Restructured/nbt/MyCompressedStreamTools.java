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

public class MyCompressedStreamTools {

	public static MyNBTTagCompound readCompressed(InputStream stream) throws IOException {
		DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
		MyNBTTagCompound nbttagcompound;
		try {
			nbttagcompound = read(datainputstream, MyNBTSizeTracker.noopTracker);
		} finally {
			datainputstream.close();
		}

		return nbttagcompound;
	}

	public static void writeCompressed(MyNBTTagCompound nbt, OutputStream stream) throws IOException {
		DataOutputStream dataoutputstream = new DataOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(stream)));
		try {
			write(nbt, dataoutputstream);
		} finally {
			dataoutputstream.close();
		}
	}

	public static MyNBTTagCompound read(byte[] buffer, MyNBTSizeTracker tracker) throws IOException {
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

	public static byte[] compress(MyNBTTagCompound nbt) throws IOException {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));
		try {
			write(nbt, dataoutputstream);
		} finally {
			dataoutputstream.close();
		}

		return bytearrayoutputstream.toByteArray();
	}

	public static void safeWrite(MyNBTTagCompound nbt, File file) throws IOException {
		File file2 = new File(file.getAbsolutePath() + "_tmp");

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

	public static MyNBTTagCompound read(DataInputStream stream) throws IOException {
		return read(stream, MyNBTSizeTracker.noopTracker);
	}

	public static MyNBTTagCompound read(DataInput stream, MyNBTSizeTracker tracker) throws IOException {
		MyNBTBase nbtbase = read0(stream, 0, tracker);

		if (nbtbase instanceof MyNBTTagCompound) {
			return ((MyNBTTagCompound) nbtbase);
		}

		throw new IOException("Root tag must be a named compound tag");
	}

	public static void write(MyNBTTagCompound nbt, DataOutput stream) throws IOException {
		write0(nbt, stream);
	}

	private static void write0(MyNBTBase tag, DataOutput stream) throws IOException {
		stream.writeByte(tag.getId());

		if (tag.getId() == 0)
			return;
		stream.writeUTF("");
		tag.writeStream(stream);
	}

	private static MyNBTBase read0(DataInput stream, int depth, MyNBTSizeTracker tracker)
			throws IOException {
		byte b0 = stream.readByte();
		tracker.func_152450_a(8L);

		if (b0 == 0) {
			return new MyNBTTagEnd();
		}

		MyNBTSizeTracker.readUTF(tracker, stream.readUTF());
		tracker.func_152450_a(32L);
		MyNBTBase nbtbase = NBTFactory.getTag(b0);
		try {
			nbtbase.readStream(stream, depth, tracker);
			return nbtbase;
		} catch (IOException ioexception) {
			CrashReport crashreport = CrashReport.makeCrashReport(ioexception, "Loading NBT data");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("NBT Tag");
			crashreportcategory.addCrashSection("Tag name", "[UNNAMED TAG]");
			crashreportcategory.addCrashSection("Tag type", Byte.valueOf(b0));
			throw new ReportedException(crashreport);
		}
	}

	public static void write(MyNBTTagCompound nbt, File file) throws IOException {
		DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));
		try {
			write(nbt, dataoutputstream);
		} finally {
			dataoutputstream.close();
		}
	}

	public static MyNBTTagCompound read(File file) throws IOException {
		return read(file, MyNBTSizeTracker.noopTracker);
	}

	public static MyNBTTagCompound read(File file, MyNBTSizeTracker tracker) throws IOException {
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