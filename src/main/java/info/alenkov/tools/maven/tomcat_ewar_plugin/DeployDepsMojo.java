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
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

@Mojo(name = "deploy-deps", defaultPhase = LifecyclePhase.DEPLOY)
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

		collectDependencies();
	}

	private void collectDependencies() throws MojoExecutionException {
		Plugin pluginDependency = plugin("org.apache.maven.plugins", "maven-dependency-plugin");

		Xpp3Dom cfg = new Xpp3Dom("configuration");
		Xpp3Dom entry0 = new Xpp3Dom("useSubDirectoryPerScope");
		entry0.setValue("true");
		cfg.addChild(entry0);

		executeMojo(pluginDependency, "copy-dependencies", cfg, _pluginEnv);
	}
}
