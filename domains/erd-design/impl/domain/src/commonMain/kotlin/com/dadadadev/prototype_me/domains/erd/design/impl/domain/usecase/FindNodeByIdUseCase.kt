package com.dadadadev.prototype_me.domains.erd.design.impl.domain.usecase

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode

class FindNodeByIdUseCase {
    operator fun invoke(nodes: Map<String, EntityNode>, nodeId: String): EntityNode? = nodes[nodeId]
}

