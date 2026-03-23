package com.dadadadev.prototype_me.domains.board.sysdesign.impl.data.store

import com.dadadadev.prototype_me.domains.board.sysdesign.api.model.SystemDesignNode

class SystemDesignCache {
    private val nodes = linkedMapOf<String, SystemDesignNode>()

    fun upsert(node: SystemDesignNode) {
        nodes[node.id] = node
    }

    fun all(): List<SystemDesignNode> = nodes.values.toList()
}
