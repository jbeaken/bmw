package org.bookmarks.website.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate5.encryptor.HibernatePBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EncryptionConfiguration {
	
	@Autowired
	private Environment environment;

	@Bean
	public BouncyCastleProvider bcProvider() {
		return new BouncyCastleProvider();
	}

	@Bean
	public StandardPBEStringEncryptor jsonEcryptor() {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm(environment.getProperty("json.encrypt.algorithm"));
		standardPBEStringEncryptor.setPassword(environment.getProperty("json.encrypt.password"));
		standardPBEStringEncryptor.setProvider(bcProvider());

		return standardPBEStringEncryptor;
	}

	@Bean
	public HibernatePBEStringEncryptor hibernateStringEncryptor() {
		HibernatePBEStringEncryptor hibernatePBEStringEncryptor = new HibernatePBEStringEncryptor();
		hibernatePBEStringEncryptor.setRegisteredName("strongHibernateStringEncryptor");
		hibernatePBEStringEncryptor.setEncryptor(dbEncryptor());

		return hibernatePBEStringEncryptor;
	}

	@Bean
	public StandardPBEStringEncryptor dbEncryptor() {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm(environment.getProperty("db.encrypt.algorithm"));
		standardPBEStringEncryptor.setPassword(environment.getProperty("db.encrypt.password"));
		standardPBEStringEncryptor.setProvider(bcProvider());

		return standardPBEStringEncryptor;
	}

}
