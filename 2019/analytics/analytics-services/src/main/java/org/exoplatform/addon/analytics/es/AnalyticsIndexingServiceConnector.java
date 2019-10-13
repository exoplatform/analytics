package org.exoplatform.addon.analytics.es;

import static org.exoplatform.addon.analytics.utils.AnalyticsUtils.*;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.addon.analytics.api.service.AnalyticsQueueService;
import org.exoplatform.addon.analytics.dummy.queue.DummyAnalyticsQueueService;
import org.exoplatform.addon.analytics.model.StatisticData;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsIndexingServiceConnector extends ElasticIndexingServiceConnector {

  private static final long               serialVersionUID        = -8492300786563730708L;

  private static final Log                LOG                     = ExoLogger.getLogger(DummyAnalyticsQueueService.class);

  public static final String              ES_INDEX                = "analytics_v1";

  public static final String              ES_TYPE                 = "analytics";

  public static final String              ES_ALIAS                = "analytics_alias";

  private static final String             MAPPING_FILE_PATH_PARAM = "mapping.file.path";

  private transient AnalyticsQueueService analyticsQueueService;

  private String                          elastcisearchMapping;

  public AnalyticsIndexingServiceConnector(ConfigurationManager configurationManager,
                                           AnalyticsQueueService analyticsQueueService,
                                           InitParams initParams) {
    super(initParams);

    this.analyticsQueueService = analyticsQueueService;

    if (initParams != null && initParams.containsKey(MAPPING_FILE_PATH_PARAM)) {
      String mappingFilePath = initParams.getValueParam(MAPPING_FILE_PATH_PARAM).getValue();
      try {
        InputStream mappingFileIS = configurationManager.getInputStream(mappingFilePath);
        this.elastcisearchMapping = IOUtil.getStreamContentAsString(mappingFileIS);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch index mapping from path {}", mappingFilePath, e);
      }
    } else {
      LOG.error("Empty elasticsearch index mapping file path parameter");
    }
  }

  @Override
  public Document create(String timestampString) {
    if (StringUtils.isBlank(timestampString)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    long timestamp = Long.parseLong(timestampString);
    StatisticData data = this.analyticsQueueService.get(timestamp);
    return data == null ? null : create(data);
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

  private Document create(StatisticData data) {
    String timestampString = String.valueOf(data.getTimestamp());

    Map<String, String> fields = new HashMap<>();
    fields.put(FIELD_TIMESTAMP, timestampString);
    fields.put(FIELD_YEAR, String.valueOf(data.getYear()));
    fields.put(FIELD_MONTH, String.valueOf(data.getMonth()));
    fields.put(FIELD_WEEK, String.valueOf(data.getWeek()));
    fields.put(FIELD_DAY_OF_MONTH, String.valueOf(data.getDayOfMonth()));
    fields.put(FIELD_DAY_OF_WEEK, String.valueOf(data.getDayOfWeek()));
    fields.put(FIELD_DAY_OF_YEAR, String.valueOf(data.getDayOfYear()));
    fields.put(FIELD_HOUR, String.valueOf(data.getHour()));
    fields.put(FIELD_USER_ID, String.valueOf(data.getUserId()));
    fields.put(FIELD_SPACE_ID, String.valueOf(data.getSpaceId()));
    fields.put(FIELD_MODULE, data.getModule());
    fields.put(FIELD_SUB_MODULE, data.getSubModule());
    fields.put(FIELD_OPERATION, data.getModule());
    fields.put(FIELD_STATUS, String.valueOf(data.getStatus().ordinal()));
    fields.put(FIELD_ERROR_CODE, String.valueOf(data.getErrorCode()));
    fields.put(FIELD_ERROR_MESSAGE, data.getErrorMessage());
    if (data.getParameters() != null && !data.getParameters().isEmpty()) {
      fields.putAll(data.getParameters());
    }
    return new Document(ES_TYPE,
                        timestampString,
                        null,
                        null,
                        (Set<String>) null,
                        fields);
  }

}
