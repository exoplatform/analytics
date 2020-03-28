<script>
  require(['PORTLET/analytics/StatisticsCollection'], (stats) => {
    stats.init.call(stats,
      <%=request.getAttribute("userSettings")%>,
      <%=request.getAttribute("uiWatchers")%>
    );
  });
</script>
