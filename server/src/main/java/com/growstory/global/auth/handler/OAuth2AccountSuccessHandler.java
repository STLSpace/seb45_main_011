package com.growstory.global.auth.handler;

import com.growstory.domain.account.entity.Account;
import com.growstory.domain.account.repository.AccountRepository;
import com.growstory.domain.point.entity.Point;
import com.growstory.domain.point.repository.PointRepository;
import com.growstory.domain.point.service.PointService;
import com.growstory.global.auth.jwt.JwtTokenizer;
import com.growstory.global.auth.utils.CustomAuthorityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
//OAuth2 인증이 성공한 이후 동작 (SimpleUrlAuthenticationSuccessHandler : 인증 성공했을 때 URL 지정 등 역할 수행)
public class OAuth2AccountSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private static final String S3_ENDPOINT_URL = "http://localhost";

    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;
    private final AccountRepository accountRepository;
    private final PointService pointService;
    private final PointRepository pointRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("# OAuth2AccountSuccessHandler success!");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");
        String name = (String) oAuth2User.getAttributes().get("name");
        String profileImageUrl = (String) oAuth2User.getAttributes().get("picture");
        List<String> authorities = authorityUtils.createRoles(email);

        Optional<Account> optionalAccount = accountRepository.findByEmail(email);
        Account savedAccount = null;
        if (optionalAccount.isEmpty()) {
            Point point = pointService.createPoint(email);
            savedAccount = accountRepository.save(Account.builder()
                    .email(email)
                    .displayName(name)
                    .password("")
                    .profileImageUrl(profileImageUrl)
                    .point(point)
                    .roles(authorities)
                    .accountGrade(Account.AccountGrade.GRADE_BRONZE)
                    .build());

            point.updateAccount(savedAccount);
            pointRepository.save(point);
        }

        redirect(request, response, optionalAccount.orElse(savedAccount), authorities);
    }

    private void redirect(HttpServletRequest request, HttpServletResponse response,
                          Account account, List<String> authorities) throws IOException {

        // accessToken과 refreshToken 생성
        String accessToken = delegateAccessToken(account, authorities);
        String refreshToken = delegateRefreshToken(account.getEmail());

        //FE 애플리케이션 쪽의 URI 생성.
        String uri = createURI(accessToken, refreshToken, account).toString();

        response = addCookies(response, account, accessToken, refreshToken);
//        HttpSession httpSession = request.getSession(true);
//        httpSession.setAttribute("accessToken", accessToken);

        //SimpleUrlAuthenticationSuccessHandler에서 제공하는 sendRedirect() 메서드를 이용해 Frontend 애플리케이션 쪽으로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, uri);
    }


    private String delegateAccessToken(Account account, List<String> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountId", account.getAccountId());
        claims.put("username", account.getEmail());
        claims.put("displayName", account.getDisplayName());
        claims.put("profileImageUrl", account.getProfileImageUrl());
        claims.put("roles", authorities);

        String subject = account.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }

    private String delegateRefreshToken(String username) {
        String subject = username;
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }
    private Object createURI(String accessToken, String refreshToken, Account account) {
        return UriComponentsBuilder
                .newInstance()
                .scheme("http")
                .host("localhost")
                .port(3000)
//                .host("growstory.s3-website.ap-northeast-2.amazonaws.com")
//                .port(80) //S3는 80포트
                .path("/signin")
                .build()
                .toUri();
    }

    private HttpServletResponse addCookies(HttpServletResponse response, Account account, String accessToken, String refreshToken) {
        response.addCookie(createCookie("access_token", accessToken));
        response.addCookie(createCookie("refresh_token", refreshToken));
        response.addCookie(createCookie("account_id", account.getAccountId().toString()));
        response.addCookie(createCookie("displayName", UriEncoder.encode(account.getDisplayName())));
        response.addCookie(createCookie("profileImageUrl", account.getProfileImageUrl()));

        return response;
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
//        cookie.setMaxAge(180);

        return cookie;
    }
}
