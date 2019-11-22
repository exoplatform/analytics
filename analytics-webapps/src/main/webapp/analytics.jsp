<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:resourceURL var="retrieveSettingsURL">
  <jsp:param name="operation" value="GET_SETTINGS"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveFiltersURL">
  <jsp:param name="operation" value="GET_FILTERS"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveChartDataURL">
  <jsp:param name="operation" value="GET_CHART_DATA"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveChartSamplesURL">
  <jsp:param name="operation" value="GET_CHART_SAMPLES_DATA"/>
</portlet:resourceURL>
<portlet:actionURL var="saveSettingsURL" />

<div class="VuetifyApp">
  <% int generatedId = (int) (Math.random() * 100000); %>
  <div class="analytics-app-parent"
    data-id="<%= generatedId %>"
    data-settings-url="<%=retrieveSettingsURL%>"
    data-filters-url="<%=retrieveFiltersURL%>"
    data-chart-data-url="<%=retrieveChartDataURL%>"
    data-chart-samples-url="<%=retrieveChartSamplesURL%>"
    data-save-settings-url="<%=saveSettingsURL%>">
  </div>
</div>
