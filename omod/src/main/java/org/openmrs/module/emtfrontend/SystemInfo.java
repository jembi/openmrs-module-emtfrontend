package org.openmrs.module.emtfrontend;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SystemInfo {
	private Properties properties = System.getProperties();
	private String openmrsSpecificInfo;//oVersion:openmrsVersion;oModulesFolderPath:/path/
	public String operatingSystem = properties.getProperty("os.name");
	public String operatingSystemArch = properties.getProperty("os.arch");
	public String operatingSystemVersion = properties.getProperty("os.version");
	public String javaVersion = properties.getProperty("java.version");
	public String javaVendor = properties.getProperty("java.vendor");
	public String jvmVersion = properties.getProperty("java.vm.version");
	public String jvmVendor = properties.getProperty("java.vm.vendor");
	public String javaRuntimeName = properties.getProperty("java.runtime.name");
	public String javaRuntimeVersion = properties.getProperty("java.runtime.version");
	public String userName = properties.getProperty("user.name");
	public String systemLanguage = properties.getProperty("user.language");
	public String systemTimezone = properties.getProperty("user.timezone");
	public String fileSystemEncoding = properties.getProperty("sun.jnu.encoding");
	public String userDirectory = properties.getProperty("user.dir");
	public String tempDirectory = properties.getProperty("java.io.tmpdir");
	private String openMRSVersion;
	private List<List<String>> loadedModules;
	private String oModulesFolderPath;
	private String loadedModulesString;

	public SystemInfo(String openmrsSpecificInfo) {
		setOpenmrsSpecificInfo(openmrsSpecificInfo);
		intialiseOpenMRSSpecificInformation();
	}

	public String getOpenmrsSpecificInfo() {
		return openmrsSpecificInfo;
	}

	public void setOpenmrsSpecificInfo(String openmrsSpecificInfo) {
		this.openmrsSpecificInfo = openmrsSpecificInfo;
	}
	
	private void intialiseOpenMRSSpecificInformation() {
		if(getOpenmrsSpecificInfo() != null && getOpenmrsSpecificInfo().split(";").length == 2) {
			setOpenMRSVersion(getOpenmrsSpecificInfo().split(";")[0] != null && getOpenmrsSpecificInfo().split(";")[0].startsWith("oVersion:") ? getOpenmrsSpecificInfo().split(";")[0].replace("oVersion:", "") : "1.6.7");
			setModulesFolderPath(getOpenmrsSpecificInfo().split(";")[1] != null && getOpenmrsSpecificInfo().split(";")[1].startsWith("oModulesFolderPath:") ? getOpenmrsSpecificInfo().split(";")[1].replace("oModulesFolderPath:", "") : "1.6.7");
			setModuleObjects();
		}
	}

	public String getOpenMRSVersion() {
		return openMRSVersion;
	}

	public void setOpenMRSVersion(String openMRSVersion) {
		this.openMRSVersion = openMRSVersion;
	}

	public List<List<String>> getLoadedModules() {
		if(loadedModules == null) {
			this.loadedModules = new ArrayList<List<String>>();
		}
		return loadedModules;
	}

	public void setLoadedModules(List<List<String>> loadedModules) {
		this.loadedModules = loadedModules;
	}
	
	public void addLoadedModule(List<String> moduleNameAndVersion) {
		getLoadedModules().add(moduleNameAndVersion);
	}

	public String getLoadedModulesString() {
		if(loadedModulesString == null) {
			this.loadedModulesString = "";
		}
		return loadedModulesString;
	}

	public void setLoadedModulesString(String loadedModulesString) {
		this.loadedModulesString = loadedModulesString;
	}

	public String getModulesFolderPath() {
		return oModulesFolderPath;
	}

	public void setModulesFolderPath(String oModulesFolderPath) {
		this.oModulesFolderPath = oModulesFolderPath;
	}
	
	private void setModuleObjects() {
		File modulesDirectory = new File(getModulesFolderPath());
		
		if(modulesDirectory.exists() && modulesDirectory.isDirectory()) {
			File[] modules = modulesDirectory.listFiles();
			
			for(int i = 0; i < modules.length; i++) {
				if(modules[i].getName().endsWith(".omod")) {
					String moduleId = modules[i].getName().replace(".omod", "").split("", 2)[0];
					String moduleVersion = modules[i].getName().replace(".omod", "").split("", 2)[1];
					ArrayList<String> module = new ArrayList<String>();
					
					module.add(moduleId);
					module.add(moduleVersion);
					getLoadedModules().add(module);
					setLoadedModulesString(getLoadedModulesString().equals("") ? getLoadedModulesString() + modules[i].getName().replace(".omod", "") : getLoadedModulesString() + "," + modules[i].getName().replace(".omod", ""));
				}
			}
		}
	}
}
