package io.curity.identityserver.plugin.test.descriptor;

import io.curity.identityserver.plugin.test.authentication.TestBackchannelAuthenticatorHandler;
import io.curity.identityserver.plugin.test.config.TestBackchannelAuthenticatorConfig;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationHandler;
import se.curity.identityserver.sdk.plugin.descriptor.AuthenticatorPluginDescriptor;
import se.curity.identityserver.sdk.plugin.descriptor.BackchannelAuthenticatorPluginDescriptor;

public final class TestBackchannelAuthenticatorPluginDescriptor
        implements BackchannelAuthenticatorPluginDescriptor<TestBackchannelAuthenticatorConfig>
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
    public Class<? extends TestBackchannelAuthenticatorConfig> getConfigurationType()
    {
        return TestBackchannelAuthenticatorConfig.class;
    }

    @Override
    public Class<? extends AuthenticatorPluginDescriptor<?>> getFrontchannelPluginDescriptorReference()
    {
        return TestAuthenticatorPluginDescriptor.class;
    }
}
