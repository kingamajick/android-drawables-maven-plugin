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
package com.github.kingamajick.maven.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @author R King
 * 
 */
public class TestUtils {

	/**
	 * Generate a md5 checksum for a file.
	 * 
	 * @param f
	 * @throws MojoFailureException
	 */
	public final static String getChecksum(final File f) throws MojoFailureException {
		try {
			FileInputStream fis = new FileInputStream(f);
			return DigestUtils.md5Hex(fis);
		}
		catch (FileNotFoundException e) {
			throw new MojoFailureException("Unable to generate checksum for " + f, e);
		}
		catch (IOException e) {
			throw new MojoFailureException("Unable to generate checksum for " + f, e);
		}
	}

}
