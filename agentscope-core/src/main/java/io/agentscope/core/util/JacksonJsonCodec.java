/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.agentscope.core.util;

import java.lang.reflect.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Jackson-based implementation of {@link JsonCodec}.
 *
 * <p>This is the default implementation used by {@link JsonUtils}. It uses
 * Jackson's ObjectMapper with the following configuration:
 * <ul>
 *   <li>{@code FAIL_ON_UNKNOWN_PROPERTIES = false} - allows unknown fields in JSON</li>
 * </ul>
 *
 * <p>Users can access the underlying ObjectMapper via {@link #objectMapper ()}
 * for advanced operations not covered by the JsonCodec interface.
 *
 * @see JsonCodec
 * @see JsonUtils
 */
public record JacksonJsonCodec(ObjectMapper objectMapper) implements JsonCodec {

  private static final Logger log = LoggerFactory.getLogger(JacksonJsonCodec.class);

  /**
   * Creates a new JacksonJsonCodec with default ObjectMapper configuration.
   */
  public JacksonJsonCodec() {
    this(createDefaultObjectMapper());
  }

  /**
   * Creates a new JacksonJsonCodec with a custom ObjectMapper.
   *
   * @param objectMapper the ObjectMapper to use
   */
  public JacksonJsonCodec {
  }

  /**
   * Creates the default ObjectMapper with standard configuration.
   *
   * @return configured ObjectMapper
   */
  private static ObjectMapper createDefaultObjectMapper() {
    return JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(new JavaTimeModule()).build();
  }

  /**
   * Get the underlying ObjectMapper for advanced operations.
   *
   * @return the ObjectMapper instance
   */
  @Override
  public ObjectMapper objectMapper() {
    return objectMapper;
  }

  @Override
  public String toJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonException e) {
      log.error("Failed to serialize object to JSON: {}", e.getMessage(), e);
      throw new JsonException("Failed to serialize object to JSON", e);
    }
  }

  @Override
  public String toPrettyJson(Object obj) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonException e) {
      log.error("Failed to serialize object to pretty JSON: {}", e.getMessage(), e);
      throw new JsonException("Failed to serialize object to pretty JSON", e);
    }
  }

  @Override
  public <T> T fromJson(String json, Class<T> type) {
    try {
      return objectMapper.readValue(json, type);
    } catch (JsonException e) {
      throw new JsonException("Failed to deserialize JSON to " + type.getName(), e);
    }
  }

  @Override
  public <T> T fromJson(String json, TypeReference<T> typeRef) {
    try {
      return objectMapper.readValue(json, typeRef);
    } catch (JsonException e) {
      throw new JsonException("Failed to deserialize JSON", e);
    }
  }

  @Override
  public <T> T convertValue(Object from, Class<T> toType) {
    try {
      return objectMapper.convertValue(from, toType);
    } catch (IllegalArgumentException e) {
      throw new JsonException("Failed to convert value to " + toType.getName(), e);
    }
  }

  @Override
  public <T> T convertValue(Object from, TypeReference<T> toTypeRef) {
    try {
      return objectMapper.convertValue(from, toTypeRef);
    } catch (IllegalArgumentException e) {
      throw new JsonException("Failed to convert value", e);
    }
  }

  @Override
  public Object convertValue(Object from, Type toType) {
    try {
      JavaType javaType = objectMapper.getTypeFactory().constructType(toType);
      return objectMapper.convertValue(from, javaType);
    } catch (IllegalArgumentException e) {
      throw new JsonException("Failed to convert value to " + toType.getTypeName(), e);
    }
  }
}
