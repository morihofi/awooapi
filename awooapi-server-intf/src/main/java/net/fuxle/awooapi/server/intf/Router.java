package net.fuxle.awooapi.server.intf;

import net.fuxle.awooapi.annotations.HandlerType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {
    private final Map<HandlerType, Map<String, Endpoint>> staticHandlers = new HashMap<>();
    private final Map<HandlerType, List<VariableEndpoint>> variableHandlers = new HashMap<>();

    public void addHandler(Endpoint endpoint) {
        HandlerType type = endpoint.getType();
        String path = endpoint.getPath();

        if (path.contains("{")) {
            // Path with variables
            variableHandlers.computeIfAbsent(type, k -> new ArrayList<>())
                    .add(new VariableEndpoint(path, endpoint));
        } else {
            // Static path
            staticHandlers.computeIfAbsent(type, k -> new HashMap<>())
                    .put(path, endpoint);
        }
    }

    public void removeHandler(String path) {
        for (HandlerType type : HandlerType.values()) {
            staticHandlers.getOrDefault(type, Collections.emptyMap()).remove(path);

            variableHandlers.getOrDefault(type, Collections.emptyList())
                    .removeIf(variableEndpoint -> variableEndpoint.template.equals(path));
        }
    }

    public Handler getHandler(String path, String method) {
        return getHandler(path, HandlerType.valueOf(method));
    }

    public Handler getHandler(String path, HandlerType method) {
        // Check for static path
        Endpoint staticEndpoint = staticHandlers.getOrDefault(method, Collections.emptyMap()).get(path);
        if (staticEndpoint != null) {
            return staticEndpoint.getHandler();
        }

        // Check for variable path
        List<VariableEndpoint> varEndpoints = variableHandlers.getOrDefault(method, Collections.emptyList());
        for (VariableEndpoint variableEndpoint : varEndpoints) {
            Map<String, String> pathVariables = variableEndpoint.match(path);
            if (pathVariables != null) {
                // Optionally: Attach pathVariables to some context
                return variableEndpoint.endpoint.getHandler();
            }
        }

        return null; // No matching handler
    }

    public String getPathParam(String path, String param) {
        for (HandlerType type : HandlerType.values()) {
            List<VariableEndpoint> varEndpoints = variableHandlers.getOrDefault(type, Collections.emptyList());
            for (VariableEndpoint variableEndpoint : varEndpoints) {
                Map<String, String> pathVariables = variableEndpoint.match(path);
                if (pathVariables != null && pathVariables.containsKey(param)) {
                    return pathVariables.get(param);
                }
            }
        }
        return null; // Parameter not found
    }

    private static class VariableEndpoint {
        private final String template;
        private final Endpoint endpoint;
        private final Pattern pattern;
        private final List<String> variableNames;

        public VariableEndpoint(String template, Endpoint endpoint) {
            this.template = template;
            this.endpoint = endpoint;
            this.variableNames = new ArrayList<>();
            this.pattern = compileTemplate(template);
        }

        private Pattern compileTemplate(String template) {
            StringBuilder regex = new StringBuilder();
            Matcher matcher = Pattern.compile("\\{([^/]+)}").matcher(template);

            int lastIndex = 0;
            while (matcher.find()) {
                // Append static text (non-variable part of the path)
                regex.append(Pattern.quote(template.substring(lastIndex, matcher.start())));
                // Append the variable placeholder regex
                regex.append("([^/]+)");
                // Store the variable name
                variableNames.add(matcher.group(1));
                lastIndex = matcher.end();
            }

            // Append the remaining static part of the template
            regex.append(Pattern.quote(template.substring(lastIndex)));
            return Pattern.compile("^" + regex + "$");
        }


        public Map<String, String> match(String path) {
            Matcher matcher = pattern.matcher(path);
            if (!matcher.matches()) {
                return null;
            }

            Map<String, String> variables = new HashMap<>();
            for (int i = 0; i < variableNames.size(); i++) {
                variables.put(variableNames.get(i), matcher.group(i + 1));
            }
            return variables;
        }
    }
}
