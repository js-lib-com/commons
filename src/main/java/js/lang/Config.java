package js.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import js.converter.Converter;
import js.converter.ConverterException;
import js.converter.ConverterRegistry;
import js.util.Params;

/**
 * Generic configuration object with XML like structure. A configuration object has a name, value, attributes,
 * properties and optional parent and child configuration objects. Its structure closely resemble a XML tree structure.
 * Therefore the usual way to load configuration object is from XML files.
 * <p>
 * One may wonder why to add extra processing and not use directly XML files. One reason would be XML has heavy
 * dependencies, and this matter on embedded devices. The second is configuration object is not loaded only from XML. It
 * can be loaded from Java properties files; in fact there is no formal requirements for specific format. This class can
 * be used to unify varied configuration sources.
 * <p>
 * Configuration object resemble XML element. Its name is element tag name and attributes are mapped directly. Value is
 * element text content and parent and children fields are similar and use for the same purpose: describe the tree
 * structure. The only specialized field is properties that are stored in a standard {@link Properties} object.
 * 
 * <pre>
 * &lt;emails repository="/srv/emails" files-pattern="*.html"&gt;
 * 	&lt;property name="mail.transport.protocol" value="smtp" /&gt;
 * 	&lt;property name="mail.debug" value="true" /&gt;
 * &lt;/emails&gt;
 * </pre>
 * 
 * Above XML fragment describe an configuration object with name <code>emails</code>, two attributes
 * <code>repository</code> and <code>files-pattern</code> and two properties, <code>mail.transport.protocol</code> and
 * <code>mail.debug</code>.
 * <p>
 * Configuration object is not reusable and is not thread safe. Anyway, if a configurable instance does not alter state
 * using setters, configuration object still can be used in a concurrent context.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class Config
{
  /** Configuration object name. */
  private final String name;

  /** Configuration object value. */
  private String value;

  /** Parent reference, null if configuration object is root. */
  private Config parent;

  /** Children list, possible empty. Children list is usually empty if value exists but is not mandatory. */
  private final List<Config> children = new ArrayList<Config>();

  /** Optional attributes list, possible empty. An attribute is a name/value string pair. */
  private final Map<String, String> attributes = new HashMap<String, String>();

  /** Optional properties, possible empty. Properties are usually empty if value exists but is not mandatory. */
  private Properties properties = new Properties();

  /**
   * Keep track of used properties. This set is updated by {@link #getProperty(String)} and tested by
   * {@link #getUnusedProperties()}.
   */
  private Set<String> usedProperties = Collections.synchronizedSet(new HashSet<String>());

  /** Converter for objects conversion to / from strings. */
  private final Converter converter = ConverterRegistry.getConverter();

  /**
   * Create configuration object with name.
   * 
   * @param name configuration object name.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public Config(String name)
  {
    Params.notNullOrEmpty(name, "Root name");
    this.name = name;
  }

  // ----------------------------------------------------
  // mutators

  /**
   * Add child configuration object.
   * 
   * @param child child configuration object.
   * @throws IllegalArgumentException if <code>child</code> argument is null.
   */
  public void addChild(Config child)
  {
    Params.notNull(child, "Child");
    child.parent = this;
    children.add(child);
  }

  /**
   * Set configuration object attribute. If attribute already exists overwrite old value. Empty value is not accepted
   * but null is considered indication to remove attribute. So that, an existing attribute cannot be either null or
   * empty.
   * 
   * @param name attribute name,
   * @param value attribute value, null ignored.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>value</code> argument is empty.
   */
  public void setAttribute(String name, String value)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    Params.notEmpty(value, "Attribute value");
    if(value != null) {
      attributes.put(name, value);
    }
    else {
      attributes.remove(name);
    }
  }

  /**
   * Set this configuration object properties. All individual properties set using {@link #setProperty(String, String)}
   * and {@link #setProperty(String, Object)} are lost.
   * 
   * @param properties properties.
   */
  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }

  /**
   * Set configuration object string property. If property already exists overwrite old value. If <code>value</code>
   * argument is null this setter does nothing.
   * 
   * @param name property name,
   * @param value property value, null ignored.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>value</code> argument is empty.
   */
  public void setProperty(String name, String value)
  {
    Params.notNullOrEmpty(name, "Property name");
    Params.notEmpty(value, "Property value");
    if(value != null) {
      properties.setProperty(name, value);
    }
  }

  /**
   * Set configuration object property. Convert <code>value</code> to string and set property. If property already
   * exists overwrite old value. If <code>value</code> argument is null this setter does nothing.
   * 
   * @param name property name,
   * @param value property value, null ignored.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws ConverterException if there is no converter registered for value type.
   */
  public void setProperty(String name, Object value)
  {
    Params.notNullOrEmpty(name, "Property name");
    if(value != null) {
      properties.setProperty(name, converter.asString(value));
    }
  }

  /**
   * Set this configuration object value. Value is converter to string before store it. If <code>value</code> argument
   * is null this setter will reset this configuration object value.
   * 
   * @param value value to set, null accepted.
   * @throws IllegalArgumentException if <code>value</code> argument is an empty string.
   * @throws ConverterException if there is no converter registered for value type.
   */
  public void setValue(Object value)
  {
    if(value instanceof String) {
      Params.notEmpty((String)value, "Value");
      this.value = (String)value;
    }
    else {
      this.value = value != null ? converter.asString(value) : null;
    }
  }

  // ----------------------------------------------------
  // getters - this section is thread safe

  /**
   * Get root of the tree this configuration object is part of.
   * 
   * @return configuration object root.
   */
  public Config getRoot()
  {
    Config root = this;
    while(root.parent != null) {
      root = root.parent;
    }
    return root;
  }

  /**
   * Get configuration object parent.
   * 
   * @return configuration object parent.
   */
  public Config getParent()
  {
    return parent;
  }

  /**
   * Get configuration object name.
   * 
   * @return configuration object name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Test if configuration object has an attribute with requested name.
   * 
   * @param name attribute name to search for.
   * @return true if configuration object has an attribute with requested name.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public boolean hasAttribute(String name)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    return attributes.containsKey(name);
  }

  /**
   * Test if configuration object has an attribute with requested name and value.
   * 
   * @param name name of the attribute to search for,
   * @param value attribute value.
   * @return true if configuration object has an attribute with requested name and value.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>value</code> argument is null or empty.
   */
  public boolean hasAttribute(String name, String value)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    Params.notNullOrEmpty(value, "Attribute value");
    return value.equals(attributes.get(name));
  }

  /**
   * Get attribute value or null if there is no attribute with requested name. An existing attribute value cannot ever
   * be empty or null so if this method returns null is for missing attribute.
   * 
   * @param name attribute name.
   * @return attribute value or null if named attribute not found.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public String getAttribute(String name)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    return attributes.get(name);
  }

  /**
   * Get attribute value or given default value if there is no attribute with requested name. If given default value is
   * null and attribute is not found this method still returns null, that is, requested default value.
   * 
   * @param name attribute name.
   * @param defaultValue default value, null or empty accepted.
   * @return attribute value or default value.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public String getAttribute(String name, String defaultValue)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    return getAttribute(name, String.class, defaultValue);
  }

  /**
   * Get attribute value converted to requested type or null if there is no attribute with requested name.
   * 
   * @param name attribute name,
   * @param type type to converter attribute value to.
   * @param <T> type to convert attribute value.
   * @return newly created value object or null if named attribute not found.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>type</code> argument is null.
   * @throws ConverterException if there is no converter registered for value type or value parse fails.
   */
  public <T> T getAttribute(String name, Class<T> type)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    Params.notNull(type, "Attribute type");
    return getAttribute(name, type, null);
  }

  /**
   * Get attribute value converted to requested type or default value if there is no attribute with requested name. If
   * given default value is null and attribute is not found this method still returns null, that is, the requested
   * default value.
   * 
   * @param name attribute name,
   * @param type type to converter attribute value to,
   * @param defaultValue default value.
   * @param <T> type to convert attribute value to.
   * @return newly created value object instance or default value.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>type</code> argument is null.
   * @throws ConverterException if there is no converter registered for value type or value parse fails.
   */
  public <T> T getAttribute(String name, Class<T> type, T defaultValue)
  {
    Params.notNullOrEmpty(name, "Attribute name");
    Params.notNull(type, "Attribute type");
    String value = attributes.get(name);
    return value != null ? converter.asObject(value, type) : defaultValue;
  }

  /**
   * Get configuration object Java raw properties. This method returns reference to internal properties storage that can
   * be manipulated directly. Changes on returned reference does affect configuration object internal properties.
   * 
   * @return configuration object properties storage.
   */
  public Properties getProperties()
  {
    return properties;
  }

  /**
   * Get properties not yet used at the moment this method is executed. A method is considered used if is read at least
   * once by {@link #getProperty(String)} or related. This method returns a new properties instance. Changes on returned
   * instance does not affect configuration object internal properties.
   * 
   * @return yet unused properties.
   */
  public Properties getUnusedProperties()
  {
    Properties p = new Properties();
    for(Map.Entry<Object, Object> entry : properties.entrySet()) {
      if(!usedProperties.contains(entry.getKey())) {
        p.put(entry.getKey(), entry.getValue());
      }
    }
    return p;
  }

  /**
   * Test if configuration object has named property.
   * 
   * @param name property name.
   * @return true if configuration object has property.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public boolean hasProperty(String name)
  {
    Params.notNullOrEmpty(name, "Property name");
    return properties.containsKey(name);
  }

  /**
   * Get configuration object property value or null if there is no property with requested name.
   * 
   * @param name property name.
   * @return configuration object property value or null.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public String getProperty(String name)
  {
    Params.notNullOrEmpty(name, "Property name");
    usedProperties.add(name);
    return properties.getProperty(name);
  }

  /**
   * Get configuration object property value or default value if there is no property with requested name.
   * 
   * @param name property name,
   * @param defaultValue default value.
   * @return configuration object property value or given default value.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public String getProperty(String name, String defaultValue)
  {
    String value = getProperty(name);
    return value != null ? value : defaultValue;
  }

  /**
   * Get configuration object property converter to requested type or null if there is no property with given name.
   * 
   * @param name property name.
   * @param type type to convert property value to.
   * @param <T> value type.
   * @return newly created value object or null.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>type</code> argument is null.
   * @throws ConverterException if there is no converter registered for value type or value parse fails.
   */
  public <T> T getProperty(String name, Class<T> type)
  {
    return getProperty(name, type, null);
  }

  /**
   * Get configuration object property converter to requested type or default value if there is no property with given
   * name.
   * 
   * @param name property name.
   * @param type type to convert property value to,
   * @param defaultValue default value, possible null or empty.
   * @param <T> value type.
   * @return newly created value object or default value.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   * @throws IllegalArgumentException if <code>type</code> argument is null.
   * @throws ConverterException if there is no converter registered for value type or value parse fails.
   */
  public <T> T getProperty(String name, Class<T> type, T defaultValue)
  {
    Params.notNullOrEmpty(name, "Property name");
    Params.notNull(type, "Property type");
    String value = getProperty(name);
    if(value != null) {
      return converter.asObject(value, type);
    }
    return defaultValue;
  }

  /**
   * Test if configuration object has children.
   * 
   * @return true if configuration object has children.
   */
  public boolean hasChildren()
  {
    return !children.isEmpty();
  }

  /**
   * Get configuration object children count.
   * 
   * @return configuration object children count.
   */
  public int getChildrenCount()
  {
    return children.size();
  }

  /**
   * Get configuration object children list.
   * 
   * @return configuration object children list.
   */
  public List<Config> getChildren()
  {
    return children;
  }

  /**
   * Test if configuration object has at least a child with requested name.
   * 
   * @param name child name.
   * @return true if configuration object has at least one child with requested name.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public boolean hasChild(String name)
  {
    Params.notNullOrEmpty(name, "Child name");
    for(Config child : children) {
      if(child.name.equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get configuration object first child with requested name or null if there is no child with given name.
   * 
   * @param name child name.
   * @return configuration object child or null.
   * @throws IllegalArgumentException if <code>name</code> argument is null or empty.
   */
  public Config getChild(String name)
  {
    Params.notNullOrEmpty(name, "Child name");
    for(Config child : children) {
      if(child.name.equals(name)) {
        return child;
      }
    }
    return null;
  }

  /**
   * Get configuration object child by index. Given index should be in range so that invoking this method on empty
   * children list will throw exception.
   * 
   * @param index child index.
   * @return configuration object child at requested index.
   * @throws IllegalArgumentException if <code>index</code> argument is not in range.
   */
  public Config getChild(int index)
  {
    Params.range(index, 0, children.size(), "Index");
    return children.get(index);
  }

  /**
   * Find configuration object children with requested names.
   * 
   * @param name one or more child names.
   * @return configuration object children with requested names, possible empty.
   * @throws IllegalArgumentException if <code>name</code> list does not contains at least one item.
   */
  public List<Config> findChildren(String... name)
  {
    Params.notNullOrEmpty(name, "Children names");
    List<String> names = Arrays.asList(name);
    List<Config> results = new ArrayList<Config>();
    for(Config child : children) {
      if(names.contains(child.name)) {
        results.add(child);
      }
    }
    return results;
  }

  /**
   * Get this configuration object string value, possible null.
   * 
   * @return configuration object string value, possible null.
   * @see #value
   */
  public String getValue()
  {
    return value;
  }

  /**
   * Get this configuration object value converted to requested type. Returns null if this configuration object has no
   * value.
   * 
   * @param type desired value object type.
   * @param <T> value type.
   * @return newly created value object or null.
   * @throws IllegalArgumentException if <code>type</code> argument is null.
   * @throws ConverterException if there is no converter registered for value type or value parse fails.
   */
  public <T> T getValue(Class<T> type)
  {
    Params.notNull(type, "Value type");
    if(value == null) {
      return null;
    }
    return converter.asObject(value, type);
  }

  /**
   * Get named child string value or null if child not found or it has no value.
   * 
   * @param name name of the child to retrieve value from.
   * @return named child string value, possible null.
   */
  public String getChildValue(String name)
  {
    Config child = getChild(name);
    return child != null ? child.getValue() : null;
  }

  /** Dump configuration object tree to standard out, for debugging purposes. */
  public void dump()
  {
    print(this, 0);
  }

  /**
   * Recursively print configuration object tree to standard out.
   * 
   * @param config configuration object,
   * @param indent indentation index.
   */
  private void print(Config config, int indent)
  {
    for(int i = 0; i < indent; ++i) {
      System.out.print("\t");
    }
    System.out.print(config.name);
    System.out.print("\r\n");

    for(Config child : config.children) {
      print(child, indent + 1);
    }
  }

  @Override
  public String toString()
  {
    return name;
  }
}
