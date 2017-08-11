/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rs.in.staleksit.evidencija.uaa.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

/**
 * Configration of OAuth2 for Evidencija UAA.
 *
 * @author Aleksandar Stoisavljevic
 *
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig
		extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager;

	/**
	 * configure security for Authorization Server.
	 */
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security)
			throws Exception {
		// @formatter:off
		security
			.allowFormAuthenticationForClients()
			.checkTokenAccess("isAuthenticated()");
		// @formatter:on
	}

	/**
	 * configure ClientDetails.
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		// @formatter:off
		clients
			.inMemory()
			.withClient("web_app")
			.secret("secret")
			.authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
			.scopes("read", "write")
			.autoApprove(true)
			.accessTokenValiditySeconds(600)
			.refreshTokenValiditySeconds(1209600);
		// @formatter:on
	}

	/**
	 * configure server endpoints.
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
			throws Exception {
		endpoints.tokenStore(tokenStore()).authenticationManager(this.authenticationManager);
	}

	/**
	 * Define token services as bean.
	 *
	 * @return {@link DefaultTokenServices}
	 */
	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		return defaultTokenServices;
	}

	/**
	 * Token persistance storage in database.
	 *
	 * @return {@link TokenStore}
	 */
	@Bean
	public TokenStore tokenStore() {
		return new JdbcTokenStore(dataSource());
	}

	/**
	 * dataSource definition.
	 *
	 * @return {@link DataSource}
	 */
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/evidencija-uaa");
		dataSource.setUsername("root");
		dataSource.setPassword("root");

		return dataSource;
	}

}
