package com.visualengine.engine;

import java.util.Map;

public class AIReviewer {
    public String classifyDiff(double score, Map<String, Integer> domDiff, Map<String, Object> metadata) {
        // If no LLM is available, we fall back to a purely deterministic Rule-Based engine
        // relying on OpenCV's image score and the JSoup DOM Diff.
        
        if (domDiff == null || domDiff.isEmpty()) {
            // Visual diff exists, but DOM is identical. This means a CSS styling change, 
            // image change, or responsive layout shift occurred.
            return "LAYOUT_OR_STYLE_SHIFT";
        }
        
        int totalDomChanges = domDiff.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalDomChanges < 5 && score > 0.90) {
            // Small visual diff, small DOM diff (e.g., a timestamp or ID changed)
            return "MINOR_DOM_CHANGE";
        }
        
        return "STRUCTURAL_DEFECT";
    }
}
