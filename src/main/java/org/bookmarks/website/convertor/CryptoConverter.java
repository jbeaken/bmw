package org.bookmarks.website.convertor;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.jasypt.util.text.StrongTextEncryptor;

@Converter
public class CryptoConverter implements AttributeConverter<String, String> {
	 
	    private static final String PASSWORD = "asdf;lkj2(8Udjj*&&^ddFTD";
	 
	    @Override
	    public String convertToDatabaseColumn(String text) {
	    	
			//Encrypt
			StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
			textEncryptor.setPassword(PASSWORD);	    	
			
			return textEncryptor.encrypt(text);
	    }
	 
	    @Override
	    public String convertToEntityAttribute(String dbData) {
	    	
	    	//Decrypt
	    	StrongTextEncryptor textEncryptor = new StrongTextEncryptor();
			textEncryptor.setPassword(PASSWORD);	 
			
			return textEncryptor.decrypt(dbData);
	    }
}
