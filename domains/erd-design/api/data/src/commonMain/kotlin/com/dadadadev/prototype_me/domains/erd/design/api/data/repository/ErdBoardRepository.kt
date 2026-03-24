package com.dadadadev.prototype_me.domains.erd.design.api.data.repository

import com.dadadadev.prototype_me.domains.board.core.api.data.repository.RealtimeBoardRepository
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge

interface ErdBoardRepository : RealtimeBoardRepository<EntityNode, RelationEdge, ErdBoardAction>
