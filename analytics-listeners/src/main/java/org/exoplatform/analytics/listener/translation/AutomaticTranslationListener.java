package org.exoplatform.analytics.listener.translation;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationEvent;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addSpaceStatistics;

public class AutomaticTranslationListener  extends Listener<String,AutomaticTranslationEvent> {

  private static final Log LOG = ExoLogger.getLogger(AutomaticTranslationListener.class);

  @Override
  public void onEvent(Event<String, AutomaticTranslationEvent> event) throws Exception {
    AutomaticTranslationEvent data = event.getData();
    StatisticData statisticData = new StatisticData();
    statisticData.setModule("automatic-translation");
    statisticData.setSubModule("automatic-translation");
    statisticData.setOperation("translate");
    statisticData.setUserId(AnalyticsUtils.getCurrentUserIdentityId());
    statisticData.setSpaceId(data.getSpaceId());
    statisticData.addParameter("messageLength", data.getMessageLength());
    statisticData.addParameter("targetLanguage", data.getTargetLanguage());
    statisticData.addParameter("contentType", data.getContentType());
    statisticData.addParameter("connector", data.getConnectorName());
    AnalyticsUtils.addStatisticData(statisticData);
  }
}
