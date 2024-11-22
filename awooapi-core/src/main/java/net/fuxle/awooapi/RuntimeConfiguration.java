package net.fuxle.awooapi;

public class RuntimeConfiguration {

    // Debug-related settings
    public static class DebugConfig {
        private boolean debugEnabled = false;

        public boolean isDebugEnabled() {
            return debugEnabled;
        }

        public void setDebugEnabled(boolean debugEnabled) {
            this.debugEnabled = debugEnabled;
        }
    }

    // Search-related settings
    public static class SearchConfig {
        private String packagePrefix = ".";
        private ClassLoader classLoader = RuntimeConfiguration.class.getClassLoader();

        public String getPackagePrefix() {
            return packagePrefix;
        }

        public void setPackagePrefix(String packagePrefix) {
            this.packagePrefix = packagePrefix;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public void setClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
    }

    // API-related settings
    public static class ApiConfig {
        private String apiPrefix = "/api";
        private boolean graphQLEnabled = false;
        private boolean restEnabled = false;

        public String getApiPrefix() {
            return apiPrefix;
        }

        public void setApiPrefix(String apiPrefix) {
            this.apiPrefix = apiPrefix;
        }

        public boolean isGraphQLEnabled() {
            return graphQLEnabled;
        }

        public void setGraphQLEnabled(boolean graphQLEnabled) {
            this.graphQLEnabled = graphQLEnabled;
        }

        public boolean isRestEnabled() {
            return restEnabled;
        }

        public void setRestEnabled(boolean restEnabled) {
            this.restEnabled = restEnabled;
        }
    }

    private final DebugConfig debugConfig = new DebugConfig();
    private final SearchConfig searchConfig = new SearchConfig();
    private final ApiConfig apiConfig = new ApiConfig();

    public DebugConfig getDebugConfig() {
        return debugConfig;
    }

    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

    public ApiConfig getApiConfig() {
        return apiConfig;
    }
}

