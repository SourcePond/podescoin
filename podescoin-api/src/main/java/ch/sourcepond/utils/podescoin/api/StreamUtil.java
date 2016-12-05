/*Copyright (C) 2016 Roland Hauser, <sourcepond@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package ch.sourcepond.utils.podescoin.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

/**
 * Utility class which provides static methods for writing primitive or and
 * string values derived from objects to an {@link ObjectOutputStream} and
 * restoring objects based on primitive or string values read from an
 * {@link ObjectInputStream}.
 * 
 * <p>
 * The utility methods are useful when you want to use a primitive value or a
 * string to write and restore an object. For instance, suppose an object which
 * is serializable and persistently stored in a database. Instead of
 * transferring the whole object it's more efficient to transfer its ID only
 * through a {@code write} method. That can then be read by a {@code read}
 * method, and, be used to re-load the object from the database.
 * </p>
 *
 */
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

	private static void checkNotNull(final Object pObj, final String pMessage) {
		if (pObj == null) {
			throw new NullPointerException(pMessage);
		}
	}

	private static <R, T> Optional<R> read(final ObjectInputStream pIn, final Reader<T> pReader, final T pNullToken,
			final Function<T, R> pKeyFunction) throws IOException {
		checkNotNull(pIn, "ObjectInputStream cannot be null!");
		checkNotNull(pNullToken, "Null-token cannot be null!");
		checkNotNull(pKeyFunction, "Function cannot be null!");

		final T key = pReader.read(pIn);
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
		checkNotNull(pOut, "ObjectOutputStream cannot be null!");
		checkNotNull(pNullToken, "Null-token cannot be null!");
		checkNotNull(pKeyFunction, "Function cannot be null!");

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

		// The apply the null token
		if (key == null) {
			key = pNullToken;
		}
		pWriter.write(pOut, key);
	}

	/**
	 * Reads a {@link Byte} from the stream specified using
	 * {@link ObjectInputStream#readByte()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Byte} read from the stream is equal to
	 * {@code -1}, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Byte} used as
	 *            argument for the function specified. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Byte} to evaluate the
	 *            result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readByte()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readByte(final ObjectInputStream pIn, final Function<Byte, T> pFunction)
			throws IOException {
		return readByte(pIn, DEFAULT_NULL_BYTE, pFunction);
	}

	/**
	 * Reads a {@link Byte} from the stream specified using
	 * {@link ObjectInputStream#readByte()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Byte} read from the stream is equal to
	 * the null-token specified, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Byte} used as
	 *            argument for the function specified. Must not be {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Byte} read from
	 *            the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Byte} to evaluate the
	 *            result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readByte()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readByte(final ObjectInputStream pIn, final Byte pNullToken,
			final Function<Byte, T> pFunction) throws IOException {
		return read(pIn, in -> in.readByte(), pNullToken, pFunction);
	}

	/**
	 * Reads a {@link Short} from the stream specified using
	 * {@link ObjectInputStream#readShort()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Short} read from the stream is equal to
	 * {@code -1}, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Short} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Short} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readShort()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readShort(final ObjectInputStream pIn, final Function<Short, T> pFunction)
			throws IOException {
		return readShort(pIn, DEFAULT_NULL_SHORT, pFunction);
	}

	/**
	 * Reads a {@link Short} from the stream specified using
	 * {@link ObjectInputStream#readShort()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Short} read from the stream is equal to
	 * the null-token specified, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Short} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Short} read from
	 *            the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Short} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readShort()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readShort(final ObjectInputStream pIn, final Short pNullToken,
			final Function<Short, T> pFunction) throws IOException {
		return read(pIn, in -> in.readShort(), pNullToken, pFunction);
	}

	/**
	 * Reads a {@link Character} from the stream specified using
	 * {@link ObjectInputStream#readChar()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Character} read from the stream is equal
	 * to {@code '\u0000'}, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Character}
	 *            used as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Character} to
	 *            evaluate the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readChar()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readChar(final ObjectInputStream pIn, final Function<Character, T> pFunction)
			throws IOException {
		return readChar(pIn, DEFAULT_NULL_CHARACTER, pFunction);
	}

	/**
	 * Reads a {@link Character} from the stream specified using
	 * {@link ObjectInputStream#readChar()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Character} read from the stream is equal
	 * to the null-token specified, the function will not be executed and an
	 * empty {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Character}
	 *            used as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Character} read
	 *            from the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Character} to
	 *            evaluate the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readChar()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readChar(final ObjectInputStream pIn, final Character pNullToken,
			final Function<Character, T> pFunction) throws IOException {
		return read(pIn, in -> in.readChar(), pNullToken, pFunction);
	}

	/**
	 * Reads a {@link Integer} from the stream specified using
	 * {@link ObjectInputStream#readInt()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Integer} read from the stream is equal to
	 * {@code -1}, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Integer} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Integer} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readInt()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readInt(final ObjectInputStream pIn, final Function<Integer, T> pFunction)
			throws IOException {
		return readInt(pIn, DEFAULT_NULL_INT, pFunction);
	}

	/**
	 * Reads a {@link Integer} from the stream specified using
	 * {@link ObjectInputStream#readInt()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Integer} read from the stream is equal to
	 * the null-token specified, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Integer} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Integer} read
	 *            from the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Integer} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readInt()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readInt(final ObjectInputStream pIn, final Integer pNullToken,
			final Function<Integer, T> pFunction) throws IOException {
		return read(pIn, in -> in.readInt(), pNullToken, pFunction);
	}

	/**
	 * Reads a {@link Long} from the stream specified using
	 * {@link ObjectInputStream#readLong()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Long} read from the stream is equal to
	 * {@code -1l}, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Long} used as
	 *            argument for the function specified. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Long} to evaluate the
	 *            result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readLong()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readLong(final ObjectInputStream pIn, final Function<Long, T> pFunction)
			throws IOException {
		return readLong(pIn, DEFAULT_NULL_LONG, pFunction);
	}

	/**
	 * Reads a {@link Long} from the stream specified using
	 * {@link ObjectInputStream#readLong()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Long} read from the stream is equal to
	 * the null-token specified, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Long} used as
	 *            argument for the function specified. Must not be {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Long} read from
	 *            the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Long} to evaluate the
	 *            result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readLong()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readLong(final ObjectInputStream pIn, final Long pNullToken,
			final Function<Long, T> pFunction) throws IOException {
		return read(pIn, in -> in.readLong(), pNullToken, pFunction);
	}

	/**
	 * Reads a {@link Float} from the stream specified using
	 * {@link ObjectInputStream#readFloat()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Float} read from the stream is equal to
	 * {@code -1f}, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Float} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Float} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readFloat()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readFloat(final ObjectInputStream pIn, final Function<Float, T> pFunction)
			throws IOException {
		return readFloat(pIn, DEFAULT_NULL_FLOAT, pFunction);
	}

	/**
	 * Reads a {@link Float} from the stream specified using
	 * {@link ObjectInputStream#readFloat()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the {@code Float} read from the stream is equal to
	 * the null-token specified, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Float} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Float} read from
	 *            the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Float} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readFloat()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readFloat(final ObjectInputStream pIn, final Float pNullToken,
			final Function<Float, T> pFunction) throws IOException {
		return read(pIn, in -> in.readFloat(), pNullToken, pFunction);
	}

	/**
	 * Reads a {@link Double} from the stream specified using
	 * {@link ObjectInputStream#readDouble()} which is then passed as argument
	 * to the function specified to evaluate an object. The resulting object
	 * will be wrapped into an {@link Optional} which will be returned to the
	 * caller. If the result of the function is {@code null}, an empty
	 * {@link Optional} will be returned. If the {@code Double} read from the
	 * stream is equal to {@code -1d}, the function will not be executed and an
	 * empty {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Double} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Double} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readDouble()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readDouble(final ObjectInputStream pIn, final Function<Double, T> pFunction)
			throws IOException {
		return readDouble(pIn, DEFAULT_NULL_DOUBLE, pFunction);
	}

	/**
	 * Reads a {@link Double} from the stream specified using
	 * {@link ObjectInputStream#readDouble()} which is then passed as argument
	 * to the function specified to evaluate an object. The resulting object
	 * will be wrapped into an {@link Optional} which will be returned to the
	 * caller. If the result of the function is {@code null}, an empty
	 * {@link Optional} will be returned. If the {@code Double} read from the
	 * stream is equal to the null-token specified, the function will not be
	 * executed and an empty {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the {@code Double} used
	 *            as argument for the function specified. Must not be
	 *            {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the {@code Double} read from
	 *            the stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the {@code Double} to evaluate
	 *            the result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readDouble()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readDouble(final ObjectInputStream pIn, final Double pNullToken,
			final Function<Double, T> pFunction) throws IOException {
		return read(pIn, in -> in.readDouble(), pNullToken, pFunction);
	}

	/**
	 * Reads a string from the stream specified using
	 * {@link ObjectInputStream#readUTF()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the string read from the stream is empty, the
	 * function will not be executed and an empty {@link Optional} will be
	 * returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the string used as
	 *            argument for the function specified. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the string to evaluate the
	 *            result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readUTF()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> Optional<T> readUTF(final ObjectInputStream pIn, final Function<String, T> pFunction)
			throws IOException {
		return readUTF(pIn, DEFAULT_NULL_STRING, pFunction);
	}

	/**
	 * Reads a string from the stream specified using
	 * {@link ObjectInputStream#readUTF()} which is then passed as argument to
	 * the function specified to evaluate an object. The resulting object will
	 * be wrapped into an {@link Optional} which will be returned to the caller.
	 * If the result of the function is {@code null}, an empty {@link Optional}
	 * will be returned. If the string read from the stream is equal to the
	 * null-token specified, the function will not be executed and an empty
	 * {@link Optional} will be returned.
	 * 
	 * @param <T>
	 *            Type of the object to be evaluated
	 * 
	 * @param pIn
	 *            The object input stream where to read the string used as
	 *            argument for the function specified. Must not be {@code null}
	 * @param pNullToken
	 *            Token which should be compared to the string read from the
	 *            stream. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the string to evaluate the
	 *            result object. Must not be {@code null}
	 * @return An optional which wraps the result, never {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectInputStream#readUTF()} has
	 *             failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectInputStream, the function
	 *             specified or the null-token is {@code null}
	 */
	public static <T> Optional<T> readUTF(final ObjectInputStream pIn, final String pNullToken,
			final Function<String, T> pFunction) throws IOException {
		return read(pIn, in -> in.readUTF(), pNullToken, pFunction);
	}

	/**
	 * Evaluates a {@link Byte} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Byte} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeByte(int)}. If the source object itself or
	 * the result is {@code null}, {@code -1} will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Byte} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Byte} be to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeByte(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeByte(final ObjectOutputStream pOut, final T pSource, final Function<T, Byte> pFunction)
			throws IOException {
		writeByte(pOut, pSource, DEFAULT_NULL_BYTE, pFunction);
	}

	/**
	 * Evaluates a {@link Byte} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Byte} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeByte(int)}. If the source object itself or
	 * the result is {@code null}, the null-token specified will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Byte} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Byte} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeByte(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeByte(final ObjectOutputStream pOut, final T pSource, final Byte pNullToken,
			final Function<T, Byte> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeByte(key.intValue()), pNullToken);
	}

	/**
	 * Evaluates a {@link Short} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Short} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeShort(int)}. If the source object itself
	 * or the result is {@code null}, {@code -1} will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Short} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Short} be to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeShort(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeShort(final ObjectOutputStream pOut, final T pSource,
			final Function<T, Short> pFunction) throws IOException {
		writeShort(pOut, pSource, DEFAULT_NULL_SHORT, pFunction);
	}

	/**
	 * Evaluates a {@link Short} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Short} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeShort(int)}. If the source object itself
	 * or the result is {@code null}, the null-token specified will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Short} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Short} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeShort(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeShort(final ObjectOutputStream pOut, final T pSource, final Short pNullToken,
			final Function<T, Short> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeShort(key.shortValue()), pNullToken);
	}

	/**
	 * Evaluates a {@link Character} from the source object specified applying
	 * the function specified. If not {@code null}, the resulting
	 * {@code Character} will be written to the stream specified using
	 * {@link ObjectOutputStream#writeChar(int)}. If the source object itself or
	 * the result is {@code null}, {@code '\u0000'} will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Character} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Character} be to be written. Must not be
	 *            {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeChar(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeChar(final ObjectOutputStream pOut, final T pSource,
			final Function<T, Character> pFunction) throws IOException {
		writeChar(pOut, pSource, DEFAULT_NULL_CHARACTER, pFunction);
	}

	/**
	 * Evaluates a {@link Character} from the source object specified applying
	 * the function specified. If not {@code null}, the resulting
	 * {@code Character} will be written to the stream specified using
	 * {@link ObjectOutputStream#writeChar(int)}. If the source object itself or
	 * the result is {@code null}, the null-token specified will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Character} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Character} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeChar(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeChar(final ObjectOutputStream pOut, final T pSource, final Character pNullToken,
			final Function<T, Character> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeChar(key.charValue()), pNullToken);
	}

	/**
	 * Evaluates a {@link Integer} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Integer}
	 * will be written to the stream specified using
	 * {@link ObjectOutputStream#writeInt(int)}. If the source object itself or
	 * the result is {@code null}, {@code -1} will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Integer} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Integer} be to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeInt(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeInt(final ObjectOutputStream pOut, final T pSource,
			final Function<T, Integer> pFunction) throws IOException {
		writeInt(pOut, pSource, DEFAULT_NULL_INT, pFunction);
	}

	/**
	 * Evaluates a {@link Integer} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Integer}
	 * will be written to the stream specified using
	 * {@link ObjectOutputStream#writeInt(int)}. If the source object itself or
	 * the result is {@code null}, the null-token specified will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Integer} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Integer} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeInt(int)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeInt(final ObjectOutputStream pOut, final T pSource, final Integer pNullToken,
			final Function<T, Integer> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeInt(key.intValue()), pNullToken);
	}

	/**
	 * Evaluates a {@link Long} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Long} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeLong(long)}. If the source object itself
	 * or the result is {@code null}, {@code -1l} will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Long} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Long} be to written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeLong(long)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeLong(final ObjectOutputStream pOut, final T pSource, final Function<T, Long> pFunction)
			throws IOException {
		writeLong(pOut, pSource, DEFAULT_NULL_LONG, pFunction);
	}

	/**
	 * Evaluates a {@link Long} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Long} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeLong(long)}. If the source object itself
	 * or the result is {@code null}, the null-token specified will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Long} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Long} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeLong(long)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeLong(final ObjectOutputStream pOut, final T pSource, final Long pNullToken,
			final Function<T, Long> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeLong(key.longValue()), pNullToken);
	}

	/**
	 * Evaluates a {@link Float} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Float} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeFloat(float)}. If the source object itself
	 * or the result is {@code null}, {@code -1f} will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Float} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Float} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeFloat(float)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeFloat(final ObjectOutputStream pOut, final T pSource,
			final Function<T, Float> pFunction) throws IOException {
		writeFloat(pOut, pSource, DEFAULT_NULL_FLOAT, pFunction);
	}

	/**
	 * Evaluates a {@link Float} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Float} will
	 * be written to the stream specified using
	 * {@link ObjectOutputStream#writeFloat(float)}. If the source object itself
	 * or the result is {@code null}, the null-token specified will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Float} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Float} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeFloat(float)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeFloat(final ObjectOutputStream pOut, final T pSource, final Float pNullToken,
			final Function<T, Float> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeFloat(key.floatValue()), pNullToken);
	}

	/**
	 * Evaluates a {@link Double} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Double}
	 * will be written to the stream specified using
	 * {@link ObjectOutputStream#writeDouble(double)}. If the source object
	 * itself or the result is {@code null}, {@code -1d} will be written
	 * instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Double} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Double} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeDouble(double)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeDouble(final ObjectOutputStream pOut, final T pSource,
			final Function<T, Double> pFunction) throws IOException {
		writeDouble(pOut, pSource, DEFAULT_NULL_DOUBLE, pFunction);
	}

	/**
	 * Evaluates a {@link Double} from the source object specified applying the
	 * function specified. If not {@code null}, the resulting {@code Double}
	 * will be written to the stream specified using
	 * {@link ObjectOutputStream#writeDouble(double)}. If the source object
	 * itself or the result is {@code null}, the null-token specified will be
	 * written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            {@code Double} into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the {@code Double} to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeDouble(double)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeDouble(final ObjectOutputStream pOut, final T pSource, final Double pNullToken,
			final Function<T, Double> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeDouble(key.doubleValue()), pNullToken);
	}

	/**
	 * Evaluates a string from the source object specified applying the function
	 * specified. If not {@code null}, the resulting string will be written to
	 * the stream specified using {@link ObjectOutputStream#writeBytes(String)}.
	 * If the source object itself or the result is {@code null}, an empty
	 * string will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            string into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the string to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeBytes(String)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeBytes(final ObjectOutputStream pOut, final T pSource,
			final Function<T, String> pFunction) throws IOException {
		writeBytes(pOut, pSource, DEFAULT_NULL_STRING, pFunction);
	}

	/**
	 * Evaluates a string from the source object specified applying the function
	 * specified. If not {@code null}, the resulting string will be written to
	 * the stream specified using {@link ObjectOutputStream#writeBytes(String)}.
	 * If the source object itself or the result is {@code null}, the null-token
	 * specified will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            string into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the string to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeBytes(String)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeBytes(final ObjectOutputStream pOut, final T pSource, final String pNullToken,
			final Function<T, String> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeBytes(key), pNullToken);
	}

	/**
	 * Evaluates a string from the source object specified applying the function
	 * specified. If not {@code null}, the resulting string will be written to
	 * the stream specified using {@link ObjectOutputStream#writeChars(String)}.
	 * If the source object itself or the result is {@code null}, an empty
	 * string will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            string into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the string to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeChars(String)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeChars(final ObjectOutputStream pOut, final T pSource,
			final Function<T, String> pFunction) throws IOException {
		writeChars(pOut, pSource, DEFAULT_NULL_STRING, pFunction);
	}

	/**
	 * Evaluates a string from the source object specified applying the function
	 * specified. If not {@code null}, the resulting string will be written to
	 * the stream specified using {@link ObjectOutputStream#writeChars(String)}.
	 * If the source object itself or the result is {@code null}, the null-token
	 * specified will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            string into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the string to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if
	 *             {@link ObjectOutputStream#writeChars(String)} has failed for
	 *             some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeChars(final ObjectOutputStream pOut, final T pSource, final String pNullToken,
			final Function<T, String> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeChars(key), pNullToken);
	}

	/**
	 * Evaluates a string from the source object specified applying the function
	 * specified. If not {@code null}, the resulting string will be written to
	 * the stream specified using {@link ObjectOutputStream#writeUTF(String)}.
	 * If the source object itself or the result is {@code null}, an empty
	 * string will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            string into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the string to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeUTF(String)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream or the function
	 *             specified is {@code null}
	 */
	public static <T> void writeUTF(final ObjectOutputStream pOut, final T pSource, final Function<T, String> pFunction)
			throws IOException {
		writeUTF(pOut, pSource, DEFAULT_NULL_STRING, pFunction);
	}

	/**
	 * Evaluates a string from the source object specified applying the function
	 * specified. If not {@code null}, the resulting string will be written to
	 * the stream specified using {@link ObjectOutputStream#writeUTF(String)}.
	 * If the source object itself or the result is {@code null}, the null-token
	 * specified will be written instead.
	 * 
	 * @param <T>
	 *            Type of the source object
	 * @param pOut
	 *            The object output stream to be used to write the resulting
	 *            string into. Must not be {@code null}
	 * @param pSource
	 *            The source-object or {@code null}
	 * @param pNullToken
	 *            Token which should be written instead if the source object or
	 *            the result is {@code null}. Must not be {@code null}
	 * @param pFunction
	 *            The function to be applied on the source object to evaluate
	 *            the string to be written. Must not be {@code null}
	 * @throws IOException
	 *             Thrown if {@link Function#apply(Object)} has thrown an
	 *             exception, or, if {@link ObjectOutputStream#writeUTF(String)}
	 *             has failed for some reason.
	 * @throws NullPointerException
	 *             Thrown, if either the ObjectOutputStream, the null-token or
	 *             the function specified is {@code null}
	 */
	public static <T> void writeUTF(final ObjectOutputStream pOut, final T pSource, final String pNullToken,
			final Function<T, String> pFunction) throws IOException {
		write(pOut, pSource, pFunction, (out, key) -> out.writeUTF(key), pNullToken);
	}
}
