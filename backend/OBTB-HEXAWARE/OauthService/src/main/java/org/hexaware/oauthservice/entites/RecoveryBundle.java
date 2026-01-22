package org.hexaware.oauthservice.entites;

import java.io.Serializable;
import java.util.List;

public class RecoveryBundle implements Serializable {
    private AuthIdentity authIdentity;
    private List<String> securityQuestions;

    public RecoveryBundle(AuthIdentity authIdentity, List<String> securityQuestions) {
        this.authIdentity = authIdentity;
        this.securityQuestions = securityQuestions;
    }

    public AuthIdentity getAuthIdentity() { return authIdentity; }
    public List<String> getSecurityQuestions() { return securityQuestions; }
}
