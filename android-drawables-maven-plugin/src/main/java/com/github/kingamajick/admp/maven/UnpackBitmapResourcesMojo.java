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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

import com.github.kingamajick.admp.maven.beans.DrawableArtifact;
import com.github.kingamajick.admp.maven.util.Constants;

/**
 * Unpack the bitmap resources contained in one or more 'android-drawables' artifacts.
 * 
 * @author R King
 * 
 * @goal unpack
 */
public class UnpackBitmapResourcesMojo extends AbstractMojo {

	/**
	 * @component
	 * @readonly
	 */
	private RepositorySystem repositorySystem;

	/**
	 * @parameter expression = "${localRepository}"
	 * @readonly
	 */
	private ArtifactRepository localRepository;

	/**
	 * @parameter expression = "${project.remoteArtifactRepositories}"
	 * @readonly
	 */
	private List<ArtifactRepository> remoteRepositories;

	/**
	 * The location to unpack the drawables too.
	 * 
	 * @parameter expression = "${unpackLocation}" default-value = "${project.build.directory}/android-drawables"
	 */
	private File unpackLocation;

	/**
	 * A list of {@link DrawableArtifact}s to unpack.
	 * 
	 * @parameter expression = "${drawableArtifacts}"
	 */
	private List<DrawableArtifact> drawableArtifacts;

	/**
	 * @parameter expression = "${cleanDrawableLocations} default-value = "false"
	 * @readonly
	 */
	private boolean cleanDrawableLocations;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.cleanDrawableLocations) {

		}
		List<Artifact> artifacts = resolve(this.drawableArtifacts);
		unpackArchive(this.unpackLocation, artifacts);
	}

	/**
	 * Resolves the given list of {@link DrawableArtifact} from both the local and remote repositories.
	 * 
	 * @param drawableArtifacts
	 * @return
	 * @throws MojoFailureException
	 */
	List<Artifact> resolve(final List<DrawableArtifact> drawableArtifacts) throws MojoFailureException {
		ArtifactResolutionRequest request = new ArtifactResolutionRequest();
		request.setLocalRepository(this.localRepository);
		request.setRemoteRepositories(this.remoteRepositories);
		List<Artifact> artifacts = new ArrayList<Artifact>();
		for (DrawableArtifact drawableArtifact : drawableArtifacts) {
			Artifact artifact = this.repositorySystem.createArtifact(drawableArtifact.getGroupId(), drawableArtifact.getArtifactId(), drawableArtifact.getVersion(), "android-drawables");
			request.setArtifact(artifact);
			ArtifactResolutionResult result = this.repositorySystem.resolve(request);
			if (!artifact.isResolved()) {
				handleNotResolved(artifact, result);
			}
			artifacts.add(artifact);
		}
		return artifacts;
	}

	/**
	 * Unpacks the list of artifacts representing 'android-drawable' artifacts to given output location.
	 * 
	 * @param outputLocation
	 * @param artifacts
	 * @throws MojoFailureException
	 */
	void unpackArchive(final File outputLocation, final List<Artifact> artifacts) throws MojoFailureException {
		if (!outputLocation.exists()) {
			boolean success = outputLocation.mkdirs();
			if (!success) {
				throw new MojoFailureException("Unable to create output location " + outputLocation.getAbsolutePath());
			}
		}
		for (Artifact artifact : artifacts) {
			File artifactFile = artifact.getFile();
			// If artifact is resolved using m2e it's possible that the artifact file will actually be the target location of the workspace
			// project.
			if (artifactFile.isFile()) {
				unpackZipFile(outputLocation, artifactFile);
			}
			if (artifactFile.isDirectory()) {
				unpackDirectory(outputLocation, artifactFile);
			}

		}

	}

	/**
	 * @param outputLocation
	 * @param zipFile
	 */
	private void unpackZipFile(final File outputLocation, final File zipFile) throws MojoFailureException {
		try {
			ZipFile archiveFile = new ZipFile(zipFile);
			for (ZipEntry zipEntry : Collections.list(archiveFile.entries())) {
				String entryName = zipEntry.getName();
				if (zipEntry.isDirectory()) {
					continue;
				}
				if (Constants.IMAGE_TYPES.contains(FilenameUtils.getExtension(entryName))) {
					InputStream is = archiveFile.getInputStream(zipEntry);
					File destination = new File(outputLocation, entryName);
					if (destination.exists()) {
						// TODO: Check if source and destination are different!
						getLog().warn("Overwritting " + destination + ", this entry must appear more than once in the 'artifact-drawable' artifacts");
					}
					getLog().debug("Unpacking " + entryName + " -> " + destination);
					FileUtils.copyStreamToFile(new RawInputStreamFacade(is), destination);
				}
				else {
					getLog().debug("Ignoring entry " + entryName);
				}
			}

		}
		catch (IOException e) {
			throw new MojoFailureException("Unable to unpack jar " + zipFile.getAbsolutePath(), e);
		}
	}

	/**
	 * @param outputLocation
	 * @param artifactFile
	 */
	private void unpackDirectory(final File outputLocation, final File directory) throws MojoFailureException {
		getLog().info("unpackingDirectory");
		File resDir = new File(directory, "res");
		for (String dirName : Constants.DRAWABLE_DIRS) {
			File dir = new File(resDir, dirName);
			if (dir.exists()) {
				File target = new File(outputLocation, "res/" + dirName);
				for (File drawable : dir.listFiles()) {
					String drawableName = drawable.getName();
					if (Constants.IMAGE_TYPES.contains(FilenameUtils.getExtension(drawableName))) {
						try {
							FileUtils.copyFile(drawable, new File(target, drawableName));
						}
						catch (IOException e) {
							throw new MojoFailureException("Unable to unpack drawable from workspace resolution " + directory.getAbsolutePath(), e);
						}
					}
				}
			}
		}
	}

	/**
	 * Handles a result when the artifact hasn't been resolved. This is to wrap the separate problems that can occur when resolving an
	 * artifact, missing metadata, version range violations, version circular dependencies, missing artifacts, network/transfer errors, file
	 * system errors: permissions. This method should be factored out once {@link ArtifactResolutionResult} wraps these issues itself.
	 * 
	 * @param result
	 * @throws MojoFailureException
	 */
	void handleNotResolved(final Artifact artifact, final ArtifactResolutionResult result) throws MojoFailureException {
		if (result.hasMissingArtifacts()) {
			throw new MojoFailureException("Unable to resolve " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getType()
					+ ", artifact is missing");
		}
		else if (result.hasExceptions()) {
			throw new MojoFailureException("Unable to resolve " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getType(), result
					.getExceptions().get(0));
		}
		else {
			throw new MojoFailureException("Unable to resolve " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + ":" + artifact.getType()
					+ ", unknown resolution issue");
		}
	}
}
