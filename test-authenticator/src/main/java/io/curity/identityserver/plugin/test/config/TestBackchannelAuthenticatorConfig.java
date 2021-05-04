package io.curity.identityserver.plugin.test.config;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.Description;

import java.util.Optional;

public interface TestBackchannelAuthenticatorConfig extends Configuration
{
    @Description("The number of seconds that should be delayed before authentication succeeds")
    Optional<Integer> getDelay();

    @Description("The username for which the authentication is denied")
    Optional<String> getRejectingUser();
}
