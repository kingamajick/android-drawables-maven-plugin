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

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.w3c.dom.Document;

import com.github.kingamajick.admp.maven.beans.Density;
import com.github.kingamajick.admp.maven.transcoder.TranscoderFactory;
import com.github.kingamajick.admp.maven.transcoder.TranscoderFactoryException;
import com.github.kingamajick.admp.maven.util.Asserts;
import com.github.kingamajick.admp.maven.util.Constants;

/**
 * Rasterizes any SVGs contained <code>${svgDirectory}</code> (default: <code>'src/main/svg'</code>) to
 * <code>'${project.build.outputDirectory}/res'</code> for any densities specified.
 * <ul>
 * <li>ldpi (default)</li>
 * <li>mdpi (default)</li>
 * <li>hdpi (default)</li>
 * <li>xhdpi (default)</li>
 * <li>nodpi</li>
 * <li>tvdpi</li>
 * </ul>
 * 
 * @author R King
 * 
 * @goal rasterize
 */
public class RasterizeSVGMojo extends AbstractMojo {

	private UserAgent userAgent;
	private DocumentLoader loader;
	private BridgeContext context;
	private GVTBuilder builder;
	private SAXSVGDocumentFactory svgDocFactory;
	private TranscoderFactory transcoderFactory;

	/**
	 * The directory containing the SVG resource to be rasterized.
	 * 
	 * @parameter expression="${svgDirectory}" default-value = "src/main/svg"
	 */
	File svgDirectory;

	/**
	 * @parameter expression="${project.build.outputDirectory}/res"
	 * @readonly
	 */
	File targetDir;

	/**
	 * A list of {@link Density}s, if none are specified the following configuration is used:
	 * <ul>
	 * <li>name : 'drawable-ldpi', scale-factor : 0.75f</li>
	 * <li>name : 'drawable-mdpi', scale-factor : 1.00f</li>
	 * <li>name : 'drawable-hdpi', scale-factor : 1.50f</li>
	 * <li>name : 'drawable-xhdpi', scale-factor : 2.00f</li>
	 * </ul>
	 * 
	 * @parameter expression="${densities}"
	 */
	List<Density> densities;

	/**
	 * The type to raterized the SVGs to. The available formats are <code>png</code> and <code>jpg</code>
	 * 
	 * @parameter expression="${rasterizedType}" default-value = "png"
	 */
	String rasterizedType;

	/**
	 * Create a directory with the specified name, and all the necessary parent directories to do that.
	 * 
	 * @param root
	 * @param newDirName
	 * @return
	 * @throws MojoFailureException
	 *             if it was not possible to create the directory.
	 */
	static File createDirectory(final File root, final String newDirName) throws MojoFailureException {
		File dir = new File(root, newDirName);
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
			if (!success) {
				throw new MojoFailureException("Unable to make directory " + dir.getAbsolutePath());
			}
		}
		return dir;
	}

	public RasterizeSVGMojo() {
		this.userAgent = new UserAgentAdapter();
		this.loader = new DocumentLoader(this.userAgent);
		this.context = new BridgeContext(this.userAgent, this.loader);
		this.context.setDynamicState(BridgeContext.DYNAMIC);
		this.builder = new GVTBuilder();
		this.svgDocFactory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
		this.transcoderFactory = new TranscoderFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.densities.size() == 0) {
			Density.defaults(this.densities);
		}
		ImageTranscoder transcoder = null;
		try {
			transcoder = this.transcoderFactory.create(this.rasterizedType);
		}
		catch (TranscoderFactoryException e) {
			throw new MojoExecutionException("Unable to create transcoder", e);
		}
		Map<String, File> svgsToProcess = getSVGsToProcess(this.svgDirectory);
		for (Entry<String, File> svgToProcess : svgsToProcess.entrySet()) {
			try {
				String inputURI = svgToProcess.getValue().toURI().toString();
				Document svgDoc = this.svgDocFactory.createDocument(inputURI);
				GraphicsNode rootGN = this.builder.build(this.context, svgDoc);
				Rectangle2D bounds = rootGN.getBounds();

				for (Density density : this.densities) {
					getLog().debug(
							"Rasterizing " + svgToProcess.getValue() + " -> " + density.getName() + "/" + svgToProcess.getKey() + "." + this.rasterizedType + " [" + density.getScaleFactor() + "]");

					File outputDir = createDirectory(this.targetDir, density.getName());
					File outputFile = new File(outputDir, svgToProcess.getKey() + "." + this.rasterizedType);
					outputFile.createNewFile();
					OutputStream os = new FileOutputStream(outputFile);

					TranscoderInput input = new TranscoderInput(svgDoc);
					TranscoderOutput output = new TranscoderOutput(os);

					transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(Math.ceil(density.getScaleFactor() * bounds.getWidth())));
					transcoder.transcode(input, output);
				}

			}
			catch (IOException e) {
				throw new MojoFailureException("Unable to rasterize " + svgToProcess.getValue().getAbsolutePath(), e);
			}

			catch (TranscoderException e) {
				throw new MojoFailureException("Unable to rasterize " + svgToProcess.getValue().getAbsolutePath(), e);
			}
		}
	}

	/**
	 * Returns a map of all SVGs contain in the rootDirectory and its sub directories, the map is keyed by the output name for that file
	 * when rasterized and contains the input file as the value. The output names will be in lower case, and in the case where the file is
	 * contained in a sub directory, the output file name will be prefixed with the directory path separated by '_', i.e
	 * ${subDir1}_${subDir2}_${imageFileName}.
	 * 
	 * @param rootDirectory
	 * @return
	 */
	Map<String, File> getSVGsToProcess(final File rootDirectory) {
		Asserts.notNull(rootDirectory, "rootDirectory");
		Map<String, File> fileMappings = new HashMap<String, File>();
		getSVGsToProcess(fileMappings, rootDirectory.listFiles(), "");
		return fileMappings;
	}

	/**
	 * Processes the file list for potential SVG files. If a file is found it is added to the map with a key of the file name prefixed with
	 * the file name prefix. If a potential SVG file is found to be a directory, it will call this method recursively to process the
	 * potential SVGs in this folder, with the fileNamePrefix appended by the directory name and a '_'
	 * 
	 * @param fileMappings
	 * @param potentialSVGs
	 * @param fileNamePrefix
	 */
	void getSVGsToProcess(final Map<String, File> fileMappings, final File[] potentialSVGs, final String fileNamePrefix) {
		for (File potentialSVG : potentialSVGs) {
			String potentialSVGName = fileNamePrefix + potentialSVG.getName();
			if (potentialSVG.isFile() && potentialSVG.getName().toLowerCase().endsWith(Constants.SVG_FILE_TYPE)) {
				// Output file name sans the extension
				String outputFileName = potentialSVGName.substring(0, potentialSVGName.length() - Constants.SVG_FILE_TYPE.length());
				getLog().debug("Mapping " + potentialSVG + " to " + outputFileName);
				fileMappings.put(outputFileName, potentialSVG);
			}
			else if (potentialSVG.isDirectory()) {
				getSVGsToProcess(fileMappings, potentialSVG.listFiles(), potentialSVGName + "_");
			}
			else {
				getLog().debug("Ignoring resource " + potentialSVG);
			}
		}
	}

}
