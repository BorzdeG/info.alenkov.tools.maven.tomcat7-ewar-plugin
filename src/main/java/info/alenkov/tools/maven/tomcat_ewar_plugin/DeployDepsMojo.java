package info.alenkov.tools.maven.tomcat_ewar_plugin;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "deploy-deps",
      requiresDependencyResolution = ResolutionScope.TEST,
      defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class DeployDepsMojo extends AbstractDeploy {
	public static final String FILE_MASK = "*.jar";

	@Parameter(required = false)
	public String excludeGroupIds;
	@Parameter(defaultValue = "compile")
	public String dependencyScope;
	@Parameter(defaultValue = "${project.build.directory}/dependency")
	public String dependencyDirectory;

	@Parameter(defaultValue = "./bin/shutdown.sh", property = "tomcat.script.shutdown")
	public String tomcatScriptShutdown;
	@Parameter(defaultValue = "./bin/startup.sh", property = "tomcat.script.startup")
	public String tomcatScriptStartup;
	@Parameter(defaultValue = "./shared", property = "tomcat.dir.shared")
	public String tomcatDirShared;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		checkDirPath(tomcatDirShared);

		super.execute();
		copyDependencies();
		tomcatShutdown();
		cleanDirSharedLibrary();
		uploadDirSharedLibrary();
		tomcatStartup();
	}

	private void copyDependencies() throws MojoExecutionException {
		// TODO expects corrections https://github.com/TimMoore/mojo-executor/issues/18
		Plugin pluginDependency = plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.8");

		final Xpp3Dom cfg = configuration(element(name("useSubDirectoryPerScope"), "true"));
		if (null != excludeGroupIds && !excludeGroupIds.trim().isEmpty()) {
			cfg.addChild(element(name("excludeGroupIds"), excludeGroupIds).toDom());
		}
		executeMojo(pluginDependency, goal("copy-dependencies"), cfg, _pluginEnv);
	}

	private void tomcatShutdown() throws MojoExecutionException {
		Xpp3Dom cfg = getWagonConfig(getSshCommands(tomcatScriptShutdown), getWagonUrl());

		executeMojo(_pluginWagon, WAGON_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void tomcatStartup() throws MojoExecutionException {
		Xpp3Dom cfg = getWagonConfig(getSshCommands(tomcatScriptStartup), getWagonUrl());

		executeMojo(_pluginWagon, WAGON_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void cleanDirSharedLibrary() throws MojoExecutionException {
		runWagonCommandCleanDir(tomcatDirShared, FILE_MASK);
	}

	private void uploadDirSharedLibrary() throws MojoExecutionException {
		final Element elToDir = element(name("toDir"), tomcatDirShared);
		final Element elFromDir = element(name("fromDir"), dependencyDirectory + "/" + dependencyScope);
		final Element elIncludes = element(name("includes"), FILE_MASK);
		Xpp3Dom cfg = getWagonConfig(elIncludes, elFromDir, elToDir, getWagonUrl());

		executeMojo(_pluginWagon, WAGON_GOAL_UPLOAD, cfg, _pluginEnv);
	}

}
