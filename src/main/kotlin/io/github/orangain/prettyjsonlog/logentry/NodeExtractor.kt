package io.github.orangain.prettyjsonlog.logentry

import com.fasterxml.jackson.databind.JsonNode

typealias NodeExtractor = (JsonNode) -> JsonNode?

fun buildNodeExtractors(keys: List<String>): List<NodeExtractor> {
    return keys.flatMap { key ->
        buildList {
            add { node: JsonNode -> node.get(key) }
            if (key.contains('.')) {
                val parts = key.split('.')
                add { node: JsonNode ->
                    var currentNode: JsonNode? = node
                    for (part in parts) {
                        currentNode = currentNode?.get(part)
                        if (currentNode == null) {
                            break
                        }
                    }
                    currentNode
                }
            }
        }

    }
}
