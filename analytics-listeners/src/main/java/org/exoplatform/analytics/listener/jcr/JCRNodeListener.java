package org.exoplatform.analytics.listener.jcr;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import javax.jcr.*;

import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class JCRNodeListener implements Action {

  private static final Log                      LOG                            = ExoLogger.getLogger(JCRNodeListener.class);

  private static final String                   DEFAULT_CHARSET                = Charset.defaultCharset().name();

  private static final ScheduledExecutorService SCHEDULER                      = new ScheduledThreadPoolExecutor(0);        // NOSONAR

  private static final Executor                 DEFAULT_DELAYED_EXECUTOR       = delayedExecutor(10, TimeUnit.SECONDS);

  private static final Map<String, String>      CURRENTLY_PROCESSING_NODE_PATH = new HashMap<>();

  private PortalContainer                       container;

  private TemplateService                       templateService;

  private SpaceService                          spaceService;

  public JCRNodeListener() {
    this.container = PortalContainer.getInstance();
  }

  public boolean execute(Context context) throws Exception {
    String username = AnalyticsUtils.getUsername(ConversationState.getCurrent());
    boolean unkownUser = AnalyticsUtils.isUnkownUser(username);
    if (unkownUser) {
      return true;
    }

    Object item = context.get("currentItem");
    Node node = (item instanceof Property) ? ((Property) item).getParent() : (Node) item;
    String nodePath = node.getPath();

    String currentlyProcessingPath = CURRENTLY_PROCESSING_NODE_PATH.get(username);
    if (currentlyProcessingPath != null && (StringUtils.startsWith(currentlyProcessingPath, nodePath)
        || StringUtils.startsWith(nodePath, currentlyProcessingPath))) {
      // Ignore multiple action invocations when adding new node or updating a
      // node
      return true;
    }
    CURRENTLY_PROCESSING_NODE_PATH.put(username, nodePath);

    ManageableRepository repository = SessionProviderService.getRepository();
    String workspace = node.getSession().getWorkspace().getName();

    boolean isNew = node.isNew();

    DEFAULT_DELAYED_EXECUTOR.execute(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        CURRENTLY_PROCESSING_NODE_PATH.remove(username);

        SessionProvider systemProvider = SessionProviderService.getSystemSessionProvider();
        Session session = systemProvider.getSession(workspace, repository);
        if (!session.itemExists(nodePath)) {
          Thread.sleep(1000);
        }
        Node changedNode = (Node) session.getItem(nodePath);
        Node managedNode = getManagedNode(changedNode);
        if (managedNode == null) {
          return;
        }

        boolean isFile = managedNode.isNodeType("nt:file");

        StatisticData statisticData = new StatisticData();
        statisticData.setModule("Content");
        if (isFile) {
          statisticData.setSubModule("Document");
        } else {
          statisticData.setSubModule("Content");
        }

        String operation = null;
        if (isNew) {
          if (isFile) {
            operation = "fileCreated";
          } else {
            operation = "documentCreated";
          }
        } else {
          if (isFile) {
            operation = "fileUpdated";
          } else {
            operation = "documentUpdated";
          }
        }
        statisticData.setOperation(operation);

        statisticData.setUserId(AnalyticsUtils.getUserIdentityId(username));
        statisticData.addParameter("documentType", managedNode.getPrimaryNodeType().getName());
        addDocumentTitle(managedNode, statisticData);
        addSpaceStatistic(statisticData, nodePath);

        AnalyticsUtils.addStatisticData(statisticData);
      } catch (Exception e) {
        LOG.warn("Error computing wiki statistics", e);
      } finally {
        RequestLifeCycle.end();
        ExoContainerContext.setCurrentContainer(null);
      }
      return;
    });
    return true;
  }

  private void addDocumentTitle(Node managedNode, StatisticData statisticData) throws RepositoryException,
                                                                               UnsupportedEncodingException {
    String title = null;
    if (managedNode.hasProperty("exo:title")) {
      title = managedNode.getProperty("exo:title").getString();
    } else if (managedNode.hasProperty("exo:name")) {
      title = managedNode.getProperty("exo:name").getString();
    } else {
      title = managedNode.getName();
    }
    title = URLDecoder.decode(URLDecoder.decode(title, DEFAULT_CHARSET), DEFAULT_CHARSET);
    statisticData.addParameter("documentName", title);
  }

  private Node getManagedNode(Node changedNode) throws RepositoryException {
    String rootNodePath = changedNode.getSession().getRootNode().getPath();
    Node managedNode = null;
    Node nodeIndex = changedNode;
    do {
      String nodeType = nodeIndex.getPrimaryNodeType().getName();
      if (!nodeIndex.getName().equals("nt:resource")
          && (nodeIndex.isNodeType("nt:file") || getTemplateService().isManagedNodeType(nodeType))) {
        // Found parent managed node
        managedNode = nodeIndex;
      } else {
        // Continue iteration
        if (StringUtils.equals(rootNodePath, nodeIndex.getPath())) {
          nodeIndex = null;
        } else {
          nodeIndex = nodeIndex.getParent();
        }
      }
    } while (managedNode == null && nodeIndex != null);
    return managedNode;
  }

  private void addSpaceStatistic(StatisticData statisticData, String nodePath) {
    if (nodePath.startsWith("/Groups/spaces")) {
      String[] nodePathParts = nodePath.split("/");

      if (nodePathParts.length > 3) {
        String groupId = "/spaces" + nodePathParts[3];
        Space space = getSpaceService().getSpaceByGroupId(groupId);
        if (space != null) {
          statisticData.setSpaceId(Long.parseLong(space.getId()));
          statisticData.addParameter("spaceTemplate", space.getTemplate());
        }
      }
    }
  }

  public TemplateService getTemplateService() {
    if (templateService == null) {
      templateService = this.container.getComponentInstanceOfType(TemplateService.class);
    }
    return templateService;
  }

  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = this.container.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  private static Executor delayedExecutor(long delay, TimeUnit unit) {
    return delayedExecutor(delay, unit, ForkJoinPool.commonPool());
  }

  private static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor) {
    return r -> SCHEDULER.schedule(() -> executor.execute(r), delay, unit);
  }

}
