package com.dadadadev.prototype_me.domains.erd.design.api.data.repository

import com.dadadadev.prototype_me.domains.board.core.api.data.repository.RealtimeBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge

interface ErdBoardRepository : RealtimeBoardRepository<ErdEntityNode, ErdRelationEdge, ErdBoardAction> {
    suspend fun sendNodeDragUpdate(nodeId: String, position: Position)
}
