<%@ page import="org.exoplatform.portal.webui.util.Util"%>
<%@ page import="org.exoplatform.portal.webui.portal.UIPortal"%>

<%
  UIPortal currentPortal = Util.getUIPortal();
  String currentURI = currentPortal.getSiteKey().getTypeName() + "/" + currentPortal.getSiteKey().getName() + "/" + currentPortal.getSelectedUserNode().getURI();
  String cacheId = "analyticsDashboardBreadcrumb/" + currentURI;
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="analyticsDashboardBreadcrumb">
    <v-cacheable-dom-app cache-id="<%=cacheId%>"></v-cacheable-dom-app>
    <script type="text/javascript">
            require(['PORTLET/analytics/AnalyticsDashboardBreadcrumb'], app => app.init('<%=cacheId%>'));
        </script>
      <script>
        console.log('=============breadcrumb.jsp============== ');
        console.log('the cache-id should be :  analyticsDashboardBreadcrumb/portal/dw/analytics')
        console.log('this is the cache-id:  <%=cacheId%> ');
      </script>
  </div>
</div>
