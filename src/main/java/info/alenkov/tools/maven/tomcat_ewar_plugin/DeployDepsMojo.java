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

	@Component
	protected MavenProject                      mavenProject;
	@Component
	protected MavenSession                      mavenSession;
	@Component
	protected BuildPluginManager                pluginManager;
	protected MojoExecutor.ExecutionEnvironment _pluginEnv;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		_pluginEnv = executionEnvironment(mavenProject, mavenSession, pluginManager);
		copyDependencies();
	}

	private void copyDependencies() throws MojoExecutionException {
		// TODO expects corrections https://github.com/TimMoore/mojo-executor/issues/18
		Plugin pluginDependency = plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.8");

		final Xpp3Dom cfg = configuration(element(name("useSubDirectoryPerScope"), "true"));

		executeMojo(pluginDependency, goal("copy-dependencies"), cfg, _pluginEnv);
	}
}
