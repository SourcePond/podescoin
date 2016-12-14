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
package ch.sourcepond.utils.podescoin.testbundle;

import ch.sourcepond.utils.podescoin.api.Component;
import ch.sourcepond.utils.podescoin.testservice.NameService;

/**
 * The framework should be able to enhance this class, but, should log that it
 * is not serializable.
 *
 */
public class UnserializableObject {

	@Component
	private transient NameService nameService;
}
