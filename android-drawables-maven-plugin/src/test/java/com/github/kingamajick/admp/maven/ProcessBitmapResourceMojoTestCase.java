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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.fest.reflect.core.Reflection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.kingamajick.admp.maven.ProcessBitmapResourcesMojo;
import com.github.kingamajick.maven.utils.TestUtils;

/**
 * @author R King
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileUtils.class })
public class ProcessBitmapResourceMojoTestCase {

	private final static File TEST_DIR = new File("target/test-classes/process-bitmap-resources/");
	private final static String IMAGE_FILE_NAME = "ic_launcher.png";
	private Log mockLogger = createNiceMock(Log.class);

	private final static class FileExpectationHolder {
		File inputFile;
		String inputFileChecksum;
		File expectedFile;
	}

	/**
	 * Helper method to create a {@link FileExpectationHolder} with the given input and expected file with the holders checksum generated.
	 * 
	 * @param inputFile
	 * @param expectedFile
	 * @return
	 * @throws MojoFailureException
	 */
	private final static FileExpectationHolder createExpectation(final File inputFile, final File expectedFile) throws MojoFailureException {
		FileExpectationHolder holder = new FileExpectationHolder();
		holder.inputFile = inputFile;
		holder.inputFileChecksum = TestUtils.getChecksum(inputFile);
		holder.expectedFile = expectedFile;
		return holder;
	}

	@Test
	public void successfulCopy() throws Exception {
		// Setup
		File testBaseDir = new File(TEST_DIR, "baseCase");
		File resouceDir = new File(testBaseDir, "src/main/resources");
		File targetDir = new File(testBaseDir, "target/classes/res");

		Resource mockResource = createMock(Resource.class);
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(mockResource);

		ProcessBitmapResourcesMojo mojo = new ProcessBitmapResourcesMojo();
		Reflection.field("resources").ofType(List.class).in(mojo).set(resources);
		Reflection.field("targetDir").ofType(File.class).in(mojo).set(targetDir);
		mojo.setLog(this.mockLogger);

		// @formatter:off
		FileExpectationHolder[] expectedFiles = {
			createExpectation(new File(resouceDir, "ldpi/"  + IMAGE_FILE_NAME), new File(targetDir, "drawable-ldpi/"  + IMAGE_FILE_NAME)),
			createExpectation(new File(resouceDir, "mdpi/"  + IMAGE_FILE_NAME), new File(targetDir, "drawable-mdpi/"  + IMAGE_FILE_NAME)),
			createExpectation(new File(resouceDir, "hdpi/"  + IMAGE_FILE_NAME), new File(targetDir, "drawable-hdpi/"  + IMAGE_FILE_NAME)),
			createExpectation(new File(resouceDir, "xhdpi/" + IMAGE_FILE_NAME), new File(targetDir, "drawable-xhdpi/" + IMAGE_FILE_NAME)),
			createExpectation(new File(resouceDir, "nodpi/" + IMAGE_FILE_NAME), new File(targetDir, "drawable-nodpi/" + IMAGE_FILE_NAME)),
			createExpectation(new File(resouceDir, "tvdpi/" + IMAGE_FILE_NAME), new File(targetDir, "drawable-tvdpi/" + IMAGE_FILE_NAME)),
				
		};
		// @formatter:on 

		// Expectations
		expect(mockResource.getDirectory()).andReturn(resouceDir.getAbsolutePath());

		replay(mockResource);

		// Execute
		mojo.execute();

		// Assert
		for (FileExpectationHolder holder : expectedFiles) {
			assertTrue("Expected file " + holder.expectedFile.getAbsolutePath() + " does not exist.", holder.expectedFile.exists());
			assertEquals("Checksums don't match for files " + holder.inputFile + " and " + holder.expectedFile, holder.inputFileChecksum, TestUtils.getChecksum(holder.expectedFile));

		}
		verify(mockResource);
	}

	@Test(expected = MojoFailureException.class)
	public void ioException() throws Exception {
		// Setup
		File testBaseDir = new File(TEST_DIR, "baseCase");
		File resouceDir = new File(testBaseDir, "src/main/resources");
		File targetDir = new File(testBaseDir, "target/classes/res");

		Resource mockResource = createMock(Resource.class);
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(mockResource);

		PowerMock.mockStatic(FileUtils.class);

		ProcessBitmapResourcesMojo mojo = new ProcessBitmapResourcesMojo();
		Reflection.field("resources").ofType(List.class).in(mojo).set(resources);
		Reflection.field("targetDir").ofType(File.class).in(mojo).set(targetDir);
		mojo.setLog(this.mockLogger);

		// Expectations
		expect(mockResource.getDirectory()).andReturn(resouceDir.getAbsolutePath());
		FileUtils.copyFile(anyObject(File.class), anyObject(File.class));
		PowerMock.expectLastCall().andThrow(new IOException());

		replay(mockResource);
		PowerMock.replayAll();

		// Execute
		mojo.execute();
	}
}
