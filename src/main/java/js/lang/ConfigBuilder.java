package js.lang;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Configuration object builder. Create configuration object loaded from various sources.
 * <p>
 * Usage pattern is trivial: create configuration builder instance for desired configuration source and invoke
 * {@link #build()}.
 * 
 * <pre>
 * ConfigBuilder builder = new ConfigBuilder(new File(&quot;conf/logs.xml&quot;));
 * Config config = builder.build();
 * </pre>
 * <p>
 * Current builder implementation supports two main configuration sources: XML and Java properties.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class ConfigBuilder
{
  /** Configuration source XML stream. */
  private InputSource inputSource;

  /** Configuration properties. */
  private Properties properties;

  /**
   * Create configuration builder from XML represented as string, merely for tests.
   * 
   * @param xml source XML serialized as string.
   */
  public ConfigBuilder(String xml)
  {
    this(new StringReader(xml));
  }

  /**
   * Create configuration builder from XML file.
   * 
   * @param xmlFile source XML file.
   * @throws FileNotFoundException if source XML file does not exist.
   */
  public ConfigBuilder(File xmlFile) throws FileNotFoundException
  {
    this(new FileReader(xmlFile));
  }

  /**
   * Create configuration builder from XML character stream.
   * 
   * @param reader source character stream.
   */
  public ConfigBuilder(Reader reader)
  {
    this.inputSource = new InputSource(reader);
  }

  /**
   * Create configuration builder from XML byte stream.
   * 
   * @param stream source byte stream.
   */
  public ConfigBuilder(InputStream stream)
  {
    this.inputSource = new InputSource(stream);
  }

  /**
   * Create configuration builder from Java properties.
   * 
   * @param properties source properties.
   */
  public ConfigBuilder(Properties properties)
  {
    this.properties = properties;
  }

  /** Protected default constructor for sub-classing. */
  protected ConfigBuilder()
  {
  }

  /**
   * Build configuration object. Configuration object is not reusable so this factory creates a new instance for every
   * call.
   * 
   * @return newly created configuration object.
   * @throws ConfigException if XML stream read operation fails or is not well formed.
   */
  public Config build() throws ConfigException
  {
    if(properties != null) {
      Config config = new Config("properties");
      config.setProperties(properties);
      return config;
    }

    try {
      Loader loader = new Loader();

      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      reader.setContentHandler(loader);
      reader.parse(inputSource);

      return loader.getConfig();
    }
    catch(Exception e) {
      throw new ConfigException(e);
    }
  }

  /**
   * SAX handler for configuration object loading from XML source.
   * 
   * @author Iulian Rotaru
   * @version final
   */
  private static class Loader extends DefaultHandler
  {
    /** Keep track of nested configuration objects. */
    private Stack<Config> stack = new Stack<Config>();
    /** String builder for text content. */
    private StringBuilder textBuilder = new StringBuilder();

    /**
     * Get configuration object loaded from XML source.
     * 
     * @return configuration object.
     */
    public Config getConfig()
    {
      return stack.get(0);
    }

    /**
     * Create configuration object with tag name and initialize its attributes. If tag name is <code>property</code> add
     * new property to last configuration object from stack. In any case reset text builder.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      textBuilder.setLength(0);

      if(qName.equals("property")) {
        Config config = stack.peek();
        config.setProperty(value(attributes, "name"), value(attributes, "value"));
      }
      else {
        Config config = new Config(qName);
        if(!stack.isEmpty()) {
          Config parent = stack.peek();
          parent.addChild(config);
        }
        stack.push(config);

        for(int i = 0; i < attributes.getLength(); ++i) {
          config.setAttribute(attributes.getQName(i), attributes.getValue(i));
        }
      }
    }

    /**
     * If text builder is not empty set text content to last configuration object from stack. If element tag is not
     * <code>property</code> pop most recent configuration object from stack.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
      Config config = stack.peek();
      if(textBuilder.length() > 0) {
        config.setValue(textBuilder.toString());
      }

      if(!qName.equals("property") && stack.size() > 1) {
        stack.pop();
      }
    }

    /** Load characters into text builder. */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
      textBuilder.append(ch, start, length);
    }

    /**
     * Get named attribute value throwing exception if not attribute found.
     * 
     * @param attributes attributes list,
     * @param attributeName name of attribute to retrieve.
     * @return attribute value.
     * @throws SAXException if there is no attribute with requested name.
     */
    private static String value(Attributes attributes, String attributeName) throws SAXException
    {
      String value = attributes.getValue(attributeName);
      if(value == null) {
        throw new SAXException(new ConfigException("Missing attribute <%s>.", attributeName));
      }
      return value;
    }
  }
}
