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
package com.github.kingamajick.admp.maven.beans;

import java.util.List;

/**
 * @author R King
 * 
 */
public class Density {

	private String name;
	private float scaleFactor;

	/**
	 * Populates the list with the following densities: *
	 * <ul>
	 * <li>name : 'drawable-ldpi', scale-factor : 0.75f</li>
	 * <li>name : 'drawable-mdpi', scale-factor : 1.00f</li>
	 * <li>name : 'drawable-hdpi', scale-factor : 1.50f</li>
	 * <li>name : 'drawable-xhdpi', scale-factor : 2.00f</li>
	 * </ul>
	 * 
	 * @param densities
	 */
	public static void defaults(final List<Density> densities) {
		densities.add(new Density("drawable-ldpi", 0.75f));
		densities.add(new Density("drawable-mdpi", 1.00f));
		densities.add(new Density("drawable-hdpi", 1.50f));
		densities.add(new Density("drawable-xhdpi", 2.00f));
	}

	public Density() {
	}

	private Density(final String name, final float scaleFactor) {
		this.name = name;
		this.scaleFactor = scaleFactor;
	}

	/**
	 * @return the name of this density, used for the output directory.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the scale factor for the bitmap generated from the svg.
	 */
	public float getScaleFactor() {
		return this.scaleFactor;
	}

}
