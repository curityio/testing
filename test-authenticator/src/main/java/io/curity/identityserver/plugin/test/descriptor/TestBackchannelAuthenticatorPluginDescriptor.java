/*
 *  Copyright 2021 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
