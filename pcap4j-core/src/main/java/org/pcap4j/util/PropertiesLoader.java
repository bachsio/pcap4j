/*_##########################################################################
  _##
  _##  Copyright (C) 2011-2012  Kaito Yamada
  _##
  _##########################################################################
*/

package org.pcap4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.1
 */
public class PropertiesLoader {

  private static final Logger logger
    = LoggerFactory.getLogger(PropertiesLoader.class);

  private final String resourceName;
  private final boolean systemPropertiesOverPropertiesFile;
  private final boolean caching;
  private final Properties prop = new Properties();

  private final Map<String, Object> cache = new HashMap<String, Object>();

  /**
   *
   * @param resourceName
   * @param systemPropertiesOverPropertiesFile
   * @param caching
   */
  public PropertiesLoader(
    String resourceName,
    boolean systemPropertiesOverPropertiesFile,
    boolean caching
  ) {
    this.resourceName = resourceName;
    this.systemPropertiesOverPropertiesFile = systemPropertiesOverPropertiesFile;
    this.caching = caching;

    InputStream in
      = this.getClass().getClassLoader().getResourceAsStream(resourceName);
    if (in == null) {
      logger.warn("{} not found.", resourceName);
      return;
    }

    try {
      this.prop.load(in);
    } catch (IOException e) {
      logger.error("Exception follows", e);
    }
  }

  /**
   *
   * @return resource name
   */
  public final String getResourceName() {
    return resourceName;
  }

  /**
   *
   * @return a new Properties object containing properties
   *         loaded by this object.
   */
  public final Properties getProp() {
    Properties copy = new Properties();
    copy.putAll(prop);
    return copy;
  }

  /**
   *
   * @return true if this object gives priority to the system properties
   *         over the properties loaded by this object; false otherwise.
   */
  public final boolean isSystemPropertiesOverPropertiesFile() {
    return systemPropertiesOverPropertiesFile;
  }

  /**
   *
   * @return true if this object is caching values of properties;
   *         false otherwise.
   */
  public final boolean isCaching() {
    return caching;
  }

  /**
   *
   * @param key
   * @param defaultValue
   * @return a string value with a specified key value.
   */
  public String getString(String key, String defaultValue) {
    synchronized (cache) {
      if (caching && cache.containsKey(key)) {
        String cacheValue = ((String)cache.get(key));
        logger.debug(
          "[{}] Got {} from cache for {}",
          new Object[] {resourceName, cacheValue, key}
        );
        return cacheValue;
      }

      String value = null;

      if (systemPropertiesOverPropertiesFile) {
        value = System.getProperty(key);
      }

      if (value != null) {
        logger.info(
          "[System properties] Got {} for {}", value, key
        );
      }
      else {
        value = prop.getProperty(key);

        if (value != null) {
          logger.info(
            "[{}] Got {} for {}",
            new Object[] {resourceName, value, key}
          );
        }
        else {
          logger.warn(
            "[{}] Could not get value for {}, use default value: {}",
            new Object[] {resourceName, key, defaultValue}
          );
          value = defaultValue;
        }
      }

      if (caching) {
        cache.put(key, value);
      }

      return value;
    }
  }

  /**
   *
   * @param key
   * @param defaultValue
   * @return an int value representing a string with the specified key value.
   */
  public int getInteger(String key, Integer defaultValue) {
    synchronized (cache) {
      if (caching && cache.containsKey(key)) {
        Integer cacheValue = (Integer)cache.get(key);
        logger.debug(
          "[{}] Got {} from cache for {}",
          new Object[] {resourceName, cacheValue, key}
        );
        return cacheValue;
      }

      Integer value = null;

      if (systemPropertiesOverPropertiesFile) {
        value = Integer.getInteger(key);
      }

      if (value != null) {
        logger.info(
          "[System properties] Got {} for {}", value, key
        );
      }
      else {
        String strValue = prop.getProperty(key);

        if (strValue != null) {
          try {
            value = Integer.decode(strValue);
            logger.info(
              "[{}] Got {} for {}",
              new Object[] {resourceName, value, key}
            );
          } catch (NumberFormatException e) {
            logger.warn(
              "[{}] {} is invalid for {}, use default value: {}",
              new Object[] {resourceName, strValue, key, defaultValue}
            );
            value = defaultValue;
          }
        }
        else {
          logger.warn(
            "[{}] Could not get value for {}, use default value: {}",
            new Object[] {resourceName, key, defaultValue}
          );
          value = defaultValue;
        }
      }

      if (caching) {
        cache.put(key, value);
      }

      return value;
    }
  }

