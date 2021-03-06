<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />

<portlet:resourceURL var="retrieveSettingsURL">
  <portlet:param name="operation" value="GET_SETTINGS"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveMappingsURL">
  <portlet:param name="operation" value="GET_MAPPINGS"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveFiltersURL">
  <portlet:param name="operation" value="GET_FILTERS"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveChartDataURL">
  <portlet:param name="operation" value="GET_DATA"/>
</portlet:resourceURL>
<portlet:resourceURL var="retrieveFieldValuesURL">
    <portlet:param name="operation" value="GET_FIELD_VALUES"/>
</portlet:resourceURL>
<portlet:actionURL var="saveSettingsURL" />

<div class="VuetifyApp">
  <% int generatedId = (int) (Math.random() * 1000000l); %>
  <div class="analytics-rate-app-parent"
    id="analytics-rate-<%= generatedId %>"
    data-id="<%= generatedId %>"
    data-settings-url="<%=retrieveSettingsURL%>"
    data-mappings-url="<%=retrieveMappingsURL%>"
    data-filters-url="<%=retrieveFiltersURL%>"
    data-chart-data-url="<%=retrieveChartDataURL%>"
    data-field-values-url="<%=retrieveFieldValuesURL%>"
    data-save-settings-url="<%=saveSettingsURL%>">
  </div>
  <script type="text/javascript">
    require(['PORTLET/analytics/AnalyticsRatePortlet'], app => app.init('analytics-rate-<%= generatedId %>'));
  </script>
</div>