package com.fraternityos.server.responsibility.domain;

/**
 * FIXED responsibilities keep a single assignee; ROTATING responsibilities are
 * cycled through the eligible member pool by the scheduled worker.
 */
public enum ResponsibilityType {
    FIXED,
    ROTATING
}
