package org.ocpsoft.redoculous.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ocpsoft.redoculous.rest.model.VersionResult;
import org.ocpsoft.redoculous.service.RepositoryService;

public class DocumentServiceImpl implements DocumentService
{
   private static final String UTF8 = "UTF8";

   @Inject
   private RepositoryService rs;

   @Override
   public Response serve(String namespace, String repoName, String refName, String path) throws Exception
   {
      String content = rs.getRenderedContent(namespace, repoName, refName, path);
      if (content != null)
         return Response.ok(content).build();
      return Response.status(404).build();
   }

   @Override
   public Response serveNoTableOfContents(String namespace, String repoName, String refName, String path)
            throws Exception
   {
      String content = rs.getRenderedContent(namespace, repoName, refName, path);
      if (content != null)
      {
         Document document = Jsoup.parse(content, UTF8);
         Element toc = document.getElementById("toc");
         if (toc != null)
         {
            toc.remove();
         }
         return Response.ok(document.toString()).build();
      }
      return Response.status(404).build();
   }

   @Override
   public Response serveTableOfContents(String namespace, String repoName, String refName, String path)
            throws Exception
   {
      String content = rs.getRenderedContent(namespace, repoName, refName, path);
      if (content != null)
      {
         Document document = Jsoup.parse(content, UTF8);
         Element toc = document.getElementById("toc");
         if (toc != null)
            return Response.ok(toc.toString()).build();
         else
            return Response.noContent().status(201).build();
      }
      return Response.status(404).build();
   }

   @Override
   public VersionResult getAvailableVersions(String namespace, String repoName, String filter) throws Exception
   {
      Iterable<String> refs = rs.getRepositoryRefs(namespace, repoName);
      List<String> result = processRefs(refs, filter);
      return new VersionResult(result);
   }

   private List<String> processRefs(Iterable<String> refs, String filter)
   {
      List<String> result = new ArrayList<String>();
      for (String name : refs)
      {
         if (filter == null || filter.isEmpty() || name.matches(filter))
            result.add(name);
      }
      return result;
   }
}
