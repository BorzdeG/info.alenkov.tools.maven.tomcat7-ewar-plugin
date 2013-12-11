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

	@Parameter(required = false)
	public String excludeGroupIds;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
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
		Xpp3Dom cfg = getPluginExecBaseConfig(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "bin/shutdown.sh").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void tomcatStartup() throws MojoExecutionException {
		Xpp3Dom cfg = getPluginExecBaseConfig(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "bin/startup.sh").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void cleanDirSharedLibrary() throws MojoExecutionException {
		Xpp3Dom cfg = getPluginExecBaseConfig(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "rm").toDom());
		arguments.addChild(element(name("argument"), "-f").toDom());
		arguments.addChild(element(name("argument"), "${tomcat.lib.shared}/*.jar").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void uploadDirSharedLibrary() throws MojoExecutionException {
		Xpp3Dom cfg = getPluginExecBaseConfig(PLG_EXEC_PROTOCOL_SCP);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${project.build.directory}/dependency/compile/*.jar").toDom());
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}:${tomcat.lib.shared}/").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PSCP);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

}
