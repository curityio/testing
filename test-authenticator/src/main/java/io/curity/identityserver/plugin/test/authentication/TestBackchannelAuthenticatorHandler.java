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

package io.curity.identityserver.plugin.test.authentication;

import io.curity.identityserver.plugin.test.config.TestBackchannelAuthenticatorConfig;
import io.curity.identityserver.plugin.test.descriptor.TestAuthenticatorPluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationHandler;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationRequest;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationResult;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState;
import se.curity.identityserver.sdk.authentication.BackchannelStartAuthenticationResult;
import se.curity.identityserver.sdk.plugin.descriptor.BackchannelAuthenticatorPluginDescriptor;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.SECONDS;
import static se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState.STARTED;

public final class TestBackchannelAuthenticatorHandler implements BackchannelAuthenticationHandler
{
    private static final Logger _logger = LoggerFactory.getLogger(TestBackchannelAuthenticatorHandler.class);

    private static final Map<String, BackchannelAuthenticatorState> mutableRequestStateMap = new HashMap<>();
    private static final Map<String, Map.Entry<String, Instant>> mutableRequestSubjectMap = new HashMap<>();

    private final TestBackchannelAuthenticatorConfig _backchannelConfiguration;

    /**
     * Creates a test backchannel authentication handler.
     * The configuration is injected by the dependency injection framework.
     * If the backchannel plugin "links" to the {@link TestAuthenticatorPluginDescriptor}
     * via the {@link BackchannelAuthenticatorPluginDescriptor#getFrontchannelPluginDescriptorReference()} method,
     * this plugin can obtain both its own configuration object, as well as the linked plugin's configuration!
     *
     * @param backchannelConfiguration  this plugin's configuration
     */
    public TestBackchannelAuthenticatorHandler(
            TestBackchannelAuthenticatorConfig backchannelConfiguration)
    {
        _backchannelConfiguration = backchannelConfiguration;
    }

    @Override
    public BackchannelStartAuthenticationResult startAuthentication(String authReqId,
                                                                    BackchannelAuthenticationRequest request)
    {
        _logger.trace("startAuthentication() called.");

        mutableRequestSubjectMap.put(authReqId, new AbstractMap.SimpleImmutableEntry<>(request.getSubject(), Instant.now()));
        return BackchannelStartAuthenticationResult.ok();
    }

    @Override
    public Optional<BackchannelAuthenticationResult> checkAuthenticationStatus(String authReqId)
    {
        _logger.trace("checkAuthentication() called.");

        String subject = mutableRequestSubjectMap.get(authReqId).getKey();

        if (!mutableRequestSubjectMap.containsKey(authReqId))
        {
            return Optional.of(new BackchannelAuthenticationResult(null,
                    BackchannelAuthenticatorState.EXPIRED));
        }

        if (mutableRequestStateMap.containsKey(authReqId))
        {
            if (_backchannelConfiguration.getDelay().isPresent())
            {
                Instant startAuthInstant = mutableRequestSubjectMap.get(authReqId).getValue();
                Instant continueInstant = startAuthInstant.plus(_backchannelConfiguration.getDelay().get(), SECONDS);
                Instant now = Instant.now();

                if (now.isBefore(continueInstant))
                {
                    _logger.trace("Authentication pending till {}..", continueInstant);
                    return createStartedBackchannelAuthenticationResult();
                }
            }

            Optional<String> maybeRejectingUser = _backchannelConfiguration.getRejectingUser();
            if (maybeRejectingUser.isPresent() && subject.equals(maybeRejectingUser.get()))
            {
                _logger.trace("User {} is rejecting authentication", maybeRejectingUser.get());
                return Optional.of(new BackchannelAuthenticationResult(null,
                        BackchannelAuthenticatorState.FAILED));
            }

            _logger.trace("Authentication done");
            AuthenticationAttributes authenticationAttributes = AuthenticationAttributes.of(
                    SubjectAttributes.of(subject, Attributes.empty()),
                    ContextAttributes.empty());
            return Optional.of(new BackchannelAuthenticationResult(authenticationAttributes,
                    BackchannelAuthenticatorState.SUCCEEDED));
        }
        else
        {
            // for test purposes, first time return STARTED status and SUCCEEDED subsequently
            mutableRequestStateMap.put(authReqId, STARTED);
            _logger.trace("Authentication pending, still..");
            return createStartedBackchannelAuthenticationResult();
        }
    }

    private static Optional<BackchannelAuthenticationResult> createStartedBackchannelAuthenticationResult()
    {
        return Optional.of(new BackchannelAuthenticationResult(null, STARTED));
    }

    @Override
    public void cancelAuthenticationRequest(String authReqID)
    {
        _logger.trace("cancelAuthentication() called.");
        mutableRequestStateMap.remove(authReqID);
    }
}
