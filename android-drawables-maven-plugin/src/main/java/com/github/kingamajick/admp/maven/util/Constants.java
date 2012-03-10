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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

/**
 * @author R King
 * 
 */
public class Constants {

	/**
	 * Drawable directory prefix
	 */
	public final static String DRAWABLE_PREFIX = "drawable-";

	/**
	 * Array of directory names to copy the contents of when processing static artifacts.
	 */
	public final static String[] STATIC_IMAGE_DIRS = { "ldpi", "mdpi", "hdpi", "xhdpi", "nodpi", "tvdpi" };

	/**
	 * Array of each of the possible density directory names.
	 */
	public final static String[] DRAWABLE_DIRS = { "drawable-ldpi", "drawable-mdpi", "drawable-hdpi", "drawable-xhdpi", "drawable-nodpi", "drawable-tvdpi" };

	/**
	 * Array of acceptable file types
	 */
	public final static List<String> IMAGE_TYPES = Arrays.asList("png", "jpg", "gif");

	/**
	 * SVG file extension
	 */
	public static final String SVG_FILE_TYPE = ".svg";

	/**
	 * Map of valid types prefixed mapped to the {@link ImageTranscoder} class.
	 */
	public static final Map<String, Class<? extends ImageTranscoder>> VALID_TYPES = new HashMap<String, Class<? extends ImageTranscoder>>();
	// Init valid types map
	static {
		VALID_TYPES.put("png", PNGTranscoder.class);
		VALID_TYPES.put("jpg", JPEGTranscoder.class);
	}

}
