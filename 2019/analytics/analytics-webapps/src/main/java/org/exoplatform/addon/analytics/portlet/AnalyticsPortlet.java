package org.exoplatform.addon.analytics.portlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Set;

import javax.portlet.*;

import org.json.JSONException;
import org.json.JSONObject;

public class AnalyticsPortlet extends GenericPortlet {

  @Override
  protected void doView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/analytics.jsp");
    dispatcher.forward(request, response);
  }

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
    PortletPreferences preferences = request.getPreferences();

    Enumeration<String> parameterNames = request.getParameterNames();
    if (parameterNames != null && parameterNames.hasMoreElements()) {
      while (parameterNames.hasMoreElements()) {
        String name = parameterNames.nextElement();
        String paramValue = request.getParameter(name);
        preferences.setValue(name, paramValue);
      }
    }

    preferences.store();
    response.setPortletMode(PortletMode.VIEW);
  }

  @Override
  public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
    JSONObject jsonResponse = new JSONObject();

    PortletPreferences preferences = request.getPreferences();
    if (preferences != null) {
      Set<Entry<String, String[]>> preferencesEntries = preferences.getMap().entrySet();
      for (Entry<String, String[]> entry : preferencesEntries) {
        try {
          jsonResponse.put(entry.getKey(),
                           entry.getValue() == null || entry.getValue().length == 0 ? "" : entry.getValue()[0]);
        } catch (JSONException e) {
          throw new PortletException("Error getting portlet preferences", e);
        }
      }
    }

    response.setContentType("application/json");
    response.getWriter().write(jsonResponse.toString());
    super.serveResource(request, response);
  }
}
