package com.tamirtw.main;

// Command line imports
import java.io.File;
import java.io.IOException;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.browsermob.proxy.jetty.http.HttpContext;
import org.browsermob.proxy.jetty.http.HttpException;
import org.browsermob.proxy.jetty.http.HttpRequest;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import org.browsermob.proxy.ProxyServer;

public class MainApp {
	private final String appVersion = "v0.1";
	private static MainApp app = null;
	// Command line options
	protected MainAppCLI cliOptions = null;
	// Config
	final private String appConfigDefaultsFile = "default.config";
	protected PropertiesConfiguration appConfig = null;

	/**
	 * @param args
	 *            The options available are: [--config] : use config file other
	 *            than the default.config [--help] : show this help and exit
	 *            [--port] : Listen on port # (default is 80) [--verbose -v] :
	 *            explain what is being done [--version] : output version
	 *            information and exit
	 */
	public static void main(String[] args) {
		Cli<MainAppCLI> cliApp = CliFactory.createCli(MainAppCLI.class);
		app = new MainApp();
		try {
			app.cliOptions = cliApp.parseArguments(args);
		} catch (ArgumentValidationException e) {
			System.out.println(e.getValidationErrors());
		}
		// Parse runtime options
		if (null == app.cliOptions || app.cliOptions.getHelp())
			System.exit(0);
		app.run();
	}

	private void showVersion() {
		System.out.println("WAF XSS Proxy " + this.appVersion
				+ " written by Tamir Twina");
		System.exit(0);
	}

	protected MainApp config(MainAppCLI options) {
		this.defaultConfig();
		return this;
	}

	protected void defaultConfig() {
		try {
			this.appConfig = new PropertiesConfiguration(
					this.appConfigDefaultsFile);
		} catch (ConfigurationException e) {
			System.out.println("Error: Can not read config file");
			System.exit(0);
		}
	}

	protected void loadConfigurationFile(boolean isConfig) {
		if (isConfig) {
			app.userConfig();
		} else {
			app.defaultConfig();
		}
	}

	protected void userConfig() {
		try {
			File conf = new File(cliOptions.getConfig());
			if (!(conf.exists())) {
				System.out.println("Error: Config file not found");
				System.exit(0);
			}
			this.appConfig = new PropertiesConfiguration(cliOptions.getConfig());
		} catch (ConfigurationException e) {
			System.out.println("Error: Can not read config file");
			System.exit(0);
		}
	}

	public static MainAppCLI cliOptions() {
		return app.cliOptions;
	}

	public PropertiesConfiguration config() {
		return app.appConfig;
	}

	protected int getPortConfig() {
		int port = 8082;
		if (app.cliOptions.isPort()) {
			port = app.cliOptions.getPort();
		} else {
			try {
				port = this.config().getInt("proxy.port");
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
		return port;
	}

	public void run() {
		// Show version and exit
		if (app.cliOptions.isVersion()) {
			app.showVersion();
			System.exit(0);
		}
		// Load config file default or user
		this.loadConfigurationFile(cliOptions.isConfig());

		// Init proxy server
		int proxyPort = this.getPortConfig();
//		System.setProperty("http.proxyHost", "localhost");
//		System.setProperty("http.proxyPort",Integer.toString(proxyPort));

        ProxyServer server = new ProxyServer(9090);
        try {
            server.start();
            server.setCaptureContent(true);
            server.setCaptureHeaders(true);
            server.setPort(proxyPort);
            server.addRequestInterceptor(new HttpRequestInterceptor() {
                @Override
                public void process(org.apache.http.HttpRequest httpRequest,
                                    org.apache.http.protocol.HttpContext httpContext)
                        throws org.apache.http.HttpException, IOException {
                    httpRequest.removeHeaders("User-Agent");
                    httpRequest.addHeader("User-Agent", "Bananabot/1.0");
                }
            });
            server.addResponseInterceptor(new HttpResponseInterceptor() {
                @Override
                public void process(HttpResponse httpResponse, org.apache.http.protocol.HttpContext httpContext) throws org.apache.http.HttpException, IOException {
                    System.out.println(httpResponse.getEntity());
                }
            });
        } catch (Exception e) {
          e.printStackTrace();
        }


    }

}