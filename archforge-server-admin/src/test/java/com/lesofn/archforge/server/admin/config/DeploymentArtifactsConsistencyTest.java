package com.lesofn.archforge.server.admin.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Deployment files drift silently from the application: a compose service that does not pass a variable the profile
 * requires crash-loops only in that environment (prod admin lost {@code CORS_ALLOWED_ORIGINS}), and a deploy script
 * pointing at a renamed directory fails only when someone deploys ({@code archforge-domain/}).
 */
@Tag("contract")
class DeploymentArtifactsConsistencyTest {

    private static final Path REPO = Path.of("..");
    /** {@code ${VAR}} with no default: the application cannot start without it. */
    private static final Pattern REQUIRED_PLACEHOLDER = Pattern.compile("\\$\\{([A-Z][A-Z0-9_]*)}");
    private static final Pattern REPO_PATH = Pattern.compile("\\$\\{REPO_ROOT}/([A-Za-z0-9_./-]+)");

    @Test
    void composeServicesPassEveryVariableTheirProfileRequires() throws IOException {
        List<String> missing = new ArrayList<>();
        for (String[] deployment : List.of(
                new String[] {
                        "prod", "docker-compose.prod.yml", "backend", "archforge-server-admin"
                },
                new String[] {
                        "prod", "docker-compose.prod.yml", "backend-web", "archforge-server-web"
                },
                new String[] {
                        "staging", "docker-compose.staging.yml", "backend", "archforge-server-admin"
                },
                new String[] {
                        "staging", "docker-compose.staging.yml", "backend-web", "archforge-server-web"
                })) {
            Set<String> required = requiredVariables(REPO.resolve(deployment[3] + "/src/main/resources/application.yaml"));
            required.addAll(requiredVariables(REPO.resolve(deployment[3] + "/src/main/resources/application-" + deployment[0] +
                    ".yaml")));
            Collection<String> provided = environmentOf(REPO.resolve("docker/" + deployment[1]), deployment[2]);
            for (String variable : required) {
                if (!provided.contains(variable)) {
                    missing.add(deployment[1] + " " + deployment[2] + " lacks " + variable);
                }
            }
        }

        assertEquals(List.of(), missing);
    }

    /** An unauthenticated Redis is open to every container on the network (sessions, caches, locks, rate limits). */
    @Test
    @SuppressWarnings("unchecked")
    void prodAndStagingRedisRequireAPassword() throws IOException {
        for (String file : List.of("docker-compose.prod.yml", "docker-compose.staging.yml")) {
            Map<String, Object> compose = new Yaml().load(Files.readString(REPO.resolve("docker/" + file)));
            Map<String, Object> services = (Map<String, Object>) Objects.requireNonNull(compose.get("services"), "services");
            Map<String, Object> redis = (Map<String, Object>) Objects.requireNonNull(services.get("redis"), file);
            assertTrue(String.valueOf(redis.get("command")).contains("--requirepass"), file + " starts Redis without a password");
        }
    }

    @Test
    void deployScriptsOnlyReferenceExistingRepositoryPaths() throws IOException {
        List<String> broken = new ArrayList<>();
        try (Stream<Path> scripts = Files.walk(REPO.resolve("scripts"))) {
            for (Path script : scripts.filter(p -> p.toString().endsWith(".sh")).toList()) {
                Matcher matcher = REPO_PATH.matcher(Files.readString(script));
                while (matcher.find()) {
                    String path = matcher.group(1);
                    if (!path.startsWith("..") && !Files.exists(REPO.resolve(path))) {
                        broken.add(REPO.relativize(script) + " -> " + path);
                    }
                }
            }
        }

        assertTrue(broken.isEmpty(), "deploy scripts reference missing paths: " + broken);
    }

    /** "Read repos.yaml first" — it has to be YAML a parser accepts, and it has to name every sibling repository. */
    @Test
    @SuppressWarnings("unchecked")
    void reposYamlParsesAndListsEverySiblingRepository() throws IOException {
        Map<String, Object> map = new Yaml().load(Files.readString(REPO.resolve("repos.yaml")));
        List<Map<String, Object>> repos = (List<Map<String, Object>>) Objects.requireNonNull(map.get("repos"), "repos");

        assertEquals(List.of("ArchForge", "ArchForgeAdmin", "ArchForgeWeb", "ArchForgeDocs"),
                repos.stream().map(repo -> String.valueOf(repo.get("name"))).toList());
    }

    private static Set<String> requiredVariables(Path yaml) throws IOException {
        Set<String> variables = new TreeSet<>();
        for (String line : Files.readAllLines(yaml)) {
            // comments (commented-out blocks such as an optional read replica) require nothing
            String content = line.strip().startsWith("#") ? "" : line.replaceFirst("\\s+#.*$", "");
            Matcher matcher = REQUIRED_PLACEHOLDER.matcher(content);
            while (matcher.find()) {
                variables.add(matcher.group(1));
            }
        }
        return variables;
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> environmentOf(Path composeFile, String service) throws IOException {
        Map<String, Object> compose = new Yaml().load(Files.readString(composeFile));
        Map<String, Object> services = (Map<String, Object>) Objects.requireNonNull(compose.get("services"), "services");
        Map<String, Object> definition = (Map<String, Object>) Objects.requireNonNull(services.get(service), service);
        Object environment = definition.get("environment");
        if (environment instanceof Map<?, ?> map) {
            return (Collection<String>) map.keySet();
        }
        List<String> names = new ArrayList<>();
        if (environment instanceof List<?> list) {
            for (Object entry : list) {
                names.add(String.valueOf(entry).split("=", 2)[0]);
            }
        }
        return names;
    }
}
