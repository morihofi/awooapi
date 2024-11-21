package net.fuxle.awooapi;

public class RuntimeConfiguration {
    private boolean debugEnabled = false;
    private String searchPackagePrefix = ".";
    private ClassLoader searchClassloader = RuntimeConfiguration.class.getClassLoader();
    private String apiPrefix = "/api";

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public String getSearchPackagePrefix() {
        return searchPackagePrefix;
    }

    public void setSearchPackagePrefix(String searchPackagePrefix) {
        this.searchPackagePrefix = searchPackagePrefix;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public ClassLoader getSearchClassloader() {
        return searchClassloader;
    }

    public void setSearchClassloader(ClassLoader searchClassloader) {
        this.searchClassloader = searchClassloader;
    }
}
