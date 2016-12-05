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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks a method on a serializable class as write injector. This causes the
 * enhancer to generate a {@code writeObject} method as specified by <a href=
 * "http://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html">Serializable</a>.
 * An existing {@code writeObject} method will be enhanced.
 * 
 * <p>
 * Note: {@link ObjectOutputStream#defaultWriteObject()} will be called as very
 * first action if and only if a <em>new</em> {@code writeObject} method is
 * being generated. If an existing {@code writeObject} method is enhanced, the
 * client code is responsible to call {@code defaultWriteObject} at the
 * appropriate place if necessary.
 * </p>
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface WriteObject {

}