  /**
   *
   * @param key
   * @param defaultValue
   * @return a Boolean object representing a string
   *         with the specified key value.
   */
  public Boolean getBoolean(String key, Boolean defaultValue) {
    synchronized (cache) {
      if (caching && cache.containsKey(key)) {
        Boolean cacheValue = (Boolean)cache.get(key);
        logger.debug(
          "[{}] Got {} from cache for {}",
          new Object[] {resourceName, cacheValue, key}
        );
        return cacheValue;
      }

      Boolean value = null;

      if (systemPropertiesOverPropertiesFile) {
        String strValue = System.getProperty(key);

        if (strValue != null) {
          value = Boolean.valueOf(strValue);
          logger.info(
            "[System properties] Got \"{}\" means {} for {}",
            new Object[] {strValue, value, key}
          );
        }
      }

      if (value == null) {
        String strValue = prop.getProperty(key);

        if (strValue != null) {
          value = Boolean.valueOf(strValue);
          logger.info(
            "[{}] Got\"{}\" means {} for {}",
            new Object[] {resourceName, strValue, value, key}
          );
        }
        else {
          logger.warn(
            "[{}] Could not get value for {}, use default value: {}",
            new Object[] {resourceName, key, defaultValue}
          );
          value = defaultValue;
        }
      }

      if (caching) {
        cache.put(key, value);
      }

      return value;
    }
  }

  /**
   *
   * @param key
   * @param defaultValue
   * @return a Class object representing a string with the specified key value.
   */
  public <T> Class<? extends T> getClass(
    String key, Class<? extends T> defaultValue
  ) {
    synchronized (cache) {
      if (caching && cache.containsKey(key)) {
        @SuppressWarnings("unchecked")
        Class<? extends T> cacheValue = (Class<? extends T>)cache.get(key);
        logger.debug(
          "[{}] Got {} from cache for {}",
          new Object[] {resourceName, cacheValue, key}
        );
        return cacheValue;
      }

      Class<? extends T> value = null;

      if (systemPropertiesOverPropertiesFile) {
        String strValue = System.getProperty(key);

        if (strValue != null) {
          try {
            @SuppressWarnings("unchecked")
            Class<? extends T> clazz
              = (Class<? extends T>)Class.forName(strValue);
            value = clazz;

            logger.info(
              "[System properties] Got {} for {}", strValue, key
            );
          } catch (ClassNotFoundException e) {
            logger.error(
              "[System properties] Got Invalid value: {} for {}, ignore it.",
                strValue, key
            );
          } catch (ClassCastException e) {
            logger.error(
              "[System properties] Got Invalid value: {} for {}, ignore it.",
                strValue, key
            );
          }
        }
      }

      if (value == null) {
        String strValue = prop.getProperty(key);

        if (strValue != null) {
          try {
            @SuppressWarnings("unchecked")
            Class<? extends T> clazz
              = (Class<? extends T>)Class.forName(strValue);
            value = clazz;
            logger.info(
              "[{}] Got {} for {}",
              new Object[] {resourceName, strValue, key}
            );
          } catch (ClassNotFoundException e) {
            logger.warn(
              "[{}] {} is invalid for {}, use default value: {}",
              new Object[] {resourceName, strValue, key, defaultValue}
            );
            value = defaultValue;
          } catch (ClassCastException e) {
            logger.warn(
              "[{}] {} is invalid for {}, use default value: {}",
              new Object[] {resourceName, strValue, key, defaultValue}
            );
            value = defaultValue;
          }
        }
        else {
          logger.warn(
            "[{}] Could not get value for {}, use default value: {}",
            new Object[] {resourceName, key, defaultValue}
          );
          value = defaultValue;
        }
      }

      if (caching) {
        cache.put(key, value);
      }

      return value;
    }
  }

  /**
   *
   * @param key
   * @param defaultValue
   * @return an InetAddress object representing a string
   *         with the specified key value.
   */
  public InetAddress getInetAddress(String key, InetAddress defaultValue) {
    synchronized (cache) {
      if (caching && cache.containsKey(key)) {
        InetAddress cacheValue = (InetAddress)cache.get(key);
        logger.debug(
          "[{}] Got {} from cache for {}",
          new Object[] {resourceName, cacheValue, key}
        );
        return cacheValue;
      }

      InetAddress value = null;

      if (systemPropertiesOverPropertiesFile) {
        String strValue = System.getProperty(key);

        if (strValue != null) {
          try {
            value = InetAddress.getByName(strValue);
          } catch (UnknownHostException e) {
            logger.error(
              "[System properties] Got Invalid value: {} for {}, ignore it.",
                strValue, key
            );
          }

          logger.info(
            "[System properties] Got \"{}\" means {} for {}",
            new Object[] {strValue, value, key}
          );
        }
      }

      if (value == null) {
        String strValue = prop.getProperty(key);

        if (strValue != null) {
          try {
            value = InetAddress.getByName(strValue);
          } catch (UnknownHostException e) {
            logger.warn(
              "[{}] {} is invalid for {}, use default value: {}",
              new Object[] {resourceName, strValue, key, defaultValue}
            );
            value = defaultValue;
          }

          logger.info(
            "[{}] Got\"{}\" means {} for {}",
            new Object[] {resourceName, strValue, value, key}
          );
        }
        else {
          logger.warn(
            "[{}] Could not get value for {}, use default value: {}",
            new Object[] {resourceName, key, defaultValue}
          );
          value = defaultValue;
        }
      }

      if (caching) {
        cache.put(key, value);
      }

      return value;
    }
  }

  /**
   *
   */
  public final void clearCache() {
    synchronized (cache) {
      cache.clear();
    }
  }

}
