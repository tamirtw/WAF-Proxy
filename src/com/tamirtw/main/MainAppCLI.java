package com.tamirtw.main;





import uk.co.flamingpenguin.jewel.cli.Option;

public interface MainAppCLI {
	@Option(shortName = "p",description = "Listen on port # (default is 80)")
	boolean isPort();
	int getPort();
	
	@Option(description = "use config file other than the default.config")
	boolean isConfig();
	String getConfig();
	
	@Option(shortName = "v", description = "explain what is being done")
	boolean isVerbose();

	@Option(helpRequest = true,description = "show this help and exit")
	boolean getHelp();
	
	@Option(description = "output version information and exit")
	boolean isVersion();
}
