package org.exoplatform.analytics.es;

import static org.exoplatform.analytics.es.ESAnalyticsUtils.ES_INDEX_PLACEHOLDER;
import static org.exoplatform.analytics.es.ESAnalyticsUtils.ES_TYPE;
import static org.exoplatform.analytics.utils.AnalyticsUtils.*;

import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.api.service.StatisticDataQueueService;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.queue.service.DummyStatisticDataQueueService;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class AnalyticsIndexingServiceConnector extends ElasticIndexingServiceConnector {

  private static final long   serialVersionUID        = -3143010828698498081L;

  private static final Log    LOG                     = ExoLogger.getLogger(DummyStatisticDataQueueService.class);

  private static final String MAPPING_FILE_PATH_PARAM = "mapping.file.path";

  private transient StatisticDataQueueService analyticsQueueService;

  private String              esInitialMapping;

  public AnalyticsIndexingServiceConnector(ConfigurationManager configurationManager,
                                           StatisticDataQueueService analyticsQueueService,
                                           InitParams initParams) {
    super(initParams);

    this.analyticsQueueService = analyticsQueueService;

    if (initParams != null && initParams.containsKey(MAPPING_FILE_PATH_PARAM)) {
      String mappingFilePath = initParams.getValueParam(MAPPING_FILE_PATH_PARAM).getValue();
      try {
        InputStream mappingFileIS = configurationManager.getInputStream(mappingFilePath);
        this.esInitialMapping = IOUtil.getStreamContentAsString(mappingFileIS);
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch index mapping from path {}", mappingFilePath, e);
      }
    } else {
      LOG.error("Empty elasticsearch index mapping file path parameter");
    }
  }

  @Override
  public String getIndex() {
    return ES_INDEX_PLACEHOLDER;
  }

  @Override
  public String getType() {
    return ES_TYPE;
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
    return data == null ? null : create(data);
  }

  public Document create(StatisticData data) {
    String timestampString = String.valueOf(data.getTimestamp());

    Map<String, String> fields = new HashMap<>();
    fields.put(FIELD_TIMESTAMP, timestampString);
    fields.put(FIELD_USER_ID, String.valueOf(data.getUserId()));
    fields.put(FIELD_SPACE_ID, String.valueOf(data.getSpaceId()));
    fields.put(FIELD_MODULE, data.getModule());
    fields.put(FIELD_SUB_MODULE, data.getSubModule());
    fields.put(FIELD_OPERATION, data.getOperation());
    fields.put(FIELD_STATUS, String.valueOf(data.getStatus().ordinal()));
    fields.put(FIELD_ERROR_CODE, String.valueOf(data.getErrorCode()));
    fields.put(FIELD_ERROR_MESSAGE, data.getErrorMessage());
    fields.put(FIELD_IS_ANALYTICS, "true");
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

  @Override
  public Document update(String id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    throw new UnsupportedOperationException();
  }

}
