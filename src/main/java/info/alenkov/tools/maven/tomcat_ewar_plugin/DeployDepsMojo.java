package info.alenkov.tools.maven.tomcat_ewar_plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "deploy-deps",
      requiresDependencyResolution = ResolutionScope.TEST,
      defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class DeployDepsMojo extends AbstractMojo {

	public static final String  PLG_EXEC_CFG_ARGUMENTS  = "arguments";
	public static final Xpp3Dom PLG_EXEC_CFG_EXEC_PLINK = element(name("executable"), "plink").toDom();
	public static final Xpp3Dom PLG_EXEC_CFG_EXEC_PSCP  = element(name("executable"), "pscp").toDom();
	public static final String  PLG_EXEC_GOAL_EXEC      = goal("exec");
	public static final String  PLG_EXEC_PROTOCOL_SSH   = "-ssh";
	public static final String  PLG_EXEC_PROTOCOL_SCP   = "-scp";

	@Component
	protected MavenProject                      mavenProject;
	@Component
	protected MavenSession                      mavenSession;
	@Component
	protected BuildPluginManager                pluginManager;
	protected MojoExecutor.ExecutionEnvironment _pluginEnv;

	protected Plugin _pluginExec;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		_pluginEnv = executionEnvironment(mavenProject, mavenSession, pluginManager);
		_pluginExec = plugin("org.codehaus.mojo", "exec-maven-plugin", "1.2.1");
		copyDependencies();
		tomcatShutdown();
		libSharedClean();
		libSharedUpload();
		tomcatStartup();
	}

	private void copyDependencies() throws MojoExecutionException {
		// TODO expects corrections https://github.com/TimMoore/mojo-executor/issues/18
		Plugin pluginDependency = plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.8");

		final Xpp3Dom cfg = configuration(element(name("useSubDirectoryPerScope"), "true"));
		executeMojo(pluginDependency, goal("copy-dependencies"), cfg, _pluginEnv);
	}

	private void tomcatShutdown() throws MojoExecutionException {
		Xpp3Dom cfg = getBaseConfigExec(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "bin/shutdown.sh").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void tomcatStartup() throws MojoExecutionException {
		Xpp3Dom cfg = getBaseConfigExec(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "bin/startup.sh").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void libSharedClean() throws MojoExecutionException {
		Xpp3Dom cfg = getBaseConfigExec(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "rm").toDom());
		arguments.addChild(element(name("argument"), "-f").toDom());
		arguments.addChild(element(name("argument"), "${tomcat.lib.shared}/*.jar").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void libSharedUpload() throws MojoExecutionException {
		Xpp3Dom cfg = getBaseConfigExec(PLG_EXEC_PROTOCOL_SCP);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${project.build.directory}/dependency/compile/*.jar").toDom());
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}:${tomcat.lib.shared}/").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PSCP);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private Xpp3Dom getBaseConfigExec(String protocol) {
		final Element el0 = element(name("argument"), protocol);
		final Element el1 = element(name("argument"), "-4");
		final Element el2 = element(name("argument"), "-agent");
		final Element el3 = element(name("argument"), "-i");
		final Element el4 = element(name("argument"), "${putty.key}");
		return configuration(element(name(PLG_EXEC_CFG_ARGUMENTS), el0, el1, el2, el3, el4));
	}
}
