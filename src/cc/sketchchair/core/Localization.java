package cc.sketchchair.core;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Localization {
	static ResourceBundle languageStrings;
	static Locale language = Locale.getDefault();
	static String baseDir;
	static String languageStr;
	Localization(String _baseDir, String _language){
		baseDir = _baseDir;
		languageStr = _language;
		LOGGER.info("base dir " +_baseDir);

		LOGGER.info("loading language " +_language);
		
		if(_language.equals("auto")){
			languageStr = Locale.getDefault().toString();
			setLaguage(Locale.getDefault());
		}else
			setLaguage(new Locale(_language));

	}
	
	
	void setLaguage(Locale _language){
		
		LOGGER.info(_language+"");
		language = _language;
		//languageStr = _language.toString();
		loadLanguageFiles();
		loadLanguageFonts();
	}
	
	
	void loadLanguageFiles(){
		//languageStrings = ResourceBundle.getBundle( "Strings",language);
		try{
			languageStrings = ResourceBundle.getBundle( "Strings_"+languageStr);
			
		
			
			
			}catch (MissingResourceException  e){
				
				LOGGER.info("cant load "+ languageStr + " falling back to en_US");
				languageStr = "en_US";
				languageStrings = ResourceBundle.getBundle( "Strings_"+languageStr);

				
			}
		
	    LOGGER.info("Loading language file: " +language.getCountry());

	    LOGGER.info("file.encoding "+ System.getProperty("file.encoding"));
	    LOGGER.info("file.encoding "+ Charset.defaultCharset().name());
	    // get the keys
	      Enumeration<String> enumeration = languageStrings.getKeys();

	      // print all the keys
	      while (enumeration.hasMoreElements()) {
	    	  LOGGER.info("" + getString(enumeration.nextElement()));
	      }
	      
	      
	}
	
	public void loadLanguageFonts(){
		if(languageStr.toLowerCase().equals("ja_jp") && !GLOBAL.isMacOSX()){
			GLOBAL.font = GLOBAL.applet.createFont("Meiryo UI Bold", 12);
			GLOBAL.gui.myFontMedium = GLOBAL.font ;
			GLOBAL.gui.reload();
			
			LOGGER.info("Local loading fonts, reloading gui");
		}
	}
	public static String getString(String _key){	
		String val;
		try{
		 val =  languageStrings.getString(_key);
		} catch (MissingResourceException e) {
		 val = "__";
        }
		try {
			return new String(val.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
