package io.curity.identityserver.plugin.test.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationHandler;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationRequest;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticationResult;
import se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState.State.EXPIRED;
import static se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState.State.FAILED;
import static se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState.State.STARTED;
import static se.curity.identityserver.sdk.authentication.BackchannelAuthenticatorState.State.SUCCEEDED;

public class TestBackchannelAuthenticatorHandler implements BackchannelAuthenticationHandler
{
    private static final Logger _logger = LoggerFactory.getLogger(TestBackchannelAuthenticatorHandler.class);

    private static Map<String, BackchannelAuthenticatorState.State> mutableRequestStateMap = new HashMap<>();
    private static Map<String, String> mutableRequestSubjectMap = new HashMap<>();

    public TestBackchannelAuthenticatorHandler()
    {
    }

    @Override
    public Optional<BackchannelAuthenticationResult> startAuthentication(String authReqId, String s1, BackchannelAuthenticationRequest backchannelAuthenticationRequest)
    {
        _logger.trace("startAuthentication() called.");
        //TODO call frontchannel authenticator and get authenticationAttributes
        mutableRequestSubjectMap.put(authReqId, backchannelAuthenticationRequest.getSubject());
        return Optional.of(new BackchannelAuthenticationResult(null, new BackchannelAuthenticatorState(STARTED)));
    }

    @Override
    public Optional<BackchannelAuthenticationResult> checkAuthenticationStatus(String authReqId)
    {
        _logger.trace("checkAuthentication() called.");

        if(!mutableRequestSubjectMap.containsKey(authReqId))
        {
            return Optional.of(new BackchannelAuthenticationResult(null,
                    new BackchannelAuthenticatorState(EXPIRED)));
        }
        else if("denying-user".equals(mutableRequestSubjectMap.get(authReqId)))
        {
            return Optional.of(new BackchannelAuthenticationResult(null,
                    new BackchannelAuthenticatorState(FAILED)));
        }

        if (!mutableRequestStateMap.containsKey(authReqId))
        {
            // for test purposes, first time return STARTED status and SUCCEEDED subsequently
            mutableRequestStateMap.put(authReqId, STARTED);
            _logger.trace("Authentication pending, still..");
            return Optional.of(new BackchannelAuthenticationResult(null,
                    new BackchannelAuthenticatorState(STARTED)));
        }
        else
        {
            _logger.trace("Authentication done");
            AuthenticationAttributes authenticationAttributes = AuthenticationAttributes.of(
                    SubjectAttributes.of(mutableRequestSubjectMap.get(authReqId), Attributes.empty()),
                    ContextAttributes.empty());
            return Optional.of(new BackchannelAuthenticationResult(authenticationAttributes,
                    new BackchannelAuthenticatorState(SUCCEEDED)));
        }
    }

    @Override
    public void cancelAuthenticationRequest(String authReqID)
    {
        _logger.trace("cancelAuthentication() called.");
        mutableRequestStateMap.remove(authReqID);
    }
}
