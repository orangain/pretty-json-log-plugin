package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

typealias NodeExtractor = (JsonNode) -> JsonNode?
