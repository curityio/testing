package io.curity.identityserver.plugin.test.authentication;

import io.curity.identityserver.plugin.test.config.TestAuthenticatorPluginConfig;
import io.curity.identityserver.plugin.test.config.TestBackchannelAuthenticatorConfig;
import io.curity.identityserver.plugin.test.descriptor.TestAuthenticatorPluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.Result;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationHandler;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationRequest;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationResult;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState;
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
    private final TestAuthenticatorPluginConfig _frontchannelConfiguration;

    /**
     * Notice that because this backchannel plugin "links" to the {@link TestAuthenticatorPluginDescriptor}
     * via the {@link BackchannelAuthenticatorPluginDescriptor#getFrontchannelPluginDescriptorReference()} method,
     * this plugin can obtain both its own configuration object, as well as the linked plugin's configuration!
     *
     * @param backchannelConfiguration  this plugin's configuration
     * @param frontchannelConfiguration the linked frontchannel plugins' configuration
     */
    public TestBackchannelAuthenticatorHandler(
            TestBackchannelAuthenticatorConfig backchannelConfiguration,
            TestAuthenticatorPluginConfig frontchannelConfiguration)
    {
        _backchannelConfiguration = backchannelConfiguration;
        _frontchannelConfiguration = frontchannelConfiguration;
    }

    @Override
    public Result startAuthentication(String authReqId,
                                      BackchannelAuthenticationRequest request)
    {
        _logger.trace("startAuthentication() called.");
        //TODO call frontchannel authenticator and get authenticationAttributes
        mutableRequestSubjectMap.put(authReqId, new AbstractMap.SimpleImmutableEntry<>(request.getSubject(), Instant.now()));
        Result.ok();
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

            Optional<String> mayBeRejectingUser = _backchannelConfiguration.getRejectingUser();
            if (mayBeRejectingUser.isPresent() && subject.equals(mayBeRejectingUser.get()))
            {
                _logger.trace("User {} is rejecting authentication", mayBeRejectingUser.get());
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
