# Android-Drawables-Maven-Plugin

The **android-drawables-maven-plugin** was created to allow the generation of Android drawable artifacts from a SVG source.  After originally using the [Batik Maven Plugin](http://mojo.codehaus.org/batik-maven-plugin/) to perform this task, I found the amount of configuration grew rapidly so I created the android-drawables-maven-plugin to make the process simpler.

## Getting Started

### Prerequisities

Maven 3.0.3+ installed, see http://maven.apache.org/download.html

### Snapshots

**Currently only version supported**

Snapshots of the android-drawables-maven-plugin are available from the Sonatype OSS Repository Hosting Service.  To use snapshots, add the following configuration to the pom.

```
<repositories>
  <repository>
    <id>oss-sonatype</id>
    <name>oss-sonatype</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

### Creating a android-drawables artifact

1. Create a new maven project with a packaging type of android-drawables and add the android-drawables-maven-plugin to its build configuration.

    ```
...
<packaging>android-drawables</packaging>  
...  
<build>
  <plugins>
    <plugin>
      <groupId>com.github.kingamajick.admp</groupId>
      <artifactId>android-drawables-maven-plugin</artifactId>
      <version>0.0.1-SNAPSHOT</version>
      <extensions>true</extensions>
      <configuration>
        <rasterizedType>[png|jpg]</rasterizedType>
      </configuration>
    </plugin>
  </plugins>
</build>
```

2. Create the following directory structure to store the drawable resources  

    ```
src/main/svg/          <= Contains any SVG resources to be rasterized
src/main/resources/    <= Contains any static resources
    |-- ldpi
    |-- mdpi
    |-- hdpi
    |-- xhdpi
    |-- nodpi
    `-- tvdpi
```
The directories may contain sub folders (both for SVG resources and static resources).  If this is the case, the resultant name of the file packaged in the will be generated from the directory structure.  Given ```dir1\dir2\image.svg```, the resulting image file name will be ```dir1_dir2_image.png``` (assuming ```png``` is the ```rasterizedType```).

3. By default the SVG resources are rasterized at the following dimensions.
<table>
  <tr>
    <th>Output</th><th>Scale Factor (relative to the SVG image dimensions)</th>
  </tr>
  <tr>
    <td>ldpi</td><td>0.75</td>
  </tr>
  <tr>
    <td>mdpi</td><td>1.00</td>
  </tr>
  <tr>
    <td>hdpi</td><td>1.50</td>
  </tr>
  <tr>
    <td>xhdpi</td><td>2.00</td>
  </tr>
</table>
If this is not suitable, custom densities can be supplied via the plugin configuration using the following form:

    ```
<configuration>
  ...
  <densities>
    <density>
      <name>[ldpi|mdpi|hdpi|xhdpi|nodpi|tvdpi]</name>
      <scaleFactor></scaleFactor>
    <density>
  </densities>
</configuration>
```
Note, by doing this all densities will have to be specified as the defaults will no longer be generated.

4. Your project is now ready to be built.

### Consuming a android-drawables artifact

1. In the project to consume a ```android-drawables``` artifact, configure the POM as follows:

    ```
<plugin>
  <groupId>com.github.kingamajick.admp</groupId>
  <artifactId>android-drawables-maven-plugin</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <extensions>true</extensions>
    <executions>
      <execution>
        <id>Unpack Drawables</id>
        <phase>initialize</phase>
        <goals>
          <goal>unpack</goal>
        </goals>
        <configuration>
          <drawableArtifacts>
            <drawableArtifact>
              <groupId>${drawableArtifactGroupId}</groupId>
              <artifactId>${drawableArtifactArtifactId}</artifactId>
              <version>${drawableArtifactVersion}</version>
            </drawableArtifact>
          </drawableArtifacts>
        <unpackLocation></unpackLocation>
      </configuration>
    </execution>
  </executions>
</plugin>
```
This will unpack the specified artifact(s) during the ```initialize``` phase.

### m2e Connector

A m2e connector is available for this plugin at https://github.com/kingamajick/android-drawables-maven-plugin-m2e

### Maven Site

The maven site for this project can be found at http://kingamajick.github.com/android-drawables-maven-plugin/






