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
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

import com.github.kingamajick.admp.maven.util.Asserts;
import com.github.kingamajick.admp.maven.util.Constants;

/**
 * 
 * Copies all files with the extension (<code>'.png'</code>, <code>'.jpg'</code>, <code>'.gif'</code>) from any defined resource directories
 * to <code>'${project.build.outputDirectory}/res'</code>. Only files contained in the following directories will be considered:
 * <ul>
 * <li>ldpi</li>
 * <li>mdpi</li>
 * <li>hdpi</li>
 * <li>xhdpi</li>
 * <li>nodpi</li>
 * <li>tvdpi</li>
 * </ul>
 * Any files contained within sub directories of the above will have their name created from their path. For example
 * <code>/${subDir1}/${subDir2}/${imageFileName}</code> will be named <code>${subDir1}_${subDir2}_${imageFileName}</code>. All names will be
 * in lower case.
 * 
 * @author R King
 * 
 * @goal static-resources
 */
// TODO: Check for bad drawable names
public class ProcessBitmapResourcesMojo extends AbstractMojo {

	/**
	 * @parameter expression="${project.resources}"
	 */
	List<Resource> resources;

	/**
	 * @parameter expression="${project.build.outputDirectory}/res"
	 */
	File targetDir;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.resources.size() == 0) {
			return;
		}
		for (Resource resource : this.resources) {
			File resourcesDir = new File(resource.getDirectory());
			for (String imageResolutionDirName : Constants.STATIC_IMAGE_DIR) {
				File imageResolutionDir = new File(resourcesDir, imageResolutionDirName);
				if (imageResolutionDir.exists() || imageResolutionDir.isDirectory()) {
					try {
						copyImageResources(new File(this.targetDir, Constants.DRAWABLE_PREFIX + imageResolutionDirName), imageResolutionDir);
					}
					catch (IOException e) {
						throw new MojoFailureException("Unable to copy static resources", e);
					}
				}
				else {
					getLog().info("No image resources defined for " + imageResolutionDirName);
					continue;
				}
			}
		}
	}

	/**
	 * Copies all images resources ({@link Constants#IMAGE_TYPES}) found in the list of files (and its sub directories) to the output directory. All
	 * images will be given lower case names and any images contained in sub directories will be named as follows:
	 * ${subDir1}_${subDir2}_${imageFileName}, these steps guarantee compatibility with android resource file names restrictions.
	 * 
	 * @param outputDir
	 * @param rootDirectory
	 * @throws IOException
	 */
	void copyImageResources(final File outputDir, final File rootDirectory) throws IOException {
		Asserts.notNull(outputDir, "outputDir");
		Asserts.notNull(rootDirectory, "files");

		copyImageResources(outputDir, "", rootDirectory.listFiles());
	}

	/**
	 * Copies the image resources ({@link Constants#IMAGE_TYPES}) to the output directory prefixing the file name with the file name prefix argument.
	 * The file name will also be in lower case, these steps guarantee compatibility with android resource file names restrictions. If a sub
	 * directory is discovered in the list of files, this method will be recursively called with the file name prefix of the directory name
	 * postfixed with a '_'.
	 * 
	 * @param outputDir
	 *            The location to copy the files to.
	 * @param fileNamePrefix
	 *            The prefix to append to the output file name.
	 * @param files
	 *            The list of files to copy.
	 * @throws IOException
	 */
	void copyImageResources(final File outputDir, final String fileNamePrefix, final File[] files) throws IOException {
		Asserts.notNull(fileNamePrefix, "fileNamePrefix");

		for (File file : files) {
			if (file.isFile() && isImageFile(file)) {
				File destination = new File(outputDir, fileNamePrefix.toLowerCase() + file.getName().toLowerCase());
				getLog().debug("Copying " + file + " -> " + destination);
				FileUtils.copyFile(file, destination);
			}
			else {
				copyImageResources(outputDir, file.getName() + "_", file.listFiles());
			}
		}
	}

	/**
	 * Checks if the file ends with an extension specified in {@link Constants#IMAGE_TYPES}.
	 * 
	 * @param fileName
	 * @return
	 */
	boolean isImageFile(final File file) {
		Asserts.notNull(file, "file");

		String fileName = file.getName().toLowerCase();
		for (String imageType : Constants.IMAGE_TYPES) {
			if (fileName.endsWith(imageType)) {
				return true;
			}
		}
		return false;
	}

}
