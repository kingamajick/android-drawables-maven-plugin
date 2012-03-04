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
package com.github.kingamajick.admp.maven.transcoder;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.junit.Test;

import com.github.kingamajick.admp.maven.util.Constants;

/**
 * @author R King
 * 
 */
public class TranscoderFactoryTestCase {

	@Test
	public void instantiatesCorrectTypes() throws Exception {
		// Setup
		TranscoderFactory factory = new TranscoderFactory();

		// Execute and Assert
		for (Entry<String, Class<? extends ImageTranscoder>> entry : Constants.VALID_TYPES.entrySet()) {
			ImageTranscoder transcoder = factory.create(entry.getKey());
			assertEquals(entry.getValue(), transcoder.getClass());
		}
	}

	@Test(expected = TranscoderFactoryException.class)
	public void unknownType() throws Exception {
		// Setup
		String type = "unknown";
		TranscoderFactory factory = new TranscoderFactory();

		// Exectue
		factory.create(type);
	}

}
