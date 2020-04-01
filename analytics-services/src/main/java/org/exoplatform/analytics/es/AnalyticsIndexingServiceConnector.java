package org.exoplatform.analytics.es;

import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.api.service.StatisticDataQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsIndexingServiceConnector extends ElasticIndexingServiceConnector {

  private static final long                   serialVersionUID                = -3143010828698498081L;

  private static final Log                    LOG                             =
                                                  ExoLogger.getLogger(AnalyticsIndexingServiceConnector.class);

  private static final String                 MAPPING_FILE_PATH_PARAM         = "mapping.file.path";

  private static final String                 ES_INDEX_PLACEHOLDER            = "@ES_INDEX_PLACEHOLDER@";

  private static final String                 DEFAULT_ES_ANALYTICS_INDEX_NAME = "analytics";

  private static final String                 ES_ANALYTICS_TYPE               = "analytics";

  private static final String                 ES_ANALYTICS_INDEX_PER_DAYS     = "exo.es.analytics.index.per.days";

  private static final String                 ES_ANALYTICS_INDEX_PREFIX       = "exo.es.analytics.index.prefix";

  private static final long                   DAY_IN_MS                       = 86400000L;

  private transient StatisticDataQueueService analyticsQueueService;

  private String                              esInitialMapping;

  private String                              indexPrefix;

  private int                                 indexPerDays;

  public AnalyticsIndexingServiceConnector(ConfigurationManager configurationManager,
                                           StatisticDataQueueService analyticsQueueService,
                                           InitParams initParams) {
    super(initParams);

    this.analyticsQueueService = analyticsQueueService;

    if (initParams != null) {
      if (initParams.containsKey(MAPPING_FILE_PATH_PARAM)) {
        String mappingFilePath = initParams.getValueParam(MAPPING_FILE_PATH_PARAM).getValue();
        try {
          InputStream mappingFileIS = configurationManager.getInputStream(mappingFilePath);
          this.esInitialMapping = IOUtil.getStreamContentAsString(mappingFileIS);
        } catch (Exception e) {
          LOG.error("Can't read elasticsearch index mapping from path {}", mappingFilePath, e);
        }
      }
      if (initParams.containsKey(ES_ANALYTICS_INDEX_PER_DAYS)) {
        this.indexPerDays = Integer.parseInt(initParams.getValueParam(ES_ANALYTICS_INDEX_PER_DAYS).getValue());
      }
      if (initParams.containsKey(ES_ANALYTICS_INDEX_PREFIX)) {
        this.indexPrefix = initParams.getValueParam(ES_ANALYTICS_INDEX_PREFIX).getValue();
      }
    }
    if (StringUtils.isBlank(this.indexPrefix)) {
      this.indexPrefix = DEFAULT_ES_ANALYTICS_INDEX_NAME;
    }
    if (StringUtils.isBlank(this.esInitialMapping)) {
      LOG.error("Empty elasticsearch index mapping file path parameter");
    }
  }

  @Override
  public String getIndex() {
    return ES_INDEX_PLACEHOLDER;
  }

  @Override
  public String getType() {
    return ES_ANALYTICS_TYPE;
  }

  @Override
  public String getMapping() {
    return esInitialMapping;
  }

  @Override
  public Document create(String idString) {
    if (StringUtils.isBlank(idString)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    long id = Long.parseLong(idString);
    StatisticData data = this.analyticsQueueService.get(id);
    if (data == null) {
      LOG.warn("Can't find document with id {}", id);
      return null;
    }
    String timestampString = String.valueOf(data.getTimestamp());

    Map<String, String> fields = new HashMap<>();
    fields.put("id", idString);
    fields.put(FIELD_TIMESTAMP, timestampString);
    fields.put(FIELD_USER_ID, String.valueOf(data.getUserId()));
    fields.put(FIELD_SPACE_ID, String.valueOf(data.getSpaceId()));
    fields.put(FIELD_MODULE, data.getModule());
    fields.put(FIELD_SUB_MODULE, data.getSubModule());
    fields.put(FIELD_OPERATION, data.getOperation());
    fields.put(FIELD_STATUS, String.valueOf(data.getStatus().ordinal()));
    fields.put(FIELD_ERROR_CODE, String.valueOf(data.getErrorCode()));
    fields.put(FIELD_ERROR_MESSAGE, data.getErrorMessage());
    fields.put(FIELD_DURATION, String.valueOf(data.getDuration()));
    fields.put(FIELD_IS_ANALYTICS, "true");
    if (data.getParameters() != null && !data.getParameters().isEmpty()) {
      fields.putAll(data.getParameters());
    }
    return new Document(DEFAULT_ES_ANALYTICS_INDEX_NAME,
                        String.valueOf(id),
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
    throw new UnsupportedOperationException();
  }

  public final String getIndex(long timestamp) {
    if (indexPerDays > 0) {
      long indexSuffix = timestamp / (DAY_IN_MS * indexPerDays);
      return this.indexPrefix + "_" + indexSuffix;
    } else {
      return this.indexPrefix;
    }
  }

  public String replaceByIndexName(String esQuery, String index) {
    return esQuery.replace(ES_INDEX_PLACEHOLDER, index);
  }

}
