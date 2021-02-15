package za.co.imqs.api;

import za.co.imqs.TestUtils;

public class TestConfig {
    public static final String COMPOSE_FILE = TestUtils.resolveWorkingFolder()+"/Docker_Test_Env/docker-compose.yml";
    public static final boolean DOCKER = false;
    public static final boolean SANDPIT_MODE = true;
}
