/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addon.analytics.service.es;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;

public class AnalyticsIndexingServiceConnector extends ElasticIndexingServiceConnector {

  private static final long                             serialVersionUID         = -8492300786563730708L;

  public static final String                            ES_INDEX                 = "analytics_v1";

  public static final String                            ES_TYPE                  = "analytics";

  public static final String                            ES_ALIAS                 = "analytics_alias";

  private static final String                           MAPPING_FILE_PATH_PARAM  = "mapping.file.path";

  private transient ElasticSearchStatisticDataProcessor esStatisticDataProcessor = null;

  private String                                        elastcisearchMapping;

  public AnalyticsIndexingServiceConnector(ElasticSearchStatisticDataProcessor esStatisticDataProcessor,
                                           ConfigurationManager configurationManager,
                                           InitParams initParams) {
    super(initParams);

    this.esStatisticDataProcessor = esStatisticDataProcessor;

    if (initParams != null && initParams.containsKey(MAPPING_FILE_PATH_PARAM)) {
      String mappingFilePath = initParams.getValueParam(MAPPING_FILE_PATH_PARAM).getValue();
      try {
        InputStream mappingFileIS = configurationManager.getInputStream(mappingFilePath);
        this.elastcisearchMapping = IOUtil.getStreamContentAsString(mappingFileIS);
      } catch (Exception e) {
        throw new IllegalStateException("Can't read elasticsearch index mapping from path " + mappingFilePath, e);
      }
    } else {
      throw new IllegalStateException("Empty elasticsearch index mapping file path");
    }
  }

  @Override
  public Document create(String timestamp) {
    if (StringUtils.isBlank(timestamp)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    StatisticData data = esStatisticDataProcessor.getStatisticData(Long.parseLong(timestamp));

    Map<String, String> fields = new HashMap<>();
    fields.put("module", data.getModule());
    fields.put("subModule", data.getSubModule());
    fields.put("operation", data.getModule());
    fields.put("status", String.valueOf(data.getStatus().ordinal()));
    fields.put("userId", String.valueOf(data.getUserId()));
    fields.put("spaceId", String.valueOf(data.getSpaceId()));
    fields.put("errorMessage", data.getErrorMessage());
    fields.put("errorCode", String.valueOf(data.getErrorCode()));
    if (data.getParameters() != null && !data.getParameters().isEmpty()) {
      fields.putAll(data.getParameters());
    }
    return new Document(ES_TYPE,
                        timestamp,
                        null,
                        null,
                        (Set<String>) null,
                        fields);
  }

  @Override
  public Document update(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    return Collections.emptyList();
  }

  @Override
  public String getMapping() {
    return elastcisearchMapping;
  }

}
