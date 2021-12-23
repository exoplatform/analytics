package org.exoplatform.analytics.listener.wiki;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.NoteService;
import org.exoplatform.wiki.utils.Utils;

public class NotesMetadataListener extends Listener<Long, MetadataItem> {

  private final NoteService  noteService;

  private final SpaceService spaceService;

  public NotesMetadataListener(NoteService noteService, SpaceService spaceService) {
    this.noteService = noteService;
    this.spaceService = spaceService;
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    ConversationState conversationstate = ConversationState.getCurrent();
    Identity currentIdentity = conversationstate == null
        || conversationstate.getIdentity() == null ? null : conversationstate.getIdentity();
    MetadataItem metadataItem = event.getData();
    String objectType = event.getData().getObjectType();
    String objectId = metadataItem.getObjectId();
    if (StringUtils.equals(objectType, Utils.NOTES_METADATA_OBJECT_TYPE) && currentIdentity != null) {
      Page note = noteService.getNoteById(objectId);
      if (note != null) {
        StatisticData statisticData = new StatisticData();
        statisticData.setModule("portal");
        statisticData.setSubModule("ui");
        statisticData.setOperation("Bookmark");
        statisticData.addParameter("type", "Notes");
        if (StringUtils.isNotBlank(note.getWikiOwner())) {
          Space space = spaceService.getSpaceByGroupId(note.getWikiOwner());
          statisticData.setSpaceId(Long.parseLong(space.getId()));
        }
        statisticData.addParameter("wikiPageId", note.getId());
        statisticData.setUserId(AnalyticsUtils.getUserIdentityId(currentIdentity.getUserId()));

        AnalyticsUtils.addStatisticData(statisticData);
      }

    }
  }
}
