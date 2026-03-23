package com.dadadadev.prototype_me.domains.board.sysdesign.impl.domain.usecase

import com.dadadadev.prototype_me.domains.board.sysdesign.api.model.SystemComponentType
import com.dadadadev.prototype_me.domains.board.sysdesign.api.model.SystemDesignNode

class CountPublicComponentsUseCase {
    operator fun invoke(nodes: List<SystemDesignNode>): Int {
        return nodes.count {
            it.componentType == SystemComponentType.CLIENT ||
                    it.componentType == SystemComponentType.API_GATEWAY ||
                    it.componentType == SystemComponentType.CDN
        }
    }
}
