<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:resourceURL var="retrieveSettingsURL" />
<portlet:actionURL var="saveSettingsURL" />

<div class="VuetifyApp">
  <% int generatedId = (int) (Math.random() * 100000); %>
  <div class="analytics-app-parent" data-id="<%= generatedId %>" data-settings-url="<%=retrieveSettingsURL%>" data-save-settings-url="<%=saveSettingsURL%>"></div>
</div>
