package org.pado.api.core.vault.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class DeployVaultService {
    @Value("${spring.cloud.vault.uri}")
    private String vaultUri; // 예: https://vault.internal:8200

    @Value("${spring.cloud.vault.app-role.role-id}")
    private String javaRoleId;   // Java(PADO) AppRole
    @Value("${spring.cloud.vault.app-role.secret-id}")
    private String javaSecretId; // Java(PADO) AppRole

    private RestTemplate newVaultRestTemplate() {
        var rt = new RestTemplate();
        // 타임아웃 등 설정하고 싶으면 ClientHttpRequestFactory 커스텀
        return rt;
    }

     /**
     * go-role의 SecretId를 "래핑 토큰"으로 발급한다.
     * @param goRoleName 예: "go-role"
     * @param wrapTtlSec 예: 60
     * @return wrapped token (예: s.wrapped.xxxxx)
     */
    public String issueWrappedSecretId(String goRoleName, int wrapTtlSec) {
        RestTemplate rt = newVaultRestTemplate();

        String loginUrl = vaultUri + "/v1/auth/approle/login";
        Map<String, Object> loginBody = Map.of(
            "role_id", javaRoleId,
            "secret_id", javaSecretId
        );
        var loginResp = rt.postForEntity(loginUrl, loginBody, Map.class);
        if (!loginResp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Vault login failed: " + loginResp.getStatusCode());
        }
        Map<?,?> auth = (Map<?,?>) ((Map<?,?>) loginResp.getBody()).get("auth");
        if (auth == null || auth.get("client_token") == null) {
            throw new IllegalStateException("No client_token in Vault login response");
        }
        String javaClientToken = (String) auth.get("client_token");

        String secretIdUrl = vaultUri + "/v1/auth/approle/role/" + goRoleName + "/secret-id";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", javaClientToken);
        headers.set("X-Vault-Wrap-TTL", wrapTtlSec + "s");

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(Map.of(), headers);

        var resp = rt.exchange(secretIdUrl, HttpMethod.POST, entity, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("SecretId wrap failed: " + resp.getStatusCode());
        }

        Map<?,?> body = resp.getBody();
        Map<?,?> wrapInfo = (Map<?,?>) body.get("wrap_info");
        if (wrapInfo == null || wrapInfo.get("token") == null) {
            throw new IllegalStateException("No wrap_info.token in response: " + body);
        }
        return (String) wrapInfo.get("token");
    }
}
