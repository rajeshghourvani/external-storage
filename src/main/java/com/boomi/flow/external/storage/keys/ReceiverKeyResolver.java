package com.boomi.flow.external.storage.keys;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.DecryptionKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;

import javax.inject.Inject;
import java.security.Key;
import java.util.List;
import java.util.UUID;

public class ReceiverKeyResolver implements DecryptionKeyResolver, VerificationKeyResolver {
    private final KeyRepository keyRepository;

    @Inject
    public ReceiverKeyResolver(KeyRepository keyRepository) {
        this.keyRepository = keyRepository;
    }

    @Override
    public Key resolveKey(JsonWebEncryption jwe, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        return resolveKey(jwe.getKeyIdHeaderValue())
                .getPrivateKey();
    }

    @Override
    public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
        return resolveKey(jws.getKeyIdHeaderValue())
                .getPublicKey();
    }

    public PublicJsonWebKey resolveKeyFromPublicKey(String publicJwk) {
        PublicJsonWebKey publicKey;
        try {
            publicKey = PublicJsonWebKey.Factory.newPublicJwk(publicJwk);
        } catch (JoseException e) {
            throw new RuntimeException("Unable to create a JWK instance from the public receiver key", e);
        }

        return keyRepository.findReceiverKey(UUID.fromString(publicKey.getKeyId()));
    }

    private PublicJsonWebKey resolveKey(String id) throws UnresolvableKeyException {
        var key = keyRepository.findReceiverKey(UUID.fromString(id));
        if (key == null) {
            throw new UnresolvableKeyException("Unable to resolve a receiver key with the ID " + id);
        }

        return key;
    }
}
