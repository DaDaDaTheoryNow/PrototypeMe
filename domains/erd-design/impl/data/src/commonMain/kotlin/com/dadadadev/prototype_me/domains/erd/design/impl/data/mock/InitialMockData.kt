package com.dadadadev.prototype_me.domains.erd.design.impl.data.mock

import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdEntityNode
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdBoardContext
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.FieldType
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdNodeField
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.Position
import com.dadadadev.prototype_me.domains.erd.design.api.domain.model.ErdRelationEdge

/**
 * Realistic e-commerce ERD used as initial board state:
 *
 * User -> Order -> OrderItem <- Product
 */
internal object InitialMockData {
    fun create(): ErdBoardContext {
        val nodes = mapOf(
            "user" to ErdEntityNode(
                id = "user",
                name = "User",
                position = Position(60f, 80f),
                fields = listOf(
                    ErdNodeField("u_id", "id", FieldType.NUMBER),
                    ErdNodeField("u_email", "email", FieldType.TEXT),
                    ErdNodeField("u_role", "role", FieldType.TEXT),
                ),
            ),
            "order" to ErdEntityNode(
                id = "order",
                name = "Order",
                position = Position(340f, 80f),
                fields = listOf(
                    ErdNodeField("o_id", "id", FieldType.NUMBER),
                    ErdNodeField("o_userId", "userId", FieldType.NUMBER),
                    ErdNodeField("o_status", "status", FieldType.TEXT),
                    ErdNodeField("o_createdAt", "createdAt", FieldType.DATE),
                ),
            ),
            "product" to ErdEntityNode(
                id = "product",
                name = "Product",
                position = Position(640f, 80f),
                fields = listOf(
                    ErdNodeField("p_id", "id", FieldType.NUMBER),
                    ErdNodeField("p_name", "name", FieldType.TEXT),
                    ErdNodeField("p_price", "price", FieldType.NUMBER),
                ),
            ),
            "order_item" to ErdEntityNode(
                id = "order_item",
                name = "OrderItem",
                position = Position(490f, 320f),
                fields = listOf(
                    ErdNodeField("oi_id", "id", FieldType.NUMBER),
                    ErdNodeField("oi_orderId", "orderId", FieldType.NUMBER),
                    ErdNodeField("oi_productId", "productId", FieldType.NUMBER),
                    ErdNodeField("oi_qty", "qty", FieldType.NUMBER),
                ),
            ),
        )

        val edges = mapOf(
            "e_user_order" to ErdRelationEdge(
                id = "e_user_order",
                sourceNodeId = "user",
                sourceFieldId = "u_id",
                targetNodeId = "order",
                targetFieldId = "o_userId",
            ),
            "e_order_item" to ErdRelationEdge(
                id = "e_order_item",
                sourceNodeId = "order",
                sourceFieldId = "o_id",
                targetNodeId = "order_item",
                targetFieldId = "oi_orderId",
            ),
            "e_product_item" to ErdRelationEdge(
                id = "e_product_item",
                sourceNodeId = "product",
                sourceFieldId = "p_id",
                targetNodeId = "order_item",
                targetFieldId = "oi_productId",
            ),
        )

        return ErdBoardContext(boardId = "board_1", nodes = nodes, edges = edges)
    }
}

