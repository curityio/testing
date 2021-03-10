package io.curity.identityserver.plugin.test.config;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.annotation.Description;

import java.util.Optional;

public interface TestBackchannelAuthenticatorConfigurationSetting extends Configuration
{
    @Description("The optional frontchannel authenticator that this backchannel authenticator is based on.")
    Optional<String> getFrontchannelAuthenticator();
}
