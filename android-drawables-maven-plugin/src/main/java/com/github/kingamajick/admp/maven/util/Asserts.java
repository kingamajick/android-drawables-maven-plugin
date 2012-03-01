/**
 * Copyright 2012 R King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kingamajick.admp.maven.util;

/**
 * Helper class for argument checking.
 * 
 * @author R King
 * 
 */
public class Asserts {

	/**
	 * Throws an {@link IllegalArgumentException} if the object is null. The exception message will include the name specified.
	 * 
	 * @param o
	 * @param name
	 */
	public static void notNull(final Object o, final String name) {
		if (o == null) {
			throw new IllegalArgumentException(name + " must not be null");
		}
	}

	/**
	 * Throws an {@link IllegalArgumentException} if the number is negative. The exception message will include the name specified.
	 * 
	 * @param o
	 * @param name
	 */
	public static void positive(final double d, final String name) {
		if (d < 0) {
			throw new IllegalArgumentException(name + "must be postive");
		}
	}
}
