package info.alenkov.tools.maven.tomcat_ewar_plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

@Mojo(name = "deploy-static", defaultPhase = LifecyclePhase.PACKAGE)
public class DeployStaticMojo extends AbstractDeploy {
	private static final String PACKAGING_TYPE = "war";

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
		Xpp3Dom cfg = getPluginExecBaseConfig(PLG_EXEC_PROTOCOL_SSH);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}").toDom());
		arguments.addChild(element(name("argument"), "rm").toDom());
		arguments.addChild(element(name("argument"), "-rf").toDom());
		arguments.addChild(element(name("argument"), "${tomcat.files.static}/*").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PLINK);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

	private void uploadDirStaticFiles() throws MojoExecutionException {
		Xpp3Dom cfg = getPluginExecBaseConfig(PLG_EXEC_PROTOCOL_SCP);
		final Xpp3Dom arguments = cfg.getChild(PLG_EXEC_CFG_ARGUMENTS);
		arguments.addChild(element(name("argument"), "-r").toDom());
		arguments.addChild(element(name("argument"), "${project.build.directory}/${project.build.finalName}/static/*")
			                   .toDom());
		arguments.addChild(element(name("argument"), "${ssh.user}@${ssh.host}:${tomcat.files.static}/").toDom());
		cfg.addChild(PLG_EXEC_CFG_EXEC_PSCP);

		executeMojo(_pluginExec, PLG_EXEC_GOAL_EXEC, cfg, _pluginEnv);
	}

}
