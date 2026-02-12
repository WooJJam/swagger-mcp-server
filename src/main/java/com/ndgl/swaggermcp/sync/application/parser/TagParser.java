package com.ndgl.swaggermcp.sync.application.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.ndgl.swaggermcp.sync.dto.ParsedTag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TagParser {

    public List<ParsedTag> parseTags(final JsonNode swaggerJson) {
        log.info("Tags 파싱 시작");
        final List<ParsedTag> tags = new ArrayList<>();

        final JsonNode tagsNode = swaggerJson.path("tags");
        if (tagsNode.isMissingNode() || !tagsNode.isArray()) {
            log.warn("Swagger JSON에 'tags' 필드가 없습니다");
            return tags;
        }

        tagsNode.forEach(tagNode -> {
            final String name = tagNode.path("name").asText("");
            final String description = tagNode.path("description").asText("");

            if (!name.isEmpty()) {
                final ParsedTag tag = new ParsedTag(name, description);
                tags.add(tag);
                log.debug("Tag 파싱 완료: {}", name);
            }
        });

        log.info("Tags 파싱 완료: {}개", tags.size());
        return tags;
    }
}
