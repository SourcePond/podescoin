package ch.sourcepond.utils.podescoin.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public final class StreamUtil {
	public static final Character DEFAULT_NULL_CHARACTER = Character.valueOf(Character.MIN_VALUE);
	public static final Byte DEFAULT_NULL_BYTE = (byte) -1;
	public static final Short DEFAULT_NULL_SHORT = (short) -1;
	public static final Integer DEFAULT_NULL_INT = -1;
	public static final Long DEFAULT_NULL_LONG = -1l;
	public static final Float DEFAULT_NULL_FLOAT = -1f;
	public static final Double DEFAULT_NULL_DOUBLE = -1d;
	public static final String DEFAULT_NULL_STRING = "";

	private StreamUtil() {
		// Only static methods, so make constructor private
	}

	@FunctionalInterface
	private static interface Reader<T> {

		T read(ObjectInputStream in) throws IOException;
	}

	@FunctionalInterface
	private static interface Writer<T> {

		void write(ObjectOutputStream out, T key) throws IOException;
	}

	private static <R, T> Optional<R> read(final ObjectInputStream in, final Reader<T> pReader, final T pNullToken,
			final Function<T, R> pKeyFunction) throws IOException {
		final T key = pReader.read(in);
		if (!pNullToken.equals(key)) {
			try {
				return Optional.ofNullable(pKeyFunction.apply(key));
			} catch (final Exception e) {
				throw new IOException("Reader key-function could not be applied!", e);
			}
		}
		return Optional.empty();
	}

	private static <T, R> void write(final ObjectOutputStream pOut, final T pObj, final Function<T, R> pKeyFunction,
			final Writer<R> pWriter, final R pNullToken) throws IOException {
		R key = null;
		if (pObj != null) {
			try {
				key = pKeyFunction.apply(pObj);
			} catch (final Exception e) {
				throw new IOException("Writer key-function could not be applied!", e);
			}
		} else {
			key = pNullToken;
		}
		if (key == null) {
			key = pNullToken;
		}
		pWriter.write(pOut, key);
	}

	public static <T> Optional<T> readByte(final ObjectInputStream pIn, final Function<Byte, T> pLoader)
			throws IOException {
		return readByte(pIn, DEFAULT_NULL_BYTE, pLoader);
	}

	public static <T> Optional<T> readByte(final ObjectInputStream pIn, final Byte pNullToken,
			final Function<Byte, T> pLoader) throws IOException {
		return read(pIn, in -> in.readByte(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readShort(final ObjectInputStream pIn, final Function<Short, T> pLoader)
			throws IOException {
		return readShort(pIn, DEFAULT_NULL_SHORT, pLoader);
	}

	public static <T> Optional<T> readShort(final ObjectInputStream pIn, final Short pNullToken,
			final Function<Short, T> pLoader) throws IOException {
		return read(pIn, in -> in.readShort(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readChar(final ObjectInputStream pIn, final Function<Character, T> pLoader)
			throws IOException {
		return readChar(pIn, DEFAULT_NULL_CHARACTER, pLoader);
	}

	public static <T> Optional<T> readChar(final ObjectInputStream pIn, final Character pNullToken,
			final Function<Character, T> pLoader) throws IOException {
		return read(pIn, in -> in.readChar(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readInt(final ObjectInputStream pIn, final Function<Integer, T> pLoader)
			throws IOException {
		return readInt(pIn, DEFAULT_NULL_INT, pLoader);
	}

	public static <T> Optional<T> readInt(final ObjectInputStream pIn, final Integer pNullToken,
			final Function<Integer, T> pLoader) throws IOException {
		return read(pIn, in -> in.readInt(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readLong(final ObjectInputStream pIn, final Function<Long, T> pLoader)
			throws IOException {
		return readLong(pIn, DEFAULT_NULL_LONG, pLoader);
	}

	public static <T> Optional<T> readLong(final ObjectInputStream pIn, final Long pNullToken,
			final Function<Long, T> pLoader) throws IOException {
		return read(pIn, in -> in.readLong(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readFloat(final ObjectInputStream pIn, final Function<Float, T> pLoader)
			throws IOException {
		return readFloat(pIn, DEFAULT_NULL_FLOAT, pLoader);
	}

	public static <T> Optional<T> readFloat(final ObjectInputStream pIn, final Float pNullToken,
			final Function<Float, T> pLoader) throws IOException {
		return read(pIn, in -> in.readFloat(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readDouble(final ObjectInputStream pIn, final Function<Double, T> pLoader)
			throws IOException {
		return readDouble(pIn, DEFAULT_NULL_DOUBLE, pLoader);
	}

	public static <T> Optional<T> readDouble(final ObjectInputStream pIn, final Double pNullToken,
			final Function<Double, T> pLoader) throws IOException {
		return read(pIn, in -> in.readDouble(), pNullToken, pLoader);
	}

	public static <T> Optional<T> readUTF(final ObjectInputStream pIn, final Function<String, T> pLoader)
			throws IOException {
		return readUTF(pIn, DEFAULT_NULL_STRING, pLoader);
	}

	public static <T> Optional<T> readUTF(final ObjectInputStream pIn, final String pNullToken,
			final Function<String, T> pLoader) throws IOException {
		return read(pIn, in -> in.readUTF(), pNullToken, pLoader);
	}

	public static <T> void writeByte(final ObjectOutputStream pOut, final T pObj, final Function<T, Byte> pResolver)
			throws IOException {
		writeByte(pOut, pObj, DEFAULT_NULL_BYTE, pResolver);
	}

	public static <T> void writeByte(final ObjectOutputStream pOut, final T pObj, final Byte pNullToken,
			final Function<T, Byte> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeByte(key.intValue()), pNullToken);
	}

	public static <T> void writeShort(final ObjectOutputStream pOut, final T pObj, final Function<T, Short> pResolver)
			throws IOException {
		writeShort(pOut, pObj, DEFAULT_NULL_SHORT, pResolver);
	}

	public static <T> void writeShort(final ObjectOutputStream pOut, final T pObj, final Short pNullToken,
			final Function<T, Short> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeShort(key.shortValue()), pNullToken);
	}

	public static <T> void writeChar(final ObjectOutputStream pOut, final T pObj,
			final Function<T, Character> pResolver) throws IOException {
		writeChar(pOut, pObj, DEFAULT_NULL_CHARACTER, pResolver);
	}

	public static <T> void writeChar(final ObjectOutputStream pOut, final T pObj, final Character pNullToken,
			final Function<T, Character> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeChar(key.charValue()), pNullToken);
	}

	public static <T> void writeInt(final ObjectOutputStream pOut, final T pObj, final Function<T, Integer> pResolver)
			throws IOException {
		writeInt(pOut, pObj, DEFAULT_NULL_INT, pResolver);
	}

	public static <T> void writeInt(final ObjectOutputStream pOut, final T pObj, final Integer pNullToken,
			final Function<T, Integer> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeInt(key.intValue()), pNullToken);
	}

	public static <T> void writeLong(final ObjectOutputStream pOut, final T pObj, final Function<T, Long> pResolver)
			throws IOException {
		writeLong(pOut, pObj, DEFAULT_NULL_LONG, pResolver);
	}

	public static <T> void writeLong(final ObjectOutputStream pOut, final T pObj, final Long pNullToken,
			final Function<T, Long> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeLong(key.longValue()), pNullToken);
	}

	public static <T> void writeFloat(final ObjectOutputStream pOut, final T pObj, final Function<T, Float> pResolver)
			throws IOException {
		writeFloat(pOut, pObj, DEFAULT_NULL_FLOAT, pResolver);
	}

	public static <T> void writeFloat(final ObjectOutputStream pOut, final T pObj, final Float pNullToken,
			final Function<T, Float> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeFloat(key.floatValue()), pNullToken);
	}

	public static <T> void writeDouble(final ObjectOutputStream pOut, final T pObj, final Function<T, Double> pResolver)
			throws IOException {
		writeDouble(pOut, pObj, DEFAULT_NULL_DOUBLE, pResolver);
	}

	public static <T> void writeDouble(final ObjectOutputStream pOut, final T pObj, final Double pNullToken,
			final Function<T, Double> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeDouble(key.doubleValue()), pNullToken);
	}

	public static <T> void writeBytes(final ObjectOutputStream pOut, final T pObj, final Function<T, String> pResolver)
			throws IOException {
		writeBytes(pOut, pObj, DEFAULT_NULL_STRING, pResolver);
	}

	public static <T> void writeBytes(final ObjectOutputStream pOut, final T pObj, final String pNullToken,
			final Function<T, String> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeBytes(key), pNullToken);
	}

	public static <T> void writeChars(final ObjectOutputStream pOut, final T pObj, final Function<T, String> pResolver)
			throws IOException {
		writeChars(pOut, pObj, DEFAULT_NULL_STRING, pResolver);
	}

	public static <T> void writeChars(final ObjectOutputStream pOut, final T pObj, final String pNullToken,
			final Function<T, String> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeChars(key), pNullToken);
	}

	public static <T> void writeUTF(final ObjectOutputStream pOut, final T pObj, final Function<T, String> pResolver)
			throws IOException {
		writeUTF(pOut, pObj, DEFAULT_NULL_STRING, pResolver);
	}

	public static <T> void writeUTF(final ObjectOutputStream pOut, final T pObj, final String pNullToken,
			final Function<T, String> pResolver) throws IOException {
		write(pOut, pObj, pResolver, (out, key) -> out.writeUTF(key), pNullToken);
	}
}
