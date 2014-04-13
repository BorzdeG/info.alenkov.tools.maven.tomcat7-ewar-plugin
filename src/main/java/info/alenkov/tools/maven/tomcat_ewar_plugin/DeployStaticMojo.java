package info.alenkov.tools.maven.tomcat_ewar_plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "deploy-static", defaultPhase = LifecyclePhase.PACKAGE)
public class DeployStaticMojo extends AbstractDeploy {
	private static final String PACKAGING_TYPE = "war";
	public static final  String FILE_MASK      = "*";
	public static final  String INCLUDE_MASK   = "**";

	@Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}/static")
	public String staticDirectory;
	@Parameter(defaultValue = "./static_files", property = "tomcat.dir.static")
	public String tomcatDirStatic;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
		if (!PACKAGING_TYPE.equalsIgnoreCase(mavenProject.getPackaging())) {
			throw new MojoExecutionException("packaging type not " + PACKAGING_TYPE);
		}
		cleanDirStaticFiles();
		uploadDirStaticFiles();
	}

	private void cleanDirStaticFiles() throws MojoExecutionException {
		runWagonCommandCleanDir(tomcatDirStatic, FILE_MASK);
	}

	private void uploadDirStaticFiles() throws MojoExecutionException {
		final Element elToDir = element(name("toDir"), tomcatDirStatic);
		final Element elFromDir = element(name("fromDir"), staticDirectory);
		final Element elIncludes = element(name("includes"), INCLUDE_MASK);
		Xpp3Dom cfg = getWagonConfig(elFromDir, elToDir, elIncludes, getWagonUrl());

		executeMojo(_pluginWagon, WAGON_GOAL_UPLOAD, cfg, _pluginEnv);
	}


}
