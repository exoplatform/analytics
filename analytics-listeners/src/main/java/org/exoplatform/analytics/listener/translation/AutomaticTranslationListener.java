/*
 * Copyright (C) 2021 eXo Platform SAS.
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
package org.exoplatform.analytics.listener.translation;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.automatic.translation.api.dto.AutomaticTranslationEvent;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
