package com.capstone.safeGuard.config;

import com.capstone.safeGuard.util.JwtAuthenticationFilter;
import com.capstone.safeGuard.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;

	private final String[] permitAllList = {
		"/", "/home", "/login", "/signup", "/group",
		"/childsignup", "/check-auth", "/member-logout", "/update-nickname",
		"/update-coordinate", "/return-coordinate",
		"/find-member-id", "/find-child-id-list", "/find-child-list",
		"/find-parenting-helping-list", "/find-helping-list",
		"/verification-email-request", "/verification-email",
		"/reset-member-password", "/chose-child-form", "/chose-child",
		"/add-safe", "/add-dangerous", "/delete-area", "/read-areas", "/area-detail",
		"/childremove", "/addhelper", "/helperremove", "/memberremove",
		"/duplicate-check-child", "/duplicate-check-member",
		"/send-confirm", "/fatal", "/add-parent", "/find-member-by-child",
		"/received-notice", "/received-confirm", "/sent-confirm",
		"/sent-emergency", "/received-emergency", "/emergency", "/emergency-detail",
		"/delete-comment", "/write-comment", "/return-nickname",
		"/upload-file", "/get-file", "/imagePath/**",
		"/swagger-ui/**", "/actuator/**", "/v3/api-docs/**",
	};

	private final String[] memberPermitList = {

	};

	private final String[] childPermitList = {

	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.httpBasic(AbstractHttpConfigurer::disable)
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeRequests()
			.requestMatchers(permitAllList).permitAll()
//                .requestMatchers(memberPermitList).hasRole(Authority.ROLE_MEMBER.toString())
//                .requestMatchers(childPermitList).hasRole(Authority.ROLE_CHILD.toString())
			.anyRequest().authenticated()
			.and()
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		DelegatingPasswordEncoder passwordEncoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
		passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
		return passwordEncoder;
	}

}
