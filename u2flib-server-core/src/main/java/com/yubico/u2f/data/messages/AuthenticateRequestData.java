package com.yubico.u2f.data.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.yubico.u2f.U2fPrimitives;
import com.yubico.u2f.crypto.ChallengeGenerator;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.json.JsonSerializable;
import com.yubico.u2f.data.messages.json.Persistable;
import com.yubico.u2f.data.messages.key.util.U2fB64Encoding;
import com.yubico.u2f.exceptions.NoEligibleDevicesException;
import com.yubico.u2f.exceptions.U2fBadInputException;
import java.util.List;
import lombok.EqualsAndHashCode;

import static com.google.common.base.Preconditions.checkArgument;

@EqualsAndHashCode
public class AuthenticateRequestData extends JsonSerializable implements Persistable {

    private static final long serialVersionUID = 35378338769078256L;

    @JsonProperty
    private final String appId;

    /**
     * The websafe-base64-encoded challenge.
     */
    @JsonProperty
    private final String challenge;

    @JsonProperty
    private final List<AuthenticateRequest> authenticateRequests;

    @JsonCreator
    public AuthenticateRequestData(@JsonProperty("appId") String appId, @JsonProperty("challenge") String challenge, @JsonProperty("authenticateRequests") List<AuthenticateRequest> authenticateRequests) {
        this.appId = appId;
        this.challenge = challenge;
        this.authenticateRequests = authenticateRequests;
    }

    public AuthenticateRequestData(String appId, Iterable<? extends DeviceRegistration> devices, U2fPrimitives u2f, ChallengeGenerator challengeGenerator) throws U2fBadInputException, NoEligibleDevicesException {
        this.appId = appId;

        byte[] challenge = challengeGenerator.generateChallenge();
        this.challenge = U2fB64Encoding.encode(challenge);

        ImmutableList.Builder<AuthenticateRequest> requestBuilder = ImmutableList.builder();
        for (DeviceRegistration device : devices) {
            if(!device.isCompromised()) {
                requestBuilder.add(u2f.startAuthentication(appId, device, challenge));
            }
        }
        authenticateRequests = requestBuilder.build();

        if (authenticateRequests.isEmpty()) {
            if(Iterables.isEmpty(devices)) {
                throw new NoEligibleDevicesException(devices, "No devices registrered", NoEligibleDevicesException.ErrorType.NONE_REGISTERED);
            } else {
                throw new NoEligibleDevicesException(devices, "All devices compromised", NoEligibleDevicesException.ErrorType.ALL_COMPROMISED);
            }
        }
    }

    public List<AuthenticateRequest> getAuthenticateRequests() {
        return ImmutableList.copyOf(authenticateRequests);
    }

    public AuthenticateRequest getAuthenticateRequest(AuthenticateResponse response) throws U2fBadInputException {
        checkArgument(Objects.equal(getRequestId(), response.getRequestId()), "Wrong request for response data");

        for (AuthenticateRequest request : authenticateRequests) {
            if (Objects.equal(request.getKeyHandle(), response.getKeyHandle())) {
                return request;
            }
        }
        throw new U2fBadInputException("Responses keyHandle does not match any contained request");
    }

    public String getRequestId() {
        return authenticateRequests.get(0).getChallenge();
    }

    public static AuthenticateRequestData fromJson(String json) throws U2fBadInputException {
        return fromJson(json, AuthenticateRequestData.class);
    }
}
