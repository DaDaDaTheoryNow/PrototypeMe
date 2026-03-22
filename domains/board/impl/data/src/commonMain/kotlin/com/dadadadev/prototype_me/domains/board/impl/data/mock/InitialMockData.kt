package com.dadadadev.prototype_me.domains.board.impl.data.mock

import com.dadadadev.prototype_me.domain.models.BoardContext
import com.dadadadev.prototype_me.domain.models.EntityNode
import com.dadadadev.prototype_me.domain.models.FieldType
import com.dadadadev.prototype_me.domain.models.NodeField
import com.dadadadev.prototype_me.domain.models.Position
import com.dadadadev.prototype_me.domain.models.RelationEdge
import com.dadadadev.prototype_me.domain.models.RelationType

/**
 * Realistic e-commerce ERD used as initial board state:
 *
 *   User ──1:N──> Order ──1:N──> OrderItem <──1:N── Product
 *
 * All edges connect at the field level (FK references).
 */
object InitialMockData {
    fun create(): BoardContext {
        val nodes = mapOf(
            "user" to EntityNode(
                id = "user", name = "User", position = Position(60f, 80f),
                fields = listOf(
                    NodeField("u_id",    "id",        FieldType.NUMBER),
                    NodeField("u_email", "email",     FieldType.TEXT),
                    NodeField("u_role",  "role",      FieldType.TEXT),
                )
            ),
            "order" to EntityNode(
                id = "order", name = "Order", position = Position(340f, 80f),
                fields = listOf(
                    NodeField("o_id",        "id",        FieldType.NUMBER),
                    NodeField("o_userId",    "userId",    FieldType.NUMBER),
                    NodeField("o_status",    "status",    FieldType.TEXT),
                    NodeField("o_createdAt", "createdAt", FieldType.DATE),
                )
            ),
            "product" to EntityNode(
                id = "product", name = "Product", position = Position(640f, 80f),
                fields = listOf(
                    NodeField("p_id",    "id",    FieldType.NUMBER),
                    NodeField("p_name",  "name",  FieldType.TEXT),
                    NodeField("p_price", "price", FieldType.NUMBER),
                )
            ),
            "order_item" to EntityNode(
                id = "order_item", name = "OrderItem", position = Position(490f, 320f),
                fields = listOf(
                    NodeField("oi_orderId",   "orderId",   FieldType.NUMBER),
                    NodeField("oi_productId", "productId", FieldType.NUMBER),
                    NodeField("oi_qty",       "qty",       FieldType.NUMBER),
                )
            ),
        )

        val edges = mapOf(
            // User.id ──1:N──> Order.userId
            "e_user_order" to RelationEdge(
                id = "e_user_order",
                sourceNodeId = "user",  sourceFieldId = "u_id",
                targetNodeId = "order", targetFieldId = "o_userId",
                type = RelationType.ONE_TO_MANY
            ),
            // Order.id ──1:N──> OrderItem.orderId
            "e_order_item" to RelationEdge(
                id = "e_order_item",
                sourceNodeId = "order",      sourceFieldId = "o_id",
                targetNodeId = "order_item", targetFieldId = "oi_orderId",
                type = RelationType.ONE_TO_MANY
            ),
            // Product.id ──1:N──> OrderItem.productId
            "e_product_item" to RelationEdge(
                id = "e_product_item",
                sourceNodeId = "product",    sourceFieldId = "p_id",
                targetNodeId = "order_item", targetFieldId = "oi_productId",
                type = RelationType.ONE_TO_MANY
            ),
        )

        return BoardContext("board_1", nodes, edges)
    }
}
