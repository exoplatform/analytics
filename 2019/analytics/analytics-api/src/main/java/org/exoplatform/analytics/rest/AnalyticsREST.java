package org.exoplatform.analytics.rest;

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.analytics.api.service.AnalyticsService;
import org.exoplatform.analytics.model.StatisticFieldMapping;
import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.model.chart.ChartDataList;
import org.exoplatform.analytics.model.filter.AnalyticsFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/analytics")
@RolesAllowed("users")
public class AnalyticsREST implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(AnalyticsREST.class);

  private AnalyticsService analyticsService;

  public AnalyticsREST(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getMappings() {
    Set<StatisticFieldMapping> fieldsMapping = analyticsService.retrieveMapping(false);
    return Response.ok(fieldsMapping).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getChartData(AnalyticsFilter filter) {
    if (filter == null) {
      LOG.warn("Empty analytics filter parameter");
      return Response.status(400).build();
    }
    if (filter.getAggregations() != null && !filter.getAggregations().isEmpty()) {
      ChartDataList chartsData = this.analyticsService.compueChartData(filter);
      return Response.ok(chartsData).build();
    } else {
      List<StatisticData> searchResults = this.analyticsService.retrieveData(filter);
      return Response.ok(searchResults).build();
    }
  }

}
