/*
 * Copyright 2014 Jeffrey M. Barber; see LICENSE for more details
 */
package io.jeffrey.web.stages;

import io.jeffrey.web.sources.ApplyTemplateBodySource;
import io.jeffrey.web.sources.Source;
import io.jeffrey.web.sources.Source.SourceType;
import io.jeffrey.web.sources.SourceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Split the sources into templates and non-templates; then take the non-templates and utilize the templates (if need be)
 * TODO: document annotations needed for this to work
 * TODO: document in a sane way
 */
public class TemplateCrossStage extends Stage {
  private final Stage priorStage;

  public TemplateCrossStage(final Stage priorStage) {
    this.priorStage = priorStage;
  }

  @Override
  public Collection<Source> sources() {
    final HashMap<String, Source> templates = new HashMap<>();
    final Collection<Source> priorSources = priorStage.sources();
    final ArrayList<Source> nonTemplates = new ArrayList<>();
    for (final Source source : priorSources) {
      if (source.getType() == SourceType.Template) {
        templates.put(source.get("template-name"), source);
      } else {
        nonTemplates.add(source);
      }
    }
    final ArrayList<Source> notTemplatesWithTemplatesOrNot = new ArrayList<>();
    for (final Source nonTemplate : nonTemplates) {
      final String templateToUse = nonTemplate.get("use-template");
      if (templateToUse == null || "$".equals(templateToUse)) {
        // it is both the data AND the template
        notTemplatesWithTemplatesOrNot.add(new ApplyTemplateBodySource(nonTemplate, nonTemplate));
      } else {
        final Source template = templates.get(templateToUse);
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
