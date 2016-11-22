package ch.sourcepond.utils.podescoin.api;

import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_BYTE;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_CHARACTER;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_DOUBLE;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_FLOAT;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_INT;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_LONG;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_SHORT;
import static ch.sourcepond.utils.podescoin.api.StreamUtil.DEFAULT_NULL_STRING;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class StreamUtilTest {
	private static final Byte BYTE_KEY = Byte.MAX_VALUE;
	private static final Short SHORT_KEY = Short.MAX_VALUE;
	private static final Integer INT_KEY = Integer.MAX_VALUE;
	private static final Character CHAR_KEY = Character.MAX_VALUE;
	private static final Long LONG_KEY = Long.MAX_VALUE;
	private static final Float FLOAT_KEY = Float.MAX_VALUE;
	private static final Double DOUBLE_KEY = Double.MAX_VALUE;
	private static final String STRING_KEY = "anyStringKey";
	private static final Byte CUSTOM_BYTE_NULL_TOKEN = Byte.MIN_VALUE;
	private static final Short CUSTOM_SHORT_NULL_TOKEN = Short.MIN_VALUE;
	private static final Integer CUSTOM_INT_NULL_TOKEN = Integer.MIN_VALUE;
	private static final Character CUSTOM_CHAR_NULL_TOKEN = Character.MIN_VALUE;
	private static final Long CUSTOM_LONG_NULL_TOKEN = Long.MIN_VALUE;
	private static final Float CUSTOM_FLOAT_NULL_TOKEN = Float.MIN_VALUE;
	private static final Double CUSTOM_DOUBLE_NULL_TOKEN = Double.MIN_VALUE;
	private static final String CUSTOM_STRING_NULL_TOKEN = "1";
	private final ObjectInputStream in = mock(ObjectInputStream.class);
	private final ObjectOutputStream out = mock(ObjectOutputStream.class);
	private final TestStore store = mock(TestStore.class);

	@Test
	public void verifyPrivateConstructor() throws Exception {
		final Constructor<StreamUtil> c = StreamUtil.class.getDeclaredConstructor();
		c.setAccessible(true);
		c.newInstance();
	}

	@Test
	public void verifyReadWrapExceptionIntoIOException() throws Exception {
		final Exception expected = new Exception();
		doThrow(expected).when(store).load(BYTE_KEY);
		try {
			StreamUtil.readByte(in, key -> store.load(BYTE_KEY));
			fail("Exception expected");
		} catch (final IOException e) {
			assertSame(expected, e.getCause());
		}
	}

	@Test
	public void verifyWriteWrapExceptionIntoIOException() throws Exception {
		final Exception expected = new Exception();
		final TestObject<Byte> obj = mock(TestObject.class);
		doThrow(expected).when(obj).getKey();
		try {
			StreamUtil.writeByte(out, obj, o -> o.getKey());
			fail("Exception expected");
		} catch (final IOException e) {
			assertSame(expected, e.getCause());
		}
	}

	@Test
	public void readByte() throws Exception {
		when(in.readByte()).thenReturn(BYTE_KEY);
		final TestObject<Byte> obj = mock(TestObject.class);
		when(store.load(BYTE_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readByte(in, key -> store.load(BYTE_KEY)).get());
	}

	@Test
	public void readByteEqualsCustomNullToken() throws Exception {
		when(in.readByte()).thenReturn(CUSTOM_BYTE_NULL_TOKEN);
		final TestObject<Byte> obj = mock(TestObject.class);
		when(store.load(CUSTOM_BYTE_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readByte(in, CUSTOM_BYTE_NULL_TOKEN, key -> store.load(BYTE_KEY)).isPresent());
	}

	@Test
	public void readByteEqualsDefaultNullToken() throws Exception {
		when(in.readByte()).thenReturn(DEFAULT_NULL_BYTE);
		final TestObject<Byte> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_BYTE)).thenReturn(obj);
		assertFalse(StreamUtil.readByte(in, key -> store.load(DEFAULT_NULL_BYTE)).isPresent());
	}

	@Test
	public void readShort() throws Exception {
		when(in.readShort()).thenReturn(SHORT_KEY);
		final TestObject<Short> obj = mock(TestObject.class);
		when(store.load(SHORT_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readShort(in, key -> store.load(SHORT_KEY)).get());
	}

	@Test
	public void readShortEqualsCustomNullToken() throws Exception {
		when(in.readShort()).thenReturn(CUSTOM_SHORT_NULL_TOKEN);
		final TestObject<Short> obj = mock(TestObject.class);
		when(store.load(CUSTOM_SHORT_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readShort(in, CUSTOM_SHORT_NULL_TOKEN, key -> store.load(SHORT_KEY)).isPresent());
	}

	@Test
	public void readShortEqualsDefaultNullToken() throws Exception {
		when(in.readShort()).thenReturn(DEFAULT_NULL_SHORT);
		final TestObject<Short> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_SHORT)).thenReturn(obj);
		assertFalse(StreamUtil.readShort(in, key -> store.load(DEFAULT_NULL_SHORT)).isPresent());
	}

	@Test
	public void readInt() throws Exception {
		when(in.readInt()).thenReturn(INT_KEY);
		final TestObject<Integer> obj = mock(TestObject.class);
		when(store.load(INT_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readShort(in, key -> store.load(INT_KEY)).get());
	}

	@Test
	public void readIntEqualsCustomNullToken() throws Exception {
		when(in.readInt()).thenReturn(CUSTOM_INT_NULL_TOKEN);
		final TestObject<Integer> obj = mock(TestObject.class);
		when(store.load(CUSTOM_INT_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readInt(in, CUSTOM_INT_NULL_TOKEN, key -> store.load(INT_KEY)).isPresent());
	}

	@Test
	public void readIntEqualsDefaultNullToken() throws Exception {
		when(in.readInt()).thenReturn(DEFAULT_NULL_INT);
		final TestObject<Integer> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_INT)).thenReturn(obj);
		assertFalse(StreamUtil.readInt(in, key -> store.load(DEFAULT_NULL_INT)).isPresent());
	}

	@Test
	public void readChar() throws Exception {
		when(in.readChar()).thenReturn(CHAR_KEY);
		final TestObject<Character> obj = mock(TestObject.class);
		when(store.load(CHAR_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readShort(in, key -> store.load(CHAR_KEY)).get());
	}

	@Test
	public void readCharEqualsCustomNullToken() throws Exception {
		when(in.readChar()).thenReturn(CUSTOM_CHAR_NULL_TOKEN);
		final TestObject<Character> obj = mock(TestObject.class);
		when(store.load(CUSTOM_CHAR_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readChar(in, CUSTOM_CHAR_NULL_TOKEN, key -> store.load(CHAR_KEY)).isPresent());
	}

	@Test
	public void readCharEqualsDefaultNullToken() throws Exception {
		when(in.readChar()).thenReturn(DEFAULT_NULL_CHARACTER);
		final TestObject<Character> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_CHARACTER)).thenReturn(obj);
		assertFalse(StreamUtil.readChar(in, key -> store.load(DEFAULT_NULL_CHARACTER)).isPresent());
	}

	@Test
	public void readLong() throws Exception {
		when(in.readLong()).thenReturn(LONG_KEY);
		final TestObject<Long> obj = mock(TestObject.class);
		when(store.load(LONG_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readLong(in, key -> store.load(LONG_KEY)).get());
	}

	@Test
	public void readLongEqualsCustomNullToken() throws Exception {
		when(in.readLong()).thenReturn(CUSTOM_LONG_NULL_TOKEN);
		final TestObject<Long> obj = mock(TestObject.class);
		when(store.load(CUSTOM_LONG_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readLong(in, CUSTOM_LONG_NULL_TOKEN, key -> store.load(LONG_KEY)).isPresent());
	}

	@Test
	public void readLongEqualsDefaultNullToken() throws Exception {
		when(in.readLong()).thenReturn(DEFAULT_NULL_LONG);
		final TestObject<Long> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_LONG)).thenReturn(obj);
		assertFalse(StreamUtil.readLong(in, key -> store.load(DEFAULT_NULL_LONG)).isPresent());
	}

	@Test
	public void readFloat() throws Exception {
		when(in.readFloat()).thenReturn(FLOAT_KEY);
		final TestObject<Float> obj = mock(TestObject.class);
		when(store.load(FLOAT_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readFloat(in, key -> store.load(FLOAT_KEY)).get());
	}

	@Test
	public void readFloatEqualsCustomNullToken() throws Exception {
		when(in.readFloat()).thenReturn(CUSTOM_FLOAT_NULL_TOKEN);
		final TestObject<Float> obj = mock(TestObject.class);
		when(store.load(CUSTOM_FLOAT_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readFloat(in, CUSTOM_FLOAT_NULL_TOKEN, key -> store.load(FLOAT_KEY)).isPresent());
	}

	@Test
	public void readFloatEqualsDefaultNullToken() throws Exception {
		when(in.readFloat()).thenReturn(DEFAULT_NULL_FLOAT);
		final TestObject<Float> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_FLOAT)).thenReturn(obj);
		assertFalse(StreamUtil.readFloat(in, key -> store.load(DEFAULT_NULL_FLOAT)).isPresent());
	}

	@Test
	public void readDouble() throws Exception {
		when(in.readDouble()).thenReturn(DOUBLE_KEY);
		final TestObject<Double> obj = mock(TestObject.class);
		when(store.load(DOUBLE_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readDouble(in, key -> store.load(DOUBLE_KEY)).get());
	}

	@Test
	public void readDoubleEqualsCustomNullToken() throws Exception {
		when(in.readDouble()).thenReturn(CUSTOM_DOUBLE_NULL_TOKEN);
		final TestObject<Double> obj = mock(TestObject.class);
		when(store.load(CUSTOM_DOUBLE_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readDouble(in, CUSTOM_DOUBLE_NULL_TOKEN, key -> store.load(DOUBLE_KEY)).isPresent());
	}

	@Test
	public void readDoubleEqualsDefaultNullToken() throws Exception {
		when(in.readDouble()).thenReturn(DEFAULT_NULL_DOUBLE);
		final TestObject<Double> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_DOUBLE)).thenReturn(obj);
		assertFalse(StreamUtil.readDouble(in, key -> store.load(DEFAULT_NULL_DOUBLE)).isPresent());
	}

	@Test
	public void readUTF() throws Exception {
		when(in.readUTF()).thenReturn(STRING_KEY);
		final TestObject<String> obj = mock(TestObject.class);
		when(store.load(STRING_KEY)).thenReturn(obj);
		assertSame(obj, StreamUtil.readUTF(in, key -> store.load(STRING_KEY)).get());
	}

	@Test
	public void readUTFEqualsCustomNullToken() throws Exception {
		when(in.readUTF()).thenReturn(CUSTOM_STRING_NULL_TOKEN);
		final TestObject<String> obj = mock(TestObject.class);
		when(store.load(CUSTOM_STRING_NULL_TOKEN)).thenReturn(obj);
		assertFalse(StreamUtil.readUTF(in, CUSTOM_STRING_NULL_TOKEN, key -> store.load(STRING_KEY)).isPresent());
	}

	@Test
	public void readUTFEqualsDefaultNullToken() throws Exception {
		when(in.readUTF()).thenReturn(DEFAULT_NULL_STRING);
		final TestObject<String> obj = mock(TestObject.class);
		when(store.load(DEFAULT_NULL_STRING)).thenReturn(obj);
		assertFalse(StreamUtil.readUTF(in, key -> store.load(DEFAULT_NULL_STRING)).isPresent());
	}

	@Test
	public void writeByte() throws Exception {
		final TestObject<Byte> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(BYTE_KEY);
		StreamUtil.writeByte(out, obj, o -> o.getKey());
		verify(out).writeByte(BYTE_KEY);
	}

	@Test
	public void writeByteObjectIsNull() throws Exception {
		StreamUtil.writeByte(out, (TestObject<Byte>) null, o -> o.getKey());
		verify(out).writeByte(DEFAULT_NULL_BYTE);
	}

	@Test
	public void writeByteKeyIsNull() throws Exception {
		final TestObject<Byte> obj = mock(TestObject.class);
		StreamUtil.writeByte(out, obj, o -> o.getKey());
		verify(out).writeByte(DEFAULT_NULL_BYTE);
	}

	@Test
	public void writeByteKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Byte> obj = mock(TestObject.class);
		StreamUtil.writeByte(out, obj, CUSTOM_BYTE_NULL_TOKEN, o -> o.getKey());
		verify(out).writeByte(CUSTOM_BYTE_NULL_TOKEN);
	}

	@Test
	public void writeByteObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeByte(out, (TestObject<Byte>) null, CUSTOM_BYTE_NULL_TOKEN, o -> o.getKey());
		verify(out).writeByte(CUSTOM_BYTE_NULL_TOKEN);
	}

	@Test
	public void writeShort() throws Exception {
		final TestObject<Short> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(SHORT_KEY);
		StreamUtil.writeShort(out, obj, o -> o.getKey());
		verify(out).writeShort(SHORT_KEY);
	}

	@Test
	public void writeShortObjectIsNull() throws Exception {
		StreamUtil.writeShort(out, (TestObject<Short>) null, o -> o.getKey());
		verify(out).writeShort(DEFAULT_NULL_SHORT);
	}

	@Test
	public void writeShortKeyIsNull() throws Exception {
		final TestObject<Short> obj = mock(TestObject.class);
		StreamUtil.writeShort(out, obj, o -> o.getKey());
		verify(out).writeShort(DEFAULT_NULL_SHORT);
	}

	@Test
	public void writeShortKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Short> obj = mock(TestObject.class);
		StreamUtil.writeShort(out, obj, CUSTOM_SHORT_NULL_TOKEN, o -> o.getKey());
		verify(out).writeShort(CUSTOM_SHORT_NULL_TOKEN);
	}

	@Test
	public void writeShortObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeShort(out, (TestObject<Short>) null, CUSTOM_SHORT_NULL_TOKEN, o -> o.getKey());
		verify(out).writeShort(CUSTOM_SHORT_NULL_TOKEN);
	}

	@Test
	public void writeChar() throws Exception {
		final TestObject<Character> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(CHAR_KEY);
		StreamUtil.writeChar(out, obj, o -> o.getKey());
		verify(out).writeChar(CHAR_KEY);
	}

	@Test
	public void writeCharObjectIsNull() throws Exception {
		StreamUtil.writeChar(out, (TestObject<Character>) null, o -> o.getKey());
		verify(out).writeChar(DEFAULT_NULL_CHARACTER);
	}

	@Test
	public void writeCharKeyIsNull() throws Exception {
		final TestObject<Character> obj = mock(TestObject.class);
		StreamUtil.writeChar(out, obj, o -> o.getKey());
		verify(out).writeChar(DEFAULT_NULL_CHARACTER);
	}

	@Test
	public void writeCharKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Character> obj = mock(TestObject.class);
		StreamUtil.writeChar(out, obj, CUSTOM_CHAR_NULL_TOKEN, o -> o.getKey());
		verify(out).writeChar(CUSTOM_CHAR_NULL_TOKEN);
	}

	@Test
	public void writeCharObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeChar(out, (TestObject<Character>) null, CUSTOM_CHAR_NULL_TOKEN, o -> o.getKey());
		verify(out).writeChar(CUSTOM_CHAR_NULL_TOKEN);
	}

	@Test
	public void writeInt() throws Exception {
		final TestObject<Integer> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(INT_KEY);
		StreamUtil.writeInt(out, obj, o -> o.getKey());
		verify(out).writeInt(INT_KEY);
	}

	@Test
	public void writeIntObjectIsNull() throws Exception {
		StreamUtil.writeInt(out, (TestObject<Integer>) null, o -> o.getKey());
		verify(out).writeInt(DEFAULT_NULL_INT);
	}

	@Test
	public void writeIntKeyIsNull() throws Exception {
		final TestObject<Integer> obj = mock(TestObject.class);
		StreamUtil.writeInt(out, obj, o -> o.getKey());
		verify(out).writeInt(DEFAULT_NULL_INT);
	}

	@Test
	public void writeIntKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Integer> obj = mock(TestObject.class);
		StreamUtil.writeInt(out, obj, CUSTOM_INT_NULL_TOKEN, o -> o.getKey());
		verify(out).writeInt(CUSTOM_INT_NULL_TOKEN);
	}

	@Test
	public void writeIntObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeInt(out, (TestObject<Integer>) null, CUSTOM_INT_NULL_TOKEN, o -> o.getKey());
		verify(out).writeInt(CUSTOM_INT_NULL_TOKEN);
	}

	@Test
	public void writeLong() throws Exception {
		final TestObject<Long> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(LONG_KEY);
		StreamUtil.writeLong(out, obj, o -> o.getKey());
		verify(out).writeLong(LONG_KEY);
	}

	@Test
	public void writeLongObjectIsNull() throws Exception {
		StreamUtil.writeLong(out, (TestObject<Long>) null, o -> o.getKey());
		verify(out).writeLong(DEFAULT_NULL_LONG);
	}

	@Test
	public void writeLongKeyIsNull() throws Exception {
		final TestObject<Long> obj = mock(TestObject.class);
		StreamUtil.writeLong(out, obj, o -> o.getKey());
		verify(out).writeLong(DEFAULT_NULL_LONG);
	}

	@Test
	public void writeLongKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Long> obj = mock(TestObject.class);
		StreamUtil.writeLong(out, obj, CUSTOM_LONG_NULL_TOKEN, o -> o.getKey());
		verify(out).writeLong(CUSTOM_LONG_NULL_TOKEN);
	}

	@Test
	public void writeLongObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeLong(out, (TestObject<Long>) null, CUSTOM_LONG_NULL_TOKEN, o -> o.getKey());
		verify(out).writeLong(CUSTOM_LONG_NULL_TOKEN);
	}

	@Test
	public void writeFloat() throws Exception {
		final TestObject<Float> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(FLOAT_KEY);
		StreamUtil.writeFloat(out, obj, o -> o.getKey());
		verify(out).writeFloat(FLOAT_KEY);
	}

	@Test
	public void writeFloatObjectIsNull() throws Exception {
		StreamUtil.writeFloat(out, (TestObject<Float>) null, o -> o.getKey());
		verify(out).writeFloat(DEFAULT_NULL_FLOAT);
	}

	@Test
	public void writeFloatKeyIsNull() throws Exception {
		final TestObject<Float> obj = mock(TestObject.class);
		StreamUtil.writeFloat(out, obj, o -> o.getKey());
		verify(out).writeFloat(DEFAULT_NULL_FLOAT);
	}

	@Test
	public void writeFloatKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Float> obj = mock(TestObject.class);
		StreamUtil.writeFloat(out, obj, CUSTOM_FLOAT_NULL_TOKEN, o -> o.getKey());
		verify(out).writeFloat(CUSTOM_FLOAT_NULL_TOKEN);
	}

	@Test
	public void writeFloatObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeFloat(out, (TestObject<Float>) null, CUSTOM_FLOAT_NULL_TOKEN, o -> o.getKey());
		verify(out).writeFloat(CUSTOM_FLOAT_NULL_TOKEN);
	}

	@Test
	public void writeDouble() throws Exception {
		final TestObject<Double> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(DOUBLE_KEY);
		StreamUtil.writeDouble(out, obj, o -> o.getKey());
		verify(out).writeDouble(DOUBLE_KEY);
	}

	@Test
	public void writeDoubleObjectIsNull() throws Exception {
		StreamUtil.writeDouble(out, (TestObject<Double>) null, o -> o.getKey());
		verify(out).writeDouble(DEFAULT_NULL_DOUBLE);
	}

	@Test
	public void writeDoubleKeyIsNull() throws Exception {
		final TestObject<Double> obj = mock(TestObject.class);
		StreamUtil.writeDouble(out, obj, o -> o.getKey());
		verify(out).writeDouble(DEFAULT_NULL_DOUBLE);
	}

	@Test
	public void writeDoubleKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<Double> obj = mock(TestObject.class);
		StreamUtil.writeDouble(out, obj, CUSTOM_DOUBLE_NULL_TOKEN, o -> o.getKey());
		verify(out).writeDouble(CUSTOM_DOUBLE_NULL_TOKEN);
	}

	@Test
	public void writeDoubleObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeDouble(out, (TestObject<Double>) null, CUSTOM_DOUBLE_NULL_TOKEN, o -> o.getKey());
		verify(out).writeDouble(CUSTOM_DOUBLE_NULL_TOKEN);
	}

	@Test
	public void writeBytes() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(STRING_KEY);
		StreamUtil.writeBytes(out, obj, o -> o.getKey());
		verify(out).writeBytes(STRING_KEY);
	}

	@Test
	public void writeBytesObjectIsNull() throws Exception {
		StreamUtil.writeBytes(out, (TestObject<String>) null, o -> o.getKey());
		verify(out).writeBytes(DEFAULT_NULL_STRING);
	}

	@Test
	public void writeBytesKeyIsNull() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		StreamUtil.writeBytes(out, obj, o -> o.getKey());
		verify(out).writeBytes(DEFAULT_NULL_STRING);
	}

	@Test
	public void writeBytesKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		StreamUtil.writeBytes(out, obj, CUSTOM_STRING_NULL_TOKEN, o -> o.getKey());
		verify(out).writeBytes(CUSTOM_STRING_NULL_TOKEN);
	}

	@Test
	public void writeBytesObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeBytes(out, (TestObject<String>) null, CUSTOM_STRING_NULL_TOKEN, o -> o.getKey());
		verify(out).writeBytes(CUSTOM_STRING_NULL_TOKEN);
	}

	@Test
	public void writeChars() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(STRING_KEY);
		StreamUtil.writeChars(out, obj, o -> o.getKey());
		verify(out).writeChars(STRING_KEY);
	}

	@Test
	public void writeCharsObjectIsNull() throws Exception {
		StreamUtil.writeChars(out, (TestObject<String>) null, o -> o.getKey());
		verify(out).writeChars(DEFAULT_NULL_STRING);
	}

	@Test
	public void writeCharsKeyIsNull() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		StreamUtil.writeChars(out, obj, o -> o.getKey());
		verify(out).writeChars(DEFAULT_NULL_STRING);
	}

	@Test
	public void writeCharsKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		StreamUtil.writeChars(out, obj, CUSTOM_STRING_NULL_TOKEN, o -> o.getKey());
		verify(out).writeChars(CUSTOM_STRING_NULL_TOKEN);
	}

	@Test
	public void writeCharsObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeChars(out, (TestObject<String>) null, CUSTOM_STRING_NULL_TOKEN, o -> o.getKey());
		verify(out).writeChars(CUSTOM_STRING_NULL_TOKEN);
	}

	@Test
	public void writeUTF() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		when(obj.getKey()).thenReturn(STRING_KEY);
		StreamUtil.writeUTF(out, obj, o -> o.getKey());
		verify(out).writeUTF(STRING_KEY);
	}

	@Test
	public void writeUTFObjectIsNull() throws Exception {
		StreamUtil.writeUTF(out, (TestObject<String>) null, o -> o.getKey());
		verify(out).writeUTF(DEFAULT_NULL_STRING);
	}

	@Test
	public void writeUTFKeyIsNull() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		StreamUtil.writeUTF(out, obj, o -> o.getKey());
		verify(out).writeUTF(DEFAULT_NULL_STRING);
	}

	@Test
	public void writeUTFKeyIsNullWithCustomNullToken() throws Exception {
		final TestObject<String> obj = mock(TestObject.class);
		StreamUtil.writeUTF(out, obj, CUSTOM_STRING_NULL_TOKEN, o -> o.getKey());
		verify(out).writeUTF(CUSTOM_STRING_NULL_TOKEN);
	}

	@Test
	public void writeUTFObjectIsNullWithCustomNullToken() throws Exception {
		StreamUtil.writeUTF(out, (TestObject<String>) null, CUSTOM_STRING_NULL_TOKEN, o -> o.getKey());
		verify(out).writeUTF(CUSTOM_STRING_NULL_TOKEN);
	}
}
