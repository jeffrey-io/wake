package io.jeffrey.web.stages;

import io.jeffrey.web.sources.ApplyTemplateBodySource;
import io.jeffrey.web.sources.Source;
import io.jeffrey.web.sources.SourceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Split the sources into templates and non-templates; then take the non-templates and utilize the templates (if need be)
 */
public class TemplateCrossStage extends Stage {
   private final Stage priorStage;

   public TemplateCrossStage(Stage priorStage) {
      this.priorStage = priorStage;
   }

   @Override
   public Collection<Source> sources() {
      HashMap<String, Source> templates = new HashMap<>();
      Collection<Source> priorSources = priorStage.sources();
      ArrayList<Source> nonTemplates = new ArrayList<>();
      for (Source source : priorSources) {
         String templateName = source.get("template-name");
         if (templateName != null) {
            templates.put(templateName, source);
         } else {
            nonTemplates.add(source);
         }
      }
      ArrayList<Source> notTemplatesWithTemplatesOrNot = new ArrayList<>();
      for (Source nonTemplate : nonTemplates) {
         String templateToUse = nonTemplate.get("use-template");
         if (templateToUse == null || "$".equals(templateToUse)) {
            // it is both the data AND the template
            notTemplatesWithTemplatesOrNot.add(new ApplyTemplateBodySource(nonTemplate, nonTemplate));
         } else {
            Source template = templates.get(templateToUse);
            if (template == null) {
               throw new SourceException("source specified the template '" + templateToUse + "' that does not exist");
            } else {
               notTemplatesWithTemplatesOrNot.add(new ApplyTemplateBodySource(nonTemplate, template));
            }
         }
      }
      return notTemplatesWithTemplatesOrNot;
   }
}
