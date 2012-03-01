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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.repository.RepositorySystem;
import org.fest.reflect.core.Reflection;
import org.fest.reflect.reference.TypeRef;
import org.junit.Test;

import com.github.kingamajick.admp.maven.UnpackBitmapResourcesMojo;
import com.github.kingamajick.admp.maven.beans.DrawableArtifact;
import com.github.kingamajick.maven.utils.TestUtils;

/**
 * @author R King
 * 
 */
public class UnpackBitmapResourcesMojoTestCase {
	private final static File TEST_DIR = new File("target/test-classes/unpack-bitmap-resources/");
	// MD5 checksum for the image file contain in drawable-artifact.zip
	private final static String TEST_PNG_MD5CHSUM = "0b49398485cd262e38244521a70dbb11";
	private Log mockLogger = createNiceMock(Log.class);

	@Test
	public void successfulUnpack() throws Exception {
		// Setup
		RepositorySystem mockRepositorySystem = createMock(RepositorySystem.class);
		ArtifactRepository mockLocalRepository = createMock(ArtifactRepository.class);
		List<ArtifactRepository> remoteRepositories = Collections.emptyList();

		File testBaseDir = new File(TEST_DIR, "baseCase");
		File unpackLocation = new File(testBaseDir, "target/unpack");
		File drawableArtifactZip = new File(testBaseDir, "drawable-artifact.zip");

		String groupId = "org.test";
		String artifactId = "drawable-artifact";
		String version = "0.0.0";
		List<DrawableArtifact> drawableArtifacts = new ArrayList<DrawableArtifact>();
		DrawableArtifact drawableArtifact = new DrawableArtifact(groupId, artifactId, version);
		drawableArtifacts.add(drawableArtifact);

		Artifact mockArtifact = createMock(Artifact.class);

		UnpackBitmapResourcesMojo mojo = new UnpackBitmapResourcesMojo();
		Reflection.field("repositorySystem").ofType(RepositorySystem.class).in(mojo).set(mockRepositorySystem);
		Reflection.field("localRepository").ofType(ArtifactRepository.class).in(mojo).set(mockLocalRepository);
		Reflection.field("remoteRepositories").ofType(new TypeRef<List<ArtifactRepository>>() {}).in(mojo).set(remoteRepositories);
		Reflection.field("unpackLocation").ofType(File.class).in(mojo).set(unpackLocation);
		Reflection.field("drawableArtifacts").ofType(new TypeRef<List<DrawableArtifact>>() {}).in(mojo).set(drawableArtifacts);
		mojo.setLog(this.mockLogger);

		// Expectations
		mockRepositorySystem.createArtifact(groupId, artifactId, version, "android-drawables");
		expectLastCall().andReturn(mockArtifact);
		mockRepositorySystem.resolve(anyObject(ArtifactResolutionRequest.class));
		expectLastCall().andReturn(createNiceMock(ArtifactResolutionResult.class));
		mockArtifact.isResolved();
		expectLastCall().andReturn(true);
		mockArtifact.getFile();
		expectLastCall().andReturn(drawableArtifactZip);

		replay(mockRepositorySystem, mockLocalRepository, mockArtifact);

		// Execute
		mojo.execute();

		// Assert
		File unpackedImageFile = new File(testBaseDir, "target/unpack/res/image.png");
		assertTrue(unpackedImageFile.exists());
		assertEquals(TEST_PNG_MD5CHSUM, TestUtils.getChecksum(unpackedImageFile));
	}
}
