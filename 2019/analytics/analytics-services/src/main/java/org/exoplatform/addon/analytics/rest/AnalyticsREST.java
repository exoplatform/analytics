package org.exoplatform.addon.analytics.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.addon.analytics.api.service.AnalyticsService;
import org.exoplatform.addon.analytics.model.chart.ChartType;
import org.exoplatform.addon.analytics.model.chart.LineChartData;

@Path("/analytics")
@RolesAllowed("users")
public class AnalyticsREST {

  private AnalyticsService analyticsService;

  public AnalyticsREST(AnalyticsService analyticsService) {
    this.analyticsService = analyticsService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response getChartData(@QueryParam("type") String chartType) {
    LineChartData chartData = this.analyticsService.getChartData(ChartType.valueOf(chartType.toUpperCase()));
    return Response.ok(chartData).build();
  }

}
