package org.ocpsoft.redoculous.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jruby.embed.ScriptingContainer;
import org.ocpsoft.common.util.Streams;

@ApplicationScoped
public class AsciidocRenderer implements Renderer
{
   private static final Charset UTF8 = Charset.forName("UTF8");
   private final static String SCRIPT = "require 'asciidoctor'\n" +
            "Asciidoctor.render(input)\n";

   private ScriptingContainer container;

   public AsciidocRenderer()
   {}

   @Inject
   public AsciidocRenderer(ScriptingContainer container)
   {
      this.container = container;
      List<String> loadPaths = Arrays.asList("ruby/asciidoctor/lib");
      container.getLoadPaths().addAll(loadPaths);
   }

   @Override
   public Iterable<String> getSupportedExtensions()
   {
      return Arrays.asList("ad", "adoc", "asciidoc");
   }

   @Override
   public String getName()
   {
      return "asciidoc";
   }

   @Override
   public void render(InputStream inputStream, OutputStream outputStream)
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Streams.copy(inputStream, bos);
      String input = new String(bos.toByteArray(), UTF8);

      container.put("input", input);

      Object output = container.runScriptlet(SCRIPT);
      // write result to the output stream
      try {
         outputStream.write(output.toString().getBytes(UTF8));
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

}