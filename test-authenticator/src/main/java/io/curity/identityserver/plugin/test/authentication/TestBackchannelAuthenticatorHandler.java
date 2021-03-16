package io.curity.identityserver.plugin.test.authentication;

import io.curity.identityserver.plugin.test.config.TestAuthenticatorPluginConfig;
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
import se.curity.identityserver.sdk.plugin.descriptor.BackchannelAuthenticatorPluginDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TestBackchannelAuthenticatorHandler implements BackchannelAuthenticationHandler
{
    private static final Logger _logger = LoggerFactory.getLogger(TestBackchannelAuthenticatorHandler.class);

    private static Map<String, BackchannelAuthenticatorState> mutableRequestStateMap = new HashMap<>();
    private static Map<String, String> mutableRequestSubjectMap = new HashMap<>();

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
    public Optional<BackchannelAuthenticationResult> startAuthentication(String authReqId,
                                                                         String authenticatorId,
                                                                         BackchannelAuthenticationRequest request)
    {
        _logger.trace("startAuthentication() called.");
        //TODO call frontchannel authenticator and get authenticationAttributes
        mutableRequestSubjectMap.put(authReqId, request.getSubject());
        return Optional.of(new BackchannelAuthenticationResult(null, BackchannelAuthenticatorState.STARTED));
    }

    @Override
    public Optional<BackchannelAuthenticationResult> checkAuthenticationStatus(String authReqId)
    {
        _logger.trace("checkAuthentication() called.");

        if (!mutableRequestSubjectMap.containsKey(authReqId))
        {
            return Optional.of(new BackchannelAuthenticationResult(null,
                    BackchannelAuthenticatorState.EXPIRED));
        }
        else if ("denying-user".equals(mutableRequestSubjectMap.get(authReqId)))
        {
            return Optional.of(new BackchannelAuthenticationResult(null,
                    BackchannelAuthenticatorState.FAILED));
        }

        if (!mutableRequestStateMap.containsKey(authReqId))
        {
            // for test purposes, first time return STARTED status and SUCCEEDED subsequently
            mutableRequestStateMap.put(authReqId, BackchannelAuthenticatorState.STARTED);
            _logger.trace("Authentication pending, still..");
            return Optional.of(new BackchannelAuthenticationResult(null,
                    BackchannelAuthenticatorState.STARTED));
        }
        else
        {
            _logger.trace("Authentication done");
            AuthenticationAttributes authenticationAttributes = AuthenticationAttributes.of(
                    SubjectAttributes.of(mutableRequestSubjectMap.get(authReqId), Attributes.empty()),
                    ContextAttributes.empty());
            return Optional.of(new BackchannelAuthenticationResult(authenticationAttributes,
                    BackchannelAuthenticatorState.SUCCEEDED));
        }
    }

    @Override
    public void cancelAuthenticationRequest(String authReqID)
    {
        _logger.trace("cancelAuthentication() called.");
        mutableRequestStateMap.remove(authReqID);
    }
}
