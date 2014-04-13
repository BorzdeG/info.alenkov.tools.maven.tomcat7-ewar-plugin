package info.alenkov.tools.maven.tomcat_ewar_plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

abstract public class AbstractDeploy extends AbstractMojo {
	public static final String WAGON_GOAL_UPLOAD = "upload";
	public static final String WAGON_GOAL_EXEC   = "sshexec";

	@Component
	protected MavenProject       mavenProject;
	@Component
	protected MavenSession       mavenSession;
	@Component
	protected BuildPluginManager pluginManager;

	protected MojoExecutor.ExecutionEnvironment _pluginEnv;
	protected Plugin                            _pluginWagon;

	@Parameter(property = "ssh.serverId")
	public String sshServerId;
	@Parameter(property = "ssh.host")
	public String sshHost;
	@Parameter(defaultValue = "~", property = "ssh.home")
	public String sshHome;

	@Parameter(property = "maven.plugin.wagon.version", defaultValue = "1.0-beta-5")
	public String versionPluginWagon;

	protected Server sshServer;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		_pluginEnv = executionEnvironment(mavenProject, mavenSession, pluginManager);

		_pluginWagon = plugin("org.codehaus.mojo", "wagon-maven-plugin", versionPluginWagon);

		sshServer = mavenSession.getSettings().getServer(sshServerId);
		assert sshServer != null;
	}

	protected Element getSshCommands(String... commands) {
		assert commands.length > 0;

		List<Element> list = new ArrayList<Element>();
		for (String command : commands) {
			list.add(element(name("command"), command));
		}
		return element(name("commands"), list.toArray(new Element[list.size()]));
	}

	protected Element getWagonUrl() {
		assert sshHome != null && sshHome.isEmpty();

		return element(name("url"), "scp://" + sshServer.getUsername() + "@" + sshHost + sshHome);
	}

	protected Xpp3Dom getWagonConfig(Element... elements) {
		assert elements.length > 0;

		List<Element> list = new ArrayList<Element>(Arrays.asList(elements));
		list.add(element(name("serverId"), sshServerId));
		return configuration(list.toArray(new Element[list.size()]));
	}

	protected void runWagonCommandCleanDir(String dir, String mask) throws MojoExecutionException {
		checkDirPath(dir);

		final String cmd = "rm -Rf " + dir + "/" + mask;
		final Element elCommands = getSshCommands(cmd);
		Xpp3Dom cfg = getWagonConfig(elCommands, getWagonUrl());

		executeMojo(_pluginWagon, WAGON_GOAL_EXEC, cfg, _pluginEnv);
	}

	protected void checkDirPath(String path) {
		assert !path.equals("/");
		assert !path.contains("../");
		assert path.startsWith("./") || (!path.startsWith("~/") && !path.startsWith("/"));
	}
}
