package org.exoplatform.addon.analytics.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.addon.analytics.api.service.AnalyticsService;
import org.exoplatform.addon.analytics.model.AnalyticsFilter;
import org.exoplatform.addon.analytics.model.ChartData;

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
  public Response getChartData() {
    AnalyticsFilter filter = new AnalyticsFilter();
    ChartData chartData = this.analyticsService.getChartData(filter);
    return Response.ok(chartData).build();
  }

}
