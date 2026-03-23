package com.dadadadev.prototype_me.domains.erd.design.api.data.repository

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.BoardAction
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.EntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.RelationEdge
import com.dadadadev.prototype_me.domains.board.core.api.data.repository.RealtimeBoardRepository

interface BoardRepository : RealtimeBoardRepository<EntityNode, RelationEdge, BoardAction>


