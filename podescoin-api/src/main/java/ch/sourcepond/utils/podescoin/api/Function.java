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

/**
 * Represents a function that accepts one argument and produces a result. The
 * function can throw an exception of any kind and is considered to be used in
 * conjunction with {@link StreamUtil}.
 * 
 * <p>
 * This is a functional <a href=
 * "https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">functional
 * interface</a> whose functional method is {@link #apply(Object)}.
 * 
 *
 * @param <T>
 *            the type of the input to the function
 * @param <R>
 *            the type of the result of the function
 */
@FunctionalInterface
public interface Function<T, R> {

	/**
	 * Applies this function to the given argument.
	 *
	 * @param pSource
	 *            the function argument
	 * @return the function result
	 * @throws Exception
	 *             Thrown, if something went wrong.
	 */
	R apply(T pSource) throws Exception;
}
