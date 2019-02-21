package at.porscheinformatik.sonarqube.licensecheck.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

class SettingsXmlParser extends SettingsXmlHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsXmlParser.class);

    private SettingsXmlParser() {

    }

    public static Setting parseXmlFile(File filePath) {

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SettingsXmlHandler settingsXmlHandler = new SettingsXmlHandler();
        SAXParser saxParser;

        if (filePath.exists()) {
            try {
                saxParser = saxParserFactory.newSAXParser();
                saxParser.parse(filePath, settingsXmlHandler);
            } catch (Exception e) {
                LOGGER.warn("Could not parse file " + filePath, e);
            }
        } else {
            LOGGER.info("Could not find file {}", filePath);
        }

        return settingsXmlHandler.getSetting();

    }
}
