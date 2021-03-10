/*
 *  Copyright 2018 Curity AB
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

import io.curity.identityserver.plugin.test.authentication.TestAuthenticatorRequestHandler;
import io.curity.identityserver.plugin.test.config.TestAuthenticatorPluginConfig;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.haapi.RepresentationFunction;
import se.curity.identityserver.sdk.plugin.descriptor.AuthenticatorPluginDescriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TestAuthenticatorPluginDescriptor
        implements AuthenticatorPluginDescriptor<TestAuthenticatorPluginConfig>
{
    public final static String IMPLEMENTATION_TYPE = "test";

    public final static String CALLBACK = "callback";

    @Override
    public String getPluginImplementationType()
    {
        return IMPLEMENTATION_TYPE;
    }

    @Override
    public Class<? extends TestAuthenticatorPluginConfig> getConfigurationType()
    {
        return TestAuthenticatorPluginConfig.class;
    }

    @Override
    public Map<String, Class<? extends AuthenticatorRequestHandler<?>>> getAuthenticationRequestHandlerTypes()
    {
        Map<String, Class<? extends AuthenticatorRequestHandler<?>>> handlersMap = new HashMap<>();
        handlersMap.put("index", TestAuthenticatorRequestHandler.class);
        handlersMap.put("ciba-auth", TestAuthenticatorRequestHandler.class);
        return handlersMap;
    }

    @Override
    public Map<String, Class<? extends RepresentationFunction>> getRepresentationFunctions()
    {
        // This plugin works by automatically signing in any user who sends a GET request to the server.
        // It doesn't collect any information from the user, hence it doesn't require any representation to generate
        // views, the user is automatically authenticated and redirected back to the application, so all we need to
        // do here is return a non-null value, so the Curity Server knows this plugin supports HAAPI even without
        // providing any representations.
        return Collections.emptyMap();
    }

    private static <T> Map<String, T> mapOf(String key, T value)
    {
        return Collections.unmodifiableMap(Collections.singletonMap(key, value));
    }
}
