package info.alenkov.tools.maven.tomcat_ewar_plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

abstract public class AbstractDeploy extends AbstractMojo {
	protected static final String  PLG_EXEC_PROTOCOL_SCP   = "-scp";
	protected static final String  PLG_EXEC_PROTOCOL_SSH   = "-ssh";
	protected static final String  PLG_EXEC_GOAL_EXEC      = goal("exec");
	protected static final Xpp3Dom PLG_EXEC_CFG_EXEC_PSCP  = element(name("executable"), "pscp").toDom();
	protected static final Xpp3Dom PLG_EXEC_CFG_EXEC_PLINK = element(name("executable"), "plink").toDom();
	protected static final String  PLG_EXEC_CFG_ARGUMENTS  = "arguments";

	@Component
	protected MavenProject       mavenProject;
	@Component
	protected MavenSession       mavenSession;
	@Component
	protected BuildPluginManager pluginManager;

	protected MojoExecutor.ExecutionEnvironment _pluginEnv;
	protected Plugin                            _pluginExec;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		_pluginEnv = executionEnvironment(mavenProject, mavenSession, pluginManager);
		_pluginExec = plugin("org.codehaus.mojo", "exec-maven-plugin", "1.2.1");
	}

	protected Xpp3Dom getPluginExecBaseConfig(String protocol) {
		final Element el0 = element(name("argument"), protocol);
		final Element el1 = element(name("argument"), "-4");
		final Element el2 = element(name("argument"), "-agent");
		final Element el3 = element(name("argument"), "-i");
		final Element el4 = element(name("argument"), "${putty.key}");
		return configuration(element(name(PLG_EXEC_CFG_ARGUMENTS), el0, el1, el2, el3, el4));
	}

}
