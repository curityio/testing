package io.curity.identityserver.plugin.test.descriptor;

import io.curity.identityserver.plugin.test.authentication.TestBackchannelAuthenticatorHandler;
import io.curity.identityserver.plugin.test.config.TestBackchannelAuthenticatorConfigurationSetting;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationHandler;
import se.curity.identityserver.sdk.plugin.descriptor.BackchannelAuthenticatorPluginDescriptor;

// SDK plugins do not have access to @NoYangGeneration
// TODO Make yang generation work for backchannel authenticators, for now this is not fully validated configuration
public class TestBackchannelAuthenticatorPluginDescriptor
        implements BackchannelAuthenticatorPluginDescriptor<TestBackchannelAuthenticatorConfigurationSetting>
{
    @Override
    public Class<? extends BackchannelAuthenticationHandler> getBackchannelAuthenticationHandlerType()
    {
        return TestBackchannelAuthenticatorHandler.class;
    }

    @Override
    public String getPluginImplementationType()
    {
        return "bca_test";
    }

    @Override
    public Class<? extends TestBackchannelAuthenticatorConfigurationSetting> getConfigurationType()
    {
        return TestBackchannelAuthenticatorConfigurationSetting.class;
    }
}
