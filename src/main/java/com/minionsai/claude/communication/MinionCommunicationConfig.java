package com.minionsai.claude.communication;

import com.minionsai.claude.core.Minion;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Configuration for the messaging system
 */
@Component
public class MinionCommunicationConfig {

  @lombok.RequiredArgsConstructor
  public static class MinionRegistry {
    private final ConcurrentHashMap<String, Minion> minions = new ConcurrentHashMap<>();

    public void registerMinion(Minion minion) {
      minions.put(minion.getId(), minion);
    }

    public Minion getMinion(String id) {
      return minions.get(id);
    }

    public void unregisterMinion(String id) {
      minions.remove(id);
    }
  }
}
