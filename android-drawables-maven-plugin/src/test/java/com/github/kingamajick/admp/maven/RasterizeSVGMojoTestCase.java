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
package com.github.kingamajick.admp.maven;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.fest.reflect.core.Reflection;
import org.junit.Test;

import com.github.kingamajick.admp.maven.RasterizeSVGMojo;
import com.github.kingamajick.admp.maven.beans.Density;

/**
 * @author R King
 * 
 */
public class RasterizeSVGMojoTestCase {

	private final static File TEST_DIR = new File("target/test-classes/rasterize-svgs/");
	private Log mockLogger = createNiceMock(Log.class);

	private final static class PNGExpectationHolder {
		File expectedFile;
		int expectedWidth;
		int expectedHeight;
	}

	/**
	 * Helper method to create a {@link PNGExpectationHolder} with the given expected file, width and height.
	 * 
	 * @param expectedFile
	 * @param expectedWidth
	 * @param expectedHeight
	 * @return
	 */
	private final static PNGExpectationHolder createExpectation(final File expectedFile, final int expectedWidth, final int expectedHeight) {
		PNGExpectationHolder holder = new PNGExpectationHolder();
		holder.expectedFile = expectedFile;
		holder.expectedWidth = expectedWidth;
		holder.expectedHeight = expectedHeight;
		return holder;
	}

	@Test
	public void successfulRaterizationDefaultDensities() throws Exception {
		// Setup
		File testBaseDir = new File(TEST_DIR, "baseCase");
		File svgDir = new File(testBaseDir, "src/main/svg");
		File targetDir = new File(testBaseDir, "target/classes/res");
		File ldpiDir = new File(targetDir, "drawable-ldpi");
		File mdpiDir = new File(targetDir, "drawable-mdpi");
		File hdpiDir = new File(targetDir, "drawable-hdpi");
		File xhdpiDir = new File(targetDir, "drawable-xhdpi");
		String subDirFile = "icon_main.png";
		String rootFile = "image.png";

		RasterizeSVGMojo mojo = new RasterizeSVGMojo();
		mojo.setLog(this.mockLogger);

		Reflection.field("svgDirectory").ofType(File.class).in(mojo).set(svgDir);
		Reflection.field("targetDir").ofType(File.class).in(mojo).set(targetDir);
		Reflection.field("densities").ofType(List.class).in(mojo).set(new ArrayList<Density>());

		// @formatter:off
		PNGExpectationHolder[] expectedPNGs = {
				createExpectation(new File(ldpiDir,  subDirFile), 36, 36),
				createExpectation(new File(mdpiDir,  subDirFile), 48, 48),
				createExpectation(new File(hdpiDir,  subDirFile), 72, 72),
				createExpectation(new File(xhdpiDir, subDirFile), 96, 96),
				
				createExpectation(new File(ldpiDir,  rootFile),  150, 75),
				createExpectation(new File(mdpiDir,  rootFile),  200, 100),
				createExpectation(new File(hdpiDir,  rootFile),  300, 150),
				createExpectation(new File(xhdpiDir, rootFile),  400, 200),
		};
		// @formatter:on

		// Execute
		mojo.execute();

		// Assert
		for (PNGExpectationHolder holder : expectedPNGs) {
			assertTrue("Expected file " + holder.expectedFile.getAbsolutePath() + " does not exist.", holder.expectedFile.exists());
			BufferedImage img = ImageIO.read(holder.expectedFile);
			assertEquals("Width of generated PNG is " + img.getWidth() + "px, expected " + holder.expectedWidth + "px", holder.expectedWidth, img.getWidth());
			assertEquals("Height of generated PNG is " + img.getHeight() + "px, expected " + holder.expectedHeight + "px", holder.expectedHeight, img.getHeight());
		}
	}

	@Test(expected = MojoFailureException.class)
	public void unableToTranscode() throws Exception {
		// Setup
		File testBaseDir = new File(TEST_DIR, "transcodeException");
		File svgDir = new File(testBaseDir, "src/main/svg");
		File targetDir = new File(testBaseDir, "target/classes/res");

		PNGTranscoder mockTranscoder = createNiceMock(PNGTranscoder.class);

		RasterizeSVGMojo mojo = new RasterizeSVGMojo();
		mojo.setLog(this.mockLogger);

		Reflection.field("svgDirectory").ofType(File.class).in(mojo).set(svgDir);
		Reflection.field("targetDir").ofType(File.class).in(mojo).set(targetDir);
		Reflection.field("densities").ofType(List.class).in(mojo).set(new ArrayList<Density>());
		Reflection.field("transcoder").ofType(PNGTranscoder.class).in(mojo).set(mockTranscoder);

		// Expectation
		mockTranscoder.transcode(anyObject(TranscoderInput.class), anyObject(TranscoderOutput.class));
		expectLastCall().andThrow(new TranscoderException(""));

		replay(mockTranscoder);

		// Execute
		mojo.execute();
	}

}
