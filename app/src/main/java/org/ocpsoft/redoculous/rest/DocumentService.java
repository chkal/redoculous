package org.ocpsoft.redoculous.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ocpsoft.redoculous.model.Ref;
import org.ocpsoft.redoculous.model.Repository;
import org.ocpsoft.redoculous.service.RepositoryService;

@Path("/v1/serve")
@Produces({ "text/html" })
public class DocumentService
{
   private static final String UTF8 = "UTF8";

   @Inject
   private RepositoryService rs;

   @GET
   public Response serve(
            @QueryParam("repo") String repoName,
            @QueryParam("ref") String refName,
            @QueryParam("path") String path)
            throws Exception
   {
      Repository repository = rs.getRepository(repoName);
      Ref ref = rs.getRef(repository, refName);
      String content = rs.getRenderedPath(ref, path);
      return Response.ok(content).build();
   }

   @GET
   @Path("/toc")
   public Response getTableOfContents(
            @QueryParam("repo") String repoName,
            @QueryParam("ref") String refName,
            @QueryParam("path") String path)
            throws Exception
   {
      Repository repository = rs.getRepository(repoName);
      Ref ref = rs.getRef(repository, refName);
      String content = rs.getRenderedPath(ref, path);
      Document document = Jsoup.parse(content, UTF8);
      Element toc = document.getElementById("toc");
      if (toc != null)
         return Response.ok(toc.toString()).build();
      return Response.noContent().build();
   }

   @GET
   @Path("/versions")
   @Produces({ "application/xml", "application/json" })
   public VersionResult getAvailableVersions(@QueryParam("repo") String repoName,
            @QueryParam("filter") @DefaultValue(".*") String filter)
            throws Exception
   {
      Repository repository = rs.getRepository(repoName);
      Set<Ref> refs = rs.getRefs(repository);
      List<String> result = processRefs(refs, filter);
      return new VersionResult(result);
   }

   private List<String> processRefs(Iterable<Ref> refs, String filter)
   {
      List<String> result = new ArrayList<String>();
      for (Ref ref : refs)
      {
         String name = ref.getName();
         if (filter == null || filter.isEmpty() || name.matches(filter))
            result.add(name);
      }
      return result;
   }

   @XmlRootElement(name = "versions")
   public static class VersionResult
   {
      private List<String> versions;

      public VersionResult()
      {
      }

      public VersionResult(List<String> versions)
      {
         this.versions = versions;
      }

      @XmlElement
      public List<String> getVersions()
      {
         return versions;
      }
   }

}
