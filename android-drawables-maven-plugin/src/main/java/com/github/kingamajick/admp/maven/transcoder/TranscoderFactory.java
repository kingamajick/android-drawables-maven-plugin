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

import org.apache.batik.transcoder.image.ImageTranscoder;

import com.github.kingamajick.admp.maven.util.Constants;

/**
 * @author R King
 * 
 */
public class TranscoderFactory {

	/**
	 * Creates a instance of a {@link ImageTranscoder} for the given type.
	 * 
	 * @param type
	 * @return An instance of a {@link ImageTranscoder} for the given type.
	 * @throws TranscoderFactoryException
	 *             if it is not possible to instantiate the transcoder, or no transcoder if found for that type.
	 */
	public ImageTranscoder create(final String type) throws TranscoderFactoryException {
		ImageTranscoder transcoder = null;
		if (Constants.VALID_TYPES.containsKey(type)) {
			Class<? extends ImageTranscoder> clazz = Constants.VALID_TYPES.get(type);
			try {
				transcoder = clazz.newInstance();
			}
			catch (InstantiationException e) {
				throw new TranscoderFactoryException("Unable to instantiate transcoder for type " + type, e);
			}
			catch (IllegalAccessException e) {
				throw new TranscoderFactoryException("Unable to instantiate transcoder for type " + type, e);
			}
		}
		if (transcoder == null) {
			throw new TranscoderFactoryException("No transcoder found for type " + type);
		}
		return transcoder;
	}
}
