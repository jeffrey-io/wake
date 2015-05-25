package io.jeffrey.web.stages;

import io.jeffrey.web.sources.BodyFinalizerSource;
import io.jeffrey.web.sources.BodyFinalizerSource.BodyMutator;
import io.jeffrey.web.sources.Source;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This essentially allows a final stage to be done on the body of each source using a BodyMutator
 * @author jeffrey
 */
public class BodyFinalizerStage extends Stage {

  private final Stage       prior;
  private final BodyMutator mutator;

  public BodyFinalizerStage(final Stage prior, final BodyMutator mutator) {
    this.prior = prior;
    this.mutator = mutator;
  }

  @Override
  public Collection<Source> sources() {
    final ArrayList<Source> next = new ArrayList<Source>();
    for (final Source src : prior.sources()) {
      next.add(new BodyFinalizerSource(src, mutator));
    }
    return next;
  }

}
